package com.hcl.mi.mapper;

import org.springframework.stereotype.Component;

import com.hcl.mi.entities.Vendor;
import com.hcl.mi.responsedtos.VendorDto;

@Component
public class VendorMapper {

    public static VendorDto convertEntityToDto(Vendor vendor) {
        VendorDto dto = new VendorDto();
        dto.setVendorId(vendor.getVendorId());
        dto.setName(vendor.getName());
        dto.setEmail(vendor.getEmail());
        dto.setStatus(vendor.isStatus());
        return dto;
    }

    public static Vendor convertDtoToEntity(VendorDto dto) {
        Vendor vendor = new Vendor();
        vendor.setName(dto.getName());
        vendor.setEmail(dto.getEmail());
        vendor.setStatus(dto.isStatus());
        return vendor;
    }
}