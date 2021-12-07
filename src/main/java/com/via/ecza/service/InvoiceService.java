package com.via.ecza.service;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
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
import javax.validation.Valid;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

@Service
@Transactional
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private InvoiceTypeRepository invoiceTypeRepository;
    @Autowired
    private InvoiceStatusRepository invoiceStatusRepository;
    //    @Autowired
//    private FinalReceiptRepository finalReceiptRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ControlService controlService;
    @Autowired
    private CheckingCardRepository checkingCardRepository;
    @Autowired
    private AccountActivityRepository accountActivityRepository;
    @Autowired
    private UtilityInvoiceContentRepository utilityInvoiceContentRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private RefundRepository refundRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private RefundReceiptRepository refundReceiptRepository;
    @Autowired
    private RefundReceiptContentRepository refundReceiptContentRepository;
    @Autowired
    private RefundPriceRepository refundPriceRepository;
    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private DepotRepository depotRepository;
    @Autowired
    private ReceiptStatusRepository receiptStatusRepository;
    @Autowired
    private OtherReceiptPriceRepository otherReceiptPriceRepository;
    @Autowired
    private OtherReceiptRepository otherReceiptRepository;
    @Autowired
    private OtherCompanyRepository otherCompanyRepository;
    @Autowired
    private AccountActivityService accountActivityService;
    @Autowired
    private OtherRefundReceiptRepository otherRefundReceiptRepository;
    @Autowired
    private OtherRefundPriceRepository otherRefundPriceRepository;


    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");


    public Page<InvoiceDto> search(InvoiceSearchDto dto, Integer pageNo, Integer pageSize, String sortBy) {

        StringBuilder createSqlQuery = new StringBuilder("(select * from invoice i inner join checking_card cc1 on i.checking_card_id=cc1.checking_card_id inner join checking_card cc2 on i.other_checking_card_id=cc2.checking_card_id where 1=1 and i.other_company_id="+dto.getOtherCompanyId()+" ");
        //Alış Kriterleri
        if (dto.getInvoiceNoInComing() != null && dto.getInvoiceNoInComing().trim().length() > 0)
            createSqlQuery.append("and i.invoice_no ILIKE  '%" + dto.getInvoiceNoInComing().trim() + "%' ");

        if (dto.getInvoicePurposeInComing() != null && dto.getInvoicePurposeInComing().trim().length() > 0) {
            if (dto.getInvoicePurposeInComing().equals("all_in_coming")) {
                createSqlQuery.append("and ( i.invoice_purpose='" + InvoicePurpose.BUY_INVOICE.toString() + "' or i.invoice_purpose= '" + InvoicePurpose.REFUND_BUY_INVOICE.toString() + "' ) ");
            } else {
                createSqlQuery.append(" and i.invoice_purpose= '" + dto.getInvoicePurposeInComing().trim() + "' ");
            }
        }


        if (dto.getInvoiceDateInComing() != null)
            createSqlQuery.append(" and i.created_at>= to_date('" + sdf.format(dto.getInvoiceDateInComing()) + "'," + "'dd.MM.yyyy') ");
        if (dto.getBiller() != null && dto.getBiller().trim().length() > 0)
            createSqlQuery.append("and cc1.checking_card_name ILIKE  '%" + dto.getBiller().trim() + "%' ");


        //Satış Kriterleri
        if (dto.getInvoiceNoOutGoing() != null && dto.getInvoiceNoOutGoing().trim().length() > 0)
            createSqlQuery.append("and i.invoice_no ILIKE  '%" + dto.getInvoiceNoOutGoing().trim() + "%' ");


        if (dto.getInvoicePurposeOutGoing() != null && dto.getInvoicePurposeOutGoing().trim().length() > 0) {
            if (dto.getInvoicePurposeOutGoing().equals("all_out_going")) {
                createSqlQuery.append("and ( i.invoice_purpose= '" + InvoicePurpose.SELL_INVOICE.toString() + "' or i.invoice_purpose= '" + InvoicePurpose.REFUND_SELL_INVOICE.toString() + "' or i.invoice_purpose='" + InvoicePurpose.DOMESTIC_SELL_INVOICE + "' ) ");
            } else {
                createSqlQuery.append("and i.invoice_purpose= '" + dto.getInvoicePurposeOutGoing().trim() + "' ");
            }
        }

        if (dto.getInvoiceDateOutGoing() != null)
            createSqlQuery.append(" and i.created_at>= to_date('" + sdf.format(dto.getInvoiceDateOutGoing()) + "'," + "'dd.MM.yyyy') ");
        if (dto.getBilled() != null && dto.getBilled().trim().length() > 0)
            createSqlQuery.append("and cc2.checking_card_name ILIKE  '%" + dto.getBilled().trim() + "%' ");
        if (dto.getInvoiceStatusId() != null)
            createSqlQuery.append("and i.invoice_status_id=" + dto.getInvoiceStatusId());

        createSqlQuery.append(" order by i.invoice_id)");


        //pharma firmaları arasında aktarılan fatura var ise
        // aktarılan fatura alan kısımda görünüyor fakat satan tarafta görünmüyor çözüm için bu kısım eklendi
        //sadece satış listesi görüntülenirken kullanılacak
        /*
        *satış sayfası olacak
        *fatura kesen kendisi olacak
        *fatura amacı buy_invoice olacak
        *fatura other_company_id kendisine eşit olmayacak
         */

        if (dto.getInvoicePurposeOutGoing() != null && dto.getInvoicePurposeOutGoing().trim().length() > 0) {
            if (dto.getInvoicePurposeOutGoing().equals("all_out_going")) {
                createSqlQuery.append(" union select * from invoice i " +
                        "inner join checking_card cc1 on i.checking_card_id=cc1.checking_card_id " +
                        "inner join checking_card cc2 on i.other_checking_card_id=cc2.checking_card_id " +
                        "where i.invoice_purpose ='BUY_INVOICE' " +
                        "and i.checking_card_id =(select oc.checking_card_id from other_company oc where oc.other_company_id="+dto.getOtherCompanyId()+") " +
                        "and i.other_company_id !="+dto.getOtherCompanyId());
            }
        }


        //yukarıdakinin iade için olanı
        if (dto.getInvoicePurposeInComing() != null && dto.getInvoicePurposeInComing().trim().length() > 0) {
            if (dto.getInvoicePurposeInComing().equals("all_in_coming")) {
                createSqlQuery.append(" union select * from invoice i " +
                        "inner join checking_card cc1 on i.checking_card_id=cc1.checking_card_id " +
                        "inner join checking_card cc2 on i.other_checking_card_id=cc2.checking_card_id " +
                        "where i.invoice_purpose ='DOMESTIC_SELL_INVOICE' " +
                        "and i.other_checking_card_id =(select oc.checking_card_id from other_company oc where oc.other_company_id="+dto.getOtherCompanyId()+") " +
                        "and i.other_company_id !="+dto.getOtherCompanyId());
            }
        }


        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Invoice.class).getResultList();
        InvoiceDto[] dtos = mapper.map(list, InvoiceDto[].class);
        List<InvoiceDto> dtosList = Arrays.asList(dtos);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        int start = Math.min((int) paging.getOffset(), dtosList.size());
        int end = Math.min((start + paging.getPageSize()), dtosList.size());

        Page<InvoiceDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, dtosList.size());

        return pageList;


    }

    public List<InvoiceType> getInvoiceTypes() throws Exception {
        List<InvoiceType> list = invoiceTypeRepository.findAll();
        return list;
    }

    //Hizmet Faturası Oluşturma
    public String createUtilityInvoice(UtilityInvoiceSaveDto dto, String authHeader) throws Exception {

        //Kullanıcı Bilgisi Alındı
        User user = controlService.getUserFromToken(authHeader);

        if(dto.getOtherCompanyId()==null){
            throw new NotFoundException("Firma Id Bilgisi Bulunamadı !");
        }

        CheckingCard otherCompanyCheckingCard=otherCompanyRepository.findById(dto.getOtherCompanyId()).get().getCheckingCard();

        if (dto.getCheckingCardId() == dto.getOtherCheckingCardId()) {
            throw new Exception("Cariler Aynı Olamaz !");
        }

        Invoice invoice = new Invoice();
        String invoiceNo = null;

        //Faturaya toplam tutarı set etmek için tanımlandı
        Double totalPrice = 0.0;

        //Hizmet Faturasının İçeriği Çekiliyor Alış Satış Durumuna Göre
        List<UtilityInvoiceContent> contentList = null;
        if (dto.getInvoicePurpose().equals(InvoicePurpose.BUY_INVOICE.toString())) {
            contentList = utilityInvoiceContentRepository.getInvoiceContentList(user.getUserId(), InvoicePurpose.BUY_INVOICE.toString(),dto.getOtherCompanyId());
            invoice.setCheckingCard(checkingCardRepository.findById(dto.getCheckingCardId()).get());
            invoice.setOtherCheckingCard(otherCompanyCheckingCard);
            invoice.setInvoicePurpose(InvoicePurpose.BUY_INVOICE);
            invoice.setInvoiceType(invoiceTypeRepository.findById(dto.getInvoiceType()).get());
            invoice.setCurrencyFee(1d);
            invoice.setCurrencyType(CurrencyType.TL);
            invoice.setUser(user);
            invoice.setCreatedAt(new Date());
            invoice.setStatus(1);
            invoice.setTotalChargePrice(0.0);
            invoice.setInvoiceStatus(invoiceStatusRepository.findById(10L).get());
            invoice.setUtilityInvoiceContents(contentList);
            invoice.setInvoiceDate(dto.getInvoiceDate());
            invoice.setInvoiceNo(dto.getInvoiceNo());
            invoice.setOtherCompanyId(dto.getOtherCompanyId());
            //invoiceNo = generateInvoiceNo(invoice.getInvoiceId());
            //invoice.setInvoiceNo(invoiceNo);
            invoice.setCurrencyType(CurrencyType.TL);
            invoice.setCurrencyFee(1.0);

            List<Category> list120 = categoryRepository.searchTo120CheckingCardId(invoice.getOtherCheckingCard().getCheckingCardId());
            List<Category> list320 = categoryRepository.searchTo320CheckingCardId(invoice.getCheckingCard().getCheckingCardId());

            if (list120.size() != 1 || list320.size() != 1) {
                throw new Exception("120 - 320 Muhasebe Kodları Bulunamadı !");
            }

            invoice.setCategory(list120.get(0));
            invoice.setOtherCategory(list320.get(0));

            invoice = invoiceRepository.save(invoice);


            for (UtilityInvoiceContent content : contentList) {
                totalPrice += content.getTotalPrice();
                content.setInvoice(invoice);
                utilityInvoiceContentRepository.save(content);
            }

            invoice.setTotalPrice(totalPrice);
            invoice.setTotalPriceCurrency(totalPrice);
            invoice = invoiceRepository.save(invoice);

            //fatura eden tarafa hareket ekleme
            AccountActivity accountActivity = new AccountActivity();
            accountActivity.setCurrencyFee(invoice.getCurrencyFee());
            accountActivity.setCurrencyType(invoice.getCurrencyType());
            accountActivity.setCheckingCard(invoice.getCheckingCard());
            accountActivity.setOtherCheckingCard(invoice.getOtherCheckingCard());
            accountActivity.setAccountActivityType(AccountActivityType.CUSTOMER_INVOICE);
            accountActivity.setInvoice(invoice);
            accountActivity.setInvoiceNo(invoiceNo);
            accountActivity.setCreatedAt(new Date());
            accountActivity.setCharge(invoice.getTotalPriceCurrency());
            accountActivity.setStatus(1);
            accountActivityRepository.save(accountActivity);

            //fatura edilen tarafa hareket ekleme
            AccountActivity accountActivity2 = new AccountActivity();
            accountActivity2.setCurrencyFee(invoice.getCurrencyFee());
            accountActivity2.setCurrencyType(invoice.getCurrencyType());
            accountActivity2.setOtherCheckingCard(invoice.getCheckingCard());
            accountActivity2.setCheckingCard(invoice.getOtherCheckingCard());
            accountActivity2.setAccountActivityType(AccountActivityType.PURCHASE_INVOICE);
            accountActivity2.setInvoice(invoice);
            accountActivity2.setInvoiceNo(invoiceNo);
            accountActivity2.setCreatedAt(new Date());
            accountActivity2.setDebt(invoice.getTotalPriceCurrency());
            accountActivity2.setStatus(1);
            accountActivityRepository.save(accountActivity2);


//            accountActivity.setAccount(accountRepository.findById(dto.getAccountId()).get());
//            accountActivity.setOtherAccount(accountRepository.findById(dto.getOtherAccountId()).get());
//            if (dto.getOtherAccountType().equals("CHEQUE") || dto.getOtherAccountType().equals("BOND")) {
//                accountActivity.setDocumentCreatedDate(dto.getDocumentCreatedDate());
//                accountActivity.setPaidDay(dto.getPayDay());
//                accountActivity.setDateOfIssue(dto.getDateOfIssue());
//                accountActivity.setBondPayerIdentityNumber(dto.getBondPayerIdentityNumber());
//            }

//            accountActivity2.setAccount(accountRepository.findById(dto.getOtherAccountId()).get());
//            accountActivity2.setOtherAccount(accountRepository.findById(dto.getAccountId()).get());
//            if (dto.getOtherAccountType().equals("CHEQUE") || dto.getOtherAccountType().equals("BOND")) {
//                accountActivity2.setDocumentCreatedDate(dto.getDocumentCreatedDate());
//                accountActivity2.setPaidDay(dto.getPayDay());
//                accountActivity2.setDateOfIssue(dto.getDateOfIssue());
//                accountActivity2.setBondPayerIdentityNumber(dto.getBondPayerIdentityNumber());
//            }


        } else if (dto.getInvoicePurpose().equals(InvoicePurpose.DOMESTIC_SELL_INVOICE.toString())) {
            contentList = utilityInvoiceContentRepository.getInvoiceContentList(user.getUserId(), InvoicePurpose.SELL_INVOICE.toString(),dto.getOtherCompanyId());
            invoice.setCheckingCard(otherCompanyCheckingCard);
            invoice.setOtherCheckingCard(checkingCardRepository.findById(dto.getOtherCheckingCardId()).get());
            invoice.setInvoicePurpose(InvoicePurpose.SELL_INVOICE);
            invoice.setInvoiceType(invoiceTypeRepository.findById(dto.getInvoiceType()).get());
            invoice.setCurrencyFee(1d);
            invoice.setCurrencyType(CurrencyType.TL);
            invoice.setUser(user);
            invoice.setCreatedAt(new Date());
            invoice.setStatus(1);
            invoice.setTotalChargePrice(0.0);
            invoice.setInvoiceStatus(invoiceStatusRepository.findById(10L).get());
            invoice.setUtilityInvoiceContents(contentList);
            invoice.setInvoiceDate(dto.getInvoiceDate());
            invoice.setInvoiceNo(dto.getInvoiceNo());
            invoice.setOtherCompanyId(dto.getOtherCompanyId());

            List<Category> list120 = categoryRepository.searchTo120CheckingCardId(invoice.getOtherCheckingCard().getCheckingCardId());
            List<Category> list320 = categoryRepository.searchTo320CheckingCardId(invoice.getCheckingCard().getCheckingCardId());

            if (list120.size() != 1 || list320.size() != 1) {
                throw new Exception("120 - 320 Muhasebe Kodları Bulunamadı !");
            }

            invoice.setCategory(list320.get(0));
            invoice.setOtherCategory(list120.get(0));

            invoice = invoiceRepository.save(invoice);
            //invoiceNo = generateInvoiceNo(invoice.getInvoiceId());
            //invoice.setInvoiceNo(invoiceNo);
            invoice.setCurrencyType(CurrencyType.TL);
            invoice.setCurrencyFee(1.0);


            for (UtilityInvoiceContent content : contentList) {
                totalPrice += content.getTotalPrice();
                content.setInvoice(invoice);
                utilityInvoiceContentRepository.save(content);
            }

            invoice.setTotalPrice(totalPrice);
            invoice.setTotalPriceCurrency(totalPrice);
            invoiceRepository.save(invoice);

            //fatura eden tarafa hareket ekleme
            AccountActivity accountActivity = new AccountActivity();
            accountActivity.setCurrencyFee(invoice.getCurrencyFee());
            accountActivity.setCurrencyType(invoice.getCurrencyType());
            accountActivity.setCheckingCard(invoice.getCheckingCard());
            accountActivity.setAccountActivityType(AccountActivityType.CUSTOMER_INVOICE);
            accountActivity.setOtherCheckingCard(invoice.getOtherCheckingCard());
            accountActivity.setInvoice(invoice);
            accountActivity.setInvoiceNo(invoiceNo);
            accountActivity.setCreatedAt(new Date());
            accountActivity.setCharge(invoice.getTotalPriceCurrency());
            accountActivity.setStatus(1);
            accountActivityRepository.save(accountActivity);

            //fatura edilen tarafa hareket ekleme
            AccountActivity accountActivity2 = new AccountActivity();
            accountActivity2.setCurrencyFee(invoice.getCurrencyFee());
            accountActivity2.setCurrencyType(invoice.getCurrencyType());
            accountActivity2.setOtherCheckingCard(invoice.getCheckingCard());
            accountActivity2.setAccountActivityType(AccountActivityType.PURCHASE_INVOICE);
            accountActivity2.setCheckingCard(invoice.getOtherCheckingCard());
            accountActivity2.setInvoice(invoice);
            accountActivity2.setInvoiceNo(invoiceNo);
            accountActivity2.setCreatedAt(new Date());
            accountActivity2.setDebt(invoice.getTotalPriceCurrency());
            accountActivity2.setStatus(1);
            accountActivityRepository.save(accountActivity2);

        }
        return invoice.getInvoiceNo();

    }

    public Boolean createBuyInvoiceContent(InvoiceCreateBuyServiceDto dto, String authHeader) throws Exception {

        if(dto.getOtherCompanyId()==null){
            throw new NotFoundException("Firma Id Bilgisi Bulunamadı !");
        }


        //Kullanıcı Bilgisi Alındı
        User user = controlService.getUserFromToken(authHeader);


        UtilityInvoiceContent utilityInvoiceContent = new UtilityInvoiceContent();
        utilityInvoiceContent.setDiscount(dto.getDiscount());
        utilityInvoiceContent.setQuantity(dto.getQuantity());
        utilityInvoiceContent.setDiscountSum(dto.getDiscountSum());
        utilityInvoiceContent.setCategory(categoryRepository.findById(Long.valueOf(dto.getCategoryId())).get());
        utilityInvoiceContent.setProductService(dto.getProductService());
        utilityInvoiceContent.setProductServiceSum(dto.getProductServiceSum());
        utilityInvoiceContent.setUnit(dto.getUnit());
        utilityInvoiceContent.setUnitPrice(dto.getUnitPrice());
        utilityInvoiceContent.setVat(dto.getVat());
        utilityInvoiceContent.setVatSum(dto.getVatSum());
        utilityInvoiceContent.setTotalPrice(dto.getTotalPrice());
        utilityInvoiceContent.setCreatedAt(new Date());
        utilityInvoiceContent.setUser(user);
        utilityInvoiceContent.setOtherCompanyId(dto.getOtherCompanyId());
        utilityInvoiceContent.setInvoicePurpose(InvoicePurpose.BUY_INVOICE);
        utilityInvoiceContentRepository.save(utilityInvoiceContent);


//        invoice.setInvoicePurpose(InvoicePurpose.BUY_INVOICE);
//        Optional<InvoiceType> invoiceType = invoiceTypeRepository.findById(2L);
//        if(!invoiceType.isPresent())
//            throw new NotFoundException("Fatura tipi bulunamadı");
//        invoice.setInvoiceType(invoiceType.get());
//        invoice = invoiceRepository.save(invoice);
        return true;
    }

    public Boolean createSellInvoiceContent(InvoiceCreateSellServiceDto dto, String authHeader) throws Exception {


        if(dto.getOtherCompanyId()==null){
            throw new NotFoundException("Firma Id Bilgisi Bulunamadı !");
        }

        //Kullanıcı Bilgisi Alındı
        User user = controlService.getUserFromToken(authHeader);

        UtilityInvoiceContent utilityInvoiceContent = new UtilityInvoiceContent();
        utilityInvoiceContent.setProductService(dto.getCategoryId());
        utilityInvoiceContent.setQuantity(dto.getQuantity());
        utilityInvoiceContent.setUnit(dto.getUnit());
        utilityInvoiceContent.setNetPrice(dto.getNetPrice());
        utilityInvoiceContent.setUnitPrice(dto.getUnitPrice());
        utilityInvoiceContent.setTagPrice(dto.getTagPrice());
        utilityInvoiceContent.setWareHousemanPrice(dto.getWareHousemanPrice());
        utilityInvoiceContent.setGeneralDiscount(dto.getGeneralDiscount());
        utilityInvoiceContent.setSellDiscount(dto.getSellDiscount());
        utilityInvoiceContent.setAdvanceDiscount(dto.getAdvanceDiscount());
        utilityInvoiceContent.setOtherCompanyId(dto.getOtherCompanyId());

        Optional<Category> category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(Long.valueOf(dto.getCategoryId()));
        }

        utilityInvoiceContent.setCategory(category.get());
        utilityInvoiceContent.setVat(dto.getVat());
        utilityInvoiceContent.setVatSum(dto.getVatSum());
        utilityInvoiceContent.setTotalPrice(dto.getTotalPrice());
        utilityInvoiceContent.setCreatedAt(new Date());
        utilityInvoiceContent.setUser(user);
        utilityInvoiceContent.setInvoicePurpose(InvoicePurpose.SELL_INVOICE);
        utilityInvoiceContent.setProductServiceSum(dto.getProductServiceSum());
        utilityInvoiceContentRepository.save(utilityInvoiceContent);
//        invoice.setInvoicePurpose(InvoicePurpose.SELL_INVOICE);
//        Optional<InvoiceType> invoiceType = invoiceTypeRepository.findById(2L);
//        if(!invoiceType.isPresent())
//            throw new NotFoundException("Fatura tipi bulunamadı");
//        invoice.setInvoiceType(invoiceType.get());
//        invoice = invoiceRepository.save(invoice);
        return true;
    }

    public List<InvoiceContentDto> getBuyInvoiceContentList(String authHeader, Long  otherCompanyId) throws Exception {
        List<InvoiceContentDto> dtosList = null;
        try {
            //Kullanıcı Bilgisi Alındı
            User user = controlService.getUserFromToken(authHeader);

            List<UtilityInvoiceContent> list = utilityInvoiceContentRepository.getInvoiceContentList(user.getUserId(), InvoicePurpose.BUY_INVOICE.toString(),otherCompanyId);
            InvoiceContentDto[] dtos = mapper.map(list, InvoiceContentDto[].class);
            dtosList = Arrays.asList(dtos);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return dtosList;
    }

    public List<InvoiceContentDto> getSellInvoiceContentList(String authHeader, Long  otherCompanyId) throws Exception {
        List<InvoiceContentDto> dtosList = null;

        try {
            //Kullanıcı Bilgisi Alındı
            User user = controlService.getUserFromToken(authHeader);

            List<UtilityInvoiceContent> list = utilityInvoiceContentRepository.getInvoiceContentList(user.getUserId(), InvoicePurpose.SELL_INVOICE.toString(),otherCompanyId);
            InvoiceContentDto[] dtos = mapper.map(list, InvoiceContentDto[].class);
            dtosList = Arrays.asList(dtos);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return dtosList;
    }

    public Boolean deleteInvoiceContent(Long invoicePurpose) throws Exception {

        try {
            utilityInvoiceContentRepository.deleteById(invoicePurpose);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public String createInvoicePdf(String authHeader, Long invoiceId) throws Exception {

        try {

            Optional<Invoice> optionalInvoice = invoiceRepository.findById(invoiceId);

            //ticari faturada kullanmak için fiş listesi değişkeni
            Receipt receipt = null;
            RefundReceipt refundReceipt = null;
            OtherReceipt otherReceipt = null;
            OtherRefundReceipt otherRefundReceipt =null;
            //hizmet faturasında kullanmak için içerik listesi değişkeni
            List<UtilityInvoiceContent> utilityInvoiceContents = null;

            /* Belge sonuna kullanıcı Id'si eklmek için kullanıcı bilgisi alınıyor */
            User user = controlService.getUserFromToken(authHeader);

            String fileName = "fatura_pdf_" + user.getUsername() + ".pdf";
            //PDF BAŞLANGIÇ
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("docs/" + fileName));
            document.open();

            Image image1 = Image.getInstance("image/logo/pharma.png");
            image1.setAlignment(Element.ALIGN_LEFT);
            image1.scaleAbsolute(60, 60);
            document.add(image1);

            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Paragraph date = new Paragraph(dateFormat.format(new Date()));
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);

            document.add(new Paragraph("\n"));

            BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
            Font catFont = new Font(bf, 16, Font.NORMAL, BaseColor.BLACK);


            PdfPTable table = null;
            Invoice invoice = optionalInvoice.get();

            //FATURA TİPİ TİCARİ İSE
            if (invoice.getInvoiceType().getInvoiceTypeId() == 1) {
                //TİCARİ ALIŞ FATURASI İSE
                if (invoice.getInvoicePurpose() == InvoicePurpose.BUY_INVOICE) {
                    //fiş listesi alınır
//                    if(optionalInvoice.get().getFinalReceipt() == null){
//                        throw new Exception("Muhasebe Fiş Bulunamadı");
//                    }

                    if(optionalInvoice.get().getOtherReceipt()==null){
                        if (optionalInvoice.get().getReceipt() == null) {
                            throw new Exception("Fiş Bulunamadı");
                        }
                        receipt = optionalInvoice.get().getReceipt();
                    }else{
                         otherReceipt=optionalInvoice.get().getOtherReceipt();
                    }


                    document.add(new Paragraph("\n"));

                    BaseFont bf2 = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
                    Font catFont2 = new Font(bf2, 16, Font.NORMAL, BaseColor.BLACK);

                    Paragraph tableHeaderCheckingCard = new Paragraph("Cari Kart Bilgileri", catFont2);
                    tableHeaderCheckingCard.setAlignment(Element.ALIGN_CENTER);
                    document.add(tableHeaderCheckingCard);

                    document.add(new Paragraph("\n"));

                    PdfPTable tableCheckingCard = new PdfPTable(2);
                    tableCheckingCard.setWidths(new int[]{6, 6});

                    tableCheckingCard.setWidthPercentage(100);
                    addTableCheckingCard(tableCheckingCard);

                    addRows(tableCheckingCard, optionalInvoice.get().getCheckingCard().getCheckingCardName(), 1);
                    addRows(tableCheckingCard, optionalInvoice.get().getOtherCheckingCard().getCheckingCardName(), 1);
                    addRowsWithNullControl(tableCheckingCard, "Vergi Dairesi : ", optionalInvoice.get().getCheckingCard().getTaxOffice(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Vergi Dairesi : ", optionalInvoice.get().getOtherCheckingCard().getTaxOffice(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Vergi Numarası : ", optionalInvoice.get().getCheckingCard().getTaxIdentificationNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Vergi Numarası : ", optionalInvoice.get().getOtherCheckingCard().getTaxIdentificationNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Adres : ", optionalInvoice.get().getCheckingCard().getAddress(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Adres : ", optionalInvoice.get().getOtherCheckingCard().getAddress(), 0);
                    addRowsWithNullControl(tableCheckingCard, "E-mail : ", optionalInvoice.get().getCheckingCard().getEmail(), 0);
                    addRowsWithNullControl(tableCheckingCard, "E-mail : ", optionalInvoice.get().getOtherCheckingCard().getEmail(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Fax : ", optionalInvoice.get().getCheckingCard().getFaxNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Fax : ", optionalInvoice.get().getOtherCheckingCard().getFaxNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Telefon No : ", optionalInvoice.get().getCheckingCard().getPhoneNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Telefon No : ", optionalInvoice.get().getOtherCheckingCard().getPhoneNumber(), 0);


                    document.add(tableCheckingCard);

                    //Tablonun Başlığı Girilir
                    Paragraph tableHeader = new Paragraph("Fatura İlaç Listesi", catFont);

                    tableHeader.setAlignment(Element.ALIGN_CENTER);
                    document.add(tableHeader);

                    document.add(new Paragraph("\n"));

                    //Tablonun Sütun Sayısı Girilir
                    table = new PdfPTable(8);
                    table.setWidths(new int[]{2, 5, 3, 3, 3, 3, 3, 3});


                    table.setWidthPercentage(100);
                    addTableHeader(table, InvoicePurpose.BUY_INVOICE);
                    //Hücrelere Veriler Girilir
                    int a = 0;
                    Double value = 0.0, accountTotalPrice = 0.0, totalPrice = 0.0, totalVatSum = 0.0;

                    if(receipt!=null){
                        //receipt ile doldurulur
                        for (CustomerSupplyOrder customerSupplyOrder : receipt.getCustomerSupplyOrders()) {
                            a++;
                            addRows(table, String.valueOf(a), 1);
                            addRows(table, customerSupplyOrder.getDrugCard().getDrugName(), 0);
                            addRows(table, customerSupplyOrder.getTotality().toString(), 1);


                            value = (double) ((int) ((customerSupplyOrder.getSupplyOrderPrice().getAccountTotalPrice() / customerSupplyOrder.getTotality()) * 1000.0)) / 1000.0;
//
//
                            addRows(table, value.toString(), 1);
                  if (customerSupplyOrder.getSupplyOrderPrice() != null) {
                            addRows(table, customerSupplyOrder.getSupplyOrderPrice().getVat().toString(), 1);

                            // toplam kdv fiyatı
                            totalVatSum += customerSupplyOrder.getSupplyOrderPrice().getVatSum();
                            value = ((double) ((int) (customerSupplyOrder.getSupplyOrderPrice().getVatSum() * 1000.0))) / 1000.0;
                            addRows(table, String.valueOf(value), 1);

                            // kdv siz toplam
                            totalPrice += customerSupplyOrder.getTotalPrice();
                            value = ((double) ((int) (customerSupplyOrder.getTotalPrice() * 1000.0))) / 1000.0;
                            addRows(table, String.valueOf(value), 1);

                            value = ((double) ((int) (customerSupplyOrder.getSupplyOrderPrice().getAccountTotalPrice() * 1000.0))) / 1000.0;
                            addRows(table, String.valueOf(value), 1);

                        } else {

                            addRows(table, "", 1);
                            addRows(table, "", 1);
                            addRows(table, "", 1);
                            addRows(table, "", 1);
                        }

                        }

                    }else{
                        //other receipt ile doldurulur
                        for (OtherReceiptPrice otherReceiptPrice : otherReceipt.getOtherReceiptPrices()) {
                            a++;
                            addRows(table, String.valueOf(a), 1);
                            addRows(table, otherReceiptPrice.getDrugCard().getDrugName(), 0);
                            addRows(table, otherReceiptPrice.getSupplyOrder().getTotality().toString(), 1);

                            value = (double) ((int) ((otherReceiptPrice.getAccountTotalPrice() / otherReceiptPrice.getSupplyOrder().getTotality()) * 1000.0)) / 1000.0;
//
//
                            addRows(table, value.toString(), 1);
                            if (otherReceiptPrice != null) {
                                addRows(table, otherReceiptPrice.getVat().toString(), 1);

                                // toplam kdv fiyatı
                                totalVatSum += otherReceiptPrice.getVatSum();
                                value = ((double) ((int) (otherReceiptPrice.getVatSum() * 1000.0))) / 1000.0;
                                addRows(table, String.valueOf(value), 1);

                                // kdv siz toplam
                                totalPrice += otherReceiptPrice.getAccountTotalPrice();
                                value = ((double) ((int) (otherReceiptPrice.getAccountTotalPrice() * 1000.0))) / 1000.0;
                                addRows(table, String.valueOf(value), 1);

                                value = ((double) ((int) (otherReceiptPrice.getAccountTotalPrice()  * 1000.0))) / 1000.0;
                                addRows(table, String.valueOf(value), 1);

                            } else {

                                addRows(table, "", 1);
                                addRows(table, "", 1);
                                addRows(table, "", 1);
                                addRows(table, "", 1);
                            }


                        }

                    }

                    document.add(table);

                    ////////////////////////////////////////////////////
                    document.add(new Paragraph("\n"));

                    Paragraph addTableResult = new Paragraph("", catFont);
                    addTableResult.setAlignment(Element.ALIGN_RIGHT);
                    document.add(addTableResult);

                    document.add(new Paragraph("\n"));

                    PdfPTable tableResult = new PdfPTable(2);
                    tableResult.setWidths(new int[]{8, 4});
                    tableResult.setHorizontalAlignment(Element.ALIGN_RIGHT);

                    tableResult.setWidthPercentage(50);
                    addTableResult(tableResult);


                    addRows(tableResult, "Mal Hizmet Toplam Tutarı", 0);
                    value = ((double) ((int) (totalPrice * 1000.0))) / 1000.0;
                    addRowsWithControl(tableResult, invoice.getCurrencyType(), value, 0);

                    addRows(tableResult, "Hesaplanan KDV (%8)", 0);
                    value = ((double) ((int) (totalVatSum * 1000.0))) / 1000.0;
                    addRowsWithControl(tableResult, invoice.getCurrencyType(), value, 0);

                    addRows(tableResult, "Vergiler Dahil Toplam Tutar", 0);
                    value = ((double) ((int) ((invoice.getTotalPrice() ) * 1000.0))) / 1000.0;
                    addRowsWithControl(tableResult, invoice.getCurrencyType(), value, 0);

                    addRows(tableResult, "Ödenecek Tutar", 0);
                    value = ((double) ((int) ((invoice.getTotalPriceCurrency() ) * 1000.0))) / 1000.0;
                    addRowsWithControl(tableResult, invoice.getCurrencyType(), value, 0);

                    document.add(tableResult);
                    ////////////////////////////////////////////////////
                }
                //TİCARİ MÜŞTERİ SATIŞ FATURASI İSE
                else if (invoice.getInvoicePurpose() == InvoicePurpose.SELL_INVOICE) {
                    //fiş listesi alınır
                    //CustomerReceipt customerReceipt = optionalInvoice.get().getFinalReceipt().getCustomerReceipt();
                    CustomerReceipt customerReceipt = optionalInvoice.get().getCustomerReceipt();

                    List<CustomerOrderDrugs> customerOrderDrugsList = customerReceipt.getCustomerOrderDrugs();

                    document.add(new Paragraph("\n"));

                    BaseFont bf2 = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
                    Font catFont2 = new Font(bf2, 16, Font.NORMAL, BaseColor.BLACK);

                    Paragraph tableHeaderCheckingCard = new Paragraph("Cari Kart Bilgileri", catFont2);
                    tableHeaderCheckingCard.setAlignment(Element.ALIGN_CENTER);
                    document.add(tableHeaderCheckingCard);

                    document.add(new Paragraph("\n"));

                    PdfPTable tableCheckingCard = new PdfPTable(2);
                    tableCheckingCard.setWidths(new int[]{6, 6});

                    tableCheckingCard.setWidthPercentage(100);
                    addTableCheckingCard(tableCheckingCard);

                    addRows(tableCheckingCard, optionalInvoice.get().getCheckingCard().getCheckingCardName(), 1);
                    addRows(tableCheckingCard, optionalInvoice.get().getOtherCheckingCard().getCheckingCardName(), 1);
                    addRowsWithNullControl(tableCheckingCard, "Vergi Dairesi : ", optionalInvoice.get().getCheckingCard().getTaxOffice(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Vergi Dairesi : ", optionalInvoice.get().getOtherCheckingCard().getTaxOffice(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Vergi Numarası : ", optionalInvoice.get().getCheckingCard().getTaxIdentificationNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Vergi Numarası : ", optionalInvoice.get().getOtherCheckingCard().getTaxIdentificationNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Adres : ", optionalInvoice.get().getCheckingCard().getAddress(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Adres : ", optionalInvoice.get().getOtherCheckingCard().getAddress(), 0);
                    addRowsWithNullControl(tableCheckingCard, "E-mail : ", optionalInvoice.get().getCheckingCard().getEmail(), 0);
                    addRowsWithNullControl(tableCheckingCard, "E-mail : ", optionalInvoice.get().getOtherCheckingCard().getEmail(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Fax : ", optionalInvoice.get().getCheckingCard().getFaxNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Fax : ", optionalInvoice.get().getOtherCheckingCard().getFaxNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Telefon No : ", optionalInvoice.get().getCheckingCard().getPhoneNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Telefon No : ", optionalInvoice.get().getOtherCheckingCard().getPhoneNumber(), 0);

                    document.add(tableCheckingCard);

                    //Tablonun Başlığı Girilir
                    Paragraph tableHeader = new Paragraph("Fatura İlaç Listesi", catFont);

                    tableHeader.setAlignment(Element.ALIGN_CENTER);
                    document.add(tableHeader);

                    document.add(new Paragraph("\n"));

                    //Tablonun Sütun Sayısı Girilir
                    table = new PdfPTable(6);
                    table.setWidths(new int[]{2, 6, 2, 3, 3, 4});


                    table.setWidthPercentage(100);
                    addTableHeader(table, InvoicePurpose.SELL_INVOICE);

                    int totalDrug = 0;
                    for (CustomerOrderDrugs drug : customerOrderDrugsList)
                        totalDrug += drug.getTotalQuantity();

                    //Hücrelere Veriler Girilir
                    int a = 0, totalDrugQuantity = 0;
                    for (CustomerOrderDrugs cod : customerOrderDrugsList) {
                        a++;
                        totalDrugQuantity += cod.getTotalQuantity();
                        addRows(table, String.valueOf(a), 1);
                        addRows(table, cod.getDrugCard().getDrugName(), 0);
                        addRows(table, cod.getTotalQuantity().toString(), 1);

                        Double calculate = (cod.getUnitPrice() / cod.getInstantCurrencyFee());
                        Double unitPrice = ((double) ((int) (calculate * 1000.0))) / 1000.0;

                        addRows(table, String.valueOf(unitPrice) + " " + cod.getCustomerOrder().getCurrencyType(), 1);

                        if (cod.getFreightCostTl() != null) {
                            //Double totalFreight = ((double) ((int) ((cod.getFreightCostTl()/cod.getInstantCurrencyFee()) * 1000.0))) / 1000.0;
                            Double totalFreight = ((double) ((int) ((totalDrug * invoice.getFreightCostCurrency() / cod.getTotalQuantity()) * 1000.0))) / 1000.0;
                            addRows(table, String.valueOf(totalFreight) + " " + cod.getCustomerOrder().getCurrencyType(), 1);
                        } else{
                            if(invoice.getFreightCostCurrency()!=null){
                                addRows(table, String.valueOf(invoice.getFreightCostCurrency()+" "+invoice.getCurrencyType()), 1);
                            }else{
                                addRows(table, String.valueOf(""), 1);
                            }
                        }


                        Double calculate2 = ((cod.getTotalQuantity() * cod.getUnitPrice()) / cod.getInstantCurrencyFee());
                        Double totalPrice = ((double) ((int) (calculate2 * 1000.0))) / 1000.0;

                        addRows(table, String.valueOf(totalPrice) + " " + cod.getCustomerOrder().getCurrencyType(), 1);
                    }
                    document.add(table);

                    ////////////////////////////////////////////////////

                    Paragraph addTableResult = new Paragraph("", catFont);
                    addTableResult.setAlignment(Element.ALIGN_RIGHT);
                    document.add(addTableResult);

                    document.add(new Paragraph("\n"));

                    PdfPTable tableResult = new PdfPTable(2);
                    tableResult.setWidths(new int[]{8, 4});
                    tableResult.setHorizontalAlignment(Element.ALIGN_RIGHT);

                    tableResult.setWidthPercentage(50);
                    addTableResult(tableResult);

                    addRows(tableResult, "Mal Hizmet Toplam Tutarı", 0);
                    addRowsWithControl(tableResult, invoice.getCurrencyType(), invoice.getTotalPriceCurrency(), 0);

                    addRows(tableResult, "Navlun", 0);
                    addRowsWithControl(tableResult, invoice.getCurrencyType(), invoice.getFreightCostCurrency(), 0);

                    addRows(tableResult, "Hesaplanan KDV (%0)", 0);
                    addRows(tableResult, " ", 0);

                    addRows(tableResult, "Vergiler Dahil Toplam Tutar", 0);
                    if (invoice.getTotalPriceCurrency() != null && invoice.getFreightCostCurrency() != null)
                        addRowsWithControl(tableResult, invoice.getCurrencyType(), invoice.getFreightCostCurrency() + invoice.getTotalPriceCurrency(), 0);
                    else
                        addRows(tableResult, " ", 0);

                    addRows(tableResult, "Ödenecek Tutar", 0);
                    if (invoice.getTotalPriceCurrency() != null && invoice.getFreightCostCurrency() != null)
                        addRowsWithControl(tableResult, invoice.getCurrencyType(), invoice.getFreightCostCurrency() + invoice.getTotalPriceCurrency(), 0);
                    else
                        addRows(tableResult, " ", 0);

                    addRows(tableResult, "Mal Hizmet Toplam Tutarı (TL)", 0);
                    addRowsWithControl(tableResult, CurrencyType.TL, roundDoubleData(invoice.getTotalPrice()), 0);

                    addRows(tableResult, "Navlun (TL)", 0);
                    addRowsWithControl(tableResult, CurrencyType.TL, roundDoubleData(invoice.getFreightCostTl()), 0);

                    addRows(tableResult, "Vergiler Dahil Toplam Tutar (TL)", 0);
                    if (invoice.getTotalPrice() != null && invoice.getFreightCostTl() != null)
                        addRowsWithControl(tableResult, CurrencyType.TL, roundDoubleData(invoice.getFreightCostTl() + invoice.getTotalPrice()), 0);
                    else
                        addRows(tableResult, " ", 0);

                    addRows(tableResult, "Ödenecek Tutar (TL)", 0);
                    if (invoice.getTotalPrice() != null && invoice.getFreightCostTl() != null)
                        addRowsWithControl(tableResult, CurrencyType.TL, roundDoubleData(invoice.getFreightCostTl() + invoice.getTotalPrice()), 0);
                    else
                        addRows(tableResult, " ", 0);
                    document.add(tableResult);

                    ////////////////////////////////////////////////////

                    Paragraph addEndTable = new Paragraph("", catFont);
                    addEndTable.setAlignment(Element.ALIGN_RIGHT);
                    document.add(addEndTable);

                    document.add(new Paragraph("\n"));

                    PdfPTable endTableResult = new PdfPTable(1);
                    endTableResult.setWidths(new int[]{10});
                    endTableResult.setHorizontalAlignment(Element.ALIGN_LEFT);

                    endTableResult.setWidthPercentage(100);

                    addRows(endTableResult, "Yalnız " + invoice.getTotalPriceExpression() + "'dir", 0);
                    addRows(endTableResult, "Yalnız " + invoice.getTotalPriceCurrencyExpression() + "'dir", 0);
                    addRows(endTableResult, "Döviz Kuru : " + invoice.getInstantCurrencyFee(), 0);
                    addRows(endTableResult, "Navlun Bedeli : " + invoice.getFreightCostCurrency().toString(), 0);
                    addRows(endTableResult, "Fatura Vadesi : " + dateFormat.format(invoice.getPaymentTerm()), 0);
                    addRows(endTableResult, "Mersis No : " + invoice.getCrsNo(), 0);
                    addRows(endTableResult, "TOPLAM " + customerOrderDrugsList.size() + " KALEM, " + totalDrugQuantity + " ADETTİR.", 0);
                    addRows(endTableResult, "Ödeme Notu : " + dateFormat.format(invoice.getPaymentTerm()) + " - " + invoice.getTotalPriceCurrency() + " " + invoice.getCurrencyType(), 0);
                    addRows(endTableResult, "İrsaliye yerine geçer", 0);

                    document.add(endTableResult);
                    ////////////////////////////////////////////////////////

                }
                //HİZMET SATIŞ
                else if (invoice.getInvoicePurpose() == InvoicePurpose.DOMESTIC_SELL_INVOICE) {
                    //fiş listesi alınır
//                    if(optionalInvoice.get().getFinalReceipt() == null){
//                        throw new Exception("Muhasebe Fiş Bulunamadı");
//                    }
//                    if (optionalInvoice.get().getRefundReceipt() == null) {
//                        throw new Exception("Fiş Bulunamadı");
//                    }

                    if(optionalInvoice.get().getOtherRefundReceipt()==null){
                        if (optionalInvoice.get().getRefundReceipt() == null) {
                            throw new Exception("Fiş Bulunamadı");
                        }
                        refundReceipt = optionalInvoice.get().getRefundReceipt();
                    }else{
                        otherRefundReceipt=optionalInvoice.get().getOtherRefundReceipt();
                    }




                    document.add(new Paragraph("\n"));

                    BaseFont bf2 = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
                    Font catFont2 = new Font(bf2, 16, Font.NORMAL, BaseColor.BLACK);

                    Paragraph tableHeaderCheckingCard = new Paragraph("Cari Kart Bilgileri", catFont2);
                    tableHeaderCheckingCard.setAlignment(Element.ALIGN_CENTER);
                    document.add(tableHeaderCheckingCard);

                    document.add(new Paragraph("\n"));

                    PdfPTable tableCheckingCard = new PdfPTable(2);
                    tableCheckingCard.setWidths(new int[]{6, 6});

                    tableCheckingCard.setWidthPercentage(100);
                    addTableCheckingCard(tableCheckingCard);

                    addRows(tableCheckingCard, optionalInvoice.get().getCheckingCard().getCheckingCardName(), 1);
                    addRows(tableCheckingCard, optionalInvoice.get().getOtherCheckingCard().getCheckingCardName(), 1);
                    addRowsWithNullControl(tableCheckingCard, "Vergi Dairesi : ", optionalInvoice.get().getCheckingCard().getTaxOffice(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Vergi Dairesi : ", optionalInvoice.get().getOtherCheckingCard().getTaxOffice(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Vergi Numarası : ", optionalInvoice.get().getCheckingCard().getTaxIdentificationNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Vergi Numarası : ", optionalInvoice.get().getOtherCheckingCard().getTaxIdentificationNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Adres : ", optionalInvoice.get().getCheckingCard().getAddress(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Adres : ", optionalInvoice.get().getOtherCheckingCard().getAddress(), 0);
                    addRowsWithNullControl(tableCheckingCard, "E-mail : ", optionalInvoice.get().getCheckingCard().getEmail(), 0);
                    addRowsWithNullControl(tableCheckingCard, "E-mail : ", optionalInvoice.get().getOtherCheckingCard().getEmail(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Fax : ", optionalInvoice.get().getCheckingCard().getFaxNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Fax : ", optionalInvoice.get().getOtherCheckingCard().getFaxNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Telefon No : ", optionalInvoice.get().getCheckingCard().getPhoneNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Telefon No : ", optionalInvoice.get().getOtherCheckingCard().getPhoneNumber(), 0);


                    document.add(tableCheckingCard);

                    //Tablonun Başlığı Girilir
                    Paragraph tableHeader = new Paragraph("Fatura İlaç Listesi", catFont);

                    tableHeader.setAlignment(Element.ALIGN_CENTER);
                    document.add(tableHeader);

                    document.add(new Paragraph("\n"));

                    //Tablonun Sütun Sayısı Girilir
                    table = new PdfPTable(8);
                    table.setWidths(new int[]{2, 5, 3, 3, 3, 3, 3, 3});


                    table.setWidthPercentage(100);
                    addTableHeader(table, InvoicePurpose.DOMESTIC_SELL_INVOICE);
                    //Hücrelere Veriler Girilir
                    int a = 0;
                    Double value = 0.0, accountTotalPrice = 0.0, totalPrice = 0.0, totalVatSum = 0.0;

                    if(refundReceipt!=null){
                        for (Refund refund : refundReceipt.getRefunds()) {
                            a++;
                            addRows(table, String.valueOf(a), 1);
                            addRows(table, refund.getDrugCard().getDrugName(), 0);
                            addRows(table, refund.getTotality().toString(), 1);

                            Double unitPrice=0D;

                            unitPrice=refund.getRefundPrice().getAccountTotalPrice()/refund.getTotality();

                            value = ((double) ((int) (unitPrice * 1000.0))) / 1000.0;
                            addRows(table, new DecimalFormat("##.##").format(value), 1);

                            addRows(table, refund.getRefundPrice().getVat().toString(), 1);

                            // toplam kdv fiyatı
                            totalVatSum += unitPrice*refund.getTotality()*(refund.getRefundPrice().getVat()*0.01);
                            value = ((double) ((int) (totalVatSum * 1000.0))) / 1000.0;
                            addRows(table, String.valueOf(value), 1);

                            // kdv siz toplam
                            totalPrice += unitPrice*refund.getTotality();
                            value = ((double) ((int) (totalPrice * 1000.0))) / 1000.0;
                            addRows(table, String.valueOf(value), 1);

                            value = ((double) ((int) (totalPrice * 1000.0))) / 1000.0;
                            addRows(table, String.valueOf(value), 1);
                            break;//sadece 1 kez çalışsın
                        }
                    }else{
                        for (OtherRefundPrice otherRefundPrice : otherRefundReceipt.getOtherRefundPrices()) {

                            Refund refund=otherRefundPrice.getRefund();
                            a++;
                            addRows(table, String.valueOf(a), 1);
                            addRows(table, otherRefundPrice.getDrugCard().getDrugName(), 0);
                            addRows(table, refund.getTotality().toString(), 1);

                            Double unitPrice=0D;

                            unitPrice=otherRefundPrice.getAccountTotalPrice()/refund.getTotality();

                            value = ((double) ((int) (unitPrice * 1000.0))) / 1000.0;
                            addRows(table, new DecimalFormat("##.##").format(value), 1);

                            addRows(table, otherRefundPrice.getVat().toString(), 1);

                            // toplam kdv fiyatı
                            totalVatSum += unitPrice*refund.getTotality()*(otherRefundPrice.getVat()*0.01);
                            value = ((double) ((int) (totalVatSum * 1000.0))) / 1000.0;
                            addRows(table, String.valueOf(value), 1);

                            // kdv siz toplam
                            totalPrice += unitPrice*refund.getTotality();
                            value = ((double) ((int) (totalPrice * 1000.0))) / 1000.0;
                            addRows(table, String.valueOf(value), 1);

                            value = ((double) ((int) (totalPrice * 1000.0))) / 1000.0;
                            addRows(table, String.valueOf(value), 1);
                            break;//sadece 1 kez çalışsın
                        }
                    }




                    document.add(table);

                    ////////////////////////////////////////////////////
                    document.add(new Paragraph("\n"));

                    Paragraph addTableResult = new Paragraph("", catFont);
                    addTableResult.setAlignment(Element.ALIGN_RIGHT);
                    document.add(addTableResult);

                    document.add(new Paragraph("\n"));

                    PdfPTable tableResult = new PdfPTable(2);
                    tableResult.setWidths(new int[]{8, 4});
                    tableResult.setHorizontalAlignment(Element.ALIGN_RIGHT);

                    tableResult.setWidthPercentage(50);
                    addTableResult(tableResult);

                    addRows(tableResult, "Mal Hizmet Toplam Tutarı", 0);
                    value = ((double) ((int) (totalPrice * 1000.0))) / 1000.0;
                    addRowsWithControl(tableResult, invoice.getCurrencyType(), (new DecimalFormat("##.##").format(totalPrice)), 0);

                    addRows(tableResult, "Hesaplanan KDV (%8)", 0);

                    addRowsWithControl(tableResult, invoice.getCurrencyType(), new DecimalFormat("##.##").format(totalVatSum), 0);

                    addRows(tableResult, "Vergiler Dahil Toplam Tutar", 0);
                    addRowsWithControl(tableResult, invoice.getCurrencyType(), new DecimalFormat("##.##").format(totalPrice + totalVatSum), 0);

                    addRows(tableResult, "Ödenecek Tutar", 0);
                    addRowsWithControl(tableResult, invoice.getCurrencyType(), new DecimalFormat("##.##").format(totalPrice + totalVatSum), 0);

                    document.add(tableResult);
                    ////////////////////////////////////////////////////

                }
                //TİCARİ İADE SATIŞ FATURASI İSE
                else if (invoice.getInvoicePurpose() == InvoicePurpose.REFUND_SELL_INVOICE) {
                    //fiş listesi alınır
                    receipt = optionalInvoice.get().getReceipt();

                    document.add(new Paragraph("\n"));

                    BaseFont bf2 = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
                    Font catFont2 = new Font(bf2, 16, Font.NORMAL, BaseColor.BLACK);

                    Paragraph tableHeaderCheckingCard = new Paragraph("Cari Kart Bilgileri", catFont2);
                    tableHeaderCheckingCard.setAlignment(Element.ALIGN_CENTER);
                    document.add(tableHeaderCheckingCard);

                    document.add(new Paragraph("\n"));

                    PdfPTable tableCheckingCard = new PdfPTable(2);
                    tableCheckingCard.setWidths(new int[]{6, 6});

                    tableCheckingCard.setWidthPercentage(100);
                    addTableCheckingCard(tableCheckingCard);

                    addRows(tableCheckingCard, optionalInvoice.get().getCheckingCard().getCheckingCardName(), 1);
                    addRows(tableCheckingCard, optionalInvoice.get().getOtherCheckingCard().getCheckingCardName(), 1);
                    addRowsWithNullControl(tableCheckingCard, "Vergi Dairesi : ", optionalInvoice.get().getCheckingCard().getTaxOffice(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Vergi Dairesi : ", optionalInvoice.get().getOtherCheckingCard().getTaxOffice(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Vergi Numarası : ", optionalInvoice.get().getCheckingCard().getTaxIdentificationNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Vergi Numarası : ", optionalInvoice.get().getOtherCheckingCard().getTaxIdentificationNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Adres : ", optionalInvoice.get().getCheckingCard().getAddress(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Adres : ", optionalInvoice.get().getOtherCheckingCard().getAddress(), 0);
                    addRowsWithNullControl(tableCheckingCard, "E-mail : ", optionalInvoice.get().getCheckingCard().getEmail(), 0);
                    addRowsWithNullControl(tableCheckingCard, "E-mail : ", optionalInvoice.get().getOtherCheckingCard().getEmail(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Fax : ", optionalInvoice.get().getCheckingCard().getFaxNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Fax : ", optionalInvoice.get().getOtherCheckingCard().getFaxNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Telefon No : ", optionalInvoice.get().getCheckingCard().getPhoneNumber(), 0);
                    addRowsWithNullControl(tableCheckingCard, "Telefon No : ", optionalInvoice.get().getOtherCheckingCard().getPhoneNumber(), 0);

                    document.add(tableCheckingCard);

                    //Tablonun Başlığı Girilir
                    Paragraph tableHeader = new Paragraph("Fatura İlaç Listesi", catFont);

                    tableHeader.setAlignment(Element.ALIGN_CENTER);
                    document.add(tableHeader);

                    document.add(new Paragraph("\n"));

                    //Tablonun Sütun Sayısı Girilir
                    table = new PdfPTable(8);
                    table.setWidths(new int[]{2, 6, 3, 3, 3, 3, 3, 3});


                    table.setWidthPercentage(100);
                    addTableHeader(table, InvoicePurpose.REFUND_SELL_INVOICE);
                    //Hücrelere Veriler Girilir
                    int a = 0, totalDrugQuantity = 0;
                    Double value = 0.0, accountTotalPrice = 0.0, totalPrice = 0.0, totalVatSum = 0.0;
                    for (Refund refund : receipt.getRefunds()) {
                        a++;
                        totalDrugQuantity += refund.getTotality();
                        addRows(table, String.valueOf(a), 1);
                        addRows(table, refund.getDrugCard().getDrugName(), 0);
                        addRows(table, refund.getTotality().toString(), 1);

                        Double unitPrice = ((double) ((int) (refund.getUnitPrice() * 1000.0))) / 1000.0;
                        addRows(table, unitPrice.toString(), 1);

                        addRows(table, refund.getRefundPrice().getVat().toString(), 1);

                        // toplam kdv fiyatı
                        totalVatSum += refund.getRefundPrice().getVatSum();
                        value = ((double) ((int) (refund.getRefundPrice().getVatSum() * 1000.0))) / 1000.0;
                        addRows(table, String.valueOf(value), 1);

                        // kdv siz toplam
                        totalPrice += refund.getTotalPrice();
                        value = ((double) ((int) (refund.getTotalPrice() * 1000.0))) / 1000.0;
                        addRows(table, String.valueOf(value), 1);

                        value = ((double) ((int) (refund.getRefundPrice().getAccountTotalPrice() * 1000.0))) / 1000.0;
                        addRows(table, String.valueOf(value), 1);

                    }
                    document.add(table);

                    ////////////////////////////////////////////////////
                    document.add(new Paragraph("\n"));

                    Paragraph addTableResult = new Paragraph("", catFont);
                    addTableResult.setAlignment(Element.ALIGN_RIGHT);
                    document.add(addTableResult);

                    document.add(new Paragraph("\n"));

                    PdfPTable tableResult = new PdfPTable(2);
                    tableResult.setWidths(new int[]{8, 4});
                    tableResult.setHorizontalAlignment(Element.ALIGN_RIGHT);

                    tableResult.setWidthPercentage(50);
                    addTableResult(tableResult);

                    addRows(tableResult, "Mal Hizmet Toplam Tutarı", 0);
                    value = ((double) ((int) (totalPrice * 1000.0))) / 1000.0;
                    addRowsWithControl(tableResult, invoice.getCurrencyType(), value, 0);

                    addRows(tableResult, "Hesaplanan KDV (%8)", 0);
                    value = ((double) ((int) (totalVatSum * 1000.0))) / 1000.0;
                    addRowsWithControl(tableResult, invoice.getCurrencyType(), value, 0);

                    addRows(tableResult, "KDV Matrahı", 0);
                    value = ((double) ((int) (totalPrice * 1000.0))) / 1000.0;
                    addRowsWithControl(tableResult, invoice.getCurrencyType(), value, 0);

                    addRows(tableResult, "Vergiler Dahil Toplam Tutar", 0);
                    addRowsWithControl(tableResult, invoice.getCurrencyType(), invoice.getTotalPrice(), 0);

                    addRows(tableResult, "Ödenecek Tutar", 0);
                    addRowsWithControl(tableResult, invoice.getCurrencyType(), invoice.getTotalPriceCurrency(), 0);

                    document.add(tableResult);

                    ////////////////////////////////////////////////////

                    Paragraph addEndTable = new Paragraph("", catFont);
                    addEndTable.setAlignment(Element.ALIGN_RIGHT);
                    document.add(addEndTable);

                    document.add(new Paragraph("\n"));

                    PdfPTable endTableResult = new PdfPTable(1);
                    endTableResult.setWidths(new int[]{10});
                    endTableResult.setHorizontalAlignment(Element.ALIGN_LEFT);

                    endTableResult.setWidthPercentage(100);

                    addRows(endTableResult, "Yalnız " + invoice.getTotalPriceExpression() + "'dir", 0);
                    addRows(endTableResult, "Eczane : " + invoice.getReceipt().getSupplier().getSupplierName(), 0);
                    addRows(endTableResult, "Fatura Vadesi : " + dateFormat.format(invoice.getPaymentTerm()), 0);
                    addRows(endTableResult, "Mersis No : " + invoice.getCrsNo(), 0);
                    addRows(endTableResult, "TOPLAM " + a + " KALEM, " + totalDrugQuantity + " ADETTİR.", 0);
                    addRows(endTableResult, "Ödeme Notu : " + dateFormat.format(invoice.getPaymentTerm()) + " - " + invoice.getTotalPrice(), 0);
                    addRows(endTableResult, "İrsaliye yerine geçer", 0);

                    document.add(endTableResult);
                    ////////////////////////////////////////////////////////
                }
                //FATURA TİPİ HİZMET İSE
            } else if (optionalInvoice.get().getInvoiceType().getInvoiceTypeId() == 2) {
                //içerik listesi alınır
                utilityInvoiceContents = optionalInvoice.get().getUtilityInvoiceContents();

                document.add(new Paragraph("\n"));

                BaseFont bf2 = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
                Font catFont2 = new Font(bf2, 16, Font.NORMAL, BaseColor.BLACK);

                Paragraph tableHeaderCheckingCard = new Paragraph("Cari Kart Bilgileri", catFont2);
                tableHeaderCheckingCard.setAlignment(Element.ALIGN_CENTER);
                document.add(tableHeaderCheckingCard);

                document.add(new Paragraph("\n"));

                PdfPTable tableCheckingCard = new PdfPTable(2);
                tableCheckingCard.setWidths(new int[]{6, 6});

                tableCheckingCard.setWidthPercentage(100);
                addTableCheckingCard(tableCheckingCard);

                addRows(tableCheckingCard, optionalInvoice.get().getCheckingCard().getCheckingCardName(), 1);
                addRows(tableCheckingCard, optionalInvoice.get().getOtherCheckingCard().getCheckingCardName(), 1);
                addRowsWithNullControl(tableCheckingCard, "Vergi Dairesi : ", optionalInvoice.get().getCheckingCard().getTaxOffice(), 0);
                addRowsWithNullControl(tableCheckingCard, "Vergi Dairesi : ", optionalInvoice.get().getOtherCheckingCard().getTaxOffice(), 0);
                addRowsWithNullControl(tableCheckingCard, "Vergi Numarası : ", optionalInvoice.get().getCheckingCard().getTaxIdentificationNumber(), 0);
                addRowsWithNullControl(tableCheckingCard, "Vergi Numarası : ", optionalInvoice.get().getOtherCheckingCard().getTaxIdentificationNumber(), 0);
                addRowsWithNullControl(tableCheckingCard, "Adres : ", optionalInvoice.get().getCheckingCard().getAddress(), 0);
                addRowsWithNullControl(tableCheckingCard, "Adres : ", optionalInvoice.get().getOtherCheckingCard().getAddress(), 0);
                addRowsWithNullControl(tableCheckingCard, "E-mail : ", optionalInvoice.get().getCheckingCard().getEmail(), 0);
                addRowsWithNullControl(tableCheckingCard, "E-mail : ", optionalInvoice.get().getOtherCheckingCard().getEmail(), 0);
                addRowsWithNullControl(tableCheckingCard, "Fax : ", optionalInvoice.get().getCheckingCard().getFaxNumber(), 0);
                addRowsWithNullControl(tableCheckingCard, "Fax : ", optionalInvoice.get().getOtherCheckingCard().getFaxNumber(), 0);
                addRowsWithNullControl(tableCheckingCard, "Telefon No : ", optionalInvoice.get().getCheckingCard().getPhoneNumber(), 0);
                addRowsWithNullControl(tableCheckingCard, "Telefon No : ", optionalInvoice.get().getOtherCheckingCard().getPhoneNumber(), 0);

                document.add(tableCheckingCard);

                //Tablonun Başlığı Girilir
                Paragraph tableHeader = new Paragraph("Hizmet Faturası", catFont);

                tableHeader.setAlignment(Element.ALIGN_CENTER);
                document.add(tableHeader);

                document.add(new Paragraph("\n"));

                //Tablonun Sütun Sayısı Girilir
                table = new PdfPTable(7);
                table.setWidths(new int[]{2, 6, 3, 2, 2, 2, 2});

                table.setWidthPercentage(100);
                addTableHeaderForServiceInvoice(table);
                //Hücrelere Veriler Girilir
                int a = 0;
                Double productServiceSum = 0.0, vatSum = 0.0;
                for (UtilityInvoiceContent content : utilityInvoiceContents) {

                    a++;
                    addRows(table, String.valueOf(a), 1);
                    // addRows(table, content.getProductCode(), 1);
                    if (invoice.getInvoicePurpose() == InvoicePurpose.BUY_INVOICE)
                        addRows(table, content.getProductService(), 0);
                    else
                        addRows(table, content.getCategory().getName(), 0);
                    addRows(table, String.valueOf(content.getQuantity()) + " " + content.getUnit(), 1);
                    addRows(table, content.getUnitPrice().toString(), 1);
                    addRows(table, content.getVat().toString(), 1);
                    addRows(table, content.getVatSum().toString(), 1);
                    if (content.getProductServiceSum() != null)
                        addRows(table, content.getProductServiceSum().toString(), 1);
                    else
                        addRows(table, "", 1);
                    vatSum += content.getVatSum();
                    if (content.getProductServiceSum() != null)
                        productServiceSum += content.getProductServiceSum();

                }
                document.add(table);

                ////////////////////////////////////////////////////
                document.add(new Paragraph("\n"));

                Paragraph addTableResult = new Paragraph("", catFont);
                addTableResult.setAlignment(Element.ALIGN_RIGHT);
                document.add(addTableResult);

                document.add(new Paragraph("\n"));

                PdfPTable tableResult = new PdfPTable(2);
                tableResult.setWidths(new int[]{8, 4});
                tableResult.setHorizontalAlignment(Element.ALIGN_RIGHT);

                tableResult.setWidthPercentage(50);
                addTableResult(tableResult);

                addRows(tableResult, "Mal Hizmet Toplam Tutarı", 0);
                addRowsWithControl(tableResult, CurrencyType.TL, productServiceSum.toString(), 0);

                addRows(tableResult, "Hesaplanan KDV Tutarı", 0);
                addRowsWithControl(tableResult, CurrencyType.TL, vatSum.toString(), 0);

                addRows(tableResult, "Vergiler Dahil Toplam Tutar", 0);
                addRowsWithControl(tableResult, CurrencyType.TL, invoice.getTotalPrice().toString(), 0);

                addRows(tableResult, "Ödenecek Tutar", 0);
                addRowsWithControl(tableResult, CurrencyType.TL, invoice.getTotalPrice().toString(), 0);

                document.add(tableResult);
                ////////////////////////////////////////////////////
            }

            document.close();
            //PDF SON
            int index = fileName.indexOf(".pdf");
            fileName=fileName.substring(0,index);
            return fileName;
        } catch (Exception e) {
            throw new Exception("Fatura Pdf Oluşturma İşleminde Hata Oluştu.", e);
        }
    }

    private void addRowsWithControl(PdfPTable table, CurrencyType type, Object value, int alignmentValue) throws IOException, DocumentException {
        if (value != null)
            addRows(table, String.valueOf(value) + " " + type, alignmentValue);
        else
            addRows(table, " ", alignmentValue);
    }

    private void addRowsWithNullControl(PdfPTable table, String expression, Object value, int alignmentValue) throws IOException, DocumentException {
        if (value != null)
            addRows(table, expression + " " + value, alignmentValue);
        else
            addRows(table, expression, alignmentValue);
    }

    private void addTableResult(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);

        //Tablo Sütün Başlıkları Girilir
        Stream.of(" ", " ")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(1);
                    header.setPhrase(new Phrase(columnTitle, catFont));
                    header.setPadding(3);
                    header.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    header.setVerticalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(header);
                });
    }

    private void addEndTable(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);

        //Tablo Sütün Başlıkları Girilir
        Stream.of(" ")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(1);
                    header.setPhrase(new Phrase(columnTitle, catFont));
                    header.setPadding(3);
                    header.setHorizontalAlignment(Element.ALIGN_LEFT);
                    header.setVerticalAlignment(Element.ALIGN_LEFT);
                    table.addCell(header);
                });
    }

    private void addTableCheckingCard(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);

        //Tablo Sütün Başlıkları Girilir
        Stream.of("Fatura Eden", "Fatura Edilen")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(1);
                    header.setPhrase(new Phrase(columnTitle, catFont));
                    header.setPadding(3);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private void addTableHeader(PdfPTable table, InvoicePurpose purpose) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);

        if (purpose == InvoicePurpose.BUY_INVOICE) {
            //Tablo Sütün Başlıkları Girilir
            Stream.of("No", "Mal Hizmet", "Miktar", "Birim Fiyat", "KDV Oranı", "KDV Tutarı", "KDVsiz Tutar", "Mal Hizmet Tutarı")
                    .forEach(columnTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setBorderWidth(1);
                        header.setPhrase(new Phrase(columnTitle, catFont));
                        header.setPadding(3);
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);


                        table.addCell(header);

                    });
        } else if (purpose == InvoicePurpose.REFUND_SELL_INVOICE) {
            //Tablo Sütün Başlıkları Girilir
            Stream.of("No", "Mal Hizmet", "Miktar", "Birim Fiyat", "KDV Oranı", "KDV Tutarı", "KDVsiz Tutar", "Mal Hizmet Tutarı")
                    .forEach(columnTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setBorderWidth(1);
                        header.setPhrase(new Phrase(columnTitle, catFont));
                        header.setPadding(3);
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);


                        table.addCell(header);

                    });
        } else if (purpose == InvoicePurpose.SELL_INVOICE) {
            //Tablo Sütün Başlıkları Girilir
            Stream.of("No", "Mal Hizmet", "Miktar", "Birim Fiyat", "Navlun", "Mal Hizmet Tutarı")
                    .forEach(columnTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setBorderWidth(1);
                        header.setPhrase(new Phrase(columnTitle, catFont));
                        header.setPadding(3);
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);


                        table.addCell(header);

                    });
        } else if (purpose == InvoicePurpose.DOMESTIC_SELL_INVOICE) {
            //Tablo Sütün Başlıkları Girilir
            Stream.of("No", "Mal Hizmet", "Miktar", "Birim Fiyat", "KDV Oranı", "KDV Tutarı", "KDVsiz Tutar", "Mal Hizmet Tutarı")
                    .forEach(columnTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setBorderWidth(1);
                        header.setPhrase(new Phrase(columnTitle, catFont));
                        header.setPadding(3);
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);


                        table.addCell(header);

                    });
        } else {
            //Tablo Sütün Başlıkları Girilir
            Stream.of("No", "Mal Hizmet", "Miktar", "Birim Fiyat", "KDV Oranı", "KDV Tutarı", "Mal Hizmet Tutarı")
                    .forEach(columnTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setBorderWidth(1);
                        header.setPhrase(new Phrase(columnTitle, catFont));
                        header.setPadding(3);
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);


                        table.addCell(header);

                    });
        }

    }

    private void addTableHeaderForServiceInvoice(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);

        //Tablo Sütün Başlıkları Girilir
        Stream.of("No", "Mal Hizmet", "Miktar-Birim", "Birim Fiyat", "KDV Oranı", "KDV Tutarı", "Mal Hizmet Tutarı")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(1);
                    header.setPhrase(new Phrase(columnTitle, catFont));
                    header.setPadding(3);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);

                    table.addCell(header);

                });
    }


    private void addRows(PdfPTable table, String value, int alignmentValue) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 8, Font.NORMAL, BaseColor.BLACK);


        PdfPCell cell1 = new PdfPCell(new Phrase(value, catFont));
        cell1.setHorizontalAlignment(alignmentValue);
        table.addCell(cell1);


    }

    private Double roundDoubleData(Double data) {
        Double totalPrice = ((double) ((int) (data * 1000.0))) / 1000.0;
        return totalPrice;
    }

    private String generateInvoiceNo(Long invoiceId) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String code = "FATURA-" + year;
        int size = invoiceId.toString().length();
        for (int i = 0; i < 7 - size; i++)
            code += "0";
        code += invoiceId;
        return code;
    }

    public List<InvoiceStatus> getInvoiceStatusList() throws Exception {
        List<InvoiceStatus> list = invoiceStatusRepository.findAll();
        return list;
    }

    public SingleInvoiceWithActivityDto findByInvoiceId(Long invoiceId) throws Exception {
        Optional<Invoice> optionalInvoice = invoiceRepository.findById(invoiceId);
        if (!optionalInvoice.isPresent())
            throw new Exception("Fatura Bulunamadı");
        SingleInvoiceWithActivityDto dto = mapper.map(optionalInvoice.get(), SingleInvoiceWithActivityDto.class);
        return dto;
    }

    public SingleCustomerOrderInvoiceDto getCustomerInvoice(Long invoiceId) throws Exception {

        Optional<Invoice> optionalInvoice = invoiceRepository.findById(invoiceId);
        if (!optionalInvoice.isPresent())
            throw new Exception("Fatura Bulunamadı");

        SingleCustomerOrderInvoiceDto dto = mapper.map(optionalInvoice.get(), SingleCustomerOrderInvoiceDto.class);

        return dto;
    }

//    public List<InvoiceComboboxDto> getInvoicesByCheckingCard(InvoiceComboboxCheckingCardDto dto) {
//
//        Optional<CheckingCard> optCheckingCard = checkingCardRepository.findById(dto.checkingCardId);
//        if (!optCheckingCard.isPresent())
//            return null;
//
//        Optional<CheckingCard> optOtherCheckingCard = checkingCardRepository.findById(dto.otherCheckingCardId);
//        if (!optOtherCheckingCard.isPresent())
//            return null;
//
//        List<Invoice> invoiceList = invoiceRepository.getAllInvoices(optOtherCheckingCard.get().getCheckingCardId(),
//                optCheckingCard.get().getCheckingCardId());
//
//        InvoiceComboboxDto[] dtos = mapper.map(invoiceList, InvoiceComboboxDto[].class);
//        List<InvoiceComboboxDto> dtoList = Arrays.asList(dtos);
//
//        return dtoList;
//    }

    public Boolean createDomesticInvoice(String authHeader, DomesticReceiptSaveCategoryDto dto) throws Exception {

        User user = this.controlService.getUserFromToken(authHeader);

        List<DomesticReceiptSaveCategoryContentDto> sendReceiptCategoriesList = dto.getSendReceiptCategoriesList();

        if (sendReceiptCategoriesList.size() > 0) {
            Optional<Refund> optRefund = refundRepository.findById(sendReceiptCategoriesList.get(0).getRefundOrderId());
            if (!optRefund.isPresent()) {
                throw new Exception("Satış Siparişi Bulunamadı !");
            }

            Optional<OtherCompany> optOtherCompany = otherCompanyRepository.findById(optRefund.get().getOtherCompanyId());
            CheckingCard optCheckingCard=optOtherCompany.get().getCheckingCard();

//            Optional<CheckingCard> optCheckingCard = checkingCardRepository.findById(optOtherCompany.get().get);
//            if (!optCheckingCard.isPresent()) {
//                throw new NotFoundException("Cari Kaydı Bulunamadı !");
//            }

            Optional<CheckingCard> optOtherCheckingCard = checkingCardRepository.findBySupplierId(optRefund.get().getSupplier().getSupplierId());
            if (!optOtherCheckingCard.isPresent()) {
                throw new Exception("Cari Kaydı Bulunamadı !");
            }


            List<Depot> depotRefundDrugList = depotRepository.depotRefundDrugList(optRefund.get().getRefundId());
            CustomerSupplyOrder cso = null;
            if (depotRefundDrugList.size() > 0) {
                cso = depotRefundDrugList.get(0).getCustomerSupplyOrder();
            }




            Invoice invoice = new Invoice();
            invoice.setUser(user);
            invoice.setCreatedAt(new Date());
            invoice.setStatus(1);
            invoice.setInvoiceType(invoiceTypeRepository.findById(1l).get());
            invoice.setCheckingCard(optCheckingCard);
            invoice.setOtherCheckingCard(optOtherCheckingCard.get());
            invoice.setInvoicePurpose(InvoicePurpose.DOMESTIC_SELL_INVOICE);
            invoice.setInvoiceStatus(invoiceStatusRepository.findById(10L).get());
            invoice.setTotalChargePrice(0.0);
            invoice.setCurrencyFee(1D);
            invoice.setOtherCompanyId(otherCompanyRepository.findByCheckingCard(invoice.getCheckingCard()).get().getOtherCompanyId());
            invoice.setCurrencyType(CurrencyType.TL);
           invoice.setTotalPriceCurrency(optRefund.get().getTotalPrice() * 1);
//            invoice = invoiceRepository.save(invoice);
            //invoice.setInvoiceNo(this.generateInvoiceNo(invoice.getInvoiceId()));
            invoice.setInvoiceNo(dto.getInvoiceNo());
            invoice.setInvoiceDate(dto.getInvoiceDate());

            List<Category> list120 = categoryRepository.searchTo120CheckingCardId(invoice.getOtherCheckingCard().getCheckingCardId());
            List<Category> list320 = categoryRepository.searchTo320CheckingCardId(invoice.getCheckingCard().getCheckingCardId());

            if (list120.size() != 1 || list320.size() != 1) {
                throw new Exception("120 - 320 Muhasebe Kodları Bulunamadı !");
            }

            invoice.setCategory(list320.get(0));
            invoice.setOtherCategory(list120.get(0));

            invoice = invoiceRepository.save(invoice);





            //price tabloları kayıt ekleme
            RefundPrice refundPrice=new RefundPrice();
            refundPrice.setRefund(optRefund.get());
            refundPrice.setVat(cso.getSupplyOrderPrice().getVat());
            refundPrice.setAccountTotalPrice((optRefund.get().getTotality()*optRefund.get().getUnitPrice())*1.0);
            refundPrice.setVatSum((refundPrice.getAccountTotalPrice()*refundPrice.getVat())*0.01);
            refundPrice=refundPriceRepository.save(refundPrice);








                Double accountTopLiva = ((double) ((int) ((refundPrice.getAccountTotalPrice()+refundPrice.getVatSum()) * 1000.0))) / 1000.0;
                invoice.setTotalPriceLiva(accountTopLiva);
                invoice.setTotalPriceCurrencyLiva(accountTopLiva / invoice.getCurrencyFee());


                Double accountTop = ((double) ((int) ((refundPrice.getAccountTotalPrice()+refundPrice.getVatSum()) * 1000.0))) / 1000.0;
                invoice.setTotalPrice(accountTop);
                invoice.setTotalPriceCurrency(accountTop / invoice.getCurrencyFee());
                invoice = invoiceRepository.save(invoice);



                //-------ACTIVITY-------//

                //firmanın eczaneye fatura etme hareketi
                AccountActivity accountActivity = new AccountActivity();
                accountActivity.setCurrencyFee(invoice.getCurrencyFee());
                accountActivity.setCurrencyType(invoice.getCurrencyType());
                accountActivity.setCheckingCard(invoice.getCheckingCard());
                accountActivity.setOtherCheckingCard(invoice.getOtherCheckingCard());
                accountActivity.setAccountActivityType(AccountActivityType.CUSTOMER_INVOICE);
                accountActivity.setInvoice(invoice);
                accountActivity.setInvoiceNo(invoice.getInvoiceNo());
                accountActivity.setCreatedAt(new Date());
                accountActivity.setCharge(invoice.getTotalPriceCurrency());
                accountActivity.setStatus(1);
                accountActivityRepository.save(accountActivity);

                //eczanenin  fatura edilme hareketi
                AccountActivity accountActivity2 = new AccountActivity();
                accountActivity2.setCurrencyFee(invoice.getCurrencyFee());
                accountActivity2.setCurrencyType(invoice.getCurrencyType());
                accountActivity2.setCheckingCard(invoice.getOtherCheckingCard());
                accountActivity2.setOtherCheckingCard(invoice.getCheckingCard());
                accountActivity2.setAccountActivityType(AccountActivityType.PURCHASE_INVOICE);
                accountActivity2.setInvoice(invoice);
                accountActivity2.setInvoiceNo(invoice.getInvoiceNo());
                accountActivity2.setCreatedAt(new Date());
                accountActivity2.setDebt(invoice.getTotalPriceCurrency());
                accountActivity2.setStatus(1);
                accountActivityRepository.save(accountActivity2);







            RefundReceipt refundReceipt = new RefundReceipt();

            refundReceipt.setCreatedAt(new Date());
            refundReceipt.setInvoice(invoice);
            refundReceipt.setInvoiceDate(dto.getInvoiceDate());
            refundReceipt.setInvoiceNo(dto.getInvoiceNo().trim());
            refundReceipt.setReceiptType(ReceiptType.SATIS);

            List<Refund> refundList = new ArrayList<>();
            refundList.add(optRefund.get());
            refundReceipt.setRefunds(refundList);
            refundReceipt.setSupplier(optRefund.get().getSupplier());
            refundReceipt.setStatus(1);
            //refundReceipt.setReceiptStatus();
            refundReceipt = refundReceiptRepository.save(refundReceipt);

            Refund refund = optRefund.get();
            refund.setRefundReceipt(refundReceipt);
            refundRepository.save(refund);

            RefundReceiptContent refundReceiptContent = new RefundReceiptContent();
            refundReceiptContent.setCreatedAt(new Date());
            refundReceiptContent.setRefundReceipt(refundReceipt);
            refundReceiptContent.setCategory(categoryRepository.findById(sendReceiptCategoriesList.get(0).getCategoryId()).get());
            refundReceiptContent.setDrugCard(drugCardRepository.findById(sendReceiptCategoriesList.get(0).getDrugCardId()).get());
            refundReceiptContent.setStatus(1);
            refundReceiptContent = refundReceiptContentRepository.save(refundReceiptContent);

            List<RefundReceiptContent> refundReceiptContents=new ArrayList<>();
            refundReceiptContents.add(refundReceiptContent);
            refundReceipt.setRefundReceiptContents(refundReceiptContents);
            refundReceipt = refundReceiptRepository.save(refundReceipt);


            //firmalardan keserak gelsin en son eczaneye kesilsin
            Boolean result=createOtherCompanyDomesticInvoice(cso,optRefund.get(),authHeader);

            if(result!=true){
                throw new Exception("Firmalar Arası İade Faturası Kesiminde Hata Oluştu !");
            }



            return true;


        } else {
            throw new Exception("İçerik Listesi Boş !");
        }

    }


    //yurt içi satışı yapılan ilacın firmalar arası aktarım faturaları belirlenip tersine faturaları oluşturuluyor
    public Boolean createOtherCompanyDomesticInvoice(CustomerSupplyOrder cso,Refund refund, String authHeader) throws Exception {
        User user = this.controlService.getUserFromToken(authHeader);


        //eczaneden gelen fatura
        Invoice firstInvoice=cso.getReceipt().getInvoice();



        //eczaneden gelen fatura firmalar arasında kesildiyse sırası ile listeye eklenecek
        List<Invoice> otherInvoiceList=new ArrayList<>();
        Boolean control=true;
        Long invoiceId=firstInvoice.getInvoiceId();




        while(control==true){
            //fatura başka firmaya kesilmiş mi ? kesildiyse listeye ekleniyor
            List<OtherReceipt> otherReceiptList=otherReceiptRepository.getOtherReceiptForPreviousInvoiceId(invoiceId);
            if(otherReceiptList.size()>0){
                //iletilen fatura listeye eklendi
                otherInvoiceList.add(otherReceiptList.get(0).getInvoice());

                //bir sonraki faturanın kontrol edilmesi için id güncellendi
                invoiceId=otherReceiptList.get(0).getInvoice().getInvoiceId();
            }else{
                //son adıma gelince döngü biter
                control=false;
            }
        }


        //listede fatura var ise aldıkları fiyattan satış(iade) faturaları oluşturulur
        for(int i=otherInvoiceList.size()-1;i>=0;i--){
            Invoice oldInvoice=otherInvoiceList.get(i);

            Invoice invoice = new Invoice();
            invoice.setUser(user);
            invoice.setCreatedAt(new Date());
            invoice.setStatus(1);
            invoice.setInvoiceType(invoiceTypeRepository.findById(1l).get());
            invoice.setCheckingCard(oldInvoice.getOtherCheckingCard());
            invoice.setOtherCheckingCard(oldInvoice.getCheckingCard());
            invoice.setInvoicePurpose(InvoicePurpose.DOMESTIC_SELL_INVOICE);
            invoice.setInvoiceStatus(invoiceStatusRepository.findById(10L).get());
            invoice.setTotalChargePrice(0.0);
            invoice.setCurrencyFee(1D);
            invoice.setCurrencyType(CurrencyType.TL);
            invoice.setTotalPriceCurrency(oldInvoice.getTotalPrice() * 1);
            invoice.setOtherCompanyId(otherCompanyRepository.findByCheckingCard(invoice.getCheckingCard()).get().getOtherCompanyId());
            invoice = invoiceRepository.save(invoice);
            //invoice.setInvoiceNo(this.generateInvoiceNo(invoice.getInvoiceId()));
            invoice.setInvoiceNo("IADE-"+invoice.getInvoiceId());
            invoice.setInvoiceDate(new Date());

            List<Category> list120 = categoryRepository.searchTo120CheckingCardId(invoice.getOtherCheckingCard().getCheckingCardId());
            List<Category> list320 = categoryRepository.searchTo320CheckingCardId(invoice.getCheckingCard().getCheckingCardId());

            if (list120.size() != 1 || list320.size() != 1) {
                throw new Exception("120 - 320 Muhasebe Kodları Bulunamadı !");
            }

            invoice.setCategory(list320.get(0));
            invoice.setOtherCategory(list120.get(0));

            invoice = invoiceRepository.save(invoice);


            OtherRefundReceipt otherRefundReceipt=new OtherRefundReceipt();
            otherRefundReceipt.setCreatedAt(new Date());
            otherRefundReceipt.setInvoice(invoice);
            otherRefundReceipt.setInvoiceNo(invoice.getInvoiceNo());
            otherRefundReceipt.setInvoiceDate(invoice.getInvoiceDate());
            otherRefundReceipt.setReceiptStatus(receiptStatusRepository.findById(40L).get());
            otherRefundReceipt.setReceiptType(ReceiptType.SATIS);
            otherRefundReceipt.setStatus(1);
            otherRefundReceipt=otherRefundReceiptRepository.save(otherRefundReceipt);
            otherRefundReceipt.setOtherRefundReceiptNo("OthRefRec"+otherRefundReceipt.getOtherRefundReceiptId());



            OtherRefundPrice otherRefundPrice=new OtherRefundPrice();
            otherRefundPrice.setCreatedAt(new Date());
            otherRefundPrice.setStatus(1);
            otherRefundPrice.setCategory(refund.getRefundReceipt().getRefundReceiptContents().get(0).getCategory());
            otherRefundPrice.setDrugCard(refund.getDrugCard());
otherRefundPrice.setRefund(refund);
otherRefundPrice.setDrugCard(refund.getRefundReceipt().getRefundReceiptContents().get(0).getDrugCard());
otherRefundPrice.setOtherRefundReceipt(otherRefundReceipt);

            for (OtherReceiptPrice othRecPrice:oldInvoice.getOtherReceipt().getOtherReceiptPrices()) {
                if(refund.getDrugCard().getDrugCardId()==othRecPrice.getDrugCard().getDrugCardId()){
                    otherRefundPrice.setVat(othRecPrice.getVat());
Double unitPrice=(othRecPrice.getAccountTotalPrice()/othRecPrice.getSupplyOrder().getTotality())*1.0;
otherRefundPrice.setAccountTotalPrice(unitPrice*refund.getTotality());
otherRefundPrice.setVatSum((otherRefundPrice.getAccountTotalPrice()*otherRefundPrice.getVat())*0.01);
break;
                }
            }

            otherRefundPrice=otherRefundPriceRepository.save(otherRefundPrice);

            List<OtherRefundPrice> list=new ArrayList<>();
            list.add(otherRefundPrice);
            otherRefundReceipt.setOtherRefundPrices(list);
            otherRefundReceipt.setInvoice(invoice);
            otherRefundReceipt=otherRefundReceiptRepository.save(otherRefundReceipt);





          /*  //price tabloları kayıt ekleme
            RefundPrice refundPrice=new RefundPrice();
           // refundPrice.setRefund(refund);
            refundPrice.setVat(cso.getSupplyOrderPrice().getVat());
            refundPrice.setVatSum((refund.getTotality()*refundPrice.getVat())*0.01);
            refundPrice.setAccountTotalPrice((refund.getTotality()*refund.getUnitPrice())*1.0);
           // refundPrice=refundPriceRepository.save(refundPrice);


*/





            Double accountTopLiva = ((double) ((int) ((otherRefundPrice.getAccountTotalPrice()+otherRefundPrice.getVatSum()) * 1000.0))) / 1000.0;
            invoice.setTotalPriceLiva(accountTopLiva);
            invoice.setTotalPriceCurrencyLiva(accountTopLiva / invoice.getCurrencyFee());


            Double accountTop = ((double) ((int) ((otherRefundPrice.getAccountTotalPrice()+otherRefundPrice.getVatSum()) * 1000.0))) / 1000.0;
            invoice.setTotalPrice(accountTop);
            invoice.setTotalPriceCurrency(accountTop / invoice.getCurrencyFee());
            invoice = invoiceRepository.save(invoice);



            //-------ACTIVITY-------//

            //fatura etme hareketi
            AccountActivity accountActivity = new AccountActivity();
            accountActivity.setCurrencyFee(invoice.getCurrencyFee());
            accountActivity.setCurrencyType(invoice.getCurrencyType());
            accountActivity.setCheckingCard(invoice.getCheckingCard());
            accountActivity.setOtherCheckingCard(invoice.getOtherCheckingCard());
            accountActivity.setAccountActivityType(AccountActivityType.CUSTOMER_INVOICE);
            accountActivity.setInvoice(invoice);
            accountActivity.setInvoiceNo(invoice.getInvoiceNo());
            accountActivity.setCreatedAt(new Date());
            accountActivity.setCharge(invoice.getTotalPriceCurrency());
            accountActivity.setStatus(1);
            accountActivityRepository.save(accountActivity);

            //fatura edilme hareketi
            AccountActivity accountActivity2 = new AccountActivity();
            accountActivity2.setCurrencyFee(invoice.getCurrencyFee());
            accountActivity2.setCurrencyType(invoice.getCurrencyType());
            accountActivity2.setCheckingCard(invoice.getOtherCheckingCard());
            accountActivity2.setOtherCheckingCard(invoice.getCheckingCard());
            accountActivity2.setAccountActivityType(AccountActivityType.PURCHASE_INVOICE);
            accountActivity2.setInvoice(invoice);
            accountActivity2.setInvoiceNo(invoice.getInvoiceNo());
            accountActivity2.setCreatedAt(new Date());
            accountActivity2.setDebt(invoice.getTotalPriceCurrency());
            accountActivity2.setStatus(1);
            accountActivityRepository.save(accountActivity2);







           /*RefundReceipt refundReceipt = new RefundReceipt();

            refundReceipt.setCreatedAt(new Date());
            refundReceipt.setInvoice(invoice);
            refundReceipt.setInvoiceDate(invoice.getInvoiceDate());
            refundReceipt.setInvoiceNo(invoice.getInvoiceNo().trim());
            refundReceipt.setReceiptType(ReceiptType.SATIS);

            List<Refund> refundList = new ArrayList<>();
            refundList.add(refund);
            refundReceipt.setRefunds(refundList);
            refundReceipt.setSupplier(refund.getSupplier());
            refundReceipt.setStatus(1);
            //refundReceipt.setReceiptStatus();
            refundReceipt = refundReceiptRepository.save(refundReceipt);


            refund.setRefundReceipt(refundReceipt);
            refundRepository.save(refund);

            RefundReceiptContent refundReceiptContent = new RefundReceiptContent();
            refundReceiptContent.setCreatedAt(new Date());
            refundReceiptContent.setRefundReceipt(refundReceipt);
            refundReceiptContent.setCategory(refund.getRefundReceipt().getRefundReceiptContents().get(0).getCategory());
            refundReceiptContent.setDrugCard(refund.getRefundReceipt().getRefundReceiptContents().get(0).getDrugCard());
            refundReceiptContent.setStatus(1);
            refundReceiptContent = refundReceiptContentRepository.save(refundReceiptContent);


            List<RefundReceiptContent> refundReceiptContents=new ArrayList<>();
            refundReceiptContents.add(refundReceiptContent);
            refundReceipt.setRefundReceiptContents(refundReceiptContents);
            refundReceipt = refundReceiptRepository.save(refundReceipt);
*/
            invoice=invoiceRepository.save(invoice);


        }
        return true;
    }

    //Var Olan Ticari Alış Faturasını Başka Cariye Kesme
    public String createNewOtherInvoice(@Valid CreateOtherInvoiceDto dto, String authHeader) throws Exception {
        User user = this.controlService.getUserFromToken(authHeader);

        Optional<ReceiptStatus> receiptStatus = receiptStatusRepository.findById(40l);
        if (!receiptStatus.isPresent()) {
            throw new NotFoundException("Statü Bulunamadı");
        }


        Optional<Invoice> optPreviousInvoice = invoiceRepository.findById(dto.getInvoiceId());
        if (!optPreviousInvoice.isPresent()) {
            throw new NotFoundException("Önceki Fatura Bilgisi Bulunamadı !");
        }

        Optional<CheckingCard> optCheckingCard = checkingCardRepository.findById(dto.getCheckingCardId());
        if (!optCheckingCard.isPresent()) {
            throw new NotFoundException("Cari Kart Bilgisi Bulunamadı !");
        }

        Optional<CheckingCard> optOtherCheckingCard = checkingCardRepository.findById(dto.getOtherCheckingCardId());
        if (!optOtherCheckingCard.isPresent()) {
            throw new NotFoundException("Diğer Cari Kart Bilgisi Bulunamadı !");
        }

//fatura edenin other company id'si set ediliyor
        Optional<OtherCompany> optOtherCompany=otherCompanyRepository.findByCheckingCard(optOtherCheckingCard.get());
        if(!optOtherCompany.isPresent() && optOtherCompany.get().getCheckingCard()!=null){
            throw new NotFoundException("Cari Bilgisi Bulunamadı !");
        }

        Optional<InvoiceType> invoiceTypeOpt = invoiceTypeRepository.findById(1l);
        if (!invoiceTypeOpt.isPresent()) {
            throw new NotFoundException("Fatura Tipi Bulunamadı");
        }






        /* Fatura Oluşturma Başladı */
        Invoice invoice = new Invoice();
        invoice.setUser(user);
        invoice.setCreatedAt(new Date());
        invoice.setStatus(1);
        invoice.setInvoiceStatus(invoiceStatusRepository.findById(10L).get());
        invoice.setCurrencyType(CurrencyType.TL);
        invoice.setCurrencyFee(1.0);
        invoice.setTotalChargePrice(0.0);
        invoice.setCheckingCard(optCheckingCard.get());
        invoice.setOtherCheckingCard(optOtherCheckingCard.get());

//hangi firma için oluşturuldu onun bilgisi eklendi

            invoice.setOtherCompanyId(optOtherCompany.get().getOtherCompanyId());



        List<Category> list120 = categoryRepository.searchTo120CheckingCardId(invoice.getOtherCheckingCard().getCheckingCardId());
        List<Category> list320 = categoryRepository.searchTo320CheckingCardId(invoice.getCheckingCard().getCheckingCardId());

        if (list120.size() != 1 || list320.size() != 1) {
            throw new Exception("120 - 320 Muhasebe Kodları Bulunamadı !");
        }


        invoice.setCategory(list120.get(0));
        invoice.setOtherCategory(list320.get(0));

        invoice = invoiceRepository.save(invoice);


        //otherReceipt oluşturulur
        OtherReceipt otherReceipt=new OtherReceipt();
        otherReceipt.setCreatedAt(new Date());
        otherReceipt.setInvoiceDate(dto.getInvoiceDate());
        otherReceipt.setInvoiceNo(dto.getInvoiceNo().trim());
        otherReceipt=otherReceiptRepository.save(otherReceipt);
        otherReceipt.setOtherReceiptNo("OtherReceipt"+otherReceipt.getOtherReceiptId());
        otherReceipt.setPreviousInvoice(optPreviousInvoice.get());
        otherReceipt.setInvoice(invoice);
        otherReceipt.setReceiptStatus(receiptStatus.get());
        otherReceipt.setStatus(1);
        otherReceipt.setReceiptType(ReceiptType.ALIS);
        otherReceipt=otherReceiptRepository.save(otherReceipt);


        invoice.setInvoiceNo(dto.getInvoiceNo().trim());
        invoice.setInvoiceDate(dto.getInvoiceDate());
        //invoice.setReceipt(null);
        invoice.setOtherReceipt(otherReceipt);
        if (optCheckingCard.get().getTaxIdentificationNumber() != null && optCheckingCard.get().getTaxIdentificationNumber().toString().trim().length() > 0) {
            invoice.setTaxNo(optCheckingCard.get().getTaxIdentificationNumber().toString());
        }
        if (optCheckingCard.get().getTaxOffice() != null && optCheckingCard.get().getTaxOffice().trim().length() > 0) {
            invoice.setTaxOffice(optCheckingCard.get().getTaxOffice());
        }
        invoice.setCrsNo(optCheckingCard.get().getCrsNo());
        //invoice.setInvoiceStatus(optionalInvoiceStatus.get());
        invoice.setInvoiceType(invoiceTypeOpt.get());
        Double accountTotalPrice = 0.0, accountTotalPriceLiva = 0.0, totalPrice = 0.0, chargeLiva = 0.0;
        int index=-1;

            invoice.setInvoicePurpose(InvoicePurpose.BUY_INVOICE);
//            invoice.setCheckingCard(checkingCardOptional.get());
//            invoice.setOtherCheckingCard(checkingCardOpt.get());
        if(optPreviousInvoice.get().getOtherReceipt()!=null){
            for (OtherReceiptPrice othReceiptPrice : optPreviousInvoice.get().getOtherReceipt().getOtherReceiptPrices()) {
                index++;
                Double newUnitPrice = 0D;

                Double oldUnitPrice=(othReceiptPrice.getAccountTotalPrice()/(othReceiptPrice.getSupplyOrder().getTotality()*1.0));

                //---  price kayıtları ekleniyor ---
                newUnitPrice = Double.valueOf(oldUnitPrice + ((oldUnitPrice * dto.getProfit()) / 100.0));

                Double vat = othReceiptPrice.getVat();
                Double vatSum = othReceiptPrice.getSupplyOrder().getTotality() * (newUnitPrice * (vat * 0.01));
                //accountTotalPrice KDV SİZ HALİ
                Double newAccountTotalPrice = (othReceiptPrice.getSupplyOrder().getTotality()) * newUnitPrice;

                OtherReceiptPrice otherReceiptPrice = new OtherReceiptPrice();
                otherReceiptPrice.setVat(vat);
                otherReceiptPrice.setVatSum(vatSum);
                otherReceiptPrice.setAccountTotalPrice(newAccountTotalPrice);
                otherReceiptPrice.setSupplyOrder(othReceiptPrice.getSupplyOrder());
                otherReceiptPrice.setCreatedAt(new Date());
                otherReceiptPrice.setStatus(1);
                otherReceiptPrice.setProfit(dto.getProfit());
                otherReceiptPrice.setCategory(othReceiptPrice.getCategory());
                otherReceiptPrice.setDrugCard(othReceiptPrice.getDrugCard());
                otherReceiptPrice.setOtherReceipt(otherReceipt);
                otherReceiptPrice = otherReceiptPriceRepository.save(otherReceiptPrice);



                //toplam fatura tutarı kdv dahil
                accountTotalPrice = accountTotalPrice + (newAccountTotalPrice + vatSum);

                Double accountTopLiva = ((double) ((int) (accountTotalPrice * 1000.0))) / 1000.0;
                invoice.setTotalPriceLiva(accountTopLiva);
                invoice.setTotalPriceCurrencyLiva(accountTopLiva / invoice.getCurrencyFee());


                Double accountTop = ((double) ((int) (accountTotalPrice * 1000.0))) / 1000.0;
                invoice.setTotalPrice(accountTop);
                invoice.setTotalPriceCurrency(accountTop / invoice.getCurrencyFee());




            }

        }else{
            for (CustomerSupplyOrder cso : optPreviousInvoice.get().getReceipt().getCustomerSupplyOrders()) {
                index++;
                Double newUnitPrice = 0D;

                Double oldUnitPrice=(cso.getSupplyOrderPrice().getAccountTotalPrice()/(cso.getTotality()*1.0));

                //---  price kayıtları ekleniyor ---
                newUnitPrice = Double.valueOf(oldUnitPrice + ((oldUnitPrice * dto.getProfit()) / 100.0));

                Double vat = cso.getSupplyOrderPrice().getVat();
                Double vatSum = cso.getTotality() * (newUnitPrice * (vat * 0.01));
                //accountTotalPrice KDV SİZ HALİ
                Double newAccountTotalPrice = cso.getTotality() * newUnitPrice;

                OtherReceiptPrice otherReceiptPrice = new OtherReceiptPrice();
                otherReceiptPrice.setVat(vat);
                otherReceiptPrice.setVatSum(vatSum);
                otherReceiptPrice.setAccountTotalPrice(newAccountTotalPrice);
                otherReceiptPrice.setSupplyOrder(cso);
                otherReceiptPrice.setCreatedAt(new Date());
                otherReceiptPrice.setStatus(1);
                otherReceiptPrice.setProfit(dto.getProfit());
                otherReceiptPrice.setCategory(cso.getReceipt().getReceiptContents().get(index).getCategory());
                otherReceiptPrice.setDrugCard(cso.getDrugCard());
                otherReceiptPrice.setOtherReceipt(otherReceipt);
                otherReceiptPrice = otherReceiptPriceRepository.save(otherReceiptPrice);



                //toplam fatura tutarı kdv dahil
                accountTotalPrice = accountTotalPrice + (newAccountTotalPrice + vatSum);

                Double accountTopLiva = ((double) ((int) (accountTotalPrice * 1000.0))) / 1000.0;
                invoice.setTotalPriceLiva(accountTopLiva);
                invoice.setTotalPriceCurrencyLiva(accountTopLiva / invoice.getCurrencyFee());


                Double accountTop = ((double) ((int) (accountTotalPrice * 1000.0))) / 1000.0;
                invoice.setTotalPrice(accountTop);
                invoice.setTotalPriceCurrency(accountTop / invoice.getCurrencyFee());




            }

        }
//
            invoice = invoiceRepository.save(invoice);
            /* Fatura Oluşturma Bitti */

            if (invoice.getInvoicePurpose() == InvoicePurpose.BUY_INVOICE) {
//            if (controlSupplierType) {
//                //eczanenin livaya fatura etme hareketi
//                accountActivityService.createAccountActivity(invoice, invoice.getCheckingCard(), checkingCardRepository.findById(2l).get(), AccountActivityType.CUSTOMER_INVOICE, invoice.getTotalPriceCurrencyLiva());
//
//                //livaya eczaneden fatura edilme hareketi
//                accountActivityService.createAccountActivity(invoice, checkingCardRepository.findById(2l).get(), invoice.getCheckingCard(), AccountActivityType.PURCHASE_INVOICE, invoice.getTotalPriceCurrencyLiva());
//
//                //livadan ekibe fatura etme hareketi
//                accountActivityService.createAccountActivity(invoice, checkingCardRepository.findById(2l).get(), checkingCardRepository.findById(1l).get(), AccountActivityType.CUSTOMER_INVOICE, invoice.getTotalPriceCurrency());
//
//                //ekibe livadan fatura edilme hareketi
//                accountActivityService.createAccountActivity(invoice, checkingCardRepository.findById(1l).get(), checkingCardRepository.findById(2l).get(), AccountActivityType.PURCHASE_INVOICE, invoice.getTotalPriceCurrency());
//            } else {
                //eczanenin bize fatura etme hareketi
                accountActivityService.createAccountActivity(invoice, invoice.getCheckingCard(), invoice.getOtherCheckingCard(), AccountActivityType.CUSTOMER_INVOICE, invoice.getTotalPriceCurrencyLiva());

                //bizim eczaneden fatura edilme hareketimiz
                accountActivityService.createAccountActivity(invoice, invoice.getOtherCheckingCard(), invoice.getCheckingCard(), AccountActivityType.PURCHASE_INVOICE, invoice.getTotalPriceCurrencyLiva());
            }

        otherReceipt=otherReceiptRepository.save(otherReceipt);
            invoice=invoiceRepository.save(invoice);


        return invoice.getInvoiceNo();

    }
}
