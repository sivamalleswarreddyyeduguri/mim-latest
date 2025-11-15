package com.hcl.mi.responsedtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
public class MaterialInspectionCharacteristicsDto {

    private Integer characteristicId; 

    @NotNull(message = "Characteristic description must not be null")
    @Size(min = 1, message = "Characteristic description must not be empty")
    private String characteristicDescription;

    @NotNull(message = "Upper tolerance limit must not be null")
    @PositiveOrZero(message = "Upper tolerance limit must be zero or positive")
    private Double upperToleranceLimit;

    @NotNull(message = "Lower tolerance limit must not be null")
    @PositiveOrZero(message = "Lower tolerance limit must be zero or positive")
    private Double lowerToleranceLimit;

    private String unitOfMeasure; 
    
    private String matId; 

    @AssertTrue(message = "Lower tolerance limit must be less than or equal to upper tolerance limit")
    public boolean isToleranceRangeValid() {
        if (lowerToleranceLimit == null || upperToleranceLimit == null) return true;
        return lowerToleranceLimit <= upperToleranceLimit;
    }
}

