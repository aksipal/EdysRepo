package com.via.ecza.api;

import com.via.ecza.config.AppConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/show-document")
public class ShowDocumentApi {


    @Autowired
    private AppConfiguration app;

    @GetMapping(value = "/logistic-document-pdf/{fileName}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> logisticPdfFile(@PathVariable String fileName) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/"+fileName+".pdf")));
        return ResponseEntity.ok(bytes);
    }

    @GetMapping(value = "/logistic-document-excel/{fileName}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> logisticExcelFile(@PathVariable String fileName) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/"+fileName+"xlsx")));
        return ResponseEntity.ok(bytes);

    }

    @GetMapping(value = "/depo-pdf/{pdfTitle}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getPdf(@PathVariable String pdfTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/depo_pdf_"+pdfTitle+".pdf")));
        return ResponseEntity.ok(bytes);
    }

    @GetMapping(value = "/depo-excel/{excelTitle}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> getExcel(@PathVariable String excelTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/depo_excel_"+excelTitle+".xlsx")));
        return ResponseEntity.ok(bytes);

    }
    @GetMapping(value = "/stok-pdf/{pdfTitle}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getStockPdf(@PathVariable String pdfTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/stok_pdf_"+pdfTitle+".pdf")));
        return ResponseEntity.ok(bytes);
    }
    @GetMapping(value = "/stok-excel/{excelTitle}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> getStockExcel(@PathVariable String excelTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/stok_excel_"+excelTitle+".xlsx")));
        return ResponseEntity.ok(bytes);

    }
    @GetMapping(value = "/ilac-excel/{excelTitle}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> getDrugCardExcel(@PathVariable String excelTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/ilac_excel_"+excelTitle+".xlsx")));
        return ResponseEntity.ok(bytes);

    }
    @GetMapping(value = "/ilac-pdf/{pdfTitle}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getDrugCardPdf(@PathVariable String pdfTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/ilac_pdf_"+pdfTitle+".pdf")));
        return ResponseEntity.ok(bytes);
    }
    @GetMapping(value = "/customer-order-excel/{excelTitle}"
            , produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            //, produces = "application/vnd.ms-excel"
    )
    public ResponseEntity<byte[]> customerOrderExcel(@PathVariable String excelTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/"+excelTitle+".xlsx")));
        return ResponseEntity.ok(bytes);
    }
    @GetMapping(value = "/customer-order-pdf/{pdfTitle}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getCustomerOrderPdf(@PathVariable String pdfTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/"+pdfTitle+".pdf")));
        return ResponseEntity.ok(bytes);
    }
//    @GetMapping(value = "/customer-order-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
//    public ResponseEntity<byte[]> getOrderPdf() throws IOException {
//        byte[] bytes = Files.readAllBytes(Paths.get(("docs/musteri-siparis.pdf")));
//        return ResponseEntity.ok(bytes);
//    }
//    @GetMapping(value = "/siparis_bilgileri_excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
//    public ResponseEntity<byte[]> getOrderExcel() throws IOException {
//        byte[] bytes = Files.readAllBytes(Paths.get(("docs/siparis_excel.xlsx")));
//        return ResponseEntity.ok(bytes);
//    }

    @GetMapping(value = "/purchase-excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> getExcelPurchase() throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/satin_alma_excel.xlsx")));
        return ResponseEntity.ok(bytes);

    }
    @GetMapping(value = "/pharmacy-excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> getExcelParmacy() throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/Eczane-Satın-Alma-Siparişleri-excel.xlsx")));
        return ResponseEntity.ok(bytes);

    }
    @GetMapping(value = "/pharmacy-refund-excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> getExcelParmacyRefund() throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/Eczane-İade-Siparişleri-Excel.xlsx")));
        return ResponseEntity.ok(bytes);

    }

    @GetMapping(value = "/box-pdf/{pdfTitle}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> boxPdf(@PathVariable String pdfTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/"+pdfTitle+".pdf")));
        return ResponseEntity.ok(bytes);
    }

    @GetMapping(value = "/box-excel/{excelTitle}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> boxExcel(@PathVariable String excelTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/"+excelTitle+".xlsx")));
        return ResponseEntity.ok(bytes);
    }
    @GetMapping(value = "/invoice-pdf/{pdfTitle}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable String pdfTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/"+pdfTitle+".pdf")));
        return ResponseEntity.ok(bytes);
    }

    @GetMapping(value = "/purchase-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getPdfPurchase() throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/Satın-Alma-Siparişleri.pdf")));
        return ResponseEntity.ok(bytes);
    }
    @GetMapping(value = "/pharmacy-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getPdfPharmacy() throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/Eczane-Satın-Alma-Siparişleri.pdf")));
        return ResponseEntity.ok(bytes);
    }

    @GetMapping(value = "/pharmacy-refund-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getPdfPharmacyRefund() throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/Eczane-İade-Siparişleri.pdf")));
        return ResponseEntity.ok(bytes);
    }

    //Email için logoyu burdan çekicez
    @GetMapping(value = "/pharma-logo", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getPharmaLogo() throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/ekip.png")));
        return ResponseEntity.ok(bytes);
    }

    @GetMapping(value = "/buy-receipt-pdf/{fileName}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getBuyReceiptPdf(@PathVariable String fileName) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/"+fileName+".pdf")));
        return ResponseEntity.ok(bytes);
    }

    @GetMapping(value = "/buy-receipt-excel/{excelTitle}",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> buyReceiptExcel(@PathVariable String excelTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/"+excelTitle+".xlsx")));
        return ResponseEntity.ok(bytes);
    }

    @GetMapping(value = "/sell-receipt-pdf/{fileName}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getSellReceiptPdf(@PathVariable String fileName) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/"+fileName+".pdf")));
        return ResponseEntity.ok(bytes);
    }

    @GetMapping(value = "/sell-receipt-excel/{excelTitle}",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> sellReceiptExcel(@PathVariable String excelTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/"+excelTitle+".xlsx")));
        return ResponseEntity.ok(bytes);
    }

    @GetMapping(value = "/small-box-excel/{excelTitle}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> smallBoxExcel(@PathVariable String excelTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/SBX_"+excelTitle+".xlsx")));
        return ResponseEntity.ok(bytes);
    }

    @GetMapping(value = "/small-box-packing-excel/{excelTitle}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> smallBoxPackingExcel(@PathVariable String excelTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/SBX_Packing_"+excelTitle+".xlsx")));
        return ResponseEntity.ok(bytes);
    }

    @GetMapping(value = "/small-box-pdf/{pdfTitle}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> smallBoxPdf(@PathVariable String pdfTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/"+pdfTitle+".pdf")));
        return ResponseEntity.ok(bytes);
    }

    @GetMapping(value = "/accounting-excel/{excelTitle}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> accountActivityExcel(@PathVariable String excelTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/"+excelTitle+".xlsx")));
        return ResponseEntity.ok(bytes);

    }
    @GetMapping(value = "/campaign-pdf/{pdfTitle}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> campaignPdf(@PathVariable String pdfTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/"+pdfTitle+".pdf")));
        return ResponseEntity.ok(bytes);
    }
    @GetMapping(value = "/fiyat-excel/{excelTitle}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> getPriceExcel(@PathVariable String excelTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/fiyat_excel_"+excelTitle+".xlsx")));
        return ResponseEntity.ok(bytes);

    }
    @GetMapping(value = "/stock-counting-explanation-excel/{excelTitle}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> getStockCountingExplanationExcel(@PathVariable String excelTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/stock_explanation_excel_"+excelTitle+".xlsx")));
        return ResponseEntity.ok(bytes);

    }
    @GetMapping(value = "/buy-accounting-pdf/{pdfTitle}", produces = MediaType.APPLICATION_PDF_VALUE)
        public ResponseEntity<byte[]> accountActivityDetailsPdf(@PathVariable String pdfTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/"+pdfTitle+".pdf")));
        return ResponseEntity.ok(bytes);
    }

    @GetMapping(value = "/buy-accounting-excel/{excelTitle}", produces =  "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> accountActivityDetailsExcel(@PathVariable String excelTitle) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/"+excelTitle+".xlsx")));
        return ResponseEntity.ok(bytes);
    }

    @GetMapping(value = "/sample-pts-excel/{fileName}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> samplePtsExcel(@PathVariable String fileName) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(("docs/"+fileName+".xlsx")));
        return ResponseEntity.ok(bytes);

    }

}
