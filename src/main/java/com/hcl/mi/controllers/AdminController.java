package com.hcl.mi.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hcl.mi.requestdtos.NewUser;
import com.hcl.mi.responsedtos.ResponseDto;
import com.hcl.mi.services.UserService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin")
@Slf4j
public class AdminController {

    private final UserService userService; 

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create-inspector")
    public ResponseEntity<ResponseDto> createInspector(@Valid @RequestBody NewUser newUser) {
    	    	
        newUser.setRole("INSPECTOR");
        userService.saveUser(newUser);
        
            log.info("Inspector created: {}", newUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
            		.body(new ResponseDto("201", "registration successfull"));  
        
    }
    
    @PutMapping("/update/{id}")
	public ResponseEntity<ResponseDto> updateUser(@PathVariable Integer id, @RequestBody NewUser newUser ) {
				 		
		userService.updateUser(id, newUser);
		return ResponseEntity.ok(new ResponseDto("200", "user updated successfully"));
	}
	
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<ResponseDto> deleteVendor(@PathVariable Integer id) {
		
		log.info("inside deleteVendor():");

		userService.deleteVendor(id);
		return  ResponseEntity
				.status(HttpStatus.OK)
				.body(new ResponseDto("200", "vendor deleted successfully"));
	}
}