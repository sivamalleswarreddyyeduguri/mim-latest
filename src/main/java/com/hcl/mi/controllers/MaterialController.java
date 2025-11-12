package com.hcl.mi.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.hcl.mi.entities.Material;
import com.hcl.mi.entities.MaterialInspectionCharacteristics;
import com.hcl.mi.requestdtos.MaterialCharDto;
import com.hcl.mi.requestdtos.MaterialCharUpdateDto;
import com.hcl.mi.responsedtos.MaterialDto;
import com.hcl.mi.responsedtos.MaterialInspectionCharacteristicsDto;
import com.hcl.mi.responsedtos.ResponseDto;
import com.hcl.mi.responsedtos.VendorDto;
import com.hcl.mi.services.MaterialService;
import com.hcl.mi.utils.ApplicationConstants;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/material")
@Tag(name = "Material Controller", description = "responsible for adding material related data")
@Slf4j
public class MaterialController {

	private MaterialService materialService;

	private Logger LOG = LoggerFactory.getLogger(MaterialController.class);

	public MaterialController(MaterialService materialService) {

		this.materialService = materialService; 

	}
 
	@PostMapping("/save")
	public ResponseEntity<ResponseDto> addNewMaterial(@Valid @RequestBody MaterialDto materialDto) {

		LOG.info("new material saving {}", materialDto);
		
		materialService.addNewMaterial(materialDto);
			
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(new ResponseDto("201", "Material saved successfully")); 

	}

	@GetMapping("/get-all") 
	public ResponseEntity<List<MaterialDto>> getAllMaterials() {   

		LOG.info("calling material service for all material list");

		List<MaterialDto> materialsList = materialService.getAllMaterials(); 

		LOG.info("returing all active material list to view");
		
		return ResponseEntity.status(HttpStatus.OK).body(materialsList);  
	}

	@GetMapping("/{id}")
	public ResponseEntity<MaterialDto> getMaterial(@PathVariable String id) {

		MaterialDto material = materialService.getMaterial(id);
 
		return ResponseEntity.status(HttpStatus.OK).body(material);  
 
	} 

	@PutMapping("/edit")
	public ResponseEntity<ResponseDto> editMaterialSave(@Valid @RequestBody MaterialDto materialDto) {

		LOG.info("material updation saving {}", materialDto);

		materialService.saveEditMaterial(materialDto);

		return ResponseEntity.status(HttpStatus.OK).body(new ResponseDto("200", "Material Updated successfully"));   

	}
	
	@DeleteMapping("/delete/{materialId}")
	public ResponseEntity<ResponseDto> deleteVendor(@PathVariable String materialId) {

		 materialService.deleteMaterial(materialId);
		
		return ResponseEntity.status(HttpStatus.OK).body(new ResponseDto("200", "material deleted successfully"));   
		
		 
	}
	
/* 
 * material characteristics section
 * 
 */
	@GetMapping("/view/char")
	public ResponseEntity<List<MaterialInspectionCharacteristicsDto>> viewCharacteristics(@RequestParam String materialId) {

		LOG.info("calling material service for material characteristics if material id : {}", materialId);
		List<MaterialInspectionCharacteristicsDto> list = materialService.getAllCharacteristicsOfMaterial(materialId);
				LOG.info("returning characteristics list of material id, {}", materialId);

		return ResponseEntity.status(HttpStatus.OK).body(list);      
	} 
 
	  
  
	@PostMapping("/material-char/save")
	public ResponseEntity<ResponseDto> addMaterialCharacteristics(@Valid @RequestBody MaterialCharDto matChar) {

		LOG.info("new material characteristics adding for material id : {}", matChar.getMatId());

		materialService.addNewMaterialCharacteristic(matChar);

		return ResponseEntity.status(HttpStatus.OK).body(new ResponseDto("201", "material characteristics added successfully"));   
 
	}
	
	
	@GetMapping("/ch/{id}")
	public ResponseEntity<MaterialInspectionCharacteristicsDto> getCharacteristicsByChId(@PathVariable Integer id) {

		MaterialInspectionCharacteristicsDto material = materialService.getCharacteristicsByChId(id);
 
		return ResponseEntity.status(HttpStatus.OK).body(material);  
  
	}  
 
	@GetMapping("/rem/char/lot")
	public ResponseEntity<?> getLotCurrentCharacteristicsOfAssociatedMaterial(@RequestParam Integer id) {
		
		List<MaterialInspectionCharacteristicsDto> characteristicsList = materialService.getMaterialCharByLotId(id);
		
		Map<String, Object> response = new HashMap<>(); 
		response.put(ApplicationConstants.MSG, ApplicationConstants.SUCCESS_MSG);
		if(characteristicsList.size() == 0) {
			response.put("info", "all characteristics are inspected");
		}else {
			response.put(ApplicationConstants.DATA, characteristicsList);
		}
		
		LOG.info("returing lot inspection characteristics needs to be added");

		return ResponseEntity.status(HttpStatus.OK).body(response);    

	} 
	
	
	@PutMapping("/material-char/edit")  
	public ResponseEntity<ResponseDto> updateCharcteristics(@Valid @RequestBody MaterialCharUpdateDto charDto) {
		log.info("inside editVendor(): {}", charDto);
		materialService.update(charDto); 
		return  ResponseEntity
				.status(HttpStatus.OK)
				.body(new ResponseDto("200", "characteristics details updated Successfully"));
	} 
	
	
	@PostMapping(value = "/char/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> addMaterialCharacteristics(@RequestParam("file") MultipartFile file) throws Exception{
		
		Map<String, Object> response = new HashMap<>();
		
		boolean isCharacteristicsAdded = materialService.addListOfCharacteristicsForMaterial(file);
		
		if(isCharacteristicsAdded) {
			response.put(ApplicationConstants.MSG, ApplicationConstants.SUCCESS_MSG);
		}else {
			response.put(ApplicationConstants.MSG, ApplicationConstants.FAIL_MSG);
		}
		
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

}
