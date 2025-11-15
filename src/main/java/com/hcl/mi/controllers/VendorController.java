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

import com.hcl.mi.requestdtos.ErrorResponseDto;
import com.hcl.mi.responsedtos.ResponseDto;
import com.hcl.mi.responsedtos.VendorDto;
import com.hcl.mi.services.VendorService;

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
@RequestMapping("/api/v1/vendor")
@Tag(name = "Vendor Controller", description = "responsible for adding vendors related data")
@OpenAPIDefinition(info = @Info(title = "APIs", version = "1.0", description = "Documentation APIs v2.0"))
@Slf4j
public class VendorController {

	private final VendorService vendorService;

	public VendorController(VendorService vendorService) {
		this.vendorService = vendorService;
	}
  
	@Operation(summary = "Add Vendor")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Add Vendor", content = {@Content(schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@PostMapping("/save") 
	public ResponseEntity<ResponseDto> addNewVendor(@Valid @RequestBody VendorDto vendorDto) {
	    vendorService.addNewVendor(vendorDto);
	    log.info("New vendor added successfully | {}", vendorDto.getName());
	    return ResponseEntity
	            .status(HttpStatus.CREATED)
	            .body(new ResponseDto("201", "Vendor saved successfully"));
	}
 
	@Operation(summary = "Get Vendor by id")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Add Plant", content = {@Content(schema = @Schema(implementation = VendorDto.class))}),
            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@GetMapping("/{id}")
	public ResponseEntity<VendorDto> getVendorDetails(@PathVariable Integer id) {
		return  ResponseEntity
				.status(HttpStatus.OK)
				.body(vendorService.getVendor(id)); 
		
	}
 
	@Operation(summary = "Get All Vendors")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Add Plant", content = {@Content(schema = @Schema(implementation = VendorDto.class))}),
            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@GetMapping("/all")
	public ResponseEntity<List<VendorDto>> getAllVendors() {  
		
		return ResponseEntity.status(HttpStatus.OK).body(vendorService.getAllVendor());
	}
 
	@Operation(summary = "Edit Vendor")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Add Plant", content = {@Content(schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@PutMapping("/edit") 
	public ResponseEntity<ResponseDto> editVendor(@Valid @RequestBody VendorDto vendorDto) {
		log.info("inside editVendor(): {}", vendorDto);
		vendorService.updateVendor(vendorDto); 
		return  ResponseEntity
				.status(HttpStatus.OK)
				.body(new ResponseDto("200", "Vendor details updated Successfully"));
	} 
     
	@Operation(summary = "Delete Vendor by id")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Add Plant", content = {@Content(schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "400", description = "BAD Request", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Data not Found", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Server not responded", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})})
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<ResponseDto> deleteVendor(@PathVariable Integer id) {
		
		log.info("inside deleteVendor():");

		vendorService.deleteVendor(id);
		return  ResponseEntity
				.status(HttpStatus.OK)
				.body(new ResponseDto("200", "vendor deleted successfully"));
	}
 
}
 