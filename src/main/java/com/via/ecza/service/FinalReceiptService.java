package com.via.ecza.service;

import com.via.ecza.entity.FinalReceiptStatus;
import com.via.ecza.repo.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Calendar;
import java.util.List;

@Service
@Transactional
public class FinalReceiptService {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private ReceiptRepository receiptRepository;
    @Autowired
    private FinalReceiptStatusRepository finalReceiptStatusRepository;
    @Autowired
    private ControlService controlService;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private CheckingCardRepository checkingCardRepository;
    @Autowired
    private InvoiceStatusRepository invoiceStatusRepository;
    @Autowired
    private InvoiceTypeRepository invoiceTypeRepository;
    @Autowired
    private RefundRepository refundRepository;
    @Autowired
    private RefundPriceRepository refundPriceRepository;
    @Autowired
    private AccountActivityRepository accountActivityRepository;

    @Transactional
//    public String createNewBuyFinalReceipt(FinalReceiptCreateDto dto) throws Exception{
//        Boolean control = true;
//        Optional<Supplier> optionalSupplier = supplierRepository.findById(dto.getSupplierId());
//        if (!optionalSupplier.isPresent()) {
//            throw new NotFoundException("Tedarikçi Bulunamadı");
//        }
//        Optional<FinalReceiptStatus> optionalFinalReceiptStatus = finalReceiptStatusRepository.findById(1l);
//        if(!optionalFinalReceiptStatus.isPresent()) {
//            throw new NotFoundException("Statü Bulunamadı");
//        }
//        if (dto.getCheckList() == null) {
//            return null;
//        }
//        if (dto.getCheckList().size() < 1) {
//            return null;
//        }
////        FinalReceipt finalReceipt = null;
////        finalReceipt = createFinalReceipt(optionalFinalReceiptStatus.get(),optionalSupplier.get(), ReceiptType.ALIS );
//
////        Optional<FinalReceipt> optionalFinalReceipt = finalReceiptRepository.findBySupplierId(ReceiptType.ALIS.toString(), dto.getSupplierId());
////
////        if (!optionalFinalReceipt.isPresent()) {
////            finalReceipt = createFinalReceipt(optionalFinalReceiptStatus.get(),optionalSupplier.get(), ReceiptType.ALIS );
////        }else{
////            if(optionalFinalReceipt.get().getFinalReceiptStatus().getFinalReceiptStatusId()<10)
////                finalReceipt = optionalFinalReceipt.get();
////            else
////                finalReceipt = createFinalReceipt(optionalFinalReceiptStatus.get(),optionalSupplier.get(), ReceiptType.ALIS );
////        }
//
//        for (FinalReceiptCheckListDto checkListDto : dto.getCheckList()) {
//            if(checkListDto == null)
//                continue;
//            if (checkListDto.getValue()) {
//                Optional<Receipt> optionalReceipt = receiptRepository.findById(checkListDto.getReceiptId());
//                if (optionalReceipt.isPresent()) {
//                    Receipt receipt = optionalReceipt.get();
//                    if(receipt.getSupplier().getSupplierId() != optionalSupplier.get().getSupplierId() ){
//                        control = false;
//                        break;
//                    }else{
//                        receipt.setFinalReceipt(finalReceipt);
//                        receipt = receiptRepository.save(receipt);
//
//                    }
//                }
//            }
//        }
//
//        if(control==false){
//            List<Receipt> receipts = receiptRepository.findByFinalReceipt(finalReceipt);
//            for (Receipt recpt:receipts) {
//                recpt.setFinalReceipt(null);
//                recpt = receiptRepository.save(recpt);
//            }
//            finalReceiptRepository.delete(finalReceipt);
//            throw new Exception("Farklı Tedarikçiler İçin Tek Bir Muhasebe Fişi Oluşturulamaz !");
//        }
//        return finalReceipt.getFinalReceiptNo();
//    }

//    private FinalReceipt createFinalReceipt(FinalReceiptStatus finalReceiptStatus, Supplier supplier, ReceiptType receiptType) {
//        FinalReceipt finalReceipt = new FinalReceipt();
//        finalReceipt.setCreatedAt(new Date());
//        finalReceipt.setStatus(1);
//        finalReceipt.setSupplier(supplier);
//        finalReceipt.setFinalReceiptStatus(finalReceiptStatus);
//        finalReceipt = finalReceiptRepository.save(finalReceipt);
//        finalReceipt.setFinalReceiptNo(this.generateFinalReceiptNo(finalReceipt));
//        finalReceipt.setFinalReceiptType(receiptType);
//        finalReceipt = finalReceiptRepository.save(finalReceipt);
//        return finalReceipt;
//    }

//    private String generateFinalReceiptNo(FinalReceipt finalReceipt) {
//        int year = Calendar.getInstance().get(Calendar.YEAR);
//        String code = "MUH-FIS-" + year;
//        int size = finalReceipt.getFinalReceiptId().toString().length();
//        for (int i = 0; i < 7 - size; i++)
//            code += "0";
//        code += finalReceipt.getFinalReceiptId();
//        return code;
//    }

//    public String addToExistingFinalReceipt(FinalReceiptCreateDto dto) throws Exception{
//        Boolean control = true;
//        Optional<Supplier> optionalSupplier = supplierRepository.findById(dto.getSupplierId());
//        if (!optionalSupplier.isPresent()) {
//            throw new NotFoundException("Tedarikçi Bulunamadı");
//        }
//        Optional<FinalReceiptStatus> optionalFinalReceiptStatus = finalReceiptStatusRepository.findById(1l);
//        if(!optionalFinalReceiptStatus.isPresent()) {
//            throw new NotFoundException("Statü Bulunamadı");
//        }
//        if (dto.getCheckList() == null) {
//            return null;
//        }
//        if (dto.getCheckList().size() < 1) {
//            return null;
//        }
//        Optional<FinalReceipt> optionalFinalReceipt = finalReceiptRepository.findById(dto.getFinalReceiptId());
//        if(!optionalFinalReceipt.isPresent()) {
//            throw new NotFoundException("Muhasebe Fişi Bulunamadı");
//        }
//        FinalReceipt finalReceipt = optionalFinalReceipt.get();
//        if(finalReceipt.getSupplier().getSupplierId() != optionalSupplier.get().getSupplierId())
//            throw new NotFoundException("Seçilen Fişteki Tedarikçi Muhasebe Fişinin Tedarikçisi İle Uyuşmuyor ! Lütfen Doğru Fiş Seçiniz");
//
//        for (FinalReceiptCheckListDto checkListDto : dto.getCheckList()) {
//            if(checkListDto == null)
//                continue;
//            if (checkListDto.getValue()) {
//                Optional<Receipt> optionalReceipt = receiptRepository.findById(checkListDto.getReceiptId());
//                if (optionalReceipt.isPresent()) {
//                    Receipt receipt = optionalReceipt.get();
//                    if(receipt.getSupplier().getSupplierId() != optionalSupplier.get().getSupplierId() ){
//                        control = false;
//                        break;
//                    }else{
//                        receipt.setFinalReceipt(finalReceipt);
//                        receipt = receiptRepository.save(receipt);
//
//                    }
//                }
//            }
//        }
//
//        if(control==false){
//
//            for (FinalReceiptCheckListDto checkListDto : dto.getCheckList()) {
//                if(checkListDto == null)
//                    continue;
//                if (checkListDto.getValue()) {
//                    Optional<Receipt> optionalReceipt = receiptRepository.findById(checkListDto.getReceiptId());
//                    if (optionalReceipt.isPresent()) {
//                        Receipt receipt = optionalReceipt.get();
//                        receipt.setFinalReceipt(null);
//                        receipt = receiptRepository.save(receipt);
//                    }
//                }
//            }
//            throw new Exception("Farklı Tedarikçiler İçin Tek Bir Muhasebe Fişi Oluşturulamaz !");
//        }
//        return finalReceipt.getFinalReceiptNo();
//    }

//    public Page<FinalReceiptDto> searchForWarehouseman(String authHeader, FinalReceiptSearchDto dto, Pageable page) throws Exception {
//
//        User user = controlService.getUserFromToken(authHeader);
//        if(!(user.getRole() == Role.WAREHOUSEMAN ||  user.getRole() == Role.ADMIN))
//            return null;
//
//        StringBuilder createSqlQuery = new StringBuilder("select f.* from final_receipt f where status=1 ");
//        createSqlQuery.append(" and f.final_receipt_status_id=1 ");
//
//        if (dto.getSupplierId() != null)
//            createSqlQuery.append(" and supplier_id = " + dto.getSupplierId());
//        if (dto.getInvoiceNo() != null){
//            if (dto.getInvoiceNo().trim().length() > 0)
//                createSqlQuery.append(" and invoice_no ILIKE '%" + dto.getInvoiceNo().trim() + "%' ");
//        }
//        if (dto.getFinalReceiptNo() != null){
//            if (dto.getFinalReceiptNo().trim().length() > 0)
//                createSqlQuery.append(" and final_receipt_no ILIKE '%" + dto.getFinalReceiptNo().trim() + "%' ");
//        }
//        if (dto.getCreatedAt() != null)
//            createSqlQuery.append(" and created_at = " + dto.getCreatedAt());
//
//        createSqlQuery.append(" order by final_receipt_id ");
//
//        List<FinalReceipt> list = entityManager.createNativeQuery(createSqlQuery.toString(), FinalReceipt.class).getResultList();
//
//        FinalReceiptDto[] dtos = mapper.map(list, FinalReceiptDto[].class);
//        List<FinalReceiptDto> dtosList = Arrays.asList(dtos);
//
//        int start = Math.min((int) page.getOffset(), dtosList.size());
//        int end = Math.min((start + page.getPageSize()), dtosList.size());
//
//        Page<FinalReceiptDto> pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());
//
//        pageList.forEach(data -> data.setReceiptSize(data.getReceipts().size()));
//
//        return pageList;
//    }

//    public Page<FinalReceiptAccountingDto> searchForAccounting(String authHeader, FinalReceiptSearchDto dto, Pageable page) throws Exception {
//        User user = controlService.getUserFromToken(authHeader);
//        if((user.getRole() == Role.WAREHOUSEMAN))
//            return null;
//        StringBuilder createSqlQuery = new StringBuilder("select f.* from final_receipt f where status=1 ");
//
//        if(dto.getFinalReceiptStatusId() != null )
//            createSqlQuery.append(" and f.final_receipt_status_id="+dto.getFinalReceiptStatusId()+" ");
//        else
//            createSqlQuery.append(" and f.final_receipt_status_id>1 and f.final_receipt_status_id<40 ");
//
//        if (dto.getSupplierId() != null)
//            createSqlQuery.append(" and f.supplier_id = " + dto.getSupplierId());
//        if (dto.getInvoiceNo() != null){
//            if (dto.getInvoiceNo().trim().length() > 0)
//                createSqlQuery.append(" and f.invoice_no ILIKE '%" + dto.getInvoiceNo().trim() + "%' ");
//        }
//        if (dto.getFinalReceiptNo() != null){
//            if (dto.getFinalReceiptNo().trim().length() > 0)
//                createSqlQuery.append(" and f.final_receipt_no ILIKE '%" + dto.getFinalReceiptNo().trim() + "%' ");
//        }
//        if (dto.getFinalReceiptStatus() != null)
//            createSqlQuery.append("and f.final_receipt_status_id = " + dto.getFinalReceiptStatus() + " ");
//        if (dto.getCreatedAt() != null)
//            createSqlQuery.append(" and f.created_at = " + dto.getCreatedAt());
//
//        createSqlQuery.append(" order by f.final_receipt_id ");
//
//        List<FinalReceipt> list = entityManager.createNativeQuery(createSqlQuery.toString(), FinalReceipt.class).getResultList();
//
//        FinalReceiptAccountingDto[] dtos = mapper.map(list, FinalReceiptAccountingDto[].class);
//        List<FinalReceiptAccountingDto> dtosList = Arrays.asList(dtos);
//
//        int start = Math.min((int) page.getOffset(), dtosList.size());
//        int end = Math.min((start + page.getPageSize()), dtosList.size());
//
//        Page<FinalReceiptAccountingDto> pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());
//
//        pageList.forEach(data -> data.setReceiptSize(data.getReceipts().size() >0 ? data.getReceipts().size() : 1));
//
//        return pageList;
//    }

//    public List<FinalReceiptReceiptDto> getBuyDetailForWarehouseman(Long finalReceiptId) throws Exception {
//        StringBuilder createSqlQuery = new StringBuilder("select r.* from receipt r " +
//                "join final_receipt fr on fr.final_receipt_id = r.final_receipt_id " +
//                "where fr.final_receipt_id =" +finalReceiptId);
//
//        List<Receipt> list = entityManager.createNativeQuery(createSqlQuery.toString(), Receipt.class).getResultList();
//        FinalReceiptReceiptDto[] dtos = mapper.map(list, FinalReceiptReceiptDto[].class);
//        List<FinalReceiptReceiptDto> listDtos = Arrays.asList(dtos);
//
//        int index=0;
//        for (Receipt receipt : list) {
//            Long totality = 0l;
//            Double totalPrice = 0.0;
//            for (CustomerSupplyOrder cso : receipt.getCustomerSupplyOrders()) {
//                totality = totality + cso.getTotality();
//                totalPrice = totalPrice + cso.getTotalPrice();
//            }
//            listDtos.get(index).setTotality(totality);
//            listDtos.get(index).setTotalPrice(totalPrice);
//            index++;
//        }
//
//        return listDtos;
//    }

//    public List<FinalReceiptReceiptDto> getSellDetailForWarehouseman(Long finalReceiptId) throws Exception{
//        StringBuilder createSqlQuery = new StringBuilder("select r.* from receipt r " +
//                "join final_receipt fr on fr.final_receipt_id = r.final_receipt_id " +
//                "where fr.final_receipt_id =" +finalReceiptId);
//
//        List<Receipt> list = entityManager.createNativeQuery(createSqlQuery.toString(), Receipt.class).getResultList();
//        FinalReceiptReceiptDto[] dtos = mapper.map(list, FinalReceiptReceiptDto[].class);
//        List<FinalReceiptReceiptDto> listDtos = Arrays.asList(dtos);
//
//        int index=0;
//        for (Receipt receipt : list) {
//            Long totality = 0l;
//            Double totalPrice = 0.0;
//            for(Refund refund : receipt.getRefunds()){
//                totality = totality + refund.getTotality();
//                totalPrice = totalPrice + refund.getTotalPrice();
//            }
//            listDtos.get(index).setTotality(totality);
//            listDtos.get(index).setTotalPrice(totalPrice);
//            index++;
//        }
//
//        return listDtos;
//    }

//    public List<CustomerSupplyOrderDrugListDto> getBuyDetailForAccounting(Long finalReceiptId) {
//        StringBuilder createSqlQuery = new StringBuilder("select cso.* from customer_supply_order cso " +
//                "join receipt r on r.receipt_id = cso.receipt_id " +
//                "join final_receipt fr on fr.final_receipt_id = r.final_receipt_id " +
//                "where r.final_receipt_id =" +finalReceiptId);
//
//        List<CustomerSupplyOrder> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerSupplyOrder.class).getResultList();
//        CustomerSupplyOrderDrugListDto[] dtos = mapper.map(list, CustomerSupplyOrderDrugListDto[].class);
//        List<CustomerSupplyOrderDrugListDto> listDtos = Arrays.asList(dtos);
//
//        int index=0;
//        for (Receipt receipt : list) {
//            Long totality = 0l;
//            Double totalPrice = 0.0;
//            for (CustomerSupplyOrder cso : receipt.getCustomerSupplyOrders()) {
//                totality = totality + cso.getTotality();
//                totalPrice = totalPrice + cso.getTotalPrice();
//            }
//            listDtos.get(index).setTotality(totality);
//            listDtos.get(index).setTotalPrice(totalPrice);
//            index++;
//        }
//
//        return listDtos;
//    }

//    public Boolean removeFromFinalReceipt(Long finalReceiptId, List<FinalReceiptRemoveReceiptsDto> dtos) {
//
//        Optional<FinalReceipt> optionalFinalReceipt = finalReceiptRepository.findById(finalReceiptId);
//        if(!optionalFinalReceipt.isPresent()){
//            return false;
//        }
//        if(!(dtos.size()>0)){
//            return false;
//        }
//        FinalReceipt finalReceipt = optionalFinalReceipt.get();
//
//        for (FinalReceiptRemoveReceiptsDto dto:dtos) {
//            if(dto == null)
//                continue;
//            if (dto.getValue()){
//                Optional<Receipt> optionalReceipt = receiptRepository.findById(dto.getReceiptId());
//                if(optionalReceipt.isPresent()){
//                    Receipt receipt = optionalReceipt.get();
//                    if(finalReceipt.getFinalReceiptId() != receipt.getFinalReceipt().getFinalReceiptId())
//                        continue;
//                    receipt.setFinalReceipt(null);
//                    receipt = receiptRepository.save(receipt);
//                }
//            }
//        }
////        FinalReceiptDto dto =  mapper.map(finalReceipt, FinalReceiptDto.class);
//        return true;
//    }

//    public Boolean sendToApproveFromWarehouseman(Long finalReceiptId) throws Exception {
//
//        Optional<FinalReceiptStatus> finalReceiptStatus = finalReceiptStatusRepository.findById(10l);
//        if(!finalReceiptStatus.isPresent()){
//            throw new NotFoundException("Statü Bulunamadı");
//        }
//        if(finalReceiptId == null)
//            return false;
//        Optional<FinalReceipt> finalReceiptOptional = finalReceiptRepository.findById(finalReceiptId);
//        if(!finalReceiptOptional.isPresent()){
//            throw new NotFoundException("Muhasebe Fişi Bulunamadı");
//        }
//
//        FinalReceipt finalReceipt = finalReceiptOptional.get();
//        finalReceipt.setFinalReceiptStatus(finalReceiptStatus.get());
//        finalReceipt = finalReceiptRepository.save(finalReceipt);
//
//        return true;
//    }

//    public Boolean sendToWarehousemanForCorrection(Long finalReceiptId) throws Exception {
//
//        Optional<FinalReceiptStatus> finalReceiptStatus = finalReceiptStatusRepository.findById(1l);
//        if(!finalReceiptStatus.isPresent()){
//            throw new NotFoundException("Statü Bulunamadı");
//        }
//        if(finalReceiptId == null)
//            return false;
//        Optional<FinalReceipt> finalReceiptOptional = finalReceiptRepository.findById(finalReceiptId);
//        if(!finalReceiptOptional.isPresent()){
//            throw new NotFoundException("Muhasebe Fişi Bulunamadı");
//        }
//
//        FinalReceipt finalReceipt = finalReceiptOptional.get();
//        finalReceipt.setFinalReceiptStatus(finalReceiptStatus.get());
//        finalReceipt = finalReceiptRepository.save(finalReceipt);
//
//        return true;
//    }

//    public Boolean sendToAdminForApprove(Long finalReceiptId) throws Exception {
//
//        Optional<FinalReceiptStatus> finalReceiptStatus = finalReceiptStatusRepository.findById(20l);
//        if(!finalReceiptStatus.isPresent()){
//            throw new NotFoundException("Statü Bulunamadı");
//        }
//        if(finalReceiptId == null)
//            return false;
//        Optional<FinalReceipt> finalReceiptOptional = finalReceiptRepository.findById(finalReceiptId);
//        if(!finalReceiptOptional.isPresent()){
//            throw new NotFoundException("Muhasebe Fişi Bulunamadı");
//        }
//
//        FinalReceipt finalReceipt = finalReceiptOptional.get();
//        finalReceipt.setFinalReceiptStatus(finalReceiptStatus.get());
//        finalReceipt = finalReceiptRepository.save(finalReceipt);
//
//        return true;
//    }

//    public String createBuyInvoice(String authHeader, Long finalReceiptId ) throws Exception{
//
//        User user = this.controlService.getUserFromToken(authHeader);
//
//        Optional<FinalReceiptStatus> finalReceiptStatus = finalReceiptStatusRepository.findById(30l);
//        if(!finalReceiptStatus.isPresent()){
//            throw new NotFoundException("Statü Bulunamadı");
//        }
//
//        if(finalReceiptId == null)
//            return null;
//        Optional<FinalReceipt> finalReceiptOptional = finalReceiptRepository.findById(finalReceiptId);
//        if(!finalReceiptOptional.isPresent()){
//            throw new NotFoundException("Muhasebe Fişi Bulunamadı");
//        }
//
////        Optional<InvoiceStatus> optionalInvoiceStatus = invoiceStatusRepository.findById(1l);
////        if(!optionalInvoiceStatus.isPresent()){
////            throw new NotFoundException("Fatura Statüsü Bulunamadı");
////        }
//
//        Optional<CheckingCard> checkingCardOpt = checkingCardRepository.findById(1l);
//        if(!checkingCardOpt.isPresent()){
//            throw new NotFoundException("Cari Kart Bulunamadı");
//        }
//
//        FinalReceipt finalReceipt = finalReceiptOptional.get();
//        finalReceipt.setFinalReceiptStatus(finalReceiptStatus.get());
//        finalReceipt = finalReceiptRepository.save(finalReceipt);
//
//        if(finalReceipt.getSupplier() == null)
//            throw new NotFoundException("Tedarikçi Bulunamadı");
//
//        Optional<CheckingCard> checkingCardOptional = checkingCardRepository.findBySupplierId(finalReceipt.getSupplier().getSupplierId());
//        if(!checkingCardOptional.isPresent()){
//            throw new NotFoundException("Tedarikçi Bulunamadı..");
//        }
//
//        Optional<InvoiceType> invoiceTypeOpt = invoiceTypeRepository.findById(1l);
//        if(!invoiceTypeOpt.isPresent()){
//            throw new NotFoundException("Fatura Tipi Bulunamadı");
//        }
//        if(finalReceipt.getInvoice() != null ){
//            throw new Exception("Daha Önce Fatura Oluşturulmuştur");
//        }
//        /* Fatura Oluşturma Başladı */
//        Invoice invoice = new Invoice();
//        invoice.setUser(user);
//        invoice.setCreatedAt(new Date());
//        invoice.setStatus(1);
//        invoice.setInvoiceStatus(invoiceStatusRepository.findById(10L).get());
//        invoice.setCurrencyType(CurrencyType.TL);
//        invoice.setCurrencyFee(1.0);
//        invoice.setTotalChargePrice(0.0);
//        invoice = invoiceRepository.save(invoice);
//        invoice.setInvoiceNo(this.generateInvoiceNo(invoice.getInvoiceId()));
//        invoice.setFinalReceipt(finalReceipt);
//        if(checkingCardOptional.get().getTaxIdentificationNumber()!=null && checkingCardOptional.get().getTaxIdentificationNumber().toString().trim().length()>0) {
//            invoice.setTaxNo(checkingCardOptional.get().getTaxIdentificationNumber().toString());
//        }
//        if(checkingCardOptional.get().getTaxOffice()!=null && checkingCardOptional.get().getTaxOffice().trim().length()>0) {
//            invoice.setTaxOffice(checkingCardOptional.get().getTaxOffice());
//        }
//        invoice.setCrsNo(checkingCardOpt.get().getCrsNo());
//        //invoice.setInvoiceStatus(optionalInvoiceStatus.get());
//        invoice.setInvoiceType(invoiceTypeOpt.get());
//        invoice.setCheckingCard(checkingCardOptional.get());
//        invoice.setOtherCheckingCard(checkingCardOpt.get());
//        Double accountTotalPrice = 0.0,totalPrice = 0.0;
//        if(finalReceipt.getFinalReceiptType() == ReceiptType.ALIS){
//            invoice.setInvoicePurpose(InvoicePurpose.BUY_INVOICE);
//            invoice.setCheckingCard(checkingCardOptional.get());
//            invoice.setOtherCheckingCard(checkingCardOpt.get());
//            for (Receipt receipt : finalReceipt.getReceipts()) {
//                for(CustomerSupplyOrder cso : receipt.getCustomerSupplyOrders()){
//                    SupplyOrderPrice supplyOrderPrice = new SupplyOrderPrice();
//                    supplyOrderPrice.setSupplyOrder(cso);
//                    if(cso.getDrugCard().getStatus() == 1){
//                        //titck daki ilaç listesi için %8 kdv
//                        supplyOrderPrice.setVat(8D);
//                    }else{
//                        // fason ilaç için %0 kdv
//                        supplyOrderPrice.setVat(0D);
//                    }
//                    supplyOrderPrice.setVatSum(cso.getTotalPrice()*supplyOrderPrice.getVat()/100);
//                    supplyOrderPrice.setAccountTotalPrice(cso.getTotalPrice()+supplyOrderPrice.getVatSum());
//                    supplyOrderPrice = supplyOrderPriceRepository.save(supplyOrderPrice);
//
//                    // kdv siz toplam
//                    //totalPrice +=  cso.getTotalPrice();
//                    //kdv li toplam
//                    accountTotalPrice += supplyOrderPrice.getAccountTotalPrice();
//                }
//            }
//            Double accountTop = ( (double) ( (int) (accountTotalPrice * 1000.0) ) ) / 1000.0 ;
//            invoice.setTotalPrice(accountTop);
//            invoice.setTotalPriceCurrency(accountTop/invoice.getCurrencyFee());
//        }
////
//        invoice = invoiceRepository.save(invoice);
//        /* Fatura Oluşturma Bitti */
//
//        if(invoice.getInvoicePurpose() == InvoicePurpose.BUY_INVOICE){
//
//            //eczanenin livaya fatura etme hareketi
//            AccountActivity accountActivity = new AccountActivity();
//            accountActivity.setCurrencyFee(invoice.getCurrencyFee());
//            accountActivity.setCurrencyType(invoice.getCurrencyType());
//            accountActivity.setCheckingCard(invoice.getCheckingCard());
//            accountActivity.setOtherCheckingCard(checkingCardRepository.findById(2l).get());
//            accountActivity.setAccountActivityType(AccountActivityType.CUSTOMER_INVOICE);
//            accountActivity.setInvoice(invoice);
//            accountActivity.setInvoiceNo(invoice.getInvoiceNo());
//            accountActivity.setCreatedAt(new Date());
//            accountActivity.setCharge(invoice.getTotalPriceCurrency());
//            accountActivity.setStatus(1);
//            accountActivityRepository.save(accountActivity);
//
//            //livaya eczaneden fatura edilme hareketi
//            AccountActivity accountActivity2 = new AccountActivity();
//            accountActivity2.setCurrencyFee(invoice.getCurrencyFee());
//            accountActivity2.setCurrencyType(invoice.getCurrencyType());
//            accountActivity2.setCheckingCard(checkingCardRepository.findById(2l).get());
//            accountActivity2.setOtherCheckingCard(invoice.getCheckingCard());
//            accountActivity2.setAccountActivityType(AccountActivityType.PURCHASE_INVOICE);
//            accountActivity2.setInvoice(invoice);
//            accountActivity2.setInvoiceNo(invoice.getInvoiceNo());
//            accountActivity2.setCreatedAt(new Date());
//            accountActivity2.setDebt(invoice.getTotalPriceCurrency());
//            accountActivity2.setStatus(1);
//            accountActivityRepository.save(accountActivity2);
//
//            //livadan ekibe fatura etme hareketi
//            AccountActivity accountActivity3 = new AccountActivity();
//            accountActivity3.setCurrencyFee(invoice.getCurrencyFee());
//            accountActivity3.setCurrencyType(invoice.getCurrencyType());
//            accountActivity3.setCheckingCard(checkingCardRepository.findById(2l).get());
//            accountActivity3.setOtherCheckingCard(checkingCardRepository.findById(1l).get());
//            accountActivity3.setAccountActivityType(AccountActivityType.CUSTOMER_INVOICE);
//            accountActivity3.setInvoice(invoice);
//            accountActivity3.setInvoiceNo(invoice.getInvoiceNo());
//            accountActivity3.setCreatedAt(new Date());
//            accountActivity3.setCharge(invoice.getTotalPriceCurrency());
//            accountActivity3.setStatus(1);
//            accountActivityRepository.save(accountActivity3);
//
//            //ekibe livadan fatura edilme hareketi
//            AccountActivity accountActivity4 = new AccountActivity();
//            accountActivity4.setCurrencyFee(invoice.getCurrencyFee());
//            accountActivity4.setCurrencyType(invoice.getCurrencyType());
//            accountActivity4.setCheckingCard(checkingCardRepository.findById(1l).get());
//            accountActivity4.setOtherCheckingCard(checkingCardRepository.findById(2l).get());
//            accountActivity4.setAccountActivityType(AccountActivityType.PURCHASE_INVOICE);
//            accountActivity4.setInvoice(invoice);
//            accountActivity4.setInvoiceNo(invoice.getInvoiceNo());
//            accountActivity4.setCreatedAt(new Date());
//            accountActivity4.setDebt(invoice.getTotalPriceCurrency());
//            accountActivity4.setStatus(1);
//            accountActivityRepository.save(accountActivity4);
//        }
//
//        finalReceipt.setInvoice(invoice);
//        finalReceipt = finalReceiptRepository.save(finalReceipt);
//
//        return invoice.getInvoiceNo();
//    }
//    public String createSellInvoice(String authHeader, Long finalReceiptId, SellInvoiceDto dto) throws Exception{
//
//        User user = this.controlService.getUserFromToken(authHeader);
//
//        Optional<FinalReceiptStatus> finalReceiptStatus = finalReceiptStatusRepository.findById(30l);
//        if(!finalReceiptStatus.isPresent()){
//            throw new NotFoundException("Statü Bulunamadı");
//        }
//
//        if(finalReceiptId == null)
//            return null;
//        Optional<FinalReceipt> finalReceiptOptional = finalReceiptRepository.findById(finalReceiptId);
//        if(!finalReceiptOptional.isPresent()){
//            throw new NotFoundException("Muhasebe Fişi Bulunamadı");
//        }
//
////        Optional<InvoiceStatus> optionalInvoiceStatus = invoiceStatusRepository.findById(1l);
////        if(!optionalInvoiceStatus.isPresent()){
////            throw new NotFoundException("Fatura Statüsü Bulunamadı");
////        }
//
//        FinalReceipt finalReceipt = finalReceiptOptional.get();
//        finalReceipt.setFinalReceiptStatus(finalReceiptStatus.get());
//        finalReceipt = finalReceiptRepository.save(finalReceipt);
//
//        if(finalReceipt.getSupplier() == null)
//            throw new NotFoundException("Tedarikçi Bulunamadı");
//
//        Optional<CheckingCard> checkingCardOptional = checkingCardRepository.findById(1l);
//        if(!checkingCardOptional.isPresent()){
//            throw new NotFoundException("Tedarikçi Bulunamadı..");
//        }
//
//        Optional<CheckingCard> checkingCardOpt = checkingCardRepository.findBySupplierId(finalReceipt.getSupplier().getSupplierId());
//        if(!checkingCardOpt.isPresent()){
//            throw new NotFoundException("Cari Kart Bulunamadı");
//        }
//
//        Optional<InvoiceType> invoiceTypeOpt = invoiceTypeRepository.findById(1l);
//        if(!invoiceTypeOpt.isPresent()){
//            throw new NotFoundException("Fatura Tipi Bulunamadı");
//        }
//        if(finalReceipt.getInvoice() != null ){
//            throw new Exception("Daha Önce Fatura Oluşturulmuştur");
//        }
//        /* Fatura Oluşturma Başladı */
//        Invoice invoice = new Invoice();
//        invoice.setUser(user);
//        invoice.setCreatedAt(new Date());
//        invoice.setStatus(1);
//        invoice.setInvoiceStatus(invoiceStatusRepository.findById(10L).get());
//        invoice.setCurrencyType(CurrencyType.TL);
//        invoice.setCurrencyFee(1.0);
//        invoice.setTotalChargePrice(0.0);
//        invoice = invoiceRepository.save(invoice);
//        invoice.setInvoiceNo(this.generateInvoiceNo(invoice.getInvoiceId()));
//        invoice.setFinalReceipt(finalReceipt);
//        if(checkingCardOptional.get().getTaxIdentificationNumber()!=null && checkingCardOptional.get().getTaxIdentificationNumber().toString().trim().length()>0) {
//            invoice.setTaxNo(checkingCardOptional.get().getTaxIdentificationNumber().toString());
//        }
//        if(checkingCardOptional.get().getTaxOffice()!=null && checkingCardOptional.get().getTaxOffice().trim().length()>0) {
//            invoice.setTaxOffice(checkingCardOptional.get().getTaxOffice());
//        }
//        invoice.setCrsNo(checkingCardOpt.get().getCrsNo());
//        //invoice.setInvoiceStatus(optionalInvoiceStatus.get());
//        invoice.setInvoiceType(invoiceTypeOpt.get());
//        invoice.setCheckingCard(checkingCardOptional.get());
//        invoice.setOtherCheckingCard(checkingCardOpt.get());
//        invoice.setTotalPriceExpression(dto.getTotalPriceExpression());
//        invoice.setPaymentTerm(dto.getPaymentTerm());
//        invoice.setCrsNo(dto.getCrsNo());
//        Double accountTotalPrice = 0.0,totalPrice = 0.0;
//        if(finalReceipt.getFinalReceiptType() == ReceiptType.SATIS){
//            invoice.setInvoicePurpose(InvoicePurpose.REFUND_SELL_INVOICE);
//            for (Receipt receipt : finalReceipt.getReceipts()) {
//                for(Refund refund : receipt.getRefunds()){
//                    RefundPrice refundPrice = new RefundPrice();
//                    refundPrice.setRefund(refund);
//                    if(refund.getDrugCard().getStatus() == 1){
//                        //titck daki ilaç listesi için %8 kdv
//                        refundPrice.setVat(8D);
//                    }else{
//                        // fason ilaç için %0 kdv
//                        refundPrice.setVat(0D);
//                    }
//                    refundPrice.setVatSum(refund.getTotalPrice()*refundPrice.getVat()/100);
//                    refundPrice.setAccountTotalPrice(refund.getTotalPrice()+refundPrice.getVatSum());
//                    refundPrice = refundPriceRepository.save(refundPrice);
//
//                    // kdv siz toplam
//                    //totalPrice +=  refund.getTotalPrice();
//                    //kdv li toplam
//                    accountTotalPrice += refundPrice.getAccountTotalPrice();
//                }
//            }
//            Double accountTop = ( (double) ( (int) (accountTotalPrice * 1000.0) ) ) / 1000.0 ;
//            invoice.setTotalPrice(accountTop);
//            invoice.setTotalPriceCurrency(accountTop/invoice.getCurrencyFee());
//
//        }
//
//        invoice = invoiceRepository.save(invoice);
//        /* Fatura Oluşturma Bitti */
//
//        if(invoice.getInvoicePurpose() == InvoicePurpose.REFUND_SELL_INVOICE){
//
//            //ekip eczanın livaya fatura etme hareketi
//            AccountActivity accountActivity = new AccountActivity();
//            accountActivity.setCurrencyFee(invoice.getCurrencyFee());
//            accountActivity.setCurrencyType(invoice.getCurrencyType());
//            accountActivity.setCheckingCard(checkingCardRepository.findById(1l).get());
//            accountActivity.setOtherCheckingCard(checkingCardRepository.findById(2l).get());
//            accountActivity.setAccountActivityType(AccountActivityType.CUSTOMER_INVOICE);
//            accountActivity.setInvoice(invoice);
//            accountActivity.setInvoiceNo(invoice.getInvoiceNo());
//            accountActivity.setCreatedAt(new Date());
//            accountActivity.setCharge(invoice.getTotalPriceCurrency());
//            accountActivity.setStatus(1);
//            accountActivityRepository.save(accountActivity);
//
//            //livaya ekip eczadan fatura edilme hareketi
//            AccountActivity accountActivity2 = new AccountActivity();
//            accountActivity2.setCurrencyFee(invoice.getCurrencyFee());
//            accountActivity2.setCurrencyType(invoice.getCurrencyType());
//            accountActivity2.setCheckingCard(checkingCardRepository.findById(2l).get());
//            accountActivity2.setOtherCheckingCard(checkingCardRepository.findById(1l).get());
//            accountActivity2.setAccountActivityType(AccountActivityType.PURCHASE_INVOICE);
//            accountActivity2.setInvoice(invoice);
//            accountActivity2.setInvoiceNo(invoice.getInvoiceNo());
//            accountActivity2.setCreatedAt(new Date());
//            accountActivity2.setDebt(invoice.getTotalPriceCurrency());
//            accountActivity2.setStatus(1);
//            accountActivityRepository.save(accountActivity2);
//
//            //livadan eczaneye fatura etme hareketi
//            AccountActivity accountActivity3 = new AccountActivity();
//            accountActivity3.setCurrencyFee(invoice.getCurrencyFee());
//            accountActivity3.setCurrencyType(invoice.getCurrencyType());
//            accountActivity3.setCheckingCard(checkingCardRepository.findById(2l).get());
//            accountActivity3.setOtherCheckingCard(invoice.getOtherCheckingCard());
//            accountActivity3.setAccountActivityType(AccountActivityType.CUSTOMER_INVOICE);
//            accountActivity3.setInvoice(invoice);
//            accountActivity3.setInvoiceNo(invoice.getInvoiceNo());
//            accountActivity3.setCreatedAt(new Date());
//            accountActivity3.setCharge(invoice.getTotalPriceCurrency());
//            accountActivity3.setStatus(1);
//            accountActivityRepository.save(accountActivity3);
//
//            //eczaneye livadan fatura edilme hareketi
//            AccountActivity accountActivity4 = new AccountActivity();
//            accountActivity4.setCurrencyFee(invoice.getCurrencyFee());
//            accountActivity4.setCurrencyType(invoice.getCurrencyType());
//            accountActivity4.setCheckingCard(invoice.getOtherCheckingCard());
//            accountActivity4.setOtherCheckingCard(checkingCardRepository.findById(2l).get());
//            accountActivity4.setAccountActivityType(AccountActivityType.PURCHASE_INVOICE);
//            accountActivity4.setInvoice(invoice);
//            accountActivity4.setInvoiceNo(invoice.getInvoiceNo());
//            accountActivity4.setCreatedAt(new Date());
//            accountActivity4.setDebt(invoice.getTotalPriceCurrency());
//            accountActivity4.setStatus(1);
//            accountActivityRepository.save(accountActivity4);
//        }
//
//        finalReceipt.setInvoice(invoice);
//        finalReceipt = finalReceiptRepository.save(finalReceipt);
//
//        return invoice.getInvoiceNo();
//    }

    private String generateInvoiceNo(Long invoiceId) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String code = "FATURA-" + year;
        int size = invoiceId.toString().length();
        for (int i = 0; i < 7 - size; i++)
            code += "0";
        code += invoiceId;
        return code;
    }

    public List<FinalReceiptStatus> getFinalReceiptStatus() throws Exception{

        return finalReceiptStatusRepository.getFinalReceiptStatus();
    }

//    public String createSellFinalReceipt(Long receiptId) throws Exception{
//        Boolean control = true;
//
//        Optional<FinalReceiptStatus> optionalFinalReceiptStatus = finalReceiptStatusRepository.findById(1l);
//        if(!optionalFinalReceiptStatus.isPresent()) {
//            throw new NotFoundException("Statü Bulunamadı");
//        }
//
//        Optional<Receipt> optionalReceipt = receiptRepository.findById(receiptId);
//        if (!optionalReceipt.isPresent()) {
//            throw new NotFoundException("Fiş Bulunamadı");
//        }
//
//        FinalReceipt finalReceipt = createFinalReceipt(optionalFinalReceiptStatus.get(), optionalReceipt.get().getSupplier(), ReceiptType.SATIS);
//
//        Receipt receipt = optionalReceipt.get();
//        receipt.setFinalReceipt(finalReceipt);
//        receipt = receiptRepository.save(receipt);
//
//        return finalReceipt.getFinalReceiptNo();
//    }

//    public SellFinalReceiptDetailDto getSellDetailForAccounting(Long finalReceiptId) throws Exception{
//
//        SellFinalReceiptDetailDto dto = new SellFinalReceiptDetailDto();
//
//        Optional<FinalReceipt> optionalFinalReceipt = finalReceiptRepository.findById(finalReceiptId);
//        if(!optionalFinalReceipt.isPresent())
//            throw new NotFoundException("Muhasebe Fişi Bulunamadı");
//
//        if(optionalFinalReceipt.get().getSupplier() != null){
//            dto.setSupplierId(optionalFinalReceipt.get().getSupplier().getSupplierId());
//            List<Refund> refunds = refundRepository.getRefundsByFinalReceiptId(finalReceiptId);
//            ReceiptRefundListDto[] dtos = mapper.map(refunds, ReceiptRefundListDto[].class);
//            List<ReceiptRefundListDto> list = Arrays.asList(dtos);
//            dto.setRefundList(list);
//        }else{
//            List<CustomerOrderDrugs> customerOrderDrugsList =  optionalFinalReceipt.get().getCustomerReceipt().getCustomerOrderDrugs();
//            CustomerOrderDrugsListDto[] dtos = mapper.map(customerOrderDrugsList, CustomerOrderDrugsListDto[].class);
//            List<CustomerOrderDrugsListDto> list = Arrays.asList(dtos);
//            dto.setCustomerOrderDrugsList(list);
//        }
//        return dto;
//    }

//    public List<FinalReceiptDto> getFinalReceiptAdminApprove() throws Exception{
//
//        List<FinalReceipt> adminApprove = finalReceiptRepository.getAllAdminApprove();
//        FinalReceiptDto[] dtos = mapper.map(adminApprove, FinalReceiptDto[].class);
//        List<FinalReceiptDto> list = Arrays.asList(dtos);
//
//        return list;
//    }

//    public Boolean sendToApproved(Long finalReceiptId) throws Exception {
//
//        Optional<FinalReceiptStatus> finalReceiptStatus = finalReceiptStatusRepository.findById(25l);
//        if(!finalReceiptStatus.isPresent()){
//            throw new NotFoundException("Statü Bulunamadı");
//        }
//        if(finalReceiptId == null)
//            return false;
//        Optional<FinalReceipt> finalReceiptOptional = finalReceiptRepository.findById(finalReceiptId);
//        if(!finalReceiptOptional.isPresent()){
//            throw new NotFoundException("Muhasebe Fişi Bulunamadı");
//        }
//
//        FinalReceipt finalReceipt = finalReceiptOptional.get();
//        finalReceipt.setFinalReceiptStatus(finalReceiptStatus.get());
//        finalReceipt = finalReceiptRepository.save(finalReceipt);
//
//        return true;
//    }

//    public Boolean sendToBack(Long finalReceiptId) throws Exception {
//
//        Optional<FinalReceiptStatus> finalReceiptStatus = finalReceiptStatusRepository.findById(10l);
//        if(!finalReceiptStatus.isPresent()){
//            throw new NotFoundException("Statü Bulunamadı");
//        }
//        if(finalReceiptId == null)
//            return false;
//        Optional<FinalReceipt> finalReceiptOptional = finalReceiptRepository.findById(finalReceiptId);
//        if(!finalReceiptOptional.isPresent()){
//            throw new NotFoundException("Muhasebe Fişi Bulunamadı");
//        }
//
//        FinalReceipt finalReceipt = finalReceiptOptional.get();
//        finalReceipt.setFinalReceiptStatus(finalReceiptStatus.get());
//        finalReceipt = finalReceiptRepository.save(finalReceipt);
//
//        return true;
//    }

//    public List<FinalReceiptComboboxDto> getAllFinalReceiptsBySupplierId(Long supplierId) throws Exception{
//
//        List<FinalReceipt> finalReceiptOptional = finalReceiptRepository.getFinalReceiptsBySupplierId(supplierId);
//        if(finalReceiptOptional == null)
//            return null;
//
//        if(finalReceiptOptional.size()<1){
//            return null;
//        }
//
//        FinalReceiptComboboxDto[] dtos = mapper.map(finalReceiptOptional, FinalReceiptComboboxDto[].class);
//        List<FinalReceiptComboboxDto> listDtos = Arrays.asList(dtos);
//
//        return listDtos;
//    }
}
