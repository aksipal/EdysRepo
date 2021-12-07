package com.via.ecza.manager;

import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.BoxSize;
import com.via.ecza.entity.enumClass.LogisticDocumentType;
import com.via.ecza.entity.enumClass.LogisticFileType;
import com.via.ecza.manager.service.LogisticService;
import com.via.ecza.repo.*;
import com.via.ecza.service.ControlService;
import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

@Service
public class LogisticManager implements LogisticService {



    @Autowired
    private PreLogisticCalcuationRepository preLogisticCalcuationRepository;
    @Autowired
    private ControlService controlService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private BoxRepository boxRepository;
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private CustomerOrderLogisticDocumentRepository customerOrderLogisticDocumentRepository;

    @Override
    public Page<CustomerOrderListDto> searchLogisticCustomerOrder(String authHeader, Pageable page, SearchCustomerOrderDto dto) throws Exception {
        try {
            StringBuilder createSqlQuery = null;
            List<Object> list = null;
            Page<CustomerOrderListDto> pageList = null;
            createSqlQuery = new StringBuilder("select * from customer_order co ");
            if (dto.getCustomerId() != null || dto.getCity() != null || dto.getCountryId() != null) {

                createSqlQuery.append("join customer c on c.customer_id = co.customer_id  where co.order_status_id>=30 ");
                if (dto.getCustomerId() != null) if (dto.getCustomerId() != 0)
                    createSqlQuery.append("and c.customer_id = " + dto.getCustomerId() + " ");

                if (dto.getCountryId() != null) if (dto.getCountryId() != 0)
                    createSqlQuery.append("and c.countryid = " + dto.getCountryId() + " ");

                if (dto.getCity() != null)
                    createSqlQuery.append("and  c.city ILIKE '%" + dto.getCity() + "%' ");

            } else {
                createSqlQuery.append(" where co.order_status_id>=30 ");
            }

            if (dto.getUserId() != null)
                createSqlQuery.append("and co.user_id= " + dto.getUserId() + " ");

            if (dto.getCustomerOrderNo() != null) {
                if (dto.getCustomerOrderNo().trim().length() > 0)
                    createSqlQuery.append("and  co.customer_order_no ILIKE '%" + dto.getCustomerOrderNo().trim() + "%' ");
            }


            if (dto.getOrderStatusId() != null)
                createSqlQuery.append("and  co.order_status_id = " + dto.getOrderStatusId() + " ");

            if (dto.getOrderDate() != null) {
                createSqlQuery.append("and co.order_date = " + dto.getOrderDate() + " ");
            }
            createSqlQuery.append(" order by co.customer_order_id ");


            list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerOrder.class).getResultList();

            CustomerOrderListDto[] dtos = mapper.map(list, CustomerOrderListDto[].class);
            List<CustomerOrderListDto> dtosList = Arrays.asList(dtos);

            int start = Math.min((int) page.getOffset(), dtosList.size());
            int end = Math.min((start + page.getPageSize()), dtosList.size());

            pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());

            return pageList;

        } catch (Exception e) {
            throw e;
        }
    }


    @Override
    public List<LogisticBoxDto> getAllLogisticBoxesWithCustomerOrder(Long customerOrderId) {

        List<Box> box = boxRepository.findBoxes(customerOrderId);
        LogisticBoxDto[] array = mapper.map(box, LogisticBoxDto[].class);
        List<LogisticBoxDto> boxList = Arrays.asList(array);

        int counter = 0;
        for (Box box1 : box) {
            boxList.get(counter).setDrugCount(box1.getBoxDrugList().size());
            counter++;
        }

        return boxList;
    }


    @Override
    public Page<CustomerOrderSearchListDto> getAllWithPagee(String authHeader, Pageable page, SearchCustomerOrderDto dto) throws Exception {
        try {
            StringBuilder createSqlQuery = null;
            List<CustomerOrder> list = null;
            Page<CustomerOrderSearchListDto> pageList = null;
            createSqlQuery = new StringBuilder("select * from customer_order co ");
            if (dto.getCustomerId() != null || dto.getCity() != null || dto.getCountryId() != null) {

                createSqlQuery.append("join customer c on c.customer_id = co.customer_id  where co.status=1 ");
                if (dto.getCustomerId() != null) if (dto.getCustomerId() != 0)
                    createSqlQuery.append("and c.customer_id = " + dto.getCustomerId() + " ");

                if (dto.getCountryId() != null) if (dto.getCountryId() != 0)
                    createSqlQuery.append("and c.countryid = " + dto.getCountryId() + " ");

                if (dto.getCity() != null)
                    createSqlQuery.append("and  c.city ILIKE '%" + dto.getCity() + "%' ");

            } else {
                createSqlQuery.append(" where co.status=1  and co.order_status_id >2 and co.order_status_id <30 ");
            }

            if (dto.getCustomerOrderNo() != null)
                createSqlQuery.append("and  co.customer_order_no ILIKE '%" + dto.getCustomerOrderNo() + "%' ");

            if (dto.getUserId() != null)
                createSqlQuery.append("and  co.user_id = " + dto.getUserId() + " ");

            if (dto.getOrderDate() != null)
                createSqlQuery.append("and co.order_date = " + dto.getOrderDate() + " ");

            createSqlQuery.append(" order by co.customer_order_id ");

            list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerOrder.class).getResultList();

            CustomerOrderSearchListDto[] dtos = mapper.map(list, CustomerOrderSearchListDto[].class);
            List<CustomerOrderSearchListDto> dtosList = Arrays.asList(dtos);

            int start = Math.min((int) page.getOffset(), dtosList.size());
            int end = Math.min((start + page.getPageSize()), dtosList.size());

            pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());

            return pageList;

        } catch (Exception e) {
            throw e;
        }

    }


    @Override
    public SingleCustomerOrderDto getSingleCustomerOrder(String authHeader, Long customerOrderId) throws Exception {

        Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(customerOrderId);
        if (!optionalCustomerOrder.isPresent()) throw new Exception("Sipariş kaydı bulunamadı");

        CustomerOrder customerOrder = optionalCustomerOrder.get();
        SingleCustomerOrderDto dto = mapper.map(customerOrder, SingleCustomerOrderDto.class);
        return dto;

    }
    public Boolean savePreFreightCostCalculation(String authHeader, PreFreightCostDto dto) throws Exception {
        List<PreLogisticDto> list = new ArrayList<>();
        Optional<CustomerOrder> customerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());
        if (!customerOrder.isPresent())
            throw new Exception("Sipariş Kaydı Bulunamadı");


        BoxSize boxSize = dto.getBoxSize();
        Long boxVolume = 1L;
        boxVolume *= Long.valueOf(boxSize.getValue().split("\\*")[0]);
        boxVolume *= Long.valueOf(boxSize.getValue().split("\\*")[1]);
        boxVolume *= Long.valueOf(boxSize.getValue().split("\\*")[2]);

        PreLogisticDto preLogisticDto = new PreLogisticDto();
        preLogisticDto.setCreatedAt(new Date());
        preLogisticDto.setBoxSize(boxSize);
        preLogisticDto.setTotalBoxWeight(0D);
        preLogisticDto.setTotalBoxVolume(boxVolume);
        preLogisticDto.setRestOfBoxVolume(boxVolume);
        preLogisticDto.setTotalDrugCount(0L);

        list.add(preLogisticDto);
        Long drugTotalVolume = 0L;
        for (int i = 0; i< customerOrder.get().getCustomerOrderDrugs().size(); i++) {
            CustomerOrderDrugs drug = customerOrder.get().getCustomerOrderDrugs().get(i);
            if (drug.getDrugCard().getDrugBoxProperties() != null) {
                int singleVolume = (int) (drug.getDrugCard().getDrugBoxProperties().getDrugBoxWidth() *
                        drug.getDrugCard().getDrugBoxProperties().getDrugBoxHeight() *
                        drug.getDrugCard().getDrugBoxProperties().getDrugBoxLength());

                drugTotalVolume = (drug.getTotalQuantity() * singleVolume);
                //cc += (drug.getTotalQuantity() * singleVolume);
                preLogisticDto = this.calculation(boxSize, list, drug, drug.getTotalQuantity(), preLogisticDto, drugTotalVolume,singleVolume);
            } else {
                throw new NotFoundException("İlacın Ağırlık Ve Boyut Bilgileri Eksiktir :" +drug.getDrugCard().getDrugName());
            }
        }
        PreLogisticDto lastOne = new PreLogisticDto();
        preLogisticDto = list.get(list.size()-1);
        list.remove(list.size()-1);
        lastOne.setRestOfBoxVolume(preLogisticDto.getRestOfBoxVolume());
        lastOne.setTotalBoxVolume(preLogisticDto.getTotalBoxVolume());
        lastOne.setBoxSize(preLogisticDto.getBoxSize());
        lastOne.setStatus(preLogisticDto.getStatus());
        lastOne.setTotalBoxWeight(preLogisticDto.getTotalBoxWeight());
        lastOne.setTotalDrugCount(preLogisticDto.getTotalDrugCount());
        lastOne = this.createPreBox(lastOne, (lastOne.getTotalBoxVolume()- lastOne.getRestOfBoxVolume()));
        list.add(lastOne);

        for (PreLogisticDto data: list){
            PreLogisticCalcuation cal = mapper.map(data, PreLogisticCalcuation.class);
            cal.setCustomerOrder(customerOrder.get());
            cal = preLogisticCalcuationRepository.save(cal);
        }

        if(dto.getPreFreighCost() == null)
            throw new NotFoundException("Ön Navlum Bedeli giriniz.");
        if(dto.getPreFreighCost() <= 0)
            throw new NotFoundException("Ön Navlum Bedeli giriniz.");
        CustomerOrder co = customerOrder.get();
        co.setPreFreighCost(dto.getPreFreighCost());
        co = customerOrderRepository.save(co);

        return true;
    }
    public List<PreLogisticDto> preFreightCostCalculation(String authHeader, PreFreightCostDto dto) throws Exception {
        List<PreLogisticDto> list = new ArrayList<>();
        Optional<CustomerOrder> customerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());
        if (!customerOrder.isPresent())
            throw new Exception("Sipariş Kaydı Bulunamadı");


        BoxSize boxSize = dto.getBoxSize();
        Long boxVolume = 1L;
        boxVolume *= Long.valueOf(boxSize.getValue().split("\\*")[0]);
        boxVolume *= Long.valueOf(boxSize.getValue().split("\\*")[1]);
        boxVolume *= Long.valueOf(boxSize.getValue().split("\\*")[2]);

        PreLogisticDto preLogisticDto = new PreLogisticDto();
        preLogisticDto.setCreatedAt(new Date());
        preLogisticDto.setBoxSize(boxSize);
        preLogisticDto.setTotalBoxWeight(0D);
        preLogisticDto.setTotalBoxVolume(boxVolume);
        preLogisticDto.setRestOfBoxVolume(boxVolume);
        preLogisticDto.setTotalDrugCount(0L);

        list.add(preLogisticDto);
        Long drugTotalVolume = 0L;
        for (int i = 0; i< customerOrder.get().getCustomerOrderDrugs().size(); i++) {
            CustomerOrderDrugs drug = customerOrder.get().getCustomerOrderDrugs().get(i);
            if (drug.getDrugCard().getDrugBoxProperties() != null) {
                int singleVolume = (int) (drug.getDrugCard().getDrugBoxProperties().getDrugBoxWidth() *
                        drug.getDrugCard().getDrugBoxProperties().getDrugBoxHeight() *
                        drug.getDrugCard().getDrugBoxProperties().getDrugBoxLength());

                drugTotalVolume = (drug.getTotalQuantity() * singleVolume);
                //cc += (drug.getTotalQuantity() * singleVolume);
                preLogisticDto = this.calculation(boxSize, list, drug, drug.getTotalQuantity(), preLogisticDto, drugTotalVolume,singleVolume);
            } else {
                throw new NotFoundException("İlacın Ağırlık Ve Boyut Bilgileri Eksiktir :" +drug.getDrugCard().getDrugName());
            }
        }
        PreLogisticDto lastOne = new PreLogisticDto();
        preLogisticDto = list.get(list.size()-1);
        list.remove(list.size()-1);
        lastOne.setRestOfBoxVolume(preLogisticDto.getRestOfBoxVolume());
        lastOne.setTotalBoxVolume(preLogisticDto.getTotalBoxVolume());
        lastOne.setBoxSize(preLogisticDto.getBoxSize());
        lastOne.setStatus(preLogisticDto.getStatus());
        lastOne.setTotalBoxWeight(preLogisticDto.getTotalBoxWeight());
        lastOne.setTotalDrugCount(preLogisticDto.getTotalDrugCount());
        lastOne = this.createPreBox(lastOne, (lastOne.getTotalBoxVolume()- lastOne.getRestOfBoxVolume()));
        list.add(lastOne);
        return list;
    }
    public PreLogisticDto calculation(BoxSize defaultBoxSize, List<PreLogisticDto> list,
                                        CustomerOrderDrugs drug,
                                        Long totalQuantity,
                                        PreLogisticDto dto,
                                        Long drugTotalVolume,
                                        int singleVolume) {
        PreLogisticDto preLogisticDto = dto;
        Long result = preLogisticDto.getRestOfBoxVolume() - drugTotalVolume;
        if(result>=0l){
            preLogisticDto.setRestOfBoxVolume(preLogisticDto.getRestOfBoxVolume() - drugTotalVolume);
            preLogisticDto.setTotalBoxWeight(preLogisticDto.getTotalBoxWeight()+( totalQuantity* drug.getDrugCard().getDrugBoxProperties().getDrugBoxWeight()));
            preLogisticDto.setTotalDrugCount(preLogisticDto.getTotalDrugCount() + totalQuantity);

        }else{
            int restOfBoxDrugCount = (int) (preLogisticDto.getRestOfBoxVolume()/ singleVolume);
            if(restOfBoxDrugCount >= totalQuantity ){
                Long totalDrugVolume = Long.valueOf((totalQuantity * singleVolume));
                preLogisticDto.setRestOfBoxVolume(preLogisticDto.getRestOfBoxVolume() - totalDrugVolume);
                preLogisticDto.setTotalBoxWeight(preLogisticDto.getTotalBoxWeight()+( totalQuantity * drug.getDrugCard().getDrugBoxProperties().getDrugBoxWeight()));
                preLogisticDto.setTotalDrugCount(preLogisticDto.getTotalDrugCount() + totalQuantity);
            }
            else if (restOfBoxDrugCount == 0){
                preLogisticDto =this.createDefaultPreBox( defaultBoxSize,(totalQuantity-restOfBoxDrugCount) * singleVolume   );
                list.add(preLogisticDto);
                preLogisticDto = this.calculation(defaultBoxSize,list,drug,totalQuantity-restOfBoxDrugCount,preLogisticDto, (totalQuantity-restOfBoxDrugCount)* singleVolume , singleVolume );
            }
            else{
                Long totalDrugVolume = Long.valueOf((restOfBoxDrugCount * singleVolume));
                preLogisticDto.setRestOfBoxVolume(preLogisticDto.getRestOfBoxVolume() - totalDrugVolume);
                preLogisticDto.setTotalBoxWeight(preLogisticDto.getTotalBoxWeight()+( restOfBoxDrugCount* drug.getDrugCard().getDrugBoxProperties().getDrugBoxWeight()));
                preLogisticDto.setTotalDrugCount(preLogisticDto.getTotalDrugCount() + restOfBoxDrugCount);
                preLogisticDto =this.createDefaultPreBox(defaultBoxSize, (totalQuantity-restOfBoxDrugCount) * singleVolume   );
                list.add(preLogisticDto);
                preLogisticDto = this.calculation(defaultBoxSize,list,drug,totalQuantity-restOfBoxDrugCount,preLogisticDto, (totalQuantity-restOfBoxDrugCount)* singleVolume , singleVolume );
            }
        }

        return preLogisticDto;
    }
    public PreLogisticDto createDefaultPreBox( BoxSize defaultBoxSize,  Long drugTotalVolume){
        PreLogisticDto preLogisticDto = new PreLogisticDto();
        Long defaultBoxVolume = 1L;

        defaultBoxVolume *= Long.valueOf(defaultBoxSize.getValue().split("\\*")[0]);
        defaultBoxVolume *= Long.valueOf(defaultBoxSize.getValue().split("\\*")[1]);
        defaultBoxVolume *= Long.valueOf(defaultBoxSize.getValue().split("\\*")[2]);


        preLogisticDto.setCreatedAt(new Date());
        preLogisticDto.setBoxSize(defaultBoxSize);
        preLogisticDto.setTotalBoxWeight(0D);
        preLogisticDto.setRestOfBoxVolume(defaultBoxVolume);
        preLogisticDto.setTotalBoxVolume(defaultBoxVolume);
        preLogisticDto.setTotalDrugCount(0L);
        return preLogisticDto;
    }
    public PreLogisticDto createPreBox(PreLogisticDto preLogisticDto ,    Long drugTotalVolume){

        Long boxVolume = 1L;
        BoxSize boxSize;
        if (drugTotalVolume <= 64000){
            boxSize= BoxSize.SIZE_40;
            boxVolume *= Long.valueOf(BoxSize.SIZE_40.getValue().split("\\*")[0]);
            boxVolume *= Long.valueOf(BoxSize.SIZE_40.getValue().split("\\*")[1]);
            boxVolume *= Long.valueOf(BoxSize.SIZE_40.getValue().split("\\*")[2]);
        } else if (drugTotalVolume <= 125000) {
            boxSize= BoxSize.SIZE_50;
            boxVolume *= Long.valueOf(BoxSize.SIZE_50.getValue().split("\\*")[0]);
            boxVolume *= Long.valueOf(BoxSize.SIZE_50.getValue().split("\\*")[1]);
            boxVolume *= Long.valueOf(BoxSize.SIZE_50.getValue().split("\\*")[2]);
        }
        else{
            boxSize= BoxSize.SIZE_60;
            boxVolume *= Long.valueOf(BoxSize.SIZE_60.getValue().split("\\*")[0]);
            boxVolume *= Long.valueOf(BoxSize.SIZE_60.getValue().split("\\*")[1]);
            boxVolume *= Long.valueOf(BoxSize.SIZE_60.getValue().split("\\*")[2]);
        }

        preLogisticDto.setCreatedAt(new Date());
        preLogisticDto.setBoxSize(boxSize);
        //preLogisticDto.setTotalBoxWeight(0D);
        preLogisticDto.setRestOfBoxVolume(boxVolume - drugTotalVolume);
        preLogisticDto.setTotalBoxVolume(boxVolume);
        //preLogisticDto.setTotalDrugCount(0L);
        return preLogisticDto;
    }

    @Override
    public String uploadFile(Long customerOrderId, LogisticFileType fileType, LogisticDocumentType documentType, MultipartFile logisticFileName) throws Exception {

        String fileName = customerOrderId+"_"+logisticFileName.getOriginalFilename();
        File convertFile=new File("docs/"+fileName);
        convertFile.createNewFile();
        FileOutputStream fout=new FileOutputStream(convertFile);
        fout.write(logisticFileName.getBytes());
        fout.close();

        Optional<CustomerOrder> opt = customerOrderRepository.findById(customerOrderId);
        if( !opt.isPresent())
            throw new NotFoundException("Sipariş Kaydı Bulunamadı");
        CustomerOrder customerOrder = opt.get();

        CustomerOrderLogisticDocument doc = new CustomerOrderLogisticDocument();
        doc.setCustomerOrder(customerOrder);
        doc.setDocumentType(documentType);
        doc.setFileType(fileType);
        doc.setFileName(fileName);
        doc = customerOrderLogisticDocumentRepository.save(doc);

        return fileName;
    }

    public List<CustomerOrderLogisticDocumentListDto> getAllDocumentByCustomerId (Long customerOrderId)  throws Exception {
        List<CustomerOrderLogisticDocumentListDto> dtosList = null;

        Optional<CustomerOrder> opt = customerOrderRepository.findById(customerOrderId);
        if( !opt.isPresent())
            throw new NotFoundException("Sipariş Kaydı Bulunamadı");
        CustomerOrder customerOrder = opt.get();
        List<CustomerOrderLogisticDocument> list = customerOrderLogisticDocumentRepository.findByCustomerOrder(customerOrder);
        CustomerOrderLogisticDocumentListDto[] dtos = mapper.map(list, CustomerOrderLogisticDocumentListDto[].class);
        dtosList = Arrays.asList(dtos);

        return dtosList;
    }

    public Boolean deleteCustomerOrderLogisticDocument(Long logisticDocumentId) throws Exception {

        Optional<CustomerOrderLogisticDocument> opt = customerOrderLogisticDocumentRepository.findById(logisticDocumentId);
        if( !opt.isPresent())
            throw new NotFoundException("Belge Kaydı Bulunamadı");
        CustomerOrderLogisticDocument data = opt.get();
        data.setCustomerOrder(null);
        String fileName = data.getFileName();

        File excelFile = new File("docs/"+fileName);
        if (!excelFile.delete()) {
            return false;
        }else{
            customerOrderLogisticDocumentRepository.delete(data);
        }
        return true;
    }

    public CustomerOrderDto getCustomerOrder(Long customerOrderId) throws Exception {

        Optional<CustomerOrder> opt = customerOrderRepository.findById(customerOrderId);
        if(!opt.isPresent())
            throw new NotFoundException("Müşteri Bulunamadı");
        CustomerOrder co = opt.get();
        CustomerOrderDto dto = mapper.map(co, CustomerOrderDto.class);
        return dto;

    }

}
