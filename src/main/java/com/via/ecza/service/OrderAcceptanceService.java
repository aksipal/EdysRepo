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
                throw new NotFoundException("B??yle bir sat??n alma sipari??i yoktur..");
            }
            CustomerOrder customerOrder = optionalCustomerSupplyOrder.get().getPurchaseOrderDrugs().getCustomerOrder();

            //Karekod Anlaml?? Hale Getirilip Pre Depot'a Kaydediliyor
            Boolean seperateQrCode = seperateQrCode(customerOrder, optionalCustomerSupplyOrder.get(), authHeader);
            if (seperateQrCode != true) {
                throw new NotFoundException("Karekod B??l??nemedi..");
            }


            List<PreDepot> list = preDepotRepository.getAllWithCustomerSupplyOrderId(dto.getCustomerSupplyOrderId());
            PreDepotDto[] array = mapper.map(list, PreDepotDto[].class);
            List<PreDepotDto> dtoList = Arrays.asList(array);

            //Depodan veya Stoktan Kar????an ??la??lar 1 Kez G??sterilecek Sonra Silinecek
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
            //Kullan??c?? Bilgisi Al??nd??
            User user = controlService.getUserFromToken(authHeader);

            //Veritaban??nda Kay??t Var m?? Kontrol?? Yap??ld??
            Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);
            if (!optUserCamera.isPresent()) {
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
                            // System.out.println("??art Sa??land??");
                        } else {
                            preDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 3).get());
                            // System.out.println("??art Sa??lanmad??");
                        }
                    } else {
                        preDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 4).get());
                        //System.out.println("Yanl???? ??la??");
                    }
                } else {
                    preDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 4).get());
                    // System.out.println("Yanl???? ??la??");
                }

                //preDepot tablosuna yeni kay??t olarak eklendi.
                preDepotRepository.save(mapper.map(preDepot, PreDepot.class));

                //preDepot'a eklenen kay??t QrCode tablosundan silindi.
                //qrCodeRepository.deleteById(QRCode.getQrCodeId());

                //preDepot'a eklenen kay??t QrCode tablosunda status degi??tirdi.
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
                int numarator = 0;//se??ili olan ila?? say??s??

                for (AcceptanceCheckedListDto list : dtoList) {
                    if (list != null && (preDepotRepository.findById(list.getPreDepotId()).get()).getPreDepotStatus().getPreDepotStatusId() != 4 //??la?? Hatal?? De??il ??se//
                    ) {
                        //preDepot bilgisinden sipari?? bilgisi al??nd??
                        PreDepot preDepotv2 = preDepotRepository.findById((dtoList.get(dtoList.indexOf(list))).getPreDepotId()).get();
                        custSuppOrder = preDepotv2.getCustomerSupplyOrder();
                        break;
                    }
                }

                if (custSuppOrder == null) {
                    return "L??tfen Hatal?? ??lac?? ????kar??n??z.";
                }

                if (custSuppOrder != null && (custSuppOrder.getCustomerSupplyStatus().getCustomerSupplyStatusId() == 20 || custSuppOrder.getCustomerSupplyStatus().getCustomerSupplyStatusId() == 50)) {
                    return "Teslim Almas?? Sonland??r??lan Sipari??e ??la?? Kabul Edilemez!";
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
                                //m????teri stok ??se
                                //stok i??in gelen ila??
                                depot.setDepotStatus(depotStatusRepository.findById((long) 10).get());
                            } else {

                                if (numarator + countOfDrugsOnlyDepot > custSuppOrder.getTotalQuantity()) {
                                    //e??er sipari?? i??in istenen say??ya ula????ld??ysa geri kalan stok i??in eklenmeli
                                    depot.setDepotStatus(depotStatusRepository.findById((long) 10).get());
                                } else {
                                    //sipari?? i??in istenen say??ya ula????lmad??ysa ila?? sipari?? i??in eklenecek
                                    depot.setDepotStatus(depotStatusRepository.findById((long) 1).get());
                                }
                            }

                            Optional<Depot> itsControl = depotRepository.findByItsNo(dto.getDrugItsNo());

                            //HATALI ??LA?? KONTROL?? BA??LANGI??
                            if (dto.getPreDepotStatus().getPreDepotStatusId() == 4) {
                                numarator--;

                                if (dtoList.indexOf(list) + 1 < dtoList.size()) {//liste sonu de??il ise
                                    for (int i = dtoList.indexOf(list) + 1; i < dtoList.size(); i++) {
                                        //listenin geri kalan??nda se??ili ba??ka ila?? varsa ve hatal?? ila?? de??il ise sipari?? durumlar?? de??i??mesin
                                        if (dtoList.get(i).getValue() == true) {
                                            if (preDepotRepository.findById(dtoList.get(i).getPreDepotId()).get().getPreDepotStatus().getPreDepotStatusId() != 4) {
                                                preDepotRepository.deleteById(dto.getPreDepotId());
                                                DrugCard wrongDrug = drugCardRepository.findByDrugCode(dto.getDrugBarcode()).get();
                                                return "L??tfen Hatal?? ??lac?? ????kar??n??z: <br/> ??la?? Ad??: " + wrongDrug.getDrugName() + " <br/> Seri No: " + dto.getDrugSerialNo();
                                            }
                                        }
                                    }
                                }
                                //Aksi Durumda Sipari?? A????k Kals??n m?? Kapat??ls??n m???
                                if (custSuppOrder.getTotality() > (countOfDrugsInDepot + numarator)) {

                                    // ??NEML?? ==> Front k??sm??nda ilk 3 "*" kontrol edilip ona g??re yeni se??enek sunuluyor
                                    return "*** L??tfen Listede Kalan Hatal?? ??la??lar?? ????kar??n??z. ***<br/>Di??er ??la??lar Depoya Eklendi. <br/><br/>Sipari??te Eksik ??la?? Bulunmaktad??r.<br/><br/> Sipari??te ??stenen Miktar: " + custSuppOrder.getTotality() + " <br/> Teslim" +
                                            " Al??nan Miktar: " + (countOfDrugsInDepot + numarator) + " <br/> Sipari?? Sonland??r??l??rsa Teslim Al??nan ??la??lar ??ptal Edilir.<br/>Sipari?? Bu Haliyle Sonland??r??ls??n m???";

                                } else if (custSuppOrder.getTotality() == (countOfDrugsInDepot + numarator)) {
                                    custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipari?? tam teslim al??nd??
                                    custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                                    custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                                    customerSupplyOrderRepository.save(custSuppOrder);

                                    PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                                    poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim al??nd??
                                    purchaseOrderDrugsRepository.save(poDrugs);

                                    //yurtd?????? sipari?? durumu g??ncelleme
                                    Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                                    if (result == false) {
                                        return "Yurtd?????? Sipari?? Durumu G??ncellenemedi";
                                    }

                                    return "L??tfen Listede Kalan Hatal?? ??la??lar?? ????kar??n??z.<br/>Di??er ??la??lar Depoya Eklendi.<br/>Sipari?? Durumu Tam Teslim Al??nd?? Olarak G??ncellendi ve Sipari?? Teslim Almaya Kapat??ld??";
                                }
                            } //HATALI ??LA?? KONTROL?? B??T????

                            if (itsControl.isPresent()) {
                                numarator--;
                                //preDepotRepository.deleteById(dto.getPreDepotId());
                                PreDepot changeStatusPreDepot = preDepotRepository.findById(dto.getPreDepotId()).get();
                                changeStatusPreDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 5).get());
                                preDepotRepository.save(changeStatusPreDepot);


                                //Teslimat Tam Al??n??rsa Sipari?? Statusu Tam Olarak de??i??sin
                                if (Long.valueOf(countOfDrugsInDepot) == depot.getCustomerSupplyOrder().getTotality()) {
                                    custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipari?? tam teslim al??nd??
                                    custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                                    custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                                    customerSupplyOrderRepository.save(custSuppOrder);

                                    PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                                    poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim al??nd??
                                    purchaseOrderDrugsRepository.save(poDrugs);

                                    //yurtd?????? sipari?? durumu g??ncelleme
                                    Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                                    if (result == false) {
                                        return "Yurtd?????? Sipari?? Durumu G??ncellenemedi";
                                    }
                                }
                                continue;//d??ng?? s??radakinden devam etsin
                                //return dto.getDrugSerialNo() + " seri numaral?? ila?? depoya daha ??nce eklenmi??tir. <br/> L??tfen Bu ??lac?? Depoya Teslim Ediniz.";
                            }
                            if (numarator + countOfDrugsInDepot <= depot.getCustomerSupplyOrder().getTotality()) {
                                depotRepository.save(mapper.map(depot, Depot.class));
                                dto.setPreDepotStatus(preDepotStatusRepository.findById((long) 1).get());
                                preDepotRepository.save(dto);

                            } else if (numarator + countOfDrugsInDepot > depot.getCustomerSupplyOrder().getTotality()) {
                                custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipari?? tam teslim al??nd??
                                custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                                custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                                customerSupplyOrderRepository.save(custSuppOrder);

                                PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                                poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim al??nd??
                                purchaseOrderDrugsRepository.save(poDrugs);

                                //yurtd?????? sipari?? durumu g??ncelleme
                                Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                                if (result == false) {
                                    return "Yurtd?????? Sipari?? Durumu G??ncellenemedi";
                                }


                                return "Okutulan ??la?? Say??s??, Sipari?? Miktar??ndan Fazla Olamaz." +
                                        "<br/> Bu Sipari??te ??stenen Miktardan Fazla ??la?? Bulunmaktad??r." +
                                        "<br/> L??tfen Listede Kalan ??la??lar?? ????kar??n??z.<br/><br/>" +
                                        "Sipari?? Durumu Tam Teslim Al??nd?? Olarak G??ncellendi ve Sipari?? Teslim Almaya Kapat??ld??.";
                            }


                        }
                    }


                    if (custSuppOrder != null) {
                        if (custSuppOrder.getTotality() > (countOfDrugsInDepot + numarator)) {

                            // ??NEML?? ==> Front k??sm??nda ilk 3 "*" kontrol edilip ona g??re yeni se??enek sunuluyor
                            return "*** Sipari??te Eksik ??la?? Bulunmaktad??r. *** <br/><br/> Sipari??te ??stenen Miktar: " + custSuppOrder.getTotality() + " <br/> Teslim" +
                                    " Al??nan Miktar: " + (countOfDrugsInDepot + numarator) + " <br/> Sipari?? Sonland??r??l??rsa Teslim Al??nan ??la??lar ??ptal Edilir.<br/>Sipari?? Bu Haliyle Sonland??r??ls??n m???";

                        } else if (custSuppOrder.getTotality() == (countOfDrugsInDepot + numarator)) {
                            custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipari?? tam teslim al??nd??
                            custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                            custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                            customerSupplyOrderRepository.save(custSuppOrder);

                            PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                            poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim al??nd??
                            purchaseOrderDrugsRepository.save(poDrugs);

                            //yurtd?????? sipari?? durumu g??ncelleme
                            Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                            if (result == false) {
                                return "Yurtd?????? Sipari?? Durumu G??ncellenemedi";
                            }

                            return "Sipari?? Durumu Tam Teslim Al??nd?? Olarak G??ncellendi ve Sipari?? Teslim Almaya Kapat??ld??";
                        }
                    }

                    return "Gelen ??la??lar Ba??ar??yla Depoya Eklendi";
                }
            }
            return "Depoya Eklenecek ??la?? Bulunamad??";
        } catch (Exception e) {
            throw e;
        }
    }

    //Yurtd?????? Sipari??inin ??lac?? Stoktan Kar????lan??yor
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

                    //stokta bulunan ila?? bulundu
                    drugInStock = new Depot();
                    Optional<Depot> optionalDepot = depotRepository.findByItsNo(dto.getDrugItsNo());


                    if (!optionalDepot.isPresent()) {
                        numarator--;
                        //Teslimat Tam Al??n??rsa Sipari?? Statusu Tam Olarak de??i??sin
                        if (custSuppOrder.getTotality() == (countOfDrugsOnlyDepot + numarator)) {
                            custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipari?? tam teslim al??nd??
                            custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                            custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                            customerSupplyOrderRepository.save(custSuppOrder);

                            PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                            poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim al??nd??
                            purchaseOrderDrugsRepository.save(poDrugs);

                            //yurtd?????? sipari?? durumu g??ncelleme
                            Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                            if (result == false) {
                                return "Yurtd?????? Sipari?? Durumu G??ncellenemedi";
                            }
                        }
                        //preDepotRepository.deleteById(dto.getPreDepotId());
                        PreDepot changeStatusPreDepot = preDepotRepository.findById(dto.getPreDepotId()).get();
                        changeStatusPreDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 6).get());
                        preDepotRepository.save(changeStatusPreDepot);
                        continue;//d??ng?? s??radakinden devam etsin

                        //return dto.getDrugSerialNo() + " seri numaral?? ila?? stok kay??tlar??nda yoktur. <br/> L??tfen Hatal?? ??lac?? ????kar??n??z";
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
                        //Teslimat Tam Al??n??rsa Sipari?? Statusu Tam Olarak de??i??sin
                        if (custSuppOrder.getTotality() == (countOfDrugsOnlyDepot + numarator)) {
                            custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipari?? tam teslim al??nd??
                            custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                            custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                            customerSupplyOrderRepository.save(custSuppOrder);

                            PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                            poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim al??nd??
                            purchaseOrderDrugsRepository.save(poDrugs);

                            //yurtd?????? sipari?? durumu g??ncelleme
                            Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                            if (result == false) {
                                return "Yurtd?????? Sipari?? Durumu G??ncellenemedi";
                            }
                        }
                        //preDepotRepository.deleteById(dto.getPreDepotId());
                        PreDepot changeStatusPreDepot = preDepotRepository.findById(dto.getPreDepotId()).get();
                        changeStatusPreDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 5).get());
                        preDepotRepository.save(changeStatusPreDepot);
                        continue;//d??ng?? s??radakinden devam etsin
                        // return dto.getDrugSerialNo() + " seri numaral?? ila?? bu sat??n alman??n ilac?? de??ildir. <br/> L??tfen Bu ??lac?? Sto??a Teslim Ediniz.";
                    } else if (drugInStock.getDepotStatus().getDepotStatusId() != 4) {
                        numarator--;
                        //Teslimat Tam Al??n??rsa Sipari?? Statusu Tam Olarak de??i??sin
                        if (custSuppOrder.getTotality() == (countOfDrugsOnlyDepot + numarator)) {
                            custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipari?? tam teslim al??nd??
                            custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                            custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                            customerSupplyOrderRepository.save(custSuppOrder);

                            PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                            poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim al??nd??
                            purchaseOrderDrugsRepository.save(poDrugs);

                            //yurtd?????? sipari?? durumu g??ncelleme
                            Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                            if (result == false) {
                                return "Yurtd?????? Sipari?? Durumu G??ncellenemedi";
                            }
                        }
                        //preDepotRepository.deleteById(dto.getPreDepotId());
                        PreDepot changeStatusPreDepot = preDepotRepository.findById(dto.getPreDepotId()).get();
                        changeStatusPreDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 5).get());
                        preDepotRepository.save(changeStatusPreDepot);
                        continue;//d??ng?? s??radakinden devam etsin
                        //return dto.getDrugSerialNo() + " seri numaral?? ila?? bu sat??n alman??n ilac?? de??ildir. <br/> L??tfen Bu ??lac?? Eski Yerine Teslim Ediniz.";
                    }


                    //HATALI ??LA?? KONTROL?? BA??LANGI??
                    if (dto.getPreDepotStatus().getPreDepotStatusId() == 4) {
                        numarator--;

                        if (dtoList.indexOf(list) + 1 < dtoList.size()) {//liste sonu de??il ise
                            for (int i = dtoList.indexOf(list) + 1; i < dtoList.size(); i++) {
                                //listenin geri kalan??nda se??ili ba??ka ila?? varsa ve hatal?? ila?? de??il ise sipari?? durumlar?? de??i??mesin
                                if (dtoList.get(i).getValue() == true) {
                                    if (preDepotRepository.findById(dtoList.get(i).getPreDepotId()).get().getPreDepotStatus().getPreDepotStatusId() != 4) {
                                        preDepotRepository.deleteById(dto.getPreDepotId());
                                        DrugCard wrongDrug = drugCardRepository.findByDrugCode(dto.getDrugBarcode()).get();
                                        return "L??tfen Hatal?? ??lac?? Stoktaki Eski Yerine ??ade Ediniz: <br/> ??la?? Ad??: " + wrongDrug.getDrugName() + " <br/> Seri No: " + dto.getDrugSerialNo();
                                    }
                                }
                            }
                        }
                        //Aksi Durumda Sipari?? A????k Kals??n m?? Kapat??ls??n m???
                        if (custSuppOrder.getTotality() > (countOfDrugsOnlyDepot + numarator)) {

                            // ??NEML?? ==> Front k??sm??nda ilk 3 "*" kontrol edilip ona g??re yeni se??enek sunuluyor
                            return "*** L??tfen Listede Kalan Hatal?? ??la??lar?? Stoktaki Eski Yerine ??ade Ediniz. ***<br/>Di??er ??la??lar Depoya Eklendi. <br/><br/>Sipari??te Eksik ??la?? Bulunmaktad??r.<br/><br/> Sipari??te ??stenen Miktar: " + custSuppOrder.getTotality() + " <br/> Teslim" +
                                    " Al??nan Miktar: " + (countOfDrugsOnlyDepot + numarator) + " <br/> Sipari?? Sonland??r??l??rsa Teslim Al??nan ??la??lar ??ptal Edilir.<br/>Sipari?? Bu Haliyle Sonland??r??ls??n m???";

                        } else if (custSuppOrder.getTotality() == (countOfDrugsOnlyDepot + numarator)) {
                            custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipari?? tam teslim al??nd??
                            custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                            custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                            customerSupplyOrderRepository.save(custSuppOrder);

                            PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                            poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim al??nd??
                            purchaseOrderDrugsRepository.save(poDrugs);

                            //yurtd?????? sipari?? durumu g??ncelleme
                            Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                            if (result == false) {
                                return "Yurtd?????? Sipari?? Durumu G??ncellenemedi";
                            }

                            return "L??tfen Listede Kalan Hatal?? ??la??lar?? Stoktaki Eski Yerine ??ade Ediniz.<br/>Di??er ??la??lar Depoya Eklendi.<br/>Sipari?? Durumu Tam Teslim Al??nd?? Olarak G??ncellendi ve Sipari?? Teslim Almaya Kapat??ld??";
                        }
                    } //HATALI ??LA?? KONTROL?? B??T????


                    if (numarator + countOfDrugsOnlyDepot <= drugInStock.getCustomerSupplyOrder().getTotality()) {
                        depotRepository.save(drugInStock);
                        dto.setPreDepotStatus(preDepotStatusRepository.findById((long) 1).get());
                        preDepotRepository.save(dto);

                    } else if (numarator + countOfDrugsOnlyDepot > drugInStock.getCustomerSupplyOrder().getTotality()) {
                        custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipari?? tam teslim al??nd??
                        custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                        custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                        customerSupplyOrderRepository.save(custSuppOrder);

                        PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                        poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim al??nd??
                        purchaseOrderDrugsRepository.save(poDrugs);

                        //yurtd?????? sipari?? durumu g??ncelleme
                        Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                        if (result == false) {
                            return "Yurtd?????? Sipari?? Durumu G??ncellenemedi";
                        }


                        return "Okutulan ??la?? Say??s??, Sipari?? Miktar??ndan Fazla Olamaz." +
                                "<br/> Bu Sipari??te ??stenen Miktardan Fazla ??la?? Bulunmaktad??r." +
                                "<br/> L??tfen Listede Kalan ??la??lar?? Stoktaki Eski Yerine ??ade Ediniz.<br/><br/>" +
                                "Sipari?? Durumu Tam Teslim Al??nd?? Olarak G??ncellendi ve Sipari?? Teslim Almaya Kapat??ld??.";
                    }


                }

            }

            if (custSuppOrder != null) {
                if (custSuppOrder.getTotality() > (countOfDrugsOnlyDepot + numarator)) {

                    // ??NEML?? ==> Front k??sm??nda ilk 3 "*" kontrol edilip ona g??re yeni se??enek sunuluyor
                    return "*** Sipari??te Eksik ??la?? Bulunmaktad??r. *** <br/><br/> Sipari??te ??stenen Miktar: " + custSuppOrder.getTotality() + " <br/> Teslim" +
                            " Al??nan Miktar: " + (countOfDrugsOnlyDepot + numarator) + " <br/> Sipari?? Sonland??r??l??rsa Teslim Al??nan ??la??lar ??ptal Edilir.<br/> Sipari?? Bu Haliyle Sonland??r??ls??n m???";

                } else if (custSuppOrder.getTotality() == (countOfDrugsOnlyDepot + numarator)) {
                    custSuppOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipari?? tam teslim al??nd??
                    custSuppOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(custSuppOrder.getCustomerSupplyOrderId()));
                    custSuppOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(custSuppOrder.getCustomerSupplyOrderId()));
                    customerSupplyOrderRepository.save(custSuppOrder);

                    PurchaseOrderDrugs poDrugs = custSuppOrder.getPurchaseOrderDrugs();
                    poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim al??nd??
                    purchaseOrderDrugsRepository.save(poDrugs);

                    //yurtd?????? sipari?? durumu g??ncelleme
                    Boolean result = changeCustomerOrderStatus(custSuppOrder.getCustomerSupplyOrderId());
                    if (result == false) {
                        return "Yurtd?????? Sipari?? Durumu G??ncellenemedi";
                    }

                    return "Sipari?? Durumu Tam Teslim Al??nd?? Olarak G??ncellendi ve Sipari?? Teslim Almaya Kapat??ld??";
                }
            }

            return "Gelen ??la??lar Ba??ar??yla Depoya Eklendi";

        } catch (Exception e) {
            throw e;
        }
    }

    public String changeOrderStatus(PreDepotSearchDto dto) throws Exception {


        try {
            Optional<CustomerSupplyOrder> customerSupplyOrder = customerSupplyOrderRepository.findById(dto.getCustomerSupplyOrderId());
            CustomerOrder customerOrder = customerSupplyOrder.get().getPurchaseOrderDrugs().getCustomerOrder();


            /* Eksik Kabul Edilen ??la??lar Siliniyor */
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
                customerSupplyOrder.get().setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 20).get());//sipari?? eksik teslim al??nd??
                customerSupplyOrder.get().setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(customerSupplyOrder.get().getCustomerSupplyOrderId()));
                customerSupplyOrder.get().setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(customerSupplyOrder.get().getCustomerSupplyOrderId()));
                customerSupplyOrderRepository.save(customerSupplyOrder.get());

                PurchaseOrderDrugs poDrugs = customerSupplyOrder.get().getPurchaseOrderDrugs();
                poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 30).get());//purchase order drugs eksik teslim al??nd??
                purchaseOrderDrugsRepository.save(poDrugs);


                //yurtd?????? sipari?? durumu eksik olarak g??ncellendi
                //en az 1 tane sat??n alma eksik ise otomatik olarak yurtd?????? eksik teslim al??nd?? olarak g??ncellenecek
                customerOrder.setOrderStatus(customerOrderStatusRepository.findById((long) 40).get());
                if (!customerOrderStatusHistoryService.save(customerOrder, customerOrder.getOrderStatus()))
                    throw new Exception("Sipari?? Olu??turulamad??");
                customerOrderRepository.save(customerOrder);

                return "Sipari?? Durumu Eksik Teslim Al??nd?? Olarak G??ncellendi ve Sipari?? Teslim Almaya Kapat??ld?? ";
            }

            return "????lem S??ras??nda Hata Olu??tu";
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
                return "Sipari?? Durumu Eksik Teslim Al??nd?? Ama Devam?? Gelecek Olarak G??ncellendi";
            }

            return "????lem S??ras??nda Hata Olu??tu";
        } catch (Exception e) {
            throw e;
        }

    }

    public Boolean changeCustomerOrderStatus(Long customerSupplyOrderId) throws Exception {

        try {
            //yurtd??????na ba??l?? sat??n alma sipari??leri kontrol ediliyor ona g??re durum g??ncelleniyor

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
                        //hala teslim almay?? bekleyen sat??n alma sipari??i var ise yurt d?????? durumu de??i??mez
                        break;
                    } else if (listItem.getCustomerSupplyStatus().getCustomerSupplyStatusId() == 20) {
                        //en az 1 tane eksik sat??n alma sipari??i var ise
                        //yurtd?????? sipari?? durumu eksik olarak g??ncellenir
                        CustomerOrder customerOrder = listItem.getPurchaseOrderDrugs().getCustomerOrder();
                        customerOrder.setOrderStatus(customerOrderStatusRepository.findById((long) 40).get());
                        if (!customerOrderStatusHistoryService.save(customerOrder, customerOrder.getOrderStatus()))
                            throw new Exception("Sipari?? Olu??turulamad??");
                        customerOrderRepository.save(customerOrder);
                    } else if (counter == custSupOrderListToCustOrder.size()) {
                        //t??m sat??n alma sipari??leri tam teslim al??nm????t??r e??er cod tablosundaki total quantity e ula????ld??ysa
                        //yani sat??nalmas?? yap??lmam???? ila?? yoksa yurtd?????? sipari?? durumu tam teslim al??nd?? olarak g??ncellenir
                        CustomerOrder customerOrder = listItem.getPurchaseOrderDrugs().getCustomerOrder();
                        //cod tablosu total quantity toplam miktar??
                        Long sumOfTotalQuantityInCod = customerOrderDrugsRepository.sumOfTotalQuantityInCOD(customerOrder.getCustomerOrderId());
                       Long countOfDrugsOnlyDepotToCO=depotRepository.countOfDrugsOnlyDepotToCO(customerOrder.getCustomerOrderId());
                        //M????teri stok ise stoktaki say?? ile kontrol edilir
                        if (customerOrder.getCustomer().getCustomerId() == 1 && (sumOfTotalQuantityInCod == depotRepository.countOfDrugsOnlyStockToCO(customerOrder.getCustomerOrderId()))) {
                            customerOrder.setOrderStatus(customerOrderStatusRepository.findById((long) 50).get());
                            if (!customerOrderStatusHistoryService.save(customerOrder, customerOrder.getOrderStatus()))
                                throw new Exception("Sipari?? Olu??turulamad??");
                            customerOrderRepository.save(customerOrder);
                        } else if (customerOrder.getCustomer().getCustomerId() != 1 && (sumOfTotalQuantityInCod.equals(countOfDrugsOnlyDepotToCO))) {
                            //M????teri stok de??il ise depodaki say?? ile kontrol edilir
                            customerOrder.setOrderStatus(customerOrderStatusRepository.findById((long) 50).get());
                            if (!customerOrderStatusHistoryService.save(customerOrder, customerOrder.getOrderStatus()))
                                throw new Exception("Sipari?? Olu??turulamad??");
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
                    customerSupplyOrder.get().setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 30).get());//sipari?? reddedildi
                    customerSupplyOrder.get().setDepotTotalQuantity((long) 0);
                    customerSupplyOrder.get().setDepotStockQuantity((long) 0);
                    customerSupplyOrderRepository.save(customerSupplyOrder.get());


                    return "Sipari?? Reddedildi";
                }
                return "????lem S??ras??nda Hata Olu??tu";
            }
            return "Depoda Bu Sipari??e Ait ??la?? Bulundu??u ????in Sipari?? Reddedilemez !";
        } catch (Exception e) {
            throw e;
        }
    }

    public String clearPreDepot(PreDepotSearchDto dto) throws Exception {
        try {
            StringBuilder createSqlQuery = new StringBuilder("delete from pre_depot pd where pd.customer_supply_order_id = " + dto.getCustomerSupplyOrderId() + " and pd.pre_depot_status_id!=1");
            int countOfDeletedElements = entityManager.createNativeQuery(createSqlQuery.toString(), PreDepot.class).executeUpdate();
            return "Listede Bulunan " + countOfDeletedElements + " Adet Kay??t Ba??ar??yla Silindi";
        } catch (Exception e) {
            throw e;
        }
    }

    public int controlCommunicationStatus(CommunicationSearchDto comSearchDto, String authHeader) throws Exception {
        int result = 3;//communication bo?? ise 3 d??necek
        try {

            //Kullan??c?? Bilgisi Al??nd??
            User user = controlService.getUserFromToken(authHeader);

            //Veritaban??nda Kay??t Var m?? Kontrol?? Yap??ld??
            Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);

            if (!optUserCamera.isPresent()) {
                throw new Exception("Kullan??c?? Kamera Kayd?? Bulunamad?? !");
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

    public int controlCommunicationStatusForTest(String authHeader) throws Exception {
        int result = 3;//communication bo?? ise 3 d??necek

        //Kullan??c?? Bilgisi Al??nd??
        User user = controlService.getUserFromToken(authHeader);

        //Veritaban??nda Kay??t Var m?? Kontrol?? Yap??ld??
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);

        if (!optUserCamera.isPresent()) {
            throw new Exception("Kullan??c?? Kamera Kayd?? Bulunamad?? !");
        }

        try {


            StringBuilder createSqlQuery = new StringBuilder("select * from communication c " +
                    "where c.barcode='11111111111111' and c.camera_type=" + optUserCamera.get().getCameraType() +
                    "and c.status=1 " +
                    "and c.total_quantity=1");

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

    public List<PreDepotDto> getAllPreDepotForTest(String authHeader) throws Exception {
        try {

            //Kullan??c?? Bilgisi Al??nd??
            User user = controlService.getUserFromToken(authHeader);

            //Veritaban??nda Kay??t Var m?? Kontrol?? Yap??ld??
            Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);
            if (!optUserCamera.isPresent()) {
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

                    //System.out.println("??la?? Seri No: " + serialNo.toString());
                }

                //??la?? SKT Al??nd??.
                if (qrCode.charAt(secondGroupSeperatorIndex + 1) == '1' && qrCode.charAt(secondGroupSeperatorIndex + 2) == '7') {
                    expirationDate.append("20");
                    expirationDate.append(qrCode.substring(secondGroupSeperatorIndex + 3, secondGroupSeperatorIndex + 5));
                    expirationDate.append("-");
                    expirationDate.append(qrCode.substring(secondGroupSeperatorIndex + 5, secondGroupSeperatorIndex + 7));
                    expirationDate.append("-");
                    expirationDate.append(qrCode.substring(secondGroupSeperatorIndex + 7, secondGroupSeperatorIndex + 9));

                    // System.out.println("??la?? SKT: " + expirationDate.toString());
                }

                //??la?? Parti Numaras?? Al??nd??.
                if (qrCode.charAt(secondGroupSeperatorIndex + 9) == '1' && qrCode.charAt(secondGroupSeperatorIndex + 10) == '0') {
                    lotNo.append(qrCode.substring(secondGroupSeperatorIndex + 11, qrCode.length()));

                    //  System.out.println("??la?? Parti No: " + lotNo.toString());
                }

                //??TS No Al??nd??.
                if (barcode.length() > 0 && serialNo.length() > 0) {
                    itsNo.append(barcode + "21" + serialNo);

                    // System.out.println("??ts No: " + itsNo.toString());
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
                        //  System.out.println("??art Sa??land??");
                    } else {
                        preDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 3).get());
                        // System.out.println("??art Sa??lanmad??");
                    }
                } else {
                    preDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 4).get());
                    //System.out.println("Yanl???? ??la??");
                }


                //preDepot tablosuna yeni kay??t olarak eklendi.
                preDepotRepository.save(mapper.map(preDepot, PreDepot.class));

                //preDepot'a eklenen kay??t QrCode tablosundan silindi.
                //qrCodeRepository.deleteById(QRCode.getQrCodeId());

                //preDepot'a eklenen kay??t QrCode tablosunda status degi??tirdi.
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

    public String saveManualAcceptanceQrCodes(OrderManualAcceptanceDto dto, String authHeader) throws Exception {
        if (dto.getCustomerSupplyOrderId() == null || dto.getQuantity() == null || dto.getStt() == null || dto.getLotNo().trim().length() == 0) {
            throw new Exception("Manuel Teslim Almada Eksik Bilgi Bulunmaktad??r !");
        }

        User user = controlService.getUserFromToken(authHeader);


        Optional<CustomerSupplyOrder> optCustomerSupplyOrder = customerSupplyOrderRepository.findById(dto.getCustomerSupplyOrderId());
        if (!optCustomerSupplyOrder.isPresent()) {
            throw new Exception("Sat??n Alma Sipari??i Bulunamad?? !");
        }

        OrderAcceptanceCustomerSupplyOrderDto customerSupplyOrderDto = mapper.map(optCustomerSupplyOrder.get(), OrderAcceptanceCustomerSupplyOrderDto.class);

        if (customerSupplyOrderDto != null && (customerSupplyOrderDto.getCustomerSupplyStatus().getCustomerSupplyStatusId() == 20 || customerSupplyOrderDto.getCustomerSupplyStatus().getCustomerSupplyStatusId() == 50)) {
            return "Teslim Almas?? Sonland??r??lan Sipari??e ??la?? Kabul Edilemez!";
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

                            //sipari?? tam teslim al??nd?? olacak ve uyar?? verilecek ekrana fazla diye

                            customerSupplyOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipari?? tam teslim al??nd??
                            customerSupplyOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(customerSupplyOrder.getCustomerSupplyOrderId()));
                            customerSupplyOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(customerSupplyOrder.getCustomerSupplyOrderId()));
                            customerSupplyOrderRepository.save(customerSupplyOrder);

                            PurchaseOrderDrugs poDrugs = customerSupplyOrder.getPurchaseOrderDrugs();
                            poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim al??nd??
                            purchaseOrderDrugsRepository.save(poDrugs);

                            //yurtd?????? sipari?? durumu g??ncelleme
                            Boolean result = changeCustomerOrderStatus(customerSupplyOrder.getCustomerSupplyOrderId());
                            if (result == false) {
                                throw new Exception("Yurtd?????? Sipari?? Durumu G??ncellenemedi");
                            }
                            return "Okutulan ??la?? Say??s??, Sipari?? Miktar??ndan Fazla Olamaz.<br/> " +
                                    "Bu Sipari??te ??stenen Miktardan Fazla ??la?? Bulunmaktad??r.<br/>" +
                                    "Sipari?? Durumu Tam Teslim Al??nd?? Olarak G??ncellendi ve Sipari?? Teslim Almaya Kapat??ld??.";
                        }
                    }

                    //for bitince kontrol edilecek tamm?? eksikmi diye ona g??re sipari??lerin statusu de??i??ecek
                    if (numarator == depotList.size()) {

                        //sipari?? tam teslim al??nd?? olacak ve uyar?? verilecek ekrana fazla diye

                        customerSupplyOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipari?? tam teslim al??nd??
                        customerSupplyOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(customerSupplyOrder.getCustomerSupplyOrderId()));
                        customerSupplyOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(customerSupplyOrder.getCustomerSupplyOrderId()));
                        customerSupplyOrderRepository.save(customerSupplyOrder);

                        PurchaseOrderDrugs poDrugs = customerSupplyOrder.getPurchaseOrderDrugs();
                        poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim al??nd??
                        purchaseOrderDrugsRepository.save(poDrugs);

                        //yurtd?????? sipari?? durumu g??ncelleme
                        Boolean result = changeCustomerOrderStatus(customerSupplyOrder.getCustomerSupplyOrderId());
                        if (result == false) {
                            throw new Exception("Yurtd?????? Sipari?? Durumu G??ncellenemedi");
                        }

                        return "Sipari?? Durumu Tam Teslim Al??nd?? Olarak G??ncellendi ve Sipari?? Teslim Almaya Kapat??ld??.";

                    } else if (numarator < depotList.size()) {
                        // ??NEML?? ==> Front k??sm??nda ilk 3 "*" kontrol edilip ona g??re yeni se??enek sunuluyor
                        return "*** Sipari??te Eksik ??la?? Bulunmaktad??r. ***<br/><br/> Sipari??te ??stenen Miktar: " + customerSupplyOrderDto.getTotality() + " <br/> Teslim" +
                                " Al??nan Miktar: " + (customerSupplyOrderDto.getTotality() - (depotList.size() - numarator)) + " <br/> Sipari?? Sonland??r??l??rsa Teslim Al??nan ??la??lar ??ptal Edilir.<br/>Sipari?? Bu Haliyle Sonland??r??ls??n m???";


                    }


                } else {
                    return "Stokta Bu Sipari??e Ayr??lm???? ??la?? Bulunamad??";
                }


            } else {
                return "??la?? SKT ??art?? Sa??lanm??yor.";
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
                            depot.setDepotStatus(depotStatusRepository.findById(10L).get());//Stok Sipari??i ??se
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

                        //sipari?? tam teslim al??nd?? olacak ve uyar?? verilecek ekrana fazla diye

                        customerSupplyOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipari?? tam teslim al??nd??
                        customerSupplyOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(customerSupplyOrder.getCustomerSupplyOrderId()));
                        customerSupplyOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(customerSupplyOrder.getCustomerSupplyOrderId()));
                        customerSupplyOrderRepository.save(customerSupplyOrder);

                        PurchaseOrderDrugs poDrugs = customerSupplyOrder.getPurchaseOrderDrugs();
                        poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim al??nd??
                        purchaseOrderDrugsRepository.save(poDrugs);

                        //yurtd?????? sipari?? durumu g??ncelleme
                        Boolean result = changeCustomerOrderStatus(customerSupplyOrder.getCustomerSupplyOrderId());
                        if (result == false) {
                            throw new Exception("Yurtd?????? Sipari?? Durumu G??ncellenemedi");
                        }

                        return "Okutulan ??la?? Say??s??, Sipari?? Miktar??ndan Fazla Olamaz.<br/> " +
                                "Bu Sipari??te ??stenen Miktardan Fazla ??la?? Bulunmaktad??r.<br/>" +
                                "Sipari?? Durumu Tam Teslim Al??nd?? Olarak G??ncellendi ve Sipari?? Teslim Almaya Kapat??ld??.";


                    }
                }
                //for bitince kontrol edilecek tamm?? eksikmi diye ona g??re sipari??lerin statusu de??i??ecek
                if (countInDepot + countInStock + numarator == customerSupplyOrderDto.getTotality()) {

                    //sipari?? tam teslim al??nd?? olacak ve uyar?? verilecek ekrana fazla diye

                    customerSupplyOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());//sipari?? tam teslim al??nd??
                    customerSupplyOrder.setDepotTotalQuantity((long) depotRepository.countOfDrugsOnlyDepot(customerSupplyOrder.getCustomerSupplyOrderId()));
                    customerSupplyOrder.setDepotStockQuantity((long) depotRepository.countOfDrugsOnlyStock(customerSupplyOrder.getCustomerSupplyOrderId()));
                    customerSupplyOrderRepository.save(customerSupplyOrder);

                    PurchaseOrderDrugs poDrugs = customerSupplyOrder.getPurchaseOrderDrugs();
                    poDrugs.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());//purchase order drugs tam teslim al??nd??
                    purchaseOrderDrugsRepository.save(poDrugs);

                    //yurtd?????? sipari?? durumu g??ncelleme
                    Boolean result = changeCustomerOrderStatus(customerSupplyOrder.getCustomerSupplyOrderId());
                    if (result == false) {
                        throw new Exception("Yurtd?????? Sipari?? Durumu G??ncellenemedi");
                    }

                    return "Sipari?? Durumu Tam Teslim Al??nd?? Olarak G??ncellendi ve Sipari?? Teslim Almaya Kapat??ld??.";

                } else if (countInDepot + countInStock + numarator < customerSupplyOrderDto.getTotality()) {
                    // ??NEML?? ==> Front k??sm??nda ilk 3 "*" kontrol edilip ona g??re yeni se??enek sunuluyor
                    return "*** Sipari??te Eksik ??la?? Bulunmaktad??r. ***<br/><br/> Sipari??te ??stenen Miktar: " + customerSupplyOrderDto.getTotality() + " <br/> Teslim" +
                            " Al??nan Miktar: " + (countInDepot + countInStock + numarator) + " <br/> Sipari?? Sonland??r??l??rsa Teslim Al??nan ??la??lar ??ptal Edilir.<br/>Sipari?? Bu Haliyle Sonland??r??ls??n m???";


                }
            }


        }
        return "??la?? SKT ??art?? Sa??lanm??yor.";
    }

    public Boolean handReaderAccept(HandReaderOrderAcceptanceAcceptDto dto, String authHeader) throws Exception {
        Boolean control = false;

        User user = controlService.getUserFromToken(authHeader);
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);
        if (optUserCamera.isPresent()) {
            communicationRepository.deleteByCameraType(optUserCamera.get().getCameraType());
            qrCodeRepository.deleteByCameraType(optUserCamera.get().getCameraType());
        } else {
            throw new Exception("Kullan??c?? Kamera Bilgisi Bulunamad?? !");
        }
        //List<QrCode> qrCodeList = qrCodeRepository.findByCameraType(optUserCamera.get().getCameraType());
        if (dto.getQrcodeList() == null)
            throw new Exception("Eklemek ??stedi??iniz Karekodlar?? Giriniz");

        if (dto.getQrcodeList().trim().length() <= 0)
            throw new Exception("Eklemek ??stedi??iniz Karekodlar?? Giriniz");

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
            //??la?? Barkod Kontrol??
            if (qrCode.length() > 29) {
                if (qrCode.charAt(0) == '0' && qrCode.charAt(1) == '1') {
                    barcode.append(qrCode.substring(0, 16)); // 01 dahil edildi.
                }

                //??la?? Seri Numaras?? Kontrol??
                if (qrCode.charAt(16) == '2' && qrCode.charAt(17) == '1') {
                    String expDate = "";

                    // 19   20   21 22 23 24 25 26
                    expDateStartIndex = qrCode.indexOf("17", 18);

                    if (expDateStartIndex > 0  /* && partiNoStartIndex>0*/) {

                        // 17AABBCC10 ile olu??an b??lumdek?? 17 karakter??ndek?? 1 in index numaras??n?? ar??yor.
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
            throw new Exception("Kullan??c?? Kamera Bilgisi Bulunamad?? !");
        }
        //List<QrCode> qrCodeList = qrCodeRepository.findByCameraType(optUserCamera.get().getCameraType());
        if (dto.getQrcodeList() == null)
            throw new Exception("Eklemek ??stedi??iniz Karekodlar?? Giriniz");

        if (dto.getQrcodeList().trim().length() <= 0)
            throw new Exception("Eklemek ??stedi??iniz Karekodlar?? Giriniz");

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
            //??la?? Barkod Kontrol??
            if (qrCode.length() > 29) {
                if (qrCode.charAt(0) == '0' && qrCode.charAt(1) == '1') {
                    barcode.append(qrCode.substring(0, 16)); // 01 dahil edildi.
                }

                //??la?? Seri Numaras?? Kontrol??
                if (qrCode.charAt(16) == '2' && qrCode.charAt(17) == '1') {
                    String expDate = "";

                    // 19   20   21 22 23 24 25 26
                    expDateStartIndex = qrCode.indexOf("17", 18);

                    if (expDateStartIndex > 0  /* && partiNoStartIndex>0*/) {

                        // 17AABBCC10 ile olu??an b??lumdek?? 17 karakter??ndek?? 1 in index numaras??n?? ar??yor.
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
