package com.via.ecza.service;

import com.via.ecza.dto.PurchaseOrderDrugDto;

import com.via.ecza.dto.SupplyExportNoteDto;
import com.via.ecza.entity.CustomerOrderDrugs;
import com.via.ecza.entity.CustomerSupplyOrder;
import com.via.ecza.entity.DrugCard;
import com.via.ecza.entity.PurchaseOrderDrugs;
import com.via.ecza.repo.DrugCardRepository;
import com.via.ecza.repo.PurchaseOrderDrugsRepository;
import com.via.ecza.repo.PurchaseStatusRepository;
import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class PurchaseOrderDrugsService {

    @Autowired
    PurchaseOrderDrugsRepository purchaseOrderDrugsRepository;
    @Autowired
    PurchaseStatusRepository purchaseStatusRepository;

    @Autowired
    ModelMapper mapper;

    @Autowired
    DrugCardRepository drugCardRepository;

    @Autowired
    EntityManager entityManager;

    public PurchaseOrderDrugDto getPurchaseOrderDrug(Long purchaseOrderDrugId) throws NotFoundException {
        PurchaseOrderDrugs purchaseOrderDrug = purchaseOrderDrugsRepository.getPurchaseOrderDrug(purchaseOrderDrugId);
        PurchaseOrderDrugDto purchaseOrderDrugDto = mapper.map(purchaseOrderDrug, PurchaseOrderDrugDto.class);

        return purchaseOrderDrugDto;

    }

    public String getOnTheWayForStock(Long drugCardId) throws NotFoundException{

        Optional<DrugCard> optionalDrugCard=drugCardRepository.findById(drugCardId);
        if (!optionalDrugCard.isPresent()){throw new NotFoundException(" Böyle bir ilaç yok");}
        StringBuilder createSqlQuery = new StringBuilder("select sum(stocks) from customer_supply_order cso where drug_card_id=" + optionalDrugCard.get().getDrugCardId() + " and customer_supply_status_id=10");

        Query sumOfStockForDrug = entityManager.createNativeQuery(createSqlQuery.toString());
        return String.valueOf( (Object) sumOfStockForDrug.getSingleResult());
    }

    public Boolean setExporterNote(Long purchaseOrderDrugId, SupplyExportNoteDto dto) throws NotFoundException {

        Optional<PurchaseOrderDrugs> optionalPurchaseOrderDrugs=purchaseOrderDrugsRepository.findById(purchaseOrderDrugId);
        if(!optionalPurchaseOrderDrugs.isPresent()){throw new NotFoundException(" Böyle bir ilaç siparişi yok");}
       if(optionalPurchaseOrderDrugs.get().getPurchaseStatus().getPurchaseStatusId()==5L){throw new NotFoundException(" Zaten ihracata not gönderilmiş durumda!");}
        optionalPurchaseOrderDrugs.get().getCustomerOrder().setPurchaseOrderNote(optionalPurchaseOrderDrugs.get().getCustomerOrder().getPurchaseOrderNote()+" null");
        String note= optionalPurchaseOrderDrugs.get().getCustomerOrder().getPurchaseOrderNote().replace("null","");
        optionalPurchaseOrderDrugs.get().getCustomerOrder().setPurchaseOrderNote(note+"\n "+
                optionalPurchaseOrderDrugs.get().getTotalQuantity()+" adetlik "+
                optionalPurchaseOrderDrugs.get().getDrugCard().getDrugName()+" adlı ilaç siparişi için not: "+
                dto.getExporterNote());
       optionalPurchaseOrderDrugs.get().setPurchaseStatus(purchaseStatusRepository.findById(5L).get());
        return true;
    }
}
/*String qlString = "SELECT AVG(x.price), SUM(x.stocks) FROM Magazine x WHERE ...";
Query q = em.createQuery(qlString);
Object[] results = (Object[]) q.getSingleResult();

for (Object object : results) {
    System.out.println(object);
}*/