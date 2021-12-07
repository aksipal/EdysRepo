package com.via.ecza.service;

import com.via.ecza.dto.OrderChangingCustomerSupplyOrderDto;
import com.via.ecza.dto.OrderChangingExchangeCsoDto;
import com.via.ecza.dto.OrderChangingSearchDto;
import com.via.ecza.dto.OrderChangingToStockOrderDto;
import com.via.ecza.entity.CustomerOrder;
import com.via.ecza.entity.CustomerSupplyOrder;
import com.via.ecza.entity.Depot;
import com.via.ecza.entity.PurchaseOrderDrugs;
import com.via.ecza.repo.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderChangingService {
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
    private CustomerOrderStatusHistoryService customerOrderStatusHistoryService;

    public Page<OrderChangingCustomerSupplyOrderDto> search(OrderChangingSearchDto dto, Integer pageNo, Integer pageSize, String sortBy) {
        /* Sadece Depo İçin Gelenler Listeleniyor, Stok Siparişleri Listede Gelmeyecek
           Customer Order Id'si 30-40-50-90 Olanlar Listelenecek
           Customer Supply Order 20-50 Olanlar Listelenecek
           Customer Stok Olmayacak */
        StringBuilder createSqlQuery = new StringBuilder("select * from customer_supply_order cso " +
                "inner join drug_card dc on dc.drug_card_id=cso.drug_card_id " +
                "inner join supplier s on s.supplier_id=cso.supplier_id " +
                "inner join purchase_order_drugs pod on pod.purchase_order_drugs_id =cso.purchase_order_drugs_id " +
                "inner join customer_order co on co.customer_order_id =pod.customer_order_id " +
                "where (cso.customer_supply_status_id=20 or cso.customer_supply_status_id=50) " +
                "and cso.depot_total_quantity>0 and co.customer_id !=1 " +
                "and (co.order_status_id =15 or co.order_status_id =30 or co.order_status_id =40 or co.order_status_id =50 or co.order_status_id =90)");

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

        OrderChangingCustomerSupplyOrderDto[] dtos = mapper.map(list, OrderChangingCustomerSupplyOrderDto[].class);
        List<OrderChangingCustomerSupplyOrderDto> dtosList = Arrays.asList(dtos);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        int start = Math.min((int) paging.getOffset(), dtosList.size());
        int end = Math.min((start + paging.getPageSize()), dtosList.size());

        Page<OrderChangingCustomerSupplyOrderDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, dtosList.size());

        return pageList;

    }


    public Boolean changeToStockOrder(OrderChangingToStockOrderDto dto) throws Exception {


        try {
            Optional<CustomerSupplyOrder> customerSupplyOrder = customerSupplyOrderRepository.findById(dto.getCustomerSupplyOrderId());
            Optional<CustomerOrder> customerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());

            if (!customerOrder.isPresent() || !customerOrder.isPresent()) {
                throw new Exception("Sipariş Kaydırma İşleminde Sipariş Bilgisi Bulunamdı !");
            }

            /* depoya kabul edilen miktarı stoğa ekledik, depoyu 0 yaptık */
            Long depotCount = customerSupplyOrder.get().getDepotTotalQuantity();
            customerSupplyOrder.get().setDepotStockQuantity(customerSupplyOrder.get().getDepotStockQuantity() + depotCount);
            customerSupplyOrder.get().setDepotTotalQuantity(0L);
            customerSupplyOrderRepository.save(customerSupplyOrder.get());

            /* stoğa kaydırılan adet, sipariş verilmiş miktardan çıkarıldı sipariş verilmemiş miktara eklendi  */
            PurchaseOrderDrugs pod = customerSupplyOrder.get().getPurchaseOrderDrugs();
            pod.setChargedQuantity(pod.getChargedQuantity() - depotCount);
            pod.setIncompleteQuantity(pod.getIncompleteQuantity() + depotCount);
            if (pod.getChargedQuantity() == 0) {
                /* satın almaya başlanmadı */
                pod.setPurchaseStatus(purchaseStatusRepository.findById(10L).get());
            } else {
                /* eksik satın alma */
                pod.setPurchaseStatus(purchaseStatusRepository.findById(20L).get());
            }
            purchaseOrderDrugsRepository.save(pod);

            /* Depodaki ilaçların durumu stok olarak değişiyor */
            depotRepository.updateDrugsFromDepotToStock(customerSupplyOrder.get().getCustomerSupplyOrderId(), customerOrder.get().getCustomerOrderId(), customerSupplyOrder.get().getDrugCard().getDrugCardId());

            /* Yurtdışı sipariş durumu değiştirildi ve satın almaya düşmesi sağlandı */
            customerOrder.get().setOrderStatus(customerOrderStatusRepository.findById(15L).get());
            if(!customerOrderStatusHistoryService.save(customerOrder.get(),customerOrderStatusRepository.findById(15L).get()))
                throw new Exception("Sipariş Oluşturulamadı");
            customerOrderRepository.save(customerOrder.get());


            return true;
        } catch (Exception e) {
            throw e;
        }

    }


    public OrderChangingCustomerSupplyOrderDto findByCsoNo(String customerSupplyOrderNo) throws Exception {
        try {
            Optional<CustomerSupplyOrder> cso = customerSupplyOrderRepository.findBySupplyOrderNo(customerSupplyOrderNo);
            if (cso.isPresent()) {
                OrderChangingCustomerSupplyOrderDto dto = mapper.map(cso.get(), OrderChangingCustomerSupplyOrderDto.class);
                return dto;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw e;
        }

    }

    public String exchangeWrongCSO(OrderChangingExchangeCsoDto dto) throws Exception {
        try {
            Optional<CustomerSupplyOrder> csoFalse = customerSupplyOrderRepository.findById(dto.getCsoFalseId());

            if (!csoFalse.isPresent()) {
                throw new Exception("Satın Alma Siparişi Bulunamadı !");
            }


            /* depodaki ilaç listesi */
            List<Depot> drugListF = depotRepository.getDrugListForExchange(csoFalse.get().getCustomerSupplyOrderId(), csoFalse.get().getDrugCard().getDrugCardId());

            if (drugListF.size() <= 0) {
                throw new Exception("Depoda Satın Almaya Ait İlaç Bulunamadı !");
            }
            // depodaki ilaçlar siliniyor
            for (Depot drug : drugListF) {
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


            csoFalse.get().setCustomerSupplyStatus(customerSupplyStatusRepository.findById(10L).get());//satın alma beklemede
            csoFalse.get().setDepotTotalQuantity(null);
            csoFalse.get().setDepotStockQuantity(null);
            customerSupplyOrderRepository.save(csoFalse.get());

            PurchaseOrderDrugs poDrugs = csoFalse.get().getPurchaseOrderDrugs();
            poDrugs.setPurchaseStatus(purchaseStatusRepository.findById(40L).get());//purchase order drugs teslim alması bekleniyor
            purchaseOrderDrugsRepository.save(poDrugs);


            //yurtdışı sipariş durumu depo aşamasında olarak değişti
            CustomerOrder customerOrder = csoFalse.get().getPurchaseOrderDrugs().getCustomerOrder();
            customerOrder.setOrderStatus(customerOrderStatusRepository.findById(30L).get());
            if(!customerOrderStatusHistoryService.save(customerOrder,customerOrder.getOrderStatus()))
                throw new Exception("Sipariş Oluşturulamadı");
            customerOrderRepository.save(customerOrder);

            return "Satın Almaya Ait Teslim Alma İptal Edildi ve Depodaki İlaçlar Silindi.<br/>Teslim Alma İşlemini Yeniden Gerçekleştirebilirsiniz.";


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
                        if(!customerOrderStatusHistoryService.save(customerOrder,customerOrder.getOrderStatus()))
                            throw new Exception("Sipariş Oluşturulamadı");
                        customerOrderRepository.save(customerOrder);
                    } else if (counter == custSupOrderListToCustOrder.size()) {
                        //tüm satın alma siparişleri tam teslim alınmıştır eğer cod tablosundaki total quantity e ulaşıldıysa
                        //yani satınalması yapılmamış ilaç yoksa yurtdışı sipariş durumu tam teslim alındı olarak güncellenir
                        CustomerOrder customerOrder = listItem.getPurchaseOrderDrugs().getCustomerOrder();
                        //cod tablosu total quantity toplam miktarı
                        Long sumOfTotalQuantityInCod = customerOrderDrugsRepository.sumOfTotalQuantityInCOD(customerOrder.getCustomerOrderId());
                        //Müşteri stok ise stoktaki sayı ile kontrol edilir
                        if (customerOrder.getCustomer().getCustomerId() == 1 && (sumOfTotalQuantityInCod == depotRepository.countOfDrugsOnlyStockToCO(customerOrder.getCustomerOrderId()))) {
                            customerOrder.setOrderStatus(customerOrderStatusRepository.findById((long) 50).get());
                            if(!customerOrderStatusHistoryService.save(customerOrder,customerOrder.getOrderStatus()))
                                throw new Exception("Sipariş Oluşturulamadı");
                            customerOrderRepository.save(customerOrder);
                        } else if (customerOrder.getCustomer().getCustomerId() != 1 && (sumOfTotalQuantityInCod == depotRepository.countOfDrugsOnlyDepotToCO(customerOrder.getCustomerOrderId()))) {
                            //Müşteri stok değil ise depodaki sayı ile kontrol edilir
                            customerOrder.setOrderStatus(customerOrderStatusRepository.findById((long) 50).get());
                            if(!customerOrderStatusHistoryService.save(customerOrder,customerOrder.getOrderStatus()))
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

}
