package com.hcl.mi.responsedtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class PlantDto {
	
	@NotBlank(message = "please provide valid plantId")
    private String plantId;
	
	@NotBlank(message = "invalid plant name")
	private String plantName;
	
	@NotBlank(message = "please provide valid location")
	private String location;
	
	private boolean status = true;
	
}
