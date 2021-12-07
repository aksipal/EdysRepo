package com.via.ecza.service;

import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.AccountActivityType;
import com.via.ecza.entity.enumClass.AccountType;
import com.via.ecza.entity.enumClass.CheckingCardType;
import com.via.ecza.repo.*;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class AccountActivityService {
    @Autowired
    private AccountActivityRepository accountActivityRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ControlService controlService;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CheckingCardRepository checkingCardRepository;
    @Autowired
    private InvoiceStatusRepository invoiceStatusRepository;

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public Page<CheckingCardAccountActivityDto> checkingCardActivities(Pageable page, CheckinCardActivitySearchDto dto) throws Exception {
        Boolean control = false;
        if (dto.getCheckingCardId() == null)
            throw new NotFoundException("Cari kart bulunamadı.");
        Optional<CheckingCard> optOther;
        StringBuilder createSqlQuery;
        if (dto.getOtherCheckingCardId() != null) {
            optOther = checkingCardRepository.findById(dto.getOtherCheckingCardId());
            if (optOther.isPresent()) {
                if (dto.getCheckingCardId() == 1 && optOther.get().getType() == CheckingCardType.SUPPLIER) {
                    control = true;
                }
            }
        }
        //  checking card ekip ecza ve other checking card supplier (Eczane) ise
        if (control == true) {
            createSqlQuery = new StringBuilder("select aa.* from account_activity aa ");
            createSqlQuery.append("join checking_card cc on cc.checking_card_id = aa.checking_card_id ");
            // checking_card_id= 2  Liva
            createSqlQuery.append("where aa.status = 1 ");

            if (dto.getStartDate() != null)
                createSqlQuery.append(" and aa.created_at  >= to_date('" + sdf.format(dto.getStartDate()) + "'," + "'dd.MM.yyyy') ");
            if (dto.getEndDate() != null)
                createSqlQuery.append(" and aa.created_at  < to_date('" + sdf.format(dto.getEndDate()) + "'," + "'dd.MM.yyyy') ");
            if (dto.getOtherCheckingCardId() != null)
                createSqlQuery.append(" and aa.other_checking_card_id = " + dto.getOtherCheckingCardId());
            if (dto.getInvoiceNo() != null)
                if (dto.getInvoiceNo().trim().length() > 0)
                    createSqlQuery.append(" and aa.invoice_no = '" + dto.getInvoiceNo().trim() + "'");

            createSqlQuery.append(" order by aa.account_activity_id");
        } else {
            createSqlQuery = new StringBuilder("select aa.* from account_activity aa ");
            createSqlQuery.append("join checking_card cc on cc.checking_card_id = aa.checking_card_id  ");
            createSqlQuery.append("where aa.status = 1  and aa.checking_card_id =" + dto.getCheckingCardId() + " ");

            if (dto.getStartDate() != null)
                createSqlQuery.append(" and aa.created_at  >= to_date('" + sdf.format(dto.getStartDate()) + "'," + "'dd.MM.yyyy') ");
            if (dto.getEndDate() != null)
                createSqlQuery.append(" and aa.created_at  < to_date('" + sdf.format(dto.getEndDate()) + "'," + "'dd.MM.yyyy') ");
            if (dto.getOtherCheckingCardId() != null)
                createSqlQuery.append(" and aa.other_checking_card_id = " + dto.getOtherCheckingCardId());
            if (dto.getInvoiceNo() != null)
                if (dto.getInvoiceNo().trim().length() > 0)
                    createSqlQuery.append(" and aa.invoice_no = '" + dto.getInvoiceNo().trim() + "'");

            createSqlQuery.append(" order by aa.account_activity_id");
        }

        List<AccountActivity> list = entityManager.createNativeQuery(createSqlQuery.toString(), AccountActivity.class).getResultList();
        CheckingCardAccountActivityDto[] dtos = mapper.map(list, CheckingCardAccountActivityDto[].class);
        List<CheckingCardAccountActivityDto> dtosList = Arrays.asList(dtos);

        int start = Math.min((int) page.getOffset(), dtosList.size());
        int end = Math.min((start + page.getPageSize()), dtosList.size());

        Page<CheckingCardAccountActivityDto> pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());

        return pageList;
    }

    public InvoiceIncomingDto getIncomingInvoiceDetail(Long invoiceId) throws Exception {

        Optional<Invoice> optionalInvoice = invoiceRepository.findById(invoiceId);
        if (!optionalInvoice.isPresent())
            throw new NotFoundException("Fatura Bulunamadı");

        Invoice invoice = optionalInvoice.get();
        InvoiceIncomingDto dto = mapper.map(invoice, InvoiceIncomingDto.class);

        return dto;
    }

    public Boolean save(AccountActivitySaveDto dto) throws Exception {

        Optional<CheckingCard> optionalCheckingCard = checkingCardRepository.findById(dto.getCheckingCardId());
        if (!optionalCheckingCard.isPresent()) {
            throw new Exception("Ödeme Yapacak Cari Bulunamadı");
        }
        Optional<CheckingCard> optionalOtherCheckingCard = checkingCardRepository.findById(dto.getOtherCheckingCardId());
        if (!optionalOtherCheckingCard.isPresent()) {
            throw new Exception("Ödeme Yapılacak Cari Bulunamadı");
        }
        Optional<CheckingCard> optionalLiva = checkingCardRepository.findById(2l);
        if (!optionalLiva.isPresent()) {
            throw new Exception("Liva Carisi Bulunamadı");
        }
        Optional<Account> optionalAccount = null;
        Optional<Account> optionalOtherAccount = null;

        optionalAccount = accountRepository.findById(dto.getAccountId());
        if (!optionalAccount.isPresent()) {
            throw new Exception("Ödeme Yapacak Hesap Bulunamadı");
        }

        optionalOtherAccount = accountRepository.findById(dto.getOtherAccountId());
        if (!optionalOtherAccount.isPresent()) {
            throw new Exception("Ödeme Yapılacak Hesap Bulunamadı");
        }

//            Optional<Invoice> optionalInvoice = invoiceRepository.findByInvoiceNo(dto.getInvoiceNo());
//
//            if (!optionalInvoice.isPresent()) {
//                throw new Exception("Fatura Bulunamadı");
//            }
//            Invoice invoice = optionalInvoice.get();
//            if(invoice.getTotalPrice() == invoice.getTotalChargePrice())
//                throw new Exception("Fatura Ödendi! Bu Fatura İçin Daha Fazla Ödeme Yapılamaz");

        CheckingCard checkingCard = optionalCheckingCard.get();
        CheckingCard otherCheckingCard = optionalOtherCheckingCard.get();

        //cariler arasındaki hizmet veya ticari alış fatura listesi
        //fatura kesilen ödemede caridir,fatura kesen ödemede karşı caridir
       // List<Invoice> invoiceList = invoiceRepository.getAllInvoicesToCheckingCardOrderById(otherCheckingCard.getCheckingCardId(), checkingCard.getCheckingCardId(), InvoicePurpose.BUY_INVOICE.toString());


//        // Ödeme yapan Ekip ecza ise
//        if (checkingCard.getCheckingCardId() == 1) {
//            // Karşı taraf eczane ise
//            if (otherCheckingCard.getType() == CheckingCardType.SUPPLIER) {
//
//                if (invoiceList.size() > 0) {
//
//                            Double sumOfCharge=accountActivityRepository.sumOfChargeToCheckingCards(checkingCard.getCheckingCardId(),optionalLiva.get().getCheckingCardId());
//                            Double sumOfDebt=accountActivityRepository.sumOfDebtToCheckingCards(checkingCard.getCheckingCardId(),optionalLiva.get().getCheckingCardId());
//                            //cariye ait net para miktarı avans vs varsa dahil
//                            Double netBalance=0.0;
//                            //eğer önceden ödenmiş avans var ise eldeki bakiyeye eklenir fatura kapatmak için kullanılır
//                            if(sumOfCharge!=null && sumOfDebt!=null && sumOfCharge-sumOfDebt>0){
//                                netBalance+=(sumOfCharge-sumOfDebt);
//                                netBalance+=dto.getPrice();
//                            }else{
//                                if(sumOfCharge==null){
//                                    sumOfCharge=0.0;
//                                }
//                                if(sumOfDebt==null){
//                                    sumOfDebt=0.0;
//                                }
//
//                                netBalance+=dto.getPrice();
//                            }
//
//
//                            //ekipten livaya
//                            //ödeme yapan tarafa hareket ekleme
//                            addActivity(dto, checkingCard, otherCheckingCard, optionalAccount.get(), null);
//
//                            //ödeme alan tarafa hareket ekleme
//                            addOtherActivity(dto, otherCheckingCard, checkingCard, null, optionalAccount.get());
//
//
//                    Double dtoPrice=dto.getPrice();
//
//                    for (Invoice invoice : invoiceList) {
//                        //eğer arada liva vaer ise
//                        if (invoice.getChargeLiva() != null) {
//                            //önceden faturatutarından az ödeme varsa veya avans kaldıysa
//                            if ((sumOfCharge+dtoPrice>sumOfDebt) || (netBalance >= invoice.getChargeLiva() && netBalance>=invoice.getTotalPrice())) {
//                                dto.setPrice(invoice.getChargeLiva());
//
//                                //livadan eczaneye
//                                //ödeme yapan tarafa hareket ekleme
//                                addActivity(dto, checkingCard, otherCheckingCard, null, optionalOtherAccount.get());
//
//                                //ödeme alan tarafa hareket ekleme
//                                addOtherActivity(dto, otherCheckingCard, checkingCard, optionalOtherAccount.get(), null);
//
//                                if ((sumOfCharge+dtoPrice>sumOfDebt) || (netBalance >= invoice.getTotalPrice())) {
//                                    invoice.setInvoiceStatus(invoiceStatusRepository.findById(40L).get());
//                                    invoiceRepository.save(invoice);
//                                    //kapatılan fatura tutarı net bakiyeden düşülür ,
//                                    //eğer bir sonraki faturayı kaoatmaya yeterse o da kapatılır, yetmezse avans olarak kalır
//                                    netBalance=netBalance-invoice.getTotalPrice();
//                                }
//                            }
//
//
//
//                        }else{
//                            throw new Exception("Muhasebe Hareketi Eklenirken Hata Oluştu !");
//                        }
//
//
//                    }
//
//                } else {
////                    //ekipten livaya
////                    //ödeme yapan tarafa hareket ekleme
////                    addActivity(dto, checkingCard, otherCheckingCard, optionalAccount.get(), null);
////
////                    //ödeme alan tarafa hareket ekleme
////                    addOtherActivity(dto, otherCheckingCard, checkingCard, null, optionalAccount.get());
//
////                    //livadan eczaneye
////                    //ödeme yapan tarafa hareket ekleme
////                    addActivity(dto, optionalLiva.get(), otherCheckingCard, null, optionalOtherAccount.get());
////
////                    //ödeme alan tarafa hareket ekleme
////                    addOtherActivity(dto, otherCheckingCard, optionalLiva.get(), optionalOtherAccount.get(), null);
//
//                }
//
//
//            }

            // Karşı taraf müşteri ya da diğer ise
//            if (!(otherCheckingCard.getType() == CheckingCardType.SUPPLIER)) {
                //ödeme yapan tarafa hareket ekleme
                addActivity(dto, checkingCard, otherCheckingCard, optionalAccount.get(), optionalOtherAccount.get());

                //ödeme alan tarafa hareket ekleme
                addOtherActivity(dto, otherCheckingCard, checkingCard, optionalOtherAccount.get(), optionalAccount.get());

//                    setInvoiceTotalChargePrice(invoice, dto);
            //}
        //}

        // Ödeme yapan müşteri ise
//        else if (checkingCard.getType() == CheckingCardType.CUSTOMER || checkingCard.getType() == CheckingCardType.OTHER) {
//            //ödeme yapan tarafa hareket ekleme
//            addActivity(dto, checkingCard, otherCheckingCard, optionalAccount.get(), optionalOtherAccount.get());
//
//            //ödeme alan tarafa hareket ekleme
//            addOtherActivity(dto, otherCheckingCard, checkingCard, optionalOtherAccount.get(), optionalAccount.get());
//
////                setInvoiceTotalChargePrice(invoice, dto);
//        }

//        // Ödeme yapan eczane ise
//        else if (checkingCard.getType() == CheckingCardType.SUPPLIER) {
//            //eczaneden livaya
//            //ödeme yapan tarafa hareket ekleme
//            addActivity(dto, checkingCard, optionalLiva.get(), optionalAccount.get(), null);
//
//            //ödeme alan tarafa hareket ekleme
//            addOtherActivity(dto, optionalLiva.get(), checkingCard, null, optionalAccount.get());
//
//            //livadan eczaneye
//            //ödeme yapan tarafa hareket ekleme
//            addActivity(dto, optionalLiva.get(), otherCheckingCard, null, optionalOtherAccount.get());
//
//            //ödeme alan tarafa hareket ekleme
//            addOtherActivity(dto, otherCheckingCard, optionalLiva.get(), optionalOtherAccount.get(), null);
//
////                setInvoiceTotalChargePrice(invoice, dto);
//        } else {
//            return false;
//        }
        return true;

    }

    public void setInvoiceTotalChargePrice(Invoice invoice, AccountActivitySaveDto dto) {
        Double chargePrice;
        if (invoice.getTotalChargePrice() == null)
            chargePrice = 0D + dto.getPrice();
        else {
            chargePrice = invoice.getTotalChargePrice() + dto.getPrice();
        }
        invoice.setTotalChargePrice(chargePrice);
        invoice = invoiceRepository.save(invoice);
    }

    public void addActivity(AccountActivitySaveDto dto, CheckingCard checkingCard, CheckingCard otherCheckingCard,
                            Account account, Account otherAccount) {
        AccountActivity accountActivity = new AccountActivity();
        accountActivity.setCurrencyFee(dto.getCurrencyFee());
        accountActivity.setCurrencyType(CurrencyType.valueOf(dto.getCurrencyType()));
        accountActivity.setAccount(account);
        accountActivity.setOtherAccount(otherAccount);
        accountActivity.setCheckingCard(checkingCard);
        accountActivity.setOtherCheckingCard(otherCheckingCard);
        accountActivity.setAccountActivityType(AccountActivityType.PURCHASE_PAYMENT);
        accountActivity.setCreatedAt(new Date());
        accountActivity.setCharge(dto.getPrice());
        //accountActivity.setInvoice(invoice);
//        accountActivity.setInvoiceNo(dto.getInvoiceNo());
        accountActivity.setStatus(1);
        if (dto.getOtherAccountType() == AccountType.CHEQUE || dto.getOtherAccountType() == AccountType.CHEQUE) {
            accountActivity.setDocumentCreatedDate(dto.getDocumentCreatedDate());
            accountActivity.setPaidDay(dto.getPayDay());
            accountActivity.setDateOfIssue(dto.getDateOfIssue());
            accountActivity.setBondPayerIdentityNumber(dto.getBondPayerIdentityNumber());
        }
        accountActivityRepository.save(accountActivity);
    }

    public void addOtherActivity(AccountActivitySaveDto dto, CheckingCard checkingCard, CheckingCard otherCheckingCard,
                                 Account account, Account otherAccount) {
        AccountActivity accountActivity = new AccountActivity();
        accountActivity.setCurrencyFee(dto.getCurrencyFee());
        accountActivity.setCurrencyType(CurrencyType.valueOf(dto.getCurrencyType()));
        accountActivity.setAccount(account);
        accountActivity.setOtherAccount(otherAccount);
        accountActivity.setOtherCheckingCard(otherCheckingCard);
        accountActivity.setCheckingCard(checkingCard);
        accountActivity.setAccountActivityType(AccountActivityType.CUSTOMER_PAYMENT);
        accountActivity.setCreatedAt(new Date());
        accountActivity.setDebt(dto.getPrice());
//        accountActivity.setInvoiceNo(dto.getInvoiceNo());
        //accountActivity.setInvoice(invoice);
        accountActivity.setStatus(1);
        if (dto.getOtherAccountType() == AccountType.CHEQUE || dto.getOtherAccountType() == AccountType.CHEQUE) {
            accountActivity.setDocumentCreatedDate(dto.getDocumentCreatedDate());
            accountActivity.setPaidDay(dto.getPayDay());
            accountActivity.setDateOfIssue(dto.getDateOfIssue());
            accountActivity.setBondPayerIdentityNumber(dto.getBondPayerIdentityNumber());
        }
        accountActivityRepository.save(accountActivity);
    }

    public Page<AccountActivitiesDto> searchActivities(AccountActivitiesSearchDto dto, Pageable page) {


        StringBuilder createSqlQuery = new StringBuilder("select aa.* from account_activity aa ");
        createSqlQuery.append("join checking_card cc on cc.checking_card_id = aa.checking_card_id  ");
        createSqlQuery.append("where aa.status = 1 ");

        if (dto.getStartDate() != null)
            createSqlQuery.append(" and aa.created_at  >= to_date('" + sdf.format(dto.getStartDate()) + "'," + "'dd.MM.yyyy') ");
        if (dto.getEndDate() != null)
            createSqlQuery.append(" and aa.created_at  < to_date('" + sdf.format(dto.getEndDate()) + "'," + "'dd.MM.yyyy') ");
        if (dto.getInvoiceNo() != null)
            if (dto.getInvoiceNo().trim().length() > 0)
                createSqlQuery.append(" and aa.invoice_no = '" + dto.getInvoiceNo().trim() + "'");

        createSqlQuery.append(" order by aa.account_activity_id");

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), AccountActivity.class).getResultList();
        AccountActivitiesDto[] dtos = mapper.map(list, AccountActivitiesDto[].class);
        List<AccountActivitiesDto> dtosList = Arrays.asList(dtos);

        int start = Math.min((int) page.getOffset(), dtosList.size());
        int end = Math.min((start + page.getPageSize()), dtosList.size());

        Page<AccountActivitiesDto> pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());
        return pageList;


//        String no = "";
//        if(dto.getInvoiceNo() != null)
//            no= dto.getInvoiceNo().trim();
//        Optional<Invoice> invoice = invoiceRepository.findByInvoiceNo(no);
//        if (!invoice.isPresent() && dto.getInvoiceNo() != null)
//        {
//            StringBuilder createSqlQuery = new StringBuilder("select * from account_activity aa  ");
//            createSqlQuery.append("join invoice i on i.invoice_id = aa.invoice_id where aa.invoice_id is not null ");
//
//            if (dto.getInvoiceNo() != null && dto.getInvoiceNo().trim().length()>0)
//                createSqlQuery.append("and aa.invoice_no ILIKE '%"+dto.getInvoiceNo().trim()+"%' " );
//            if (dto.getStartDate() != null)
//                createSqlQuery.append(" and aa.created_at >= to_date('" + sdf.format(dto.getStartDate()) + "'," + "'dd.MM.yyyy') ");
//            if (dto.getEndDate() != null)
//                createSqlQuery.append(" and aa.created_at <= to_date('" + sdf.format(dto.getEndDate()) + "'," + "'dd.MM.yyyy') ");
//
//            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), AccountActivity.class).getResultList();
//            AccountActivitiesDto[] dtos = mapper.map(list, AccountActivitiesDto[].class);
//            List<AccountActivitiesDto> dtosList = Arrays.asList(dtos);
//            int start = Math.min((int) page.getOffset(), dtosList.size());
//            int end = Math.min((start + page.getPageSize()), dtosList.size());
//
//            Page<AccountActivitiesDto> pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());
//            return pageList;
//        }
//        else {
//
//            // Alış faturasında hep fatura edilen EkipEcza ve otherCheckingCard da olacak
//            if (invoice.get().getInvoicePurpose() == InvoicePurpose.BUY_INVOICE ||
//                    invoice.get().getInvoicePurpose() == InvoicePurpose.REFUND_SELL_INVOICE) {
//
//                StringBuilder createSqlQuery = new StringBuilder("select * from account_activity aa  ");
//                createSqlQuery.append("join invoice i on i.invoice_id = aa.invoice_id where aa.other_checking_card_id = 1 ");
//
//                if (dto.getInvoiceNo() != null && dto.getInvoiceNo().trim().length()>0)
//                    createSqlQuery.append("and aa.invoice_no ILIKE '%" + dto.getInvoiceNo().trim() + "%' ");
//                if (dto.getStartDate() != null)
//                    createSqlQuery.append(" and aa.created_at >= to_date('" + sdf.format(dto.getStartDate()) + "'," + "'dd.MM.yyyy') ");
//                if (dto.getEndDate() != null)
//                    createSqlQuery.append(" and aa.created_at <= to_date('" + sdf.format(dto.getEndDate()) + "'," + "'dd.MM.yyyy') ");
//
//                List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), AccountActivity.class).getResultList();
//                AccountActivitiesDto[] dtos = mapper.map(list, AccountActivitiesDto[].class);
//                List<AccountActivitiesDto> dtosList = Arrays.asList(dtos);
//                int start = Math.min((int) page.getOffset(), dtosList.size());
//                int end = Math.min((start + page.getPageSize()), dtosList.size());
//
//                Page<AccountActivitiesDto> pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());
//                return pageList;
//            }
//            // Satış faturasında hep fatura eden EkipEcza ve checkingCard da olacak
//            else if (invoice.get().getInvoicePurpose() == InvoicePurpose.SELL_INVOICE ||
//                    invoice.get().getInvoicePurpose() == InvoicePurpose.REFUND_BUY_INVOICE) {
//
//                StringBuilder createSqlQuery = new StringBuilder("select * from account_activity aa  ");
//                createSqlQuery.append("join invoice i on i.invoice_id = aa.invoice_id where aa.checking_card_id = 1 ");
//
//                if (dto.getInvoiceNo() != null && dto.getInvoiceNo().trim().length()>0)
//                    createSqlQuery.append("and aa.invoice_no ILIKE '%" + dto.getInvoiceNo() + "%' ");
//                if (dto.getStartDate() != null)
//                    createSqlQuery.append(" and aa.created_at >= to_date('" + sdf.format(dto.getStartDate()) + "'," + "'dd.MM.yyyy') ");
//                if (dto.getEndDate() != null)
//                    createSqlQuery.append(" and aa.created_at <= to_date('" + sdf.format(dto.getEndDate()) + "'," + "'dd.MM.yyyy') ");
//
//                List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), AccountActivity.class).getResultList();
//                AccountActivitiesDto[] dtos = mapper.map(list, AccountActivitiesDto[].class);
//                List<AccountActivitiesDto> dtosList = Arrays.asList(dtos);
//                int start = Math.min((int) page.getOffset(), dtosList.size());
//                int end = Math.min((start + page.getPageSize()), dtosList.size());
//
//                Page<AccountActivitiesDto> pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());
//                return pageList;
//
//            } else {
//                return null;
//            }
//        }
    }

    public Boolean invoiceStatusControl(Long invoiceId) throws Exception {

        try {

            Optional<Invoice> optInvoice = invoiceRepository.findById(invoiceId);
            if (!optInvoice.isPresent()) {
                throw new Exception("Fatura Bulunamadı !");
            }

            Double sumOfCharge = accountActivityRepository.sumOfCharge(invoiceId);
            Double sumOfDebt = accountActivityRepository.sumOfDebt(invoiceId);

            //faturadaki toplam tutar ile hareketlerdeki tutar birbirine eşit ise fatura kapatılır
            if (sumOfCharge.equals(sumOfDebt) && sumOfCharge.equals(optInvoice.get().getTotalPrice())) {
                optInvoice.get().setInvoiceStatus(invoiceStatusRepository.findById(20L).get());
                invoiceRepository.save(optInvoice.get());
            }


            return true;

        } catch (Exception e) {
            throw new Exception("Fatura Durum Kontrol İşleminde Hata Oluştu.", e);
        }


    }

    public String createCheckingCardActivityExcel(Pageable page, CheckinCardActivitySearchDto dto, String authHeader) throws Exception {
        User user = controlService.getUserFromToken(authHeader);
        //ARAMA BAŞLANGIÇ
        Boolean control = false;
        if (dto.getCheckingCardId() == null)
            throw new NotFoundException("Cari kart bulunamadı.");
        Optional<CheckingCard> optOther;
        StringBuilder createSqlQuery;
        if (dto.getOtherCheckingCardId() != null) {
            optOther = checkingCardRepository.findById(dto.getOtherCheckingCardId());
            if (optOther.isPresent()) {
                if (dto.getCheckingCardId() == 1 && optOther.get().getType() == CheckingCardType.SUPPLIER) {
                    control = true;
                }
            }
        }
        //  checking card ekip ecza ve other checking card supplier (Eczane) ise
        if (control == true) {
            createSqlQuery = new StringBuilder("select aa.* from account_activity aa ");
            createSqlQuery.append("join checking_card cc on cc.checking_card_id = aa.checking_card_id ");
            // checking_card_id= 2  Liva
            createSqlQuery.append("where aa.status = 1 ");

            if (dto.getStartDate() != null)
                createSqlQuery.append(" and aa.created_at  >= to_date('" + sdf.format(dto.getStartDate()) + "'," + "'dd.MM.yyyy') ");
            if (dto.getEndDate() != null)
                createSqlQuery.append(" and aa.created_at  < to_date('" + sdf.format(dto.getEndDate()) + "'," + "'dd.MM.yyyy') ");
            if (dto.getOtherCheckingCardId() != null)
                createSqlQuery.append(" and aa.other_checking_card_id = " + dto.getOtherCheckingCardId());
            if (dto.getInvoiceNo() != null)
                if (dto.getInvoiceNo().trim().length() > 0)
                    createSqlQuery.append(" and aa.invoice_no = '" + dto.getInvoiceNo().trim() + "'");

            createSqlQuery.append(" order by aa.account_activity_id");
        } else {
            createSqlQuery = new StringBuilder("select aa.* from account_activity aa ");
            createSqlQuery.append("join checking_card cc on cc.checking_card_id = aa.checking_card_id  ");
            createSqlQuery.append("where aa.status = 1  and aa.checking_card_id =" + dto.getCheckingCardId() + " ");

            if (dto.getStartDate() != null)
                createSqlQuery.append(" and aa.created_at  >= to_date('" + sdf.format(dto.getStartDate()) + "'," + "'dd.MM.yyyy') ");
            if (dto.getEndDate() != null)
                createSqlQuery.append(" and aa.created_at  < to_date('" + sdf.format(dto.getEndDate()) + "'," + "'dd.MM.yyyy') ");
            if (dto.getOtherCheckingCardId() != null)
                createSqlQuery.append(" and aa.other_checking_card_id = " + dto.getOtherCheckingCardId());
            if (dto.getInvoiceNo() != null)
                if (dto.getInvoiceNo().trim().length() > 0)
                    createSqlQuery.append(" and aa.invoice_no = '" + dto.getInvoiceNo().trim() + "'");

            createSqlQuery.append(" order by aa.account_activity_id");
        }

        List<AccountActivity> activityList = entityManager.createNativeQuery(createSqlQuery.toString(), AccountActivity.class).getResultList();
        //ARAMA SON

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sayfa1");

        //STYLE BAŞLANGIÇ
        CellStyle csBody = workbook.createCellStyle();
        XSSFFont fontBody = workbook.createFont();
        fontBody.setFontName("Times New Roman");
        fontBody.setFontHeightInPoints((short) 11);
        csBody.setWrapText(true);
        csBody.setLocked(false);
        csBody.setFont(fontBody);
        csBody.setAlignment(HorizontalAlignment.CENTER);
        //STYLE SON

        addExcelHeader(workbook, sheet);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        int a = 6;
        int b = 0;
        double totalCharge = 0.0;
        double totalDebt = 0.0;
        for (AccountActivity activity : activityList) {
            a++;
            b++;
            XSSFRow row = sheet.createRow((short) a);
            row.createCell(0).setCellValue(b);
            row.getCell(0).setCellStyle(csBody);
            //İçerik Kısmı Bu Satırdan İtibaren Obje Bilgileri İle Doldurulur
            row.createCell(1).setCellValue(activity.getInvoiceNo());
            row.getCell(1).setCellStyle(csBody);
            row.createCell(2).setCellValue(activity.getOtherCheckingCard().getCheckingCardName());
            row.getCell(2).setCellStyle(csBody);
            row.createCell(3).setCellValue(activity.getAccountActivityType().getValue());
            row.getCell(3).setCellStyle(csBody);
//            if (activity.getCheckingCard().getCheckingCardId() != 1 && activity.getOtherCheckingCard().getCheckingCardId() == 2 && activity.getAccountActivityType() == AccountActivityType.CUSTOMER_INVOICE) {
//                row.createCell(4).setCellValue(activity.getInvoice().getChargeLiva() + " TL");
//                row.getCell(4).setCellStyle(csBody);
//                totalCharge += activity.getInvoice().getChargeLiva();
//            } else {
                if (activity.getCharge() != null) {
                    row.createCell(4).setCellValue(activity.getCharge() + " TL");
                    row.getCell(4).setCellStyle(csBody);
                    totalCharge += activity.getCharge();
                } else {
                    row.createCell(4).setCellValue(" ");
                    row.getCell(4).setCellStyle(csBody);
                }

            //}
//            if (activity.getCheckingCard().getCheckingCardId() == 2 && activity.getOtherCheckingCard().getCheckingCardId() != 1 && activity.getAccountActivityType() == AccountActivityType.PURCHASE_INVOICE) {
//                row.createCell(5).setCellValue(activity.getInvoice().getChargeLiva() + " TL");
//                row.getCell(5).setCellStyle(csBody);
//                totalDebt += activity.getInvoice().getChargeLiva();
//            } else {
                if (activity.getDebt() != null) {
                    row.createCell(5).setCellValue(activity.getDebt() + " TL");
                    row.getCell(5).setCellStyle(csBody);
                    totalDebt += activity.getDebt();
                } else {
                    row.createCell(5).setCellValue(" ");
                    row.getCell(5).setCellStyle(csBody);
                }

           // }
            Double value = 0d;
//            if ((activity.getCheckingCard().getCheckingCardId() != 1 && activity.getOtherCheckingCard().getCheckingCardId() == 2 && activity.getAccountActivityType() == AccountActivityType.CUSTOMER_INVOICE) ||
//                    (activity.getCheckingCard().getCheckingCardId() == 2 && activity.getOtherCheckingCard().getCheckingCardId() != 1 && activity.getAccountActivityType() == AccountActivityType.PURCHASE_INVOICE)) {
//                value = ((double) ((int) ((activity.getInvoice().getChargeLiva() / activity.getCurrencyFee()) * 1000.0))) / 1000.0;
//                row.createCell(6).setCellValue(value);
//                row.getCell(6).setCellStyle(csBody);
//            } else {
                if (activity.getCharge() != null) {
                    value = ((double) ((int) ((activity.getCharge() / activity.getCurrencyFee()) * 1000.0))) / 1000.0;
                    row.createCell(6).setCellValue(value);
                    row.getCell(6).setCellStyle(csBody);
                } else {
                    value = ((double) ((int) ((activity.getDebt() / activity.getCurrencyFee()) * 1000.0))) / 1000.0;
                    row.createCell(6).setCellValue(value);
                    row.getCell(6).setCellStyle(csBody);
                }

          //  }
            row.createCell(7).setCellValue(activity.getCurrencyType().toString());
            row.getCell(7).setCellStyle(csBody);
            row.createCell(8).setCellValue(activity.getCurrencyFee() + " TL");
            row.getCell(8).setCellStyle(csBody);
            row.createCell(9).setCellValue(dateFormat.format(activity.getCreatedAt()));
            row.getCell(9).setCellStyle(csBody);
        }

        XSSFRow row = sheet.createRow((short) a + 3);
        row.createCell(4).setCellValue("Toplam Alacak");
        row.getCell(4).setCellStyle(this.headerStyle(workbook));
        row.createCell(5).setCellValue("Toplam Borç");
        row.getCell(5).setCellStyle(this.headerStyle(workbook));

        sheet.addMergedRegion(new CellRangeAddress(a + 3, a + 3, 7, 8));
        row.createCell(7).setCellValue("Genel Toplam");
        row.getCell(7).setCellStyle(this.headerStyle(workbook));

        XSSFRow row2 = sheet.createRow((short) a + 4);
        row2.createCell(4).setCellValue(totalCharge + " TL");
        row2.getCell(4).setCellStyle(csBody);
        row2.createCell(5).setCellValue(totalDebt + " TL");
        row2.getCell(5).setCellStyle(csBody);
        sheet.addMergedRegion(new CellRangeAddress(a + 4, a + 4, 7, 8));
        row2.createCell(7).setCellValue(totalCharge - totalDebt + " TL");
        row2.getCell(7).setCellStyle(csBody);

        String excelTitle = "activity-excel_" + user.getUserId();

        FileOutputStream fileOut = new FileOutputStream("docs/" + excelTitle + ".xlsx");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

        return excelTitle;

    }

    public String createActivityExcel(Pageable page, AccountActivitiesSearchDto dto, String authHeader) throws Exception {
        User user = controlService.getUserFromToken(authHeader);
        //ARAMA BAŞLANGIÇ
        StringBuilder createSqlQuery = new StringBuilder("select aa.* from account_activity aa ");
        createSqlQuery.append("join checking_card cc on cc.checking_card_id = aa.checking_card_id  ");
        createSqlQuery.append("where aa.status = 1 ");

        if (dto.getStartDate() != null)
            createSqlQuery.append(" and aa.created_at  >= to_date('" + sdf.format(dto.getStartDate()) + "'," + "'dd.MM.yyyy') ");
        if (dto.getEndDate() != null)
            createSqlQuery.append(" and aa.created_at  < to_date('" + sdf.format(dto.getEndDate()) + "'," + "'dd.MM.yyyy') ");
        if (dto.getInvoiceNo() != null)
            if (dto.getInvoiceNo().trim().length() > 0)
                createSqlQuery.append(" and aa.invoice_no = '" + dto.getInvoiceNo().trim() + "'");

        createSqlQuery.append(" order by aa.account_activity_id");

        List<AccountActivity> activityList = entityManager.createNativeQuery(createSqlQuery.toString(), AccountActivity.class).getResultList();

        //ARAMA SON

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sayfa1");

        //STYLE BAŞLANGIÇ
        CellStyle csBody = workbook.createCellStyle();
        XSSFFont fontBody = workbook.createFont();
        fontBody.setFontName("Times New Roman");
        fontBody.setFontHeightInPoints((short) 11);
        csBody.setWrapText(true);
        csBody.setLocked(false);
        csBody.setFont(fontBody);
        csBody.setAlignment(HorizontalAlignment.CENTER);
        //STYLE SON

        addExcelHeader2(workbook, sheet);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        int a = 6;
        int b = 0;
        for (AccountActivity activity : activityList) {
            a++;
            b++;
            XSSFRow row = sheet.createRow((short) a);
            row.createCell(0).setCellValue(b);
            row.getCell(0).setCellStyle(csBody);
            //İçerik Kısmı Bu Satırdan İtibaren Obje Bilgileri İle Doldurulur
            row.createCell(1).setCellValue(activity.getInvoiceNo());
            row.getCell(1).setCellStyle(csBody);
            row.createCell(2).setCellValue(activity.getCheckingCard().getCheckingCardName());
            row.getCell(2).setCellStyle(csBody);
            row.createCell(3).setCellValue(activity.getOtherCheckingCard().getCheckingCardName());
            row.getCell(3).setCellStyle(csBody);
            row.createCell(4).setCellValue(activity.getAccountActivityType().getValue());
            row.getCell(4).setCellStyle(csBody);
            if (activity.getCharge() != null) {
                row.createCell(5).setCellValue(activity.getCharge() + " TL");
                row.getCell(5).setCellStyle(csBody);
            } else {
                row.createCell(5).setCellValue(" ");
                row.getCell(5).setCellStyle(csBody);
            }
            if (activity.getDebt() != null) {
                row.createCell(6).setCellValue(activity.getDebt() + " TL");
                row.getCell(6).setCellStyle(csBody);
            } else {
                row.createCell(6).setCellValue(" ");
                row.getCell(6).setCellStyle(csBody);
            }
            Double value = 0d;
            if (activity.getCharge() != null) {
                value = ((double) ((int) ((activity.getCharge() / activity.getCurrencyFee()) * 1000.0))) / 1000.0;
                row.createCell(7).setCellValue(value);
                row.getCell(7).setCellStyle(csBody);
            } else {
                value = ((double) ((int) ((activity.getDebt() / activity.getCurrencyFee()) * 1000.0))) / 1000.0;
                row.createCell(7).setCellValue(value);
                row.getCell(7).setCellStyle(csBody);
            }
            row.createCell(8).setCellValue(activity.getCurrencyType().toString());
            row.getCell(8).setCellStyle(csBody);
            row.createCell(9).setCellValue(activity.getCurrencyFee() + " TL");
            row.getCell(9).setCellStyle(csBody);
            row.createCell(10).setCellValue(dateFormat.format(activity.getCreatedAt()));
            row.getCell(10).setCellStyle(csBody);
        }

        String excelTitle = "activity-excel_" + user.getUserId();

        FileOutputStream fileOut = new FileOutputStream("docs/" + excelTitle + ".xlsx");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

        return excelTitle;
    }

    private CellStyle headerStyle(XSSFWorkbook workbook) {
        XSSFFont fontHeader = workbook.createFont();
        fontHeader.setFontHeightInPoints((short) 12);
        fontHeader.setFontName("Times New Roman");
        CellStyle csHeader = workbook.createCellStyle();
        csHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        csHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        csHeader.setWrapText(true);
        csHeader.setLocked(false);
        csHeader.setAlignment(HorizontalAlignment.CENTER);
        csHeader.setFont(fontHeader);

        return csHeader;
    }

    private void addExcelHeader(XSSFWorkbook workbook, XSSFSheet sheet) throws IOException {
        //STYLE BAŞLANGIÇ
        XSSFFont fontHeader = workbook.createFont();
        fontHeader.setFontHeightInPoints((short) 12);
        fontHeader.setFontName("Times New Roman");
        CellStyle csHeader = workbook.createCellStyle();
        csHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        csHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        csHeader.setWrapText(true);
        csHeader.setLocked(false);
        csHeader.setAlignment(HorizontalAlignment.CENTER);
        csHeader.setFont(fontHeader);

        CellStyle csHeading = workbook.createCellStyle();
        XSSFFont fontHeading = workbook.createFont();
        fontHeading.setFontName("Times New Roman");
        fontHeading.setFontHeightInPoints((short) 14);
        csHeading.setFont(fontHeading);
        csHeading.setLocked(false);
        csHeading.setAlignment(HorizontalAlignment.CENTER);

        CellStyle csDate = workbook.createCellStyle();
        XSSFFont fontDate = workbook.createFont();
        fontDate.setFontName("Times New Roman");
        fontDate.setFontHeightInPoints((short) 12);
        csDate.setFont(fontDate);
        csDate.setLocked(false);
        //STYLE SON

        XSSFRow rowDate = sheet.createRow((short) 2);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        //Bu kısım Kolon Sayısına Eşit Olmalı
        rowDate.createCell(9).setCellValue(dateFormat.format(new Date()));
        rowDate.getCell(9).setCellStyle(csDate);


        InputStream inputStream = new FileInputStream("docs/pharma.png");
        byte[] bytes = IOUtils.toByteArray(inputStream);
        int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
        inputStream.close();
        CreationHelper helper = workbook.getCreationHelper();
        Drawing drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(0);
        anchor.setRow1(0);
        Picture pict = drawing.createPicture(anchor, pictureIdx);
        pict.resize(1.8, 3.6);


        //Başlık verilir
        XSSFRow rowHeader = sheet.createRow((short) 4);
        rowHeader.createCell(0).setCellValue("Cari Hesap Hareket Listesi");
        rowHeader.getCell(0).setCellStyle(csHeading);

        sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 1));
        //son parametre kolon sayısına eşit olmalı
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 10));


        XSSFRow rowhead = sheet.createRow((short) 6);

        //Kolon İsimleri Belirtilir İhtiyaç Olursa Kolonlar Genişletilir
        rowhead.createCell(0).setCellValue("No");
        rowhead.getCell(0).setCellStyle(csHeader);
        sheet.setColumnWidth(0, 1500);//kolon genişletildi
        rowhead.createCell(1).setCellValue("Fatura Numarası");
        rowhead.getCell(1).setCellStyle(csHeader);
        sheet.setColumnWidth(1, 7000);
        rowhead.createCell(2).setCellValue("Karşı Cari Hesap");
        rowhead.getCell(2).setCellStyle(csHeader);
        sheet.setColumnWidth(2, 8000);
        rowhead.createCell(3).setCellValue("Cari Hareket Türü");
        rowhead.getCell(3).setCellStyle(csHeader);
        sheet.setColumnWidth(3, 5000);
        rowhead.createCell(4).setCellValue("Alacak");
        rowhead.getCell(4).setCellStyle(csHeader);
        sheet.setColumnWidth(4, 5000);
        rowhead.createCell(5).setCellValue("Borç");
        rowhead.getCell(5).setCellStyle(csHeader);
        sheet.setColumnWidth(5, 5000);
        rowhead.createCell(6).setCellValue("Döviz Karşılığı");
        rowhead.getCell(6).setCellStyle(csHeader);
        sheet.setColumnWidth(6, 5000);
        rowhead.createCell(7).setCellValue("Döviz Tipi");
        rowhead.getCell(7).setCellStyle(csHeader);
        sheet.setColumnWidth(7, 3000);
        rowhead.createCell(8).setCellValue("Döviz Kuru");
        rowhead.getCell(8).setCellStyle(csHeader);
        sheet.setColumnWidth(8, 3000);
        rowhead.createCell(9).setCellValue("Kayıt Tarihi");
        rowhead.getCell(9).setCellStyle(csHeader);
        sheet.setColumnWidth(9, 5000);

        //A4 Sayfaya Sığdırma
        sheet.setFitToPage(true);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);

    }

    private void addExcelHeader2(XSSFWorkbook workbook, XSSFSheet sheet) throws IOException {
        //STYLE BAŞLANGIÇ
        XSSFFont fontHeader = workbook.createFont();
        fontHeader.setFontHeightInPoints((short) 12);
        fontHeader.setFontName("Times New Roman");
        CellStyle csHeader = workbook.createCellStyle();
        csHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        csHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        csHeader.setWrapText(true);
        csHeader.setLocked(false);
        csHeader.setAlignment(HorizontalAlignment.CENTER);
        csHeader.setFont(fontHeader);

        CellStyle csHeading = workbook.createCellStyle();
        XSSFFont fontHeading = workbook.createFont();
        fontHeading.setFontName("Times New Roman");
        fontHeading.setFontHeightInPoints((short) 14);
        csHeading.setFont(fontHeading);
        csHeading.setLocked(false);
        csHeading.setAlignment(HorizontalAlignment.CENTER);

        CellStyle csDate = workbook.createCellStyle();
        XSSFFont fontDate = workbook.createFont();
        fontDate.setFontName("Times New Roman");
        fontDate.setFontHeightInPoints((short) 12);
        csDate.setFont(fontDate);
        csDate.setLocked(false);
        //STYLE SON

        XSSFRow rowDate = sheet.createRow((short) 2);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        //Bu kısım Kolon Sayısına Eşit Olmalı
        rowDate.createCell(9).setCellValue(dateFormat.format(new Date()));
        rowDate.getCell(9).setCellStyle(csDate);


        InputStream inputStream = new FileInputStream("docs/pharma.png");
        byte[] bytes = IOUtils.toByteArray(inputStream);
        int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
        inputStream.close();
        CreationHelper helper = workbook.getCreationHelper();
        Drawing drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(0);
        anchor.setRow1(0);
        Picture pict = drawing.createPicture(anchor, pictureIdx);
        pict.resize(1.8, 3.6);


        //Başlık verilir
        XSSFRow rowHeader = sheet.createRow((short) 4);
        rowHeader.createCell(0).setCellValue("Cari Hesap Hareket Listesi");
        rowHeader.getCell(0).setCellStyle(csHeading);

        sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 1));
        //son parametre kolon sayısına eşit olmalı
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 11));


        XSSFRow rowhead = sheet.createRow((short) 6);

        //Kolon İsimleri Belirtilir İhtiyaç Olursa Kolonlar Genişletilir
        rowhead.createCell(0).setCellValue("No");
        rowhead.getCell(0).setCellStyle(csHeader);
        sheet.setColumnWidth(0, 1500);//kolon genişletildi
        rowhead.createCell(1).setCellValue("Fatura Numarası");
        rowhead.getCell(1).setCellStyle(csHeader);
        sheet.setColumnWidth(1, 7000);
        rowhead.createCell(2).setCellValue("Cari Hesap");
        rowhead.getCell(2).setCellStyle(csHeader);
        sheet.setColumnWidth(2, 8000);
        rowhead.createCell(3).setCellValue("Karşı Cari Hesap");
        rowhead.getCell(3).setCellStyle(csHeader);
        sheet.setColumnWidth(3, 8000);
        rowhead.createCell(4).setCellValue("Cari Hareket Türü");
        rowhead.getCell(4).setCellStyle(csHeader);
        sheet.setColumnWidth(4, 5000);
        rowhead.createCell(5).setCellValue("Alacak");
        rowhead.getCell(5).setCellStyle(csHeader);
        sheet.setColumnWidth(5, 5000);
        rowhead.createCell(6).setCellValue("Borç");
        rowhead.getCell(6).setCellStyle(csHeader);
        sheet.setColumnWidth(6, 5000);
        rowhead.createCell(7).setCellValue("Döviz Karşılığı");
        rowhead.getCell(7).setCellStyle(csHeader);
        sheet.setColumnWidth(7, 5000);
        rowhead.createCell(8).setCellValue("Döviz Tipi");
        rowhead.getCell(8).setCellStyle(csHeader);
        sheet.setColumnWidth(8, 3000);
        rowhead.createCell(9).setCellValue("Döviz Kuru");
        rowhead.getCell(9).setCellStyle(csHeader);
        sheet.setColumnWidth(9, 3000);
        rowhead.createCell(10).setCellValue("Kayıt Tarihi");
        rowhead.getCell(10).setCellStyle(csHeader);
        sheet.setColumnWidth(10, 5000);

        //A4 Sayfaya Sığdırma
        sheet.setFitToPage(true);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);

    }

    public Boolean createAccountActivity(Invoice invoice,
                                         CheckingCard checkingCard,
                                         CheckingCard otherCheckingCard,
                                         AccountActivityType accountActivityType, Double unitPrice) {
        AccountActivity accountActivity = new AccountActivity();
        accountActivity.setCurrencyFee(invoice.getCurrencyFee());
        accountActivity.setCurrencyType(invoice.getCurrencyType());
        accountActivity.setCheckingCard(checkingCard);
        accountActivity.setOtherCheckingCard(otherCheckingCard);
        accountActivity.setAccountActivityType(accountActivityType);
        accountActivity.setInvoice(invoice);
        accountActivity.setInvoiceNo(invoice.getInvoiceNo());
        accountActivity.setCreatedAt(new Date());
        accountActivity.setStatus(1);
        if (accountActivityType == AccountActivityType.PURCHASE_INVOICE)
            accountActivity.setDebt(unitPrice);
        if (accountActivityType == AccountActivityType.CUSTOMER_INVOICE)
            accountActivity.setCharge(unitPrice);
        accountActivityRepository.save(accountActivity);

        return true;
    }
}
