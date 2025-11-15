package com.hcl.mi.services;

import java.util.List;

import com.hcl.mi.entities.MaterialInspectionCharacteristics;
import com.hcl.mi.requestdtos.DateRangeLotSearch;
import com.hcl.mi.requestdtos.EditLotDto;
import com.hcl.mi.requestdtos.LotActualDto;
import com.hcl.mi.requestdtos.LotCreationDto;
import com.hcl.mi.responsedtos.DateRangeLotResponseDto;
import com.hcl.mi.responsedtos.InspectionLotDto;
import com.hcl.mi.responsedtos.LotActualsAndCharacteristicsResponseDto;
import com.hcl.mi.responsedtos.MaterialInspectionCharacteristicsDto;

public interface InspectionService {
	InspectionLotDto getLotDetails(Integer lot);
	
	List<LotActualsAndCharacteristicsResponseDto> getActualAndOriginalOfLot(Integer id);
	
	List<InspectionLotDto> getAllLotsWhoseInspectionActualNeedToAdded();
	
	void saveInspActuals(LotActualDto actuals); 
	
	List<DateRangeLotResponseDto> getAllLotsDetailsBetweenDateRange(DateRangeLotSearch obj);
	
	void updateInspectionLot(EditLotDto lot);
	
	void createInspectionLot(LotCreationDto lot); 
	
	  List<InspectionLotDto> getAllInspectionLots();

	byte[] generateReportPdf(Integer id);

	List<InspectionLotDto> getAllPendingInspectionLots();

	List<InspectionLotDto>  getAllRejectedInspectionLots(); 
	
	 List<MaterialInspectionCharacteristicsDto> getListOfMaterialInspectionCharNeedToaddForLot(Integer lotId);
		 
}
