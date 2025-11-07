package com.hcl.mi.requestdtos;
//
//import java.time.LocalDate;
//
//import jakarta.validation.constraints.NotNull;
//import lombok.Data;
//
//@Data
//public class DateRangeLotSearch {
//	
//	@NotNull(message = "please provide valid date format of yyyy-mm-dd")
//	private LocalDate fromDate;
//	
//	@NotNull(message = "please provide valid date format of yyyy-mm-dd")
//	private LocalDate toDate;
//	
//	private String materialId;
//	
//	private int vendorId;
//	
//	private String plantId;
//	
//	private String status;
//}

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DateRangeLotSearch {

    @NotNull(message = "please provide valid date format of yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @NotNull(message = "please provide valid date format of yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    private String materialId;

    private int vendorId;

    private String plantId;

    private String status;
}