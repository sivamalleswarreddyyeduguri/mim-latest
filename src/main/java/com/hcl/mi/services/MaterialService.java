package com.hcl.mi.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.hcl.mi.entities.InspectionLot;
import com.hcl.mi.entities.Material;
import com.hcl.mi.entities.MaterialInspectionCharacteristics;
import com.hcl.mi.requestdtos.MaterialCharDto;
import com.hcl.mi.responsedtos.MaterialDto;
import com.hcl.mi.responsedtos.MaterialInspectionCharacteristicsDto;


public interface MaterialService {

	List<Material> getAllMaterials();

	MaterialDto getMaterial(String id);

	void deleteMaterial(String id);

	void addNewMaterial(MaterialDto materialDto);

	void addNewMaterialCharacteristic(MaterialCharDto matChar);

	List<InspectionLot> getAllInspectionLots();
	
	List<MaterialInspectionCharacteristicsDto> getMaterialCharByLotId(Integer id);
//
//	boolean createInspectionLot(InspectionLot lot);
// 
//	List<Vendor> getAllVendors();
//
//	List<Plant> getAllPlants();
//
//	InspectionLot getInspectionLot(Integer id);
//
//	boolean saveInspActuals(InspectionActuals actuals);
 
	List<MaterialInspectionCharacteristicsDto> getAllCharacteristicsOfMaterial(String id);

	void saveEditMaterial(MaterialDto materialDto);

	List<MaterialDto> getAllActiveMaterials();
	
	boolean addListOfCharacteristicsForMaterial(MultipartFile file) throws Exception;

}
