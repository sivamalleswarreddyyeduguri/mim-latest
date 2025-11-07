package com.hcl.mi.mapper;

import org.springframework.stereotype.Component;

import com.hcl.mi.entities.Plant;
import com.hcl.mi.responsedtos.PlantDto;

@Component
public class PlantMapper {

    public static PlantDto convertEntityToDto(Plant plant) {

        PlantDto dto = new PlantDto();
        
        dto.setPlantId(plant.getPlantId());
        dto.setPlantName(plant.getPlantName());
        dto.setLocation(plant.getLocation());
        dto.setStatus(plant.isStatus());
        return dto;
    }

    public static Plant convertDtoToEntity(PlantDto plantDto) {

        Plant plant = new Plant();
        plant.setPlantId(plantDto.getPlantId());
        plant.setPlantName(plantDto.getPlantName());
        plant.setLocation(plantDto.getLocation());
        plant.setStatus(plantDto.isStatus());
        return plant;
    }
}
