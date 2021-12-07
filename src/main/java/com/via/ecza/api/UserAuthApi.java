package com.via.ecza.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class UserAuthApi {

	@GetMapping("/api/user/all")
	public ResponseEntity<String> hello(){
		
		return ResponseEntity.ok("basic basic");
	}
}
