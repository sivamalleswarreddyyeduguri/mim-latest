package com.hcl.mi.controllers;

import java.util.List;

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

import com.hcl.mi.entities.Vendor;
import com.hcl.mi.responsedtos.ResponseDto;
import com.hcl.mi.responsedtos.VendorDto;
import com.hcl.mi.services.VendorService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/vendor")
@Tag(name = "Vendor Controller", description = "responsible for adding vendors related data")
@Slf4j
public class VendorController {

	private final VendorService vendorService;

	public VendorController(VendorService vendorService) {
		this.vendorService = vendorService;
	}

	@PostMapping("/save") 
	public ResponseEntity<ResponseDto> addNewVendor(@Valid @RequestBody VendorDto vendorDto) {
	    vendorService.addNewVendor(vendorDto);
	    log.info("New vendor added successfully | {}", vendorDto.getName());
	    return ResponseEntity
	            .status(HttpStatus.CREATED)
	            .body(new ResponseDto("201", "Vendor saved successfully"));
	}
 
	@GetMapping("/{id}")
	public ResponseEntity<VendorDto> getVendorDetails(@PathVariable Integer id) {
		return  ResponseEntity
				.status(HttpStatus.OK)
				.body(vendorService.getVendor(id)); 
		
	}
 
	@GetMapping("/all")
	public ResponseEntity<List<VendorDto>> getAllVendors() {  
		
		return ResponseEntity.status(HttpStatus.OK).body(vendorService.getAllVendor());
	}
 
	@PutMapping("/edit") 
	public ResponseEntity<ResponseDto> editVendor(@Valid @RequestBody VendorDto vendorDto) {
		log.info("inside editVendor(): {}", vendorDto);
		vendorService.updateVendor(vendorDto); 
		return  ResponseEntity
				.status(HttpStatus.OK)
				.body(new ResponseDto("200", "Vendor details updated Successfully"));
	} 
     
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<ResponseDto> deleteVendor(@PathVariable Integer id) {
		
		System.out.println("inside deleteVendor():");

		vendorService.deleteVendor(id);
		return  ResponseEntity
				.status(HttpStatus.OK)
				.body(new ResponseDto("200", "vendor deleted successfully"));
	}

}
 