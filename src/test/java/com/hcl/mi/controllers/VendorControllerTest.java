package com.hcl.mi.controllers;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.hcl.mi.responsedtos.ResponseDto;
import com.hcl.mi.responsedtos.VendorDto;
import com.hcl.mi.services.VendorService;

class VendorControllerTest {

    @Mock
    private VendorService vendorService;

    @InjectMocks
    private VendorController vendorController;

    private VendorDto vendorDto; 

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        vendorDto = new VendorDto();
        vendorDto.setVendorId(1);
        vendorDto.setName("Test Vendor");
        vendorDto.setEmail("vendor@test.com");
        vendorDto.setPhoneNumber("9876543210");
        vendorDto.setStatus(true);
        vendorDto.setState("Telangana");
        vendorDto.setCity("Hyderabad");
    }

    @Test
    void testAddNewVendor() {
        doNothing().when(vendorService).addNewVendor(vendorDto);

        ResponseEntity<ResponseDto> response = vendorController.addNewVendor(vendorDto);

        assertEquals(CREATED, response.getStatusCode());
        assertEquals("201", response.getBody().getStatusCode());
        assertEquals("Vendor saved successfully", response.getBody().getStatusMsg());
        verify(vendorService, times(1)).addNewVendor(vendorDto);
    }

    @Test
    void testGetVendorDetails() {
        when(vendorService.getVendor(1)).thenReturn(vendorDto);

        ResponseEntity<VendorDto> response = vendorController.getVendorDetails(1);

        assertEquals(OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Vendor", response.getBody().getName());
        verify(vendorService, times(1)).getVendor(1);
    }

    @Test
    void testGetAllVendors() {
        List<VendorDto> vendors = Arrays.asList(vendorDto);
        when(vendorService.getAllVendor()).thenReturn(vendors);

        ResponseEntity<List<VendorDto>> response = vendorController.getAllVendors();

        assertEquals(OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(vendorService, times(1)).getAllVendor();
    }

    @Test
    void testEditVendor() {
        doNothing().when(vendorService).updateVendor(vendorDto);

        ResponseEntity<ResponseDto> response = vendorController.editVendor(vendorDto);

        assertEquals(OK, response.getStatusCode());
        assertEquals("200", response.getBody().getStatusCode());
        assertEquals("Vendor details updated Successfully", response.getBody().getStatusMsg());
        verify(vendorService, times(1)).updateVendor(vendorDto);
    }

    @Test
    void testDeleteVendor() {
        doNothing().when(vendorService).deleteVendor(1);

        ResponseEntity<ResponseDto> response = vendorController.deleteVendor(1);

        assertEquals(OK, response.getStatusCode());
        assertEquals("200", response.getBody().getStatusCode());
        assertEquals("vendor deleted successfully", response.getBody().getStatusMsg());
        verify(vendorService, times(1)).deleteVendor(1);
    }
}