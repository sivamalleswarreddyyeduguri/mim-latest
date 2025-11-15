package com.hcl.mi.responsedtos;

import java.time.LocalDate;

import lombok.Data;

@Data
public class InspectionLotDto {
	
	private Integer lotId;
	
	private LocalDate creationDate;
	
	private LocalDate inspectionStartDate;

	private LocalDate inspectionEndDate;

	private String result;

	private String remarks;
	
	private String userName;

}
 