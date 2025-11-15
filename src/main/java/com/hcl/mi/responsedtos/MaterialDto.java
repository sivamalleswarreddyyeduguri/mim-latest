package com.hcl.mi.responsedtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MaterialDto {
	
	private String materialId;
	
	@Size(min = 5, max = 256, message = "material description should be greater than 5 char and less than 256 char")
	private String materialDesc;
	
	@NotBlank(message = "invalid material type")
	private String type;
	
	private boolean status = true;
}
 