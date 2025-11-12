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
        dto.setStatus(plant.isStatus());
        dto.setCity(plant.getCity());
        dto.setState(plant.getState());  
        return dto;
    }

    public static Plant convertDtoToEntity(PlantDto plantDto) {

        Plant plant = new Plant();
        plant.setPlantId(plantDto.getPlantId());
        plant.setPlantName(plantDto.getPlantName());
        plant.setStatus(plantDto.isStatus());
        plant.setCity(plantDto.getCity());
        plant.setState(plantDto.getState());   
        return plant;
    }
}
