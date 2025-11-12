package com.hcl.mi.services;

import java.util.List;

import com.hcl.mi.entities.InspectionLot;
import com.hcl.mi.requestdtos.DateRangeLotSearch;
import com.hcl.mi.requestdtos.EditLotDto;
import com.hcl.mi.requestdtos.LotActualDto;
import com.hcl.mi.requestdtos.LotCreationDto;
import com.hcl.mi.responsedtos.DateRangeLotResponseDto;
import com.hcl.mi.responsedtos.InspectionLotDto;
import com.hcl.mi.responsedtos.LotActualsAndCharacteristicsResponseDto;

public interface InspectionService {
	InspectionLotDto getLotDetails(Integer lot);
	
	List<LotActualsAndCharacteristicsResponseDto> getActualAndOriginalOfLot(Integer id);
	
	List<InspectionLotDto> getAllInspectionLots();
	
	void saveInspActuals(LotActualDto actuals); 
	
	List<DateRangeLotResponseDto> getAllLotsDetailsBetweenDateRange(DateRangeLotSearch obj);
	
	void updateInspectionLot(EditLotDto lot);
	
	void createInspectionLot(LotCreationDto lot); 

//	List<Vendor> getAllVendors();
//
//	List<PlantDto> getAllPlants();
//	
//	List<Material> getAllMaterials();
		
}
