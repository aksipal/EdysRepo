package com.via.ecza.service;

import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.AccountActivityType;
import com.via.ecza.repo.*;
import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
public class CustomerInvoiceService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private ControlService controlService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomerReceiptRepository customerReceiptRepository;
    @Autowired
    private CheckingCardRepository checkingCardRepository;
    @Autowired
    private FinalReceiptStatusRepository finalReceiptStatusRepository;
//    @Autowired
//    private FinalReceiptRepository finalReceiptRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private InvoiceStatusRepository invoiceStatusRepository;
    @Autowired
    private CustomerOrderDrugsRepository customerOrderDrugsRepository;
    @Autowired
    private InvoiceTypeRepository invoiceTypeRepository;
    @Autowired
    private AccountActivityRepository accountActivityRepository;
    @Autowired
    private CustomerSupplyOrderRepository customerSupplyOrderRepository;
    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CustomerReceiptContentRepository customerReceiptContentRepository;
    @Autowired
    private OtherCompanyRepository otherCompanyRepository;

    public List<CustomerOrderDrugsListDto> getAllCods(CheckingCardInvoiceDto dto, String authHeader) throws Exception {

        if(dto.getCustomerOrderId() == null || dto.getCheckingCardId() == null)
            throw new Exception("Müşteri ve Sipariş Seçiniz");

        Optional<CheckingCard> optionalCheckingCard = null;
        CheckingCard checkingCard = null;
        if(dto.getCheckingCardId() != null ){
            optionalCheckingCard = checkingCardRepository.findById(dto.getCheckingCardId());
            checkingCard = optionalCheckingCard.get();
        }

        StringBuilder createSqlQuery = new StringBuilder("select cod.* from customer_order_drugs cod " +
                "join purchase_order_drugs pod on pod.purchase_order_drugs_id  = cod.purchase_order_drugs_id " +
                "join customer_supply_order cso on cso.purchase_order_drugs_id = pod.purchase_order_drugs_id " +
                "join customer_order co on co.customer_order_id = pod.customer_order_id " +
                "where co.order_status_id =50 and cod.customer_receipt_id is null ");
        if(checkingCard != null )
            if(checkingCard.getCustomerId() != null || checkingCard.getCompanyId() !=null ){
                createSqlQuery.append("and (co.customer_id ="+checkingCard.getCustomerId()+ " ");
                createSqlQuery.append(" or co.companyid = " +checkingCard.getCompanyId()+") ");
            }
        if (dto.getCustomerOrderId() != null){
            createSqlQuery.append("and co.customer_order_id = "+dto.getCustomerOrderId()+ " ");
        }

        createSqlQuery.append(" and cso.customer_supply_status_id = 50  and cod.customer_receipt_id is null ");
        createSqlQuery.append(" group by cod.customer_order_drug_id");


        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerOrderDrugs.class).getResultList();
        CustomerOrderDrugsListDto[] dtos = mapper.map(list, CustomerOrderDrugsListDto[].class);
        List<CustomerOrderDrugsListDto> dtosList = Arrays.asList(dtos);

        return dtosList;

    }

    public String createCustomerInvoice(String authHeader, CheckingCardInvoiceDto dto) throws Exception {

        User user = this.controlService.getUserFromToken(authHeader);

        if(dto.getCustomerOrderId() == null || dto.getCheckingCardId() == null)
            throw new Exception("Müşteri ve Sipariş Seçiniz");

        Optional<CheckingCard> optionalCheckingCard = null;
        CheckingCard checkingCard = null;
        if(dto.getCheckingCardId() != null ){
            optionalCheckingCard = checkingCardRepository.findById(dto.getCheckingCardId());
            checkingCard = optionalCheckingCard.get();
        }

        StringBuilder createSqlQuery = new StringBuilder("select cod.* from customer_order_drugs cod " +
                "join purchase_order_drugs pod on pod.purchase_order_drugs_id  = cod.purchase_order_drugs_id " +
                "join customer_supply_order cso on cso.purchase_order_drugs_id = pod.purchase_order_drugs_id " +
                "join customer_order co on co.customer_order_id = pod.customer_order_id " +
                "where co.order_status_id =50");
        if(checkingCard != null )
            if(checkingCard.getCustomerId() != null || checkingCard.getCompanyId() !=null ){
                createSqlQuery.append("and (co.customer_id ="+checkingCard.getCustomerId()+ " ");
                createSqlQuery.append(" or co.companyid = " +checkingCard.getCompanyId()+") ");
            }
        if (dto.getCustomerOrderId() != null )
            createSqlQuery.append("and co.customer_order_id =  "+dto.getCustomerOrderId() + " ");

        createSqlQuery.append(" and cso.customer_supply_status_id = 50  and cod.customer_receipt_id is null ");
        createSqlQuery.append(" group by cod.customer_order_drug_id");


        List<CustomerOrderDrugs> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerOrderDrugs.class).getResultList();

if(dto.getOtherCompanyId()==null){
    throw new Exception("Firma Bilgisi Bulunamadı !");
}
        CheckingCard checkingCardOpt = otherCompanyRepository.findById(dto.getOtherCompanyId()).get().getCheckingCard();
        if(checkingCardOpt==null){
            throw new NotFoundException("Cari Kart Bulunamadı");
        }

        Invoice invoice = new Invoice();
        invoice.setUser(user);
        invoice.setCreatedAt(new Date());
        invoice.setStatus(1);
        invoice.setInvoiceType(invoiceTypeRepository.findById(1l).get());
        invoice.setCheckingCard(checkingCardOpt);
        invoice.setOtherCheckingCard(optionalCheckingCard.get());
        invoice.setInvoicePurpose(InvoicePurpose.SELL_INVOICE);
        invoice.setInvoiceStatus(invoiceStatusRepository.findById(10L).get());
        invoice.setTotalChargePrice(0.0);
        invoice.setOtherCompanyId(dto.getOtherCompanyId());

        List<Category> list120=categoryRepository.searchTo120CheckingCardId(invoice.getOtherCheckingCard().getCheckingCardId());
        List<Category> list320=categoryRepository.searchTo320CheckingCardId(invoice.getCheckingCard().getCheckingCardId());

        if(list120.size()!=1 || list320.size()!=1){
            throw new Exception("120 - 320 Muhasebe Kodları Bulunamadı !");
        }

        invoice.setCategory(list320.get(0));
        invoice.setOtherCategory(list120.get(0));

        invoice = invoiceRepository.save(invoice);
        //invoice.setInvoiceNo(this.generateInvoiceNo(invoice.getInvoiceId()));
        invoice.setInvoiceNo(dto.getInvoiceNo());
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice = invoiceRepository.save(invoice);
        //invoice.setFinalReceipt(finalReceipt);


        //FinalReceipt finalReceipt = new FinalReceipt();
        //finalReceipt.setCreatedAt(new Date());
        //finalReceipt.setStatus(1);
        //finalReceipt.setFinalReceiptStatus(finalReceiptStatusRepository.findById(30l).get());
        //finalReceipt = finalReceiptRepository.save(finalReceipt);
        //finalReceipt.setFinalReceiptNo(this.generateFinalReceiptNo(finalReceipt));
        //finalReceipt.setFinalReceiptType(ReceiptType.YURTDISI_SATIS);
        //finalReceipt.setInvoice(invoice);
        //finalReceipt = finalReceiptRepository.save(finalReceipt);

        CustomerReceipt customerReceipt = new CustomerReceipt();

        customerReceipt = customerReceiptRepository.save(customerReceipt);

        if(!(list.size() >0)){
            customerReceipt.setInvoice(null);
            customerReceipt = customerReceiptRepository.save(customerReceipt);
            customerReceiptRepository.delete(customerReceipt);

//            finalReceipt.setInvoice(null);
//            finalReceipt = finalReceiptRepository.save(finalReceipt);
//            finalReceiptRepository.delete(finalReceipt);

            invoiceRepository.delete(invoice);
        }

        for (CustomerOrderDrugs customerOrderDrugs :list) {

            customerOrderDrugs.setCustomerReceipt(customerReceipt);
            customerOrderDrugs = customerOrderDrugsRepository.save(customerOrderDrugs);
        }
        customerReceipt.setStatus(1);
        customerReceipt.setCreatedAt(new Date());
        //customerReceipt.setInvoiceId(invoice.getInvoiceId());
        customerReceipt.setInvoice(invoice);
        customerReceipt.setReceiptType(ReceiptType.SATIS);
        customerReceipt.setCustomer(list.get(0).getCustomerOrder().getCustomer());
        //customerReceipt.setFinalReceipt(finalReceipt);
        customerReceipt = customerReceiptRepository.save(customerReceipt);
        Double topCurrency = 0.0, totalPrice = 0.0, totalFreightTl= 0.0, totalFreightCurrency= 0.0;
        CustomerOrder order = null;
        invoice.setInstantCurrencyFee(dto.getInstantCurrencyFee());
        //invoice.setCrsNo(dto.getCrsNo());
        invoice.setFreightCostCurrency(dto.getFreightCostCurrency());
        Double freightTl = ((double) ((int) ((dto.getFreightCostCurrency()/dto.getInstantCurrencyFee()) * 1000.0))) / 1000.0;
        invoice.setFreightCostTl(freightTl);
        invoice.setPaymentTerm(dto.getPaymentTerm());
        invoice.setTotalPriceExpression(dto.getTotalPriceExpression());
        invoice.setTotalPriceCurrencyExpression(dto.getTotalPriceCurrencyExpression());

        List<CustomerReceiptContent> customerReceiptContents = customerReceiptContentRepository.findCustomerReceiptContentsForCustomerReceiptId(dto.getCustomerOrderId());

        for (CustomerReceiptContent customerReceiptContent :customerReceiptContents) {
            customerReceiptContent.setCustomerReceipt(customerReceipt);
            customerReceiptContentRepository.save(customerReceiptContent);
        }

        int totalDrug = 0;
        for (CustomerOrderDrugs drug : list)
            totalDrug += drug.getTotalQuantity();


        for (CustomerOrderDrugs drugs : list){
            order = drugs.getCustomerOrder();
            Double totalPriceCurrency = 0.0;
            if(invoice.getInstantCurrencyFee() != null)
                if(invoice.getInstantCurrencyFee() > 1)
                    totalPriceCurrency = drugs.getTotalQuantity()*drugs.getUnitPrice()/invoice.getInstantCurrencyFee();
                else
                    totalPriceCurrency = drugs.getTotalQuantity()*drugs.getUnitPrice()/drugs.getCustomerOrder().getCurrencyFee();
            else
                totalPriceCurrency = drugs.getTotalQuantity()*drugs.getUnitPrice()/drugs.getCustomerOrder().getCurrencyFee();

            topCurrency += totalPriceCurrency;
            totalPrice += drugs.getTotalQuantity()*drugs.getUnitPrice();

            if(drugs.getFreightCostTl() != null){
                totalFreightTl += drugs.getFreightCostTl();
                totalFreightCurrency += drugs.getFreightCostTl()/drugs.getCustomerOrder().getCurrencyFee();
            }
            drugs.setInstantCurrencyFee(dto.getInstantCurrencyFee());

            //drugs.setFreightCostCurrency(drugs.getTotalQuantity()*dto.getFreightCostCurrency()/totalDrug);
            drugs = customerOrderDrugsRepository.save(drugs);
        }

//        Long customerReceiptDrugCardId = customerReceiptRepository.findCustomerReceiptForContent(invoice.getInvoiceId());
//        CustomerReceiptContent customerReceiptContent = customerReceiptContentRepository.findCustomerReceiptContentForSetReceiptId(customerReceiptDrugCardId);
//        customerReceiptContent.setCustomerReceipt(customerReceipt);

        Double finalTotal = ((double) ((int) (topCurrency * 1000.0))) / 1000.0;
        invoice.setTotalPriceCurrency(finalTotal);

        finalTotal = ((double) ((int) (totalPrice * 1000.0))) / 1000.0;
        invoice.setTotalPrice(finalTotal);

        //finalTotal = ((double) ((int) (totalFreightTl * 1000.0))) / 1000.0;
        finalTotal = invoice.getInstantCurrencyFee()*invoice.getFreightCostCurrency();
        invoice.setFreightCostTl(finalTotal);

//        finalTotal = ((double) ((int) (totalFreightCurrency * 1000.0))) / 1000.0;
//        invoice.setFreightCostCurrency(finalTotal);

//        if(order.getFreightCostTl() != null)
//            invoice.setFreightCostTl(order.getFreightCostTl());
//        if(order.getFreightCostTl() != null && order.getCurrencyFee() !=null)
//            invoice.setFreightCostCurrency(order.getFreightCostTl()/order.getCurrencyFee());

        invoice.setCurrencyType(order.getCurrencyType());
        invoice.setCurrencyFee(order.getCurrencyFee());
        invoice = invoiceRepository.save(invoice);

        //-------ACTIVITY-------//

        //ekip eczanın müşteriye fatura etme hareketi
        AccountActivity accountActivity = new AccountActivity();
        accountActivity.setCurrencyFee(invoice.getCurrencyFee());
        accountActivity.setCurrencyType(invoice.getCurrencyType());
        accountActivity.setCheckingCard(invoice.getCheckingCard());
        accountActivity.setOtherCheckingCard(invoice.getOtherCheckingCard());
        accountActivity.setAccountActivityType(AccountActivityType.CUSTOMER_INVOICE);
        accountActivity.setInvoice(invoice);
        accountActivity.setInvoiceNo(invoice.getInvoiceNo());
        accountActivity.setCreatedAt(new Date());
        Double price=0.0;

        if(invoice.getFreightCostTl()!=null){
            price+=invoice.getFreightCostTl();
        }
        price+=invoice.getTotalPrice();

        accountActivity.setCharge(price);
        accountActivity.setStatus(1);
        accountActivityRepository.save(accountActivity);

        //müşteriye ekip eczadan fatura edilme hareketi
        AccountActivity accountActivity2 = new AccountActivity();
        accountActivity2.setCurrencyFee(invoice.getCurrencyFee());
        accountActivity2.setCurrencyType(invoice.getCurrencyType());
        accountActivity2.setCheckingCard(invoice.getOtherCheckingCard());
        accountActivity2.setOtherCheckingCard(invoice.getCheckingCard());
        accountActivity2.setAccountActivityType(AccountActivityType.PURCHASE_INVOICE);
        accountActivity2.setInvoice(invoice);
        accountActivity2.setInvoiceNo(invoice.getInvoiceNo());
        accountActivity2.setCreatedAt(new Date());
        accountActivity2.setDebt(price);
        accountActivity2.setStatus(1);
        accountActivityRepository.save(accountActivity2);


        return invoice.getInvoiceNo();
    }

//    private String generateFinalReceiptNo(FinalReceipt finalReceipt) {
//        int year = Calendar.getInstance().get(Calendar.YEAR);
//        String code = "MUH-FIS-" + year;
//        int size = finalReceipt.getFinalReceiptId().toString().length();
//        for (int i = 0; i < 7 - size; i++)
//            code += "0";
//        code += finalReceipt.getFinalReceiptId();
//        return code;
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


    public Page<CustomerInvoiceDto> searchCustomerInvoice(CustomerInvoiceSearchDto dto, Pageable page) {

        StringBuilder createSqlQuery = new StringBuilder("select * from invoice i ");
        createSqlQuery.append("join final_receipt fr on fr.invoice_id = i.invoice_id ");
        createSqlQuery.append("join customer_receipt cr on cr.final_receipt_id = fr.final_receipt_id ");
        createSqlQuery.append("where i.status =1");

        if (dto.getOtherCheckingCardId() != null)
            createSqlQuery.append("and i.other_checking_card_id = " + dto.getOtherCheckingCardId() + " ");
        if (dto.getInvoiceNo() != null)
            createSqlQuery.append("and i.invoice_no ILIKE '%" + dto.getInvoiceNo() + "%' ");

        createSqlQuery.append("order by i.invoice_id ASC");

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Invoice.class).getResultList();
        CustomerInvoiceDto[] dtos = mapper.map(list, CustomerInvoiceDto[].class);
        List<CustomerInvoiceDto> dtosList = Arrays.asList(dtos);

        int start = Math.min((int) page.getOffset(), dtosList.size());
        int end = Math.min((start + page.getPageSize()), dtosList.size());

        Page<CustomerInvoiceDto> pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());

        return pageList;
    }

    public List<InvoiceCustomerOrderDrugsDto> getDrugs(Long invoiceId) {

        List<CustomerOrderDrugs> customerOrderDrugsList = customerOrderDrugsRepository.getDrugsByInvoiceId(invoiceId);
        InvoiceCustomerOrderDrugsDto[] dtos = mapper.map(customerOrderDrugsList, InvoiceCustomerOrderDrugsDto[].class);
        List<InvoiceCustomerOrderDrugsDto> list = Arrays.asList(dtos);

        return list;
    }

    public String updateCustomerInvoice(CustomerInvoiceUpdateDto dto)  throws Exception  {

        Optional<Invoice> optionalInvoice = invoiceRepository.findById(dto.getInvoiceId());
        if(!optionalInvoice.isPresent())
            throw new Exception("Fatura Bulunamadı");

        Invoice invoice = optionalInvoice.get();

        Double topCurrency = 0.0, totalPrice = 0.0, totalFreightTl= 0.0, totalFreightCurrency= 0.0;
        invoice.setInstantCurrencyFee(dto.getInstantCurrencyFee());
        invoice.setCrsNo(dto.getCrsNo());
        invoice.setFreightCostCurrency(dto.getFreightCostCurrency());
        invoice.setPaymentTerm(dto.getPaymentTerm());
        invoice.setTotalPriceExpression(dto.getTotalPriceExpression());
        invoice.setTotalPriceCurrencyExpression(dto.getTotalPriceCurrencyExpression());

        List<CustomerOrderDrugs> drugList = null;
        if(invoice.getCustomerReceipt() != null){
            if(invoice.getCustomerReceipt().getCustomerOrderDrugs() != null)
                drugList = invoice.getCustomerReceipt().getCustomerOrderDrugs();
            else{
                throw new Exception("İlaç Listesi Bulunamadı");
            }
        }else{
            throw new Exception("Müşteri Fişi Bulunamadı");
        }

        for (CustomerOrderDrugs drugs : invoice.getCustomerReceipt().getCustomerOrderDrugs()){
            Double totalPriceCurrency = 0.0;
            if(invoice.getInstantCurrencyFee() != null)
                if(invoice.getInstantCurrencyFee() > 1)
                    totalPriceCurrency = drugs.getTotalQuantity()*drugs.getUnitPrice()/invoice.getInstantCurrencyFee();
                else
                    totalPriceCurrency = drugs.getTotalQuantity()*drugs.getUnitPrice()/drugs.getCustomerOrder().getCurrencyFee();
            else
                totalPriceCurrency = drugs.getTotalQuantity()*drugs.getUnitPrice()/drugs.getCustomerOrder().getCurrencyFee();

            topCurrency += totalPriceCurrency;
            totalPrice += drugs.getTotalQuantity()*drugs.getUnitPrice();

            if(drugs.getFreightCostTl() != null){
                totalFreightTl += drugs.getFreightCostTl();
                totalFreightCurrency += drugs.getFreightCostTl()/drugs.getCustomerOrder().getCurrencyFee();
            }
            drugs.setInstantCurrencyFee(dto.getInstantCurrencyFee());
            drugs = customerOrderDrugsRepository.save(drugs);
        }

        Double finalTotal = ((double) ((int) (topCurrency * 1000.0))) / 1000.0;
        invoice.setTotalPriceCurrency(finalTotal);

        finalTotal = ((double) ((int) (totalPrice * 1000.0))) / 1000.0;
        invoice.setTotalPrice(finalTotal);

        //finalTotal = ((double) ((int) (totalFreightTl * 1000.0))) / 1000.0;
        finalTotal = invoice.getInstantCurrencyFee()*invoice.getFreightCostCurrency();
        invoice.setFreightCostTl(finalTotal);

        invoice = invoiceRepository.save(invoice);

        return invoice.getInvoiceNo()+" Güncellendi";
    }

    public String saveCustomerSellingInvoiceReceiptCategories(ReceiptSaveCategoryDto dto) throws Exception {

        int counter = 0;

        if (dto.getSendReceiptCategoriesList().size() > 0) {
            //muhasebe kodu girilmiş ilaç listesi
            List<ReceiptSaveCategoryContentDto> sendReceiptCategoriesList = dto.getSendReceiptCategoriesList();

            //listeden receipt id alınıyor
            Long customerReceiptId = null;

            for (ReceiptSaveCategoryContentDto list : sendReceiptCategoriesList) {
                if (list != null) {
                    counter++;
                    customerReceiptId = list.getReceiptId();
                }
            }
            //siparişteki ilaç listesi
            List<CustomerOrderDrugs> drugList = customerOrderDrugsRepository.getByCustomerReceiptId(customerReceiptId);
            if (drugList.size() != counter) {
                return "Muhasebe Kodu Belirlenmemiş İlaç Bulunmaktadır.";
            }

            if(drugList.size() > 0){
                customerReceiptContentRepository.deleteCustomerReceiptContent(drugList.get(0).getCustomerOrder().getCustomerOrderId());
            }

            for (ReceiptSaveCategoryContentDto item : sendReceiptCategoriesList) {

                if (item.getDrugCardId() != null && item.getCategoryId() != null && item.getReceiptId() != null) {
//                    Optional<CustomerReceipt> optionalCustomerReceipt = customerReceiptRepository.findById(item.getReceiptId());
//                    if (!optionalCustomerReceipt.isPresent()) {
//                        throw new Exception("Muhasebe Fişi Bulunamadı!");
//                    }
                    Optional<DrugCard> optionalDrugCard = drugCardRepository.findById(item.getDrugCardId());
                    if (!optionalDrugCard.isPresent()) {
                        throw new Exception("İlaç Bilgisi Bulunamadı!");
                    }
                    Optional<Category> optionalCategory = categoryRepository.findById(item.getCategoryId());
                    if (!optionalCategory.isPresent()) {
                        throw new Exception("Kategori Bilgisi Bulunamadı!");
                    }

                    CustomerReceiptContent customerReceiptContent = new CustomerReceiptContent();
                    //customerReceiptContent.setCustomerReceipt(optionalCustomerReceipt.get());
                    customerReceiptContent.setCustomerReceipt(null);
                    if(drugList.size() > 0){
                        customerReceiptContent.setCustomerOrder(drugList.get(0).getCustomerOrder());
                    }
                    customerReceiptContent.setDrugCard(optionalDrugCard.get());
                    customerReceiptContent.setCategory(optionalCategory.get());
                    customerReceiptContent.setStatus(1);
                    customerReceiptContent.setCreatedAt(new Date());
                    customerReceiptContent = customerReceiptContentRepository.save(customerReceiptContent);
                }
            }
            //aşağıdaki return front tarafında kontrol ediliyor
            return "İşlem Başarılı";
        } else {
            return "Lütfen Tüm İlaçlara Muhasebe Kodlarını Giriniz.";
        }
    }

//    public Boolean deleteSellInvoiceCustomerReceiptCategories() throws Exception {
//
//        customerReceiptContentRepository.deleteCustomerReceiptContent();
//
//        return true;
//    }
}
