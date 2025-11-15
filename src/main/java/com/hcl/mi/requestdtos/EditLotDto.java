package com.hcl.mi.requestdtos;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EditLotDto {
	
	@NotNull(message = "please provide valid lot number")
	private int id;
	
	@NotBlank(message = "empty result is not acceptable")
	private String result;
	
	@NotBlank(message = "empty remarks are not acceptable")
	private String remarks;
	
	@NotNull(message = "please provide valid date format of yyyy-mm-dd")
	private LocalDate date;
}
