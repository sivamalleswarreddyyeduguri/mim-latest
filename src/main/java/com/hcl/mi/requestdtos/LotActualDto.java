package com.hcl.mi.requestdtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LotActualDto {
	
	@NotNull(message = "please valid lot number")
	private int lotId;
	
	@NotNull(message = "please valid characteristic id")
	private int charId;
	
	@NotNull(message = "invalid upper tolerance limit")
	private double maxMeas;
	
	@NotNull(message = "invalid lower tolerance limit")
	private double minMeas;
}
  