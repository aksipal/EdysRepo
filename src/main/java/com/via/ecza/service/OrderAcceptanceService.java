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
import java.util.*;

@Service
@Transactional
public class OrderAcceptanceService {
    @Autowired
    private PreDepotRepository preDepotRepository;
    @Autowired
    private PreDepotStatusRepository preDepotStatusRepository;
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
    @Autowired
    private CustomerOrderStatusHistoryService customerOrderStatusHistoryService;

    public Page<OrderAcceptanceCustomerSupplyOrderDto> search(OrderAcceptanceSearchDto dto, Integer pageNo, Integer pageSize, String sortBy) {
        StringBuilder createSqlQuery = new StringBuilder("select * from customer_supply_order cso inner join drug_card dc on dc.drug_card_id=cso.drug_card_id inner join supplier s on s.supplier_id=cso.supplier_id where (cso.customer_supply_status_id=10 or cso.customer_supply_status_id=45) ");


        if (dto.getSupplyOrderNo() != null || dto.getSupplierId() != null) {
            if (dto.getSupplyOrderNo() != null && !dto.getSupplyOrderNo().equals(""))
                createSqlQuery.append(" and  cso.supply_order_no ILIKE '%" + dto.getSupplyOrderNo().trim() + "%' ");
            if (dto.getSupplierId() != null)
                createSqlQuery.append(" and cso.supplier_id = " + dto.getSupplierId() + " ");
        }
        if (dto.getDrugCardId() != null)
            createSqlQuery.append(" and cso.drug_card_id = " + dto.getDrugCardId() + " ");

        if (dto.getDrugCode() != null)
            createSqlQuery.append(" and dc.drug_code = " + dto.getDrugCode() + " ");

        if (dto.getSupplierName() != null && dto.getSupplierName().trim().length() > 0)
            createSqlQuery.append(" and s.supplier_name ILIKE '%" + dto.getSupplierName().trim() + "%' ");

        createSqlQuery.append(" order by cso.customer_supply_order_id ");
        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerSupplyOrder.class).getResultList();

        OrderAcceptanceCustomerSupplyOrderDto[] dtos = mapper.map(list, OrderAcceptanceCustomerSupplyOrderDto[].class);
        List<OrderAcceptanceCustomerSupplyOrderDto> dtosList = Arrays.asList(dtos);


        for (OrderAcceptanceCustomerSupplyOrderDto cso : dtosList) {
            cso.setQuantityOfDepot(depotRepository.countOfDrugsStockAndDepotForOrderAcceptance(cso.getCustomerSupplyOrderId(), cso.getPurchaseOrderDrugs().getCustomerOrder().getCustomerOrderId()));
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        int start = Math.min((int) paging.getOffset(), dtosList.size());
        int end = Math.min((start + paging.getPageSize()), dtosList.size());

        Page<OrderAcceptanceCustomerSupplyOrderDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, dtosList.size());

        return pageList;
    }

    public List<PreDepotDto> editAcceptance(PreDepotSearchDto dto, String authHeader) throws NotFoundException, ParseException {

        try {


            Optional<CustomerSupplyOrder> optionalCustomerSupplyOrder = customerSupplyOrderRepository.findById(dto.getCustomerSupplyOrderId());
            if (!optionalCustomerSupplyOrder.isPresent()) {
                throw new NotFoundException("Böyle bir satın alma siparişi yoktur..");
            }
            CustomerOrder customerOrder = optionalCustomerSupplyOrder.get().getPurchaseOrderDrugs().getCustomerOrder();

            //Karekod Anlamlı Hale Getirilip Pre Depot'a Kaydediliyor
            Boolean seperateQrCode = seperateQrCode(customerOrder, optionalCustomerSupplyOrder.get(), authHeader);
            if (seperateQrCode != true) {
                throw new NotFoundException("Karekod Bölünemedi..");
            }


            List<PreDepot> list = preDepotRepository.getAllWithCustomerSupplyOrderId(dto.getCustomerSupplyOrderId());
            PreDepotDto[] array = mapper.map(list, PreDepotDto[].class);
            List<PreDepotDto> dtoList = Arrays.asList(array);

            //Depodan veya Stoktan Karışan İlaçlar 1 Kez Gösterilecek Sonra Silinecek
            int deleteResult = preDepotRepository.deletePreDepotStatus5_6(dto.getCustomerSupplyOrderId());

            return dtoList;
        } catch (NotFoundException e) {
            throw e;
        } catch (NumberFormatException e) {
            throw e;
        } catch (ParseException e) {
            throw e;
        }

    }

    public boolean seperateQrCode(CustomerOrder customerOrder, CustomerSupplyOrder customerSupplyOrder, String authHeader) throws ParseException, NotFoundException {

        try {
            //Kullanıcı Bilgisi Alındı
            User user = controlService.getUserFromToken(authHeader);

            //Veritabanında Kayıt Var mı Kontrolü Yapıldı
            Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);
            if (!optUserCamera.isPresent()) {
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

                PreDepotDto preDepot = new PreDepotDto();
                preDepot.setDrugBarcode(Long.valueOf(barcode.toString()));
                preDepot.setDrugSerialNo(serialNo.toString());


                Date exp = new SimpleDateFormat("yyyy-MM-dd").parse(expirationDate.toString());
                preDepot.setDrugExpirationDate(exp);


                preDepot.setDrugLotNo(lotNo.toString());
                preDepot.setDrugItsNo(itsNo.toString());

                preDepot.setCustomerOrder(mapper.map(customerOrder, DepotCustomerOrderListDto.class));
                preDepot.setCustomerSupplyOrder(mapper.map(customerSupplyOrder, DepotCustomerSupplierOrderListDto.class));


                Optional<DrugCard> drug = drugCardRepository.findByDrugCode(preDepot.getDrugBarcode());
                preDepot.setDrugName(drug.get().getDrugName());

                StringBuilder createSqlQuery = new StringBuilder("select * from customer_order_drugs " +
                        "where customer_order_id= " + customerOrder.getCustomerOrderId() + " and drug_card_id=" + drug.get().getDrugCardId());
                List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerOrderDrugs.class).getResultList();
                CustomerOrderDrugsSingleDto[] dtos = mapper.map(list, CustomerOrderDrugsSingleDto[].class);
                List<CustomerOrderDrugsSingleDto> dtoList = Arrays.asList(dtos);

                if (dtoList.size() > 0) {

                    if (customerSupplyOrder.getDrugCard().getDrugCardId() == drug.get().getDrugCardId()) {
                        if (exp.after(dtoList.get(0).getExpirationDate()) || (exp.getMonth() == dtoList.get(0).getExpirationDate().getMonth() && exp.getYear() == dtoList.get(0).getExpirationDate().getYear())) {
                            preDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 2).get());
                            // System.out.println("Şart Sağlandı");
                        } else {
                            preDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 3).get());
                            // System.out.println("Şart Sağlanmadı");
                        }
                    } else {
                        preDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 4).get());
                        //System.out.println("Yanlış İlaç");
                    }
                } else {
                    preDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 4).get());
                    // System.out.println("Yanlış İlaç");
                }

                //preDepot tablosuna yeni kayıt olarak eklendi.
                preDepotRepository.save(mapper.map(preDepot, PreDepot.class));

                //preDepot'a eklenen kayıt QrCode tablosundan silindi.
                //qrCodeRepository.deleteById(QRCode.getQrCodeId());

                //preDepot'a eklenen kayıt QrCode tablosunda status degiştirdi.
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

    public String accept(OrderAcceptanceAcceptDto dtoOrderAcceptance, String authHeader) throws Exception {
        try {

            User user = controlService.getUserFromToken(authHeader);


            DepotDto depot = null;
            CustomerSupplyOrder custSuppOrder = null;
            List<AcceptanceCheckedListDto> dtoList = dtoOrderAcceptance.getCheckedList();
            if (dtoList.size() > 0) {
                int numarator = 0;//seçili olan ilaç sayısı

                for (AcceptanceCheckedListDto list : dtoList) {
                    if (list != null && (preDepotRepository.findById(list.getPreDepotId()).get()).getPreDepotStatus().getPreDepotStatusId() != 4 //İlaç Hatalı Değil İse//
                    ) {
                        //preDepot bilgisinden sipariş bilgisi alındı
                        PreDepot preDepotv2 = preDepotRepository.findById((dtoList.get(dtoList.indexOf(list))).getPreDepotId()).get();
                        custSuppOrder = preDepotv2.getCustomerSupplyOrder();
                        break;
                    }
                }

                if (custSuppOrder == null) {
                    return "Lütfen Hatalı İlacı Çıkarınız.";
                }

                if (custSuppOrder != null && (custSuppOrder.getCustomerSupplyStatus().getCustomerSupplyStatusId() == 20 || custSuppOrder.getCustomerSupplyStatus().getCustomerSupplyStatusId() == 50)) {
                    return "Teslim Alması Sonlandırılan Siparişe İlaç Kabul Edilemez!";
                }

                //stoktan tedarik ediliyorsa
                if (custSuppOrder.getSupplier().getSupplierId() == 1) {
                    String result = acceptForStock(dtoList, custSuppOrder, authHeader);
                    return result;
                } else {

                    Integer countOfDrugsInDepot = depotRepository.countOfDrugsInDepot(custSuppOrder.getCustomerSupplyOrderId());
                    Integer countOfDrugsOnlyDepot = depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId());


                    for (AcceptanceCheckedListDto list : dtoList) {


                        if (list != null && list.getValue() == true) {
                            numarator++;
                            PreDepot dto = (preDepotRepository.findById(list.getPreDepotId()).get());
                            depot = new DepotDto();
                            depot.setDrugCard(drugCardRepository.findByDrugCode(dto.getDrugBarcode()).get());
                            depot.setDrugBarcode(String.valueOf(dto.getDrugBarcode()));
                            depot.setItsNo(dto.getDrugItsNo());
                            depot.setLotNo(dto.getDrugLotNo());
                            depot.setSerialNumber(String.valueOf(dto.getDrugSerialNo()));
                            depot.setExpirationDate(dto.getDrugExpirationDate());
                            depot.setSendingDate(null);
                            depot.setCustomerOrder(mapper.map(dto.getCustomerOrder(), DepotCustomerOrderListDto.class));
                            depot.setCustomerSupplyOrder(mapper.map(dto.getCustomerSupplyOrder(), DepotCustomerSupplierOrderListDto.class));
                            depot.setAdmitionDate(new Date());
                            depot.setUser(user);
                            if (dtoOrderAcceptance.getDrugPosition() != null && dtoOrderAcceptance.getDrugPosition().trim().length() != 0) {
                                depot.setPosition(dtoOrderAcceptance.getDrugPosition().trim());
                            } else {
                                depot.setPosition(null);
                            }
                            depot.setNote(null);
                            if (depot.getCustomerOrder().getCustomer().getCustomerId() == 1) {
                                //müşteri stok İse
                                //stok için gelen ilaç
                                depot.setDepotStatus(depotStatusRepository.findById((long) 10).get());
                            } else {

                                if (numarator + countOfDrugsOnlyDepot > custSuppOrder.getTotalQuantity()) {
                                    //eğer sipariş için istenen sayıya ulaşıldıysa geri kalan stok için eklenmeli
                                    depot.setDepotStatus(depotStatusRepository.findById((long) 10).get());
                                } else {
                                    //sipariş için istenen sayıya ulaşılmadıysa ilaç sipariş için eklenecek
                                    depot.setDepotStatus(depotStatusRepository.findById((long) 1).get());
                                }
                            }

                            Optional<Depot> itsControl = depotRepository.findByItsNo(dto.getDrugItsNo());

                            //HATALI İLAÇ KONTROLÜ BAŞLANGIÇ
                            if (dto.getPreDepotStatus().getPreDepotStatusId() == 4) {
                                numarator--;

                                if (dtoList.indexOf(list) + 1 < dtoList.size()) {//liste sonu değil ise
                                    for (int i = dtoList.indexOf(list) + 1; i < dtoList.size(); i++) {
                                        //listenin geri kalanında seçili başka ilaç varsa ve hatalı ilaç değil ise sipariş durumları değişmesin
                                        if (dtoList.get(i).getValue() == true) {
                                            if (preDepotRepository.findById(dtoList.get(i).getPreDepotId()).get().getPreDepotStatus().getPreDepotStatusId() != 4) {
                                                preDepotRepository.deleteById(dto.getPreDepotId());
                                                DrugCard wrongDrug = drugCardRepository.findByDrugCode(dto.getDrugBarcode()).get();
                                                return "Lütfen Hatalı İlacı Çıkarınız: <br/> İlaç Adı: " + wrongDrug.getDrugName() + " <br/> Seri No: " + dto.getDrugSerialNo();
                                            }
                                        }
                                    }
                                }
                                //Aksi Durumda Sipariş Açık Kalsın mı Kapatılsın mı?
                                if (custSuppOrder.getTotality() > (countOfDrugsInDepot + numarator)) {

                                    // ÖNEMLİ ==> Front kısmında ilk 3 "*" kontrol edilip ona göre yeni seçenek sunuluyor
                                    return "*** Lütfen Listede Kalan Hatalı İlaçları Çıkarınız. ***<br/>Diğer İlaçlar Depoya Eklendi. <br/><br/>Siparişte Eksik İlaç Bulunmaktadır.<br/><br/> Siparişte İstenen Miktar: " + custSuppOrder.getTotality() + " <br/> Teslim" +
                                            " Alınan Miktar: " + (countOfDrugsInDepot + numarator) + " <br/> Sipariş Sonlandırılırsa Teslim Alınan İlaçlar İptal Edilir.<br/>Sipariş Bu Haliyle Sonlandırılsın mı?";

                                } else if (custSuppOrder.getTotality() == (countOfDrugsInDepot + numarator)) {
                                    custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipariş tam teslim alındı
                                    custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                                    custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                                    customerSupplyOrderRepository.save(custSuppOrder);

                                    PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                                    poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim alındı
                                    purchaseOrderDrugsRepository.save(poDrugs);

                                    //yurtdışı sipariş durumu güncelleme
                                    Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                                    if (result == false) {
                                        return "Yurtdışı Sipariş Durumu Güncellenemedi";
                                    }

                                    return "Lütfen Listede Kalan Hatalı İlaçları Çıkarınız.<br/>Diğer İlaçlar Depoya Eklendi.<br/>Sipariş Durumu Tam Teslim Alındı Olarak Güncellendi ve Sipariş Teslim Almaya Kapatıldı";
                                }
                            } //HATALI İLAÇ KONTROLÜ BİTİŞ

                            if (itsControl.isPresent()) {
                                numarator--;
                                //preDepotRepository.deleteById(dto.getPreDepotId());
                                PreDepot changeStatusPreDepot = preDepotRepository.findById(dto.getPreDepotId()).get();
                                changeStatusPreDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 5).get());
                                preDepotRepository.save(changeStatusPreDepot);


                                //Teslimat Tam Alınırsa Sipariş Statusu Tam Olarak değişsin
                                if (Long.valueOf(countOfDrugsInDepot) == depot.getCustomerSupplyOrder().getTotality()) {
                                    custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipariş tam teslim alındı
                                    custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                                    custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                                    customerSupplyOrderRepository.save(custSuppOrder);

                                    PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                                    poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim alındı
                                    purchaseOrderDrugsRepository.save(poDrugs);

                                    //yurtdışı sipariş durumu güncelleme
                                    Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                                    if (result == false) {
                                        return "Yurtdışı Sipariş Durumu Güncellenemedi";
                                    }
                                }
                                continue;//döngü sıradakinden devam etsin
                                //return dto.getDrugSerialNo() + " seri numaralı ilaç depoya daha önce eklenmiştir. <br/> Lütfen Bu İlacı Depoya Teslim Ediniz.";
                            }
                            if (numarator + countOfDrugsInDepot <= depot.getCustomerSupplyOrder().getTotality()) {
                                depotRepository.save(mapper.map(depot, Depot.class));
                                dto.setPreDepotStatus(preDepotStatusRepository.findById((long) 1).get());
                                preDepotRepository.save(dto);

                            } else if (numarator + countOfDrugsInDepot > depot.getCustomerSupplyOrder().getTotality()) {
                                custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipariş tam teslim alındı
                                custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                                custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                                customerSupplyOrderRepository.save(custSuppOrder);

                                PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                                poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim alındı
                                purchaseOrderDrugsRepository.save(poDrugs);

                                //yurtdışı sipariş durumu güncelleme
                                Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                                if (result == false) {
                                    return "Yurtdışı Sipariş Durumu Güncellenemedi";
                                }


                                return "Okutulan İlaç Sayısı, Sipariş Miktarından Fazla Olamaz." +
                                        "<br/> Bu Siparişte İstenen Miktardan Fazla İlaç Bulunmaktadır." +
                                        "<br/> Lütfen Listede Kalan İlaçları Çıkarınız.<br/><br/>" +
                                        "Sipariş Durumu Tam Teslim Alındı Olarak Güncellendi ve Sipariş Teslim Almaya Kapatıldı.";
                            }


                        }
                    }


                    if (custSuppOrder != null) {
                        if (custSuppOrder.getTotality() > (countOfDrugsInDepot + numarator)) {

                            // ÖNEMLİ ==> Front kısmında ilk 3 "*" kontrol edilip ona göre yeni seçenek sunuluyor
                            return "*** Siparişte Eksik İlaç Bulunmaktadır. *** <br/><br/> Siparişte İstenen Miktar: " + custSuppOrder.getTotality() + " <br/> Teslim" +
                                    " Alınan Miktar: " + (countOfDrugsInDepot + numarator) + " <br/> Sipariş Sonlandırılırsa Teslim Alınan İlaçlar İptal Edilir.<br/>Sipariş Bu Haliyle Sonlandırılsın mı?";

                        } else if (custSuppOrder.getTotality() == (countOfDrugsInDepot + numarator)) {
                            custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipariş tam teslim alındı
                            custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                            custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                            customerSupplyOrderRepository.save(custSuppOrder);

                            PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                            poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim alındı
                            purchaseOrderDrugsRepository.save(poDrugs);

                            //yurtdışı sipariş durumu güncelleme
                            Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                            if (result == false) {
                                return "Yurtdışı Sipariş Durumu Güncellenemedi";
                            }

                            return "Sipariş Durumu Tam Teslim Alındı Olarak Güncellendi ve Sipariş Teslim Almaya Kapatıldı";
                        }
                    }

                    return "Gelen İlaçlar Başarıyla Depoya Eklendi";
                }
            }
            return "Depoya Eklenecek İlaç Bulunamadı";
        } catch (Exception e) {
            throw e;
        }
    }

    //Yurtdışı Siparişinin İlacı Stoktan Karşılanıyor
    public String acceptForStock(List<AcceptanceCheckedListDto> dtoList, CustomerSupplyOrder custSuppOrder, String authHeader) throws Exception {
        try {


            User user = controlService.getUserFromToken(authHeader);


            Depot drugInStock;
            int numarator = 0;

            Integer countOfDrugsOnlyDepot = depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId());


            for (AcceptanceCheckedListDto list : dtoList) {


                if (list != null && list.getValue() == true) {
                    numarator++;
                    PreDepot dto = (preDepotRepository.findById(list.getPreDepotId()).get());

                    //stokta bulunan ilaç bulundu
                    drugInStock = new Depot();
                    Optional<Depot> optionalDepot = depotRepository.findByItsNo(dto.getDrugItsNo());


                    if (!optionalDepot.isPresent()) {
                        numarator--;
                        //Teslimat Tam Alınırsa Sipariş Statusu Tam Olarak değişsin
                        if (custSuppOrder.getTotality() == (countOfDrugsOnlyDepot + numarator)) {
                            custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipariş tam teslim alındı
                            custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                            custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                            customerSupplyOrderRepository.save(custSuppOrder);

                            PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                            poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim alındı
                            purchaseOrderDrugsRepository.save(poDrugs);

                            //yurtdışı sipariş durumu güncelleme
                            Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                            if (result == false) {
                                return "Yurtdışı Sipariş Durumu Güncellenemedi";
                            }
                        }
                        //preDepotRepository.deleteById(dto.getPreDepotId());
                        PreDepot changeStatusPreDepot = preDepotRepository.findById(dto.getPreDepotId()).get();
                        changeStatusPreDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 6).get());
                        preDepotRepository.save(changeStatusPreDepot);
                        continue;//döngü sıradakinden devam etsin

                        //return dto.getDrugSerialNo() + " seri numaralı ilaç stok kayıtlarında yoktur. <br/> Lütfen Hatalı İlacı Çıkarınız";
                    } else if (optionalDepot.isPresent()) {
                        drugInStock = optionalDepot.get();
                    }

                    if (drugInStock.getDepotStatus().getDepotStatusId() == 4
                            && custSuppOrder.getCustomerSupplyOrderId() == drugInStock.getCustomerSupplyOrder().getCustomerSupplyOrderId()) {

                        drugInStock.setCustomerOrder(custSuppOrder.getPurchaseOrderDrugs().getCustomerOrder());
                        drugInStock.setCustomerSupplyOrder(custSuppOrder);
                        drugInStock.setAdmitionDate(new Date());
                        drugInStock.setUser(user);
                        drugInStock.setDepotStatus(depotStatusRepository.findById((long) 1).get());
                    } else if (drugInStock.getDepotStatus().getDepotStatusId() == 4
                            && custSuppOrder.getCustomerSupplyOrderId() != drugInStock.getCustomerSupplyOrder().getCustomerSupplyOrderId()) {
                        numarator--;
                        //Teslimat Tam Alınırsa Sipariş Statusu Tam Olarak değişsin
                        if (custSuppOrder.getTotality() == (countOfDrugsOnlyDepot + numarator)) {
                            custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipariş tam teslim alındı
                            custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                            custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                            customerSupplyOrderRepository.save(custSuppOrder);

                            PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                            poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim alındı
                            purchaseOrderDrugsRepository.save(poDrugs);

                            //yurtdışı sipariş durumu güncelleme
                            Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                            if (result == false) {
                                return "Yurtdışı Sipariş Durumu Güncellenemedi";
                            }
                        }
                        //preDepotRepository.deleteById(dto.getPreDepotId());
                        PreDepot changeStatusPreDepot = preDepotRepository.findById(dto.getPreDepotId()).get();
                        changeStatusPreDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 5).get());
                        preDepotRepository.save(changeStatusPreDepot);
                        continue;//döngü sıradakinden devam etsin
                        // return dto.getDrugSerialNo() + " seri numaralı ilaç bu satın almanın ilacı değildir. <br/> Lütfen Bu İlacı Stoğa Teslim Ediniz.";
                    } else if (drugInStock.getDepotStatus().getDepotStatusId() != 4) {
                        numarator--;
                        //Teslimat Tam Alınırsa Sipariş Statusu Tam Olarak değişsin
                        if (custSuppOrder.getTotality() == (countOfDrugsOnlyDepot + numarator)) {
                            custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipariş tam teslim alındı
                            custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                            custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                            customerSupplyOrderRepository.save(custSuppOrder);

                            PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                            poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim alındı
                            purchaseOrderDrugsRepository.save(poDrugs);

                            //yurtdışı sipariş durumu güncelleme
                            Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                            if (result == false) {
                                return "Yurtdışı Sipariş Durumu Güncellenemedi";
                            }
                        }
                        //preDepotRepository.deleteById(dto.getPreDepotId());
                        PreDepot changeStatusPreDepot = preDepotRepository.findById(dto.getPreDepotId()).get();
                        changeStatusPreDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 5).get());
                        preDepotRepository.save(changeStatusPreDepot);
                        continue;//döngü sıradakinden devam etsin
                        //return dto.getDrugSerialNo() + " seri numaralı ilaç bu satın almanın ilacı değildir. <br/> Lütfen Bu İlacı Eski Yerine Teslim Ediniz.";
                    }


                    //HATALI İLAÇ KONTROLÜ BAŞLANGIÇ
                    if (dto.getPreDepotStatus().getPreDepotStatusId() == 4) {
                        numarator--;

                        if (dtoList.indexOf(list) + 1 < dtoList.size()) {//liste sonu değil ise
                            for (int i = dtoList.indexOf(list) + 1; i < dtoList.size(); i++) {
                                //listenin geri kalanında seçili başka ilaç varsa ve hatalı ilaç değil ise sipariş durumları değişmesin
                                if (dtoList.get(i).getValue() == true) {
                                    if (preDepotRepository.findById(dtoList.get(i).getPreDepotId()).get().getPreDepotStatus().getPreDepotStatusId() != 4) {
                                        preDepotRepository.deleteById(dto.getPreDepotId());
                                        DrugCard wrongDrug = drugCardRepository.findByDrugCode(dto.getDrugBarcode()).get();
                                        return "Lütfen Hatalı İlacı Stoktaki Eski Yerine İade Ediniz: <br/> İlaç Adı: " + wrongDrug.getDrugName() + " <br/> Seri No: " + dto.getDrugSerialNo();
                                    }
                                }
                            }
                        }
                        //Aksi Durumda Sipariş Açık Kalsın mı Kapatılsın mı?
                        if (custSuppOrder.getTotality() > (countOfDrugsOnlyDepot + numarator)) {

                            // ÖNEMLİ ==> Front kısmında ilk 3 "*" kontrol edilip ona göre yeni seçenek sunuluyor
                            return "*** Lütfen Listede Kalan Hatalı İlaçları Stoktaki Eski Yerine İade Ediniz. ***<br/>Diğer İlaçlar Depoya Eklendi. <br/><br/>Siparişte Eksik İlaç Bulunmaktadır.<br/><br/> Siparişte İstenen Miktar: " + custSuppOrder.getTotality() + " <br/> Teslim" +
                                    " Alınan Miktar: " + (countOfDrugsOnlyDepot + numarator) + " <br/> Sipariş Sonlandırılırsa Teslim Alınan İlaçlar İptal Edilir.<br/>Sipariş Bu Haliyle Sonlandırılsın mı?";

                        } else if (custSuppOrder.getTotality() == (countOfDrugsOnlyDepot + numarator)) {
                            custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipariş tam teslim alındı
                            custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                            custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                            customerSupplyOrderRepository.save(custSuppOrder);

                            PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                            poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim alındı
                            purchaseOrderDrugsRepository.save(poDrugs);

                            //yurtdışı sipariş durumu güncelleme
                            Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                            if (result == false) {
                                return "Yurtdışı Sipariş Durumu Güncellenemedi";
                            }

                            return "Lütfen Listede Kalan Hatalı İlaçları Stoktaki Eski Yerine İade Ediniz.<br/>Diğer İlaçlar Depoya Eklendi.<br/>Sipariş Durumu Tam Teslim Alındı Olarak Güncellendi ve Sipariş Teslim Almaya Kapatıldı";
                        }
                    } //HATALI İLAÇ KONTROLÜ BİTİŞ


                    if (numarator + countOfDrugsOnlyDepot <= drugInStock.getCustomerSupplyOrder().getTotality()) {
                        depotRepository.save(drugInStock);
                        dto.setPreDepotStatus(preDepotStatusRepository.findById((long) 1).get());
                        preDepotRepository.save(dto);

                    } else if (numarator + countOfDrugsOnlyDepot > drugInStock.getCustomerSupplyOrder().getTotality()) {
                        custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipariş tam teslim alındı
                        custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                        custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                        customerSupplyOrderRepository.save(custSuppOrder);

                        PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                        poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim alındı
                        purchaseOrderDrugsRepository.save(poDrugs);

                        //yurtdışı sipariş durumu güncelleme
                        Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                        if (result == false) {
                            return "Yurtdışı Sipariş Durumu Güncellenemedi";
                        }


                        return "Okutulan İlaç Sayısı, Sipariş Miktarından Fazla Olamaz." +
                                "<br/> Bu Siparişte İstenen Miktardan Fazla İlaç Bulunmaktadır." +
                                "<br/> Lütfen Listede Kalan İlaçları Stoktaki Eski Yerine İade Ediniz.<br/><br/>" +
                                "Sipariş Durumu Tam Teslim Alındı Olarak Güncellendi ve Sipariş Teslim Almaya Kapatıldı.";
                    }


                }

            }

            if (custSuppOrder != null) {
                if (custSuppOrder.getTotality() > (countOfDrugsOnlyDepot + numarator)) {

                    // ÖNEMLİ ==> Front kısmında ilk 3 "*" kontrol edilip ona göre yeni seçenek sunuluyor
                    return "*** Siparişte Eksik İlaç Bulunmaktadır. *** <br/><br/> Siparişte İstenen Miktar: " + custSuppOrder.getTotality() + " <br/> Teslim" +
                            " Alınan Miktar: " + (countOfDrugsOnlyDepot + numarator) + " <br/> Sipariş Sonlandırılırsa Teslim Alınan İlaçlar İptal Edilir.<br/> Sipariş Bu Haliyle Sonlandırılsın mı?";

                } else if (custSuppOrder.getTotality() == (countOfDrugsOnlyDepot + numarator)) {
                    custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipariş tam teslim alındı
                    custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                    custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                    customerSupplyOrderRepository.save(custSuppOrder);

                    PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                    poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim alındı
                    purchaseOrderDrugsRepository.save(poDrugs);

                    //yurtdışı sipariş durumu güncelleme
                    Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                    if (result == false) {
                        return "Yurtdışı Sipariş Durumu Güncellenemedi";
                    }

                    return "Sipariş Durumu Tam Teslim Alındı Olarak Güncellendi ve Sipariş Teslim Almaya Kapatıldı";
                }
            }

            return "Gelen İlaçlar Başarıyla Depoya Eklendi";

        } catch (Exception e) {
            throw e;
        }
    }

    public String changeOrderStatus(PreDepotSearchDto dto) throws Exception {


        try {
            Optional<CustomerSupplyOrder> customerSupplyOrder = customerSupplyOrderRepository.findById(dto.getCustomerSupplyOrderId());
            CustomerOrder customerOrder = customerSupplyOrder.get().getPurchaseOrderDrugs().getCustomerOrder();


            /* Eksik Kabul Edilen İlaçlar Siliniyor */
            List<Depot> drugList = depotRepository.getDepotDrugListForChangeStatus(customerOrder.getCustomerOrderId(), customerSupplyOrder.get().getCustomerSupplyOrderId(), customerSupplyOrder.get().getDrugCard().getDrugCardId());

            for (Depot drug : drugList) {
                drug.setDrugCard(null);
                drug.setCustomerOrder(null);
                drug.setCustomerSupplyOrder(null);
                drug.setBoxId(null);
                drug.setSmallBoxId(null);
                drug.setRefund(null);
                drug.setRefundOffer(null);
                drug.setUser(null);
                depotRepository.save(drug);
                depotRepository.deleteById(drug.getDepotId());
            }


            if (customerSupplyOrder.isPresent()) {
                customerSupplyOrder.get().setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 20).get());//sipariş eksik teslim alındı
                customerSupplyOrder.get().setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(customerSupplyOrder.get().getCustomerSupplyOrderId()));
                customerSupplyOrder.get().setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(customerSupplyOrder.get().getCustomerSupplyOrderId()));
                customerSupplyOrderRepository.save(customerSupplyOrder.get());

                PurchaseOrderDrugs poDrugs = customerSupplyOrder.get().getPurchaseOrderDrugs();
                poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 30).get());//purchase order drugs eksik teslim alındı
                purchaseOrderDrugsRepository.save(poDrugs);


                //yurtdışı sipariş durumu eksik olarak güncellendi
                //en az 1 tane satın alma eksik ise otomatik olarak yurtdışı eksik teslim alındı olarak güncellenecek
                customerOrder.setOrderStatus(customerOrderStatusRepository.findById((long) 40).get());
                if (!customerOrderStatusHistoryService.save(customerOrder, customerOrder.getOrderStatus()))
                    throw new Exception("Sipariş Oluşturulamadı");
                customerOrderRepository.save(customerOrder);

                return "Sipariş Durumu Eksik Teslim Alındı Olarak Güncellendi ve Sipariş Teslim Almaya Kapatıldı ";
            }

            return "İşlem Sırasında Hata Oluştu";
        } catch (Exception e) {
            throw e;
        }

    }

    public String changeCustomerSuppOrderStatus(PreDepotSearchDto dto) throws Exception {


        try {
            Optional<CustomerSupplyOrder> customerSupplyOrder = customerSupplyOrderRepository.findById(dto.getCustomerSupplyOrderId());

            if (customerSupplyOrder.isPresent()) {
                customerSupplyOrder.get().setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 45).get());
                customerSupplyOrderRepository.save(customerSupplyOrder.get());
                return "Sipariş Durumu Eksik Teslim Alındı Ama Devamı Gelecek Olarak Güncellendi";
            }

            return "İşlem Sırasında Hata Oluştu";
        } catch (Exception e) {
            throw e;
        }

    }

    public Boolean changeCustomerOrderStatus(Long customerSupplyOrderId) throws Exception {

        try {
            //yurtdışına bağlı satın alma siparişleri kontrol ediliyor ona göre durum güncelleniyor

            StringBuilder createSqlQuery = new StringBuilder("select * from customer_supply_order cso " +
                    "inner join purchase_order_drugs pod on pod.purchase_order_drugs_id =cso.purchase_order_drugs_id " +
                    "inner join customer_order co on co.customer_order_id =pod.customer_order_id " +
                    "where cso.customer_supply_order_id =" + customerSupplyOrderId);
            List<CustomerSupplyOrder> custSupOrderListToCustOrder = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerSupplyOrder.class).getResultList();

            if (custSupOrderListToCustOrder.size() > 0) {
                int counter = 0;
                for (CustomerSupplyOrder listItem : custSupOrderListToCustOrder) {

                    counter++;
                    if (listItem.getCustomerSupplyStatus().getCustomerSupplyStatusId() != 20 && listItem.getCustomerSupplyStatus().getCustomerSupplyStatusId() != 50) {
                        //hala teslim almayı bekleyen satın alma siparişi var ise yurt dışı durumu değişmez
                        break;
                    } else if (listItem.getCustomerSupplyStatus().getCustomerSupplyStatusId() == 20) {
                        //en az 1 tane eksik satın alma siparişi var ise
                        //yurtdışı sipariş durumu eksik olarak güncellenir
                        CustomerOrder customerOrder = listItem.getPurchaseOrderDrugs().getCustomerOrder();
                        customerOrder.setOrderStatus(customerOrderStatusRepository.findById((long) 40).get());
                        if (!customerOrderStatusHistoryService.save(customerOrder, customerOrder.getOrderStatus()))
                            throw new Exception("Sipariş Oluşturulamadı");
                        customerOrderRepository.save(customerOrder);
                    } else if (counter == custSupOrderListToCustOrder.size()) {
                        //tüm satın alma siparişleri tam teslim alınmıştır eğer cod tablosundaki total quantity e ulaşıldıysa
                        //yani satınalması yapılmamış ilaç yoksa yurtdışı sipariş durumu tam teslim alındı olarak güncellenir
                        CustomerOrder customerOrder = listItem.getPurchaseOrderDrugs().getCustomerOrder();
                        //cod tablosu total quantity toplam miktarı
                        Long sumOfTotalQuantityInCod = customerOrderDrugsRepository.sumOfTotalQuantityInCOD(customerOrder.getCustomerOrderId());
                       Long countOfDrugsOnlyDepotToCO=depotRepository.countOfDrugsOnlyDepotToCO(customerOrder.getCustomerOrderId());
                        //Müşteri stok ise stoktaki sayı ile kontrol edilir
                        if (customerOrder.getCustomer().getCustomerId() == 1 && (sumOfTotalQuantityInCod == depotRepository.countOfDrugsOnlyStockToCO(customerOrder.getCustomerOrderId()))) {
                            customerOrder.setOrderStatus(customerOrderStatusRepository.findById((long) 50).get());
                            if (!customerOrderStatusHistoryService.save(customerOrder, customerOrder.getOrderStatus()))
                                throw new Exception("Sipariş Oluşturulamadı");
                            customerOrderRepository.save(customerOrder);
                        } else if (customerOrder.getCustomer().getCustomerId() != 1 && (sumOfTotalQuantityInCod.equals(countOfDrugsOnlyDepotToCO))) {
                            //Müşteri stok değil ise depodaki sayı ile kontrol edilir
                            customerOrder.setOrderStatus(customerOrderStatusRepository.findById((long) 50).get());
                            if (!customerOrderStatusHistoryService.save(customerOrder, customerOrder.getOrderStatus()))
                                throw new Exception("Sipariş Oluşturulamadı");
                            customerOrderRepository.save(customerOrder);
                        }


                    }

                }
                return true;
            }
            return false;
        } catch (Exception e) {
            throw e;
        }

    }

    public String rejectOrder(PreDepotSearchDto dto) throws Exception {
        try {
            StringBuilder createSqlQuery = new StringBuilder("select * from depot d where d.customer_supply_order_id = " + dto.getCustomerSupplyOrderId());

            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Depot.class).getResultList();


            if (list.size() <= 0) {
                Optional<CustomerSupplyOrder> customerSupplyOrder = customerSupplyOrderRepository.findById(dto.getCustomerSupplyOrderId());
                if (customerSupplyOrder.isPresent()) {
                    customerSupplyOrder.get().setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 30).get());//sipariş reddedildi
                    customerSupplyOrder.get().setDepotTotalQuantity((long) 0);
                    customerSupplyOrder.get().setDepotStockQuantity((long) 0);
                    customerSupplyOrderRepository.save(customerSupplyOrder.get());


                    return "Sipariş Reddedildi";
                }
                return "İşlem Sırasında Hata Oluştu";
            }
            return "Depoda Bu Siparişe Ait İlaç Bulunduğu İçin Sipariş Reddedilemez !";
        } catch (Exception e) {
            throw e;
        }
    }

    public String clearPreDepot(PreDepotSearchDto dto) throws Exception {
        try {
            StringBuilder createSqlQuery = new StringBuilder("delete from pre_depot pd where pd.customer_supply_order_id = " + dto.getCustomerSupplyOrderId() + " and pd.pre_depot_status_id!=1");
            int countOfDeletedElements = entityManager.createNativeQuery(createSqlQuery.toString(), PreDepot.class).executeUpdate();
            return "Listede Bulunan " + countOfDeletedElements + " Adet Kayıt Başarıyla Silindi";
        } catch (Exception e) {
            throw e;
        }
    }

    public int controlCommunicationStatus(CommunicationSearchDto comSearchDto, String authHeader) throws Exception {
        int result = 3;//communication boş ise 3 dönecek
        try {

            //Kullanıcı Bilgisi Alındı
            User user = controlService.getUserFromToken(authHeader);

            //Veritabanında Kayıt Var mı Kontrolü Yapıldı
            Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);

            if (!optUserCamera.isPresent()) {
                throw new Exception("Kullanıcı Kamera Kaydı Bulunamadı !");
            }

            CustomerSupplyOrder cso = customerSupplyOrderRepository.findById(comSearchDto.getCustomerSupplyOrderId()).get();

            StringBuilder createSqlQuery = new StringBuilder("select * from communication c " +
                    "where c.barcode=" + cso.getPurchaseOrderDrugs().getDrugCard().getDrugCode() + " " +
                    "and c.expiration_date='" + cso.getPurchaseOrderDrugs().getExpirationDate() + "' " +
                    "and c.camera_type=" + optUserCamera.get().getCameraType() +
                    "and c.status=1 " +
                    "and c.total_quantity=" + cso.getTotality());

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

    public int controlCommunicationStatusForTest(String authHeader) throws Exception {
        int result = 3;//communication boş ise 3 dönecek

        //Kullanıcı Bilgisi Alındı
        User user = controlService.getUserFromToken(authHeader);

        //Veritabanında Kayıt Var mı Kontrolü Yapıldı
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);

        if (!optUserCamera.isPresent()) {
            throw new Exception("Kullanıcı Kamera Kaydı Bulunamadı !");
        }

        try {


            StringBuilder createSqlQuery = new StringBuilder("select * from communication c " +
                    "where c.barcode='11111111111111' and c.camera_type=" + optUserCamera.get().getCameraType() +
                    "and c.status=1 " +
                    "and c.total_quantity=1");

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

    public List<PreDepotDto> getAllPreDepotForTest(String authHeader) throws Exception {
        try {

            //Kullanıcı Bilgisi Alındı
            User user = controlService.getUserFromToken(authHeader);

            //Veritabanında Kayıt Var mı Kontrolü Yapıldı
            Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);
            if (!optUserCamera.isPresent()) {
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

                    //System.out.println("İlaç Seri No: " + serialNo.toString());
                }

                //İlaç SKT Alındı.
                if (qrCode.charAt(secondGroupSeperatorIndex + 1) == '1' && qrCode.charAt(secondGroupSeperatorIndex + 2) == '7') {
                    expirationDate.append("20");
                    expirationDate.append(qrCode.substring(secondGroupSeperatorIndex + 3, secondGroupSeperatorIndex + 5));
                    expirationDate.append("-");
                    expirationDate.append(qrCode.substring(secondGroupSeperatorIndex + 5, secondGroupSeperatorIndex + 7));
                    expirationDate.append("-");
                    expirationDate.append(qrCode.substring(secondGroupSeperatorIndex + 7, secondGroupSeperatorIndex + 9));

                    // System.out.println("İlaç SKT: " + expirationDate.toString());
                }

                //İlaç Parti Numarası Alındı.
                if (qrCode.charAt(secondGroupSeperatorIndex + 9) == '1' && qrCode.charAt(secondGroupSeperatorIndex + 10) == '0') {
                    lotNo.append(qrCode.substring(secondGroupSeperatorIndex + 11, qrCode.length()));

                    //  System.out.println("İlaç Parti No: " + lotNo.toString());
                }

                //İTS No Alındı.
                if (barcode.length() > 0 && serialNo.length() > 0) {
                    itsNo.append(barcode + "21" + serialNo);

                    // System.out.println("İts No: " + itsNo.toString());
                }

                PreDepotDto preDepot = new PreDepotDto();
                preDepot.setDrugBarcode(Long.valueOf(barcode.toString()));
                preDepot.setDrugSerialNo(serialNo.toString());


                Date exp = new SimpleDateFormat("yyyy-MM-dd").parse(expirationDate.toString());
                preDepot.setDrugExpirationDate(exp);


                preDepot.setDrugLotNo(lotNo.toString());
                preDepot.setDrugItsNo(itsNo.toString());

                preDepot.setCustomerOrder(null);
                preDepot.setCustomerSupplyOrder(null);


                Optional<DrugCard> drug = drugCardRepository.findByDrugCode(preDepot.getDrugBarcode());
                preDepot.setDrugName(drug.get().getDrugName());

                Communication communication = communicationRepository.findAll().get(0);

                if (drugCardRepository.findByDrugCode(communication.getBarcode()).get().getDrugCardId() == drug.get().getDrugCardId()) {

                    if (exp.after(communication.getExpirationDate()) || (exp.getMonth() == communication.getExpirationDate().getMonth() && exp.getYear() == communication.getExpirationDate().getYear())) {
                        preDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 2).get());
                        //  System.out.println("Şart Sağlandı");
                    } else {
                        preDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 3).get());
                        // System.out.println("Şart Sağlanmadı");
                    }
                } else {
                    preDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 4).get());
                    //System.out.println("Yanlış İlaç");
                }


                //preDepot tablosuna yeni kayıt olarak eklendi.
                preDepotRepository.save(mapper.map(preDepot, PreDepot.class));

                //preDepot'a eklenen kayıt QrCode tablosundan silindi.
                //qrCodeRepository.deleteById(QRCode.getQrCodeId());

                //preDepot'a eklenen kayıt QrCode tablosunda status degiştirdi.
                QRCode.setStatus(1);
                qrCodeRepository.save(QRCode);

                barcode.setLength(0);
                serialNo.setLength(0);
                expirationDate.setLength(0);
                lotNo.setLength(0);
                itsNo.setLength(0);

            }

            List<PreDepot> list = preDepotRepository.getAllPreDepotOrderIsNull();
            PreDepotDto[] dtos = mapper.map(list, PreDepotDto[].class);
            List<PreDepotDto> dtoList = Arrays.asList(dtos);
            preDepotRepository.deletePreDepotOrderIsNull();
            if (dtoList.size() > 0) {
                dtoList.sort(Comparator.comparing(PreDepotDto::getDrugLotNo));
            }

            return dtoList;
        } catch (Exception e) {
            throw e;
        }
    }


    public int saveQrCode(QrCodeSaveDto dto, String authHeader) throws NotFoundException {

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

    public String saveManualAcceptanceQrCodes(OrderManualAcceptanceDto dto, String authHeader) throws Exception {
        if (dto.getCustomerSupplyOrderId() == null || dto.getQuantity() == null || dto.getStt() == null || dto.getLotNo().trim().length() == 0) {
            throw new Exception("Manuel Teslim Almada Eksik Bilgi Bulunmaktadır !");
        }

        User user = controlService.getUserFromToken(authHeader);


        Optional<CustomerSupplyOrder> optCustomerSupplyOrder = customerSupplyOrderRepository.findById(dto.getCustomerSupplyOrderId());
        if (!optCustomerSupplyOrder.isPresent()) {
            throw new Exception("Satın Alma Siparişi Bulunamadı !");
        }

        OrderAcceptanceCustomerSupplyOrderDto customerSupplyOrderDto = mapper.map(optCustomerSupplyOrder.get(), OrderAcceptanceCustomerSupplyOrderDto.class);

        if (customerSupplyOrderDto != null && (customerSupplyOrderDto.getCustomerSupplyStatus().getCustomerSupplyStatusId() == 20 || customerSupplyOrderDto.getCustomerSupplyStatus().getCustomerSupplyStatusId() == 50)) {
            return "Teslim Alması Sonlandırılan Siparişe İlaç Kabul Edilemez!";
        }

        /*System.out.println("stok adedi --> " + customerSupplyOrderDto.getStocks());
        System.out.println("depo adedi --> " + customerSupplyOrderDto.getTotalQuantity());
        System.out.println("total --> " + customerSupplyOrderDto.getTotality());
        System.out.println("teslim almadaki total --> " + dto.getQuantity());*/

        CustomerOrder customerOrder = customerOrderRepository.findById(customerSupplyOrderDto.getPurchaseOrderDrugs().getCustomerOrder().getCustomerOrderId()).get();
        CustomerSupplyOrder customerSupplyOrder = customerSupplyOrderRepository.findById(customerSupplyOrderDto.getCustomerSupplyOrderId()).get();
        //stoktan tedarik ediliyorsa
        if (customerSupplyOrder.getSupplier().getSupplierId() == 1) {
            int numarator = 0;
            if (dto.getStt().after(customerSupplyOrderDto.getPurchaseOrderDrugs().getExpirationDate()) || (dto.getStt().getMonth() == customerSupplyOrderDto.getPurchaseOrderDrugs().getExpirationDate().getMonth() && dto.getStt().getYear() == customerSupplyOrderDto.getPurchaseOrderDrugs().getExpirationDate().getYear())) {
                List<Depot> depotList = depotRepository.getReservedDrugListToCso(customerSupplyOrder.getCustomerSupplyOrderId(), customerSupplyOrder.getDrugCard().getDrugCardId());
                if (depotList.size() > 0) {
                    for (int i = 0; i < dto.getQuantity(); i++) {
                        numarator++;
                        if (numarator <= depotList.size()) {
                            depotList.get(i).setDepotStatus(depotStatusRepository.findById(1L).get());
                            depotList.get(i).setAdmitionDate(new Date());
                            depotList.get(i).setUser(user);
                            depotList.get(i).setCustomerSupplyOrder(customerSupplyOrder);
                            depotList.get(i).setCustomerOrder(customerOrder);
                            depotRepository.save(depotList.get(i));
                        } else if (numarator <= depotList.size()) {

                            //sipariş tam teslim alındı olacak ve uyarı verilecek ekrana fazla diye

                            customerSupplyOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipariş tam teslim alındı
                            customerSupplyOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(customerSupplyOrder.getCustomerSupplyOrderId()));
                            customerSupplyOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(customerSupplyOrder.getCustomerSupplyOrderId()));
                            customerSupplyOrderRepository.save(customerSupplyOrder);

                            PurchaseOrderDrugs poDrugs = customerSupplyOrder.getPurchaseOrderDrugs();
                            poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim alındı
                            purchaseOrderDrugsRepository.save(poDrugs);

                            //yurtdışı sipariş durumu güncelleme
                            Boolean result = changeCustomerOrderStatus(customerSupplyOrder.getCustomerSupplyOrderId());
                            if (result == false) {
                                throw new Exception("Yurtdışı Sipariş Durumu Güncellenemedi");
                            }
                            return "Okutulan İlaç Sayısı, Sipariş Miktarından Fazla Olamaz.<br/> " +
                                    "Bu Siparişte İstenen Miktardan Fazla İlaç Bulunmaktadır.<br/>" +
                                    "Sipariş Durumu Tam Teslim Alındı Olarak Güncellendi ve Sipariş Teslim Almaya Kapatıldı.";
                        }
                    }

                    //for bitince kontrol edilecek tammı eksikmi diye ona göre siparişlerin statusu değişecek
                    if (numarator == depotList.size()) {

                        //sipariş tam teslim alındı olacak ve uyarı verilecek ekrana fazla diye

                        customerSupplyOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipariş tam teslim alındı
                        customerSupplyOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(customerSupplyOrder.getCustomerSupplyOrderId()));
                        customerSupplyOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(customerSupplyOrder.getCustomerSupplyOrderId()));
                        customerSupplyOrderRepository.save(customerSupplyOrder);

                        PurchaseOrderDrugs poDrugs = customerSupplyOrder.getPurchaseOrderDrugs();
                        poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim alındı
                        purchaseOrderDrugsRepository.save(poDrugs);

                        //yurtdışı sipariş durumu güncelleme
                        Boolean result = changeCustomerOrderStatus(customerSupplyOrder.getCustomerSupplyOrderId());
                        if (result == false) {
                            throw new Exception("Yurtdışı Sipariş Durumu Güncellenemedi");
                        }

                        return "Sipariş Durumu Tam Teslim Alındı Olarak Güncellendi ve Sipariş Teslim Almaya Kapatıldı.";

                    } else if (numarator < depotList.size()) {
                        // ÖNEMLİ ==> Front kısmında ilk 3 "*" kontrol edilip ona göre yeni seçenek sunuluyor
                        return "*** Siparişte Eksik İlaç Bulunmaktadır. ***<br/><br/> Siparişte İstenen Miktar: " + customerSupplyOrderDto.getTotality() + " <br/> Teslim" +
                                " Alınan Miktar: " + (customerSupplyOrderDto.getTotality() - (depotList.size() - numarator)) + " <br/> Sipariş Sonlandırılırsa Teslim Alınan İlaçlar İptal Edilir.<br/>Sipariş Bu Haliyle Sonlandırılsın mı?";


                    }


                } else {
                    return "Stokta Bu Siparişe Ayrılmış İlaç Bulunamadı";
                }


            } else {
                return "İlaç SKT Şartı Sağlanmıyor.";
            }


        } else {

            if (dto.getStt().after(customerSupplyOrderDto.getPurchaseOrderDrugs().getExpirationDate()) || (dto.getStt().getMonth() == customerSupplyOrderDto.getPurchaseOrderDrugs().getExpirationDate().getMonth() && dto.getStt().getYear() == customerSupplyOrderDto.getPurchaseOrderDrugs().getExpirationDate().getYear())) {
                int numarator = 0;
                Depot depot = new Depot();

                Integer countInDepot = depotRepository.countOfDrugsDepotBarcodeAndCsoId(String.valueOf(customerSupplyOrderDto.getDrugCard().getDrugCode()), customerSupplyOrderDto.getCustomerSupplyOrderId());
                Integer countInStock = depotRepository.countOfDrugsStockBarcodeAndCsoId(String.valueOf(customerSupplyOrderDto.getDrugCard().getDrugCode()), customerSupplyOrderDto.getCustomerSupplyOrderId());


                for (int i = 0; i < dto.getQuantity(); i++) {
                    depot = new Depot();
                    numarator++;
                    if (countInDepot + countInStock + numarator <= customerSupplyOrderDto.getTotality()) {
                        depot.setAdmitionDate(new Date());
                        depot.setDrugBarcode(String.valueOf(customerSupplyOrderDto.getDrugCard().getDrugCode()));
                        depot.setExpirationDate(dto.getStt());
                        depot.setUser(user);
                        depot.setCustomerOrder(customerOrder);
                        depot.setCustomerSupplyOrder(customerSupplyOrder);
                        depot.setDrugCard(customerSupplyOrderDto.getDrugCard());
                        depot.setLotNo(dto.getLotNo().trim());

                        if (customerOrder.getCustomer().getCustomerId() == 1) {
                            depot.setDepotStatus(depotStatusRepository.findById(10L).get());//Stok Siparişi İse
                        } else {
                            if (countInDepot + numarator <= customerSupplyOrderDto.getTotalQuantity()) {
                                depot.setDepotStatus(depotStatusRepository.findById(1L).get());
                            } else if (countInDepot + numarator > customerSupplyOrderDto.getTotalQuantity()) {
                                depot.setDepotStatus(depotStatusRepository.findById(10L).get());
                            }
                        }
                        depot.setSerialNumber((countInDepot + countInStock + numarator) + customerSupplyOrder.getSupplyOrderNo());
                        depot.setItsNo(depot.getDrugBarcode() + "21" + depot.getSerialNumber());

                        Optional<Depot> itsControl = depotRepository.findByItsNo(depot.getItsNo());

                        if (itsControl.isPresent()) {
                            int a = 0;
                            String newSerialNo;
                            while (a < 1) {
                                newSerialNo = (countInDepot + countInStock + numarator + 1) + customerSupplyOrder.getSupplyOrderNo();
                                Optional<Depot> itsControlAgain = depotRepository.findByItsNo(newSerialNo);
                                if (!itsControlAgain.isPresent()) {
                                    depot.setSerialNumber(newSerialNo);
                                    depot.setItsNo(depot.getDrugBarcode() + "21" + depot.getSerialNumber());
                                    a = 5;
                                }
                            }
                        }

                        depotRepository.save(depot);

                    } else {

                        //sipariş tam teslim alındı olacak ve uyarı verilecek ekrana fazla diye

                        customerSupplyOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipariş tam teslim alındı
                        customerSupplyOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(customerSupplyOrder.getCustomerSupplyOrderId()));
                        customerSupplyOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(customerSupplyOrder.getCustomerSupplyOrderId()));
                        customerSupplyOrderRepository.save(customerSupplyOrder);

                        PurchaseOrderDrugs poDrugs = customerSupplyOrder.getPurchaseOrderDrugs();
                        poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim alındı
                        purchaseOrderDrugsRepository.save(poDrugs);

                        //yurtdışı sipariş durumu güncelleme
                        Boolean result = changeCustomerOrderStatus(customerSupplyOrder.getCustomerSupplyOrderId());
                        if (result == false) {
                            throw new Exception("Yurtdışı Sipariş Durumu Güncellenemedi");
                        }

                        return "Okutulan İlaç Sayısı, Sipariş Miktarından Fazla Olamaz.<br/> " +
                                "Bu Siparişte İstenen Miktardan Fazla İlaç Bulunmaktadır.<br/>" +
                                "Sipariş Durumu Tam Teslim Alındı Olarak Güncellendi ve Sipariş Teslim Almaya Kapatıldı.";


                    }
                }
                //for bitince kontrol edilecek tammı eksikmi diye ona göre siparişlerin statusu değişecek
                if (countInDepot + countInStock + numarator == customerSupplyOrderDto.getTotality()) {

                    //sipariş tam teslim alındı olacak ve uyarı verilecek ekrana fazla diye

                    customerSupplyOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipariş tam teslim alındı
                    customerSupplyOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(customerSupplyOrder.getCustomerSupplyOrderId()));
                    customerSupplyOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(customerSupplyOrder.getCustomerSupplyOrderId()));
                    customerSupplyOrderRepository.save(customerSupplyOrder);

                    PurchaseOrderDrugs poDrugs = customerSupplyOrder.getPurchaseOrderDrugs();
                    poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim alındı
                    purchaseOrderDrugsRepository.save(poDrugs);

                    //yurtdışı sipariş durumu güncelleme
                    Boolean result = changeCustomerOrderStatus(customerSupplyOrder.getCustomerSupplyOrderId());
                    if (result == false) {
                        throw new Exception("Yurtdışı Sipariş Durumu Güncellenemedi");
                    }

                    return "Sipariş Durumu Tam Teslim Alındı Olarak Güncellendi ve Sipariş Teslim Almaya Kapatıldı.";

                } else if (countInDepot + countInStock + numarator < customerSupplyOrderDto.getTotality()) {
                    // ÖNEMLİ ==> Front kısmında ilk 3 "*" kontrol edilip ona göre yeni seçenek sunuluyor
                    return "*** Siparişte Eksik İlaç Bulunmaktadır. ***<br/><br/> Siparişte İstenen Miktar: " + customerSupplyOrderDto.getTotality() + " <br/> Teslim" +
                            " Alınan Miktar: " + (countInDepot + countInStock + numarator) + " <br/> Sipariş Sonlandırılırsa Teslim Alınan İlaçlar İptal Edilir.<br/>Sipariş Bu Haliyle Sonlandırılsın mı?";


                }
            }


        }
        return "İlaç SKT Şartı Sağlanmıyor.";
    }

    public Boolean handReaderAccept(HandReaderOrderAcceptanceAcceptDto dto, String authHeader) throws Exception {
        Boolean control = false;

        User user = controlService.getUserFromToken(authHeader);
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);
        if (optUserCamera.isPresent()) {
            communicationRepository.deleteByCameraType(optUserCamera.get().getCameraType());
            qrCodeRepository.deleteByCameraType(optUserCamera.get().getCameraType());
        } else {
            throw new Exception("Kullanıcı Kamera Bilgisi Bulunamadı !");
        }
        //List<QrCode> qrCodeList = qrCodeRepository.findByCameraType(optUserCamera.get().getCameraType());
        if (dto.getQrcodeList() == null)
            throw new Exception("Eklemek İstediğiniz Karekodları Giriniz");

        if (dto.getQrcodeList().trim().length() <= 0)
            throw new Exception("Eklemek İstediğiniz Karekodları Giriniz");

        String[] list = dto.getQrcodeList().split("\n");
        for (String singleQrCode : list) {
            if (singleQrCode == null)
                continue;
            String finalQrcode = "";
            StringBuilder barcode = new StringBuilder();
            StringBuilder serialNo = new StringBuilder();
            StringBuilder restOfQrCode = new StringBuilder();
            int expDateStartIndex = 0;
            int partiNoStartIndex = 0;
            String qrCode = singleQrCode.trim();
            //İlaç Barkod Kontrolü
            if (qrCode.length() > 29) {
                if (qrCode.charAt(0) == '0' && qrCode.charAt(1) == '1') {
                    barcode.append(qrCode.substring(0, 16)); // 01 dahil edildi.
                }

                //İlaç Seri Numarası Kontrolü
                if (qrCode.charAt(16) == '2' && qrCode.charAt(17) == '1') {
                    String expDate = "";

                    // 19   20   21 22 23 24 25 26
                    expDateStartIndex = qrCode.indexOf("17", 18);

                    if (expDateStartIndex > 0  /* && partiNoStartIndex>0*/) {

                        // 17AABBCC10 ile oluşan bölumdekı 17 karakterındekı 1 in index numarasını arıyor.
                        expDateStartIndex = this.getIndexFromQrcodeFor17(expDateStartIndex, qrCode);
                        // 21 dahil edildi.
                        serialNo.append(qrCode.substring(16, expDateStartIndex));
                        serialNo.append("&");
                        // 17 dahil edildi.
                        restOfQrCode.append(qrCode.substring(expDateStartIndex));
                        finalQrcode = barcode.toString() + serialNo.toString() + restOfQrCode.toString();
                        partiNoStartIndex = qrCode.indexOf("10", expDateStartIndex + 7);

                        QrCode code = new QrCode();
                        code.setQrCode(finalQrcode);
                        code.setStatus(0);
                        code.setCameraType(optUserCamera.get().getCameraType());
                        code = qrCodeRepository.save(code);

                    } else {
                        continue;
                    }
                }
            }
        }
        control = true;
        return control;
    }

    public Boolean handReaderAcceptForStockCounting(HandReaderOrderAcceptanceAcceptDto dto, String authHeader) throws Exception {
        Boolean control = false;

        User user = controlService.getUserFromToken(authHeader);
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);
        if (optUserCamera.isPresent()) {
            communicationRepository.deleteByCameraType(optUserCamera.get().getCameraType());
            qrCodeRepository.deleteByCameraType(optUserCamera.get().getCameraType());
        } else {
            throw new Exception("Kullanıcı Kamera Bilgisi Bulunamadı !");
        }
        //List<QrCode> qrCodeList = qrCodeRepository.findByCameraType(optUserCamera.get().getCameraType());
        if (dto.getQrcodeList() == null)
            throw new Exception("Eklemek İstediğiniz Karekodları Giriniz");

        if (dto.getQrcodeList().trim().length() <= 0)
            throw new Exception("Eklemek İstediğiniz Karekodları Giriniz");

        String[] list = dto.getQrcodeList().split("\n");
        for (String singleQrCode : list) {
            if (singleQrCode == null)
                continue;
            String finalQrcode = "";
            StringBuilder barcode = new StringBuilder();
            StringBuilder serialNo = new StringBuilder();
            StringBuilder restOfQrCode = new StringBuilder();
            int expDateStartIndex = 0;
            int partiNoStartIndex = 0;
            String qrCode = singleQrCode.trim();
            //İlaç Barkod Kontrolü
            if (qrCode.length() > 29) {
                if (qrCode.charAt(0) == '0' && qrCode.charAt(1) == '1') {
                    barcode.append(qrCode.substring(0, 16)); // 01 dahil edildi.
                }

                //İlaç Seri Numarası Kontrolü
                if (qrCode.charAt(16) == '2' && qrCode.charAt(17) == '1') {
                    String expDate = "";

                    // 19   20   21 22 23 24 25 26
                    expDateStartIndex = qrCode.indexOf("17", 18);

                    if (expDateStartIndex > 0  /* && partiNoStartIndex>0*/) {

                        // 17AABBCC10 ile oluşan bölumdekı 17 karakterındekı 1 in index numarasını arıyor.
                        expDateStartIndex = this.getIndexFromQrcodeFor17(expDateStartIndex, qrCode);
                        // 21 dahil edildi.
                        serialNo.append(qrCode.substring(16, expDateStartIndex));
                        serialNo.append("&");
                        // 17 dahil edildi.
                        restOfQrCode.append(qrCode.substring(expDateStartIndex));
                        finalQrcode = barcode.toString() + serialNo.toString() + restOfQrCode.toString();
                        partiNoStartIndex = qrCode.indexOf("10", expDateStartIndex + 7);

                        QrCode code = new QrCode();
                        code.setQrCode(finalQrcode);
                        code.setStatus(0);
                        code.setCameraType(optUserCamera.get().getCameraType());
                        code = qrCodeRepository.save(code);

                    } else {
                        continue;
                    }
                }
            }
        }
        control = true;
        return control;
    }


    private int getIndexFromQrcodeFor17(int expDateStartIndex, String qrCode) {
        int preDateCharachter = Integer.valueOf(qrCode.substring(expDateStartIndex, expDateStartIndex + 2));
        int partiNo = Integer.valueOf(qrCode.substring(expDateStartIndex + 8, expDateStartIndex + 10));
        if (preDateCharachter == 17 && partiNo == 10) {
            return expDateStartIndex;
        } else {
            return this.getIndexFromQrcodeFor17(++expDateStartIndex, qrCode);
        }
    }
}
