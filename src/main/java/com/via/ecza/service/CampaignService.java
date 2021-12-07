package com.via.ecza.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.repo.CampaignRepository;
import com.via.ecza.repo.CustomerOrderDrugsRepository;
import com.via.ecza.repo.DrugCardRepository;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional
public class CampaignService {

    @Autowired
    private CampaignRepository campaignRepository;
    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CustomerOrderDrugsRepository customerOrderDrugsRepository;

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public Boolean save(@Valid CampaignSaveDto dto) throws Exception {
        try {

            Optional<DrugCard> optionalDrugCard = null;

            if (dto.getDrugCardId() != null) {
                optionalDrugCard = drugCardRepository.findByDrugCardId(dto.getDrugCardId());
            } else {
                return false;
            }

            DrugCard drug = optionalDrugCard.get();

            Campaign campaign = mapper.map(dto, Campaign.class);

//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            Date date =dto.getCampaignStartDate();
//
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(date);
//            calendar.add(Calendar.DATE, -1);
//            Date yesterday = calendar.getTime();
//           campaign.setCampaignStartDate(yesterday);

            campaign.setCampaignStartDate(dto.getCampaignStartDate());
            campaign.setCampaignEndDate(dto.getCampaignEndDate());

            campaign.setDrugCard(optionalDrugCard.get());
            if(dto.getMf1()< 2)
                campaign.setMf1(1);
            campaign.setMf1(dto.getMf1());
            if( dto.getMf2() < 1)
                campaign.setMf2(0);
            campaign.setMf2(dto.getMf2());
            if(dto.getProfit()< 1)
                campaign.setProfit(0);
            campaign.setProfit(dto.getProfit());
            campaign.setCurrencyFee(dto.getCurrencyFee());
            campaign.setCampaignUnitPriceExcludingVat(dto.getCampaignUnitPriceExcludingVat());
            campaign.setVat(drug.getDrugVat());
            campaign.setStatus(1);
            campaign.setIsDeleted(0);
            campaign.setCampaignUnitPriceCurrency(dto.getCampaignUnitPriceCurrency());
            campaign.setInstutionDiscount((double) drug.getDiscount().getInstutionDiscount());
            campaign.setCurrencyType(dto.getCurrencyType());
            campaign.setDepotSalePriceExcludingVat(dto.getDepotSalePriceExcludingVat());
//            campaign.setCampaignUnitCost(dto.getCampaignUnitCost());
//            campaign.setCampaignUnitPrice(dto.getCampaignUnitPrice());
//            campaign.setCampaignStartDate(dto.getCampaignStartDate());
//            campaign.setCampaignEndDate(dto.getCampaignEndDate());
            campaign.setCreatedDate(new Date());
            campaign = campaignRepository.save(campaign);

            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    public Page<CampaignDto> searchCampaigns(CampaignSearchDto dto, Pageable page) {

        StringBuilder createSqlQuery = new StringBuilder("select c.* from campaign c " +
                "join drug_card dc on dc.drug_card_id = c.drug_card_id where c.campaign_id >0 and c.is_deleted = 0 ");
        createSqlQuery.append (" and c.campaign_end_date >= to_date('" + sdf.format(new Date()) + "'," + "'dd.MM.yyyy') ");
        if (dto.getDrugCardId() != null) createSqlQuery.append(" and dc.drug_card_id =  " + dto.getDrugCardId() + " ");
        if (dto.getCampaignUnitPriceCurrency() != null) createSqlQuery.append(" and c.campaign_unit_price_currency =  " + dto.getCampaignUnitPriceCurrency() + " ");

        createSqlQuery.append(" order by c.campaign_end_date ASC");

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Campaign.class).getResultList();
        CampaignDto[] dtos = mapper.map(list, CampaignDto[].class);
        List<CampaignDto> dtosList = Arrays.asList(dtos);

        int start = Math.min((int) page.getOffset(), dtosList.size());
        int end = Math.min((start + page.getPageSize()), dtosList.size());

        Page<CampaignDto> pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());

        return pageList;
    }

    public List<CampaignStockDto> searchStock(CampaignStockSearchDto dto) {


            StringBuilder createSqlQuery = new StringBuilder("select count(d.drug_card_id), d.drug_card_id, dc.drug_name, d.drug_barcode from depot d " +
                    "join drug_card dc on dc.drug_card_id = d.drug_card_id " +
                    "where d.depot_status_id = 10 ");
            if (dto.getDrugCardId() != null) createSqlQuery.append("and dc.drug_card_id =  " + dto.getDrugCardId() + " ");
            createSqlQuery.append(" group by d.drug_card_id, dc.drug_name, d.drug_barcode ");
            if (dto.getMinCount() != null) createSqlQuery.append(" having count(d.drug_card_id) >= " + dto.getMinCount());
            if (dto.getMaxCount() != null) createSqlQuery.append(" and count(d.drug_card_id) <= " + dto.getMaxCount());

        List<Object[]> list = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();

        List<CampaignStockDto> dtosList = new ArrayList<>();
        for(Object[] data :list){
            CampaignStockDto dtos = new CampaignStockDto();
            dtos.setCount((BigInteger) data[0]);
            dtos.setDrugCardId((BigInteger)data[1]);
            dtos.setDrugName((String)data[2]);
            dtos.setDrugBarcode((String)data[3]);

            dtosList.add(dtos);

        }
        return dtosList;
    }
    public Page<CampaignDto> searchEndCampaigns(CampaignSearchDto dto, Pageable page) {

        StringBuilder createSqlQuery = new StringBuilder("select c.* from campaign c " +
                "join drug_card dc on dc.drug_card_id = c.drug_card_id where c.campaign_id >0 and c.is_deleted = 0 ");
        createSqlQuery.append (" and c.campaign_end_date < to_date('" + sdf.format(new Date()) + "'," + "'dd.MM.yyyy') ");
        if (dto.getDrugCardId() != null) createSqlQuery.append("and dc.drug_card_id =  " + dto.getDrugCardId() + " ");
        if (dto.getCampaignUnitPriceCurrency() != null) createSqlQuery.append(" and c.campaign_unit_price_currency =  " + dto.getCampaignUnitPriceCurrency() + " ");

        createSqlQuery.append(" order by c.campaign_end_date ASC");

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Campaign.class).getResultList();
        CampaignDto[] dtos = mapper.map(list, CampaignDto[].class);
        List<CampaignDto> dtosList = Arrays.asList(dtos);

        int start = Math.min((int) page.getOffset(), dtosList.size());
        int end = Math.min((start + page.getPageSize()), dtosList.size());

        Page<CampaignDto> pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());

        return pageList;
    }
    public List<CampaignDto> campaignDetail(Long campaignId) {

        StringBuilder createSqlQuery = new StringBuilder("select c.* from campaign c " +
                "join drug_card dc on dc.drug_card_id = c.drug_card_id " +
                "where c.campaign_id = " + campaignId);

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Campaign.class).getResultList();
        CampaignDto[] dtos = mapper.map(list, CampaignDto[].class);
        return Arrays.asList(dtos);
    }

    public Boolean update(Long campaignId, CampaignDto dto)  throws NotFoundException {
        if(campaignId == null)
            return false;

        Optional<Campaign> optionalCampaign = campaignRepository.findById(campaignId);
        if(!optionalCampaign.isPresent()) {
            log.info("Kampanyalı İlaç Kaydı Bulunamadı..");
            return false;
        }

        Optional<DrugCard> optionalDrugCard = null;

        if (campaignId != null) {
            optionalDrugCard = drugCardRepository.getByCampaignId(campaignId);
        } else {
            return false;
        }

        DrugCard drug = optionalDrugCard.get();

        Campaign campaign = optionalCampaign.get();
        campaign.setCampaignEndDate(dto.getCampaignEndDate());
        campaign.setCampaignStartDate(dto.getCampaignStartDate());
        campaign.setCampaignUnitPrice(dto.getCampaignUnitPrice());
        campaign.setMf1(dto.getMf1());
        campaign.setMf2(dto.getMf2());
        campaign.setProfit(dto.getProfit());
        campaign.setCurrencyFee(dto.getCurrencyFee());
        campaign.setCampaignUnitPriceExcludingVat(dto.getCampaignUnitPriceExcludingVat());
        campaign.setVat(drug.getDrugVat());
        campaign.setCampaignUnitPriceCurrency(dto.getCampaignUnitPriceCurrency());
        if(dto.getInstutionDiscount() > 0)
            campaign.setInstutionDiscount((double) drug.getDiscount().getInstutionDiscount());
        if(dto.getInstutionDiscount() == 0 )
            campaign.setInstutionDiscount(dto.getInstutionDiscount());
        campaign.setCurrencyType(dto.getCurrencyType());

        List<CustomerOrderDrugs> customerOrderDrugsList = customerOrderDrugsRepository.findCustomerOrderDrugsWithCampaign(campaignId);
        if(customerOrderDrugsList.size()>0){
            for (CustomerOrderDrugs customerOrderDrugs:customerOrderDrugsList) {
                customerOrderDrugs.setCampaign(campaign);
                customerOrderDrugs.setCurrencyFee(campaign.getCurrencyFee());
                customerOrderDrugs.setUnitPrice(campaign.getCampaignUnitPrice());
                customerOrderDrugs.setProfit(campaign.getProfit());
                customerOrderDrugs.setSurplusOfGoods1(campaign.getMf1());
                customerOrderDrugs.setSurplusOfGoods2(campaign.getMf2());
                customerOrderDrugs.setIsCampaignedDrug(1);
                customerOrderDrugs = customerOrderDrugsRepository.save(customerOrderDrugs);
            }
        }
        campaign=campaignRepository.save(campaign);

        return true;
    }

    public CampaignDto findCampaignDrugById(Long campaignId) throws Exception{

        Optional<Campaign> optionalCampaign = campaignRepository.findById(campaignId);
        if(!optionalCampaign.isPresent())
            throw new NotFoundException("Kampanya Bulunamadı");

        CampaignDto dto= mapper.map(optionalCampaign.get(), CampaignDto.class);

        return dto;
    }

    public String createCampaignPdf() throws Exception{
        try {
            //ARAMA BAŞLANGIÇ
            StringBuilder createSqlQuery = new StringBuilder("select c.* from campaign c " +
                    "join drug_card dc on dc.drug_card_id = c.drug_card_id where c.status =1 ");
            createSqlQuery.append (" and c.campaign_end_date >= to_date('" + sdf.format(new Date()) + "'," + "'dd.MM.yyyy') ");
            createSqlQuery.append(" order by c.campaign_end_date ASC");

            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Campaign.class).getResultList();

            Campaign[] campaigns = mapper.map(list,Campaign[].class );
            List<Campaign> campaignList = Arrays.asList(campaigns);
            //ARAMA SON

            String pdfTitle="campaign.pdf";

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
            Paragraph tableHeader = new Paragraph("Drugs With Campaign", catFont);

            tableHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(tableHeader);

            document.add(new Paragraph("\n"));

            //Tablonun Sütun Sayısı Girilir
            PdfPTable table = new PdfPTable(4);
            table.setWidths(new int[]{1, 6, 3, 3});

            table.setWidthPercentage(100);
            addTableHeader(table);

            //Hücrelere Veriler Girilir
            int a = 0;
            for (Campaign campaign : campaignList) {
                a++;
                addRows(table, String.valueOf(a));
                addRows(table, campaign.getDrugCard().getDrugName());
                addRows(table, String.valueOf(campaign.getDrugCard().getDrugCode()));
                addRows(table, getCurrencyType(campaign.getCurrencyType())+" "+String.valueOf(campaign.getCampaignUnitPriceCurrency()));

            }
            document.add(table);
            document.close();
            //PDF SON
            return pdfTitle;

        }catch (Exception e){
            throw new Exception("Kampanyalı İlaç Listesi Pdf Oluşturma İşleminde Hata Oluştu",e);
        }
    }

    private void addTableHeader(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);

        //Tablo Sütün Başlıkları Girilir
        Stream.of("No", "Drug Name", "Drug Barcode", "Price With Campaign")
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
    private String getCurrencyType(CurrencyType currencyType){
        if(currencyType == CurrencyType.USD)        return "$";
        if(currencyType == CurrencyType.EURO)       return "€";
        if(currencyType== CurrencyType.STERLIN)     return "£";
        else                                        return "₺";
    }


    public Boolean deleteCampaignedDrug(Long campaignId) throws Exception {

        Optional<Campaign> optionalCampaign = campaignRepository.findById(campaignId);
        if(!optionalCampaign.isPresent())
            return false;

        Campaign campaign = optionalCampaign.get();
//
//        List<CustomerOrderDrugs> codList = customerOrderDrugsRepository.drugForDeleteWithCampaignNotPurchase(campaignId);
//        if(codList.size() == 0){
//            return true;
//        }
        List<CustomerOrderDrugs> codList = customerOrderDrugsRepository.drugForDeleteWithCampaign(campaignId);
//        if(codList2.size() >0){
//            campaign.setIsDeleted(1);
//            campaign = campaignRepository.save(campaign);
//            return true;
//        }
        if(codList.size() == 0){
            campaign.setDrugCard(null);
            campaignRepository.delete (optionalCampaign.get());
        }
        for (CustomerOrderDrugs cod:codList) {
            if(cod.getPurchaseOrderDrugsId() != null ){
                campaign.setIsDeleted(1);
                campaign = campaignRepository.save(campaign);
            }else{
                cod.setDrugCard(null);
                cod.setCampaign(null);
                cod = customerOrderDrugsRepository.save(cod);
                customerOrderDrugsRepository.delete(cod);
            }
        }


        return true;
    }
}
