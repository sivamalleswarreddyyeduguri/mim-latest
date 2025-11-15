package com.hcl.mi.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.hcl.mi.entities.InspectionLot;

import com.hcl.mi.requestdtos.MaterialCharDto;
import com.hcl.mi.requestdtos.MaterialCharUpdateDto;
import com.hcl.mi.responsedtos.MaterialDto;
import com.hcl.mi.responsedtos.MaterialInspectionCharacteristicsDto;



public interface MaterialService {

	List<MaterialDto> getAllMaterials();

	MaterialDto getMaterial(String id);

	void deleteMaterial(String id);

	void addNewMaterial(MaterialDto materialDto);

	void addNewMaterialCharacteristic(MaterialCharDto matChar);

	List<InspectionLot> getAllInspectionLots();
	
	List<MaterialInspectionCharacteristicsDto> getMaterialCharByLotId(Integer id);
 
	List<MaterialInspectionCharacteristicsDto> getAllCharacteristicsOfMaterial(String id);

	void saveEditMaterial(MaterialDto materialDto);

	List<MaterialDto> getAllActiveMaterials();
	
	boolean addListOfCharacteristicsForMaterial(MultipartFile file) throws Exception;

	MaterialInspectionCharacteristicsDto getCharacteristicsByChId(Integer id);

	void update(MaterialCharUpdateDto charDto);

	void deleteMaterialCharacteristics(Integer id);

	List<MaterialInspectionCharacteristicsDto> getAllCharacteristics();    

}
