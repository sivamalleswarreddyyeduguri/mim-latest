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

import com.hcl.mi.requestdtos.ErrorResponseDto;
import com.hcl.mi.responsedtos.PlantDto;
import com.hcl.mi.responsedtos.ResponseDto;
import com.hcl.mi.services.PlantService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/v1/plant")
@OpenAPIDefinition(info = @Info(title = "APIs", version = "1.0", description = "Documentation APIs v2.0"))
@Tag(name = "Plant Controller", description = "responsible for adding plant related data")
public class PlantController {
	 
	private final PlantService plantService;

	private Logger LOG = LoggerFactory.getLogger(PlantController.class);

	public PlantController(PlantService plantService) {
		super(); 
		this.plantService = plantService;
	}
	   
	 @Operation(summary = "Add Plant")
	    @ApiResponses({@ApiResponse(responseCode = "200", description = "Add Plant", content = {@Content(schema = @Schema(implementation = ResponseDto.class))}),
	            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
	            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
	            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@PostMapping("/save")
	public ResponseEntity<ResponseDto> addNewPlant(@Valid @RequestBody PlantDto plantDto) {
		    plantService.addNewPlant(plantDto);
			LOG.info("new plant added successfully | {}", plantDto);
			return ResponseEntity
					.status(HttpStatus.CREATED)
					.body(new ResponseDto("201", "Plant saved successfully")); 
		
	  }
 
	  
	 @Operation(summary = "Get Plant by id")
	    @ApiResponses({@ApiResponse(responseCode = "200", description = "Get Plant by id", content = {@Content(schema = @Schema(implementation = PlantDto.class))}),
	            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
	            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
	            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@GetMapping("/{plantId}")
	public ResponseEntity<PlantDto> getPlantDetails(@PathVariable String plantId) {			
			return  ResponseEntity
					.status(HttpStatus.OK)
					.body(plantService.getPlant(plantId)); 
		
	}  

	 @Operation(summary = "Get All Plants")
	    @ApiResponses({@ApiResponse(responseCode = "200", description = "Get All Plants", content = {@Content(schema = @Schema(implementation = PlantDto.class))}),
	            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
	            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
	            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@GetMapping("/get-all")
	public ResponseEntity<List<PlantDto>> getAllPlant() {
		return  ResponseEntity
				.status(HttpStatus.OK)
				.body(plantService.getAllPlants()); 
	}
 
	 @Operation(summary = "Edit Plants")
	    @ApiResponses({@ApiResponse(responseCode = "200", description = "Edit Plant", content = {@Content(schema = @Schema(implementation = PlantDto.class))}),
	            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
	            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
	            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@PutMapping("/edit")
	public ResponseEntity<ResponseDto> updatePlantDetails(@Valid @RequestBody PlantDto plantDto) {
		plantService.saveEditedPlant(plantDto);
		return  ResponseEntity
				.status(HttpStatus.OK)
				.body(new ResponseDto("200", "plant details are updated"));
	} 
  
	 @Operation(summary = "delete plant by id")
	    @ApiResponses({@ApiResponse(responseCode = "200", description = "Delete Plant by id", content = {@Content(schema = @Schema(implementation = PlantDto.class))}),
	            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
	            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
	            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@DeleteMapping("/delete/{plantId}")
	public ResponseEntity<ResponseDto> deletePlant(@PathVariable String plantId) {
		plantService.deletePlant(plantId);		
		return  ResponseEntity
				.status(HttpStatus.OK)
				.body(new ResponseDto("200", "plant deleted successfully"));
	}
 
}
