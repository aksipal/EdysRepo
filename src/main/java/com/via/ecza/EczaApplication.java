package com.via.ecza;

import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.CheckingCardType;
import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

@SpringBootApplication
public class EczaApplication  {

//	@Autowired
//	private CustomerRepository customerRepository;
//	@Autowired
//	private CountryRepository countryRepository;
//	@Autowired
//	private CheckingCardRepository checkingCardRepository;

//	ArrayList<DrugCard> drugList=new ArrayList<DrugCard>();

	@Autowired
	CustomerOrderStatusRepository customerOrderStatusRepository;

	@Autowired
	private JavaMailSender javaMailSender;

	SimpleMailMessage msg = new SimpleMailMessage();

	
	public static void main(String[] args) {
		SpringApplication.run(EczaApplication.class, args);
	}

	
	@Bean
	CommandLineRunner createInitialUsers(UserRepository usereRepository,
										 PasswordEncoder passwordEncoder) {
		return (args) -> {
			try {

//				// Kullanıcıların yüklenmesi --------------------------------------------------------------
//				User user1 = new User();
//				user1.setName("admin");
//				user1.setSurname("adminx");
//				user1.setUsername("admin");
//				user1.setIsLoggedIn(0);
//				user1.setStatus(1);
//				//user1.setRealPassword("admin123");
//		        user1.setPassword(passwordEncoder.encode( "admin123"));
//				user1.setFullname(user1.getName()+" "+user1.getSurname());
//		        user1.setCreatedDate(new Date());
//				user1.setRole(Role.ADMIN);
//				usereRepository.save(user1);
//
//				CheckingCard checkingCard1 = new CheckingCard();
//				checkingCard1.setAddress("Ostim, 1148. Sk. No: 32/C D: 3, 06374 Yenimahalle/Ankara");
//				checkingCard1.setCheckingCardName("Ekip Ecza Deposu San. Tic. AŞ.");
//				checkingCard1.setCreatedAt(new Date());
//				checkingCard1.setCity("Ankara");
//				checkingCard1.setEmail("info@ekippharma.com");
//				checkingCard1.setFaxNumber("90 (312) 473 73 13");
//				checkingCard1.setPhoneNumber("90 (312) 473 83 23");
//				checkingCard1.setTaxIdentificationNumber(3290643182L);
//				checkingCard1.setTaxOffice("OSTİM");
//
//				checkingCard1.setType(CheckingCardType.OTHER);
//				//checkingCard1.setCountry(country);
//				checkingCard1.setUser(user1);
//				checkingCardRepository.save(checkingCard1);
//
//				CheckingCard checkingCard2 = new CheckingCard();
//				checkingCard2.setAddress("Ostim, 1148. Sk. No: 32/C D: 3, 06374 Yenimahalle/Ankara");
//				checkingCard2.setCheckingCardName("LIVA");
//				checkingCard2.setCreatedAt(new Date());
//				checkingCard2.setCity("Ankara");
//				checkingCard2.setEmail("info@ekippharma.com");
//				checkingCard2.setFaxNumber("90 (312) 473 73 13");
//				checkingCard2.setPhoneNumber("90 (312) 473 83 23");
//				checkingCard2.setTaxIdentificationNumber(3290643182L);
//				checkingCard2.setTaxOffice("OSTİM");
//
//				checkingCard2.setType(CheckingCardType.OTHER);
//				//checkingCard2.setCountry(country);
//				checkingCard2.setUser(user1);
//				checkingCardRepository.save(checkingCard2);

//				User user2 = new User();
//				user2.setName("user");
//				user2.setSurname("userx");
//				user2.setIsLoggedIn(0);
//				user2.setUsername("user");
//				user2.setFullname(user2.getName()+" "+user2.getSurname());
//				//user2.setRealPassword("user123");
//		        user2.setPassword(passwordEncoder.encode( "user123"));
//		        user2.setCreatedDate(new Date());
//				user2.setRole(Role.USER);
//				usereRepository.save(user2);

//				User user4 = new User();
//				user4.setName("admin");
//				user4.setSurname("adminx");
//				user4.setIsLoggedIn(0);
//				user4.setUsername("admin1");
//				//user4.setRealPassword("admin1123");
//				user4.setPassword(passwordEncoder.encode( "admin1123"));
//				user4.setFullname(user4.getName()+" "+user4.getSurname());
//				user4.setCreatedDate(new Date());
//				user4.setRole(Role.ADMIN);
//				usereRepository.save(user4);

//				User user5 = new User();
//				user5.setName("admin");
//				user5.setSurname("adminx");
//				user5.setIsLoggedIn(0);
//				user5.setUsername("admin2");
//				//user5.setRealPassword("admin2123");
//				user5.setPassword(passwordEncoder.encode( "admin2123"));
//				user5.setCreatedDate(new Date());
//				user5.setFullname(user5.getName()+" "+user5.getSurname());
//				user5.setRole(Role.ADMIN);
//				usereRepository.save(user5);

//				User user6 = new User();
//				user6.setName("exporter");
//				user6.setIsLoggedIn(0);
//				user6.setSurname("exporter");
//				user6.setUsername("exporter");
//				//user6.setRealPassword("exporter123");
//				user6.setPassword(passwordEncoder.encode( "ekipeczayusufk"));
//				user6.setCreatedDate(new Date());
//				user6.setFullname(user6.getName()+" "+user6.getSurname());
//				user6.setRole(Role.EXPORTER);
//				usereRepository.save(user6);

//				User user7 = new User();
//				user7.setName("manager");
//				user7.setIsLoggedIn(0);
//				user7.setSurname("manager");
//				user7.setUsername("manager");
//				user7.setStatus(1);
//				//user7.setRealPassword("manager123");
//				user7.setPassword(passwordEncoder.encode( "manageryusufk"));
//				user7.setFullname(user7.getName()+" "+user7.getSurname());
//				user7.setCreatedDate(new Date());
//				user7.setRole(Role.MANAGER);
//				usereRepository.save(user7);


//				User user8 = new User();
//				user8.setName("supplier");
//				user8.setIsLoggedIn(0);
//				user8.setSurname("supplier");
//				user8.setUsername("supplier");				//user8.setRealPassword("supplier123");
//				user8.setPassword(passwordEncoder.encode( "supplier123"));
//				user8.setFullname(user8.getName()+" "+user8.getSurname());
//				user8.setCreatedDate(new Date());
//				user8.setRole(Role.SUPPLIER);
//				usereRepository.save(user8);

//				User user9 = new User();
//				user9.setName("warehouseman");
//				user9.setSurname("warehouseman");
//				user9.setUsername("warehouseman");
//				//user9.setRealPassword("warehouseman123");
//				user9.setPassword(passwordEncoder.encode( "warehouseman123"));
//				user9.setCreatedDate(new Date());
//				user9.setFullname(user9.getName()+" "+user9.getSurname());
//				user9.setRole(Role.WAREHOUSEMAN);
//				usereRepository.save(user9);

//				// ilaçların yüklenmesi --------------------------------------------------------------
//				FileInputStream file = new FileInputStream(new File("docs/....xlsx"));
//				// Excel Dosyamizi Temsil Eden Workbook Nesnesi
//				XSSFWorkbook workbook = new XSSFWorkbook(file);
//				// Excel Dosyasının Hangi Sayfası İle Çalışacağımızı Seçelim.
//				XSSFSheet sheet = workbook.getSheetAt(0);
//				// Belirledigimiz sayfa icerisinde tum satirlari tek tek dolasacak
//				// iterator nesnesi
//				Iterator rowIterator = sheet.iterator();
//				// Okunacak Satir Oldugu Surece
//				while (rowIterator.hasNext()) {
//					// Excel içerisindeki satiri temsil eden nesne
//					Row row = (Row) rowIterator.next();
//					// Her bir satir icin tum hucreleri dolasacak iterator nesnesi
//					Iterator cellIterator = row.cellIterator();
//					DrugCard drug=new DrugCard();
//					while (cellIterator.hasNext()) {
//						// Excel icerisindeki hucreyi temsil eden nesne
//						Cell cell = (Cell) cellIterator.next();
//						// Hucrede bulunan deger turunu kontrol et
//						switch (cell.getCellType())
//						{
//							case NUMERIC:
//								drug.setDrugCode(Long.valueOf((long) cell.getNumericCellValue()));
//								break;
//							case STRING:
//								if(drug.getDrugName()==null){
//									drug.setDrugName(cell.getStringCellValue());
//								}else if(drug.getAtcCode()==null){
//									drug.setAtcCode(cell.getStringCellValue());
//								}else if(drug.getAtcName()==null){
//									drug.setAtcName(cell.getStringCellValue());
//								}else {
//									drug.setDrugCompany(cell.getStringCellValue());
//								}
//								break;
//						}
//					}
//					drugList.add(drug);//Listeye Ekle
//				}
//				System.out.println("** Listede Toplam "+drugList.size()+" Adet İlaç Vardır. **");
//				for (DrugCard drug : drugList) {
//					drug.setStatus(1);
//					drugCardRepository.save(drug);
//
//				}
//				System.out.println("Drug card loaded");
//				file.close();

			} catch (Exception e) {				
				System.out.println(e);
			}
		};
	}
}
