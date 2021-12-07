package com.via.ecza.service;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.via.ecza.config.AppConfiguration;
import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


@Slf4j
@Service
@Transactional
public class BoxService {

    @Autowired
    private BoxRepository boxRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private BoxDrugListRepository boxDrugListRepository;
    @Autowired
    private DepotRepository depotRepository;
    @Autowired
    private DepotStatusRepository depotStatusRepository;
    @Autowired
    private QrCodeRepository qrCodeRepository;
    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private BarcodeRepository barcodeRepository;
    @Autowired
    private SmallBoxRepository smallBoxRepository;
    @Autowired
    private BoxTypeRepository boxTypeRepository;
    @Autowired
    private AppConfiguration app;
    @Autowired
    private CustomerOrderDrugsRepository customerOrderDrugsRepository;
    @Autowired
    private ControlService controlService;
    @Autowired
    private UserCameraRepository userCameraRepository;

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    // test edildi sıkıntı yok
    public List<PackagingDepotDto> getAllDepotForBoxing(String authHeader, PrePackageSearchDto dto) throws Exception {
        List<PackagingDepotDto> depotList = new ArrayList<>();
        List<PackagingDepotDto> controlList = new ArrayList<>();
        List<PackagingDepotDto> notThisCustomerOrderList = new ArrayList<>();
        Optional<CustomerOrder> opt = customerOrderRepository.findById(dto.getCustomerOrderId());
        if(!opt.isPresent()){
            throw new NotFoundException("Sipariş Kaydı bulunamadı");
        }
        CustomerOrder customerOrder = opt.get();

        User user = controlService.getUserFromToken(authHeader);
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);
        if (!optUserCamera.isPresent()) {
            throw new Exception("Kullanıcı Kamera Kaydı Bulunamadı !");
        }

//        List<QrCode> qrCodeList = qrCodeRepository.findByStatus(2);
        List<QrCode> qrCodeList = qrCodeRepository.findByCameraType(optUserCamera.get().getCameraType());


        for (QrCode qrCode : qrCodeList) {

            PrePackage prePackage= seperateQrCode(qrCode);
            if(prePackage == null) continue;
            prePackage.setCustomerOrder(customerOrder);

            Optional<Depot> optDepot = depotRepository.findSingleDepotForPack(
                    prePackage.getDrugLotNo(),
                    prePackage.getDrugBarcode().toString(),
                    prePackage.getDrugSerialNo().toString());

            if(optDepot.isPresent()){
                depotList.add(mapper.map(optDepot.get(), PackagingDepotDto.class));
            }
            else{
                Optional<Depot> optControlDepot = depotRepository.controlForBox(
                        prePackage.getCustomerOrder().getCustomerOrderId(),
                        prePackage.getDrugLotNo(),
                        prePackage.getDrugBarcode().toString(),
                        prePackage.getDrugSerialNo().toString());
                if(optControlDepot.isPresent()) {
                    if(optControlDepot.get().getBoxId() != null)
                        controlList.add(mapper.map(optControlDepot.get(), PackagingDepotDto.class));
                    //prePackage = prePackageRepository.save(prePackage);
                }
                else {
                    Optional<Depot> optNotThisCustomerOrder = depotRepository.controlForBoxAndCustomerOrder(
                            prePackage.getCustomerOrder().getCustomerOrderId(),
                            prePackage.getDrugLotNo(),
                            prePackage.getDrugBarcode().toString(),
                            prePackage.getDrugSerialNo().toString());
                    if(optNotThisCustomerOrder.isPresent()) {
                        notThisCustomerOrderList.add(mapper.map(optNotThisCustomerOrder.get(), PackagingDepotDto.class));
                    }

                }
            }
        }

        depotList.addAll(controlList);
        depotList.addAll(notThisCustomerOrderList);
        return depotList;
    }

    // test edildi sıkıntı yok
    public BoxDto transferForNewBox(Long customerOrderId,Long customerOrderDrugId, List<PrePackageTransferDto> dtos, String authHeader) throws Exception {

        User user = controlService.getUserFromToken(authHeader);

        Optional<CustomerOrder> optional = customerOrderRepository.findById(customerOrderId);
        if(!optional.isPresent() || !(dtos.size()>0)) {
            return null;
        }
        CustomerOrder customerOrder = optional.get();
        Box box = new Box();
        Optional<BoxType> boxOpt = boxTypeRepository.findById(1L);
        box.setBoxType(boxOpt.get());
        box.setStatus(1);
        box.setCustomerOrder(customerOrder);
        box.setCreatedDate(new Date());
        box = boxRepository.save(box);
        box.setBoxNo(this.generateBoxNo(box));
        box = boxRepository.save(box);
        box.setUser(user);
        int drugSize =0;
        for (PrePackageTransferDto dto:dtos) {
            if(dto == null)
                continue;
            if(dto.getValue()){
                Optional<Depot> opt = depotRepository.findById(dto.getDepotId());
                if(opt.isPresent()){
                    Depot depot = opt.get();
//                    depot = depotRepository
//                    Optional<DepotStatus> optStatus = depotStatusRepository.findById(30L);
//                    if(!optStatus.isPresent())
//                        continue;
                    Optional<CustomerOrderDrugs> optCustomerOrderDrugs=customerOrderDrugsRepository.getOneForPackaging(
                            customerOrderId,depot.getDepotId());
                    if(!optCustomerOrderDrugs.isPresent()){
                        drugSize++;
                        continue;
                    }
                    CustomerOrderDrugs cod = optCustomerOrderDrugs.get();
                    //depot.setDepotStatus(optStatus.get());
                    if(cod.getIncompleteQuantity()<1)
                        continue;
                    cod.setChargedQuantity(cod.getChargedQuantity()+1);
                    cod.setIncompleteQuantity(cod.getIncompleteQuantity()-1);
                    cod = customerOrderDrugsRepository.save(cod);
                    depot = depotRepository.save(depot);

                    BoxDrugList drug = null;
                    Optional<BoxDrugList>  optControl=boxDrugListRepository.controlSingleDepot(depot.getDepotId());
                    if(optControl.isPresent())
                        drug = optControl.get();
                    else
                        drug = new BoxDrugList();
                    drug.setDepot(depot);
                    drug.setBox(box);
                    drug= boxDrugListRepository.save(drug);
                    depot.setBoxId(box.getBoxId());
                    depot = depotRepository.save(depot);
                    //prePackageRepository.delete(p);


                }
                //}
            }

        }
        BoxDto dto = null;

        if(drugSize == dtos.size()){
            box.setBoxType(null);
            box.setUser(null);
            box.setCustomerOrder(null);
            box = boxRepository.save(box);
            boxRepository.delete(box);
        }else{
            Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);
            if(optUserCamera.isPresent())
                qrCodeRepository.deleteByCameraType(optUserCamera.get().getCameraType());

            dto =  mapper.map(box, BoxDto.class);
        }
        return dto;
    }

    //dokunulmayacak
    private String  generateBoxNo(Box box ){
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String code = "BX-"+year;
        int size = box.getBoxId().toString().length();
        for (int i = 0 ;i<7-size;i++)
            code+="0";
        code+=box.getBoxId();
        return code;
    }

    //dokunulmayacak
    public Page<BoxListDto> searchBoxes(Pageable page, BoxSearchDto dto) throws Exception {
        String boxNo = null;
        if(dto.getBoxNo() != null){
            boxNo = dto.getBoxNo().replace("BX*","BX-");
            dto.setBoxNo(boxNo);
        }
        StringBuilder createSqlQuery = new StringBuilder("select b2.* from box b2 ");
        createSqlQuery.append("left join customer_order co on co.customer_order_id = b2.customer_order_id " );
        createSqlQuery.append("left join box_drug_list bdl on bdl.box_id = b2.box_id ");
        createSqlQuery.append("where co.status = 1 and b2.status = 1 and bdl.box_id is not null ");
        if(dto.getBoxNo() != null  )
            if(!dto.getBoxNo().equals(""))
            createSqlQuery.append("and b2.box_no ILIKE '%"+dto.getBoxNo().trim()+"%' ");
        if(dto.getCustomerOrderNo() != null  )
            if(!dto.getCustomerOrderNo().equals(""))
            createSqlQuery.append("and co.customer_order_no ILIKE '%"+dto.getCustomerOrderNo().trim()+"%' ");
        createSqlQuery.append("group by b2.box_id, co.customer_order_no,co.customer_order_id order by co.customer_order_id asc ");
        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Box.class).getResultList();

        BoxListDto[] dtos = mapper.map(list,BoxListDto[].class );
        List<BoxListDto> dtosList = Arrays.asList(dtos);

        int start = Math.min((int) page.getOffset(), dtosList.size());
        int end = Math.min((start + page.getPageSize()), dtosList.size());

        Page<BoxListDto> pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());
        pageList.forEach( drug ->{ drug.setDrugQuantity(drug.getBoxDrugList().size());});

        return pageList;

    }

    //dokunulmayacak
    public Page<PackagingCustomerOrderListDto> searchCustomerOrder(Pageable page, PackagingCustomerOrderSearchDto dto) throws NotFoundException {
       // Page<CustomerOrder> orders = null;

        StringBuilder createSqlQuery = new StringBuilder("select * from customer_order co " +
                "inner join users u on co.user_id =u.user_id " +
                "inner join customer c on co.customer_id=c.customer_id " +
                "where co.order_status_id in (30,40,50,60) and co.status=1");




        if(dto.getSortCriteria()!=null && dto.getSortDirection()!=null){
            //datatable kullanıldıysa
            if(dto.getCustomerOrderNo() == null || dto.getCustomerOrderNo().trim().equals("")){
                if(dto.getSortCriteria().trim().equals("created_date") || dto.getSortCriteria().trim().equals("customer_order_no")){
                    createSqlQuery.append(" order by co."+dto.getSortCriteria().trim()+" "+dto.getSortDirection().trim());
                    //orders = customerOrderRepository.getAllCustomerOrderForPackaging(page);
                }else if(dto.getSortCriteria().trim().equals("fullname")){
                    createSqlQuery.append(" order by u."+dto.getSortCriteria().trim()+" "+dto.getSortDirection().trim());
                    //orders = customerOrderRepository.getAllCustomerOrderForPackaging(page);
                }else if(dto.getSortCriteria().trim().equals("name")){
                    createSqlQuery.append(" order by c."+dto.getSortCriteria().trim()+" "+dto.getSortDirection().trim());
                    //orders = customerOrderRepository.getAllCustomerOrderForPackaging(page);
                }
            }else{
                if(dto.getSortCriteria().trim().equals("created_date") || dto.getSortCriteria().trim().equals("customer_order_no")){
                    createSqlQuery.append(" and co.customer_order_no ILIKE '"+dto.getCustomerOrderNo()+"'");
                    createSqlQuery.append(" order by co."+dto.getSortCriteria().trim()+" "+dto.getSortDirection().trim());
                    //orders = customerOrderRepository.getPageableCustomerOrderForPackaging(dto.getCustomerOrderNo().trim(),page);
                }else if(dto.getSortCriteria().trim().equals("fullname")){
                    createSqlQuery.append(" and co.customer_order_no ILIKE '"+dto.getCustomerOrderNo()+"'");
                    createSqlQuery.append(" order by u."+dto.getSortCriteria().trim()+" "+dto.getSortDirection().trim());
                   // orders = customerOrderRepository.getPageableCustomerOrderForPackaging(dto.getCustomerOrderNo().trim(),page);
                }else if(dto.getSortCriteria().trim().equals("name")){
                    createSqlQuery.append(" and co.customer_order_no ILIKE '"+dto.getCustomerOrderNo()+"'");
                    createSqlQuery.append(" order by c."+dto.getSortCriteria().trim()+" "+dto.getSortDirection().trim());
                  //  orders = customerOrderRepository.getPageableCustomerOrderForPackaging(dto.getCustomerOrderNo().trim(),page);
                }
            }
        }else{
            //datatable kullanılmadıysa
            if(dto.getCustomerOrderNo() == null || dto.getCustomerOrderNo().trim().equals("")){
                createSqlQuery.append(" order by co.customer_order_id");
                //orders = customerOrderRepository.getAllCustomerOrderForPackaging(page);
            }else{
                createSqlQuery.append(" and co.customer_order_no ILIKE '"+dto.getCustomerOrderNo()+"'");
                createSqlQuery.append(" order by co.customer_order_id");
                //orders = customerOrderRepository.getPageableCustomerOrderForPackaging(dto.getCustomerOrderNo().trim(), page );
            }
        }


        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerOrder.class).getResultList();
        CustomerOrder[] dtos = mapper.map(list,CustomerOrder[].class );
        List<CustomerOrder> dtosList = Arrays.asList(dtos);
        int start = Math.min((int) page.getOffset(), dtosList.size());
        int end = Math.min((start + page.getPageSize()), dtosList.size());
        Page<CustomerOrder> orders = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());





        //plasiyer bilgisi için user bilgisi kopyalanıyor
        List<User> userList=new ArrayList<>();
        AtomicInteger counter= new AtomicInteger();
        counter.set(0);
        orders.forEach(data ->{
            userList.add(counter.get(), data.getUser());
            counter.set(counter.get() + 1);
        });
        //user bilgisi kopyalama son


        Page<PackagingCustomerOrderListDto> pageList =  orders.map(PackagingCustomerOrderListDto::new);


        //plasiyer için user bilgisi set ediliyor
        counter.set(0);
        pageList.forEach(data ->{
            data.setUser(userList.get(counter.get()));
            counter.set(counter.get() + 1);
        });


        return pageList;
    }

    //dokunulmayacak
    public PackagingCustomerOrderDto findByCustomerOrder(Long customerOrderId) throws Exception {

        Optional<CustomerOrder> optional = customerOrderRepository.getSingleCustomerOrderForPackaging(customerOrderId);
        if(!optional.isPresent()) throw new NotFoundException("Sipariş  kaydı bulunamadı.");
        PackagingCustomerOrderDto dto= mapper.map(optional.get(), PackagingCustomerOrderDto.class);

        return dto;
    }

    //dokunulmayacak
    public List<BoxListDetailDto> getSingleBox(Long boxId) throws Exception {



        if(boxId == null) return null;
        if(!(boxId >0)) return null;
        List<BoxListDetailDto> dtosList = new ArrayList<BoxListDetailDto>();

        List<Object[]> listResult = boxRepository.getBoxCount(boxId);

        for (Object[] objArr : listResult) {
            BoxListDetailDto dto = new BoxListDetailDto();
            dto.setCount((BigInteger) objArr[0]);
            dto.setDrugName((String) objArr[1]);
            dto.setDrugCode((BigInteger) objArr[2]);
            //dto.setExpirationDate((Date) objArr[3]);
            dto.setSmallBoxNo((String) objArr[3]);

            dtosList.add(dto);
        }
        return dtosList;
    }

    //dokunulmayacak
    public List<BoxComboboxDto> getAllBoxesWithCustomerOrder(Long customerOrderId) {

        List<Box> box = boxRepository.findBoxes(customerOrderId);
        BoxComboboxDto[] array = mapper.map(box, BoxComboboxDto[].class);
        List<BoxComboboxDto> boxList = Arrays.asList(array);

        return boxList;
    }

    //test edildi sıkıntı yok
    public BoxDto transferForSpecificBox(Long customerOrderId,Long customerOrderDrugId ,Long boxId, List<PrePackageTransferDto> dtos,  String authHeader) throws Exception {

        User user = controlService.getUserFromToken(authHeader);

        Optional<CustomerOrder> optional = customerOrderRepository.findById(customerOrderId);
        if(!optional.isPresent()) {
            return null;
        }
        if(!(dtos.size()>0)){
            return null;
        }
        CustomerOrder customerOrder = optional.get();
        Optional<Box> optionalBox = boxRepository.findByBoxIdAndCustomerOrder(boxId, customerOrder);
        if(!optionalBox.isPresent()) {
            return null;
        }
        Box box = optionalBox.get();
        for (PrePackageTransferDto dto:dtos) {
            if(dto == null)
                continue;
            if(dto.getValue()){
                Optional<Depot> opt = depotRepository.findById(dto.getDepotId());
                if(opt.isPresent()){
                    Depot depot = opt.get();
//                    depot = depotRepository
//                    Optional<DepotStatus> optStatus = depotStatusRepository.findById(30L);
//                    if(!optStatus.isPresent())
//                        continue;
                    Optional<CustomerOrderDrugs> optCustomerOrderDrugs=customerOrderDrugsRepository.getOneForPackaging(
                            customerOrderId, depot.getDepotId());
                    if(!optCustomerOrderDrugs.isPresent())
                        continue;
                    CustomerOrderDrugs cod = optCustomerOrderDrugs.get();
                    if(cod.getIncompleteQuantity()<1)
                        continue;
                    cod.setChargedQuantity(cod.getChargedQuantity()+1);
                    cod.setIncompleteQuantity(cod.getIncompleteQuantity()-1);
                    cod = customerOrderDrugsRepository.save(cod);
                    //depot.setDepotStatus(optStatus.get());
                    depot = depotRepository.save(depot);

                    BoxDrugList drug = null;
                    Optional<BoxDrugList>  optControl=boxDrugListRepository.controlSingleDepot(depot.getDepotId());
                    if(optControl.isPresent())
                        drug = optControl.get();
                    else
                        drug = new BoxDrugList();
                    drug.setDepot(depot);
                    drug.setBox(box);
                    drug= boxDrugListRepository.save(drug);
                    depot.setBoxId(box.getBoxId());
                    depot = depotRepository.save(depot);
                    //prePackageRepository.delete(p);


                }
                //}
            }
        }
        if(dtos.size()>0){
            Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);

            if(optUserCamera.isPresent()){
                qrCodeRepository.deleteByCameraType(optUserCamera.get().getCameraType());
            }
        }
        BoxDto dto =  mapper.map(box, BoxDto.class);
        return dto;
    }

    //dokunulmayacak
    public FileDto createBarcode(String boxNo) throws Exception {
        Optional<Box> optBox = boxRepository.findByBoxNo(boxNo);
        // The data that the QR code will contain
        String data = optBox.get().getBoxNo();
        // The path where the image will get saved
        String path = app.getUploadPath()+"/"+app.getUploadBarcodePath()+"/"+data+".png";
        // Encoding charset
        String charset = "UTF-8";
        Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
        hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        // Create the QR code and save in the specified folder as a jpg file
        createQR(data, path, charset, hashMap, 200, 400);
//        System.out.println("Barcode Generated!!! ");

        FileDto fileDto = new FileDto();
        byte[] bytes = Files.readAllBytes(Paths.get((path)));
        fileDto.setFile(bytes);
        fileDto.setFileName(path);
        return fileDto;
    }

    //dokunulmayacak
    public static void createQR(String data, String path, String charset, Map hashMap, int height, int width) throws Exception {
        BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset),
                BarcodeFormat.CODE_128, width, height);
        MatrixToImageWriter.writeToFile(matrix, path.substring(path.lastIndexOf('.') + 1), new File(path));
    }

    //dokunulmayacak
    public List<BoxDrugNameDto> getDrugNamesForBox(Long boxId) throws Exception {
        if(boxId == null) return null;
        if(!(boxId >0)) return null;
        List<BoxDrugNameDto> dtosList = new ArrayList<BoxDrugNameDto>();
        try {
            List<Object[]> listResult = boxRepository.getDrugNamesForBox(boxId);

            //dtosList = (List<BoxDrugNameDto>)(Object)list;

            for (Object[] objArr : listResult) {
                BoxDrugNameDto dto = new BoxDrugNameDto();
                dto.setBarcode((String) objArr[0]);
                dto.setDrugName((String) objArr[1]);
                dto.setQuantity((String) objArr[2].toString() );
                dto.setExpirationDate((Date) objArr[3]);
                dto.setStatus((int) objArr[4]);
                dto.setDrugCardId(Long.parseLong(objArr[5].toString()));
                dtosList.add(dto);
            }
        }
        catch (Exception e){
            throw e;
        }
        return dtosList;
    }

    //test edildi sıkıntı yok
    public Boolean removeFromBoxWithQrCode(String authHeader, PrePackageSearchDto dto) throws Exception {

        User user = controlService.getUserFromToken(authHeader);
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);
        if (!optUserCamera.isPresent()) {
            throw new Exception("Kullanıcı Kamera Kaydı Bulunamadı !");
        }


//        Optional<DepotStatus> depotStatus1 = depotStatusRepository.findById(1L);
//        Optional<DepotStatus> depotStatus2 = depotStatusRepository.findById(2L);
//        if(!depotStatus1.isPresent()){
//            return false;
//        }
        List<QrCode> qrCodeList = qrCodeRepository.findByCameraType(optUserCamera.get().getCameraType());
        if(qrCodeList == null)
            return false;
        if(!(qrCodeList.size()>0))
            return false;

        for (QrCode qrCode : qrCodeList) {
            PrePackage prePackage= seperateQrCode(qrCode);
            if(prePackage == null) continue;

            Optional<Depot> optionalDepot = depotRepository.controlDepotForRemovingFromBox(
                    prePackage.getDrugLotNo(),
                    prePackage.getDrugBarcode().toString(),
                    prePackage.getDrugSerialNo().toString());
            if(optionalDepot.isPresent()){
                Depot depot = optionalDepot.get();
                Optional<CustomerOrderDrugs> optCustomerOrderDrugs=customerOrderDrugsRepository.getOneForPackaging(
                        dto.getCustomerOrderId(),depot.getDepotId());
                if(!optCustomerOrderDrugs.isPresent())
                    continue;
                Optional<BoxDrugList> optBoxDrugList = boxDrugListRepository.findByDepot(depot);
                if(optBoxDrugList.isPresent()){
                    CustomerOrderDrugs cod = optCustomerOrderDrugs.get();
                    if(cod.getChargedQuantity()< 1)
                        continue;
                    cod.setChargedQuantity(cod.getChargedQuantity()-1);
                    cod.setIncompleteQuantity(cod.getIncompleteQuantity()+1);
                    cod = customerOrderDrugsRepository.save(cod);
                    BoxDrugList boxDrugList = optBoxDrugList.get();
                    if(boxDrugList.getSmallBox() == null){
                        depot.setSmallBoxId(null);
                        boxDrugListRepository.deleteById(boxDrugList.getBoxDrugListId());
                    }
                    else{
                        boxDrugList.setBox(null);
                        boxDrugListRepository.save(boxDrugList);
                    }
                    depot.setBoxId(null);
                    depot.setBoxDrugList(null);
                    depot = depotRepository.save(depot);


                }else{
                    continue;
                }

            }
        }
        return true;
    }

    //dokunulmayacak
    private PrePackage seperateQrCode(QrCode code) throws Exception {
        String qrCode;
        int secondGroupSeperatorIndex = 0;
        StringBuilder barcode = new StringBuilder();
        StringBuilder serialNo = new StringBuilder();
        StringBuilder expirationDate = new StringBuilder();
        StringBuilder lotNo = new StringBuilder();
        StringBuilder itsNo = new StringBuilder();
        qrCode = code.getQrCode();
        if(qrCode == null)
            return null;

        if(qrCode.trim().length() <1)
            return null;

        //İlaç Barkodu Alındı.
        if (qrCode.charAt(0) == '0' && qrCode.charAt(1) == '1') {
            barcode.append(qrCode.substring(2, 16));
        }

        //İlaç Seri Numarası Alındı.
        if (qrCode.charAt(16) == '2' && qrCode.charAt(17) == '1') {
            secondGroupSeperatorIndex = qrCode.indexOf("&", 18);
            serialNo.append(qrCode.substring(18, secondGroupSeperatorIndex));
        }

        //İlaç SKT Alındı.
        if (qrCode.charAt(secondGroupSeperatorIndex + 1) == '1' && qrCode.charAt(secondGroupSeperatorIndex + 2) == '7') {
            expirationDate.append("20");
            expirationDate.append(qrCode.substring(secondGroupSeperatorIndex + 3, secondGroupSeperatorIndex + 5));
            expirationDate.append("-");
            expirationDate.append(qrCode.substring(secondGroupSeperatorIndex + 5, secondGroupSeperatorIndex + 7));
            expirationDate.append("-");
            expirationDate.append(qrCode.substring(secondGroupSeperatorIndex + 7, secondGroupSeperatorIndex + 9));
        }

        //İlaç Parti Numarası Alındı.
        if (qrCode.charAt(secondGroupSeperatorIndex + 9) == '1' && qrCode.charAt(secondGroupSeperatorIndex + 10) == '0') {
            lotNo.append(qrCode.substring(secondGroupSeperatorIndex + 11, qrCode.length()));
        }

        //İTS No Alındı.
        if (barcode.length() > 0 && serialNo.length() > 0) {
            itsNo.append(barcode + "21" + serialNo);
        }

        PrePackage prePackage = new PrePackage();
        prePackage.setDrugBarcode(Long.valueOf(barcode.toString()));
        prePackage.setDrugSerialNo(serialNo.toString());
        Date exp = new SimpleDateFormat("yyyy-MM-dd").parse(expirationDate.toString());
        prePackage.setDrugExpirationDate(exp);
        prePackage.setDrugLotNo(lotNo.toString());
        prePackage.setDrugItsNo(itsNo.toString());
        Optional<DrugCard> drug = drugCardRepository.findByDrugCode(prePackage.getDrugBarcode());
        if(!drug.isPresent())
            return null;
        prePackage.setDrugName(drug.get().getDrugName());
        //prePackage = prePackageRepository.save(prePackage);
        return prePackage;
    }

    public String createBoxPdf(BoxFileDto dto) throws Exception{
        try {
            List<BoxAndBoxDrugListGroupDto> boxDrugList = new ArrayList<BoxAndBoxDrugListGroupDto>();
            List<Box> boxList = new ArrayList<Box>();

            if(dto.getBoxNo() != null || dto.getCustomerOrderNo() != null ){
            //ARAMA BAŞLANGIÇ
            StringBuilder createSqlQuery = new StringBuilder("select b.box_no, dc.drug_name, d.lot_no, count(dc.drug_name) as drug_count, min(d.expiration_date) from depot d ");
            createSqlQuery.append(" join box_drug_list bdl  on d.depot_id =bdl.depot_id ");
            createSqlQuery.append(" join drug_card dc on dc.drug_card_id = d.drug_card_id ");
            createSqlQuery.append(" join customer_order co on co.customer_order_id = d.customer_order_id ");
            createSqlQuery.append(" join box b on b.box_id = d.box_id ");
            createSqlQuery.append(" where b.status =1 ");
            if(dto.getBoxNo() != null  )
                if(!dto.getBoxNo().equals(""))
                    createSqlQuery.append("and b.box_no = '"+dto.getBoxNo().trim()+"' ");
            if(dto.getCustomerOrderNo() != null  )
                if(!dto.getCustomerOrderNo().equals(""))
                    createSqlQuery.append("and co.customer_order_no = '"+dto.getCustomerOrderNo().trim()+"' ");

            createSqlQuery.append(" group by b.box_no, d.lot_no, dc.drug_name");

            List<Object[]> list = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();

            for (Object[] objArr : list) {

                BoxAndBoxDrugListGroupDto boxDto = new BoxAndBoxDrugListGroupDto();
                boxDto.setBoxNo((String)objArr[0]);
                boxDto.setDrugName((String) objArr[1]);
                boxDto.setLotNo((String) objArr[2]);
                boxDto.setCount((BigInteger) objArr[3]);
                boxDto.setExpirationDate((Date) objArr[4]);

                boxDrugList.add(boxDto);
            }
            }
            else {
                StringBuilder createSqlQuery = new StringBuilder("select * from box b2  join customer_order co ");
                createSqlQuery.append(" on co.customer_order_id = b2.customer_order_id where b2.status = 1 ");
                if(dto.getBoxNo() != null  )
                    if(!dto.getBoxNo().equals(""))
                        createSqlQuery.append("and box_no = '"+dto.getBoxNo().trim()+"' ");
                if(dto.getCustomerOrderNo() != null  )
                    if(!dto.getCustomerOrderNo().equals(""))
                        createSqlQuery.append("and customer_order_no = '"+dto.getCustomerOrderNo().trim()+"' ");

                createSqlQuery.append(" order by b2.box_no");

                List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Box.class).getResultList();

                Box[] boxes = mapper.map(list,Box[].class );
                boxList = Arrays.asList(boxes);
            }

//            Box[] boxes = mapper.map(list,Box[].class );
//            List<Box> boxList = Arrays.asList(boxes);


            //ARAMA SON

            String pdfTitle="box";
            if(dto.getBoxNo() != null )
                if(!dto.getBoxNo().equals(""))
                    pdfTitle +="_"+dto.getBoxNo();
            if(dto.getCustomerOrderNo() != null )
                if(!dto.getCustomerOrderNo().equals(""))
                    pdfTitle +="_"+dto.getCustomerOrderNo();
            pdfTitle+=".pdf";

            //PDF BAŞLANGIÇ
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("docs/"+pdfTitle));
            document.open();

            Image image1 = Image.getInstance("docs/pharma.png");
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

            //Tablonun Başlığı Girilir
            Paragraph tableHeader = new Paragraph("Kutu Listesi", catFont);

            tableHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(tableHeader);

            document.add(new Paragraph("\n"));

            if(dto.getBoxNo() != null || dto.getCustomerOrderNo() != null ){

                    //Tablonun Sütun Sayısı Girilir
                    PdfPTable table = new PdfPTable(6);
                    table.setWidths(new int[]{2, 6, 6, 2, 3, 3});

                    table.setWidthPercentage(100);
                    addTableHeader(table);

                    String boxNo = null;

                    //Hücrelere Veriler Girilir
                    int a = 0;
                    for (BoxAndBoxDrugListGroupDto entity : boxDrugList) {
                        a++;

                        addRows(table, String.valueOf(a));
                        if(boxNo != null){
                            if(boxNo.equals(entity.getBoxNo()))
                                addRows(table," ");
                            else
                                addRows(table, entity.getBoxNo());
                        }
                        else
                            addRows(table,entity.getBoxNo());
                        addRows(table, entity.getDrugName());
                        addRows(table, String.valueOf(entity.getCount()));
                        addRows(table, sdf.format(entity.getExpirationDate()));
                        addRows(table, entity.getLotNo());

                        boxNo = entity.getBoxNo();
                    }
                    document.add(table);
            }
            else{
                PdfPTable table = new PdfPTable(4);
                table.setWidths(new int[]{2, 6, 6, 5});

                table.setWidthPercentage(100);
                addTableHeader2(table);

                //Hücrelere Veriler Girilir
                int a = 0;
                for (Box box : boxList) {
                    a++;
                    addRows(table, String.valueOf(a));
                    addRows(table,box.getBoxNo());
                    addRows(table, box.getCustomerOrder().getCustomerOrderNo());
                    addRows(table, String.valueOf(box.getBoxDrugList().size()));
                }
                document.add(table);
            }


            document.close();
            //PDF SON

            int index = pdfTitle.indexOf(".pdf");
            pdfTitle=pdfTitle.substring(0,index);
            return pdfTitle;

        }catch (Exception e){
            throw new Exception("Kutu Pdf Oluşturma İşleminde Hata Oluştu.",e);
        }
    }

    private void addTableHeader(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);

        //Tablo Sütün Başlıkları Girilir
        Stream.of("No", "Kutu Numarası", "İlaç Adı", "Miktar", "SKT", "Parti Numarası")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(1);
                    header.setPhrase(new Phrase(columnTitle, catFont));
                    header.setPadding(3);

                    table.addCell(header);

                });
    }
    private void addTableHeader2(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);

        //Tablo Sütün Başlıkları Girilir
        Stream.of("No", "Kutu Numarası", "Sipariş Numarası", "İlaç Miktarı")
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
        Font catFont = new Font(bf, 8, Font.NORMAL, BaseColor.BLACK);

        table.addCell(new Phrase(value, catFont));

    }

    public String createBoxExcel(BoxFileDto dto) throws Exception {

        try {
            //ARAMA BAŞLANGIÇ
            StringBuilder createSqlQuery = new StringBuilder("select * from box b2  join customer_order co ");
            createSqlQuery.append(" on co.customer_order_id = b2.customer_order_id where b2.status = 1 ");
            if(dto.getBoxNo() != null  )
                if(!dto.getBoxNo().equals(""))
                    createSqlQuery.append("and box_no = '"+dto.getBoxNo().trim()+"' ");
            if(dto.getCustomerOrderNo() != null  )
                if(!dto.getCustomerOrderNo().equals(""))
                    createSqlQuery.append("and customer_order_no = '"+dto.getCustomerOrderNo().trim()+"' ");

            createSqlQuery.append(" order by b2.box_no");

            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Box.class).getResultList();

            Box[] boxes = mapper.map(list,Box[].class );
            List<Box> boxList = Arrays.asList(boxes);
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

            addExcelHeader(workbook,sheet);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            int a = 6;
            int b = 0;
            for (Box box : boxList) {
                a++;
                b++;
                XSSFRow row = sheet.createRow((short) a);
                row.createCell(0).setCellValue(b);
                row.getCell(0).setCellStyle(csBody);
                //İçerik Kısmı Bu Satırdan İtibaren Obje Bilgileri İle Doldurulur
                row.createCell(1).setCellValue(box.getBoxNo());
                row.getCell(1).setCellStyle(csBody);
                row.createCell(2).setCellValue(box.getCustomerOrder().getCustomerOrderNo());
                row.getCell(2).setCellStyle(csBody);
                row.createCell(3).setCellValue(String.valueOf(box.getBoxDrugList().size()));
                row.getCell(3).setCellStyle(csBody);
            }

            String excelTitle="box";
            if(dto.getBoxNo() != null )
                if(!dto.getBoxNo().equals(""))
                    excelTitle +="_"+dto.getBoxNo();
            if(dto.getCustomerOrderNo() != null )
                if(!dto.getCustomerOrderNo().equals(""))
                    excelTitle +="_"+dto.getCustomerOrderNo();
            excelTitle+="";

            FileOutputStream fileOut = new FileOutputStream("docs/"+excelTitle);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();



            return excelTitle;
        }catch (Exception e){
            throw new Exception("Kutu Excel Oluşturma İşleminde Hata Oluştu.",e);
        }
    }

    private void addExcelHeader(XSSFWorkbook workbook,XSSFSheet sheet) throws IOException {
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
        rowDate.createCell(3).setCellValue(dateFormat.format(new Date()));
        rowDate.getCell(3).setCellStyle(csDate);


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
        rowHeader.createCell(0).setCellValue("Kutu Listesi");
        rowHeader.getCell(0).setCellStyle(csHeading);

        sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 1));
        //son parametre kolon sayısına eşit olmalı
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 4));


        XSSFRow rowhead = sheet.createRow((short) 6);

        //Kolon İsimleri Belirtilir İhtiyaç Olursa Kolonlar Genişletilir
        rowhead.createCell(0).setCellValue("No");
        rowhead.getCell(0).setCellStyle(csHeader);
        sheet.setColumnWidth(0, 1500);//kolon genişletildi
        rowhead.createCell(1).setCellValue("Kutu Numarası");
        rowhead.getCell(1).setCellStyle(csHeader);
        sheet.setColumnWidth(1, 10000);
        rowhead.createCell(2).setCellValue("Sipariş Numarası");
        rowhead.getCell(2).setCellStyle(csHeader);
        sheet.setColumnWidth(2, 10000);
        rowhead.createCell(3).setCellValue("İlaç Miktarı");
        rowhead.getCell(3).setCellStyle(csHeader);
        sheet.setColumnWidth(3, 5000);

        //A4 Sayfaya Sığdırma
        sheet.setFitToPage(true);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth( (short) 1);
        ps.setFitHeight( (short) 0);

    }


    public String createBoxListPdf(Long boxId) throws Exception{
        try {
            //ARAMA BAŞLANGIÇ
            Optional<Box> box = boxRepository.findById(boxId);
            if(!box.isPresent())
                throw new NotFoundException("Paket bulunamadı");

            List<BoxDrugListGroupDto> boxDrugList = new ArrayList<BoxDrugListGroupDto>();
            List<Object[]> dtosList = depotRepository.getBoxDrugs(boxId);

            for (Object[] objArr : dtosList) {

                BoxDrugListGroupDto dto = new BoxDrugListGroupDto();
                dto.setDrugName((String) objArr[0]);
                dto.setLotNo((String) objArr[1]);
                dto.setCount((BigInteger) objArr[2]);
                dto.setExpirationDate((Date) objArr[3]);

                boxDrugList.add(dto);
            }

            String pdfTitle="box-"+box.get().getBoxNo()+".pdf";

            //PDF BAŞLANGIÇ
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("docs/"+pdfTitle));




            document.open();

            Image image1 = Image.getInstance("docs/pharma.png");
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

            //Tablonun Başlığı Girilir
            Paragraph tableHeader = new Paragraph("Kutu İlaç Listesi" + " " + box.get().getBoxNo(), catFont);


            tableHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(tableHeader);

            document.add(new Paragraph("\n"));

            //Tablonun Sütun Sayısı Girilir
            PdfPTable table = new PdfPTable(5);
            table.setWidths(new int[]{1, 6, 4, 2, 4});

            table.setWidthPercentage(100);
            addTableHeaderForList(table);

            //Hücrelere Veriler Girilir
            int a = 0;
            for (BoxDrugListGroupDto entity : boxDrugList) {
                a++;
                addRows(table, String.valueOf(a));
                addRows(table, entity.getDrugName());
                addRows(table, entity.getLotNo());
                addRows(table, String.valueOf(entity.getCount()));
                addRows(table, sdf.format(entity.getExpirationDate()));
            }
            document.add(table);
            document.close();
            //PDF SON

            int index = pdfTitle.indexOf(".pdf");
            pdfTitle=pdfTitle.substring(0,index);

            return pdfTitle;

        }catch (Exception e){
            throw new Exception("Kutu İlaç Listesi Pdf Oluşturma İşleminde Hata Oluştu.",e);
        }
    }

    private void addTableHeaderForList(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);

        //Tablo Sütün Başlıkları Girilir
        Stream.of("No", "İlaç Adı", "Parti Numarası", "Adet", "Son Kullanma Tarihi")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(1);
                    header.setPhrase(new Phrase(columnTitle, catFont));
                    header.setPadding(3);

                    table.addCell(header);

                });
    }

    public String createBoxListExcel(Long boxId) throws Exception {

        try {
            //ARAMA BAŞLANGIÇ
            Optional<Box> box = boxRepository.findById(boxId);
            if(!box.isPresent())
                throw new NotFoundException("Kutu bulunamadı");
            List<BoxDrugList> boxDrugList = new ArrayList<>(box.get().getBoxDrugList());
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

            addExcelHeaderForList(workbook,sheet,box.get());
            //DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");


            int a = 6;
            int b = 0;
            for (BoxDrugList entity : boxDrugList) {
                a++;
                b++;
                XSSFRow row = sheet.createRow((short) a);
                row.createCell(0).setCellValue(b);
                row.getCell(0).setCellStyle(csBody);
                //İçerik Kısmı Bu Satırdan İtibaren Obje Bilgileri İle Doldurulur
                row.createCell(1).setCellValue(entity.getDepot().getDrugCard().getDrugName());
                row.getCell(1).setCellStyle(csBody);
                if(entity.getSmallBox()!=null)
                row.createCell(2).setCellValue(entity.getSmallBox().getSmallBoxNo());
                else
                    row.createCell(2).setCellValue("");
                row.getCell(2).setCellStyle(csBody);
                row.createCell(3).setCellValue(entity.getDepot().getDrugBarcode());
                row.getCell(3).setCellStyle(csBody);
                row.createCell(4).setCellValue(entity.getDepot().getSerialNumber());
                row.getCell(4).setCellStyle(csBody);

                row.createCell(5).setCellValue(sdf.format(entity.getDepot().getExpirationDate()));
                row.getCell(5).setCellStyle(csBody);
            }

            String excelTitle= box.get().getBoxNo();

            FileOutputStream fileOut = new FileOutputStream("docs/BX_"+excelTitle+".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            return excelTitle;
        }catch (Exception e){
            throw new Exception("Kutu İlaç Listesi Excel Oluşturma İşleminde Hata Oluştu.",e);
        }
    }

    private void addExcelHeaderForList(XSSFWorkbook workbook,XSSFSheet sheet, Box box) throws IOException {
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
        rowDate.createCell(5).setCellValue(dateFormat.format(new Date()));
        rowDate.getCell(5).setCellStyle(csDate);


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
        rowHeader.createCell(0).setCellValue("Kutu İlaç Listesi" + " " +box.getBoxNo());
        rowHeader.getCell(0).setCellStyle(csHeading);

        sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 1));
        //son parametre kolon sayısına eşit olmalı
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 5));


        XSSFRow rowhead = sheet.createRow((short) 6);

        //Kolon İsimleri Belirtilir İhtiyaç Olursa Kolonlar Genişletilir
        rowhead.createCell(0).setCellValue("No");
        rowhead.getCell(0).setCellStyle(csHeader);
        sheet.setColumnWidth(0, 1500);//kolon genişletildi
        rowhead.createCell(1).setCellValue("İlaç Adı");
        rowhead.getCell(1).setCellStyle(csHeader);
        sheet.setColumnWidth(1, 10000);
        rowhead.createCell(2).setCellValue("Paket Numarası");
        rowhead.getCell(2).setCellStyle(csHeader);
        sheet.setColumnWidth(2, 5000);
        rowhead.createCell(3).setCellValue("İlaç Barkodu");
        rowhead.getCell(3).setCellStyle(csHeader);
        sheet.setColumnWidth(3, 5000);
        rowhead.createCell(4).setCellValue("İlaç Seri Numarası");
        rowhead.getCell(4).setCellStyle(csHeader);
        sheet.setColumnWidth(4, 5000);
        rowhead.createCell(5).setCellValue("Son Kullanma Tarihi");
        rowhead.getCell(5).setCellStyle(csHeader);
        sheet.setColumnWidth(5, 10000);

        //A4 Sayfaya Sığdırma
        sheet.setFitToPage(true);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth( (short) 1);
        ps.setFitHeight( (short) 0);

    }

    public String createLogisticBoxPdf(Long customerOrderId) throws Exception{
        try {

            List<LogisticBoxDrugListDto> boxDrugList = new ArrayList<LogisticBoxDrugListDto>();
            List<Object[]> list = depotRepository.getLogisticBoxDrugList(customerOrderId);

            for (Object[] objArr : list) {

                LogisticBoxDrugListDto boxDto = new LogisticBoxDrugListDto();
                boxDto.setBoxNo((String)objArr[0]);
                boxDto.setCustomerBoxNo((String)objArr[1]);
                boxDto.setDrugName((String) objArr[2]);
                boxDto.setLotNo((String) objArr[3]);
                boxDto.setCount((BigInteger) objArr[4]);
                boxDto.setExpirationDate((Date) objArr[5]);
                boxDto.setExactBoxWeight((Double)objArr[6]);

                boxDrugList.add(boxDto);
            }

            //ARAMA SON

            String pdfTitle="box.pdf";

            //PDF BAŞLANGIÇ
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("docs/"+pdfTitle));
            document.open();

            Image image1 = Image.getInstance("docs/pharma.png");
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

            //Tablonun Başlığı Girilir
            Paragraph tableHeader = new Paragraph("PACKING LIST", catFont);

            tableHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(tableHeader);

            document.add(new Paragraph("\n"));

            //Tablonun Sütun Sayısı Girilir
            PdfPTable table = new PdfPTable(7);
            table.setWidths(new int[]{6, 5, 6, 3, 3, 3, 5});

            table.setWidthPercentage(100);
            addTableHeaderLogistic(table);

            String boxNo = null;
            String customerBoxNo = null;

            //Hücrelere Veriler Girilir
            for (LogisticBoxDrugListDto entity : boxDrugList) {
                if(boxNo != null){
                    if(boxNo.equals(entity.getBoxNo()))
                        addRows(table," ");
                    else
                        addRows(table, entity.getBoxNo());
                }
                else
                    addRows(table,entity.getBoxNo());

                if(customerBoxNo != null){
                    if(customerBoxNo.equals(entity.getCustomerBoxNo()))
                        addRows(table," ");
                    else
                        addRows(table, entity.getCustomerBoxNo());
                }
                else
                    addRows(table, entity.getCustomerBoxNo());

                addRows(table, entity.getDrugName());
                addRows(table, String.valueOf(entity.getCount()));
                addRows(table, sdf.format(entity.getExpirationDate()));
                addRows(table, entity.getLotNo());


                if(boxNo != null){
                    if(boxNo.equals(entity.getBoxNo()) && entity.getExactBoxWeight() != null )
                        addRows(table," ");
                    else
                        addRows(table, entity.getExactBoxWeight().toString());
                }
                else
                    if(entity.getExactBoxWeight() != null)
                        addRows(table, entity.getExactBoxWeight().toString());
                    else
                        addRows(table," ");

                boxNo = entity.getBoxNo();
                customerBoxNo= entity.getCustomerBoxNo();
            }
//            for (Box box : boxList) {
//                a++;
//                addRows(table, String.valueOf(a));
//                addRows(table, box.getBoxNo());
//                addRows(table, box.getCustomerOrder().getCustomerOrderNo());
//                addRows(table, String.valueOf(box.getBoxDrugList().size()));
//            }

            document.add(table);
            document.close();
            //PDF SON
            return pdfTitle;

        }catch (Exception e){
            throw new Exception("Kutu Pdf Oluşturma İşleminde Hata Oluştu.",e);
        }
    }

    private void addTableHeaderLogistic(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);

        //Tablo Sütün Başlıkları Girilir
        Stream.of("Box No", "Box No", "Product Name", "Quantity", "EXP Date", "Batch No", "Gross Weight Box (kg)")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(1);
                    header.setPhrase(new Phrase(columnTitle, catFont));
                    header.setPadding(3);

                    table.addCell(header);

                });
    }

    public Boolean deleteBarcode(String boxNo) {

        Optional<Box> optBox = boxRepository.findByBoxNo(boxNo);
        if(!optBox.isPresent()) return false;

        // The data that the QR code will contain
        String data = optBox.get().getBoxNo()+".png";
        // The path where the image will get saved
        String path = app.getUploadPath()+"/"+app.getUploadBarcodePath()+"/"+data;

        File barcode = new File(path);
        if (barcode.delete()) {
            return true;
        } else {
            return false;
        }

    }

    public BoxDto drugsForManualBoxing(Long customerOrderId, ManualBoxingDto dtos, String authHeader) throws Exception {

        User user = controlService.getUserFromToken(authHeader);

        try {
            Optional<CustomerOrder> optional = customerOrderRepository.findById(customerOrderId);
            if(!optional.isPresent()){
                throw new NotFoundException("Sipariş Kaydı bulunamadı");
            }
            CustomerOrder customerOrder = optional.get();
            Box box = new Box();
            Optional<BoxType> boxOpt = boxTypeRepository.findById(1L);
            box.setBoxType(boxOpt.get());
            box.setStatus(1);
            box.setCustomerOrder(customerOrder);
            box.setCreatedDate(new Date());
            box = boxRepository.save(box);
            box.setBoxNo(this.generateBoxNo(box));
            box = boxRepository.save(box);
            box.setUser(user);
            List<Depot> list = depotRepository.findDrugsForManuelPackaging(customerOrderId, dtos.getCustomerOrderDrugId(), dtos.getQuantity(), dtos.getDrugCardId());
            int drugSize = 0;
            for (Depot depot : list){
                Optional<CustomerOrderDrugs> optCustomerOrderDrugs=customerOrderDrugsRepository.getOneForPackaging(
                        customerOrderId,depot.getDepotId());
                if(!optCustomerOrderDrugs.isPresent()){
                    drugSize++;
                    continue;
                }
                CustomerOrderDrugs cod = optCustomerOrderDrugs.get();
                if(cod.getIncompleteQuantity()<1)
                    continue;
                cod.setChargedQuantity(cod.getChargedQuantity()+1);
                cod.setIncompleteQuantity(cod.getIncompleteQuantity()-1);
                cod = customerOrderDrugsRepository.save(cod);

                BoxDrugList drug = null;
                Optional<BoxDrugList>  optControl=boxDrugListRepository.controlSingleDepot(depot.getDepotId());
                if(optControl.isPresent())
                    drug = optControl.get();
                else
                    drug = new BoxDrugList();
                drug.setDepot(depot);
                drug.setBox(box);
                drug= boxDrugListRepository.save(drug);
                depot.setBoxId(box.getBoxId());
                depot = depotRepository.save(depot);


            }
            BoxDto dto = null;

            if(drugSize == list.size()){
                box.setBoxType(null);
                box.setUser(null);
                box.setCustomerOrder(null);
                box = boxRepository.save(box);
                boxRepository.delete(box);
            }else{
                dto =  mapper.map(box, BoxDto.class);
            }
            return dto;
        }
        catch (Exception e){
            throw e;
        }
    }

    public BoxDto specificBoxForManualBoxing(Long customerOrderId, ManualBoxingDto dtos, String authHeader) throws NotFoundException {

        //User user = controlService.getUserFromToken(authHeader);

        try {
            Optional<CustomerOrder> optional = customerOrderRepository.findById(customerOrderId);
            if(!optional.isPresent()){
                throw new NotFoundException("Sipariş Kaydı bulunamadı");
            }
            CustomerOrder customerOrder = optional.get();
            Optional<Box> optionalBox = boxRepository.findByBoxIdAndCustomerOrder(dtos.getBoxId(), customerOrder);
            if(!optionalBox.isPresent()) {
                return null;
            }

            Box box = optionalBox.get();
            List<Depot> list = depotRepository.findDrugsForManuelPackaging(customerOrderId, dtos.getCustomerOrderDrugId(), dtos.getQuantity(), dtos.getDrugCardId());

            for (Depot depot : list){
                Optional<CustomerOrderDrugs> optCustomerOrderDrugs=customerOrderDrugsRepository.getOneForPackaging(
                        customerOrderId,depot.getDepotId());
                if(!optCustomerOrderDrugs.isPresent())
                    continue;
                CustomerOrderDrugs cod = optCustomerOrderDrugs.get();
                if(cod.getIncompleteQuantity()<1)
                    continue;
                cod.setChargedQuantity(cod.getChargedQuantity()+1);
                cod.setIncompleteQuantity(cod.getIncompleteQuantity()-1);
                cod = customerOrderDrugsRepository.save(cod);
                BoxDrugList drug = null;
                Optional<BoxDrugList>  optControl=boxDrugListRepository.controlSingleDepot(depot.getDepotId());
                if(optControl.isPresent())
                    drug = optControl.get();
                else
                    drug = new BoxDrugList();
                drug.setDepot(depot);
                drug.setBox(box);
                drug= boxDrugListRepository.save(drug);
                depot.setBoxId(box.getBoxId());
                depot = depotRepository.save(depot);


            }
            BoxDto dto = null;

            if(list.size() > 0){
                dto =  mapper.map(box, BoxDto.class);
            }
            return dto;
        }

        catch (Exception e){
            throw e;
        }
    }


    public Boolean removeFakeDrug(BoxFakeDrugDto dto) {
        List<Depot> depotList = depotRepository.findFakeDrugsByBoxId(dto.getDrugQuantity(), dto.getBoxId(), dto.getCustomerOrderId(), dto.getDrugCardId());
        if (!(depotList.size() > 0)) {
            return false;
        }
        for (Depot depot : depotList) {
            if (depot != null) {

                Optional<CustomerOrderDrugs> optCustomerOrderDrugs=customerOrderDrugsRepository.getOneForPackaging(
                        dto.getCustomerOrderId(),depot.getDepotId());
                if(!optCustomerOrderDrugs.isPresent())
                    continue;
                Optional<BoxDrugList> optBoxDrugList = boxDrugListRepository.findByDepot(depot);
                if(optBoxDrugList.isPresent()){
                    CustomerOrderDrugs cod = optCustomerOrderDrugs.get();
                    if(cod.getChargedQuantity()<1)
                        continue;
                    cod.setChargedQuantity(cod.getChargedQuantity()-1);
                    cod.setIncompleteQuantity(cod.getIncompleteQuantity()+1);
                    cod = customerOrderDrugsRepository.save(cod);

                    BoxDrugList boxDrugList = optBoxDrugList.get();
                    if(boxDrugList.getSmallBox() == null){
                        depot.setSmallBoxId(null);
                        boxDrugListRepository.deleteById(boxDrugList.getBoxDrugListId());
                    }
                    else{
                        boxDrugList.setBox(null);
                        boxDrugListRepository.save(boxDrugList);
                    }
                    depot.setBoxId(null);
                    depot.setBoxDrugList(null);
                    depot = depotRepository.save(depot);



                }
            }
        }
        return true;
    }

    public PackagingSingleCustomerOrderDrugsDto getCustomerOrderDrug(Long customerOrderDrugId) {
        PackagingSingleCustomerOrderDrugsDto dto= null;
        Optional<CustomerOrderDrugs> opt = customerOrderDrugsRepository.findById(customerOrderDrugId);

        if(opt.isPresent())
            dto = mapper.map(opt.get(), PackagingSingleCustomerOrderDrugsDto.class);

        return dto ;
    }

    public Boolean addCustomerBoxNo(SingleBoxPropertiesLogisticDto dto) throws Exception{

        Optional<Box> optionalBox = boxRepository.findById(dto.getBoxId());
        if(!optionalBox.isPresent())
            throw new Exception("Kutu Bulunamadı");

        Box box = optionalBox.get();

        box.setCustomerBoxNo(dto.getCustomerBoxNo());
        //box.setExactBoxWeight(dto.getExactBoxWeight());
        box = boxRepository.save(box);

        return true;
    }

    public Boolean addBoxExactWeight(SingleBoxPropertiesLogisticDto dto) throws Exception{

        Optional<Box> optionalBox = boxRepository.findById(dto.getBoxId());
        if(!optionalBox.isPresent())
            throw new Exception("Kutu Bulunamadı");

        Box box = optionalBox.get();

        //box.setCustomerBoxNo(dto.getCustomerBoxNo());
        box.setExactBoxWeight(dto.getExactBoxWeight());
        box = boxRepository.save(box);

        return true;
    }


}
