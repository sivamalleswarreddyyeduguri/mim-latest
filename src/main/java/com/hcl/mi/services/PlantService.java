package com.hcl.mi.services;

import java.util.List;

import com.hcl.mi.responsedtos.PlantDto;

public interface PlantService {
	
	void addNewPlant(PlantDto plantDto);

	List<PlantDto> getAllPlants();

	PlantDto getPlant(String id);

	
	void saveEditedPlant(PlantDto plantDto);

	void deletePlant(String plantId);

}
 