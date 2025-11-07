package com.hcl.mi.responsedtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VendorDto {
	
	private int vendorId;
	
	@Size(min=3, max = 50, message = "vendor name should be min 5 char and max 50")
	private String name;
	
	@Email(message = "please provide valid email")
	private String email;
	
	private boolean status = true;
}
