package com.hcl.mi.requestdtos;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaterialCharDto {  
	 
	private Integer characteristicId;

    @Size(min = 5, max = 256, message = "Characteristic description should be between 5 and 256 characters")
    private String charDesc;

    @NotNull(message = "Invalid upper tolerance limit")
    private Double utl;

    @NotNull(message = "Invalid lower tolerance limit")
    private Double ltl;

    @NotBlank(message = "Unit of measurement should not be empty")
    private String uom;

    @NotBlank(message = "Invalid material ID")
    private String matId;

    @AssertTrue(message = "Lower tolerance limit must be less than or equal to upper tolerance limit")
    public boolean isToleranceRangeValid() {
        if (ltl == null || utl == null) return true;
        return ltl <= utl;
    }
}
