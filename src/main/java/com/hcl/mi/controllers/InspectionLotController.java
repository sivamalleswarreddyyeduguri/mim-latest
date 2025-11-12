package com.hcl.mi.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcl.mi.requestdtos.DateRangeLotSearch;
import com.hcl.mi.requestdtos.EditLotDto;
import com.hcl.mi.requestdtos.LotActualDto;
import com.hcl.mi.requestdtos.LotCreationDto;
import com.hcl.mi.responsedtos.DateRangeLotResponseDto;
import com.hcl.mi.responsedtos.InspectionLotDto;
import com.hcl.mi.responsedtos.LotActualsAndCharacteristicsResponseDto;
import com.hcl.mi.responsedtos.ResponseDto;
import com.hcl.mi.servicesImpl.InspectionServiceImpl;
import com.hcl.mi.utils.ApplicationConstants;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/inspection")
@Tag(name = "InspectionLot Controller", description = "responsible for actuals related data")
public class InspectionLotController {
	private final InspectionServiceImpl inspectionlotService;

	private Logger LOG = LoggerFactory.getLogger(InspectionLotController.class);

	public InspectionLotController(InspectionServiceImpl inspectionlotService) {
		super();
		this.inspectionlotService = inspectionlotService;
	} 
  
	@PostMapping("/create/lot")  
	public ResponseEntity<ResponseDto> addInspectionLot(@Valid @RequestBody LotCreationDto lotDto) {

		inspectionlotService.createInspectionLot(lotDto);

			LOG.info("lot created successfully");

			return ResponseEntity.status(HttpStatus.OK).body(new ResponseDto("201", "lot created successfully"));   
	}

	@GetMapping("/lot/{id}")
	public ResponseEntity<InspectionLotDto> fetchInspectionLotDetails(@PathVariable Integer id) {

		LOG.info("Searching lot having id : {}", id);
		 
		InspectionLotDto inspLot = inspectionlotService.getLotDetails(id); 		
		return new ResponseEntity<>(inspLot, HttpStatus.OK); 
	}

	@GetMapping("/lot/actu")
	public ResponseEntity<List<LotActualsAndCharacteristicsResponseDto>> getActualsAndCharacteristicsOfLot(@RequestParam Integer id) {

		LOG.info("Finding lot actuals and characteristics of lot id : {}", id);
		List<LotActualsAndCharacteristicsResponseDto> list = inspectionlotService.getActualAndOriginalOfLot(id);

		LOG.info("Returning list of lot actual and characteristics of lot id : {}", id);

		return new ResponseEntity<>(list, HttpStatus.OK);  
	}

	@PostMapping("/save/lot/actu")
	public ResponseEntity<ResponseDto> addInspectionActuals(@Valid @RequestBody LotActualDto actuals) {

		inspectionlotService.saveInspActuals(actuals);
		LOG.info("new Inspection actual saved for lot id : {}", actuals.getLotId());

		return ResponseEntity.status(HttpStatus.OK)
				.body(new ResponseDto("201", "Inspection actuals saved successfully"));
	}

	@PutMapping("/lot/edit")
	public ResponseEntity<ResponseDto> saveEditedLot(@Valid @RequestBody EditLotDto lot) {

		inspectionlotService.updateInspectionLot(lot);
		LOG.info("Lot details are updated successfully lot id : {}", lot.getId());

		return ResponseEntity.status(HttpStatus.OK)
				.body(new ResponseDto("200", "Inspection lot updated successfully")); 
	} 
	
	

	@PostMapping("/lot/search")   
	public ResponseEntity<List<DateRangeLotResponseDto>> DateRangeLotSearch(@Valid @RequestBody DateRangeLotSearch obj) {

		LOG.info("Searching lots ");
		List<DateRangeLotResponseDto> list = inspectionlotService.getAllLotsDetailsBetweenDateRange(obj);

		LOG.info("Returning lots having search criteria , size : {}", list.size());
		
			return ResponseEntity.status(HttpStatus.OK)
				.body(list); 	
		}
 
	/*
	 * finding inspection lots whose inspection actuals not yet added
	 */
	@GetMapping("/lots")
	public ResponseEntity<List<InspectionLotDto>> getAllLotsWhoseInspectionActualNeedToAdded() {
		
		List<InspectionLotDto> list = inspectionlotService.getAllInspectionLots(); 
		
		return ResponseEntity.status(HttpStatus.OK)
				.body(list); 
		}

	@PutMapping("/actu/edit") 
	public ResponseEntity<ResponseDto> editInspectionActuals(@Valid @RequestBody LotActualDto obj) {
		inspectionlotService.saveInspActuals(obj);
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ResponseDto("200", "Inspection actuals updated successfully")); 
	}

	
	@GetMapping("/lot/{id}/report/pdf")
    public ResponseEntity<byte[]> downloadInspectionReportPdf(@PathVariable Integer id) {
        LOG.info("Requesting PDF report for lot id: {}", id);
        byte[] pdfBytes = inspectionlotService.generateReportPdf(id);

        String fileName = "InspectionReport_" + id + ".pdf";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", encodedFileName);
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
	
	
}
