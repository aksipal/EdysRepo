package com.via.ecza.api;

import java.util.*;

import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.entity.enumClass.RoleClass;
import com.via.ecza.service.ControlService;
import com.via.ecza.service.UserServiceImpl;
import com.via.ecza.dto.AuthDto;
import com.via.ecza.entity.User;
import com.via.ecza.jwt.JwtResponse;
import com.via.ecza.jwt.JwtTokenProvider;
import com.via.ecza.repo.UserRepository;
import io.jsonwebtoken.impl.DefaultClaims;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@RestController
public class LoginApi {
	@Autowired
	private AuthenticationManager authenticationManager;


	public static final String TOKEN_PREFIX = "Bearer ";
	@Autowired
	private ControlService controlService;

	@Autowired
	private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private UserRepository repository;


    // login olmak için kullanılan linktir
	// username: -----
	// password: -----
	// parametreleri almaktadır.
	@PostMapping("/api/login")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthDto authenticationRequest) throws Exception {
		try {
			Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
					authenticationRequest.getUsername(), authenticationRequest.getPassword()));

			SecurityContextHolder.getContext().setAuthentication(authentication);
			String jwt = jwtTokenProvider.generateToken(authentication);
			String username = authenticationRequest.getUsername();
			Optional<User> opt = repository.findByUsername(username);

			if(!opt.isPresent()){
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UNAUTHORIZED");
			}
			User user = opt.get();


			// **** ekleme yapıldı ****

			//silinmiş kullanıcı var ise uyarı verilsin
			if(user.getIsDeleted()!=null && user.getIsDeleted()==true){
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UNAUTHORIZED");
			}

			//yetki eczacı değil ise reddedilsin
			if(user.getRole()!=Role.PHARMACY){
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UNAUTHORIZED");
			}

			//  üye giriş yaparken login aktifliğinin 1 olup olmadığını kontrol eder
			if(user.getIsLoggedIn() == 1)
				return ResponseEntity.status(HttpStatus.CONFLICT).body("CONFLICT");

			//  üye giriş yaparken login aktifliğini 1 e çeker
			user.setIsLoggedIn(1);
			user = userRepository.save(user);
			return ResponseEntity.ok(new JwtResponse(user.getUserId(), username,jwt,null,user.getRole()));
		}catch (BadCredentialsException e) {
			//ApiError error = new ApiError(401, "Unauthorized request : "+e.getMessage(), "/api/login");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UNAUTHORIZED");
		}
		catch (Exception e) {
			throw e;
		}
	
	}
	@GetMapping("/api/roles")
	public ResponseEntity<List<RoleClass>> getAllBookStatus() {
		List<Role> roles= Arrays.asList(Role.values());
		ArrayList<RoleClass> roleClasses= new ArrayList<>();
		roles.forEach(role->{
			RoleClass roleClass = new RoleClass(role, role.getValue());
			roleClasses.add(roleClass);
		});
		return ResponseEntity.ok(roleClasses);
	}



//	@PostMapping("/refresh-token")
//	public ResponseEntity<?> refreshToken(@RequestBody TokenDto dto) throws Exception {
//		System.out.println(dto.getToken() );
//		try {
//			if(dto.getToken() != null ) {
//				String token = dto.getToken().replace(TOKEN_PREFIX, "");
//				Boolean controlTokenExpired  =  jwtTokenProvider.isTokenExpired(token);
//				return ResponseEntity.ok(controlTokenExpired);
//			}
//			return ResponseEntity.ok(true);
//		}catch (BadCredentialsException e) {
//			//ApiError error = new ApiError(401, "Unauthorized request : "+e.getMessage(), "/api/login");
//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("bad Request");
//		}
//		catch (Exception e) {
//			throw e;
//		}
//	}





	// WebSecurityConfig linkinde permit all edilmiş bir linktir.
	// jwt tokenı yenilemek için kullanılacaktır.
	@GetMapping("/refresh-token")
	public ResponseEntity<?> refreshtoken(HttpServletRequest request) throws Exception {
		// From the HttpRequest get the claims
		DefaultClaims claims = (io.jsonwebtoken.impl.DefaultClaims) request.getAttribute("claims");

		Map<String, Object> expectedMap = getMapFromIoJsonwebtokenClaims(claims);
		String token = jwtTokenProvider.doGenerateRefreshToken(expectedMap, expectedMap.get("sub").toString());
		return ResponseEntity.ok(token);
	}

	public Map<String, Object> getMapFromIoJsonwebtokenClaims(DefaultClaims claims) {
		Map<String, Object> expectedMap = new HashMap<String, Object>();
		for (Map.Entry<String, Object> entry : claims.entrySet()) {
			expectedMap.put(entry.getKey(), entry.getValue());
		}
		return expectedMap;
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
