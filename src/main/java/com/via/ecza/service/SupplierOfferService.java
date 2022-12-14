package com.via.ecza.service;


import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.repo.*;
import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

@Service
@Transactional
public class SupplierOfferService {

    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private SupplierOfferRepository supplierOfferRepository;
    @Autowired
    private SupplyCustomerService supplyCustomerService;
    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private CustomerSupplyOrderRepository customerSupplyOrderRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private DepotRepository depotRepository;
    @Autowired
    private SupplierSupervisorRepository supplierSupervisorRepository;
    @Autowired
    private CustomerOrderDrugsRepository customerOrderDrugsRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomerOrderStatusRepository customerOrderStatusRepository;
    @Autowired
    private SupplierOfferStatusRepository supplierOfferStatusRepository;
    @Autowired
    private PurchaseOrderDrugsRepository purchaseOrderDrugsRepository;
    @Autowired
    private PurchaseStatusRepository purchaseStatusRepository;
    @Autowired
    private ControlService controlService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private OtherCompanyRepository otherCompanyRepository;



    // SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    Date date = new Date(System.currentTimeMillis());
    // SimpleMailMessage msg = new SimpleMailMessage();

    @Autowired
    private ModelMapper mapper;

    public Boolean saveOffer(@Valid SupplierOfferSaveDto dto) throws NotFoundException, ParseException, MessagingException {


        Optional<DrugCard> optionalDrugCard = drugCardRepository.findById(dto.getDrugCard());//Var olma kontrolunu sonra yap (Optional ) ve if drumu
        Optional<Supplier> optionalSupplier = supplierRepository.findById(dto.getSupplier());
        Optional<SupplierSupervisor> optionalSupplierSupervisor = supplierSupervisorRepository.findById(dto.getSupervisorId());
        Optional<PurchaseOrderDrugs> optionalPurchaseOrderDrugs = purchaseOrderDrugsRepository.findById(dto.getPurchaseOrderDrugs());

        Optional<Discount> optionalDiscount=discountRepository.findByDrugCard(optionalDrugCard.get().getDrugCardId());
        if(optionalPurchaseOrderDrugs.get().getPurchaseStatus().getPurchaseStatusId()==5L){
            throw new NotFoundException("Bu ila?? ihracat onay?? beklendi??inden dolay?? sipari??e kapal??d??r!");
        }
        //System.out.println(optionalPurchaseOrderDrugs.get().getChargedQuantity().longValue());
        int sumOffers = 0;
        if (this.getSumOfOffers(optionalPurchaseOrderDrugs.get().getPurchaseOrderDrugsId(), optionalDrugCard.get().getDrugCardId()) != null) {
            sumOffers = this.getSumOfOffers(optionalPurchaseOrderDrugs.get().getPurchaseOrderDrugsId(), optionalDrugCard.get().getDrugCardId());
        }
        Long sumOfOrdersAndOffers = optionalPurchaseOrderDrugs.get().getChargedQuantity().longValue() + sumOffers;
        if (sumOfOrdersAndOffers >= optionalPurchaseOrderDrugs.get().getTotalQuantity()) {
            throw new NotFoundException(" Toplam teklif ve sipari??ler istenilen adetten fazla oldu??u i??in b??yle bir i??lem yapamazs??n??z");
        }

        try {
            SupplierOffer supplierOffer = mapper.map(dto, SupplierOffer.class);

            Optional<OtherCompany> optOtherCompany=otherCompanyRepository.findById(dto.getOtherCompanyId());
            if(!optOtherCompany.isPresent()){
                throw new NotFoundException("fatura edilecek firma bilgisi bulunamad?? !");
            }

            supplierOffer.setOtherCompanyId(dto.getOtherCompanyId());

            if (supplierOffer.getQuantity() == null) {
                throw new NotFoundException("Teklif Verilecek Adet 0 Olamaz");
            }
            if (supplierOffer.getQuantity() == 0) {
                throw new NotFoundException("Teklif Verilecek Adet 0 Olamaz");
            }

            supplierOffer.setDrugCard(optionalDrugCard.get());
            supplierOffer.setSupplier(optionalSupplier.get());
            supplierOffer.setSupervisorId(optionalSupplierSupervisor.get().getSupplierSupervisorId());
            supplierOffer.setPurchaseOrderDrugs(optionalPurchaseOrderDrugs.get());

           // supplierOffer.setVat(0.08f);//KDV KALDIRILDI
            supplierOffer.setCreatedAt(date);
            supplierOffer.setOfferedQuantity(dto.getQuantity());
            supplierOffer.setOfferedSurplus(dto.getSurplus());
            supplierOffer.setOfferedSupplierProfit(dto.getSupplierProfit());

            //avrup=averageuniprice - totp=totalprice - genp=generalprice - qua=quantity - instdiscount=id - dd=distrubutordiscount - sur=surplus - unit=unitprice - sp=supplierprofit - mf=surplus - total=totality


            Double totp, genp, avrup, id, dd, sur, unit, sp, qua, mf1, mf2, total,totalqua,pd = 0D;
            String mf;

            StringBuilder sb=new StringBuilder(optionalSupplier.get().getSupplierType().toString());
//            if(dto.getDistributorDiscount() == null)
//                throw new NotFoundException("Ticari ??skonto Bo?? Olamaz");
//            dd = dto.getDistributorDiscount().doubleValue();
//            if(dd==null){
//                dd=0D;
//            }
            dd=Double.valueOf(optionalDiscount.get().getGeneralDiscount());
            id=Double.valueOf(optionalDiscount.get().getInstutionDiscount());
            if(sb.toString().equals("PHARMACY")){unit=optionalDrugCard.get().getPrice().getDepotSalePriceExcludingVat(); sp = dto.getSupplierProfit().doubleValue();}
            else if(sb.toString().equals("WAREHOUSE")){unit=optionalDrugCard.get().getPrice().getDepotSalePriceExcludingVat();sp=0D;}
            else if(sb.toString().equals("PRODUCER")){
                dd=0D;
                unit=optionalDrugCard.get().getPrice().getSalePriceToDepotExcludingVat();
                sp=0D;
                pd=dto.getProducerDiscount().doubleValue();
            }
            else if(sb.toString().equals("STOCK")){unit=0D;sp=0D;pd=0D;}
            else {throw new NotFoundException("Hatal?? Tedarik??i tipi");}


            supplierOffer.setProducerDiscount(pd.floatValue());
            supplierOffer.setUnitPrice(unit.floatValue());
            qua = dto.getQuantity().doubleValue();
            //Float a = 1f;//Fiyat ??ekilince silinecek
           // unit = dto.getUnitPrice().doubleValue();//Unit price suppliertype a g??re al??nacak art??k
            // unit = a.doubleValue();
            totalqua=dto.getTotality().doubleValue();
//            dd = dto.getDistributorDiscount().doubleValue();
//            id = dto.getInstitutionDiscount().doubleValue();


            mf = dto.getSurplus();
            mf1 = Double.parseDouble(mf.substring(0, mf.indexOf("+")));
            mf2 = Double.parseDouble(mf.substring(mf.indexOf("+") + 1, mf.length()));

            //avrup=averageuniprice - totp=totalprice - genp=generalprice - qua=quantity - instdiscount=id - dd=distrubutordiscount - sur=surplus - unit=unitprice - sp=supplierprofit - mf=surplus - total=totality



//

            //indirimler d??????l??r ve tedarik??i kar?? konur 1 ila?? fiyat??na
            genp = qua * unit;
            avrup = unit - (unit * (id / 100));
            avrup -= (avrup * (dd / 100));
            avrup += (avrup * (sp / 100));
           // avrup += (avrup * 0.08);//avarage unit price///KDV ??IKARILDI


            if(sb.toString().equals("PRODUCER")){
                avrup = unit - (unit * (id / 100));
                avrup=avrup-(avrup* (pd/100));
            }



            supplierOffer.setGeneralPrice((genp).longValue());
            supplierOffer.setProducerDiscount(pd.floatValue());
            supplierOffer.setInstitutionDiscount(id.floatValue());
            //  System.out.println("total price " + (avrup * qua));


            //MAL FAZLASI Hesaplama
            sur = ((qua - (qua % mf1)) / mf1) * mf2;

            total = qua + sur;
            if(qua==total || (totalqua<=(mf1+mf2))){
                qua=totalqua;
                sur=0D;
                total=totalqua;
                avrup=avrup/(mf1+mf2);
                avrup=avrup*mf1;
            }
            else if((totalqua!=(qua + sur))){//Daha sonra ifleri birle??tir
                throw new NotFoundException(" Mal fazlas?? oran?? hatal??");
            }






            supplierOffer.setSurplusQuantity(sur.longValue());
            supplierOffer.setOfferedSurplusQuantity(sur.longValue());
            //  System.out.println("sur " + sur);



            dto.setTotality(total.longValue());

            Double g = ((avrup * qua) / total);
            supplierOffer.setAverageUnitPrice(g.floatValue());
            supplierOffer.setOfferedAveragePrice(g.floatValue());

            supplierOffer.setOfferedTotality(total.longValue());
            supplierOffer.setOfferedTotalPrice(avrup * qua);
            supplierOffer.setTotality(total.longValue());
            supplierOffer.setTotalPrice(avrup * qua);

            supplierOffer.setSupplierOfferStatus(supplierOfferStatusRepository.findById(10L).get());//Eczane Onay?? Bekleniyor
            supplierOffer = supplierOfferRepository.save(supplierOffer);

            if(g>optionalPurchaseOrderDrugs.get().getExporterUnitPrice()){

                supplierOffer.setSupplierOfferStatus(supplierOfferStatusRepository.findById(5L).get());//M??d??r Onay?? Bekleniyor
                supplierOffer = supplierOfferRepository.save(supplierOffer);
                supplierOffer.getPurchaseOrderDrugs().setPurchaseStatus(purchaseStatusRepository.findById(7L).get());
            }

            emailService.sendMailWithHtml(optionalSupplier.get().getSupplierEmail(),
                    optionalSupplier.get().getSupplierName() + " Firmas?? , " + "<br/><br/>" + optionalDrugCard.get().getDrugName() + " ilac?? i??in " + (int) Math.round(total) + " adet " + " ila?? sipari??i var.", " Ekip Pharma Sipari??i");



//            if(sb.toString().equals("PRODUCER")){
//               this.offerAccept(supplierOffer.getSupplierOfferId());
//
//            }


            //////Email g??nderimi kapand??
//            try {
//
//
//                emailService.sendMailWithHtml(optionalSupplier.get().getSupplierEmail(),
//                        optionalSupplier.get().getSupplierName() + " Firmas?? , " + "<br/><br/>" + optionalDrugCard.get().getDrugName() + " ilac?? i??in " + (int) Math.round(total) + " adet " + " ila?? sipari??i var.", " Ekip Pharma Sipari??i");
//
////                SimpleMailMessage msg = new SimpleMailMessage();
////                msg.setTo("viaonur@gmail.com");
////                msg.setSubject("Pharma Tak??m?? ????in ??la?? Sipari??i");
////                msg.setText("Merhaba \n \n \n Pharma Tak??m??ndan Sipari?? Var \n \n \n Kolay Gelsin. ");
////                javaMailSender.send(msg);
//            } catch (Exception e) {
//                throw new NotFoundException("Teklif g??nderilemiyor. ??nternet Ba??lant??n??z?? Kontrol Edin veya Admin ile ??leti??ime Ge??in");
//            }


            //msg.setFrom("sptestm@gmail.com");
//            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//            String path = classLoader.getResource("src/main/resources/frontend/src/assets/pharma.png").getPath();
//            System.out.println(path);r


//            try {
//                helper.setTo("viaonur@gmail.com");
//                helper.setSubject("Pharma Tak??m?? ????in ??la?? Sipari??i");
//                helper.setText("my text <img src='cid:myLogo'>", true);
//                helper.addInline("pharma", new ClassPathResource("src/main/resources/frontend/src/assets/pharma.png"));
//            }catch (Exception e){
//                System.out.println("Hata  ::::"+e);
//            }


//
//            javaMailSender.send(msg);



     /*       msg.setTo(mail);
            msg.setSubject("Pharma Tak??m?? ????in ??la?? Sipari??i");
            msg.setText("Merhabalar a????a????daki linkten sipari??e ula??abilirsiniz \n link "+link);

      */

            //local/offers-of-supply/{supplierId}
            // msg.setTo("viaonur@gmail.com", "oonurozsoy@gmail.com");


        } catch (Exception e) {

            throw e;
        }


        return true;
    }


    public SupplierOfferDto findById(Long supplierOfferId) throws NotFoundException {
        Optional<SupplierOffer> optionalSupplierOffer = supplierOfferRepository.findById(supplierOfferId);
        if (!optionalSupplierOffer.isPresent()) {
            throw new NotFoundException("B??yle Bir Teklif yok");
        }
        SupplierOfferDto supplierOfferDto = mapper.map(optionalSupplierOffer.get(), SupplierOfferDto.class);

        // supplierOfferDto.getSupplier().getSupplierSupervisors().removeIf(c->(c.getStatus()==0));//b??yle bir??eye gerek yok ????nk?? geriye y??nelik veri tutmam??z laz??m unutma
        return supplierOfferDto;
    }


    public List<SupplyOffersDto> getByPurchaseOrderDrugs(Long purchaseOrderDrugId) throws NotFoundException {
        List<SupplierOffer> supplierOffersList = supplierOfferRepository.getByPurchaseOrderDrugs(purchaseOrderDrugId);
        SupplyOffersDto[] supplierOffersDtoArray = mapper.map(supplierOffersList, SupplyOffersDto[].class);
        List<SupplyOffersDto> supplierOffersDtoList = Arrays.asList(supplierOffersDtoArray);

        return supplierOffersDtoList;

    }

    public Integer getSumOfOffers(Long purchaseOrderDrugId, Long drugCard) throws NotFoundException {
        Integer sumOfOffers = supplierOfferRepository.getSumOfOffers(purchaseOrderDrugId, drugCard);

        return sumOfOffers;

    }

    public Boolean offerReject(Long supplierOfferId) throws NotFoundException {
        Optional<SupplierOffer> supplierOffer = supplierOfferRepository.findById(supplierOfferId);
        if (!supplierOffer.isPresent()) {
            throw new NotFoundException("B??yle Bir Teklif yok");
        }
        System.out.println(" Bu ne "+supplierOffer.get().getSupplierOfferStatus().getSupplierOfferStatusId() );
        if (supplierOffer.get().getSupplierOfferStatus().getSupplierOfferStatusId() != 20L) {
            throw new NotFoundException("Bu Sipari?? ??zerinde daha ??nce de??i??iklik yap??lm????");
        }


        Optional<PurchaseOrderDrugs> optionalPurchaseOrderDrugs = purchaseOrderDrugsRepository.findById(supplierOffer.get().getPurchaseOrderDrugs().getPurchaseOrderDrugsId());
        if (!optionalPurchaseOrderDrugs.isPresent()) {
            throw new NotFoundException("B??yle Bir Sipari?? Yok");
        }
        //gerek yok// if(supplierOffer.get().getSupplierOfferStatus().getSupplierOfferStatusId()!=2){throw new NotFoundException(" Bu teklif iptal edilemez.");}//Eczane de??i??iklik yapma durumu status 2
        supplierOffer.get().setSupplierOfferStatus(supplierOfferStatusRepository.findById(40L).get());//Eczane Teklifi ??ptal
        supplierOfferRepository.save(supplierOffer.get());
        return true;
    }

    public Boolean offerCancel(Long supplierOfferId) throws NotFoundException {
        Optional<SupplierOffer> supplierOffer = supplierOfferRepository.findById(supplierOfferId);
        if (!supplierOffer.isPresent()) {
            throw new NotFoundException("B??yle Bir Teklif yok");
        }
        if (supplierOffer.get().getSupplierOfferStatus().getSupplierOfferStatusId() != 10L) {
            throw new NotFoundException("Bu Sipari?? ??zerinde daha ??nce de??i??iklik yap??lm????");
        }

        Optional<PurchaseOrderDrugs> optionalPurchaseOrderDrugs = purchaseOrderDrugsRepository.findById(supplierOffer.get().getPurchaseOrderDrugs().getPurchaseOrderDrugsId());
        if (!optionalPurchaseOrderDrugs.isPresent()) {
            throw new NotFoundException("B??yle Bir Sipari?? Yok");
        }
        //gerek yok// if(supplierOffer.get().getSupplierOfferStatus().getSupplierOfferStatusId()!=2){throw new NotFoundException(" Bu teklif iptal edilemez.");}//Eczane de??i??iklik yapma durumu status 2
        supplierOffer.get().setSupplierOfferStatus(supplierOfferStatusRepository.findById(45L).get());//Eczane Teklifi geri al
        supplierOfferRepository.save(supplierOffer.get());
        return true;
    }


    public Boolean offerAccept(Long supplierOfferId) throws NotFoundException {
        //   System.out.println("Sat??n almac?? Eczane teklifi prefence ??al????mal??");
        Optional<SupplierOffer> supplierOffer = supplierOfferRepository.findById(supplierOfferId);
        if (!supplierOffer.isPresent()) {
            throw new NotFoundException("B??yle Bir Teklif yok");
        }

        Optional<PurchaseOrderDrugs> optionalPurchaseOrderDrugs = purchaseOrderDrugsRepository.findById(supplierOffer.get().getPurchaseOrderDrugs().getPurchaseOrderDrugsId());
        if (!optionalPurchaseOrderDrugs.isPresent()) {
            throw new NotFoundException("B??yle Bir Sipari?? Yok");
        }
        //   System.out.println("Sat??n almac?? Eczane teklifi onay ??al????mal??");
        supplierOffer.get().setSupplierProfit(supplierOffer.get().getOfferedSupplierProfit());
        supplierOffer.get().setSurplus(supplierOffer.get().getOfferedSurplus());
        supplierOffer.get().setSurplusQuantity(supplierOffer.get().getOfferedSurplusQuantity());
        supplierOffer.get().setQuantity(supplierOffer.get().getOfferedQuantity());
        supplierOffer.get().setTotalPrice(supplierOffer.get().getOfferedTotalPrice());
        supplierOffer.get().setTotality(supplierOffer.get().getOfferedTotality());
        Double avrup = supplierOffer.get().getOfferedTotalPrice() / supplierOffer.get().getOfferedTotality();
        supplierOffer.get().setAverageUnitPrice(avrup.floatValue());
        supplierOfferRepository.save(supplierOffer.get());
        SupplierOfferSaveDto supplierOfferSaveDto = new SupplierOfferSaveDto();
        supplierOfferSaveDto.setProducerDiscount(supplierOffer.get().getProducerDiscount().floatValue());
        supplierOfferSaveDto.setOfferedSupplierProfit(supplierOffer.get().getOfferedSupplierProfit());
        supplierOfferSaveDto.setOfferedSurplus(supplierOffer.get().getOfferedSurplus());
        supplierOfferSaveDto.setOfferedSurplusQuantity(supplierOffer.get().getOfferedSurplusQuantity());
        supplierOfferSaveDto.setOfferedQuantity(supplierOffer.get().getOfferedQuantity());
        supplierOfferSaveDto.setOfferedTotalPrice(supplierOffer.get().getOfferedTotalPrice());
        supplierOfferSaveDto.setOfferedTotality(supplierOffer.get().getOfferedTotality());
        supplierOfferSaveDto.setAverageUnitPrice(avrup.floatValue());
        supplierOfferSaveDto.setOfferedTotalPrice(supplierOffer.get().getOfferedTotalPrice());//ayr??nt??

        try {

            this.update(supplierOfferId, supplierOfferSaveDto);
        } catch (Exception e) {
            throw new NotFoundException(" Hata var :" + e);
        }
        return true;
    }


    public Boolean update(Long supplierOfferId, @Valid SupplierOfferSaveDto dto) throws Exception {
        Optional<SupplierOffer> supplierOffer = supplierOfferRepository.findById(supplierOfferId);
        if (!supplierOffer.isPresent()) {
            throw new NotFoundException("B??yle Bir Teklif yok");
        }
     //   System.out.println(" Bu ne "+supplierOffer.get().getSupplierOfferStatus().getSupplierOfferStatusId());
        if (supplierOffer.get().getSupplierOfferStatus().getSupplierOfferStatusId() > 21L) {
            throw new NotFoundException("Bu i??lem daha ??nce de??i??tirilmi??");
        }

        Optional<PurchaseOrderDrugs> optionalPurchaseOrderDrugs = purchaseOrderDrugsRepository.findById(supplierOffer.get().getPurchaseOrderDrugs().getPurchaseOrderDrugsId());
        if (!optionalPurchaseOrderDrugs.isPresent()) {
            throw new NotFoundException("B??yle Bir Sipari?? Yok");
        }

        Long stocks = 0L;


            Double price = supplierOffer.get().getOfferedTotalPrice() - dto.getOfferedTotalPrice();
            if (supplierOffer.get().getQuantity().equals(dto.getOfferedQuantity()) && supplierOffer.get().getSurplus().equals(dto.getOfferedSurplus()) && supplierOffer.get().getSupplierProfit().equals(dto.getOfferedSupplierProfit()) && supplierOffer.get().getOfferedTotality().equals(dto.getOfferedTotality()) && supplierOffer.get().getOfferedSurplusQuantity().equals(dto.getOfferedSurplusQuantity()) && (price < 0.1 || price > -0.1)) {

                Double avpr = supplierOffer.get().getOfferedTotalPrice() / supplierOffer.get().getOfferedTotality();
                supplierOffer.get().setOfferedAveragePrice(avpr.floatValue());
                supplierOffer.get().setOfferedTotalPrice(dto.getOfferedTotalPrice());
                if (optionalPurchaseOrderDrugs.get().getIncompleteQuantity() < supplierOffer.get().getTotality()) {
                    stocks = supplierOffer.get().getTotality() - optionalPurchaseOrderDrugs.get().getIncompleteQuantity();
                    if (optionalPurchaseOrderDrugs.get().getIncompleteQuantity() < supplierOffer.get().getQuantity()) {

                    }

                }


                //Mapper ??al????mad??      //CustomerSupplyOrderDto customerSupplyOrderDto=mapper.map(supplierOffer.get(),CustomerSupplyOrderDto.class);
                CustomerSupplyOrderDto customerSupplyOrderDto = new CustomerSupplyOrderDto();
                customerSupplyOrderDto.setPurchaseOrderDrugs(supplierOffer.get().getPurchaseOrderDrugs().getPurchaseOrderDrugsId());
                customerSupplyOrderDto.setQuantity(supplierOffer.get().getQuantity());//buraya birdaha bak cso ya totality olu??turmak gerekebilir
                customerSupplyOrderDto.setUnitPrice(supplierOffer.get().getUnitPrice());
                customerSupplyOrderDto.setTotalPrice(supplierOffer.get().getTotalPrice());
                customerSupplyOrderDto.setInstitutionDiscount(supplierOffer.get().getInstitutionDiscount());
                customerSupplyOrderDto.setDistributorDiscount(supplierOffer.get().getDistributorDiscount());
                customerSupplyOrderDto.setGeneralPrice(supplierOffer.get().getGeneralPrice());
                customerSupplyOrderDto.setSupplierId(supplierOffer.get().getSupplier().getSupplierId());
                customerSupplyOrderDto.setDrugCardId(supplierOffer.get().getDrugCard().getDrugCardId());
                customerSupplyOrderDto.setSupervisorId(supplierOffer.get().getSupervisorId());
                customerSupplyOrderDto.setAverageUnitPrice(supplierOffer.get().getAverageUnitPrice());
                customerSupplyOrderDto.setVat(0.0F);
                customerSupplyOrderDto.setProducerDiscount(supplierOffer.get().getProducerDiscount());
                customerSupplyOrderDto.setUnitPrice(supplierOffer.get().getUnitPrice());
                customerSupplyOrderDto.setSupplierProfit(supplierOffer.get().getSupplierProfit());
                customerSupplyOrderDto.setSurplus(supplierOffer.get().getSurplus());
                customerSupplyOrderDto.setTotality(supplierOffer.get().getTotality());
                customerSupplyOrderDto.setCreatedAt(date);
                customerSupplyOrderDto.setStocks(stocks);
                customerSupplyOrderDto.setSurplusQuantity(supplierOffer.get().getSurplusQuantity());
                customerSupplyOrderDto.setTotalQuantity(supplierOffer.get().getTotality() - stocks);
                customerSupplyOrderDto.setOtherCompanyId(dto.getOtherCompanyId());

                supplierOffer.get().setSupplierOfferStatus(supplierOfferStatusRepository.findById(50L).get());//Kabul Edildi

                supplyCustomerService.save(customerSupplyOrderDto);


            } else if (dto.getOfferedQuantity() == 0) {
                supplierOffer.get().setSupplierOfferStatus(supplierOfferStatusRepository.findById(30L).get());//Eczane verdi??iniz teklifi reddetti!
                supplierOfferRepository.save(supplierOffer.get());
                return true;
            } else {
                supplierOffer.get().setOfferedSupplierProfit(dto.getOfferedSupplierProfit());
                supplierOffer.get().setOfferedSurplus(dto.getOfferedSurplus());
                supplierOffer.get().setOfferedSurplusQuantity(dto.getOfferedSurplusQuantity());
                supplierOffer.get().setOfferedQuantity(dto.getOfferedQuantity());
                supplierOffer.get().setOfferedTotalPrice(dto.getOfferedTotalPrice());
                supplierOffer.get().setOfferedTotality(dto.getOfferedTotality());
                Double avrup = dto.getOfferedTotalPrice() / dto.getOfferedTotality();
                supplierOffer.get().setOfferedAveragePrice(avrup.floatValue());
                supplierOffer.get().setCreatedAt(date);
                supplierOffer.get().setSupplierOfferStatus(supplierOfferStatusRepository.findById(20L).get());//Teklif de??i??tirildi
                return true;
            }


        return true;


    }

    public Boolean managerPermission(String authHeader,Boolean permission,Long supplierOfferId) throws NotFoundException {
        Optional<SupplierOffer> optionalSupplierOffer=supplierOfferRepository.findById(supplierOfferId);
        if(!optionalSupplierOffer.isPresent()){throw new NotFoundException("B??yle bir teklif yok!");}
        if(optionalSupplierOffer.get().getSupplierOfferStatus().getSupplierOfferStatusId()!=5L){
            throw new NotFoundException("Bu teklif i??in herhangi bir izne gerek yoktur. Yanl???? i??lem!"); }
        String username = controlService.getUsernameFromToken(authHeader);
        Optional<User> optUser = userRepository.findByUsername(username);
        System.out.println(optUser.get().getRole().getValue()+" asdad");
        if(!optUser.isPresent()){throw new NotFoundException("B??yle bir kullan??c?? yok!");}
        else if(!optUser.get().getRole().getValue().toString().equals("ADMIN") && !optUser.get().getRole().getValue().toString().equals("MANAGER"))
        {throw new NotFoundException("B??yle bir i??leme bu kullan??c??n??n yetkisi yok! L??tfen Admin ile ileti??ime ge??in!");}
        else if(permission){
            optionalSupplierOffer.get().setSupplierOfferStatus(supplierOfferStatusRepository.findById(10L).get());
            supplierOfferRepository.save(optionalSupplierOffer.get());
            supplyCustomerService.setPurchases(optionalSupplierOffer.get().getPurchaseOrderDrugs());
            optionalSupplierOffer.get().setLog_so( optionalSupplierOffer.get().getLog_so()+" Bu teklif birim fiyat?? "+username+" taraf??ndan kabul edildi.");
            this.setPurchases(optionalSupplierOffer.get().getPurchaseOrderDrugs());
        }else {
            optionalSupplierOffer.get().setSupplierOfferStatus(supplierOfferStatusRepository.findById(7L).get());
            supplierOfferRepository.save(optionalSupplierOffer.get());
            supplyCustomerService.setPurchases(optionalSupplierOffer.get().getPurchaseOrderDrugs());
            optionalSupplierOffer.get().setLog_so( optionalSupplierOffer.get().getLog_so()+" Bu teklif birim fiyat?? "+username+" taraf??ndan reddedildi.");
            this.setPurchases(optionalSupplierOffer.get().getPurchaseOrderDrugs());
        }

        return true;
    }

    public Boolean setPurchases(PurchaseOrderDrugs purchaseOrderDrugs) {
         if (purchaseOrderDrugs.getChargedQuantity().equals(purchaseOrderDrugs.getTotalQuantity())) {
            purchaseOrderDrugs.setPurchaseStatus(purchaseStatusRepository.findById(40L).get());
        } else if (purchaseOrderDrugs.getChargedQuantity() < purchaseOrderDrugs.getTotalQuantity()) {
            purchaseOrderDrugs.setPurchaseStatus(purchaseStatusRepository.findById(20L).get());
        } else if (purchaseOrderDrugs.getChargedQuantity() == 0) {
            purchaseOrderDrugs.setPurchaseStatus(purchaseStatusRepository.findById(10L).get());
        } else {
            return false;
        }

        return true;
    }

//    //isim  de??i??tir //Eczane i??in teklifler listesi
//    public List<SupplyDrugOffersDto> getSupplierByUser(String authHeader) throws NotFoundException {
//        // Optional<User> optionalUser=userRepository.findByUsername(userName);
//        //Optional<Supplier> optionalSupplier=supplierRepository.getSupplierByUser(userId);
//        User user = this.getUserFromToken(authHeader);
//        Optional<Supplier> optionalSupplier = supplierRepository.getSupplierByUser(user.getUserId().longValue());
//
//        List<SupplierOffer> supplierOfferList = supplierOfferRepository.getBySupplier(optionalSupplier.get().getSupplierId());
//        //System.out.println(" supplier Id: " + optionalSupplier.get().getSupplierId());
//        SupplyDrugOffersDto[] supplierOfferDtoList = mapper.map(supplierOfferList, SupplyDrugOffersDto[].class);
//        List<SupplyDrugOffersDto> dtoList = Arrays.asList(supplierOfferDtoList);
//        return dtoList;
//    }


    private User getUserFromToken(String authHeader) throws NotFoundException {
        String username = controlService.getUsernameFromToken(authHeader);
        Optional<User> optUser = userRepository.findByUsername(username);
        if (!optUser.isPresent()) {
            throw new NotFoundException("Not found User");
        }
        return optUser.get();
    }

    public boolean isConecctedToInternet() {

        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(" ??nternet Hatas??1");
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println(" ??nternet Hatas??2");
        }
        return false;
    }
}
