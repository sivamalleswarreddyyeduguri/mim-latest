package com.hcl.mi.requestdtos;

import com.hcl.mi.customannotation.StrongPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewUser {
	
	private Integer id;
	
	@Size(min = 3, max = 25, message = "username should be min 5 char and max char 25")
	private String username;
	
	@StrongPassword
	private String password;
	
	@Email(message = "please provide valid email")
	private String email;
	
	private String mobileNum;
	
	private String role;
}
