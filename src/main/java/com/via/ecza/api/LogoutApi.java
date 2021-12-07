package com.via.ecza.api;
import com.via.ecza.entity.User;
import com.via.ecza.repo.UserRepository;
import com.via.ecza.service.CommunicationService;
import com.via.ecza.service.ControlService;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/logout")
public class LogoutApi {

    @Autowired
    private ControlService controlService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommunicationService communicationService;


    // üye cıkıs yaparken login aktifliğini 0 a çeker
    @GetMapping("/user")
    public ResponseEntity<Boolean> logOut( @RequestHeader("Authorization") String authHeader ) throws Exception {
        User user = this.getUserFromToken(authHeader);
        user.setIsLoggedIn(0);
        user = userRepository.save(user);
        communicationService.deleteByUser(user);
        return ResponseEntity.ok(true);
    }

    // bu lınke uye gırısı yapmadan ulaşılabılıyor.
    // uye gırısı yapmıs  bır user ın tokenı öldüğü zaman kullanılıyor.
    @GetMapping("/default/{username}")
    public ResponseEntity<Boolean> defaultLogOut( @RequestHeader("Authorization") String authHeader,
                                                  @PathVariable String username) throws Exception {
        Optional<User> opt = userRepository.findByUsername(username);
        User user = opt.get();
        user.setIsLoggedIn(0);
        user = userRepository.save(user);
        communicationService.deleteByUser(user);
        return ResponseEntity.ok(true);
    }

    private User getUserFromToken(String authHeader) throws NotFoundException {

        String username = controlService.getUsernameFromToken(authHeader);
        Optional<User> optUser = userRepository.findByUsername(username);
        if(!optUser.isPresent()) {
            throw new NotFoundException("Not found User");
        }
        return optUser.get();
    }
}
