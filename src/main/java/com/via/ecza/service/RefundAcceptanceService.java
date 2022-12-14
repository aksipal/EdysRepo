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
                throw new NotFoundException("B??yle bir iade sipari??i yoktur..");
            }

            //Karekod Anlaml?? Hale Getirilip Pre Refund'a Kaydediliyor
            Boolean seperateQrCode = seperateQrCode(optionalRefundOrder.get(),authHeader);
            if (seperateQrCode != true) {
                throw new NotFoundException("Karekod B??l??nemedi..");
            }


            List<PreRefund> list = preRefundRepository.getAllWithRefundId(dto.getRefundId());
            PreRefundDto[] array = mapper.map(list, PreRefundDto[].class);
            List<PreRefundDto> dtoList = Arrays.asList(array);

            //Depodan veya Stoktan Kar????an ??la??lar 1 Kez G??sterilecek Sonra Silinecek
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

            //Kullan??c?? Bilgisi Al??nd??
            User user = controlService.getUserFromToken(authHeader);

            //Veritaban??nda Kay??t Var m?? Kontrol?? Yap??ld??
            Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);
            if(!optUserCamera.isPresent()){
                throw new Exception("Kullan??c?? Kamera Kayd?? Bulunamad?? !");
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
                //karekod al??nd??
                qrCode = QRCode.getQrCode();

                if (qrCode == null || qrCode.isEmpty()) {
                    throw new NotFoundException("Karekod Bilgisi Bulunamad??");
                }

                //??la?? Barkodu Al??nd??.
                if (qrCode.charAt(0) == '0' && qrCode.charAt(1) == '1') {
                    barcode.append(qrCode.substring(2, 16));
                    // System.out.println("??la?? Barkod: " + barcode.toString());
                }

                //??la?? Seri Numaras?? Al??nd??.
                if (qrCode.charAt(16) == '2' && qrCode.charAt(17) == '1') {
                    secondGroupSeperatorIndex = qrCode.indexOf("&", 18);
                    serialNo.append(qrCode.substring(18, secondGroupSeperatorIndex));

                    //  System.out.println("??la?? Seri No: " + serialNo.toString());
                }

                //??la?? SKT Al??nd??.
                if (qrCode.charAt(secondGroupSeperatorIndex + 1) == '1' && qrCode.charAt(secondGroupSeperatorIndex + 2) == '7') {
                    expirationDate.append("20");
                    expirationDate.append(qrCode.substring(secondGroupSeperatorIndex + 3, secondGroupSeperatorIndex + 5));
                    expirationDate.append("-");
                    expirationDate.append(qrCode.substring(secondGroupSeperatorIndex + 5, secondGroupSeperatorIndex + 7));
                    expirationDate.append("-");
                    expirationDate.append(qrCode.substring(secondGroupSeperatorIndex + 7, secondGroupSeperatorIndex + 9));

                    //System.out.println("??la?? SKT: " + expirationDate.toString());
                }

                //??la?? Parti Numaras?? Al??nd??.
                if (qrCode.charAt(secondGroupSeperatorIndex + 9) == '1' && qrCode.charAt(secondGroupSeperatorIndex + 10) == '0') {
                    lotNo.append(qrCode.substring(secondGroupSeperatorIndex + 11, qrCode.length()));

                    // System.out.println("??la?? Parti No: " + lotNo.toString());
                }

                //??TS No Al??nd??.
                if (barcode.length() > 0 && serialNo.length() > 0) {
                    itsNo.append(barcode + "21" + serialNo);

                    // System.out.println("??ts No: " + itsNo.toString());
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
                        // System.out.println("??art Sa??land??");
                    } else {
                        preRefund.setPreRefundStatus(preRefundStatusRepository.findById((long) 3).get());
                        // System.out.println("??art Sa??lanmad??");
                    }
                } else {
                    preRefund.setPreRefundStatus(preRefundStatusRepository.findById((long) 4).get());
                    //System.out.println("Yanl???? ??la??");
                }


                //preRefund tablosuna yeni kay??t olarak eklendi.
                preRefundRepository.save(mapper.map(preRefund, PreRefund.class));


                //preRefund'a eklenen kay??t QrCode tablosunda status degi??tirdi.
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
                int numarator = 0;//se??ili olan ila?? say??s??

                for (RefundAcceptanceCheckedListDto list : dtoList) {
                    if (list != null && (preRefundRepository.findById(list.getPreRefundId()).get()).getPreRefundStatus().getPreRefundStatusId() != 4 //??la?? Hatal?? De??il ??se//
                    ) {
                        //preRefund bilgisinden sipari?? bilgisi al??nd??
                        PreRefund preRefundv2 = preRefundRepository.findById((dtoList.get(dtoList.indexOf(list))).getPreRefundId()).get();
                        refundOrder = preRefundv2.getRefund();
                        break;
                    }
                }

                if (refundOrder == null) {
                    return "L??tfen Hatal?? ??lac?? ????kar??n??z.";
                }

                if (refundOrder != null && (refundOrder.getRefundStatus().getRefundStatusId() != 10)) {
                    return "??ade ????lemi Sonland??r??lan Sipari??e ??la?? Kabul Edilemez!";
                } else {
                    String result = acceptForStock(dtoList, refundOrder);
                    return result;
                }

            }
            return "??la?? Bulunamad??";
        } catch (Exception e) {
            throw e;
        }
    }

    //??la?? Stoktan ??ade Ediliyor
    public String acceptForStock(List<RefundAcceptanceCheckedListDto> dtoList, Refund refundOrder) throws Exception {
        try {
            Depot drugInStock;
            int numarator = 0;

            Integer countOfDrugsOnlyDepotForRefund = depotRepository.countOfDrugsOnlyDepotForRefund(refundOrder.getRefundId());


            for (RefundAcceptanceCheckedListDto list : dtoList) {


                if (list != null && list.getValue() == true) {
                    numarator++;
                    PreRefund dto = (preRefundRepository.findById(list.getPreRefundId()).get());

                    //stokta bulunan ila?? bulundu
                    drugInStock = new Depot();
                    Optional<Depot> optionalDepot = depotRepository.findByItsNo(dto.getDrugItsNo());


                    if (!optionalDepot.isPresent()) {
                        numarator--;
                        //Teslimat Tam Al??n??rsa Sipari?? Statusu Tam Olarak de??i??sin
                        if (countOfDrugsOnlyDepotForRefund - numarator == 0) {
                            refundOrder.setRefundStatus(refundStatusRepository.findById((long) 40).get());//sipari?? haz??rland?? ve yola ????kt??
                            refundRepository.save(refundOrder);
                        }

                        PreRefund changeStatusPreRefund = preRefundRepository.findById(dto.getPreRefundId()).get();
                        changeStatusPreRefund.setPreRefundStatus(preRefundStatusRepository.findById((long) 6).get());
                        preRefundRepository.save(changeStatusPreRefund);
                        continue;//d??ng?? s??radakinden devam etsin

                        //return dto.getDrugSerialNo() + " seri numaral?? ila?? stok kay??tlar??nda yoktur. <br/> L??tfen Hatal?? ??lac?? ????kar??n??z";
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
                        //Teslimat Tam Al??n??rsa Sipari?? Statusu Tam Olarak de??i??sin
                        if (countOfDrugsOnlyDepotForRefund - numarator == 0) {
                            refundOrder.setRefundStatus(refundStatusRepository.findById((long) 40).get());//sipari?? haz??rland?? ve yola ????kt??
                            refundRepository.save(refundOrder);
                        }

                        PreRefund changeStatusPreRefund = preRefundRepository.findById(dto.getPreRefundId()).get();
                        changeStatusPreRefund.setPreRefundStatus(preRefundStatusRepository.findById((long) 5).get());
                        preRefundRepository.save(changeStatusPreRefund);
                        continue;//d??ng?? s??radakinden devam etsin
                        // return dto.getDrugSerialNo() + " seri numaral?? ila?? bu sat??n alman??n ilac?? de??ildir. <br/> L??tfen Bu ??lac?? Sto??a Teslim Ediniz.";
                    } else if (drugInStock.getDepotStatus().getDepotStatusId() != 15) {
                        numarator--;
                        //Teslimat Tam Al??n??rsa Sipari?? Statusu Tam Olarak de??i??sin
                        if (countOfDrugsOnlyDepotForRefund - numarator == 0) {
                            refundOrder.setRefundStatus(refundStatusRepository.findById((long) 40).get());//sipari?? haz??rland?? ve yola ????kt??
                            refundRepository.save(refundOrder);
                        }

                        PreRefund changeStatusPreRefund = preRefundRepository.findById(dto.getPreRefundId()).get();
                        changeStatusPreRefund.setPreRefundStatus(preRefundStatusRepository.findById((long) 5).get());
                        preRefundRepository.save(changeStatusPreRefund);
                        continue;//d??ng?? s??radakinden devam etsin
                        //return dto.getDrugSerialNo() + " seri numaral?? ila?? bu sat??n alman??n ilac?? de??ildir. <br/> L??tfen Bu ??lac?? Eski Yerine Teslim Ediniz.";
                    }


                    //HATALI ??LA?? KONTROL?? BA??LANGI??
                    if (dto.getPreRefundStatus().getPreRefundStatusId() == 4) {
                        numarator--;

                        if (dtoList.indexOf(list) + 1 < dtoList.size()) {//liste sonu de??il ise
                            for (int i = dtoList.indexOf(list) + 1; i < dtoList.size(); i++) {
                                //listenin geri kalan??nda se??ili ba??ka ila?? varsa ve hatal?? ila?? de??il ise sipari?? durumlar?? de??i??mesin
                                if (dtoList.get(i).getValue() == true) {
                                    if (preRefundRepository.findById(dtoList.get(i).getPreRefundId()).get().getPreRefundStatus().getPreRefundStatusId() != 4) {
                                        preRefundRepository.deleteById(dto.getPreRefundId());
                                        DrugCard wrongDrug = drugCardRepository.findByDrugCode(dto.getDrugBarcode()).get();
                                        return "L??tfen Hatal?? ??lac?? Stoktaki Eski Yerine ??ade Ediniz: <br/> ??la?? Ad??: " + wrongDrug.getDrugName() + " <br/> Seri No: " + dto.getDrugSerialNo();
                                    }
                                }
                            }
                        }
                        //Aksi Durumda Sipari?? A????k Kals??n m?? Kapat??ls??n m???
                        if (countOfDrugsOnlyDepotForRefund - numarator > 0) {

                            // ??NEML?? ==> Front k??sm??nda ilk 3 "*" kontrol edilip ona g??re yeni se??enek sunuluyor
                            /*return "*** L??tfen Listede Kalan Hatal?? ??la??lar?? Stoktaki Eski Yerine ??ade Ediniz. ***<br/>Di??er ??la??lar??n Depodan ????k?????? Yap??ld??. <br/><br/>Sipari??te Eksik ??la?? Bulunmaktad??r.<br/><br/> Sipari??te ??stenen Miktar: " + refundOrder.getTotality() + " <br/> ????k????" +
                                    " Yap??lan Miktar: " + (countOfDrugsOnlyDepotForRefund-numarator) + " <br/> Sipari?? Bu Haliyle Sonland??r??ls??n m???";*/

                            return "L??tfen Listede Kalan Hatal?? ??la??lar?? Stoktaki Eski Yerine ??ade Ediniz.<br/>Di??er ??la??lar??n Depodan ????k?????? Yap??ld??. <br/><br/>Sipari??te Eksik ??la?? Bulunmaktad??r.<br/><br/> Sipari??te ??stenen Miktar: " + refundOrder.getTotality() + " <br/> ????k????" +
                                    " Yap??lan Miktar: " + (refundOrder.getTotality() - (countOfDrugsOnlyDepotForRefund - numarator));


                        } else if (countOfDrugsOnlyDepotForRefund - numarator == 0) {
                            //Teslimat Tam Al??n??rsa Sipari?? Statusu Tam Olarak de??i??sin

                            refundOrder.setRefundStatus(refundStatusRepository.findById((long) 40).get());//sipari?? haz??rland?? ve yola ????kt??
                            refundRepository.save(refundOrder);

                            return "L??tfen Listede Kalan Hatal?? ??la??lar?? Stoktaki Eski Yerine ??ade Ediniz.<br/>Di??er ??la??lar??n Depodan ????k?????? Yap??ld??.<br/>Sipari?? Durumu Tamamaland?? Olarak G??ncellendi ve Sipari?? ??ade ????lemine Kapat??ld??";
                        }
                    } //HATALI ??LA?? KONTROL?? B??T????


                    if (countOfDrugsOnlyDepotForRefund - numarator >= 0) {
                        depotRepository.save(drugInStock);
                        dto.setPreRefundStatus(preRefundStatusRepository.findById((long) 1).get());
                        preRefundRepository.save(dto);

                    } else if (countOfDrugsOnlyDepotForRefund - numarator < 0) {
                        //Teslimat Tam Al??n??rsa Sipari?? Statusu Tam Olarak de??i??sin
                        if (countOfDrugsOnlyDepotForRefund - numarator == 0) {
                            refundOrder.setRefundStatus(refundStatusRepository.findById((long) 40).get());//sipari?? haz??rland?? ve yola ????kt??
                            refundRepository.save(refundOrder);
                        }
                        return "Okutulan ??la?? Say??s??, Sipari?? Miktar??ndan Fazla Olamaz." +
                                "<br/> Bu Sipari??te ??stenen Miktardan Fazla ??la?? Bulunmaktad??r." +
                                "<br/> L??tfen Listede Kalan ??la??lar?? Stoktaki Eski Yerine ??ade Ediniz.<br/><br/>" +
                                "Sipari?? Durumu Tamamland?? Olarak G??ncellendi ve Sipari?? ??ade ????lemine Kapat??ld??.";
                    }


                }

            }

            if (refundOrder != null) {
                if (countOfDrugsOnlyDepotForRefund - numarator > 0) {

                    // ??NEML?? ==> Front k??sm??nda ilk 3 "*" kontrol edilip ona g??re yeni se??enek sunuluyor
                    /*return "*** Sipari??te Eksik ??la?? Bulunmaktad??r. *** <br/><br/> Sipari??te ??stenen Miktar: " + refundOrder.getTotality() + " <br/> Depodan" +
                            " ????k?????? Yap??lan Miktar: " + (countOfDrugsOnlyDepotForRefund-numarator) + " <br/> Sipari?? Bu Haliyle Sonland??r??ls??n m???";*/

                    return "Sipari??te Eksik ??la?? Bulunmaktad??r.<br/><br/> Sipari??te ??stenen Miktar: " + refundOrder.getTotality() + " <br/> Depodan" +
                            " ????k?????? Yap??lan Miktar: " + (refundOrder.getTotality() - (countOfDrugsOnlyDepotForRefund - numarator));

                } else if (countOfDrugsOnlyDepotForRefund - numarator == 0) {
                    //Teslimat Tam Al??n??rsa Sipari?? Statusu Tam Olarak de??i??sin

                    refundOrder.setRefundStatus(refundStatusRepository.findById((long) 50).get());//sipari?? tamamland??
                    refundRepository.save(refundOrder);

                    return "Sipari?? Durumu Tamamland?? Olarak G??ncellendi ve Sipari?? ??ade ????lemine Kapat??ld??";
                }
            }

            return "??la??lar??n ????k???? ????lemi Yap??ld??";

        } catch (Exception e) {
            throw e;
        }
    }

    public String clearPreRefund(PreRefundSearchDto dto) throws Exception {
        try {
            StringBuilder createSqlQuery = new StringBuilder("delete from pre_refund pr where pr.refund_id = " + dto.getRefundId() + " and pr.pre_refund_status_id!=1");
            int countOfDeletedElements = entityManager.createNativeQuery(createSqlQuery.toString(), PreDepot.class).executeUpdate();
            return "Listede Bulunan " + countOfDeletedElements + " Adet Kay??t Ba??ar??yla Silindi";
        } catch (Exception e) {
            throw e;
        }
    }

    public int controlCommunicationStatus(CommunicationSearchForRefundAcceptanceDto comSearchDto,String authHeader) throws Exception {
        int result = 3;//communication bo?? ise 3 d??necek
        try {

            //Kullan??c?? Bilgisi Al??nd??
            User user = controlService.getUserFromToken(authHeader);

            //Veritaban??nda Kay??t Var m?? Kontrol?? Yap??ld??
            Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);

            if (!optUserCamera.isPresent()) {
                throw new Exception("Kullan??c?? Kamera Kayd?? Bulunamad?? !");
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
                    result = 0;//okuma i??lemi ba??lamad??
                } else if (list.get(0).getStatus() == 1) {
                    result = 1;//okuma i??lemi bitti
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return result;
    }

    public int saveQrCode(QrCodeSaveDto dto,String authHeader) throws NotFoundException {

        //Kullan??c?? Bilgisi Al??nd??
        User user = controlService.getUserFromToken(authHeader);

        //Veritaban??nda Kay??t Var m?? Kontrol?? Yap??ld??
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);

        dto.setQrcode(dto.getQrcode().trim());
        //QR CODE VER??TABANI KAYIT BA??LANGI??
        QrCode qrCodeSave = new QrCode();
        qrCodeSave.setCameraType(optUserCamera.get().getCameraType());
        qrCodeSave.setQrCode(dto.getQrcode());
        qrCodeSave.setStatus(0);
        qrCodeRepository.save(qrCodeSave);
        //QR CODE VER??TABANI KAYIT SON
        return 1;
    }

    public String saveManualAcceptanceQrCodes(RefundAcceptanceManualDto dto) throws Exception {
        if (dto.getRefundId() == null || dto.getQuantity() == null || dto.getStt() == null) {
            throw new Exception("Manuel ??ade ????leminde Eksik Bilgi Bulunmaktad??r !");
        }

        Optional<Refund> optRefund = refundRepository.findById(dto.getRefundId());
        if (!optRefund.isPresent()) {
            throw new Exception("??ade Sipari??i Bulunamad?? !");
        }


        if (optRefund.get() != null && optRefund.get().getRefundStatus().getRefundStatusId() != 10) {
            return "??ade ????lemi Sonland??r??lan Sipari??ten ??la?? ????k?????? Yap??lamaz!";
        }

        Integer countOfDrugsOnlyDepotForRefund = depotRepository.countOfDrugsOnlyDepotForRefund(dto.getRefundId());

        if (countOfDrugsOnlyDepotForRefund <= 0) {
            return "Bu Sipari??te ??ade Edilecek ??la?? Bulunamad?? !";

        } else if (dto.getStt().after(optRefund.get().getExpirationDate()) || (dto.getStt().getMonth() == optRefund.get().getExpirationDate().getMonth() && dto.getStt().getYear() == optRefund.get().getExpirationDate().getYear())) {
            int numarator = 0;
            Depot depot;

            List<Depot> depotList = depotRepository.DrugListOnlyDepotForRefund(dto.getRefundId());


            for (int i = 0; i < dto.getQuantity(); i++) {
                depot = new Depot();
                numarator++;
                if (countOfDrugsOnlyDepotForRefund - numarator >= 0) {
                    depot = depotList.get(i);

                    /* Depo Durumu De??i??iyor */
                    depot.setSendingDate(new Date());
                    depot.setDepotStatus(depotStatusRepository.findById(90L).get());
                    depotRepository.save(depot);

                } else {
                    numarator--;
                    //iade sipari??i tamamlanacak ve uyar?? verilecek ekrana fazla diye
                    Refund refundOrder = optRefund.get();
                    refundOrder.setRefundStatus(refundStatusRepository.findById(50L).get());
                    refundRepository.save(refundOrder);
                    return "Okutulan ??la?? Say??s??, Sipari?? Miktar??ndan Fazla Olamaz." +
                            "<br/> Bu Sipari??te ??stenen Miktardan Fazla ??la?? Bulunmaktad??r." +
                            "<br/>" + numarator + " Adet ??lac??n ????k?????? Yap??ld??.<br/><br/>" +
                            "Sipari?? Durumu Tamamland?? Olarak G??ncellendi ve Sipari?? ??ade ????lemine Kapat??ld??.";
                }
            }
            //for bitince kontrol edilecek tamm?? eksikmi diye ona g??re sipari??lerin statusu de??i??ecek
            if (countOfDrugsOnlyDepotForRefund - numarator == 0) {
                Refund refundOrder = optRefund.get();
                refundOrder.setRefundStatus(refundStatusRepository.findById(50L).get());
                refundRepository.save(refundOrder);
                return "Sipari?? Durumu Tamamland?? Olarak G??ncellendi ve Sipari?? ??ade ????lemine Kapat??ld??.";

            } else if (countOfDrugsOnlyDepotForRefund - numarator > 0) {
                // ??NEML?? ==> Front k??sm??nda ilk 3 "*" kontrol edilip ona g??re yeni se??enek sunuluyor
                return "Sipari??te Eksik ??la?? Bulunmaktad??r.<br/><br/> Sipari??te ??stenen Miktar: " + optRefund.get().getTotality() + " <br/> Depodan" +
                        " ????k?????? Yap??lan Miktar: " + (optRefund.get().getTotality() - (countOfDrugsOnlyDepotForRefund - numarator));


            }
        }
        return "??la?? SKT ??art?? Sa??lanm??yor.";
    }
}

