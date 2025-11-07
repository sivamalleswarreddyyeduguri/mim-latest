package com.hcl.mi.servicesImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hcl.mi.entities.Plant;
import com.hcl.mi.exceptions.GenericNotFoundException;
import com.hcl.mi.exceptions.GenericAlreadyExistsException;
import com.hcl.mi.mapper.PlantMapper;
import com.hcl.mi.repositories.PlantRepository;
import com.hcl.mi.responsedtos.PlantDto;
import com.hcl.mi.services.PlantService;
import com.hcl.mi.utils.StringUtil;

@Service
public class PlantServceImpl implements PlantService {
	
	private final PlantRepository plantRepository;
	

	public PlantServceImpl(PlantRepository plantReposotory) {
		super();
		this.plantRepository = plantReposotory;
	}
	
	@Override 
	public void addNewPlant(PlantDto plantDto) {
	    Optional<Plant> optPlantId = plantRepository.findById(plantDto.getPlantId().toUpperCase());
	    Optional<Plant> optPlantName = plantRepository.findByPlantName(plantDto.getPlantName().toUpperCase());

	    if (optPlantId.isPresent() || optPlantName.isPresent()) {
	        throw new GenericAlreadyExistsException("Plant with given ID or Name already exists.");
	    }

	    plantDto.setPlantId(StringUtil.removeAllSpaces(plantDto.getPlantId()).toUpperCase());
	    plantDto.setLocation(StringUtil.removeExtraSpaces(plantDto.getLocation()).toUpperCase());
	    plantDto.setPlantName(StringUtil.removeExtraSpaces(plantDto.getPlantName()).toUpperCase());

	    plantRepository.save(PlantMapper.convertDtoToEntity(plantDto)); 
	}

	@Override
	public List<PlantDto> getAllPlants() {
		return plantRepository.findAll()
				.stream()
				.map(plant-> PlantMapper.convertEntityToDto(plant))
				.toList();
	} 
 
	
	@Override
	public PlantDto getPlant(String id) {
	    Optional<Plant> optPlant = plantRepository.findById(id.toUpperCase());
	    if (optPlant.isEmpty()) {
	        throw new GenericNotFoundException("Plant with ID '" + id + "' not found.");
	    }
	    return PlantMapper.convertEntityToDto(optPlant.get()); 
	}
	
	@Override
	public void saveEditedPlant(Plant plant) {
	    String plantId = StringUtil.removeExtraSpaces(plant.getPlantId()).toUpperCase();
	    plant.setPlantId(plantId);

	    Optional<Plant> existingPlant = plantRepository.findById(plantId);
	    if (existingPlant.isEmpty()) {
	        throw new GenericNotFoundException("Plant with ID " + plantId + " does not exist.");
	    }

	    plant.setLocation(StringUtil.removeExtraSpaces(plant.getLocation()).toUpperCase());
	    plant.setPlantName(StringUtil.removeExtraSpaces(plant.getPlantName()).toUpperCase());

	    plantRepository.save(plant);
	}

 

	@Override
	public void deletePlant(String plantId) {
		
	    PlantDto plantDto = getPlant(plantId);

	    if (plantDto == null) {
	        throw new GenericNotFoundException("Plant with ID " + plantId + " does not exist.");
	    }

	    Plant plant = PlantMapper.convertDtoToEntity(plantDto);
	    plant.setStatus(false);
	    plantRepository.save(plant);
	}

}
