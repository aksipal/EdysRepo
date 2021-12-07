package com.via.ecza.api;


import com.via.ecza.dto.UserDto;
import com.via.ecza.dto.UserPasswordUpdateDto;
import com.via.ecza.entity.User;
import com.via.ecza.service.UserServiceImpl;
import com.via.ecza.util.ApiPath;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(ApiPath.MyAccountCtrl.CTRL)
public class MyAccountApi {

    @Autowired
    private UserServiceImpl userService;

    @GetMapping
    public ResponseEntity<UserDto> getYourAccount(@RequestHeader("Authorization") String authHeader) throws NotFoundException {
        return ResponseEntity.ok(userService.getYourAccount(authHeader));
    }
    @PostMapping("/update-my-password")
    public ResponseEntity<?> getYourAccount(@RequestHeader("Authorization") String authHeader, @RequestBody UserPasswordUpdateDto dto) throws Exception {
        return ResponseEntity.ok(userService.updateMyPassword(authHeader,dto));
    }
    @PutMapping("/username/{username}")
    public ResponseEntity<Boolean> updateYourSelf(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UserDto userDto) throws NotFoundException {
        return ResponseEntity.ok(userService.updateYourSelf(authHeader, userDto));
    }

}
