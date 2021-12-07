package com.via.ecza.service;
 

import java.util.List;

import com.via.ecza.dto.UserDto;
import com.via.ecza.entity.User;
import javassist.NotFoundException;
import org.springframework.http.ResponseEntity;

public interface UserService {
    Boolean saveUser(User user);

    User findByUsername(String username);

    List<User> findAllUsers();

    User getYourAccount(String authHeader) throws NotFoundException;

    ResponseEntity<?> update(UserDto dto);

}
