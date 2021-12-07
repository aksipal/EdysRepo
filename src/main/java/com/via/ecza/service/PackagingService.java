package com.via.ecza.service;

import com.via.ecza.config.AppConfiguration;
import com.via.ecza.repo.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Service
@Transactional
public class PackagingService {
    @Autowired
    private QrCodeRepository qrCodeRepository;
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private CustomerSupplyOrderRepository customerSupplyOrderRepository;
    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private DepotRepository depotRepository;
    @Autowired
    private DepotStatusRepository depotStatusRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private BoxDrugListRepository boxDrugListRepository;
    @Autowired
    private SmallBoxRepository smallBoxRepository;
    @Autowired
    private BarcodeRepository barcodeRepository;
    @Autowired
    private BoxRepository boxRepository;
    @Autowired
    private BoxTypeRepository boxTypeRepository;
    @Autowired
    private AppConfiguration app;

//    public Page<PackagingCustomerSupplyOrderDto> search(PackagingSearchDto dto, Integer pageNo, Integer pageSize, String sortBy) {
//        StringBuilder createSqlQuery = new StringBuilder("select * from customer_supply_order cso where 1=1  ");
//        if(dto.getSupplyOrderNo() != null)		createSqlQuery.append("and cso.supply_order_no ILIKE '%"+dto.getSupplyOrderNo().trim()+"%' ");
//        createSqlQuery.append("order by cso.supply_order_no asc");
//
//
//        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerSupplyOrder.class).getResultList();
//
//        PackagingCustomerSupplyOrderDto[] dtos = mapper.map(list,PackagingCustomerSupplyOrderDto[].class );
//        List<PackagingCustomerSupplyOrderDto> dtosList=Arrays.asList(dtos);
//
//        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
//        int start = Math.min((int) paging.getOffset(), dtosList.size());
//        int end = Math.min((start + paging.getPageSize()), dtosList.size());
//
//        Page<PackagingCustomerSupplyOrderDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, dtosList.size());
//
//        return pageList;
//
//    }

//    public Page<PackagingDepotDto> packagingDrugsFromDepot(Pageable page, Long customerSupplyOrderId) {
//
//        if(customerSupplyOrderId == null){
//            return null;
//        }
//        Optional<CustomerSupplyOrder> optionalCustomerSupplyOrder=null;
//        optionalCustomerSupplyOrder = customerSupplyOrderRepository.findById(customerSupplyOrderId);
//
//        Page<Depot> list = depotRepository.packagingDrugsFromDepot(customerSupplyOrderId,page);
//        Page<PackagingDepotDto> pageList = list.map(PackagingDepotDto::new);
//        return pageList;
//    }


//    public Boolean controlDepotForPackaging(List<PrePackage> list){
//
//        for (PrePackage p :list) {
//            Optional<Depot> opt = depotRepository.findSingleDepotForPack(
//                    p.getCustomerOrder().getCustomerOrderId(),
//                    p.getDrugLotNo(),
//                    p.getDrugBarcode().toString(),
//                    p.getDrugSerialNo().toString());
//            if(opt.isPresent()){
//               Depot depot = opt.get();
//               Optional<DepotStatus> optStatus = depotStatusRepository.findById(2L);
//               if(!optStatus.isPresent())
//                   continue;
//               depot.setDepotStatus(optStatus.get());
//               depotRepository.save(depot);
//            }
//        }
//        return true;
//    }


//    public List<PrePackageDto> getAllWithPrePackageId(PrePackageBoxDto dto) throws NotFoundException {
//        Optional<PrePackage> opt = prePackageRepository.findById(dto.getPrePackageId());
//        if(!opt.isPresent()){
//            throw new NotFoundException("Sipariş Kaydı bulunamadı");
//        }
//        PrePackage prePackage = opt.get();
//
//        return null;
//
//    }

}




