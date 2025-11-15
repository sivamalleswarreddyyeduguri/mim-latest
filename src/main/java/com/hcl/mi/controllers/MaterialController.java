package com.hcl.mi.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hcl.mi.requestdtos.ErrorResponseDto;
import com.hcl.mi.requestdtos.MaterialCharDto;
import com.hcl.mi.requestdtos.MaterialCharUpdateDto;
import com.hcl.mi.responsedtos.MaterialDto;
import com.hcl.mi.responsedtos.MaterialInspectionCharacteristicsDto;
import com.hcl.mi.responsedtos.ResponseDto;
import com.hcl.mi.services.MaterialService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/material")
@Tag(name = "Material Controller", description = "responsible for adding material related data")
@OpenAPIDefinition(info = @Info(title = "APIs", version = "1.0", description = "Documentation APIs v2.0"))
@Slf4j
public class MaterialController {

	private MaterialService materialService;

	public MaterialController(MaterialService materialService) {

		this.materialService = materialService; 

	}  
 
	@Operation(summary = "Save Material") 
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Save Material", content = {@Content(schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@PostMapping("/save")
	public ResponseEntity<ResponseDto> addNewMaterial(@Valid @RequestBody MaterialDto materialDto) {

		log.info("new material saving {}", materialDto);
		
		materialService.addNewMaterial(materialDto);
			
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(new ResponseDto("201", "Material saved successfully")); 

	}

	@Operation(summary = "Get ALl Materials")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Get ALl Materials", content = {@Content(schema = @Schema(implementation = MaterialDto.class))}),
            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@GetMapping("/get-all") 
	public ResponseEntity<List<MaterialDto>> getAllMaterials() {   

		log.info("calling material service for all material list");

		List<MaterialDto> materialsList = materialService.getAllMaterials(); 

		log.info("returing all active material list to view");
		
		return ResponseEntity.status(HttpStatus.OK).body(materialsList);  
	}

	@Operation(summary = "Get Material by ID")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Get Material by ID", content = {@Content(schema = @Schema(implementation = MaterialDto.class))}),
            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@GetMapping("/{id}")
	public ResponseEntity<MaterialDto> getMaterial(@PathVariable String id) {

		MaterialDto material = materialService.getMaterial(id);
 
		return ResponseEntity.status(HttpStatus.OK).body(material);  
 
	} 

	@Operation(summary = "Update Material")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Update Material", content = {@Content(schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@PutMapping("/edit")
	public ResponseEntity<ResponseDto> editMaterialSave(@Valid @RequestBody MaterialDto materialDto) {

		log.info("material updation saving {}", materialDto);

		materialService.saveEditMaterial(materialDto);

		return ResponseEntity.status(HttpStatus.OK).body(new ResponseDto("200", "Material Updated successfully"));   

	}
	
	@Operation(summary = "Delete Material")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Delete Material", content = {@Content(schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@DeleteMapping("/delete/{materialId}")
	public ResponseEntity<ResponseDto> deleteVendor(@PathVariable String materialId) {

		 materialService.deleteMaterial(materialId);
		
		return ResponseEntity.status(HttpStatus.OK).body(new ResponseDto("200", "material deleted successfully"));   
		
		 
	}
	
	@Operation(summary = "View Material Characteristics")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "View Material Characteristicsl", content = {@Content(schema = @Schema(implementation = MaterialInspectionCharacteristicsDto.class))}),
            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@GetMapping("/view/char")
	public ResponseEntity<List<MaterialInspectionCharacteristicsDto>> viewCharacteristics(@RequestParam String materialId) {

		log.info("calling material service for material characteristics if material id : {}", materialId);
		List<MaterialInspectionCharacteristicsDto> list = materialService.getAllCharacteristicsOfMaterial(materialId);
				log.info("returning characteristics list of material id, {}", materialId);

		return ResponseEntity.status(HttpStatus.OK).body(list);      
	} 
 
	  
  
	@Operation(summary = "Save Material Characteristics")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Save Material Characteristicsl", content = {@Content(schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@PostMapping("/material-char/save")
	public ResponseEntity<ResponseDto> addMaterialCharacteristics(@Valid @RequestBody MaterialCharDto matChar) {

		log.info("new material characteristics adding for material id : {}", matChar.getMatId());

		materialService.addNewMaterialCharacteristic(matChar);

		return ResponseEntity.status(HttpStatus.OK).body(new ResponseDto("201", "material characteristics added successfully"));   
 
	}
	
	
	@Operation(summary = "Get Material Characteristics by ID")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Get Material Characteristics by ID", content = {@Content(schema = @Schema(implementation = MaterialInspectionCharacteristicsDto.class))}),
            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@GetMapping("/ch/{id}")
	public ResponseEntity<MaterialInspectionCharacteristicsDto> getCharacteristicsByChId(@PathVariable Integer id) {

		MaterialInspectionCharacteristicsDto material = materialService.getCharacteristicsByChId(id);
 
		return ResponseEntity.status(HttpStatus.OK).body(material);  
  
	}  
 
	@Operation(summary = "Get Material Characteristics by Lot ID")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Get Material Characteristics by Lot ID", content = {@Content(schema = @Schema(implementation = MaterialInspectionCharacteristicsDto.class))}),
            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@GetMapping("/rem/char/lot")
	public ResponseEntity<List<MaterialInspectionCharacteristicsDto>> getLotCurrentCharacteristicsOfAssociatedMaterial(@RequestParam Integer id) {
		
		List<MaterialInspectionCharacteristicsDto> characteristicsList = materialService.getMaterialCharByLotId(id);
		
		
		
		log.info("returing lot inspection characteristics needs to be added");

		return ResponseEntity.status(HttpStatus.OK).body(characteristicsList);    

	} 
	
	
	@Operation(summary = "Edit Material Characteristics")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Edit Material Characteristics", content = {@Content(schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@PutMapping("/material-char/edit")  
	public ResponseEntity<ResponseDto> updateCharcteristics(@Valid @RequestBody MaterialCharUpdateDto charDto) {
		log.info("inside editVendor(): {}", charDto);
		materialService.update(charDto); 
		return  ResponseEntity
				.status(HttpStatus.OK)
				.body(new ResponseDto("200", "characteristics details updated Successfully"));
	} 
	
	@PostMapping(value = "/char/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseDto> addMaterialCharacteristics(@RequestParam("file") MultipartFile file) throws Exception{
		 
		log.info("inside upload(): ");
	    materialService.addListOfCharacteristicsForMaterial(file);
		
		
	    return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDto("201", "Charactersitics Uploaded successfully"));     
	}
	 
	@DeleteMapping("/delete/char/{charId}")
	public ResponseEntity<ResponseDto> deleteMaterialCharacteristics(@PathVariable Integer charId) {
		
		materialService.deleteMaterialCharacteristics(charId);
		return  ResponseEntity
				.status(HttpStatus.OK)
				.body(new ResponseDto("200", "Characteristics deleted successfully"));
	}
	
	@GetMapping("/get-all-char") 
	public ResponseEntity<List<MaterialInspectionCharacteristicsDto>> getAllCharacteristics() {

		log.info("calling material service for material characteristics if material id : {}");
		List<MaterialInspectionCharacteristicsDto> list = materialService.getAllCharacteristics();

		return ResponseEntity.status(HttpStatus.OK).body(list);      
	} 
 
} 
