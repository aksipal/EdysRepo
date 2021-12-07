package com.via.ecza.service;

import com.via.ecza.dto.UserCameraSaveDto;
import com.via.ecza.entity.enumClass.CameraType;
import com.via.ecza.entity.Communication;
import com.via.ecza.entity.User;
import com.via.ecza.entity.UserCamera;
import com.via.ecza.repo.CommunicationRepository;
import com.via.ecza.repo.QrCodeRepository;
import com.via.ecza.repo.UserCameraRepository;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class CommunicationService {

    @Autowired
    private CommunicationRepository communicationRepository;
    @Autowired
    private QrCodeRepository qrCodeRepository;
    @Autowired
    private UserCameraRepository userCameraRepository;
    @Autowired
    private ControlService controlService;


    // teslim almada kullanılıyor
    public Communication save(Communication communication, String authHeader) throws Exception {
        //Kullanıcı Bilgisi Alındı
        User user = controlService.getUserFromToken(authHeader);

        //Veritabanında Kayıt Var mı Kontrolü Yapıldı
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);

        if(optUserCamera.isPresent()){
            communicationRepository.deleteByCameraType(optUserCamera.get().getCameraType());
            qrCodeRepository.deleteByCameraType(optUserCamera.get().getCameraType());
        }else{
            throw new Exception("Kullanıcı Kamera Bilgisi Bulunamadı !");
        }


        //packageRepository.deleteAll();

        if (communication.getBarcode() == null && communication.getExpirationDate() == null && communication.getTotalQuantity() == null)
            throw new Exception("Boş Alanları Doldurunuz.");

//        Optional<Communication> optional = communicationRepository.getSingleCommunication(communication.getCamera().ordinal()+1);
//        if(optional.isPresent())
//            return optional.get();
        communication.setStatus(0);
        communication.setCameraType(optUserCamera.get().getCameraType());
        communication = communicationRepository.save(communication);
        return communication;
    }

    public Boolean delete() {
        communicationRepository.deleteAll();
        return true;
    }

    public Communication findByCommunicationId(CameraType cameraType) throws NotFoundException {

        Optional<Communication> opt = communicationRepository.findByCameraType(cameraType.ordinal() + 1);
        if (!opt.isPresent()) {
            throw new NotFoundException("Kayıt bulunamadı");
        }
        return opt.get();
    }

    public Communication control(Communication com,String authHeader) throws Exception {
        //Kullanıcı Bilgisi Alındı
        User user = controlService.getUserFromToken(authHeader);

        //Veritabanında Kayıt Var mı Kontrolü Yapıldı
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);

        if(!optUserCamera.isPresent()){
            throw new Exception("Kullanıcı Kamera Kaydı Bulunamadı !");
        }

        Optional<Communication> optional = communicationRepository.getSingleCommunication(optUserCamera.get().getCameraType());
        return optional.get();
    }

    public Communication start(Communication com,String authHeader) throws Exception {
        //packageRepository.deleteAll();

        //Kullanıcı Bilgisi Alındı
        User user = controlService.getUserFromToken(authHeader);

        //Veritabanında Kayıt Var mı Kontrolü Yapıldı
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);

        if(optUserCamera.isPresent()){
            communicationRepository.deleteByCameraType(optUserCamera.get().getCameraType());
            qrCodeRepository.deleteByCameraType(optUserCamera.get().getCameraType());
        }else{
            throw new Exception("Kullanıcı Kamera Bilgisi Bulunamadı !");
        }

        if (com.getBarcode() == null && com.getExpirationDate() == null && com.getTotalQuantity() == null)
            throw new Exception("Boş Alanları Doldurunuz.");

//        Optional<Communication> optional = communicationRepository.getSingleCommunication(communication.getCamera().ordinal()+1);
//        if(optional.isPresent())
//            return optional.get();
        com.setStatus(0);
        com.setCameraType(optUserCamera.get().getCameraType());
        com = communicationRepository.save(com);
        return com;
    }

    public String saveUserCamera(UserCameraSaveDto dto, String authHeader) throws Exception {
        //Kullanıcı Bilgisi Alındı
        User user = controlService.getUserFromToken(authHeader);

        //Dto'dan Gelen Camera Type Alındı
        int cameraType = 0;
        if (dto.getCameraType() != null && dto.getCameraType().trim().length() > 0) {
            cameraType = Integer.valueOf(String.valueOf(dto.getCameraType().trim().charAt(dto.getCameraType().trim().length() - 1)));
        }

        //Veritabanında Kayıt Var mı Kontrolü Yapıldı
        Optional<UserCamera> optUserCamera = userCameraRepository.findByCameraType(cameraType);

        if (optUserCamera.isPresent()) {
            UserCamera userCamera = optUserCamera.get();

            //Aynı Kullanıcı Kamerasını Günceller İse
            if (userCamera.getUser().getUserId() == user.getUserId() && userCamera.getCameraType() != cameraType) {
                userCamera.setCreatedAt(new Date());
                userCamera.setCameraType(cameraType);
                userCamera = userCameraRepository.save(userCamera);
                return "Kamera Seçimi Başarılı.";
            } else if (userCamera.getUser().getUserId() == user.getUserId() && userCamera.getCameraType() == cameraType) {
                //Aynı Kullanıcı Aynı Kamerayı Seçer İse
                return "Seçili Olan Kamerayı Tekrar Seçtiniz.";
            } else if (userCamera.getUser().getUserId() != user.getUserId()) {
                //Farklı Kullanıcı Kamerayı Almak İsterse Engellenir
                return "<b>" + dto.getCameraType().trim() + "</b> Adlı Kameranın Kullanımı Devam Etmektedir.<br/>Lütfen Daha Sonra Deneyiniz.";
            }

        } else if (!optUserCamera.isPresent()) {

            Optional<UserCamera> optUserCamera2 = userCameraRepository.findByUser(user);

            if (optUserCamera2.isPresent()) {
                //Kullanıcıya Ait Başka Kamera Var İse Yeni Kayıt Eklemek Yerine Var Olanın Kamera Type'ını Günceller
                UserCamera userCamera2 = optUserCamera2.get();
                userCamera2.setCreatedAt(new Date());
                userCamera2.setCameraType(cameraType);
                userCamera2 = userCameraRepository.save(userCamera2);
                return "Kamera Seçimi Başarılı.";
            } else {
                //Yeni Kayıt Oluşturulur
                UserCamera userCamera = new UserCamera();
                userCamera.setCameraType(cameraType);
                userCamera.setStatus(0);
                userCamera.setCreatedAt(new Date());
                userCamera.setUser(user);
                userCamera = userCameraRepository.save(userCamera);
                return "Kamera Seçimi Başarılı.";
            }
        }

        return "İşlem Sırasında Hata Oluştu.";
    }

    public String controlUserCamera(String authHeader) throws Exception {
        //Kullanıcı Bilgisi Alındı
        User user = controlService.getUserFromToken(authHeader);

        //Veritabanında Kayıt Var mı Kontrolü Yapıldı
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);

        if (optUserCamera.isPresent()) {
            return "CAMERA_" + optUserCamera.get().getCameraType();
        } else if (!optUserCamera.isPresent()) {
            return null;
        }

        return null;
    }

    public Boolean deleteUserCamera(String authHeader) throws Exception {
        //Kullanıcı Bilgisi Alındı
        User user = controlService.getUserFromToken(authHeader);

        //Veritabanında Kayıt Var mı Kontrolü Yapıldı
        Optional<UserCamera> optUserCamera = userCameraRepository.findByUser(user);

        if (optUserCamera.isPresent()) {
            //Kayıt Varsa Silinir
            userCameraRepository.deleteById(optUserCamera.get().getUserCameraId());
        }

        return true;
    }

    public boolean tokenControlForUserCamera() throws Exception {
        //Tüm User Camera Listesi Alındı
        List<UserCamera> list = userCameraRepository.findAll();

        Date nowDate = new Date();
        for (UserCamera camera : list) {
            //Kamera Seçimi Üzerinden 16 Saat ve Daha Fazla Geçtiyse Kayıt Silinir
            if ((TimeUnit.MILLISECONDS.toHours(nowDate.getTime() - camera.getCreatedAt().getTime())) >= 16) {
                userCameraRepository.deleteById(camera.getUserCameraId());
            }
        }
        return true;
    }
    public boolean deleteByUser(User user) throws Exception {
        //Tüm User Camera Listesi Alındı
        Optional<UserCamera> data = userCameraRepository.findByUser(user);
        if(data.isPresent())
            userCameraRepository.deleteById(data.get().getUserCameraId());

        return true;
    }

}
