package com.hcl.mi.responsedtos;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class PlantDto {
	
	@NotBlank(message = "please provide valid plantId")
    private String plantId;
	
	@NotBlank(message = "invalid plant name")
	private String plantName;
	
	private boolean status = true;
	
	@NotBlank(message = "please provide valid state")
	private String state;
	
	@NotBlank(message = "please provide valid city")
	private String city;
}
 