package com.via.ecza.service;

import com.itextpdf.text.Font;
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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;


@Service
@Transactional
public class ReceiptService {


    @Autowired
    private RefundPriceRepository refundPriceRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private CheckingCardRepository checkingCardRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private ControlService controlService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReceiptRepository receiptRepository;
    @Autowired
    private CustomerSupplyOrderRepository customerSupplyOrderRepository;
    @Autowired
    private RefundRepository refundRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private ReceiptContentRepository receiptContentRepository;
    //    @Autowired
//    private FinalReceiptRepository finalReceiptRepository;
    @Autowired
    private ReceiptStatusRepository receiptStatusRepository;
    @Autowired
    private InvoiceTypeRepository invoiceTypeRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private InvoiceStatusRepository invoiceStatusRepository;
    @Autowired
    private AccountActivityRepository accountActivityRepository;
    @Autowired
    private AccountActivityService accountActivityService;
    @Autowired
    private OtherCompanyRepository otherCompanyRepository;
    @Autowired
    private SupplyOrderPriceRepository supplyOrderPriceRepository;


    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public List<ReceiptListDto> searchReceipts(ReceiptSearchDto dto) {

        StringBuilder createSqlQuery = new StringBuilder(" select * from receipt r   where status=1  ");
        createSqlQuery.append(" and r.receipt_type = '" + ReceiptType.ALIS + "' ");

        if (dto.getReceiptStatusId() != null)
            createSqlQuery.append(" and r.receipt_status = " + dto.getReceiptStatusId() + " ");
        if (dto.getSupplierId() != null)
            createSqlQuery.append(" and r.supplier_id = " + dto.getSupplierId() + " ");
        if (dto.getInvoiceNo() != null && dto.getInvoiceNo().trim().length() > 0)
            createSqlQuery.append(" and r.invoice_no ILIKE '%" + dto.getInvoiceNo().trim() + "%' ");
        if (dto.getReceiptNo() != null && dto.getReceiptNo().trim().length() > 0)
            createSqlQuery.append(" and r.receipt_no ILIKE '%" + dto.getReceiptNo().trim() + "%' ");
        if (dto.getCreatedAt() != null) {
            createSqlQuery.append(" and r.created_at >=  to_date('" + sdf.format(dto.getCreatedAt()) + "'," + "'dd/MM/yyyy') ");
        }

        createSqlQuery.append("group  by r.receipt_id  order by r.receipt_id ");

        List<Receipt> list = entityManager.createNativeQuery(createSqlQuery.toString(), Receipt.class).getResultList();
        ReceiptListDto[] dtos = mapper.map(list, ReceiptListDto[].class);
        List<ReceiptListDto> listDtos = Arrays.asList(dtos);

        int index = 0;
        for (Receipt receipt : list) {
            Long totality = 0l;
            Double totalPrice = 0.0;
            Double totalPriceWithVat = 0.0;
            for (CustomerSupplyOrder cso : receipt.getCustomerSupplyOrders()) {
                totality = totality + cso.getTotality();
                totalPrice = totalPrice + cso.getTotalPrice();
                if (cso.getDrugCard().getStatus() == 1)
                    totalPriceWithVat += (cso.getTotalPrice() + (cso.getTotalPrice() * 8 / 100));
                if (cso.getDrugCard().getStatus() == 2) {
                    if (cso.getDrugCard().getDrugVat() != null)
                        totalPriceWithVat += (cso.getTotalPrice() + (cso.getTotalPrice() * cso.getDrugCard().getDrugVat() / 100));
                    else
                        totalPriceWithVat += cso.getTotalPrice();
                }
            }
            listDtos.get(index).setTotality(totality);
            listDtos.get(index).setTotalPriceWithVat(totalPriceWithVat);
            listDtos.get(index).setTotalPrice(totalPrice);
            index++;
        }


        return listDtos;
    }

    public String saveReceipt(ReceiptCreateDto dto) throws Exception {
        Optional<ReceiptStatus> receiptStatus = receiptStatusRepository.findById(1l);
        if (!receiptStatus.isPresent()) {
            throw new NotFoundException("Statü Bulunamadı");
        }
        String no = null;
        try {
            if (dto.getInvoiceDate() == null || dto.getCheckedList() == null || dto.getInvoiceNo() == null)
                return null;
            if (dto.getCheckedList().size() < 1)
                return null;
            Receipt receipt = new Receipt();
            receipt.setCreatedAt(new Date());
            receipt.setReceiptNote(dto.getReceiptNote());
            receipt.setInvoiceDate(dto.getInvoiceDate());
            receipt.setInvoiceNo(dto.getInvoiceNo());
            receipt.setStatus(1);
            receipt.setReceiptStatus(receiptStatus.get());
            receipt = receiptRepository.save(receipt);
            receipt.setReceiptNo(this.generateReceiptNo(receipt.getReceiptId()));
            receipt = receiptRepository.save(receipt);
            Supplier supplier = null;

            for (ReceiptCheckListDto check : dto.getCheckedList()) {
                if (check == null)
                    continue;
                if (check.getValue() == true) {
                    if (check.getCustomerSupplyOrderId() == null)
                        continue;
                    Optional<CustomerSupplyOrder> cso = customerSupplyOrderRepository.findById(check.getCustomerSupplyOrderId());
                    if (!cso.isPresent())
                        continue;
                    CustomerSupplyOrder customerSupplyOrder = cso.get();
                    if (customerSupplyOrder.getReceipt() != null)
                        continue;
                    customerSupplyOrder.setReceipt(receipt);
                    if (supplier == null)
                        supplier = customerSupplyOrder.getSupplier();
                    customerSupplyOrder = customerSupplyOrderRepository.save(customerSupplyOrder);
                    //receipt.getCustomerSupplyOrders().add(cso.get());
                }
            }


            if (supplier == null)
                throw new NotFoundException("Böyle bir tedarikçi bulunamadı");
            receipt.setSupplier(supplier);
            receipt.setReceiptType(ReceiptType.ALIS);
            receipt = receiptRepository.save(receipt);

            no = receipt.getReceiptNo();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return no;
    }

//    private FinalReceipt createFinalReceipt() {
//        FinalReceipt finalReceipt = new FinalReceipt();
//        finalReceipt.setCreatedAt(new Date());
//        finalReceipt.setStatus(1);
//        finalReceipt = finalReceiptRepository.save(finalReceipt);
//        finalReceipt.setFinalReceiptNo(this.generateFinalReceiptNo(finalReceipt.getFinalReceiptId()));
//        finalReceipt = finalReceiptRepository.save(finalReceipt);
//        return finalReceipt;
//    }

    private String generateFinalReceiptNo(Long receiptId) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String code = "FIS-MUH-" + year;
        int size = receiptId.toString().length();
        for (int i = 0; i < 7 - size; i++)
            code += "0";
        code += receiptId;
        return code;
    }

    private String generateReceiptNo(Long receiptId) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String code = "FIS-" + year;
        int size = receiptId.toString().length();
        for (int i = 0; i < 7 - size; i++)
            code += "0";
        code += receiptId;
        return code;
    }

    public List<CustomerSupplyOrderDrugListDto> searchCustomerSupplyOrderDrug(CustomerSupplyOrderDrugDto dto) {
        List<CustomerSupplyOrderDrugListDto> list = new ArrayList<>();
        StringBuilder createSqlQuery = new StringBuilder("select * from customer_supply_order cso where cso.customer_supply_status_id = 50 and cso.receipt_id is null ");


        if (dto.getSupplierId() != null)
            createSqlQuery.append(" and cso.supplier_id =" + dto.getSupplierId());
        if (dto.getSupplierOrderNo() != null)
            if (dto.getSupplierOrderNo().trim().length() > 0)
                createSqlQuery.append("and cso.supply_order_no ILIKE '%" + dto.getSupplierOrderNo().trim() + "%' ");
        if (dto.getStartDate() != null)
            createSqlQuery.append(" and cso.created_at >= to_date('" + sdf.format(dto.getStartDate()) + "'," + "'dd.MM.yyyy') ");
        if (dto.getEndDate() != null)
            createSqlQuery.append(" and cso.created_at <= to_date('" + sdf.format(dto.getEndDate()) + "'," + "'dd.MM.yyyy') ");

        createSqlQuery.append(" order by cso.customer_supply_order_id ");

        List<Object> objectList = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerSupplyOrder.class).getResultList();

        CustomerSupplyOrderDrugListDto[] dtos = mapper.map(objectList, CustomerSupplyOrderDrugListDto[].class);

        list = Arrays.asList(dtos);

        for (CustomerSupplyOrderDrugListDto item : list) {
            if (item.getOtherCompanyId() != null) {
                Optional<OtherCompany> optOtherCompany = otherCompanyRepository.findById(item.getOtherCompanyId());
                if (optOtherCompany.isPresent()) {
                    item.setOtherCompanyName(optOtherCompany.get().getOtherCompanyName());
                }
            }
        }

        return list;
    }

    public List<CustomerSupplyOrderDrugListSellDto> searchRefundSupplyOrderDrug(CustomerSupplyOrderDrugSellDto dto) {
        List<CustomerSupplyOrderDrugListSellDto> list = new ArrayList<>();
        StringBuilder createSqlQuery = new StringBuilder("select * from refund r where r.receipt_id is null ");

//
        if (dto.getSupplierId() != null)
            createSqlQuery.append(" and r.supplier_id =" + dto.getSupplierId());
        if (dto.getRefundOrderNo() != null)
            if (dto.getRefundOrderNo().trim().length() > 0)
                createSqlQuery.append("and r.refund_order_no ILIKE '%" + dto.getRefundOrderNo().trim() + "%' ");
        if (dto.getStartDate() != null)
            createSqlQuery.append(" and r.created_at >= to_date('" + sdf.format(dto.getStartDate()) + "'," + "'dd.MM.yyyy') ");
        if (dto.getEndDate() != null)
            createSqlQuery.append(" and r.created_at <= to_date('" + sdf.format(dto.getEndDate()) + "'," + "'dd.MM.yyyy') ");

        createSqlQuery.append(" order by r.refund_id ");

        System.out.println(createSqlQuery);
        List<Refund> objectList = entityManager.createNativeQuery(createSqlQuery.toString(), Refund.class).getResultList();

        CustomerSupplyOrderDrugListSellDto[] dtos = mapper.map(objectList, CustomerSupplyOrderDrugListSellDto[].class);

        list = Arrays.asList(dtos);

        return list;
    }

    public SingleReceiptDto findByReceiptId(Long receiptId) {
        SingleReceiptDto dto = null;
        Optional<Receipt> data = receiptRepository.findById(receiptId);
        if (data.isPresent())
            dto = mapper.map(data.get(), SingleReceiptDto.class);
        return dto;
    }

    public List<CustomerSupplyOrderDrugListDto> getOrderByReceiptId(Long receiptId) {
        List<CustomerSupplyOrderDrugListDto> listDtos = null;

        List<CustomerSupplyOrder> list = customerSupplyOrderRepository.getByReceiptId(receiptId);


        CustomerSupplyOrderDrugListDto[] dtos = mapper.map(list, CustomerSupplyOrderDrugListDto[].class);

        listDtos = Arrays.asList(dtos);

        return listDtos;
    }

    //satış fişinin içeriği
    public List<ReceiptRefundListDto> getByRefundId(Long receiptId) {
        List<ReceiptRefundListDto> listDtos = null;

        List<Refund> list = refundRepository.getByRefundId(receiptId);

        ReceiptRefundListDto[] dtos = mapper.map(list, ReceiptRefundListDto[].class);

        listDtos = Arrays.asList(dtos);

        return listDtos;
    }

    public List<ReceiptListDto> searchReceiptRefund(ReceiptSearchDto dto) {
//        List<ReceiptRefundListDto> list = new ArrayList<>();
        StringBuilder createSqlQuery = new StringBuilder("select * from receipt r where status=1 ");
        createSqlQuery.append(" and receipt_type = '" + ReceiptType.SATIS + "' ");

        if (dto.getReceiptStatusId() != null)
            createSqlQuery.append(" and receipt_status = " + dto.getReceiptStatusId());
        if (dto.getSupplierId() != null)
            createSqlQuery.append(" and supplier_id = " + dto.getSupplierId());
        if (dto.getInvoiceNo() != null && dto.getInvoiceNo().trim().length() > 0)
            createSqlQuery.append(" and invoice_no ILIKE '%" + dto.getInvoiceNo().trim() + "%' ");
        if (dto.getReceiptNo() != null && dto.getReceiptNo().trim().length() > 0)
            createSqlQuery.append(" and receipt_no ILIKE '%" + dto.getReceiptNo().trim() + "%' ");
        if (dto.getCreatedAt() != null) {
            createSqlQuery.append(" and r.created_at >=  to_date('" + sdf.format(dto.getCreatedAt()) + "'," + "'dd/MM/yyyy') ");
        }


        createSqlQuery.append(" order by receipt_id ");

        System.out.println(createSqlQuery);

        List<Receipt> list = entityManager.createNativeQuery(createSqlQuery.toString(), Receipt.class).getResultList();
        ReceiptListDto[] dtos = mapper.map(list, ReceiptListDto[].class);
        List<ReceiptListDto> listDtos = Arrays.asList(dtos);

        int index = 0;
        for (Receipt receipt : list) {
            Long totality = 0l;
            Double totalPrice = 0.0;
            for (Refund r : receipt.getRefunds()) {
                totality = totality + r.getTotality();
                totalPrice = totalPrice + r.getTotalPrice();
            }
            listDtos.get(index).setTotality(totality);
            listDtos.get(index).setTotalPrice(totalPrice);
            index++;
        }


        return listDtos;
    }

    public Boolean saveRefund(List<RefundCheckListDto> dtos) throws Exception {

        try {
            Optional<ReceiptStatus> receiptStatus = receiptStatusRepository.findById(1l);
            if (!receiptStatus.isPresent()) {
                throw new NotFoundException("Statü Bulunamadı");
            }
            Receipt receipt = new Receipt();
            receipt.setReceiptStatus(receiptStatus.get());
            receipt.setCreatedAt(new Date());
            receipt.setStatus(1);
            receipt = receiptRepository.save(receipt);
            receipt.setReceiptNo(this.generateReceiptNo(receipt.getReceiptId()));
            receipt = receiptRepository.save(receipt);
            Supplier supplier = null;

            for (RefundCheckListDto check : dtos) {
                if (check == null)
                    continue;
                ;
                if (check.getValue() == true) {
                    if (check.getRefundId() == null)
                        continue;
                    Optional<Refund> r = refundRepository.findById(check.getRefundId());
                    if (!r.isPresent())
                        continue;
                    Refund refund = r.get();
                    if (refund.getReceipt() != null)
                        continue;
                    refund.setReceipt(receipt);
                    supplier = refund.getSupplier();
                    refund = refundRepository.save(refund);
                    //receipt.getCustomerSupplyOrders().add(cso.get());
                }
            }

            if (supplier == null)
                throw new NotFoundException("Böyle bir tedarikçi bulunamadı");
            receipt.setSupplier(supplier);
            receipt.setReceiptType(ReceiptType.SATIS);
            receipt = receiptRepository.save(receipt);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public Boolean getByCustomerSupplyOrderId(Long customerSupplyOrderId) throws Exception {

        Optional<CustomerSupplyOrder> optResult = customerSupplyOrderRepository.getByCustomerSupplyOrderId(customerSupplyOrderId);

        if (optResult.isPresent()) {
            optResult.get().setReceipt(null);
            customerSupplyOrderRepository.save(optResult.get());
            return true;
        } else if (!optResult.isPresent()) {
            throw new Exception("Satın Alma Siparişi Bulunamadı !");
        }
        return false;
    }

    //satış fişinin içerisinden refundId silme (fişten çıkarma işlemi)
    public Boolean getByCustomerSupplyOrderSellId(Long refundId) throws Exception {

        Optional<Refund> optResult = refundRepository.getByCustomerSupplyOrderSellId(refundId);

        if (optResult.isPresent()) {
            optResult.get().setReceipt(null);
            refundRepository.save(optResult.get());
            return true;
        } else if (!optResult.isPresent()) {
            throw new Exception("Satın Alma Siparişi Bulunamadı !");
        }
        return false;
    }

    public List<CustomerSupplyOrderDrugListDto> getByOrder(Long supplierId,Long otherCompanyId) {

        List<CustomerSupplyOrderDrugListDto> listDtos = null;
        List<CustomerSupplyOrder> list = customerSupplyOrderRepository.getByOrder(supplierId,otherCompanyId);
        CustomerSupplyOrderDrugListDto[] dtos = mapper.map(list, CustomerSupplyOrderDrugListDto[].class);
        listDtos = Arrays.asList(dtos);

        return listDtos;
    }

    //satış fişine iade ekleme
    public List<CustomerSupplyOrderDrugListSellDto> getByOrderRefund(Long receiptId) {
        List<CustomerSupplyOrderDrugListSellDto> listDtos = null;

        List<Refund> list = refundRepository.getByOrderRefund(receiptId);


        CustomerSupplyOrderDrugListSellDto[] dtos = mapper.map(list, CustomerSupplyOrderDrugListSellDto[].class);


        listDtos = Arrays.asList(dtos);

        return listDtos;
    }

    public Boolean saveAddOrder(List<ReceiptSaveAddOrderDto> dtos) throws Exception {

        if (dtos == null)
            return false;
        if (dtos.size() < 1)
            return false;
        try {


            for (ReceiptSaveAddOrderDto check : dtos) {
                if (check == null)
                    continue;

                if (check.getValue()) {
                    Optional<Receipt> opt = receiptRepository.findById(check.getReceiptId());
                    if (!opt.isPresent()) {
                        throw new Exception("Fiş Bulunamadı.");
                    }

                    Optional<CustomerSupplyOrder> optCso = customerSupplyOrderRepository.findById(check.getCustomerSupplyOrderId());
                    if (!optCso.isPresent()) {
                        throw new Exception("Satın Alma Bulunamadı.");
                    }
                    Receipt receipt = opt.get();
                    CustomerSupplyOrder customerSupplyOrder = optCso.get();
                    customerSupplyOrder.setReceipt(receipt);
                    customerSupplyOrder = customerSupplyOrderRepository.save(customerSupplyOrder);
//                    receipt.getCustomerSupplyOrders().add(customerSupplyOrder);
//                    receiptRepository.save(receipt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public Boolean saveAddOrderRefund(List<RefundCheckListDto> dtos) throws Exception {

        if (dtos == null)
            return false;
        if (dtos.size() < 1)
            return false;
        try {


            for (RefundCheckListDto check : dtos) {
                if (check == null)
                    continue;

                if (check.getValue()) {
                    Optional<Receipt> opt = receiptRepository.findById(check.getReceiptId());
                    if (!opt.isPresent()) {
                        throw new Exception("Fiş Bulunamadı.");
                    }

                    Optional<Refund> optRf = refundRepository.findById(check.getRefundId());
                    if (!optRf.isPresent()) {
                        throw new Exception("Satın Alma Bulunamadı.");
                    }
                    Receipt receipt = opt.get();
                    Refund refund = optRf.get();
                    refund.setReceipt(receipt);
                    refund = refundRepository.save(refund);
//                    receipt.getCustomerSupplyOrders().add(customerSupplyOrder);
//                    receiptRepository.save(receipt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    public String createBuyReceiptExcel(String authHeader, Long receiptId) throws Exception {

        try {
            Optional<Object> result = null;
            User user = this.getUserFromToken(authHeader);
            Optional<Receipt> optionalReceipt = receiptRepository.findById(receiptId);
            if (!optionalReceipt.isPresent())
                throw new Exception("Böyle bir fiş yoktur");

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Sayfa1");

            //STYLE BAŞLANGIÇ
            CellStyle csBody = workbook.createCellStyle();
            XSSFFont fontBody = workbook.createFont();
            fontBody.setFontName("Times New Roman");
            fontBody.setFontHeightInPoints((short) 7);
            csBody.setWrapText(true);
            csBody.setLocked(false);
            csBody.setFont(fontBody);
            //STYLE SON

            addExcelHeader(workbook, sheet);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            int a = 5;
            int b = 0;
            for (CustomerSupplyOrder cso : optionalReceipt.get().getCustomerSupplyOrders()) {
                a++;
                b++;
                XSSFRow row = sheet.createRow((short) a);
                row.createCell(0).setCellValue(b);
                row.getCell(0).setCellStyle(csBody);
                //İçerik Kısmı Bu Satırdan İtibaren Obje Bilgileri İle Doldurulur
                row.createCell(1).setCellValue(cso.getSupplyOrderNo());
                row.getCell(1).setCellStyle(csBody);
                row.createCell(2).setCellValue(cso.getDrugCard().getDrugName());
                row.getCell(2).setCellStyle(csBody);
                row.createCell(3).setCellValue(cso.getTotalQuantity() + " Adet");
                row.getCell(3).setCellStyle(csBody);
                Double totalPrice = ((double) ((int) (cso.getTotalPrice() * 1000.0))) / 1000.0;
                row.createCell(4).setCellValue(totalPrice.toString() + " TL");
                row.getCell(4).setCellStyle(csBody);
                row.createCell(5).setCellValue(dateFormat.format(cso.getCreatedAt()));
                row.getCell(5).setCellStyle(csBody);

            }

            FileOutputStream fileOut = new FileOutputStream(
                    "docs/" + optionalReceipt.get().getReceiptNo() + "-" + user.getUserId() + ".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            String fileName = optionalReceipt.get().getReceiptNo() + "-" + user.getUserId();

            return fileName;
        } catch (Exception e) {
            throw new Exception("Fiş Excel Oluşturma İşleminde Hata Oluştu.", e);
        }
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
//        csHeading.setFillForegroundColor(IndexedColors.BLUE_GREY.index);

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
        rowDate.createCell(5).setCellValue(dateFormat.format(new Date()));
        rowDate.getCell(5).setCellStyle(csDate);


        InputStream inputStream = new FileInputStream("image/logo/pharma.png");
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
        rowHeader.createCell(0).setCellValue("Fiş Bilgileri");
        rowHeader.getCell(0).setCellStyle(csHeading);

        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 5));
        //son parametre kolon sayısına eşit olmalı
//        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 9));


        XSSFRow rowhead = sheet.createRow((short) 5);

        //Kolon İsimleri Belirtilir İhtiyaç Olursa Kolonlar Genişletilir

        rowhead.createCell(0).setCellValue("No");
        rowhead.getCell(0).setCellStyle(csHeader);
        sheet.setColumnWidth(0, 1500);//kolon genişletildi
        //Kolon İsimleri Belirtilir İhtiyaç Olursa Kolonlar Genişletilir
        rowhead.createCell(1).setCellValue("Satın Alma No");
        rowhead.getCell(1).setCellStyle(csHeader);
        sheet.setColumnWidth(1, 4500);//kolon genişletildi
        rowhead.createCell(2).setCellValue("İlaç Adı");
        rowhead.getCell(2).setCellStyle(csHeader);
        sheet.setColumnWidth(2, 5500);
        rowhead.createCell(3).setCellValue("Toplam Adet");
        rowhead.getCell(3).setCellStyle(csHeader);
        sheet.setColumnWidth(3, 3000);
        rowhead.createCell(4).setCellValue("Toplam Tutar");
        rowhead.getCell(4).setCellStyle(csHeader);
        sheet.setColumnWidth(4, 4000);
        rowhead.createCell(5).setCellValue("Kabul Tarihi");
        rowhead.getCell(5).setCellStyle(csHeader);
        sheet.setColumnWidth(5, 4000);

        //A4 Sayfaya Sığdırma
        sheet.setFitToPage(true);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);

    }

    public String createBuyReceiptPdf(String authHeader, Long receiptId) throws Exception {

        try {
            /* Belge sonuna kullanıcı Id'si eklmek için kullanıcı bilgisi alınıyor */
            User user = controlService.getUserFromToken(authHeader);
            Optional<Receipt> optionalReceipt = receiptRepository.findById(receiptId);

            //PDF BAŞLANGIÇ
            String fileName = optionalReceipt.get().getReceiptNo() + "-" + user.getUserId() + ".pdf";
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
            com.itextpdf.text.Font catFont = new com.itextpdf.text.Font(bf, 16, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);

            //Tablonun Başlığı Girilir
            Paragraph tableHeader = new Paragraph("Fiş Bilgisi", catFont);

            tableHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(tableHeader);

            document.add(new Paragraph("\n"));

            //Tablonun Sütun Sayısı Girilir
            PdfPTable table = new PdfPTable(6);
            table.setWidths(new int[]{2, 6, 4, 3, 3, 4});

            table.setWidthPercentage(100);
            addTableHeader(table);

            //Hücrelere Veriler Girilir
            int a = 0;
            for (CustomerSupplyOrder cso : optionalReceipt.get().getCustomerSupplyOrders()) {
                a++;
                addRows(table, String.valueOf(a));
                addRows(table, cso.getSupplyOrderNo());
                addRows(table, cso.getDrugCard().getDrugName());
                addRows(table, cso.getTotalQuantity().toString() + " Adet");
                Double totalPrice = ((double) ((int) (cso.getTotalPrice() * 1000.0))) / 1000.0;
                addRows2(table, totalPrice.toString() + " TL");
                addRows(table, dateFormat.format(cso.getCreatedAt()));

            }


            document.add(table);
            document.close();
            //PDF SON
            return fileName;

        } catch (Exception e) {
            throw new Exception("Fiş Pdf Oluşturma İşleminde Hata Oluştu.", e);
        }


    }

    private void addTableHeader(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        com.itextpdf.text.Font catFont = new com.itextpdf.text.Font(bf, 10, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);

        //Tablo Sütün Başlıkları Girilir
        Stream.of("No", "Satın Alma No", "İlaç Adı", "Toplam Adet", "Toplam Tutar", "Kabul Tarihi")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(1);
                    header.setPhrase(new Phrase(columnTitle, catFont));
                    header.setPadding(3);


                    table.addCell(header);

                });
    }

    private void addRows(PdfPTable table, String value) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        com.itextpdf.text.Font catFont = new com.itextpdf.text.Font(bf, 8, Font.NORMAL, BaseColor.BLACK);


        table.addCell(new Phrase(value, catFont));


    }

    public String createSellReceiptExcel(String authHeader, Long receiptId) throws Exception {

        try {
            Optional<Object> result = null;
            User user = this.getUserFromToken(authHeader);
            Optional<Receipt> optionalReceipt = receiptRepository.findById(receiptId);
            if (!optionalReceipt.isPresent())
                throw new Exception("Böyle bir fiş yoktur");

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Sayfa1");

            //STYLE BAŞLANGIÇ
            CellStyle csBody = workbook.createCellStyle();
            XSSFFont fontBody = workbook.createFont();
            fontBody.setFontName("Times New Roman");
            fontBody.setFontHeightInPoints((short) 7);
            csBody.setWrapText(true);
            csBody.setLocked(false);
            csBody.setFont(fontBody);
            //STYLE SON

            addExcelHeader2(workbook, sheet);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            int a = 5;
            int b = 0;
            for (Refund r : optionalReceipt.get().getRefunds()) {
                a++;
                b++;
                XSSFRow row = sheet.createRow((short) a);
                row.createCell(0).setCellValue(b);
                row.getCell(0).setCellStyle(csBody);
                //İçerik Kısmı Bu Satırdan İtibaren Obje Bilgileri İle Doldurulur
                row.createCell(1).setCellValue(r.getRefundOrderNo());
                row.getCell(1).setCellStyle(csBody);
                row.createCell(2).setCellValue(r.getDrugCard().getDrugName());
                row.getCell(2).setCellStyle(csBody);
                row.createCell(3).setCellValue(r.getTotality() + " Adet");
                row.getCell(3).setCellStyle(csBody);
                Double totalPrice = ((double) ((int) (r.getTotalPrice() * 1000.0))) / 1000.0;
                row.createCell(4).setCellValue(totalPrice.toString() + " TL");
                row.getCell(4).setCellStyle(csBody);
                row.createCell(5).setCellValue(dateFormat.format(r.getCreatedAt()));
                row.getCell(5).setCellStyle(csBody);

            }

            FileOutputStream fileOut = new FileOutputStream(
                    "docs/" + optionalReceipt.get().getReceiptNo() + "-" + user.getUserId() + ".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            String fileName = optionalReceipt.get().getReceiptNo() + "-" + user.getUserId();

            return fileName;
        } catch (Exception e) {
            throw new Exception("Fiş Excel Oluşturma İşleminde Hata Oluştu.", e);
        }
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
//        csHeading.setFillForegroundColor(IndexedColors.BLUE_GREY.index);

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
        rowDate.createCell(5).setCellValue(dateFormat.format(new Date()));
        rowDate.getCell(5).setCellStyle(csDate);


        InputStream inputStream = new FileInputStream("image/logo/pharma.png");
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
        rowHeader.createCell(0).setCellValue("Fiş Bilgileri");
        rowHeader.getCell(0).setCellStyle(csHeading);

        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 5));
        //son parametre kolon sayısına eşit olmalı
//        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 9));


        XSSFRow rowhead = sheet.createRow((short) 5);

        //Kolon İsimleri Belirtilir İhtiyaç Olursa Kolonlar Genişletilir

        rowhead.createCell(0).setCellValue("No");
        rowhead.getCell(0).setCellStyle(csHeader);
        sheet.setColumnWidth(0, 1500);//kolon genişletildi
        //Kolon İsimleri Belirtilir İhtiyaç Olursa Kolonlar Genişletilir
        rowhead.createCell(1).setCellValue("Satın Alma No");
        rowhead.getCell(1).setCellStyle(csHeader);
        sheet.setColumnWidth(1, 4500);//kolon genişletildi
        rowhead.createCell(2).setCellValue("İlaç Adı");
        rowhead.getCell(2).setCellStyle(csHeader);
        sheet.setColumnWidth(2, 5500);
        rowhead.createCell(3).setCellValue("Toplam Adet");
        rowhead.getCell(3).setCellStyle(csHeader);
        sheet.setColumnWidth(3, 3000);
        rowhead.createCell(4).setCellValue("Toplam Tutar");
        rowhead.getCell(4).setCellStyle(csHeader);
        sheet.setColumnWidth(4, 4000);
        rowhead.createCell(5).setCellValue("Kabul Tarihi");
        rowhead.getCell(5).setCellStyle(csHeader);
        sheet.setColumnWidth(5, 4000);

        //A4 Sayfaya Sığdırma
        sheet.setFitToPage(true);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);

    }

    public String createSellReceiptPdf(String authHeader, Long receiptId) throws Exception {

        try {
            /* Belge sonuna kullanıcı Id'si eklmek için kullanıcı bilgisi alınıyor */
            User user = controlService.getUserFromToken(authHeader);
            Optional<Receipt> optionalReceipt = receiptRepository.findById(receiptId);

            //PDF BAŞLANGIÇ
            String fileName = optionalReceipt.get().getReceiptNo() + "-" + user.getUserId() + ".pdf";
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
            com.itextpdf.text.Font catFont = new com.itextpdf.text.Font(bf, 16, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);

            //Tablonun Başlığı Girilir
            Paragraph tableHeader = new Paragraph("Fiş Bilgisi", catFont);

            tableHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(tableHeader);

            document.add(new Paragraph("\n"));

            //Tablonun Sütun Sayısı Girilir
            PdfPTable table = new PdfPTable(6);
            table.setWidths(new int[]{2, 6, 4, 3, 3, 4});

            table.setWidthPercentage(100);
            addTableHeader2(table);

            //Hücrelere Veriler Girilir
            int a = 0;
            for (Refund r : optionalReceipt.get().getRefunds()) {
                a++;
                addRows2(table, String.valueOf(a));
                addRows2(table, r.getRefundOrderNo());
                addRows2(table, r.getDrugCard().getDrugName());
                addRows2(table, r.getTotality().toString() + " Adet");
                Double totalPrice = ((double) ((int) (r.getTotalPrice() * 1000.0))) / 1000.0;
                addRows2(table, totalPrice.toString() + " TL");
                addRows2(table, dateFormat.format(r.getCreatedAt()));

            }


            document.add(table);
            document.close();
            //PDF SON
            return fileName;

        } catch (Exception e) {
            throw new Exception("Fiş Pdf Oluşturma İşleminde Hata Oluştu.", e);
        }


    }

    private void addTableHeader2(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        com.itextpdf.text.Font catFont = new com.itextpdf.text.Font(bf, 10, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);

        //Tablo Sütün Başlıkları Girilir
        Stream.of("No", "İade No", "İlaç Adı", "Toplam Adet", "Toplam Tutar", "Kabul Tarihi")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(1);
                    header.setPhrase(new Phrase(columnTitle, catFont));
                    header.setPadding(3);


                    table.addCell(header);

                });
    }

    private void addRows2(PdfPTable table, String value) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        com.itextpdf.text.Font catFont = new com.itextpdf.text.Font(bf, 8, Font.NORMAL, BaseColor.BLACK);


        table.addCell(new Phrase(value, catFont));


    }


    public Page<FinalReceiptReceiptDto> searchReceiptWithFinalReceipt(Pageable page, ReceiptSearchDto dto) {

        StringBuilder createSqlQuery = new StringBuilder(" select * from receipt r ");
        createSqlQuery.append(" where r.final_receipt_id is not null   ");
        // and receipt_type = 'ALIS'
        if (dto.getSupplierId() != null)
            createSqlQuery.append(" and r.supplier_id =" + dto.getSupplierId() + " ");

        if (dto.getReceiptNo() != null)
            createSqlQuery.append(" and r.receipt_no ILIKE '%" + dto.getReceiptNo().trim() + "%' ");

//        if(dto.getCreatedAt() != null)	createSqlQuery.append(" and  r.created_at ="+dto.getCreatedAt()+" ");

        if (dto.getCreatedAt() != null)
            createSqlQuery.append(" and r.created_at >=  to_date('" + sdf.format(dto.getCreatedAt()) + "'," + "'dd/MM/yyyy') ");


        List<Receipt> list = entityManager.createNativeQuery(createSqlQuery.toString(), Receipt.class).getResultList();

        FinalReceiptReceiptDto[] dtos = mapper.map(list, FinalReceiptReceiptDto[].class);
        List<FinalReceiptReceiptDto> dtosList = Arrays.asList(dtos);

        int start = Math.min((int) page.getOffset(), dtosList.size());
        int end = Math.min((start + page.getPageSize()), dtosList.size());

        Page<FinalReceiptReceiptDto> pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());

        return pageList;

    }

    public Boolean sendToAdminForApprove(Long receiptId) throws Exception {

        Optional<ReceiptStatus> receiptStatus = receiptStatusRepository.findById(10l);
        if (!receiptStatus.isPresent()) {
            throw new NotFoundException("Statü Bulunamadı");
        }
        if (receiptId == null)
            return false;
        Optional<Receipt> receiptOptional = receiptRepository.findById(receiptId);
        if (!receiptOptional.isPresent()) {
            throw new NotFoundException("Fiş Bulunamadı");
        }

        Receipt receipt = receiptOptional.get();
        receipt.setReceiptStatus(receiptStatus.get());
        receipt = receiptRepository.save(receipt);

        return true;
    }

    public List<ReceiptListDto> getAllReceiptForAdmin() throws Exception {

        List<Receipt> list = receiptRepository.getAllReceiptForAdmin();
        ReceiptListDto[] dtos = mapper.map(list, ReceiptListDto[].class);
        List<ReceiptListDto> listDtos = Arrays.asList(dtos);

        int index = 0;
        for (Receipt receipt : list) {
            Long totality = 0l;
            Double totalPrice = 0.0;
            Double totalPriceWithVat = 0.0;
            if (receipt.getReceiptType() == ReceiptType.ALIS) {
                for (CustomerSupplyOrder cso : receipt.getCustomerSupplyOrders()) {
                    totality = totality + cso.getTotality();
                    totalPrice = totalPrice + cso.getTotalPrice();
                    if (cso.getDrugCard().getDrugVat() != null)
                        totalPriceWithVat += (cso.getTotalPrice() + (cso.getTotalPrice() * cso.getDrugCard().getDrugVat() / 100));
                }
            }
            if (receipt.getReceiptType() == ReceiptType.SATIS) {
                for (Refund cso : receipt.getRefunds()) {
                    totality = totality + cso.getTotality();
                    totalPrice = totalPrice + cso.getTotalPrice();
                    if (cso.getDrugCard().getDrugVat() != null)
                        totalPriceWithVat += (cso.getTotalPrice() + (cso.getTotalPrice() * cso.getDrugCard().getDrugVat() / 100));
                }
            }
            listDtos.get(index).setTotality(totality);
            listDtos.get(index).setTotalPriceWithVat(totalPriceWithVat);
            listDtos.get(index).setTotalPrice(totalPrice);
            index++;
        }

        return listDtos;
    }

    public Boolean sendBackToWarehouseman(Long receiptId) throws Exception {

        Optional<ReceiptStatus> receiptStatus = receiptStatusRepository.findById(1l);
        if (!receiptStatus.isPresent()) {
            throw new NotFoundException("Statü Bulunamadı");
        }
        if (receiptId == null)
            return false;
        Optional<Receipt> receiptOptional = receiptRepository.findById(receiptId);
        if (!receiptOptional.isPresent()) {
            throw new NotFoundException("Fiş Bulunamadı");
        }

        Receipt receipt = receiptOptional.get();
        receipt.setReceiptStatus(receiptStatus.get());
        receipt = receiptRepository.save(receipt);

        return true;
    }

    public Boolean sendToManagerApprove(Long receiptId) throws Exception {

        Optional<ReceiptStatus> receiptStatus = receiptStatusRepository.findById(20l);
        if (!receiptStatus.isPresent()) {
            throw new NotFoundException("Statü Bulunamadı");
        }
        if (receiptId == null)
            return false;
        Optional<Receipt> receiptOptional = receiptRepository.findById(receiptId);
        if (!receiptOptional.isPresent()) {
            throw new NotFoundException("Fiş Bulunamadı");
        }

        Receipt receipt = receiptOptional.get();
        receipt.setReceiptStatus(receiptStatus.get());
        receipt = receiptRepository.save(receipt);

        return true;
    }

    public List<ReceiptListDto> getAllReceiptForManager() throws Exception {

        List<Receipt> list = receiptRepository.getAllReceiptForManager();
        ReceiptListDto[] dtos = mapper.map(list, ReceiptListDto[].class);
        List<ReceiptListDto> listDtos = Arrays.asList(dtos);

        int index = 0;
        for (Receipt receipt : list) {
            Long totality = 0l;
            Double totalPrice = 0.0;
            Double totalPriceWithVat = 0.0;
            if (receipt.getReceiptType() == ReceiptType.ALIS) {
                for (CustomerSupplyOrder cso : receipt.getCustomerSupplyOrders()) {
                    totality = totality + cso.getTotality();
                    totalPrice = totalPrice + cso.getTotalPrice();
                    if (cso.getDrugCard().getDrugVat() != null)
                        totalPriceWithVat += (cso.getTotalPrice() + (cso.getTotalPrice() * cso.getDrugCard().getDrugVat() / 100));
                }
            }
            if (receipt.getReceiptType() == ReceiptType.SATIS) {
                for (Refund cso : receipt.getRefunds()) {
                    totality = totality + cso.getTotality();
                    totalPrice = totalPrice + cso.getTotalPrice();
                    if (cso.getDrugCard().getDrugVat() != null)
                        totalPriceWithVat += (cso.getTotalPrice() + (cso.getTotalPrice() * cso.getDrugCard().getDrugVat() / 100));
                }
            }
            listDtos.get(index).setTotality(totality);
            listDtos.get(index).setTotalPriceWithVat(totalPriceWithVat);
            listDtos.get(index).setTotalPrice(totalPrice);
            index++;
        }

        return listDtos;
    }

    public Boolean sendBackToAdmin(Long receiptId) throws Exception {

        Optional<ReceiptStatus> receiptStatus = receiptStatusRepository.findById(10l);
        if (!receiptStatus.isPresent()) {
            throw new NotFoundException("Statü Bulunamadı");
        }
        if (receiptId == null)
            return false;
        Optional<Receipt> receiptOptional = receiptRepository.findById(receiptId);
        if (!receiptOptional.isPresent()) {
            throw new NotFoundException("Fiş Bulunamadı");
        }

        Receipt receipt = receiptOptional.get();
        receipt.setReceiptStatus(receiptStatus.get());
        receipt = receiptRepository.save(receipt);

        return true;
    }

    public Boolean sendToAccountingApprove(Long receiptId) throws Exception {

        Optional<ReceiptStatus> receiptStatus = receiptStatusRepository.findById(30l);
        if (!receiptStatus.isPresent()) {
            throw new NotFoundException("Statü Bulunamadı");
        }
        if (receiptId == null)
            return false;
        Optional<Receipt> receiptOptional = receiptRepository.findById(receiptId);
        if (!receiptOptional.isPresent()) {
            throw new NotFoundException("Fiş Bulunamadı");
        }

        Receipt receipt = receiptOptional.get();
        receipt.setReceiptStatus(receiptStatus.get());
        receipt = receiptRepository.save(receipt);

        return true;
    }

    public List<ReceiptListDto> getAllReceiptForAccounting(Long checkingCardId, Long otherCompanyId) throws Exception {

        if (checkingCardId == null) {
            throw new Exception("Cari Bulunamadı");
        }
        Optional<CheckingCard> checkingCard = checkingCardRepository.findById(checkingCardId);
        if (!checkingCard.isPresent())
            throw new Exception("Cari Bulunamadı");

        if (checkingCard.get().getSupplierId() == null) {
            throw new Exception("Tedarikçi Bulunamadı");
        }

        Optional<Supplier> optionalSupplier = supplierRepository.findById(checkingCard.get().getSupplierId());
        if (!optionalSupplier.isPresent())
            throw new Exception("Tedarikçi Bulunamadı");

        List<Receipt> list = receiptRepository.getAllReceiptForAccounting(optionalSupplier.get().getSupplierId(), otherCompanyId);
        ReceiptListDto[] dtos = mapper.map(list, ReceiptListDto[].class);
        List<ReceiptListDto> listDtos = Arrays.asList(dtos);

        int index = 0;
        for (Receipt receipt : list) {
            Long totality = 0l;
            Double totalPrice = 0.0;
            Double totalPriceWithVat = 0.0;
            for (CustomerSupplyOrder cso : receipt.getCustomerSupplyOrders()) {
                totality = totality + cso.getTotality();
                totalPrice = totalPrice + cso.getTotalPrice();
                if (cso.getDrugCard().getStatus() == 1)
                    totalPriceWithVat += (cso.getTotalPrice() + (cso.getTotalPrice() * 8 / 100));
                if (cso.getDrugCard().getStatus() == 2) {
                    if (cso.getDrugCard().getDrugVat() != null)
                        totalPriceWithVat += (cso.getTotalPrice() + (cso.getTotalPrice() * cso.getDrugCard().getDrugVat() / 100));
                    else
                        totalPriceWithVat += cso.getTotalPrice();
                }
            }
            listDtos.get(index).setTotality(totality);
            listDtos.get(index).setTotalPriceWithVat(totalPriceWithVat);
            listDtos.get(index).setTotalPrice(totalPrice);
            index++;
        }

        return listDtos;
    }

    public String createBuyInvoice(String authHeader, Long receiptId) throws Exception {
        Boolean controlSupplierType = false;

        User user = this.controlService.getUserFromToken(authHeader);

        Optional<ReceiptStatus> receiptStatus = receiptStatusRepository.findById(40l);
        if (!receiptStatus.isPresent()) {
            throw new NotFoundException("Statü Bulunamadı");
        }

        if (receiptId == null)
            throw new NotFoundException("Fiş Id'si Bulunamadı !");

        Optional<Receipt> receiptOptional = receiptRepository.findById(receiptId);
        if (!receiptOptional.isPresent()) {
            throw new NotFoundException("Fiş Bulunamadı");
        }

//        Optional<InvoiceStatus> optionalInvoiceStatus = invoiceStatusRepository.findById(1l);
//        if(!optionalInvoiceStatus.isPresent()){
//            throw new NotFoundException("Fatura Statüsü Bulunamadı");
//        }



        Receipt receipt = receiptOptional.get();

        //receipt = receiptRepository.save(receipt);

        if (receipt.getSupplier() == null)
            throw new NotFoundException("Tedarikçi Bulunamadı");

        Optional<CheckingCard> checkingCardOptional = checkingCardRepository.findBySupplierId(receipt.getSupplier().getSupplierId());
        if (!checkingCardOptional.isPresent()) {
            throw new NotFoundException("Tedarikçi Bulunamadı..");
        }

        Optional<OtherCompany> optOtherCompany=otherCompanyRepository.findById(receipt.getCustomerSupplyOrders().get(0).getOtherCompanyId());
        if(!optOtherCompany.isPresent() && optOtherCompany.get().getCheckingCard()!=null){
            throw new NotFoundException("Cari Bilgisi Bulunamadı !");
        }
        Optional<CheckingCard> checkingCardOpt = checkingCardRepository.findById(optOtherCompany.get().getCheckingCard().getCheckingCardId());
        if (!checkingCardOpt.isPresent()) {
            throw new NotFoundException("Cari Kart Bulunamadı");
        }

        Optional<InvoiceType> invoiceTypeOpt = invoiceTypeRepository.findById(1l);
        if (!invoiceTypeOpt.isPresent()) {
            throw new NotFoundException("Fatura Tipi Bulunamadı");
        }
        if (receipt.getInvoice() != null) {
            throw new Exception("Daha Önce Fatura Oluşturulmuştur");
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
        invoice.setCheckingCard(checkingCardOptional.get());
        invoice.setOtherCheckingCard(checkingCardOpt.get());

//hangi firma için oluşturuldu onun bilgisi eklendi
        if (receipt.getCustomerSupplyOrders().size() > 0) {
            invoice.setOtherCompanyId(receipt.getCustomerSupplyOrders().get(0).getOtherCompanyId());
        }


        List<Category> list120 = categoryRepository.searchTo120CheckingCardId(invoice.getOtherCheckingCard().getCheckingCardId());
        List<Category> list320 = categoryRepository.searchTo320CheckingCardId(invoice.getCheckingCard().getCheckingCardId());

        if (list120.size() != 1 || list320.size() != 1) {
            throw new Exception("120 - 320 Muhasebe Kodları Bulunamadı !");
        }


        invoice.setCategory(list120.get(0));
        invoice.setOtherCategory(list320.get(0));

        invoice = invoiceRepository.save(invoice);

        receipt.setReceiptStatus(receiptStatus.get());
        //receipt = receiptRepository.save(receipt);

        invoice.setInvoiceNo(receipt.getInvoiceNo());
        invoice.setInvoiceDate(receipt.getInvoiceDate());
        invoice.setReceipt(receipt);
        if (checkingCardOptional.get().getTaxIdentificationNumber() != null && checkingCardOptional.get().getTaxIdentificationNumber().toString().trim().length() > 0) {
            invoice.setTaxNo(checkingCardOptional.get().getTaxIdentificationNumber().toString());
        }
        if (checkingCardOptional.get().getTaxOffice() != null && checkingCardOptional.get().getTaxOffice().trim().length() > 0) {
            invoice.setTaxOffice(checkingCardOptional.get().getTaxOffice());
        }
        invoice.setCrsNo(checkingCardOpt.get().getCrsNo());
        //invoice.setInvoiceStatus(optionalInvoiceStatus.get());
        invoice.setInvoiceType(invoiceTypeOpt.get());
        Double accountTotalPrice = 0.0, accountTotalPriceLiva = 0.0, totalPrice = 0.0, chargeLiva = 0.0;
        controlSupplierType = receipt.getSupplier().getSupplierType() == SupplierType.PHARMACY ? true : false;
        if (receipt.getReceiptType() == ReceiptType.ALIS) {
            invoice.setInvoicePurpose(InvoicePurpose.BUY_INVOICE);
//            invoice.setCheckingCard(checkingCardOptional.get());
//            invoice.setOtherCheckingCard(checkingCardOpt.get());
            for (CustomerSupplyOrder cso : receipt.getCustomerSupplyOrders()) {
                Double newUnitPrice = 0D;
                //tedarikçi depo veya eczane ise
                if (cso.getSupplier().getSupplierType().equals(SupplierType.PHARMACY) || cso.getSupplier().getSupplierType().equals(SupplierType.WAREHOUSE)) {
                    //--- supply price kayıtları ekleniyor ---
                    newUnitPrice = Double.valueOf(cso.getUnitPrice() + ((cso.getUnitPrice() * cso.getSupplierProfit()) / 100.0));

                    Double vat = cso.getDrugCard().getDrugVat();
                    Double vatSum = cso.getTotality() * (newUnitPrice * (vat * 0.01));
                    //accountTotalPrice KDV SİZ HALİ
                    Double newAccountTotalPrice = cso.getTotality() * newUnitPrice;

                    SupplyOrderPrice supplyOrderPrice = new SupplyOrderPrice();
                    supplyOrderPrice.setVat(vat);
                    supplyOrderPrice.setVatSum(vatSum);
                    supplyOrderPrice.setAccountTotalPrice(newAccountTotalPrice);
                    supplyOrderPrice.setSupplyOrder(cso);
                    supplyOrderPrice = supplyOrderPriceRepository.save(supplyOrderPrice);

//                    //eczanenin livaya satış fiyatı
//                    chargeLiva += supplyOrderPrice.getAccountTotalPrice() + supplyOrderPrice.getVatSum();
//                    invoice.setChargeLiva(chargeLiva);

                    //--- price liva kayıtları ekleniyor ---
//                    newUnitPrice = 0D;
//                    if (cso.getSupplier().getSupplierType().equals(SupplierType.PHARMACY) || cso.getSupplier().getSupplierType().equals(SupplierType.WAREHOUSE)) {
//                        newUnitPrice = Double.valueOf(cso.getUnitPrice() + ((cso.getUnitPrice() * cso.getSupplierProfit()) / 100.0));
//                        newUnitPrice = newUnitPrice + (newUnitPrice * (0.02));
//                    }
//
//                    vat = cso.getDrugCard().getDrugVat();
//                    vatSum = cso.getTotality() * (newUnitPrice * (vat * 0.01));
//                    //accountTotalPrice KDV SİZ HALİ
//                    newAccountTotalPrice = cso.getTotality() * newUnitPrice;


//                    SupplyOrderPriceLiva supplyOrderPriceLiva = new SupplyOrderPriceLiva();
//                    supplyOrderPriceLiva.setVat(vat);
//                    supplyOrderPriceLiva.setVatSum(vatSum);
//                    supplyOrderPriceLiva.setAccountTotalPrice(newAccountTotalPrice);
//                    supplyOrderPriceLiva.setSupplyOrder(cso);
//                    supplyOrderPriceLiva = supplyOrderPriceLivaRepository.save(supplyOrderPriceLiva);

                    //toplam fatura tutarı kdv dahil
                    accountTotalPrice = accountTotalPrice + (newAccountTotalPrice + vatSum);

                    Double accountTopLiva = ((double) ((int) (accountTotalPrice * 1000.0))) / 1000.0;
                    invoice.setTotalPriceLiva(accountTopLiva);
                    invoice.setTotalPriceCurrencyLiva(accountTopLiva / invoice.getCurrencyFee());


                    Double accountTop = ((double) ((int) (accountTotalPrice * 1000.0))) / 1000.0;
                    invoice.setTotalPrice(accountTop);
                    invoice.setTotalPriceCurrency(accountTop / invoice.getCurrencyFee());

                } else {
                    //tedarikçi depo ise
                    newUnitPrice = Double.valueOf(cso.getUnitPrice());

                    Double vat = cso.getDrugCard().getDrugVat();
                    Double vatSum = cso.getTotality() * (newUnitPrice * (vat * 0.01));
                    //accountTotalPrice KDV SİZ HALİ
                    Double newAccountTotalPrice = cso.getTotality() * newUnitPrice;

                    SupplyOrderPrice supplyOrderPrice = new SupplyOrderPrice();
                    supplyOrderPrice.setVat(vat);
                    supplyOrderPrice.setVatSum(vatSum);
                    supplyOrderPrice.setAccountTotalPrice(newAccountTotalPrice);
                    supplyOrderPrice.setSupplyOrder(cso);
                    supplyOrderPrice = supplyOrderPriceRepository.save(supplyOrderPrice);

                    //toplam fatura tutarı kdv dahil
                    accountTotalPrice = accountTotalPrice + (newAccountTotalPrice + vatSum);

                    Double accountTopLiva = ((double) ((int) (accountTotalPrice * 1000.0))) / 1000.0;
                    invoice.setTotalPriceLiva(accountTopLiva);
                    invoice.setTotalPriceCurrencyLiva(accountTopLiva / invoice.getCurrencyFee());

                    Double accountTop = ((double) ((int) (accountTotalPrice * 1000.0))) / 1000.0;
                    invoice.setTotalPrice(accountTop);
                    invoice.setTotalPriceCurrency(accountTop / invoice.getCurrencyFee());
               // }


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

        }

        receipt.setInvoice(invoice);
        receipt = receiptRepository.save(receipt);
        return invoice.getInvoiceNo();
    }

    public String createSellInvoice(String authHeader, Long receiptId, SellInvoiceDto dto) throws Exception {

        User user = this.controlService.getUserFromToken(authHeader);

        Optional<ReceiptStatus> receiptStatus = receiptStatusRepository.findById(40l);
        if (!receiptStatus.isPresent()) {
            throw new NotFoundException("Statü Bulunamadı");
        }

        Optional<Receipt> optionalReceipt = receiptRepository.findById(receiptId);
        if (!optionalReceipt.isPresent()) {
            throw new NotFoundException("Statü Bulunamadı");
        }
        Receipt receipt = optionalReceipt.get();
        if (receipt.getSupplier() == null)
            throw new NotFoundException("Tedarikçi Bulunamadı");

        Optional<CheckingCard> checkingCardOptional = checkingCardRepository.findById(1l);
        if (!checkingCardOptional.isPresent()) {
            throw new NotFoundException("Tedarikçi Bulunamadı..");
        }

        Optional<CheckingCard> checkingCardOpt = checkingCardRepository.findBySupplierId(receipt.getSupplier().getSupplierId());
        if (!checkingCardOpt.isPresent()) {
            throw new NotFoundException("Cari Kart Bulunamadı");
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

        List<Category> list120 = categoryRepository.searchTo120CheckingCardId(invoice.getOtherCheckingCard().getCheckingCardId());
        List<Category> list320 = categoryRepository.searchTo320CheckingCardId(invoice.getCheckingCard().getCheckingCardId());

        if (list120.size() != 1 || list320.size() != 1) {
            throw new Exception("120 - 320 Muhasebe Kodları Bulunamadı !");
        }

        invoice.setCategory(list320.get(0));
        invoice.setOtherCategory(list120.get(0));

        invoice = invoiceRepository.save(invoice);
        invoice.setInvoiceNo(this.generateInvoiceNo(invoice.getInvoiceId()));
        invoice.setReceipt(receipt);
        if (checkingCardOptional.get().getTaxIdentificationNumber() != null && checkingCardOptional.get().getTaxIdentificationNumber().toString().trim().length() > 0) {
            invoice.setTaxNo(checkingCardOptional.get().getTaxIdentificationNumber().toString());
        }
        if (checkingCardOptional.get().getTaxOffice() != null && checkingCardOptional.get().getTaxOffice().trim().length() > 0) {
            invoice.setTaxOffice(checkingCardOptional.get().getTaxOffice());
        }
        invoice.setCrsNo(checkingCardOpt.get().getCrsNo());
        invoice.setInvoiceType(invoiceTypeOpt.get());
        invoice.setCheckingCard(checkingCardOptional.get());
        invoice.setOtherCheckingCard(checkingCardOpt.get());
        invoice.setTotalPriceExpression(dto.getTotalPriceExpression());
        invoice.setPaymentTerm(dto.getPaymentTerm());
        invoice.setCrsNo(dto.getCrsNo());
        Double accountTotalPrice = 0.0, accountTotalPriceLiva = 0.0, totalPrice = 0.0;
        if (receipt.getReceiptType() == ReceiptType.SATIS) {
            invoice.setInvoicePurpose(InvoicePurpose.REFUND_SELL_INVOICE);
            for (Refund refund : receipt.getRefunds()) {
                RefundPrice refundPrice = new RefundPrice();
                //RefundPriceLiva liva = new RefundPriceLiva();

                refundPrice.setRefund(refund);
               // liva.setRefund(refund);
                if (refund.getDrugCard().getStatus() == 1) {
                    //titck daki ilaç listesi için %8 kdv
                  //  liva.setVat(8D);
                } else {
                    if (refund.getDrugCard().getDrugVat() != null) {
                        // liva.setVat(refund.getDrugCard().getDrugVat());
                    }else{

                    }
                      //  liva.setVat(18D);
                }
               // liva.setVatSum(refund.getTotalPrice() * refundPrice.getVat() / 100);
              //  liva.setAccountTotalPrice(refund.getTotalPrice() + refundPrice.getVatSum());
              //  liva = refundPriceLivaRepository.save(liva);


                //refundPrice.setVat(2D);
               // refundPrice.setVatSum(liva.getVatSum() + (liva.getVatSum() * 2 / 100));
               // refundPrice.setAccountTotalPrice(liva.getAccountTotalPrice() + (liva.getAccountTotalPrice() * 2 / 100));
               // refundPrice = refundPriceRepository.save(refundPrice);
               // accountTotalPriceLiva += liva.getAccountTotalPrice();
               // accountTotalPrice += refundPrice.getAccountTotalPrice();
            }
            Double accountTop = ((double) ((int) (accountTotalPrice * 1000.0))) / 1000.0;
            invoice.setTotalPrice(accountTop);
            invoice.setTotalPriceCurrency(accountTop / invoice.getCurrencyFee());

        }

        invoice = invoiceRepository.save(invoice);
        /* Fatura Oluşturma Bitti */

        if (invoice.getInvoicePurpose() == InvoicePurpose.REFUND_SELL_INVOICE) {


            //ekip eczanın livaya fatura etme hareketi
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
            accountActivityService.createAccountActivity(invoice, checkingCardRepository.findById(1l).get(), checkingCardRepository.findById(2l).get(), AccountActivityType.CUSTOMER_INVOICE, invoice.getTotalPriceCurrency());

            //livaya ekip eczadan fatura edilme hareketi
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
            accountActivityService.createAccountActivity(invoice, checkingCardRepository.findById(2l).get(), checkingCardRepository.findById(1l).get(), AccountActivityType.PURCHASE_INVOICE, invoice.getTotalPriceCurrency());

            //livadan eczaneye fatura etme hareketi
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
            accountActivityService.createAccountActivity(invoice, checkingCardRepository.findById(2l).get(), invoice.getOtherCheckingCard(), AccountActivityType.CUSTOMER_INVOICE, invoice.getTotalPriceCurrency());

            //eczaneye livadan fatura edilme hareketi
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
            accountActivityService.createAccountActivity(invoice, invoice.getOtherCheckingCard(), checkingCardRepository.findById(2l).get(), AccountActivityType.PURCHASE_INVOICE, invoice.getTotalPriceCurrency());
        }

        receipt.setInvoice(invoice);
        receipt = receiptRepository.save(receipt);

        return invoice.getInvoiceNo();
    }

    private String generateInvoiceNo(Long invoiceId) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String code = "FTR-" + year;
        int size = invoiceId.toString().length();
        for (int i = 0; i < 7 - size; i++)
            code += "0";
        code += invoiceId;
        return code;
    }

    public String saveBuyingİnvoiceReceiptCategories(ReceiptSaveCategoryDto dto) throws Exception {

        int counter = 0;

        if (dto.getSendReceiptCategoriesList().size() > 0) {
            //muhasebe kodu girilmiş ilaç listesi
            List<ReceiptSaveCategoryContentDto> sendReceiptCategoriesList = dto.getSendReceiptCategoriesList();

            //listeden receipt id alınıyor
            Long receiptId = null;
            for (ReceiptSaveCategoryContentDto list : sendReceiptCategoriesList) {
                if (list != null) {
                    counter++;
                    receiptId = list.getReceiptId();
                }
            }

            //siparişteki ilaç listesi
            List<CustomerSupplyOrder> drugList = customerSupplyOrderRepository.getByReceiptId(receiptId);
            if (drugList.size() != counter) {
                return "Muhasebe Kodu Belirlenmemiş İlaç Bulunmaktadır.";
            }

            for (ReceiptSaveCategoryContentDto item : sendReceiptCategoriesList) {

                if (item.getDrugCardId() != null && item.getCategoryId() != null && item.getReceiptId() != null) {
                    Optional<Receipt> optionalReceipt = receiptRepository.findById(item.getReceiptId());
                    if (!optionalReceipt.isPresent()) {
                        throw new Exception("Muhasebe Fişi Bulunamadı!");
                    }
                    Optional<DrugCard> optionalDrugCard = drugCardRepository.findById(item.getDrugCardId());
                    if (!optionalDrugCard.isPresent()) {
                        throw new Exception("İlaç Bilgisi Bulunamadı!");
                    }
                    Optional<Category> optionalCategory = categoryRepository.findById(item.getCategoryId());
                    if (!optionalCategory.isPresent()) {
                        throw new Exception("Kategori Bilgisi Bulunamadı!");
                    }

                    ReceiptContent receiptContent = new ReceiptContent();
                    receiptContent.setReceipt(optionalReceipt.get());
                    receiptContent.setDrugCard(optionalDrugCard.get());
                    receiptContent.setCategory(optionalCategory.get());
                    receiptContent.setStatus(1);
                    receiptContent.setCreatedAt(new Date());
                    receiptContent = receiptContentRepository.save(receiptContent);

                }
            }
            //aşağıdaki return front tarafında kontrol ediliyor
            return "İşlem Başarılı";
        } else {
            return "Lütfen Tüm İlaçlara Muhasebe Kodlarını Giriniz.";
        }
    }

    public Boolean deleteBuyingInvoiceReceiptCategories(Long receiptId) throws Exception {

        Optional<Receipt> optReceipt = receiptRepository.findById(receiptId);
        if (!optReceipt.isPresent()) {
            throw new Exception("Fiş Kaydı Bulunamadı !");
        }

        receiptContentRepository.deleteReceiptContentToReceiptId(receiptId);

        return true;
    }


}
