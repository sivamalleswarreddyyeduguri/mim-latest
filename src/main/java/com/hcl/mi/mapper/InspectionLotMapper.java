package com.hcl.mi.mapper;

import org.springframework.stereotype.Component;

import com.hcl.mi.entities.InspectionLot;
import com.hcl.mi.responsedtos.InspectionLotDto;

public class InspectionLotMapper {

    public static InspectionLotDto convertEntityToDto(InspectionLot lot) {
        InspectionLotDto dto = new InspectionLotDto();
        dto.setLotId(lot.getLotId());
        dto.setCreationDate(lot.getCreationDate());
        dto.setInspectionStartDate(lot.getInspectionStartDate());
        dto.setInspectionEndDate(lot.getInspectionEndDate());
        dto.setResult(lot.getResult());
        dto.setRemarks(lot.getRemarks());
        return dto;
    }

    public static InspectionLot convertDtoToEntity(InspectionLotDto dto) {
        InspectionLot lot = new InspectionLot();
        lot.setCreationDate(dto.getCreationDate());
        lot.setInspectionStartDate(dto.getInspectionStartDate());
        lot.setInspectionEndDate(dto.getInspectionEndDate());
        lot.setResult(dto.getResult());
        lot.setRemarks(dto.getRemarks());
        return lot;
    }
}