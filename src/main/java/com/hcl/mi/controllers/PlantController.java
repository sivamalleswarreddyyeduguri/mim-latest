package com.hcl.mi.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcl.mi.entities.Plant;
import com.hcl.mi.responsedtos.PlantDto;
import com.hcl.mi.responsedtos.ResponseDto;
import com.hcl.mi.services.PlantService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/v1/plant")
@Tag(name = "Plant Controller", description = "responsible for adding plant related data")
public class PlantController {
	
	private final PlantService plantService;

	private Logger LOG = LoggerFactory.getLogger(PlantController.class);

	public PlantController(PlantService plantService) {
		super(); 
		this.plantService = plantService;
	}
	 
	@PostMapping("/save")
	public ResponseEntity<ResponseDto> addNewPlant(@Valid @RequestBody PlantDto plantDto) {
		    plantService.addNewPlant(plantDto);
			LOG.info("new plant added successfully | {}", plantDto);
			return ResponseEntity
					.status(HttpStatus.CREATED)
					.body(new ResponseDto("201", "Plant saved successfully")); 
		
	  }
 
	  
	@GetMapping("/{plantId}")
	public ResponseEntity<PlantDto> getPlantDetails(@PathVariable String plantId) {			
			return  ResponseEntity
					.status(HttpStatus.OK)
					.body(plantService.getPlant(plantId)); 
		
	}  

	@GetMapping("/get-all")
	public ResponseEntity<List<PlantDto>> getAllPlant() {
		return  ResponseEntity
				.status(HttpStatus.OK)
				.body(plantService.getAllPlants()); 
	}
 
	@PutMapping("/edit")
	public ResponseEntity<ResponseDto> updatePlantDetails(@Valid @RequestBody Plant plant) {
		plantService.saveEditedPlant(plant);
		return  ResponseEntity
				.status(HttpStatus.OK)
				.body(new ResponseDto("200", "plant details are updated"));
	} 
  
	@DeleteMapping("/delete/{plantId}")
	public ResponseEntity<ResponseDto> deletePlant(@PathVariable String plantId) {
		plantService.deletePlant(plantId);		
		return  ResponseEntity
				.status(HttpStatus.OK)
				.body(new ResponseDto("200", "plant deleted successfully"));
	}
 
}
