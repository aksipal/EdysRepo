package com.via.ecza.manager.service;

import com.via.ecza.dto.*;
import com.via.ecza.entity.enumClass.LogisticDocumentType;
import com.via.ecza.entity.enumClass.LogisticFileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface LogisticService {

    Page<CustomerOrderListDto> searchLogisticCustomerOrder(String authHeader, Pageable page, SearchCustomerOrderDto dto) throws Exception;
    List<LogisticBoxDto> getAllLogisticBoxesWithCustomerOrder(Long customerOrderId);
    Page<CustomerOrderSearchListDto> getAllWithPagee(String authHeader, Pageable page, SearchCustomerOrderDto dto) throws Exception ;
    SingleCustomerOrderDto getSingleCustomerOrder(String authHeader, Long customerOrderId) throws Exception;
    List<PreLogisticDto> preFreightCostCalculation(String authHeader, PreFreightCostDto dto) throws Exception;
    Boolean savePreFreightCostCalculation(String authHeader, PreFreightCostDto dto) throws Exception;
    //PreLogisticDto calculation(Set<PreLogisticDto> list, CustomerOrderDrugs drug, Long totalQuantity, PreLogisticDto dto, Long drugTotalVolume, int singleVolume);
    String uploadFile(Long customerOrderId, LogisticFileType fileType, LogisticDocumentType documentType, MultipartFile logisticFileName) throws Exception;
    List<CustomerOrderLogisticDocumentListDto> getAllDocumentByCustomerId (Long customerOrderId)  throws Exception;
    Boolean deleteCustomerOrderLogisticDocument(Long logisticDocumentId) throws Exception;
    CustomerOrderDto getCustomerOrder(Long customerOrderId) throws Exception;

    //PreLogisticDto createPreBox(BoxSize boxSize, Set<PreLogisticDto> list);


}
