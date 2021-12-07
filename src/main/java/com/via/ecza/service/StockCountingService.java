package com.via.ecza.service;

import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.repo.*;
import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class StockCountingService {
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
    private PurchaseOrderDrugsRepository purchaseOrderDrugsRepository;
    @Autowired
    private PurchaseStatusRepository purchaseStatusRepository;
    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private DepotRepository depotRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private DepotStatusRepository depotStatusRepository;
    @Autowired
    private CustomerSupplyStatusRepository customerSupplyStatusRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private UserCameraRepository userCameraRepository;
    @Autowired
    private ControlService controlService;

    public List<PreDepotDto> editAcceptance(String authHeader) throws NotFoundException, ParseException {

        try {

            CustomerOrder customerOrder = customerOrderRepository.findById((long) 1).get();

            //Karekod Anlamlı Hale Getirilip Pre Depot'a Kaydediliyor
            Boolean seperateQrCode = seperateQrCode(customerOrder, authHeader);
            if (seperateQrCode != true) {
                throw new NotFoundException("Karekod Bölünemedi..");
            }


            List<PreDepot> list = preDepotRepository.getAllWithCustomerOrderId();
            PreDepotDto[] array = mapper.map(list, PreDepotDto[].class);
            List<PreDepotDto> dtoList = Arrays.asList(array);

            if (dtoList.size() > 0) {
                //stok sayımında genel olarak hangi ilaçtan kaç adet var listesi
                HashMap<String, Integer> drugCountMap = new HashMap<>();
                for (PreDepotDto dto : dtoList) {
                    if (drugCountMap.get(dto.getDrugName()) != null) {
                        //ilaç daha önce var ise sayısı 1 artar
                        Integer tempValue = drugCountMap.get(dto.getDrugName());
                        tempValue++;
                        drugCountMap.replace(dto.getDrugName(), tempValue);
                    } else {
                        //ilaç hiç yoksa sayısı 1 olarak listeye eklenir
                        drugCountMap.put(dto.getDrugName(), 1);
                    }
                }

                if (drugCountMap.size() > 0) {
                    List<StockCountingDrugSummary> summaryList = new ArrayList<>();
                    drugCountMap.forEach((k, v) -> {
                        StockCountingDrugSummary summary = new StockCountingDrugSummary();
                        summary.setDrugName(k);
                        summary.setDrugCount(v);
                        summaryList.add(summary);
                    });
                    //toplam sonuç ilk elemanın list değişkenine set edildi.
                    dtoList.get(0).setDrugSummaryList(summaryList);
                }
            }

            return dtoList;
        } catch (NotFoundException e) {
            throw e;
        } catch (NumberFormatException e) {
            throw e;
        } catch (ParseException e) {
            throw e;
        }

    }

    public boolean seperateQrCode(CustomerOrder customerOrder, String authHeader) throws ParseException, NotFoundException {

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
                    //System.out.println("İlaç Barkod: " + barcode.toString());
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

                    //System.out.println("İlaç SKT: " + expirationDate.toString());
                }

                //İlaç Parti Numarası Alındı.
                if (qrCode.charAt(secondGroupSeperatorIndex + 9) == '1' && qrCode.charAt(secondGroupSeperatorIndex + 10) == '0') {
                    lotNo.append(qrCode.substring(secondGroupSeperatorIndex + 11, qrCode.length()));

                    //System.out.println("İlaç Parti No: " + lotNo.toString());
                }

                //İTS No Alındı.
                if (barcode.length() > 0 && serialNo.length() > 0) {
                    itsNo.append(barcode + "21" + serialNo);

                    //System.out.println("İts No: " + itsNo.toString());
                }

                PreDepotDto preDepot = new PreDepotDto();
                preDepot.setDrugBarcode(Long.valueOf(barcode.toString()));
                preDepot.setDrugSerialNo(serialNo.toString());


                Date exp = new SimpleDateFormat("yyyy-MM-dd").parse(expirationDate.toString());
                preDepot.setDrugExpirationDate(exp);
                preDepot.setAdmitionDate(new Date());

                preDepot.setDrugLotNo(lotNo.toString());
                preDepot.setDrugItsNo(itsNo.toString());

                preDepot.setCustomerOrder(mapper.map(customerOrder, DepotCustomerOrderListDto.class));
                preDepot.setCustomerSupplyOrder(null);


                Optional<DrugCard> drug = drugCardRepository.findByDrugCode(preDepot.getDrugBarcode());
                preDepot.setDrugName(drug.get().getDrugName());


                //ilaç SKT Geçti mi? diye kontrol ediliyor
                if (exp.after(new Date())) {
                    preDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 2).get());
                    //System.out.println("Şart Sağlandı");
                } else {
                    preDepot.setPreDepotStatus(preDepotStatusRepository.findById((long) 3).get());
                    //System.out.println("Şart Sağlanmadı");
                }


                //preDepot tablosuna yeni kayıt olarak eklendi.
                preDepotRepository.save(mapper.map(preDepot, PreDepot.class));

                //preDepot'a eklenen kayıt QrCode tablosundan silindi.
                // qrCodeRepository.deleteById(QRCode.getQrCodeId());

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

            //Kullanıcı Bilgisi Alındı
            User user = controlService.getUserFromToken(authHeader);

            DepotDto depot = null;
            CustomerSupplyOrder custSuppOrder = null;
            List<AcceptanceCheckedListDto> dtoList = dtoOrderAcceptance.getCheckedList();
            if (dtoList.size() > 0) {
                int numarator = 0;//seçili olan ilaç sayısı


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

                        /* Eğer İlaç Daha Önce Yurtdışı Siparişe Eklendiyse Miktarda Artırma Yapılır,
                        Eklenmediyse Yurtdışı Siparişe Yeni İlaç Olarak Eklenir */

                        DrugCard dc = drugCardRepository.findByDrugCode(dto.getDrugBarcode()).get();

                        //CUSTOMER ORDER DRUGS KONTROLÜ BAŞLANGIÇ
                        List<CustomerOrderDrugs> coDrugsList = customerOrderDrugsRepository.SearchInCustOrdDrugs(dc.getDrugCardId(), dto.getCustomerOrder().getCustomerOrderId());
                        if (coDrugsList.size() > 0) {
                            CustomerOrderDrugs cod = coDrugsList.get(0);
                            cod.setIncompleteQuantity(cod.getIncompleteQuantity() + 1);
                            cod.setTotalQuantity(cod.getTotalQuantity() + 1);
                            customerOrderDrugsRepository.save(cod);
                        } else {
                            CustomerOrderDrugs cod = new CustomerOrderDrugs();
                            Date date = new SimpleDateFormat("yyyy-MM-dd").parse("2021-01-01");
                            cod.setExpirationDate(date);
                            cod.setCreatedDate(new Date());
                            cod.setProfit(0);
                            cod.setIncompleteQuantity((long) 1);
                            cod.setTotalQuantity((long) 1);
                            cod.setCustomerOrder(dto.getCustomerOrder());
                            cod.setDrugCard(dc);
                            customerOrderDrugsRepository.save(cod);

                        }
                        //CUSTOMER ORDER DRUGS KONTROLÜ SON

                        //PURCHASE ORDER DRUGS KONTROLÜ BAŞLANGIÇ
                        List<PurchaseOrderDrugs> poDrugsList = purchaseOrderDrugsRepository.SearchInPurchaseOrdDrugs(dc.getDrugCardId(), dto.getCustomerOrder().getCustomerOrderId());
                        if (poDrugsList.size() > 0) {
                            PurchaseOrderDrugs pod = poDrugsList.get(0);
                            pod.setIncompleteQuantity(pod.getIncompleteQuantity() + 0);
                            pod.setTotalQuantity(pod.getTotalQuantity() + 1);
                            pod.setChargedQuantity(pod.getChargedQuantity() + 1);
                            purchaseOrderDrugsRepository.save(pod);
                        } else {
                            PurchaseOrderDrugs pod = new PurchaseOrderDrugs();
                            Date date = new SimpleDateFormat("yyyy-MM-dd").parse("2021-01-01");
                            pod.setExpirationDate(date);
                            pod.setIncompleteQuantity((long) 0);
                            pod.setTotalQuantity((long) 1);
                            pod.setChargedQuantity((long) 1);
                            pod.setCustomerOrder(dto.getCustomerOrder());
                            pod.setDrugCard(dc);
                            pod.setPurchaseStatus(purchaseStatusRepository.findById((long) 50).get());
                            purchaseOrderDrugsRepository.save(pod);
                        }
                        //PURCHASE ORDER DRUGS KONTROLÜ SON

                        //CUSTOMER SUPPLY ORDER KONTROLÜ BAŞLANGIÇ
                        PurchaseOrderDrugs poDrugs = purchaseOrderDrugsRepository.SearchInPurchaseOrdDrugs(dc.getDrugCardId(), dto.getCustomerOrder().getCustomerOrderId()).get(0);
                        List<CustomerSupplyOrder> csoList = customerSupplyOrderRepository.SearchInCustSuppOrder(dc.getDrugCardId(), poDrugs.getPurchaseOrderDrugsId(), (long) 1);
                        if (csoList.size() > 0) {
                            CustomerSupplyOrder cso = csoList.get(0);
                            cso.setTotalQuantity(cso.getTotalQuantity() + 1);
                            cso.setTotality(cso.getTotality() + 1);
                            cso.setQuantity(cso.getQuantity() + 1);
                            cso.setDepotStockQuantity(cso.getDepotStockQuantity() + 1);
                            custSuppOrder = customerSupplyOrderRepository.save(cso);
                        } else {
                            CustomerSupplyOrder cso = new CustomerSupplyOrder();
                            cso.setTotalQuantity((long) 1);
                            cso.setTotality((long) 1);
                            cso.setQuantity((long) 1);
                            cso.setSupplier(supplierRepository.findById((long) 1).get());
                            cso.setPurchaseOrderDrugs(poDrugs);
                            cso.setDrugCard(dc);
                            cso.setCustomerSupplyStatus(customerSupplyStatusRepository.findById((long) 50).get());
                            cso.setSupervisorId((long) 1);
                            cso.setCreatedAt(new Date());
                            cso.setDepotTotalQuantity((long) 0);
                            cso.setDepotStockQuantity((long) 1);
                            cso = customerSupplyOrderRepository.save(cso);
                            cso.setSupplyOrderNo(getCode(cso.getCustomerSupplyOrderId()));
                            custSuppOrder = customerSupplyOrderRepository.save(cso);
                        }
                        //CUSTOMER SUPPLY ORDER KONTROLÜ SON

                        depot.setCustomerSupplyOrder(mapper.map(custSuppOrder, DepotCustomerSupplierOrderListDto.class));
                        depot.setAdmitionDate(new Date());
                        if (dtoOrderAcceptance.getDrugPosition() != null && dtoOrderAcceptance.getDrugPosition().trim().length() != 0) {
                            depot.setPosition(dtoOrderAcceptance.getDrugPosition().trim());
                        } else {
                            depot.setPosition(null);
                        }

                        if (dtoOrderAcceptance.getStockCountingExplanation() != null && dtoOrderAcceptance.getStockCountingExplanation().trim().length() != 0) {
                            depot.setStockCountingExplanation(dtoOrderAcceptance.getStockCountingExplanation().trim());
                            depot.setStatus(0);
                        } else {
                            depot.setStockCountingExplanation(null);
                            depot.setStatus(0);
                        }

                        depot.setUser(user);
                        depot.setNote(null);
                        depot.setDepotStatus(depotStatusRepository.findById((long) 10).get());

                        Optional<Depot> itsControl = depotRepository.findByItsNo(dto.getDrugItsNo());
                        if (itsControl.isPresent()) {
                            numarator--;
                            preDepotRepository.deleteById(dto.getPreDepotId());
                            return dto.getDrugSerialNo() + " seri numaralı ilaç depoya daha önce eklenmiştir. <br/> Lütfen Bu İlacı Depoya Teslim Ediniz.";
                        } else {

                            depotRepository.save(mapper.map(depot, Depot.class));
                            dto.setPreDepotStatus(preDepotStatusRepository.findById((long) 1).get());
                            preDepotRepository.save(dto);
                        }
                    }
                }
                return "Gelen İlaçlar Başarıyla Stoğa Eklendi";
            }

            return "Stoğa Eklenecek İlaç Bulunamadı";
        } catch (Exception e) {
            throw e;
        }
    }

    private String getCode(Long customerSupplyOrderId) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String code = "SAT-" + year;
        int size = customerSupplyOrderId.toString().length();
        for (int i = 0; i < 5 - size; i++)
            code += "0";
        code += customerSupplyOrderId;
        return code;
    }

    public String clearPreDepot(PreDepotSearchDto dto) throws Exception {
        try {
            StringBuilder createSqlQuery = new StringBuilder("delete from pre_depot pd where pd.customer_order_id = 1 and pd.pre_depot_status_id!=1");
            int countOfDeletedElements = entityManager.createNativeQuery(createSqlQuery.toString(), PreDepot.class).executeUpdate();
            return "Listede Bulunan " + countOfDeletedElements + " Adet Kayıt Başarıyla Silindi";
        } catch (Exception e) {
            throw e;
        }
    }


}

