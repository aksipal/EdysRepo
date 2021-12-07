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
import java.util.*;

@Service
@Transactional
public class RefundService {

    @Autowired
    private ModelMapper mapper;
    @Autowired
    ControlService controlService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private CustomerSupplyOrderRepository customerSupplyOrderRepository;
    @Autowired
    private DepotRepository depotRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DepotStatusRepository depotStatusRepository;
    @Autowired
    private RefundOfferStatusRepository refundOfferStatusRepository;
    @Autowired
    private RefundStatusRepository refundStatusRepository;
    @Autowired
    private RefundRepository refundRepository;
    @Autowired
    private RefundOfferRepository refundOfferRepository;
    @Autowired
    private CustomerSupplyStatusRepository customerSupplyStatusRepository;
    @Autowired
    private CheckingCardRepository checkingCardRepository;

    Date createdAt = new Date(System.currentTimeMillis());

    public List<PharmacyRefundDto> search(RefundSearchDto dto) throws NotFoundException {

        StringBuilder createSqlQuery = new StringBuilder("select * from refund where refund_status_id between 9 and 51 ");

        if (dto.getRefundOrderNo() != null) {
            createSqlQuery.append(" and refund_order_no  ILIKE '%" + dto.getRefundOrderNo() + "%' ");
        }

        if (dto.getDrugCard() != null) {
            createSqlQuery.append("and drug_card_id=" + dto.getDrugCard() + " ");
        }

        if (dto.getSupplier() != null) {
            createSqlQuery.append(" and supplier_id=" + dto.getSupplier() + " ");
        }

        if (dto.getRefundStatus() != null) {
            createSqlQuery.append(" and refund_status_id=" + dto.getRefundStatus() + " ");
        }

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Refund.class).getResultList();
        PharmacyRefundDto[] dtos = mapper.map(list, PharmacyRefundDto[].class);

        List<PharmacyRefundDto> liste = Arrays.asList(dtos);


        return liste;
    }


    public List<RefundStatus> getAllRefundStatus() throws NotFoundException {
        List<RefundStatus> list = refundStatusRepository.getAllRefundStatus();
        return list;
    }

    private String getCode(Long refundId) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String code = "IADE-" + year;
        int size = refundId.toString().length();
        for (int i = 0; i < 5 - size; i++)
            code += "0";
        code += refundId;
        return code;
    }


    public Boolean cancelRefundOrder(Long refundId) throws NotFoundException {
        Optional<Refund> optionalRefund = refundRepository.findById(refundId);
        if (!optionalRefund.isPresent()) {
            throw new NotFoundException("Böyle bir sipariş yok");
        }
        if (optionalRefund.get().getRefundStatus().getRefundStatusId() != 10L) {
            throw new NotFoundException("Bu sipariş sizin tarafınızdan iptal olması için uygun değildir");
        }

        List<Depot> depotList = depotRepository.cancelRefundOrder(refundId);
        for (Depot drugs : depotList) {
            drugs.setRefund(null);
            drugs.setDepotStatus(depotStatusRepository.findById(10L).get());
            depotRepository.save(drugs);
        }
        optionalRefund.get().setRefundStatus(refundStatusRepository.findById(30L).get());
        refundRepository.save(optionalRefund.get());


        return true;
    }

    private User getUserFromToken(String authHeader) throws NotFoundException {

        String username = controlService.getUsernameFromToken(authHeader);
        Optional<User> optUser = userRepository.findByUsername(username);
        if (!optUser.isPresent()) {
            throw new NotFoundException("Not found User");
        }
        return optUser.get();
    }

    public RefundAcceptanceDto findByIdRefund(Long refundId) throws NotFoundException {

        Optional<Refund> optRefund = refundRepository.findById(refundId);
        if (!optRefund.isPresent()) {
            throw new NotFoundException("İade Siparişi Bulunamadı !");
        }

        RefundAcceptanceDto refund = mapper.map(optRefund.get(), RefundAcceptanceDto.class);
        return refund;
    }

    public List<RefundAcceptanceDto> getRefundOrdersByCheckingCard(CheckingCardSearchDto dto) throws Exception {
        if(dto.getCheckingCardId()==null){
            return null;
        }
        Optional<CheckingCard> optCheckingCard = checkingCardRepository.findById(dto.getCheckingCardId());
        if (!optCheckingCard.isPresent()) {
            throw new Exception("Yurtiçi Satış Faturasında Cari Kaydı Bulunamadı !");
        }

        StringBuilder createSqlQuery = new StringBuilder("select * from refund where refund_status_id=50 and refund_receipt_id is null and supplier_id=" + optCheckingCard.get().getSupplierId()+" and other_company_id="+dto.getOtherCompanyId()+" order by refund_id ASC");

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Refund.class).getResultList();
        RefundAcceptanceDto[] dtos = mapper.map(list, RefundAcceptanceDto[].class);
        List<RefundAcceptanceDto> dtoList = Arrays.asList(dtos);

        return dtoList;
    }
}
