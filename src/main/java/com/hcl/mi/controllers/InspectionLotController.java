package com.hcl.mi.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcl.mi.entities.InspectionLot;
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
@RequestMapping("/insp")
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
	public ResponseEntity<?> fetchInspectionLotDetails(@PathVariable Integer id) {

		LOG.info("Searching lot having id : {}", id);
		Map<String, Object> response = new HashMap<>();
		InspectionLotDto inspLot = inspectionlotService.getLotDetails(id); 		
		response.put(ApplicationConstants.MSG, ApplicationConstants.SUCCESS_MSG);
		response.put(ApplicationConstants.DATA, inspLot);
		return new ResponseEntity<>(response, HttpStatus.OK); 
	}

	@GetMapping("/lot/actu")
	public ResponseEntity<?> getActualsAndCharacteristicsOfLot(@RequestParam Integer id) {

		LOG.info("Finding lot actuals and characteristics of lot id : {}", id);
		Map<String, Object> response = new HashMap<>();
		List<LotActualsAndCharacteristicsResponseDto> list = inspectionlotService.getActualAndOriginalOfLot(id);

		LOG.info("Returning list of lot actual and characteristics of lot id : {}", id);

		response.put(ApplicationConstants.MSG, ApplicationConstants.SUCCESS_MSG);
		response.put(ApplicationConstants.DATA, list);
		return new ResponseEntity<>(response, HttpStatus.OK); 
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
	public ResponseEntity<?> DateRangeLotSearch(@Valid @RequestBody DateRangeLotSearch obj) {

		LOG.info("Searching lots ");
		Map<String, Object> response = new HashMap<>();
		List<DateRangeLotResponseDto> list = inspectionlotService.getAllLotsDetailsBetweenDateRange(obj);

		LOG.info("Returning lots having search criteria , size : {}", list.size());
		
		response.put(ApplicationConstants.MSG, ApplicationConstants.SUCCESS_MSG);
		response.put(ApplicationConstants.DATA, list);
		return ResponseEntity.status(HttpStatus.OK)
				.body(response); 	
		}

	/*
	 * finding inspection lots whose inspection actuals not yet added
	 */
	@GetMapping("/lots")
	public ResponseEntity<?> getAllLotsWhoseInspectionActualNeedToAdded() {
		Map<String, Object> response = new HashMap<>();
		
		List<InspectionLot> list = inspectionlotService.getAllInspectionLots(); 
		
		response.put(ApplicationConstants.MSG, ApplicationConstants.SUCCESS_MSG);
		response.put(ApplicationConstants.DATA, list);
		
		return ResponseEntity.status(HttpStatus.OK)
				.body(response); 
		}

	@PutMapping("/actu/edit") 
	public ResponseEntity<ResponseDto> editInspectionActuals(@Valid @RequestBody LotActualDto obj) {
		inspectionlotService.saveInspActuals(obj);
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ResponseDto("200", "Inspection actuals updated successfully")); 
	}

}
