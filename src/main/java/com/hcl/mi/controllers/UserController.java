package com.hcl.mi.controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/user") 
@Slf4j
public class UserController {
 

	private final UserService userService;
	private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
	private final JwtUtil jwtUtil;

	public UserController(UserService userService,
			org.springframework.security.core.userdetails.UserDetailsService userDetailsService, JwtUtil jwtUtil) {
		this.userService = userService;
		this.userDetailsService = userDetailsService;
		this.jwtUtil = jwtUtil;
	}
 
	@PostMapping("/register")
	public ResponseEntity<ResponseDto> register(@Valid @RequestBody NewUser newUser) {
		userService.saveUser(newUser);

		log.info("New user registered: {}", newUser.getUsername());
		return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDto("201", "user registered successfully"));

	}

	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDto loginRequest) {
		String username = loginRequest.getUsername();
		String password = loginRequest.getPassword();
 
		User user = userService.checkUserCredentails(username, password);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
		}

		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		String token = jwtUtil.generateToken(userDetails);
		String refreshToken = jwtUtil.generateRefreshToken(userDetails);

		return ResponseEntity.ok(Map.of(
				"token", token, 
				"refreshToken", refreshToken, 
				"username", username, "email",user.getEmail(), 
				"mobileNumber",user.getMobileNum(),
				"id", user.getId(),
				"roles",userDetails.getAuthorities().stream().map(a -> a.getAuthority()).toList()));
	}

	@PostMapping("/refresh-token")
	public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
 
		log.info("inside refreshToken");

		String refreshToken = request.get("refreshToken");

		if (refreshToken == null || refreshToken.isBlank()) {
			return ResponseEntity.badRequest().body(Map.of("error", "Refresh token is missing"));
		}

		try {
			String username = jwtUtil.extractUsername(refreshToken);
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);

			boolean isValid = jwtUtil.validateToken(refreshToken, userDetails);
			if (!isValid) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("error", "Invalid or expired refresh token"));
			}

			String newAccessToken = jwtUtil.generateToken(userDetails);

			return ResponseEntity.ok(Map.of("accessToken", newAccessToken, "username", username, "roles",
					userDetails.getAuthorities().stream().map(a -> a.getAuthority()).toList()));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token processing failed"));
		}
	}

	@GetMapping("/get-all")
	public ResponseEntity<List<NewUser>> getAll() {

		return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers());
	}

}