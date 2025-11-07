package com.hcl.mi.services;

import java.util.List;

import com.hcl.mi.entities.Vendor;
import com.hcl.mi.responsedtos.VendorDto;

public interface VendorService {
	
	void addNewVendor(VendorDto vendorDto);
	
	List<VendorDto> getAllVendor();

	VendorDto getVendor(Integer id);

	void deleteVendor(Integer id);
	
	List<Vendor> getAllActiveVendor();
	
	void updateVendor(VendorDto vendorDto);
}
