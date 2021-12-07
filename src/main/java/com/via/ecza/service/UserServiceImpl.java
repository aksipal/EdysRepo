package com.via.ecza.service;

import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.Role;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.via.ecza.repo.UserRepository;

import javax.persistence.EntityManager;
import java.util.*;

@Slf4j
@Service
@Transactional
public class UserServiceImpl  {


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private ControlService controlService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EntityManager entityManager;

    public Boolean saveUser(UserSaveDto dto){
        try{

            User user = mapper.map(dto,User.class);

            user.setUsername(user.getUsername().trim());

            user.setEmail(user.getEmail().trim());
            user.setIsLoggedIn(0);
            user.setPassword(user.getPassword().trim());
            user.setStatus(1);
            //user.setRealPassword(user.getPassword());
            user.setPassword(passwordEncoder.encode( user .getPassword()));
            user.setCreatedDate(new Date());
            user.setFullname(user.getName()+" "+user.getSurname());
            user = userRepository.save(user);
            return true;
        }catch (Exception e){
            throw e;
        }

    }

    public User findByUsername(String username){
        return userRepository.findByUsername(username).orElse(null);
    }

    public List<User> findAllUsers(String authHeader) throws NotFoundException {
        String usernameFromControl = controlService.getUsernameFromToken(authHeader);
        Optional<User> opt = userRepository.findByUsername(usernameFromControl);
        if(!opt.isPresent()) {
            log.error("Böyle Bir Kullanıcı Yoktur");
            throw new NotFoundException("Böyle Bir Kullanıcı Yoktur");
        }
        List<User> list = userRepository.findByUsernameNot(opt.get().getUsername(), Sort.by(Sort.Direction.ASC, "userId"));
        return list;
    }

    public UserDto getYourAccount(String authHeader) throws NotFoundException {

        String usernameFromControl = controlService.getUsernameFromToken(authHeader);
        Optional<User> opt = userRepository.findByUsername(usernameFromControl);
        if(!opt.isPresent()) {
            log.error("Böyle bir kullanıcı yoktur.");
            throw new NotFoundException("Böyle bir kullanıcı yoktur.");
        }
        UserDto dto = mapper.map(opt.get(), UserDto.class);
        return dto;
    }

    public Boolean update(long userid, UserDto dto) throws NotFoundException {
        Optional<User> opt = userRepository.findById(userid);
        if(!opt.isPresent()) {
            log.error("Böyle bir kullanıcı yoktur.");
            throw new NotFoundException("Böyle bir kullanıcı yoktur.");
        }
        User user = opt.get();
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setRole(dto.getRole());
        user.setMotherName(dto.getMotherName());
        user.setFatherName(dto.getFatherName());
        user.setTcNo(dto.getTcNo());
        user = userRepository.save(user);
        return true;
    }

    public Boolean updateYourSelf(String authHeader, UserDto userDto) throws NotFoundException {
        String usernameFromControl = controlService.getUsernameFromToken(authHeader);
        Optional<User> opt = userRepository.findByUsername(usernameFromControl);
        if(!opt.isPresent()) {
            log.error("Böyle bir kullanıcı yoktur.");
            throw new NotFoundException("Böyle bir kullanıcı yoktur.");
        }
        User user = opt.get();
        user.setUsername(userDto.getUsername().trim());
        if(userDto.getName() != null)
            user.setName(userDto.getName().trim());
        if(userDto.getSurname() != null)
            user.setSurname(userDto.getSurname().trim());
        if(userDto.getEmail() != null)
            user.setEmail(userDto.getEmail().trim());
        user.setBornDate(userDto.getBornDate());
        user.setFullname(userDto.getName()+" "+userDto.getUsername());
        user.setIsLoggedIn(0);
        user = userRepository.save(user);
        return true;
    }

    public UserDto findById(long userid) throws NotFoundException {
        Optional<User> opt = userRepository.findById(userid);
        if(!opt.isPresent()) {
            log.error("Böyle bir kullanıcı yoktur.");
            throw new NotFoundException("Böyle bir kullanıcı yoktur.");
        }
        log.info("Kullanıcı Bulundu.");
        User user = opt.get();
        UserDto dto = mapper.map(user, UserDto.class);
        return dto;
    }

    public ResponseEntity<?> updateMyPassword(String authHeader, UserPasswordUpdateDto dto) throws Exception {
        User user = this.getUserFromToken(authHeader);
        boolean result = false;
        if(!dto.getNewPassword().equals(dto.getRepeatNewPassword()))
            throw new IllegalArgumentException("Şifreler uyuşmuyor.");
        if(dto.getNewPassword().equals(dto.getRepeatNewPassword())){
            if(passwordEncoder.matches(dto.getOldPassword(), user.getPassword())){
                user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
                //user.setRealPassword(dto.getNewPassword());
                user.setIsLoggedIn(0);
                userRepository.save(user);
                result = true;
            }
        }
        return ResponseEntity.ok(result);
    }


    private User getUserFromToken(String authHeader) throws NotFoundException {

        String username = controlService.getUsernameFromToken(authHeader);
        Optional<User> optUser = userRepository.findByUsername(username);
        if(!optUser.isPresent()) {
            throw new NotFoundException("Böyle bir kullanıcı bulunamadı.");
        }
        return optUser.get();
    }

    public Boolean changePassword(long userid, UserPasswordUpdateDto dto) throws Exception {
        Optional<User> opt = userRepository.findById(userid);
        boolean result = false;
        if(!opt.isPresent()) {
            log.error("Böyle bir kullanıcı yoktur.");
            throw new NotFoundException("Böyle bir kullanıcı yoktur.");
        }
        User user = opt.get();
        if(!dto.getNewPassword().equals(dto.getRepeatNewPassword()))
            throw new IllegalArgumentException("Şifreler uyuşmuyor.");
        if(dto.getNewPassword().equals(dto.getRepeatNewPassword())){
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            //user.setRealPassword(dto.getNewPassword());
            user = userRepository.save(user);
            result = true;
        }else{
            throw new Exception("Girdiğiniz Şifreler uyuşmamaktadır.");
        }
        return result;
    }

    public Page<UserDto> getAllWithPageUser(Pageable page,String authHeader) throws Exception {
        try {

            User user = this.getUserFromToken(authHeader);
            if (user.getRole() == Role.ADMIN) {
//                StringBuilder createSqlQuery = new StringBuilder("select * from users u ");
//
//                List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), User.class).getResultList();
//
//                UserDto[] dtos = mapper.map(list,UserDto[].class );
//                List<UserDto> dtosList=Arrays.asList(dtos);
//
//                Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
//                int start = Math.min((int) paging.getOffset(), dtosList.size());
//                int end = Math.min((start + paging.getPageSize()), dtosList.size());

                Page<User> list = userRepository.findByUsernameNot(user.getUsername(), page);
                Page<UserDto> pageList = list.map(UserDto::new);
                return pageList;

            } else if (user.getRole() == Role.EXPORTER) {
                return null;
            }else {
                return null;
            }

        } catch (Exception e) {
            throw e;
        }

    }

    public Page<UserDto> getAllWithPageable(Pageable page, String authHeader, UserSearchDto dto)  throws Exception  {

        try {
            StringBuilder createSqlQuery = new StringBuilder("select * from users u where u.status=1 ");

            if(dto.getName() != null) createSqlQuery.append(" and u.name ILIKE '%"+dto.getName()+"%'");

            if(dto.getSurname() != null) createSqlQuery.append(" and u.surname ILIKE '%"+dto.getSurname()+"%'");

            if(dto.getUsername() != null) createSqlQuery.append(" and u.username ILIKE '%"+dto.getUsername()+"%'");

            if(dto.getEmail() != null) createSqlQuery.append(" and u.email ILIKE '%"+dto.getEmail()+"%'");

            if(dto.getTcNo() != null) createSqlQuery.append(" and u.tc_no ILIKE '%"+dto.getTcNo()+"%'");

            if(dto.getRole() != null) createSqlQuery.append(" and u.role ILIKE '%"+dto.getRole().toString()+"%'");


            createSqlQuery.append(" order by u.username");

            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), User.class).getResultList();

            UserDto[] dtos = mapper.map(list,UserDto[].class );
            List<UserDto> dtosList=Arrays.asList(dtos);

            int start = Math.min((int) page.getOffset(), dtosList.size());
            int end = Math.min((start + page.getPageSize()), dtosList.size());

            Page<UserDto> pageList =  new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());
            return pageList;
        } catch (Exception e) {
            throw e;
        }
    }
}
