package com.hcl.mi.responsedtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LotActualsAndCharacteristicsResponseDto {
	private int lotId;
	private int sNo;
	private int characteristicId;
	private String characteristicDesc;
	private Double upperToleranceLimit;
	private Double lowerToleranceLimit;
	private String unitOfMeasure;
	private Double actualUtl;
	private Double actualLtl;
}
 