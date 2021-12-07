package com.via.ecza.service;

import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.repo.*;
import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RefundAcceptanceService {
    @Autowired
    private PreDepotRepository preDepotRepository;
    @Autowired
    private PreDepotStatusRepository preDepotStatusRepository;
    @Autowired
    private RefundRepository refundRepository;
    @Autowired
    private RefundStatusRepository refundStatusRepository;
    @Autowired
    private PreRefundRepository preRefundRepository;
    @Autowired
    private PreRefundStatusRepository preRefundStatusRepository;
    @Autowired
    private QrCodeRepository qrCodeRepository;
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private CustomerSupplyOrderRepository customerSupplyOrderRepository;
    @Autowired
    private CustomerOrderDrugsRepository customerOrderDrugsRepository;
    @Autowired
    private CustomerOrderStatusRepository customerOrderStatusRepository;
    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private DepotRepository depotRepository;
    @Autowired
    private DepotStatusRepository depotStatusRepository;
    @Autowired
    private CustomerSupplyStatusRepository customerSupplyStatusRepository;
    @Autowired
    private PurchaseOrderDrugsRepository purchaseOrderDrugsRepository;
    @Autowired
    private PurchaseStatusRepository purchaseStatusRepository;
    @Autowired
    private CommunicationRepository communicationRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ControlService controlService;
    @Autowired
    private UserCameraRepository userCameraRepository;

    public Page<RefundAcceptanceDto> search(RefundAcceptanceSearchDto dto, Integer pageNo, Integer pageSize, String sortBy) {
        StringBuilder createSqlQuery = new StringBuilder("select * from refund r inner join supplier s on r.supplier_id=s.supplier_id inner join drug_card dc on dc.drug_card_id=r.drug_card_id where r.refund_status_id=10 ");


        if (dto.getRefundOrderNo() != null && dto.getRefundOrderNo().trim().length() > 0)
            createSqlQuery.append(" and  r.refund_order_no ILIKE '%" + dto.getRefundOrderNo().trim() + "%' ");
        if (dto.getSupplierId() != null)
            createSqlQuery.append(" and r.supplier_id = " + dto.getSupplierId() + " ");
        if (dto.getDrugCardId() != null)
            createSqlQuery.append(" and r.drug_card_id = " + dto.getDrugCardId() + " ");

        if (dto.getDrugCode() != null)
            createSqlQuery.append(" and dc.drug_code = " + dto.getDrugCode() + " ");

        if (dto.getSupplierName() != null && dto.getSupplierName().trim().length() > 0)
            createSqlQuery.append(" and s.supplier_name ILIKE '%" + dto.getSupplierName().trim() + "%' ");


        createSqlQuery.append(" order by r.refund_id ");
        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Refund.class).getResultList();

        RefundAcceptanceDto[] dtos = mapper.map(list, RefundAcceptanceDto[].class);
        List<RefundAcceptanceDto> dtosList = Arrays.asList(dtos);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        int start = Math.min((int) paging.getOffset(), dtosList.size());
        int end = Math.min((start + paging.getPageSize()), dtosList.size());

        Page<RefundAcceptanceDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, dtosList.size());

        return pageList;

    }

    public List<PreRefundDto> editAcceptance(PreRefundSearchDto dto,String authHeader) throws NotFoundException, ParseException {

        try {

            Optional<Refund> optionalRefundOrder = refundRepository.findById(dto.getRefundId());
            if (!optionalRefundOrder.isPresent()) {
                throw new NotFoundException("Böyle bir iade siparişi yoktur..");
            }

            //Karekod Anlamlı Hale Getirilip Pre Refund'a Kaydediliyor
            Boolean seperateQrCode = seperateQrCode(optionalRefundOrder.get(),authHeader);
            if (seperateQrCode != true) {
                throw new NotFoundException("Karekod Bölünemedi..");
            }


            List<PreRefund> list = preRefundRepository.getAllWithRefundId(dto.getRefundId());
            PreRefundDto[] array = mapper.map(list, PreRefundDto[].class);
            List<PreRefundDto> dtoList = Arrays.asList(array);

            //Depodan veya Stoktan Karışan İlaçlar 1 Kez Gösterilecek Sonra Silinecek
            int deleteResult = preRefundRepository.deletePreRefundStatus5_6(dto.getRefundId());

            return dtoList;
        } catch (NotFoundException e) {
            throw e;
        } catch (NumberFormatException e) {
            throw e;
        } catch (ParseException e) {
            throw e;
        }

    }

    public boolean seperateQrCode(Refund refundOrder,String authHeader) throws ParseException, NotFoundException {

        try {

            //Kullanıcı Bilgisi Alındı
            User user = controlService.getUserFromToken(authHeader);

            //Veritabanında Kayıt Var mı Kontrolü Yapıldı
            Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);
            if(!optUserCamera.isPresent()){
                throw new Exception("Kullanıcı Kamera Kaydı Bulunamadı !");
            }

            List<QrCode> qrCodeList = qrCodeRepository.findAllQrCodeForOrderAcceptance(optUserCamera.get().getCameraType());


            String qrCode;
            StringBuilder barcode = new StringBuilder();
            StringBuilder serialNo = new StringBuilder();
            StringBuilder expirationDate = new StringBuilder();
            StringBuilder lotNo = new StringBuilder();
            StringBuilder itsNo = new StringBuilder();

            int secondGroupSeperatorIndex = 0;

            for (QrCode QRCode : qrCodeList) {
                //karekod alındı
                qrCode = QRCode.getQrCode();

                if (qrCode == null || qrCode.isEmpty()) {
                    throw new NotFoundException("Karekod Bilgisi Bulunamadı");
                }

                //İlaç Barkodu Alındı.
                if (qrCode.charAt(0) == '0' && qrCode.charAt(1) == '1') {
                    barcode.append(qrCode.substring(2, 16));
                    // System.out.println("İlaç Barkod: " + barcode.toString());
                }

                //İlaç Seri Numarası Alındı.
                if (qrCode.charAt(16) == '2' && qrCode.charAt(17) == '1') {
                    secondGroupSeperatorIndex = qrCode.indexOf("&", 18);
                    serialNo.append(qrCode.substring(18, secondGroupSeperatorIndex));

                    //  System.out.println("İlaç Seri No: " + serialNo.toString());
                }

                //İlaç SKT Alındı.
                if (qrCode.charAt(secondGroupSeperatorIndex + 1) == '1' && qrCode.charAt(secondGroupSeperatorIndex + 2) == '7') {
                    expirationDate.append("20");
                    expirationDate.append(qrCode.substring(secondGroupSeperatorIndex + 3, secondGroupSeperatorIndex + 5));
                    expirationDate.append("-");
                    expirationDate.append(qrCode.substring(secondGroupSeperatorIndex + 5, secondGroupSeperatorIndex + 7));
                    expirationDate.append("-");
                    expirationDate.append(qrCode.substring(secondGroupSeperatorIndex + 7, secondGroupSeperatorIndex + 9));

                    //System.out.println("İlaç SKT: " + expirationDate.toString());
                }

                //İlaç Parti Numarası Alındı.
                if (qrCode.charAt(secondGroupSeperatorIndex + 9) == '1' && qrCode.charAt(secondGroupSeperatorIndex + 10) == '0') {
                    lotNo.append(qrCode.substring(secondGroupSeperatorIndex + 11, qrCode.length()));

                    // System.out.println("İlaç Parti No: " + lotNo.toString());
                }

                //İTS No Alındı.
                if (barcode.length() > 0 && serialNo.length() > 0) {
                    itsNo.append(barcode + "21" + serialNo);

                    // System.out.println("İts No: " + itsNo.toString());
                }

                PreRefundDto preRefund = new PreRefundDto();

                preRefund.setDrugBarcode(Long.valueOf(barcode.toString()));
                preRefund.setDrugSerialNo(serialNo.toString());


                Date exp = new SimpleDateFormat("yyyy-MM-dd").parse(expirationDate.toString());
                preRefund.setDrugExpirationDate(exp);


                preRefund.setDrugLotNo(lotNo.toString());
                preRefund.setDrugItsNo(itsNo.toString());


                preRefund.setRefund(mapper.map(refundOrder, RefundAcceptanceDto.class));


                Optional<DrugCard> drug = drugCardRepository.findByDrugCode(preRefund.getDrugBarcode());
                preRefund.setDrugName(drug.get().getDrugName());


                if (refundOrder.getDrugCard().getDrugCardId() == drug.get().getDrugCardId()) {
                    if (exp.after(refundOrder.getExpirationDate()) || (exp.getMonth() == refundOrder.getExpirationDate().getMonth() && exp.getYear() == refundOrder.getExpirationDate().getYear())) {

                        preRefund.setPreRefundStatus(preRefundStatusRepository.findById((long) 2).get());
                        // System.out.println("Şart Sağlandı");
                    } else {
                        preRefund.setPreRefundStatus(preRefundStatusRepository.findById((long) 3).get());
                        // System.out.println("Şart Sağlanmadı");
                    }
                } else {
                    preRefund.setPreRefundStatus(preRefundStatusRepository.findById((long) 4).get());
                    //System.out.println("Yanlış İlaç");
                }


                //preRefund tablosuna yeni kayıt olarak eklendi.
                preRefundRepository.save(mapper.map(preRefund, PreRefund.class));


                //preRefund'a eklenen kayıt QrCode tablosunda status degiştirdi.
                QRCode.setStatus(1);
                qrCodeRepository.save(QRCode);

                barcode.setLength(0);
                serialNo.setLength(0);
                expirationDate.setLength(0);
                lotNo.setLength(0);
                itsNo.setLength(0);
            }



        } catch (ParseException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public String accept(RefundAcceptanceAcceptDto dtoRefundAcceptance) throws Exception {
        try {
            DepotDto depot = null;
            Refund refundOrder = null;
            List<RefundAcceptanceCheckedListDto> dtoList = dtoRefundAcceptance.getCheckedList();
            if (dtoList.size() > 0) {
                int numarator = 0;//seçili olan ilaç sayısı

                for (RefundAcceptanceCheckedListDto list : dtoList) {
                    if (list != null && (preRefundRepository.findById(list.getPreRefundId()).get()).getPreRefundStatus().getPreRefundStatusId() != 4 //İlaç Hatalı Değil İse//
                    ) {
                        //preRefund bilgisinden sipariş bilgisi alındı
                        PreRefund preRefundv2 = preRefundRepository.findById((dtoList.get(dtoList.indexOf(list))).getPreRefundId()).get();
                        refundOrder = preRefundv2.getRefund();
                        break;
                    }
                }

                if (refundOrder == null) {
                    return "Lütfen Hatalı İlacı Çıkarınız.";
                }

                if (refundOrder != null && (refundOrder.getRefundStatus().getRefundStatusId() != 10)) {
                    return "İade İşlemi Sonlandırılan Siparişe İlaç Kabul Edilemez!";
                } else {
                    String result = acceptForStock(dtoList, refundOrder);
                    return result;
                }

            }
            return "İlaç Bulunamadı";
        } catch (Exception e) {
            throw e;
        }
    }

    //İlaç Stoktan İade Ediliyor
    public String acceptForStock(List<RefundAcceptanceCheckedListDto> dtoList, Refund refundOrder) throws Exception {
        try {
            Depot drugInStock;
            int numarator = 0;

            Integer countOfDrugsOnlyDepotForRefund = depotRepository.countOfDrugsOnlyDepotForRefund(refundOrder.getRefundId());


            for (RefundAcceptanceCheckedListDto list : dtoList) {


                if (list != null && list.getValue() == true) {
                    numarator++;
                    PreRefund dto = (preRefundRepository.findById(list.getPreRefundId()).get());

                    //stokta bulunan ilaç bulundu
                    drugInStock = new Depot();
                    Optional<Depot> optionalDepot = depotRepository.findByItsNo(dto.getDrugItsNo());


                    if (!optionalDepot.isPresent()) {
                        numarator--;
                        //Teslimat Tam Alınırsa Sipariş Statusu Tam Olarak değişsin
                        if (countOfDrugsOnlyDepotForRefund - numarator == 0) {
                            refundOrder.setRefundStatus(refundStatusRepository.findById((long) 40).get());//sipariş hazırlandı ve yola çıktı
                            refundRepository.save(refundOrder);
                        }

                        PreRefund changeStatusPreRefund = preRefundRepository.findById(dto.getPreRefundId()).get();
                        changeStatusPreRefund.setPreRefundStatus(preRefundStatusRepository.findById((long) 6).get());
                        preRefundRepository.save(changeStatusPreRefund);
                        continue;//döngü sıradakinden devam etsin

                        //return dto.getDrugSerialNo() + " seri numaralı ilaç stok kayıtlarında yoktur. <br/> Lütfen Hatalı İlacı Çıkarınız";
                    } else if (optionalDepot.isPresent()) {
                        drugInStock = optionalDepot.get();
                    }

                    if (drugInStock.getDepotStatus().getDepotStatusId() == 15
                            && refundOrder.getRefundId() == drugInStock.getRefund().getRefundId()) {


                        drugInStock.setSendingDate(new Date());
                        drugInStock.setDepotStatus(depotStatusRepository.findById((long) 90).get());
                    } else if (drugInStock.getDepotStatus().getDepotStatusId() == 15
                            && refundOrder.getRefundId() != drugInStock.getRefund().getRefundId()) {
                        numarator--;
                        //Teslimat Tam Alınırsa Sipariş Statusu Tam Olarak değişsin
                        if (countOfDrugsOnlyDepotForRefund - numarator == 0) {
                            refundOrder.setRefundStatus(refundStatusRepository.findById((long) 40).get());//sipariş hazırlandı ve yola çıktı
                            refundRepository.save(refundOrder);
                        }

                        PreRefund changeStatusPreRefund = preRefundRepository.findById(dto.getPreRefundId()).get();
                        changeStatusPreRefund.setPreRefundStatus(preRefundStatusRepository.findById((long) 5).get());
                        preRefundRepository.save(changeStatusPreRefund);
                        continue;//döngü sıradakinden devam etsin
                        // return dto.getDrugSerialNo() + " seri numaralı ilaç bu satın almanın ilacı değildir. <br/> Lütfen Bu İlacı Stoğa Teslim Ediniz.";
                    } else if (drugInStock.getDepotStatus().getDepotStatusId() != 15) {
                        numarator--;
                        //Teslimat Tam Alınırsa Sipariş Statusu Tam Olarak değişsin
                        if (countOfDrugsOnlyDepotForRefund - numarator == 0) {
                            refundOrder.setRefundStatus(refundStatusRepository.findById((long) 40).get());//sipariş hazırlandı ve yola çıktı
                            refundRepository.save(refundOrder);
                        }

                        PreRefund changeStatusPreRefund = preRefundRepository.findById(dto.getPreRefundId()).get();
                        changeStatusPreRefund.setPreRefundStatus(preRefundStatusRepository.findById((long) 5).get());
                        preRefundRepository.save(changeStatusPreRefund);
                        continue;//döngü sıradakinden devam etsin
                        //return dto.getDrugSerialNo() + " seri numaralı ilaç bu satın almanın ilacı değildir. <br/> Lütfen Bu İlacı Eski Yerine Teslim Ediniz.";
                    }


                    //HATALI İLAÇ KONTROLÜ BAŞLANGIÇ
                    if (dto.getPreRefundStatus().getPreRefundStatusId() == 4) {
                        numarator--;

                        if (dtoList.indexOf(list) + 1 < dtoList.size()) {//liste sonu değil ise
                            for (int i = dtoList.indexOf(list) + 1; i < dtoList.size(); i++) {
                                //listenin geri kalanında seçili başka ilaç varsa ve hatalı ilaç değil ise sipariş durumları değişmesin
                                if (dtoList.get(i).getValue() == true) {
                                    if (preRefundRepository.findById(dtoList.get(i).getPreRefundId()).get().getPreRefundStatus().getPreRefundStatusId() != 4) {
                                        preRefundRepository.deleteById(dto.getPreRefundId());
                                        DrugCard wrongDrug = drugCardRepository.findByDrugCode(dto.getDrugBarcode()).get();
                                        return "Lütfen Hatalı İlacı Stoktaki Eski Yerine İade Ediniz: <br/> İlaç Adı: " + wrongDrug.getDrugName() + " <br/> Seri No: " + dto.getDrugSerialNo();
                                    }
                                }
                            }
                        }
                        //Aksi Durumda Sipariş Açık Kalsın mı Kapatılsın mı?
                        if (countOfDrugsOnlyDepotForRefund - numarator > 0) {

                            // ÖNEMLİ ==> Front kısmında ilk 3 "*" kontrol edilip ona göre yeni seçenek sunuluyor
                            /*return "*** Lütfen Listede Kalan Hatalı İlaçları Stoktaki Eski Yerine İade Ediniz. ***<br/>Diğer İlaçların Depodan Çıkışı Yapıldı. <br/><br/>Siparişte Eksik İlaç Bulunmaktadır.<br/><br/> Siparişte İstenen Miktar: " + refundOrder.getTotality() + " <br/> Çıkış" +
                                    " Yapılan Miktar: " + (countOfDrugsOnlyDepotForRefund-numarator) + " <br/> Sipariş Bu Haliyle Sonlandırılsın mı?";*/

                            return "Lütfen Listede Kalan Hatalı İlaçları Stoktaki Eski Yerine İade Ediniz.<br/>Diğer İlaçların Depodan Çıkışı Yapıldı. <br/><br/>Siparişte Eksik İlaç Bulunmaktadır.<br/><br/> Siparişte İstenen Miktar: " + refundOrder.getTotality() + " <br/> Çıkış" +
                                    " Yapılan Miktar: " + (refundOrder.getTotality() - (countOfDrugsOnlyDepotForRefund - numarator));


                        } else if (countOfDrugsOnlyDepotForRefund - numarator == 0) {
                            //Teslimat Tam Alınırsa Sipariş Statusu Tam Olarak değişsin

                            refundOrder.setRefundStatus(refundStatusRepository.findById((long) 40).get());//sipariş hazırlandı ve yola çıktı
                            refundRepository.save(refundOrder);

                            return "Lütfen Listede Kalan Hatalı İlaçları Stoktaki Eski Yerine İade Ediniz.<br/>Diğer İlaçların Depodan Çıkışı Yapıldı.<br/>Sipariş Durumu Tamamalandı Olarak Güncellendi ve Sipariş İade İşlemine Kapatıldı";
                        }
                    } //HATALI İLAÇ KONTROLÜ BİTİŞ


                    if (countOfDrugsOnlyDepotForRefund - numarator >= 0) {
                        depotRepository.save(drugInStock);
                        dto.setPreRefundStatus(preRefundStatusRepository.findById((long) 1).get());
                        preRefundRepository.save(dto);

                    } else if (countOfDrugsOnlyDepotForRefund - numarator < 0) {
                        //Teslimat Tam Alınırsa Sipariş Statusu Tam Olarak değişsin
                        if (countOfDrugsOnlyDepotForRefund - numarator == 0) {
                            refundOrder.setRefundStatus(refundStatusRepository.findById((long) 40).get());//sipariş hazırlandı ve yola çıktı
                            refundRepository.save(refundOrder);
                        }
                        return "Okutulan İlaç Sayısı, Sipariş Miktarından Fazla Olamaz." +
                                "<br/> Bu Siparişte İstenen Miktardan Fazla İlaç Bulunmaktadır." +
                                "<br/> Lütfen Listede Kalan İlaçları Stoktaki Eski Yerine İade Ediniz.<br/><br/>" +
                                "Sipariş Durumu Tamamlandı Olarak Güncellendi ve Sipariş İade İşlemine Kapatıldı.";
                    }


                }

            }

            if (refundOrder != null) {
                if (countOfDrugsOnlyDepotForRefund - numarator > 0) {

                    // ÖNEMLİ ==> Front kısmında ilk 3 "*" kontrol edilip ona göre yeni seçenek sunuluyor
                    /*return "*** Siparişte Eksik İlaç Bulunmaktadır. *** <br/><br/> Siparişte İstenen Miktar: " + refundOrder.getTotality() + " <br/> Depodan" +
                            " Çıkışı Yapılan Miktar: " + (countOfDrugsOnlyDepotForRefund-numarator) + " <br/> Sipariş Bu Haliyle Sonlandırılsın mı?";*/

                    return "Siparişte Eksik İlaç Bulunmaktadır.<br/><br/> Siparişte İstenen Miktar: " + refundOrder.getTotality() + " <br/> Depodan" +
                            " Çıkışı Yapılan Miktar: " + (refundOrder.getTotality() - (countOfDrugsOnlyDepotForRefund - numarator));

                } else if (countOfDrugsOnlyDepotForRefund - numarator == 0) {
                    //Teslimat Tam Alınırsa Sipariş Statusu Tam Olarak değişsin

                    refundOrder.setRefundStatus(refundStatusRepository.findById((long) 50).get());//sipariş tamamlandı
                    refundRepository.save(refundOrder);

                    return "Sipariş Durumu Tamamlandı Olarak Güncellendi ve Sipariş İade İşlemine Kapatıldı";
                }
            }

            return "İlaçların Çıkış İşlemi Yapıldı";

        } catch (Exception e) {
            throw e;
        }
    }

    public String clearPreRefund(PreRefundSearchDto dto) throws Exception {
        try {
            StringBuilder createSqlQuery = new StringBuilder("delete from pre_refund pr where pr.refund_id = " + dto.getRefundId() + " and pr.pre_refund_status_id!=1");
            int countOfDeletedElements = entityManager.createNativeQuery(createSqlQuery.toString(), PreDepot.class).executeUpdate();
            return "Listede Bulunan " + countOfDeletedElements + " Adet Kayıt Başarıyla Silindi";
        } catch (Exception e) {
            throw e;
        }
    }

    public int controlCommunicationStatus(CommunicationSearchForRefundAcceptanceDto comSearchDto,String authHeader) throws Exception {
        int result = 3;//communication boş ise 3 dönecek
        try {

            //Kullanıcı Bilgisi Alındı
            User user = controlService.getUserFromToken(authHeader);

            //Veritabanında Kayıt Var mı Kontrolü Yapıldı
            Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);

            if (!optUserCamera.isPresent()) {
                throw new Exception("Kullanıcı Kamera Kaydı Bulunamadı !");
            }

            Refund ro = refundRepository.findById(comSearchDto.getRefundId()).get();

            StringBuilder createSqlQuery = new StringBuilder("select * from communication c " +
                    "where c.barcode=" + ro.getDrugCard().getDrugCode() + " " +
                    "and c.expiration_date='" + ro.getExpirationDate() + "' " +
                    "and c.camera_type=" + optUserCamera.get().getCameraType() +
                    "and c.status=1 "+
                    "and c.total_quantity=" + ro.getTotality());

            List<Communication> list = entityManager.createNativeQuery(createSqlQuery.toString(), Communication.class).getResultList();

            if (list.size() > 0) {
                if (list.get(0).getStatus() == 0) {
                    result = 0;//okuma işlemi başlamadı
                } else if (list.get(0).getStatus() == 1) {
                    result = 1;//okuma işlemi bitti
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return result;
    }

    public int saveQrCode(QrCodeSaveDto dto,String authHeader) throws NotFoundException {

        //Kullanıcı Bilgisi Alındı
        User user = controlService.getUserFromToken(authHeader);

        //Veritabanında Kayıt Var mı Kontrolü Yapıldı
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);

        dto.setQrcode(dto.getQrcode().trim());
        //QR CODE VERİTABANI KAYIT BAŞLANGIÇ
        QrCode qrCodeSave = new QrCode();
        qrCodeSave.setCameraType(optUserCamera.get().getCameraType());
        qrCodeSave.setQrCode(dto.getQrcode());
        qrCodeSave.setStatus(0);
        qrCodeRepository.save(qrCodeSave);
        //QR CODE VERİTABANI KAYIT SON
        return 1;
    }

    public String saveManualAcceptanceQrCodes(RefundAcceptanceManualDto dto) throws Exception {
        if (dto.getRefundId() == null || dto.getQuantity() == null || dto.getStt() == null) {
            throw new Exception("Manuel İade İşleminde Eksik Bilgi Bulunmaktadır !");
        }

        Optional<Refund> optRefund = refundRepository.findById(dto.getRefundId());
        if (!optRefund.isPresent()) {
            throw new Exception("İade Siparişi Bulunamadı !");
        }


        if (optRefund.get() != null && optRefund.get().getRefundStatus().getRefundStatusId() != 10) {
            return "İade İşlemi Sonlandırılan Siparişten İlaç Çıkışı Yapılamaz!";
        }

        Integer countOfDrugsOnlyDepotForRefund = depotRepository.countOfDrugsOnlyDepotForRefund(dto.getRefundId());

        if (countOfDrugsOnlyDepotForRefund <= 0) {
            return "Bu Siparişte İade Edilecek İlaç Bulunamadı !";

        } else if (dto.getStt().after(optRefund.get().getExpirationDate()) || (dto.getStt().getMonth() == optRefund.get().getExpirationDate().getMonth() && dto.getStt().getYear() == optRefund.get().getExpirationDate().getYear())) {
            int numarator = 0;
            Depot depot;

            List<Depot> depotList = depotRepository.DrugListOnlyDepotForRefund(dto.getRefundId());


            for (int i = 0; i < dto.getQuantity(); i++) {
                depot = new Depot();
                numarator++;
                if (countOfDrugsOnlyDepotForRefund - numarator >= 0) {
                    depot = depotList.get(i);

                    /* Depo Durumu Değişiyor */
                    depot.setSendingDate(new Date());
                    depot.setDepotStatus(depotStatusRepository.findById(90L).get());
                    depotRepository.save(depot);

                } else {
                    numarator--;
                    //iade siparişi tamamlanacak ve uyarı verilecek ekrana fazla diye
                    Refund refundOrder = optRefund.get();
                    refundOrder.setRefundStatus(refundStatusRepository.findById(50L).get());
                    refundRepository.save(refundOrder);
                    return "Okutulan İlaç Sayısı, Sipariş Miktarından Fazla Olamaz." +
                            "<br/> Bu Siparişte İstenen Miktardan Fazla İlaç Bulunmaktadır." +
                            "<br/>" + numarator + " Adet İlacın Çıkışı Yapıldı.<br/><br/>" +
                            "Sipariş Durumu Tamamlandı Olarak Güncellendi ve Sipariş İade İşlemine Kapatıldı.";
                }
            }
            //for bitince kontrol edilecek tammı eksikmi diye ona göre siparişlerin statusu değişecek
            if (countOfDrugsOnlyDepotForRefund - numarator == 0) {
                Refund refundOrder = optRefund.get();
                refundOrder.setRefundStatus(refundStatusRepository.findById(50L).get());
                refundRepository.save(refundOrder);
                return "Sipariş Durumu Tamamlandı Olarak Güncellendi ve Sipariş İade İşlemine Kapatıldı.";

            } else if (countOfDrugsOnlyDepotForRefund - numarator > 0) {
                // ÖNEMLİ ==> Front kısmında ilk 3 "*" kontrol edilip ona göre yeni seçenek sunuluyor
                return "Siparişte Eksik İlaç Bulunmaktadır.<br/><br/> Siparişte İstenen Miktar: " + optRefund.get().getTotality() + " <br/> Depodan" +
                        " Çıkışı Yapılan Miktar: " + (optRefund.get().getTotality() - (countOfDrugsOnlyDepotForRefund - numarator));


            }
        }
        return "İlaç SKT Şartı Sağlanmıyor.";
    }
}

