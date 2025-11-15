package com.hcl.mi.servicesImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.hcl.mi.entities.Vendor;
import com.hcl.mi.exceptions.GenericNotFoundException;
import com.hcl.mi.repositories.VendorRepository;
import com.hcl.mi.responsedtos.VendorDto;
import com.hcl.mi.utils.StringUtil;

class VendorServiceImplTest {

    @Mock
    private VendorRepository vendorRepository;

    @InjectMocks
    private VendorServiceImpl vendorService;
 
    private Vendor vendor;
    private VendorDto vendorDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        vendor = new Vendor();
        vendor.setVendorId(101);
        vendor.setName("ABC VENDOR");
        vendor.setEmail("abc@vendor.com"); 
        vendor.setStatus(true);
        vendor.setCity("DELHI");
        vendor.setState("DELHI");
        vendor.setPhoneNumber("9876543210");

        vendorDto = new VendorDto();
        vendorDto.setVendorId(101);
        vendorDto.setName("  Abc Vendor  ");
        vendorDto.setEmail("  abc@vendor.com  ");
        vendorDto.setStatus(true);
        vendorDto.setCity("delhi");
        vendorDto.setState("delhi");
        vendorDto.setPhoneNumber("9876543210");
    }

    // ---------------- ADD NEW VENDOR ----------------
    @Test
    void testAddNewVendor_Success() {
        when(vendorRepository.existsByName("ABC VENDOR")).thenReturn(false);
        when(vendorRepository.existsByEmail("abc@vendor.com")).thenReturn(false);
        when(vendorRepository.save(any(Vendor.class))).thenReturn(vendor);

        assertDoesNotThrow(() -> vendorService.addNewVendor(vendorDto));

        verify(vendorRepository).existsByName("ABC VENDOR");
        verify(vendorRepository).existsByEmail("abc@vendor.com");
        verify(vendorRepository).save(any(Vendor.class));
    }

    @Test
    void testAddNewVendor_NameAlreadyExists() {
        when(vendorRepository.existsByName("ABC VENDOR")).thenReturn(true);

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> vendorService.addNewVendor(vendorDto));

        assertEquals("Vendor with name 'ABC VENDOR' already exists.", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    void testAddNewVendor_EmailAlreadyExists() {
        when(vendorRepository.existsByName("ABC VENDOR")).thenReturn(false);
        when(vendorRepository.existsByEmail("abc@vendor.com")).thenReturn(true);

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> vendorService.addNewVendor(vendorDto));

        assertEquals("Vendor with email 'abc@vendor.com' already exists.", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    void testGetAllVendor() {
        when(vendorRepository.findAll()).thenReturn(List.of(vendor));

        List<VendorDto> result = vendorService.getAllVendor();

        assertEquals(1, result.size());
        assertEquals("ABC VENDOR", result.get(0).getName());
        verify(vendorRepository).findAll();
    }

    @Test
    void testGetAllActiveVendor() {
        when(vendorRepository.findAllActiveVendors(true)).thenReturn(List.of(vendor));

        List<VendorDto> result = vendorService.getAllActiveVendor();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isStatus());
        verify(vendorRepository).findAllActiveVendors(true);
    }

    @Test
    void testGetVendor_Success() {
        when(vendorRepository.findById(101)).thenReturn(Optional.of(vendor));

        VendorDto result = vendorService.getVendor(101);

        assertEquals("ABC VENDOR", result.getName());
        assertEquals("abc@vendor.com", result.getEmail());
        verify(vendorRepository).findById(101);
    }

    @Test
    void testGetVendor_NotFound() {
        when(vendorRepository.findById(999)).thenReturn(Optional.empty());

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> vendorService.getVendor(999));

        assertEquals("Vendor with ID 999 does not exist.", ex.getMessage());
        verify(vendorRepository).findById(999);
    }

    @Test
    void testDeleteVendor_Success() {
        when(vendorRepository.findById(101)).thenReturn(Optional.of(vendor));

        vendorService.deleteVendor(101);

        assertFalse(vendor.isStatus());
        verify(vendorRepository).save(vendor);
    }

    @Test
    void testDeleteVendor_NotFound() {
        when(vendorRepository.findById(555)).thenReturn(Optional.empty());

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> vendorService.deleteVendor(555));

        assertEquals("Vendor with ID 555 does not exist.", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    void testUpdateVendor_Success() {
        when(vendorRepository.findById(101)).thenReturn(Optional.of(vendor));

        vendorDto.setName("Updated Vendor");
        vendorDto.setEmail("updated@vendor.com");
        vendorDto.setCity("mumbai");
        vendorDto.setState("maharashtra");

        vendorService.updateVendor(vendorDto);

        assertEquals("UPDATED VENDOR", vendor.getName());
        assertEquals("updated@vendor.com", vendor.getEmail());
        assertEquals("MUMBAI", vendor.getCity());
        assertEquals("MAHARASHTRA", vendor.getState());
        verify(vendorRepository).save(vendor);
    }

    @Test
    void testUpdateVendor_NotFound() {
        when(vendorRepository.findById(999)).thenReturn(Optional.empty());
        vendorDto.setVendorId(999);

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> vendorService.updateVendor(vendorDto));

        assertEquals("Vendor with ID 999 does not exist.", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }
}