package com.via.ecza.api.admin;

import com.via.ecza.dto.*;
import com.via.ecza.entity.CustomerSupplyStatus;
import com.via.ecza.entity.RefundStatus;
import com.via.ecza.service.*;
import com.via.ecza.util.ApiPath;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping(ApiPath.PharmacyCtrl.CTRL)
@RequiredArgsConstructor
public class PharmacyApi {

    private final SupplyCustomerService supplyCustomerService;
    private final SupplierOfferService supplierOfferService;
    private final RefundService refundService;
    private final DepotService depotService;
    private final RefundOfferService refundOfferService;
    private final PharmacyOrderService pharmacyOrderService;
    private final PharmacyOfferService pharmacyOfferService;

    //Eczane için iade sorgu listesi
    @PostMapping("/refunds-of-supplier")
    public ResponseEntity<List<PharmacyRefundDto>> getRefundsBySupplier(@RequestHeader("Authorization") String authHeader, @RequestBody RefundSearchDto dto) throws Exception {
        return ResponseEntity.ok(pharmacyOrderService.getRefundsBySupplier(authHeader, dto));
    }

    @GetMapping("/refund-status")
    public ResponseEntity<List<RefundStatusForPharmacyDto>> getAllRefundStatus() throws NotFoundException {
        return ResponseEntity.ok(pharmacyOrderService.getAllRefundStatus());
    }

    @GetMapping("/order-status")
    public ResponseEntity<List<CustomerSupplyStatusForPharmacyDto>> getAllOrderStatus() throws NotFoundException {
        return ResponseEntity.ok(pharmacyOrderService.getAllOrderStatus());
    }

    //Eczane için iade teklifleri listesi
    @GetMapping("/refund-offers-of-supplier")
    public ResponseEntity<List<PharmacyRefundOfferDto>> getRefundOffersBySupplier(@RequestHeader("Authorization") String authHeader) throws Exception {
        return ResponseEntity.ok(pharmacyOfferService.getRefundOffersBySupplier(authHeader));
    }

    //Eczane için teklifler
    @PostMapping("/search-offers-of-supplier")
    public ResponseEntity<Page<SupplyDrugOffersDto>> getSupplierByUserPage(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody SupplyDrugCardDto dto,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
                @RequestParam(defaultValue = "createdAt") String sortBy) throws NotFoundException {
        return ResponseEntity.ok(pharmacyOfferService.getSupplierByUserPage(authHeader,dto,pageNo,pageSize,sortBy));
    }

    //Eczane için teklifler
//    @GetMapping("/offers-of-supplier")
//    public ResponseEntity<List<SupplyDrugOffersDto>> getBySupplier(@RequestHeader("Authorization") String authHeader) throws NotFoundException {
//        return ResponseEntity.ok(pharmacyOfferService.getSupplierByUser(authHeader));
//    }

    //Eczane teklif kabul etme , iptal etme veya değiştirme durumu
    @PutMapping("/supply-offer/{supplierOfferId}")
    public ResponseEntity<Boolean> update(
            @PathVariable Long supplierOfferId,
            @Valid @RequestBody SupplierOfferSaveDto dto) throws Exception {
        return ResponseEntity.ok(supplierOfferService.update(supplierOfferId, dto));
    }

    //    Eczane için siparişler
    @GetMapping("/orders-of-supplier")
    public ResponseEntity<List<PharmacyOrdesDto>> getOrdersByPharmacy(@RequestHeader("Authorization") String authHeader, @RequestBody SupplyCustomerOrderSearchDto dto) throws Exception {
        return ResponseEntity.ok(pharmacyOrderService.getOrdersByPharmacy(authHeader, dto));
    }

    //Eczane iade iptali
    @GetMapping("/cancel-of-refund-by-pharmacy/{refundOfferId}")
    public ResponseEntity<Boolean> cancelRefundOfferByPurchase(@PathVariable Long refundOfferId) throws Exception {
        return ResponseEntity.ok(pharmacyOfferService.cancelRefundOfferByPurchase(refundOfferId));
    }

    //Eczane iade onayı
    @GetMapping("/accept-of-refund-by-pharmacy/{refundOfferId}")
    public ResponseEntity<Boolean> acceptRefundOfferByPurchase(@PathVariable Long refundOfferId) throws Exception {
        return ResponseEntity.ok(pharmacyOrderService.acceptRefundOfferByPurchase(refundOfferId));
    }

    @GetMapping("/pharmacyCounts")
    public ResponseEntity<PharmacyIndexDto> getPharmacyCounts(@RequestHeader("Authorization") String authHeader) throws NotFoundException {
        return ResponseEntity.ok(pharmacyOfferService.getPharmacyCounts(authHeader));
    }

    //Pagination
    @PostMapping("/pharmacy-order-pagination")
    public ResponseEntity<Page<PharmacyOrdesDto>> getOrdersWithPage(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody PharmacyOrderSearchDto dto,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "supplierId") String sortBy
    ) throws Exception {
        return ResponseEntity.ok(pharmacyOrderService.getOrdersWithPage(authHeader, dto, pageNo, pageSize, sortBy));
    }

    //pdf
    @PostMapping("/pdf-pharmacy")
    public ResponseEntity<Boolean> getOrdersWithPDF(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody PharmacyOrderSearchDto dto
    ) throws Exception {
        return ResponseEntity.ok(pharmacyOrderService.getOrdersWithPDF(authHeader, dto));
    }

    //excel
    @PostMapping("/excel-pharmacy")
    public ResponseEntity<Boolean> getOrdersWithExcel(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody PharmacyOrderSearchDto dto
    ) throws Exception {
        return ResponseEntity.ok(pharmacyOrderService.getOrdersWithExcel(authHeader, dto));
    }

    @PostMapping("/pharmacy-refund-order-pagination")
    public ResponseEntity<Page<PharmacyRefundDto>> getRefundsWithPage(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody RefundSearchDto dto,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "supplierId") String sortBy
    ) throws Exception {
        return ResponseEntity.ok(pharmacyOrderService.getRefundsWithPage(authHeader, dto, pageNo, pageSize, sortBy));
    }

    //pdf
    @PostMapping("/pdf-pharmacy-refund")
    public ResponseEntity<Boolean> getRefundsWithPDF(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody RefundSearchDto dto
    ) throws Exception {
        return ResponseEntity.ok(pharmacyOrderService.getRefundsWithPDF(authHeader, dto));
    }

    //excel
    @PostMapping("/excel-pharmacy-refund")
    public ResponseEntity<Boolean> getRefundsWithExcel(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody RefundSearchDto dto
    ) throws Exception {
        return ResponseEntity.ok(pharmacyOrderService.getRefundsWithExcel(authHeader, dto));
    }

    // Eczane Modülü Ana Sayfa Pie Cart Yıllık Satışı Yapılan İlaç Miktarı
    @GetMapping("/pharmacy-total-amount-drugs")
    public int getPharmacyTotalAmountOfDrugs(@RequestHeader("Authorization") String authHeader ) throws NotFoundException {
        return pharmacyOrderService.getPharmacyTotalAmountOfDrugs(authHeader);
    }

    // Eczane Modülü Ana Sayfa Pie Cart Yıllık İade Alınan İlaç Sayısı
    @GetMapping("/pharmacy-total-return-drugs")
    public int getPharmacyTotalReturnDrugs(@RequestHeader("Authorization") String authHeader ) throws NotFoundException {
        return pharmacyOrderService.getPharmacyTotalReturnDrugs(authHeader);
    }

    @PostMapping("/uploadfile")
    public synchronized String uploadSingleFile (@RequestHeader("Authorization") String authHeader,@RequestParam("file") MultipartFile file) throws Exception {
        File convertFile=new File("docs/pts_excel.xlsx");
        convertFile.createNewFile();
        FileOutputStream fout=new FileOutputStream(convertFile);
        fout.write(file.getBytes());
        fout.close();
        //Dosyada okunarak vertabanına kayıt ekleniyor
        String resultExplanation=null;

        Boolean result=pharmacyOrderService.UpdateFromExcel(authHeader,"pts_excel");
        if(result==true){
            // System.out.println("Güncelleme İşlemi Tamamlandı");
            resultExplanation="Yükleme İşlemi Tamamlandı";
        }else{
            // System.out.println("Güncelleme İşleminde Hata Oluştu.");
            resultExplanation="Yükleme İşleminde Hata Oluştu.";
        }

        return resultExplanation;

    }
}
