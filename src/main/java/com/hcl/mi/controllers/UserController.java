package com.hcl.mi.controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcl.mi.entities.User;
import com.hcl.mi.requestdtos.LoginRequestDto;
import com.hcl.mi.requestdtos.NewUser;
import com.hcl.mi.responsedtos.ResponseDto;
import com.hcl.mi.security.JwtUtil;
import com.hcl.mi.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService,
                          org.springframework.security.core.userdetails.UserDetailsService userDetailsService,
                          JwtUtil jwtUtil) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    // public registration - always default role USER
	@PostMapping("/register")
	public ResponseEntity<ResponseDto> register(@Valid @RequestBody NewUser newUser) {
		userService.saveUser(newUser);

		LOGGER.info("New user registered: {}", newUser.getUsername());
		return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDto("201", "user registered successfully"));

	} 

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequest) {
//        String username = loginRequest.getUsername();
//        String password = loginRequest.getPassword();
//
//        boolean ok = userService.checkUserCredentails(username, password);
//        if (!ok) {
//            LOGGER.warn("Unauthorized login attempt: {}", username);
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(Map.of("error", "Invalid credentials"));
//        }
//
//        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//        String token = jwtUtil.generateToken(userDetails);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("token", token);
//        response.put("username", username);
//        response.put("roles", userDetails.getAuthorities()
//                .stream()
//                .map(a -> a.getAuthority())
//                .collect(Collectors.toList()));
//
//        LOGGER.info("User logged in: {}", username);
//        return ResponseEntity.ok(response);
//    }
	
//	@PostMapping("/login")
//	public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequest) {
//	    String username = loginRequest.getUsername();
//	    String password = loginRequest.getPassword();
//
//	    boolean ok = userService.checkUserCredentails(username, password);
//	    if (!ok) {
//	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//	                .body(Map.of("error", "Invalid credentials"));
//	    }
//
//	    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//	    String token = jwtUtil.generateToken(userDetails);
//	    String refreshToken = jwtUtil.generateRefreshToken(userDetails);
//	    
//	   
//
//	    return ResponseEntity.ok(Map.of(
//	        "token", token,
//	        "refreshToken", refreshToken,
//	        "username", username,
//	        "roles", userDetails.getAuthorities().stream()
//	            .map(a -> a.getAuthority())
//	            .collect(Collectors.toList())
//	    ));
//	}
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequest) {
	    String username = loginRequest.getUsername();
	    String password = loginRequest.getPassword();

	    User user = userService.checkUserCredentails(username, password);
	    if (user == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(Map.of("error", "Invalid credentials"));
	    }

	    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
	    String token = jwtUtil.generateToken(userDetails);
	    String refreshToken = jwtUtil.generateRefreshToken(userDetails);
	    
	   

	    return ResponseEntity.ok(Map.of(
	        "token", token,
	        "refreshToken", refreshToken,
	        "username", username,
	        "email", user.getEmail(),
	        "mobileNumber", user.getMobileNum(),
	        "roles", userDetails.getAuthorities().stream()
	            .map(a -> a.getAuthority())
	            .collect(Collectors.toList())
	    ));
	}
    
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
    	
    	System.out.println("inside refreshToken");
    	
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Refresh token is missing"));
        }

        try {
            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            boolean isValid = jwtUtil.validateToken(refreshToken, userDetails);
            if (!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired refresh token"));
            }

            String newAccessToken = jwtUtil.generateToken(userDetails); 

            return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "username", username,
                "roles", userDetails.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(Collectors.toList())
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token processing failed"));
        }
    }
    
    
    @GetMapping("/get-all")
    public ResponseEntity<List<NewUser>> getAll() {
    	
    	return ResponseEntity.status(HttpStatus.OK)
    			.body(userService.getAllUsers());  
    }
    
   

}