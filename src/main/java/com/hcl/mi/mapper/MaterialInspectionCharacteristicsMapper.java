package com.hcl.mi.mapper;

import org.springframework.stereotype.Component;

import com.hcl.mi.entities.MaterialInspectionCharacteristics;
import com.hcl.mi.responsedtos.MaterialInspectionCharacteristicsDto;

@Component
public class MaterialInspectionCharacteristicsMapper {

	public static MaterialInspectionCharacteristicsDto convertEntityToDto(MaterialInspectionCharacteristics entity) {
		MaterialInspectionCharacteristicsDto dto = new MaterialInspectionCharacteristicsDto();
		dto.setCharacteristicId(entity.getCharacteristicId());
		dto.setCharacteristicDescription(entity.getCharacteristicDescription());
		dto.setUpperToleranceLimit(entity.getUpperToleranceLimit());
		dto.setLowerToleranceLimit(entity.getLowerToleranceLimit());
		dto.setUnitOfMeasure(entity.getUnitOfMeasure());
		dto.setMatId(entity.getMaterial().getMaterialId()); 
		return dto;
	}

	public static MaterialInspectionCharacteristics convertDtoToEntity(MaterialInspectionCharacteristicsDto dto) {
		MaterialInspectionCharacteristics entity = new MaterialInspectionCharacteristics();
		entity.setCharacteristicId(dto.getCharacteristicId());
		entity.setCharacteristicDescription(dto.getCharacteristicDescription());
		entity.setUpperToleranceLimit(dto.getUpperToleranceLimit());
		entity.setLowerToleranceLimit(dto.getLowerToleranceLimit());
		entity.setUnitOfMeasure(dto.getUnitOfMeasure());
		return entity;
	}
}