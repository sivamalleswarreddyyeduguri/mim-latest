package com.hcl.mi.mapper;

import org.springframework.stereotype.Component;

import com.hcl.mi.entities.Material;
import com.hcl.mi.responsedtos.MaterialDto;

@Component
public class MaterialMapper {

    public static MaterialDto convertEntityToDto(Material material) {
        MaterialDto dto = new MaterialDto();
        dto.setMaterialId(material.getMaterialId());
        dto.setMaterialDesc(material.getMaterialDesc());
        dto.setType(material.getType());
        dto.setStatus(material.isStatus());
        return dto;
    }

    public static Material convertDtoToEntity(MaterialDto dto) {
        Material material = new Material();
        material.setMaterialId(dto.getMaterialId());
        material.setMaterialDesc(dto.getMaterialDesc());
        material.setType(dto.getType());
        material.setStatus(dto.isStatus());
        return material;
    }
}