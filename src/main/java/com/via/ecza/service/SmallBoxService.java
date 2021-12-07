package com.via.ecza.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.text.Font;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.via.ecza.config.AppConfiguration;
import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
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
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
@Transactional
public class SmallBoxService {

    @Autowired
    private SmallBoxRepository smallBoxRepository;
    @Autowired
    private DepotRepository depotRepository;
    @Autowired
    private BoxDrugListRepository boxDrugListRepository;
    @Autowired
    private DepotStatusRepository depotStatusRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private QrCodeRepository qrCodeRepository;
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private CustomerSupplyOrderRepository customerSupplyOrderRepository;
    @Autowired
    private CustomerOrderDrugsRepository customerOrderDrugsRepository;
    @Autowired
    AppConfiguration app;
    @Autowired
    private ControlService controlService;
    @Autowired
    private UserCameraRepository userCameraRepository;

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    // dokunulmayacak
    public List<PackagingDepotDto> getAllDepotForSmallBox(String authHeader, PrePackageSearchDto dto) throws Exception {
        List<PackagingDepotDto> packageList = new ArrayList<>();

        Optional<CustomerOrder> opt = customerOrderRepository.findById(dto.getCustomerOrderId());
        if (!opt.isPresent()) {
            throw new NotFoundException("Sipariş Kaydı bulunamadı");
        }
        CustomerOrder customerOrder = opt.get();

        User user = controlService.getUserFromToken(authHeader);
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);
        if (!optUserCamera.isPresent()) {
            throw new Exception("Kullanıcı Kamera Kaydı Bulunamadı !");
        }

        List<QrCode> qrCodeList = qrCodeRepository.findByCameraType(optUserCamera.get().getCameraType());

        for (QrCode qrCode : qrCodeList) {
            PrePackage prePackage = seperateQrCode(qrCode);
            if (prePackage == null) continue;
            prePackage.setCustomerOrder(customerOrder);

            Optional<Depot> optionalDepot = depotRepository.findSingleDepotForSmallBox(
                    prePackage.getDrugLotNo(),
                    prePackage.getDrugBarcode().toString(),
                    prePackage.getDrugSerialNo().toString());
            if (optionalDepot.isPresent()) {
                PackagingDepotDto packagingDepotDto = mapper.map(optionalDepot.get(), PackagingDepotDto.class);
                packageList.add(packagingDepotDto);
            }
        }

        return packageList;
    }

    // dokunulmayacak
    public List<PackagingDepotDto> getAllDepotForSmallBoxWithStatusOne(String authHeader, PrePackageSearchDto dto) throws Exception {
        List<PackagingDepotDto> packageList = new ArrayList<>();

        Optional<CustomerSupplyOrder> opt = customerSupplyOrderRepository.findById(dto.getCustomerSupplyOrderId());
        if (!opt.isPresent()) {
            throw new NotFoundException("Satın Alma Kaydı bulunamadı");
        }
        CustomerSupplyOrder customerSupplyOrder = opt.get();
        //Kullanıcı Bilgisi Alındı
        User user = controlService.getUserFromToken(authHeader);
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);
        if (!optUserCamera.isPresent()) {
            throw new Exception("Kullanıcı Kamera Kaydı Bulunamadı !");
        }
        List<QrCode> qrCodeList = qrCodeRepository.findByCameraTypeAndStatus(optUserCamera.get().getCameraType(), 1);

        for (QrCode qrCode : qrCodeList) {
            PrePackage prePackage = seperateQrCode(qrCode);
            if (prePackage == null) continue;
            prePackage.setCustomerSupplyOrder(customerSupplyOrder);
            Long customerOrderId = customerSupplyOrder.getPurchaseOrderDrugs().getCustomerOrder().getCustomerOrderId();
            Optional<Depot> optionalDepot = depotRepository.findSingleDepotForSmallBox(
                    customerOrderId,
                    prePackage.getDrugLotNo(),
                    prePackage.getDrugBarcode().toString(),
                    prePackage.getDrugSerialNo().toString());
            if (optionalDepot.isPresent()) {
                PackagingDepotDto packagingDepotDto = mapper.map(optionalDepot.get(), PackagingDepotDto.class);
                packageList.add(packagingDepotDto);
            }
        }
        return packageList;
    }

    //test edildi sıkıntı yok
    public SmallBoxDto transferToSmallBoxWithCustomerSupplyOrder(Long customerSupplyOrderId, List<PrePackageTransferDto> dtos, String authHeader) throws Exception {
        Optional<CustomerSupplyOrder> optionalCustomerSupplyOrder = customerSupplyOrderRepository.findById(customerSupplyOrderId);
        if (!optionalCustomerSupplyOrder.isPresent()) {
            throw new NotFoundException("Satın Alma Kaydı bulunamadı");
        }
        CustomerSupplyOrder customerSupplyOrder = optionalCustomerSupplyOrder.get();
//        Long customerOrderId = customerSupplyOrder.getPurchaseOrderDrugs().getCustomerOrder().getCustomerOrderId();

//        Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(customerOrderId);
//        if(!optionalCustomerOrder.isPresent())
//            throw new Exception("Müşteri Siparişi Bulunamadı");

//        User user = controlService.getUserFromToken(authHeader);

        if (!(dtos.size() > 0)) {
            return null;
        }
        SmallBox smallBox = new SmallBox();
        smallBox.setCustomerOrder(customerSupplyOrder.getPurchaseOrderDrugs().getCustomerOrder());
        smallBox.setCreatedDate(new Date());
        smallBox.setStatus(1);
        smallBox = smallBoxRepository.save(smallBox);
        smallBox.setSmallBoxNo(this.generateSmallBoxNo(smallBox));
        smallBox = smallBoxRepository.save(smallBox);
        //smallBox.setUser(user);
        for (PrePackageTransferDto dto : dtos) {
            if (dto == null)
                continue;
            if (dto.getValue()) {
                Optional<Depot> opt = depotRepository.findById(dto.getDepotId());

                if (opt.isPresent()) {
                    Depot depot = opt.get();
//                    Optional<DepotStatus> optStatus = depotStatusRepository.findById(2L);
//                    if (!optStatus.isPresent())
//                        continue;
//                    if (depot.getDepotStatus().getDepotStatusId() == optStatus.get().getDepotStatusId())
//                        continue;
//                    depot.setDepotStatus(optStatus.get());
                    if (depot.getSmallBoxId() != null)
                        continue;
                    depot = depotRepository.save(depot);

                    BoxDrugList drug = new BoxDrugList();
                    drug.setDepot(depot);
                    drug.setSmallBox(smallBox);
                    drug = boxDrugListRepository.save(drug);
                    depot.setSmallBoxId(smallBox.getSmallBoxId());
                    depot.setBoxDrugList(drug);
                    depot = depotRepository.save(depot);
                    //prePackageRepository.delete(p);
                }
                //}
            }
        }
        SmallBoxDto dto = mapper.map(smallBox, SmallBoxDto.class);
        return dto;
    }

    public SmallBoxDto transferToSmallBox(List<PrePackageTransferDto> dtos, String authHeader) throws Exception {

        //User user = controlService.getUserFromToken(authHeader);

        if (!(dtos.size() > 0)) {
            return null;
        }
        SmallBox smallBox = new SmallBox();
        smallBox.setCreatedDate(new Date());
        smallBox.setStatus(1);
        smallBox = smallBoxRepository.save(smallBox);
        smallBox.setSmallBoxNo(this.generateSmallBoxNo(smallBox));
        smallBox = smallBoxRepository.save(smallBox);
        CustomerOrder customerOrder = null;
        //smallBox.setUser(user);
        for (PrePackageTransferDto dto : dtos) {
            if (dto == null)
                continue;
            if (dto.getValue()) {
                Optional<Depot> opt = depotRepository.findById(dto.getDepotId());

                if (opt.isPresent()) {
                    Depot depot = opt.get();
//                    Optional<DepotStatus> optStatus = depotStatusRepository.findById(2L);
//                    if (!optStatus.isPresent())
//                        continue;
//                    if (depot.getDepotStatus().getDepotStatusId() == optStatus.get().getDepotStatusId())
//                        continue;
//                    depot.setDepotStatus(optStatus.get());
                    if (depot.getSmallBoxId() != null)
                        continue;
                    depot = depotRepository.save(depot);
                    customerOrder = depot.getCustomerOrder();
                    BoxDrugList drug = new BoxDrugList();
                    drug.setDepot(depot);
                    drug.setSmallBox(smallBox);
                    drug = boxDrugListRepository.save(drug);
                    depot.setSmallBoxId(smallBox.getSmallBoxId());
                    depot.setBoxDrugList(drug);
                    depot = depotRepository.save(depot);
                    //prePackageRepository.delete(p);
                }
                //}
            }
        }
        smallBox.setCustomerOrder(customerOrder);
        smallBox = smallBoxRepository.save(smallBox);
        SmallBoxDto dto = mapper.map(smallBox, SmallBoxDto.class);
        return dto;
    }

    // dokunulmayacak
    private String generateSmallBoxNo(SmallBox smallBox) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String code = "SBX-" + year;
        int size = smallBox.getSmallBoxId().toString().length();
        for (int i = 0; i < 7 - size; i++)
            code += "0";
        code += smallBox.getSmallBoxId();
        return code;
    }

    // dokunulmayacak
    public Page<SmallBoxDto> searchSmallBoxes(Pageable page, SmallBoxSearchDto dto) {
        String smallBoxNo = null;
        if(dto.getSmallBoxNo() != null){
            smallBoxNo = dto.getSmallBoxNo().replace("SBX*","SBX-");
            dto.setSmallBoxNo(smallBoxNo);
        }

        StringBuilder createSqlQuery = new StringBuilder("select sb.* from small_box sb  ");
        if (dto.getCustomerOrderNo() != null) {
            if (!dto.getCustomerOrderNo().trim().equals(""))
                createSqlQuery.append("join customer_order co on co.customer_order_id = sb.customer_order_id ");
        }
        createSqlQuery.append("join box_drug_list bdl on bdl.small_box_id = sb.small_box_id where sb.status=1");
        if (dto.getSmallBoxNo() != null)
            if (!dto.getSmallBoxNo().trim().equals(""))
                createSqlQuery.append("and sb.small_box_no ILIKE '%" + dto.getSmallBoxNo().trim() + "%' ");
        if (dto.getCustomerOrderNo() != null) {
            if (!dto.getCustomerOrderNo().trim().equals(""))
                createSqlQuery.append("and co.customer_order_no ILIKE '%" + dto.getCustomerOrderNo().trim() + "%' ");
        }
        createSqlQuery.append(" group by sb.small_box_id order by sb.customer_order_id asc");

        List<SmallBox> list = entityManager.createNativeQuery(createSqlQuery.toString(), SmallBox.class).getResultList();

        SmallBoxDto[] dtos = mapper.map(list, SmallBoxDto[].class);
        List<SmallBoxDto> dtosList = Arrays.asList(dtos);

        int start = Math.min((int) page.getOffset(), dtosList.size());
        int end = Math.min((start + page.getPageSize()), dtosList.size());

        Page<SmallBoxDto> pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());
        pageList.forEach(data -> {
            data.setDrugQuantity(data.getBoxDrugList().size());
        });
        return pageList;
    }

    // dokunulmayacak
    public Page<PackagingCustomerOrderListDto> searchCustomerOrder(Pageable page, PackagingCustomerOrderSearchDto dto) throws NotFoundException {
        Page<CustomerOrder> orders = null;
        if (dto.getCustomerOrderNo() == null) {
            if (dto.getCustomerOrderNo().trim().equals(""))
                orders = customerOrderRepository.getAllCustomerOrderForPackaging(page);
        } else {
            orders = customerOrderRepository.getPageableCustomerOrderForPackaging(dto.getCustomerOrderNo().trim(), page);
        }
        Page<PackagingCustomerOrderListDto> pageList = orders.map(PackagingCustomerOrderListDto::new);

        return pageList;
    }


    // dokunulmayacak
    public Page<PackagingCustomerSupplyOrderDto> getAllCustomerSupplyOrdersForPackaging(Pageable page, Long customerOrderId) throws Exception {

        Optional<CustomerOrder> optCustumerOrder = customerOrderRepository.findById(customerOrderId);
        if (!optCustumerOrder.isPresent())
            throw new NotFoundException("Sipariş kaydı bulunamadı.");

        Page<CustomerSupplyOrder> supplyOrders = customerSupplyOrderRepository.getAllOrdersForPackaging(
                optCustumerOrder.get().getCustomerOrderId(), page);
        Page<PackagingCustomerSupplyOrderDto> pageList = supplyOrders.map(PackagingCustomerSupplyOrderDto::new);
        return pageList;
    }

    // dokunulmayacak
    public List<SmallBoxListDetailDto> getBoxDrugListBySmallBox(Long smallBoxId) throws Exception {

        if (smallBoxId == null) return null;
        if (!(smallBoxId > 0)) return null;
        List<SmallBoxListDetailDto> dtosList = new ArrayList<SmallBoxListDetailDto>();

        List<Object[]> listResult = smallBoxRepository.getSmallBoxCount(smallBoxId);

        for (Object[] objArr : listResult) {
            SmallBoxListDetailDto dto = new SmallBoxListDetailDto();
            dto.setCount((BigInteger) objArr[0]);
            dto.setDrugName((String) objArr[1]);
            dto.setDrugCode((BigInteger) objArr[2]);
            dto.setExpirationDate((Date) objArr[3]);

            dtosList.add(dto);
        }
        return dtosList;
    }

    //test edildi sıkıntı yok
    public Boolean removeFromSmallBox(List<BoxDrugCheckListDto> dtoList) {

        if (!(dtoList.size() > 0)) {
            return false;
        }
        for (BoxDrugCheckListDto dto : dtoList) {
            if (dto != null)
                if (dto.getValue()) {
                    Optional<BoxDrugList> optBox = boxDrugListRepository.findById(dto.getBoxDrugListId());
                    if (optBox.isPresent()) {
                        BoxDrugList boxDrugList = optBox.get();
                        Optional<Depot> opt = depotRepository.findById(boxDrugList.getDepot().getDepotId());
                        if (opt.isPresent()) {
                            Depot depot = opt.get();
//                            Optional<DepotStatus> optStatus = depotStatusRepository.findById(1L);
//                            if (!optStatus.isPresent())
//                                continue;
//                            depot.setDepotStatus(optStatus.get());
                            depot.setSmallBoxId(null);
                            depot = depotRepository.save(depot);
                            boxDrugList.setSmallBox(null);
                            boxDrugList.setDepot(null);
                            boxDrugList = boxDrugListRepository.save(boxDrugList);
                            boxDrugListRepository.deleteById(boxDrugList.getBoxDrugListId());
                        }
                    }
                }
        }
        return true;
    }

    //@org.springframework.transaction.annotation.Transactional
    public List<PackagingDepotDto> removeFromSmallBoxWithQrCode(String authHeader, PrePackageSearchDto dto) throws Exception {
        List<PackagingDepotDto> packageList = null;
//        Optional<DepotStatus> depotStatus = depotStatusRepository.findById(1L);
//        if (!depotStatus.isPresent()) {
//            return packageList;
//        }

        User user = controlService.getUserFromToken(authHeader);
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);
        if (!optUserCamera.isPresent()) {
            throw new Exception("Kullanıcı Kamera Kaydı Bulunamadı !");
        }

        List<QrCode> qrCodeList = qrCodeRepository.findByCameraType(optUserCamera.get().getCameraType());
        if (qrCodeList == null || !(qrCodeList.size() > 0))
            return packageList;
        SmallBox smallBox = null;
        packageList = new ArrayList<>();
        for (QrCode qrCode : qrCodeList) {
            PrePackage prePackage = seperateQrCode(qrCode);
            if (prePackage == null) continue;

            Optional<Depot> optionalDepot = depotRepository.controlDepotForRemovingFromSmallBox(
                    prePackage.getDrugLotNo(),
                    prePackage.getDrugBarcode().toString(),
                    prePackage.getDrugSerialNo().toString());
            if (optionalDepot.isPresent()) {
                Depot depot = optionalDepot.get();
                Optional<BoxDrugList> boxDrugList = boxDrugListRepository.findByDepot(depot);
                if (boxDrugList.isPresent()) {

                    smallBox = boxDrugList.get().getSmallBox();
                    depot.setBoxDrugList(null);
                    depot = depotRepository.save(depot);
                    Long id = boxDrugList.get().getBoxDrugListId();
                    boxDrugListRepository.deleteById(id);
                    depot.setSmallBoxId(null);
//                    depot.setDepotStatus(depotStatus.get());
                    depot = depotRepository.save(depot);

                    PackagingDepotDto packagingDepotDto = mapper.map(optionalDepot.get(), PackagingDepotDto.class);
                    packageList.add(packagingDepotDto);
                } else {
                    continue;
                }

            }
        }

        return packageList;
    }

    // dokunulmayacak
    private PrePackage seperateQrCode(QrCode code) throws Exception {
        String qrCode;
        int secondGroupSeperatorIndex = 0;
        StringBuilder barcode = new StringBuilder();
        StringBuilder serialNo = new StringBuilder();
        StringBuilder expirationDate = new StringBuilder();
        StringBuilder lotNo = new StringBuilder();
        StringBuilder itsNo = new StringBuilder();
        qrCode = code.getQrCode();

        if (qrCode == null || qrCode.trim().length() < 1)
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
        if (!drug.isPresent())
            return null;
        prePackage.setDrugName(drug.get().getDrugName());
        //prePackage = prePackageRepository.save(prePackage);
        return prePackage;
    }

    // dokunulmayacak
    public FileDto createBarcode(String smallBoxNo) throws Exception {
        Optional<SmallBox> optSmallBox = smallBoxRepository.findBySmallBoxNo(smallBoxNo);
        // The data that the QR code will contain
        String data = optSmallBox.get().getSmallBoxNo();
        // The path where the image will get saved
        String path = app.getUploadPath() + "/" + app.getUploadBarcodePath() + "/" + data + ".png";
        // Encoding charset
        String charset = "UTF-8";
        Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
        hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        // Create the QR code and save in the specified folder as a jpg file
        createQR(data, path, charset, hashMap, 200, 400);

        FileDto fileDto = new FileDto();
        byte[] bytes = Files.readAllBytes(Paths.get((path)));
        fileDto.setFile(bytes);
        fileDto.setFileName(path);
        return fileDto;
    }

    // dokunulmayacak
    public static void createQR(String data, String path, String charset, Map hashMap, int height, int width) throws Exception {
        BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset),
                BarcodeFormat.CODE_128, width, height);
        MatrixToImageWriter.writeToFile(matrix, path.substring(path.lastIndexOf('.') + 1), new File(path));
    }

    // dokunulmayacak
    public Boolean deleteBarcode(String smallBoxNo) {

        Optional<SmallBox> optSmallBox = smallBoxRepository.findBySmallBoxNo(smallBoxNo);
        if (!optSmallBox.isPresent()) return false;

        // The data that the QR code will contain
        String data = optSmallBox.get().getSmallBoxNo() + ".png";
        // The path where the image will get saved
        String path = app.getUploadPath() + "/" + app.getUploadBarcodePath() + "/" + data;

        File barcode = new File(path);
        if (barcode.delete()) {
            return true;
        }
        return false;

    }

    // dokunulmayacak
    public Boolean deleteQrCodes(int status) {

        qrCodeRepository.deleteByStatus(status);

        return true;
    }

    // dokunulmayacak
    public Page<PackagingCustomerOrderDrugsDto> searchCustomerOrderDrugs(Pageable page, PackagingCustomerOrderSearchDto dto) throws NotFoundException {
        Page<CustomerOrderDrugs> drugs = null;
        if (dto.getDrugCardId() == null)
            drugs = customerOrderDrugsRepository.getDrugsForPackaging(page);
        else if (dto.getDrugCardId() < 1L)
            drugs = customerOrderDrugsRepository.getDrugsForPackaging(page);
        else
            drugs = customerOrderDrugsRepository.getDrugsForPackagingWithDrug(dto.getDrugCardId(), page);

        //customer bilgisi kopyalanıyor
        List<SingleCustomerDto> customerList=new ArrayList<>();
        AtomicInteger counter= new AtomicInteger();
        counter.set(0);
        drugs.forEach(data ->{
            SingleCustomerDto singleCustomer=mapper.map(data.getCustomerOrder().getCustomer(),SingleCustomerDto.class);
           customerList.add(counter.get(),singleCustomer);
           counter.set(counter.get() + 1);
        });
        //customer bilgisi kopyalama son


        Page<PackagingCustomerOrderDrugsDto> pageList = drugs.map(PackagingCustomerOrderDrugsDto::new);
        counter.set(0);
        pageList.forEach(data ->{
            data.setDepotCountWithSmallBox(depotRepository.depotCountByCustomerOrderAndCustomerOrderDrugId(
                    data.getCustomerOrderDrugId(),
                    data.getDrugCard().getDrugCardId()));

            //customer bilgisi sety ediliyor
            data.getCustomerOrder().setCustomer(customerList.get(counter.get()));
            counter.set(counter.get() + 1);
        });
        return pageList;
    }

    public SmallBoxDto manualSmallBoxing(ManualSmallBoxingDto dtos, String authHeader) throws Exception {

        User user = controlService.getUserFromToken(authHeader);

        SmallBox smallBox = new SmallBox();
        smallBox.setStatus(1);
        smallBox.setCustomerOrder(customerOrderRepository.findById(dtos.getCustomerOrderId()).get());
        smallBox.setCreatedDate(new Date());
        smallBox = smallBoxRepository.save(smallBox);
        smallBox.setSmallBoxNo(this.generateSmallBoxNo(smallBox));
        smallBox = smallBoxRepository.save(smallBox);
        //smallBox.setUser(user);
        List<Depot> list = depotRepository.findDrugsForManuelSmallBoxing(dtos.getCustomerOrderId(), dtos.getCustomerOrderDrugId(), dtos.getQuantity(), dtos.getDrugCardId());

        for (Depot depot : list) {


            BoxDrugList drug = new BoxDrugList();
            drug.setDepot(depot);
            drug.setSmallBox(smallBox);
            drug = boxDrugListRepository.save(drug);
            depot.setSmallBoxId(smallBox.getSmallBoxId());
            depot.setBoxDrugList(drug);
            depot = depotRepository.save(depot);
        }

        SmallBoxDto dto = mapper.map(smallBox, SmallBoxDto.class);
        return dto;
    }

    public List<PackagingDepotDto> depotBySmallBoxNo(PackagingCustomerOrderSearchDto dto) throws Exception {

        if (dto.getSmallboxNo() == null)
            return null;

        if (dto.getSmallboxNo().trim().equals(""))
            return null;

        StringBuilder createSqlQuery = new StringBuilder("select * from depot d join small_box sb on sb.small_box_id = d.small_box_id ");
        createSqlQuery.append("where d.box_id is null and sb.customer_order_id="+dto.getCustomerOrderId()+" and sb.small_box_no ILIKE '%" + dto.getSmallboxNo().trim() + "%' ");

        List<Depot> list = entityManager.createNativeQuery(createSqlQuery.toString(), Depot.class).getResultList();

        List<PackagingDepotDto> dtoList = null;
//      List<Depot> optionalDepot = depotRepository.drugsWithSmallBoxNo(smallBoxNo);
//        if(optionalDepot == null){
//            throw new NotFoundException("İlaç Bulunamadı");
//        }
//        if(optionalDepot.size() <1){
//            throw new NotFoundException("İlaç Bulunamadı");
//        }
        PackagingDepotDto[] listDtos = mapper.map(list, PackagingDepotDto[].class);
        dtoList = Arrays.asList(listDtos);

        return dtoList;
    }

    public String createSmallBoxExcel(SmallBoxFileDto dto) throws Exception {

        try {
            //ARAMA BAŞLANGIÇ
            StringBuilder createSqlQuery = new StringBuilder("select * from small_box sb  join customer_order co ");
            createSqlQuery.append(" on co.customer_order_id = sb.customer_order_id where sb.status = 1 ");
            if (dto.getSmallBoxNo() != null)
                if (!dto.getSmallBoxNo().equals(""))
                    createSqlQuery.append("and small_box_no = '" + dto.getSmallBoxNo().trim() + "' ");
//            if(dto.getCustomerOrderNo() != null  )
//                if(!dto.getCustomerOrderNo().equals(""))
//                    createSqlQuery.append("and customer_order_no = '"+dto.getCustomerOrderNo().trim()+"' ");

            createSqlQuery.append(" order by sb.small_box_no");

            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), SmallBox.class).getResultList();

            SmallBox[] smallBoxes = mapper.map(list, SmallBox[].class);
            List<SmallBox> smallBoxList = Arrays.asList(smallBoxes);
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

            int a = 4;
            int b = 0;
            for (SmallBox smallBox : smallBoxList) {
                a++;
                b++;
                XSSFRow row = sheet.createRow((short) a);
                row.createCell(0).setCellValue(b);
                row.getCell(0).setCellStyle(csBody);
                //İçerik Kısmı Bu Satırdan İtibaren Obje Bilgileri İle Doldurulur
                row.createCell(1).setCellValue(smallBox.getSmallBoxNo());
                row.getCell(1).setCellStyle(csBody);
                row.createCell(2).setCellValue(smallBox.getCustomerOrder().getCustomerOrderNo());
                row.getCell(2).setCellStyle(csBody);
                row.createCell(3).setCellValue(String.valueOf(smallBox.getBoxDrugList().size()));
                row.getCell(3).setCellStyle(csBody);
            }

            String excelTitle = "box";
            if (dto.getSmallBoxNo() != null)
                if (!dto.getSmallBoxNo().equals(""))
                    excelTitle += "_" + dto.getSmallBoxNo();
            if (dto.getCustomerOrderNo() != null)
                if (!dto.getCustomerOrderNo().equals(""))
                    excelTitle += "_" + dto.getCustomerOrderNo();
            excelTitle += ".xlsx";

            FileOutputStream fileOut = new FileOutputStream("docs/" + excelTitle);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            return excelTitle;
        } catch (Exception e) {
            throw new Exception("Paket Excel Oluşturma İşleminde Hata Oluştu.", e);
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
        rowHeader.createCell(0).setCellValue("Paket Listesi");
        rowHeader.getCell(0).setCellStyle(csHeading);

        sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 1));
        //son parametre kolon sayısına eşit olmalı
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 4));


        XSSFRow rowhead = sheet.createRow((short) 4);

        //Kolon İsimleri Belirtilir İhtiyaç Olursa Kolonlar Genişletilir
        rowhead.createCell(0).setCellValue("No");
        rowhead.getCell(0).setCellStyle(csHeader);
        sheet.setColumnWidth(0, 1500);//kolon genişletildi
        rowhead.createCell(1).setCellValue("Paket Numarası");
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
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);

    }

    public String createSmallBoxListExcel(Long smallBoxId) throws Exception {

        try {
            //ARAMA BAŞLANGIÇ
            Optional<SmallBox> smallBox = smallBoxRepository.findById(smallBoxId);
            if (!smallBox.isPresent())
                throw new NotFoundException("Paket bulunamadı");
            List<BoxDrugList> boxDrugList = new ArrayList<>(smallBox.get().getBoxDrugList());
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

            addExcelHeaderForList(workbook, sheet, smallBox.get().getSmallBoxNo());
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");


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
                row.createCell(2).setCellValue(entity.getDepot().getDrugBarcode());
                row.getCell(2).setCellStyle(csBody);
                row.createCell(3).setCellValue(entity.getDepot().getSerialNumber());
                row.getCell(3).setCellStyle(csBody);
                row.createCell(4).setCellValue(dateFormat.format(entity.getDepot().getExpirationDate()));
                row.getCell(4).setCellStyle(csBody);
                row.createCell(5).setCellValue(entity.getDepot().getLotNo());
                row.getCell(5).setCellStyle(csBody);
            }

            String excelTitle = smallBox.get().getSmallBoxNo();

            FileOutputStream fileOut = new FileOutputStream("docs/SBX_" + excelTitle + ".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            return excelTitle;
        } catch (Exception e) {
            throw new Exception("Paket İlaç Listesi Excel Oluşturma İşleminde Hata Oluştu.", e);
        }
    }

    public String createSmallBoxPackingListExcel(Long smallBoxId) throws Exception {

        try {
            //ARAMA BAŞLANGIÇ
            Optional<SmallBox> smallBox = smallBoxRepository.findById(smallBoxId);
            if (!smallBox.isPresent())
                throw new NotFoundException("Paket bulunamadı");
            List<SmallBoxDrugListGroupDto> smallBoxDrugList = new ArrayList<SmallBoxDrugListGroupDto>();
            List<Object[]> dtosList = depotRepository.getSmallBoxDrugs(smallBoxId);

            for (Object[] objArr : dtosList) {

                SmallBoxDrugListGroupDto dto = new SmallBoxDrugListGroupDto();
                dto.setDrugName((String) objArr[0]);
                dto.setLotNo((String) objArr[1]);
                dto.setCount((BigInteger) objArr[2]);
                dto.setExpirationDate((Date) objArr[3]);

                smallBoxDrugList.add(dto);
            }
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

            addExcelHeaderForPackingList(workbook, sheet, smallBox.get().getSmallBoxNo());
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");


            int a = 5;
            int b = 0;
            for (SmallBoxDrugListGroupDto entity : smallBoxDrugList) {
                a++;
                b++;
                XSSFRow row = sheet.createRow((short) a);
                row.createCell(0).setCellValue(b);
                row.getCell(0).setCellStyle(csBody);
                //İçerik Kısmı Bu Satırdan İtibaren Obje Bilgileri İle Doldurulur
                row.createCell(1).setCellValue(entity.getDrugName());
                row.getCell(1).setCellStyle(csBody);
                row.createCell(2).setCellValue(entity.getLotNo());
                row.getCell(2).setCellStyle(csBody);
                row.createCell(3).setCellValue(String.valueOf(entity.getCount()));
                row.getCell(3).setCellStyle(csBody);
                row.createCell(4).setCellValue(sdf.format(entity.getExpirationDate()));
                row.getCell(4).setCellStyle(csBody);

            }

            String excelTitle = smallBox.get().getSmallBoxNo();

            FileOutputStream fileOut = new FileOutputStream("docs/SBX_Packing_" + excelTitle + ".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            return excelTitle;
        } catch (Exception e) {
            throw new Exception("Paket İlaç Listesi Excel Oluşturma İşleminde Hata Oluştu.", e);
        }
    }

    private void addExcelHeaderForPackingList(XSSFWorkbook workbook, XSSFSheet sheet, String smallBoxNo) throws IOException {
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
        rowHeader.createCell(0).setCellValue("Paket Listesi");
        rowHeader.getCell(0).setCellStyle(csHeading);

        sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 1));
        //son parametre kolon sayısına eşit olmalı
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 5));


        XSSFRow rowhead = sheet.createRow((short) 5);

        //Kolon İsimleri Belirtilir İhtiyaç Olursa Kolonlar Genişletilir
        rowhead.createCell(0).setCellValue("No");
        rowhead.getCell(0).setCellStyle(csHeader);
        sheet.setColumnWidth(0, 1500);//kolon genişletildi
        rowhead.createCell(1).setCellValue("İlaç Adı");
        rowhead.getCell(1).setCellStyle(csHeader);
        sheet.setColumnWidth(1, 10000);
        rowhead.createCell(2).setCellValue("Parti Numarası");
        rowhead.getCell(2).setCellStyle(csHeader);
        sheet.setColumnWidth(2, 8000);
        rowhead.createCell(3).setCellValue("Adet");
        rowhead.getCell(3).setCellStyle(csHeader);
        sheet.setColumnWidth(3, 8000);
        rowhead.createCell(4).setCellValue("Son Kullanma Tarihi");
        rowhead.getCell(4).setCellStyle(csHeader);
        sheet.setColumnWidth(4, 8000);



        //A4 Sayfaya Sığdırma
        sheet.setFitToPage(true);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);

    }

    private void addExcelHeaderForList(XSSFWorkbook workbook, XSSFSheet sheet, String smallBoxNo) throws IOException {
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
        rowHeader.createCell(0).setCellValue("Package Drugs List " + smallBoxNo);
        rowHeader.getCell(0).setCellStyle(csHeading);

        sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 1));
        //son parametre kolon sayısına eşit olmalı
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 5));


        XSSFRow rowhead = sheet.createRow((short) 6);

        //Kolon İsimleri Belirtilir İhtiyaç Olursa Kolonlar Genişletilir
        rowhead.createCell(0).setCellValue("Number");
        rowhead.getCell(0).setCellStyle(csHeader);
        sheet.setColumnWidth(0, 1500);//kolon genişletildi
        rowhead.createCell(1).setCellValue("Drug Name");
        rowhead.getCell(1).setCellStyle(csHeader);
        sheet.setColumnWidth(1, 10000);
        rowhead.createCell(2).setCellValue("Drug Barcode");
        rowhead.getCell(2).setCellStyle(csHeader);
        sheet.setColumnWidth(2, 8000);
        rowhead.createCell(3).setCellValue("Drug Serial Number");
        rowhead.getCell(3).setCellStyle(csHeader);
        sheet.setColumnWidth(3, 8000);
        rowhead.createCell(4).setCellValue("Drug Expiration Date");
        rowhead.getCell(4).setCellStyle(csHeader);
        sheet.setColumnWidth(4, 8000);
        rowhead.createCell(5).setCellValue("Drug Lot Number");
        rowhead.getCell(5).setCellStyle(csHeader);
        sheet.setColumnWidth(5, 8000);


        //A4 Sayfaya Sığdırma
        sheet.setFitToPage(true);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);

    }

    public CommunicationDataDto getCommunicationDataFromSmallBox(Long smallBoxId) {

        if (smallBoxId == null)
            return null;
        CommunicationDataDto dto = new CommunicationDataDto();
        List<Object[]> data = smallBoxRepository.getCommunicationData(smallBoxId);
        dto.setTotalQuantity((BigInteger) data.get(0)[0]);
        dto.setBarcode((String) data.get(0)[1]);
        dto.setExpirationDate((Date) data.get(0)[2]);

        return dto;
    }

    public Boolean removeFakeDrug(SmallBoxFakeDrugDto dto) {
        List<Depot> depotList = depotRepository.findFakeDrugsBySmallBoxId(dto.getDrugQuantity(), dto.getSmallBoxId());
        if (!(depotList.size() > 0)) {
            return false;
        }
        for (Depot depot : depotList) {
            if (depot != null) {
                Optional<BoxDrugList> opt = boxDrugListRepository.findByDepot(depot);
                if (opt.isPresent()) {
                    BoxDrugList boxDrugList = opt.get();
                    //Depot depot = opt.get();
                    depot.setSmallBoxId(null);
                    depot = depotRepository.save(depot);
                    boxDrugList.setSmallBox(null);
                    boxDrugList.setDepot(null);
                    boxDrugList = boxDrugListRepository.save(boxDrugList);
                    boxDrugListRepository.deleteById(boxDrugList.getBoxDrugListId());
                }
            }
        }
        return true;
    }

    // dokunulmayacak
    public List<PackagingDepotDto> getAllDepotForSmallBoxWithStatusOneForStockAccounting(String authHeader, PrePackageSearchForStockAccountingDto dto) throws Exception {
        List<PackagingDepotDto> packageList = new ArrayList<>();

//        Optional<CustomerSupplyOrder> opt = customerSupplyOrderRepository.findSingleCustomerSupplyOrderForSmallBoxStockCounting(dto.getItsNo());
//        if (!opt.isPresent()) {
//            throw new NotFoundException("Satın Alma Kaydı Bulunamadı");
//        }
//        CustomerSupplyOrder customerSupplyOrder = opt.get();

        //Kullanıcı Bilgisi Alındı
        User user = controlService.getUserFromToken(authHeader);
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);
        if (!optUserCamera.isPresent()) {
            throw new Exception("Kullanıcı Kamera Kaydı Bulunamadı !");
        }
        List<QrCode> qrCodeList = qrCodeRepository.findByCameraTypeAndStatus(optUserCamera.get().getCameraType(), 1);

        for (QrCode qrCode : qrCodeList) {
            PrePackage prePackage = seperateQrCode(qrCode);
            if (prePackage == null) continue;
            //prePackage.setCustomerSupplyOrder(customerSupplyOrder);
            //Long customerOrderId = dto.getCustomerOrderId();
            Optional<Depot> optionalDepot = depotRepository.findSingleDepotForSmallBoxToStockCounting(
//                    customerOrderId,
//                    prePackage.getDrugLotNo(),
                    prePackage.getDrugBarcode().toString(),
                    prePackage.getDrugSerialNo().toString());
            prePackage.setCustomerSupplyOrder(optionalDepot.get().getCustomerSupplyOrder());
//            CustomerSupplyOrder customerSupplyOrder = customerSupplyOrderRepository.findCsoFromDepot(optionalDepot.get().getCustomerSupplyOrder().getCustomerSupplyOrderId(),);
            if (optionalDepot.isPresent()) {
                PackagingDepotDto packagingDepotDto = mapper.map(optionalDepot.get(), PackagingDepotDto.class);
                packageList.add(packagingDepotDto);
            }
        }

        return packageList;
    }

    //test edildi sıkıntı yok
    public SmallBoxDto transferToSmallBoxWithCustomerSupplyOrderForStockCounting(List<PrePackageTransferDto> dtos, String authHeader) throws Exception {

        Optional<CustomerSupplyOrder> optionalCustomerSupplyOrder = customerSupplyOrderRepository.findSingleCustomerSupplyOrderForSmallBoxStockCounting(dtos.get(0).getDepotId());
        if (!optionalCustomerSupplyOrder.isPresent()) {
            throw new NotFoundException("Satın Alma Kaydı bulunamadı");
        }
        CustomerSupplyOrder customerSupplyOrder = optionalCustomerSupplyOrder.get();
//        Long customerOrderId = customerSupplyOrder.getPurchaseOrderDrugs().getCustomerOrder().getCustomerOrderId();

//        Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(customerOrderId);
//        if(!optionalCustomerOrder.isPresent())
//            throw new Exception("Müşteri Siparişi Bulunamadı");

//        User user = controlService.getUserFromToken(authHeader);

        if (!(dtos.size() > 0)) {
            return null;
        }
        SmallBox smallBox = new SmallBox();
        smallBox.setCustomerOrder(customerSupplyOrder.getPurchaseOrderDrugs().getCustomerOrder());
        smallBox.setCreatedDate(new Date());
        smallBox.setStatus(1);
        smallBox = smallBoxRepository.save(smallBox);
        smallBox.setSmallBoxNo(this.generateSmallBoxNo(smallBox));
        smallBox = smallBoxRepository.save(smallBox);
        //smallBox.setUser(user);
        for (PrePackageTransferDto dto : dtos) {
            if (dto == null)
                continue;
            if (dto.getValue()) {
                Optional<Depot> opt = depotRepository.findById(dto.getDepotId());

                if (opt.isPresent()) {
                    Depot depot = opt.get();
//                    Optional<DepotStatus> optStatus = depotStatusRepository.findById(2L);
//                    if (!optStatus.isPresent())
//                        continue;
//                    if (depot.getDepotStatus().getDepotStatusId() == optStatus.get().getDepotStatusId())
//                        continue;
//                    depot.setDepotStatus(optStatus.get());
                    if (depot.getSmallBoxId() != null)
                        continue;
                    depot = depotRepository.save(depot);

                    BoxDrugList drug = new BoxDrugList();
                    drug.setDepot(depot);
                    drug.setSmallBox(smallBox);
                    drug = boxDrugListRepository.save(drug);
                    depot.setSmallBoxId(smallBox.getSmallBoxId());
                    depot.setBoxDrugList(drug);
                    depot = depotRepository.save(depot);
                    //prePackageRepository.delete(p);
                }
                //}
            }

        }
        SmallBoxDto dto = mapper.map(smallBox, SmallBoxDto.class);
        return dto;
    }

    public String createSmallBoxListPdf(Long smallBoxId) throws Exception{
        try {
            //ARAMA BAŞLANGIÇ
            Optional<SmallBox> smallBox = smallBoxRepository.findById(smallBoxId);
            if(!smallBox.isPresent())
                throw new NotFoundException("Paket bulunamadı");

            List<SmallBoxDrugListGroupDto> smallBoxDrugList = new ArrayList<SmallBoxDrugListGroupDto>();
            List<Object[]> dtosList = depotRepository.getSmallBoxDrugs(smallBoxId);

            for (Object[] objArr : dtosList) {

                SmallBoxDrugListGroupDto dto = new SmallBoxDrugListGroupDto();
                dto.setDrugName((String) objArr[0]);
                dto.setLotNo((String) objArr[1]);
                dto.setCount((BigInteger) objArr[2]);
                dto.setExpirationDate((Date) objArr[3]);

                smallBoxDrugList.add(dto);
            }

            String pdfTitle="box-"+smallBox.get().getSmallBoxNo()+".pdf";

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
            com.itextpdf.text.Font catFont = new com.itextpdf.text.Font(bf, 16, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);

            //Tablonun Başlığı Girilir
            Paragraph tableHeader = new Paragraph("Kutu İlaç Listesi" + " " + smallBox.get().getSmallBoxNo(), catFont);


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
            for (SmallBoxDrugListGroupDto entity : smallBoxDrugList) {
                a++;
                addRows(table, String.valueOf(a));
                addRows(table, entity.getDrugName());
                addRows(table, entity.getLotNo());
                addRows(table, String.valueOf(entity.getCount()));
                addRows(table, sdf.format(entity.getExpirationDate()));
            }
            document.add(table);
            document.close();

            int index = pdfTitle.indexOf(".pdf");
            pdfTitle=pdfTitle.substring(0,index);
            //PDF SON
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

    private void addRows(PdfPTable table, String value) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 8, Font.NORMAL, BaseColor.BLACK);

        table.addCell(new Phrase(value, catFont));

    }

}
