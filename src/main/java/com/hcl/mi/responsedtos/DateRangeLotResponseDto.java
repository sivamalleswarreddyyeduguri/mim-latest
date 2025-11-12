package com.hcl.mi.responsedtos;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DateRangeLotResponseDto {
	
	private int lotId;
	
	private LocalDate createdOn;
	
	private LocalDate startOn;
	
	private LocalDate endOn;
	
	private String result;
	
	private String inspectedBy;
	
	private String material;
	
	private String plant;
	
	private String vendor;
}
