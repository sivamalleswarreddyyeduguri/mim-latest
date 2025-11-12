package com.hcl.mi.servicesImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hcl.mi.entities.Vendor;
import com.hcl.mi.exceptions.GenericNotFoundException;
import com.hcl.mi.mapper.VendorMapper;
import com.hcl.mi.repositories.VendorRepository;
import com.hcl.mi.responsedtos.VendorDto;
import com.hcl.mi.services.VendorService;
import com.hcl.mi.utils.StringUtil;

@Service
public class VendorServiceImpl implements VendorService {

	private VendorRepository vendorRepository;

	public VendorServiceImpl(VendorRepository vendorRepository) {
		this.vendorRepository = vendorRepository;
	}

	
	@Override
	public void addNewVendor(VendorDto vendorDto) {
	    String name = StringUtil.removeExtraSpaces(vendorDto.getName()).toUpperCase();
	    String email = StringUtil.removeExtraSpaces(vendorDto.getEmail());

	    boolean nameExists = vendorRepository.existsByName(name);
	    boolean emailExists = vendorRepository.existsByEmail(email);

	    if (nameExists) {
	        throw new GenericNotFoundException("Vendor with name '" + name + "' already exists.");
	    }

	    if (emailExists) {
	        throw new GenericNotFoundException("Vendor with email '" + email + "' already exists.");
	    }

	    Vendor vendor = VendorMapper.convertDtoToEntity(vendorDto);
	    
	    vendor.setName(name);
	    vendor.setEmail(email);

	    vendorRepository.save(vendor);
	}

	@Override  
	public List<VendorDto> getAllVendor() {
		return vendorRepository.findAll()
				.stream()
				.map(vendor-> VendorMapper.convertEntityToDto(vendor))
				.toList(); 
		 
	}

	@Override
	public List<VendorDto> getAllActiveVendor() {
		return vendorRepository.findAllActiveVendors(true)
				.stream()
				.map(vendor-> VendorMapper.convertEntityToDto(vendor))
				.toList();
		
	}

	@Override
	public VendorDto getVendor(Integer id) {
	    Vendor vendor = vendorRepository.findById(id)
	            .orElseThrow(() -> new GenericNotFoundException("Vendor with ID " + id + " does not exist."));
	    
	    return VendorMapper.convertEntityToDto(vendor);
	}

	@Override
	public void deleteVendor(Integer id) {
	    Vendor vendor = vendorRepository.findById(id)
	            .orElseThrow(() -> new GenericNotFoundException("Vendor with ID " + id + " does not exist."));

	    vendor.setStatus(false);
	    vendorRepository.save(vendor);
	}
	
	@Override
	public void updateVendor(VendorDto vendorDto) {

	    Vendor existingVendor = vendorRepository.findById(vendorDto.getVendorId()) 
	            .orElseThrow(() -> new GenericNotFoundException("Vendor with ID " + vendorDto.getVendorId() + " does not exist."));

	    existingVendor.setName(StringUtil.removeExtraSpaces(vendorDto.getName()).toUpperCase());
	    existingVendor.setEmail(StringUtil.removeExtraSpaces(vendorDto.getEmail()));
	    existingVendor.setStatus(vendorDto.isStatus());
	    existingVendor.setPhoneNumber(vendorDto.getPhoneNumber());
	    existingVendor.setCity(vendorDto.getCity().toUpperCase());
	    existingVendor.setState(vendorDto.getState().toUpperCase()); 
	    

	   vendorRepository.save(existingVendor);
	    
	}

}
