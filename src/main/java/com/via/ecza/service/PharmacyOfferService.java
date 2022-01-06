package com.via.ecza.service;

import com.via.ecza.dto.PharmacyIndexDto;
import com.via.ecza.dto.PharmacyRefundOfferDto;
import com.via.ecza.dto.SupplyDrugCardDto;
import com.via.ecza.dto.SupplyDrugOffersDto;
import com.via.ecza.entity.*;
import com.via.ecza.repo.*;
import javassist.NotFoundException;
import org.json.JSONObject;
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
public class PharmacyOfferService {

    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private SupplierOfferRepository supplierOfferRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ControlService controlService;
    @Autowired
    private RefundOfferRepository refundOfferRepository;
    @Autowired
    private DepotRepository depotRepository;
    @Autowired
    private DepotStatusRepository depotStatusRepository;
    @Autowired
    private RefundOfferStatusRepository refundOfferStatusRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private OtherCompanyRepository otherCompanyRepository;

    //isim  değiştir //Eczane için teklifler listesi
    public Page<SupplyDrugOffersDto> getSupplierByUserPage(String authHeader, SupplyDrugCardDto dto,Integer pageNo,Integer pageSize,String sortBy) throws NotFoundException {
        // Optional<User> optionalUser=userRepository.findByUsername(userName);
        //Optional<Supplier> optionalSupplier=supplierRepository.getSupplierByUser(userId);
        User user = this.getUserFromToken(authHeader);
        Optional<Supplier> optionalSupplier = supplierRepository.getSupplierByUser(user.getUserId().longValue());
        StringBuilder createSqlQuery = new StringBuilder("select * from supplier_offer so where supplier_id=" + optionalSupplier.get().getSupplierId() + " and so.supplier_offer_status_id=10 ");



        if (dto.getDrugCardId() != null) {

            createSqlQuery.append("and so.drug_card_id=" + dto.getDrugCardId() + " ");
        }

        createSqlQuery.append("order by so.supplier_offer_id ASC");

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), SupplierOffer.class).getResultList();



        SupplyDrugOffersDto[] supplierOfferDtoList = mapper.map(list, SupplyDrugOffersDto[].class);
        List<SupplyDrugOffersDto> dtoList = Arrays.asList(supplierOfferDtoList);

        for (SupplyDrugOffersDto item:dtoList ) {
            if(item.getOtherCompanyId()!=null) {
                Optional<OtherCompany> optOtherCompany = otherCompanyRepository.findById(item.getOtherCompanyId());
                if (optOtherCompany.isPresent()) {
                    item.setOtherCompanyName(optOtherCompany.get().getOtherCompanyName());
                }
            }
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        int start = Math.min((int) paging.getOffset(), dtoList.size());
        int end = Math.min((start + paging.getPageSize()), dtoList.size());

        Page<SupplyDrugOffersDto> pageList = new PageImpl<>(dtoList.subList(start, end), paging, dtoList.size());

        return pageList;


    }
    //--

    //isim  değiştir //Eczane için teklifler listesi
//    public List<SupplyDrugOffersDto> getSupplierByUser(String authHeader) throws NotFoundException {
//        // Optional<User> optionalUser=userRepository.findByUsername(userName);
//        //Optional<Supplier> optionalSupplier=supplierRepository.getSupplierByUser(userId);
//        User user = this.getUserFromToken(authHeader);
//        Optional<Supplier> optionalSupplier = supplierRepository.getSupplierByUser(user.getUserId().longValue());
//
//        List<SupplierOffer> supplierOfferList = supplierOfferRepository.getBySupplier(optionalSupplier.get().getSupplierId());
//        //System.out.println(" supplier Id: " + optionalSupplier.get().getSupplierId());
//        SupplyDrugOffersDto[] supplierOfferDtoList = mapper.map(supplierOfferList, SupplyDrugOffersDto[].class);
//        List<SupplyDrugOffersDto> dtoList = Arrays.asList(supplierOfferDtoList);
//        return dtoList;
//    }


        //---

    //Eczane için iadeler teklifi listesi
    public List<PharmacyRefundOfferDto> getRefundOffersBySupplier(String authHeader) throws NotFoundException {
        User user = this.getUserFromToken(authHeader);
        Optional<Supplier> optionalSupplier = supplierRepository.getSupplierByUser(user.getUserId());
        List<RefundOffer> refundList = refundOfferRepository.getRefundOffersBySupplier(optionalSupplier.get().getSupplierId());
        PharmacyRefundOfferDto[] pharmacyRefundOfferDtos = mapper.map(refundList, PharmacyRefundOfferDto[].class);

        for (PharmacyRefundOfferDto dto :pharmacyRefundOfferDtos) {
            if(dto.getOtherCompanyId()!=null) {
                Optional<OtherCompany> optOtherCompany = otherCompanyRepository.findById(dto.getOtherCompanyId());
                if (optOtherCompany.isPresent()) {
                    dto.setOtherCompanyName(optOtherCompany.get().getOtherCompanyName());
                }
            }
        }



        return Arrays.asList(pharmacyRefundOfferDtos);
    }

    public Boolean cancelRefundOfferByPurchase(Long refundOfferId) throws NotFoundException {
        Optional<RefundOffer> optionalRefundOffer = refundOfferRepository.findById(refundOfferId);
        if (!optionalRefundOffer.isPresent()) {
            throw new NotFoundException("Böyle bir sipariş yok");
        }
        if (optionalRefundOffer.get().getRefundOfferStatus().getRefundOfferStatusId() != 10L) {
            throw new NotFoundException("Bu işlem daha önce değiştirilmiş");
        }
        List<Depot> depotList = depotRepository.cancelRefundOffer(refundOfferId);
        if (depotList.size() < 1) {
            throw new NotFoundException("Bu ilaç yanlış bir talimatla başka işlem için kullanılmış");
        }

        for (Depot drugs : depotList) {
            drugs.setRefundOffer(null);
            drugs.setDepotStatus(depotStatusRepository.findById(10L).get());
            depotRepository.save(drugs);
        }
        optionalRefundOffer.get().setRefundOfferStatus(refundOfferStatusRepository.findById(20L).get());
        refundOfferRepository.save(optionalRefundOffer.get());
        return true;
    }


    public PharmacyIndexDto getPharmacyCounts(String authHeader) throws NotFoundException {
        User user = this.getUserFromToken(authHeader);
        Optional<Supplier> optionalSupplier = supplierRepository.getSupplierByUser(user.getUserId());

        StringBuilder createSqlQueryForCSO = new StringBuilder("SELECT count(*)  FROM customer_supply_order cso where cso.supplier_id=" + optionalSupplier.get().getSupplierId() + " and cso.customer_supply_status_id=10 ");
        List<Object> cso = entityManager.createNativeQuery(createSqlQueryForCSO.toString()).getResultList();
        //       System.out.println(JSONObject.valueToString(cso));
        //   System.out.println(JSONObject.valueToString(cso.get(0)));
        long csoCount = Long.valueOf(JSONObject.valueToString(cso.get(0)));

        StringBuilder createSqlQueryForSO = new StringBuilder("SELECT count(*)  FROM supplier_offer so where so.supplier_id=" + optionalSupplier.get().getSupplierId() + " and so.supplier_offer_status_id=10 ");
        List<Object> so = entityManager.createNativeQuery(createSqlQueryForSO.toString()).getResultList();
        long soCount = Long.valueOf(JSONObject.valueToString(so.get(0)));

        StringBuilder createSqlQueryForRE = new StringBuilder("SELECT count(*)  FROM refund so where so.supplier_id=" + optionalSupplier.get().getSupplierId() + " and so.refund_status_id=10 ");
        List<Object> re = entityManager.createNativeQuery(createSqlQueryForRE.toString()).getResultList();
        long reCount = Long.valueOf(JSONObject.valueToString(re.get(0)));

        StringBuilder createSqlQueryForRO = new StringBuilder("SELECT count(*)  FROM refund_offer ro where ro.supplier_id=" + optionalSupplier.get().getSupplierId() + " and ro.refund_offer_status_id=10 ");
        List<Object> ro = entityManager.createNativeQuery(createSqlQueryForRO.toString()).getResultList();
        long roCount = Long.valueOf(JSONObject.valueToString(ro.get(0)));

        PharmacyIndexDto pharmacyIndexDto = new PharmacyIndexDto();
        pharmacyIndexDto.setSupplyOrderCount(csoCount);
        pharmacyIndexDto.setSupplyOfferCount(soCount);
        pharmacyIndexDto.setRefundCount(reCount);
        pharmacyIndexDto.setRefundOfferCount(roCount);
        return pharmacyIndexDto;
    }

    private User getUserFromToken(String authHeader) throws NotFoundException {

        String username = controlService.getUsernameFromToken(authHeader);
        Optional<User> optUser = userRepository.findByUsername(username);
        if (!optUser.isPresent()) {
            throw new NotFoundException("Not found User");
        }
        return optUser.get();
    }
}
