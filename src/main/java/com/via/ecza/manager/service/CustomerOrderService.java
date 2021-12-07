package com.via.ecza.manager.service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPTable;
import com.via.ecza.dto.*;
import com.via.ecza.entity.CustomerOrder;
import javassist.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

public interface CustomerOrderService {

    SingleCustomerOrderDto getSingleCustomerOrder(String authHeader, Long customerId, Long customerOrderId) throws Exception;
    SingleCustomerOrderDto getSingleCustomerOrderForManager(String authHeader, Long customerId, Long customerOrderId) throws Exception;
    Boolean sendtoSetOrderPrices(String authHeader, Long customerId, Long customerOrderId) throws Exception;
    Long save(String authHeader, @Valid CustomerOrderSaveDto dto) throws NotFoundException, Exception;
    String getCode(Long customerOrdeId);
    Page<CustomerOrderSearchListDto> getAllWithPagee(String authHeader, Pageable page, SearchCustomerOrderDto dto) throws Exception;
    SingleCustomerOrderDto findById(String authHeader, Long customerOrderId) throws Exception;
    List<CustomerOrderSearchListDto> getAllPricingPhase();
    List<CustomerOrderSearchListDto> getAllApprovePhase();
    List<CustomerOrderDrugsDto> getCustomerOrderDrugList(Long customerOrderId);
    Boolean approveCustomerOrder(Long customerOrderId) throws Exception;
    Boolean customerOrderUpdateCurrencyFee(String authHeader, CustomerOrderUpdateDto dto) throws Exception;
    Boolean customerOrderUpdate(String authHeader, CustomerOrderUpdateDto dto) throws Exception;
    Boolean saveOrderNote(String authHeader, CustomerOrderDto dto) throws Exception;
    Boolean setOfferApprovalStage(String authHeader, CustomerOrderSaveDto dto) throws Exception;
    Boolean sendToSupplierOrderSearch(String authHeader, CustomerOrderSaveDto dto) throws Exception;
    void purchaseSetOrders(CustomerOrder customerOrder);
    CustomerOrderTotalPackagingDataDto getTotalPackagingData(String authHeader, CustomerOrderSaveDto dto) throws Exception;
    Boolean sendToApproveToManager(String authHeader, Long customerId, Long customerOrderId) throws Exception;
    String createQuotationFormPdf(String authHeader, Long customerId, Long customerOrderId) throws Exception;
    //String createCustomerOrderExcel(String authHeader, Long customerId, Long customerOrderId) throws Exception;
    String quotationForm(String authHeader, Long customerId, Long customerOrderId) throws Exception;
    String proformaInvoice(String authHeader, Long customerId, Long customerOrderId) throws Exception;
    String createCustomerOrderExcelV3(String authHeader, Long customerId, Long customerOrderId) throws Exception;
    Boolean setCustomerOrderStatusTo65(String authHeader, Long customerId, Long customerOrderId) throws Exception;
    Boolean setCustomerOrderStatusBackTo50(String authHeader, Long customerId, Long customerOrderId) throws Exception;
}

