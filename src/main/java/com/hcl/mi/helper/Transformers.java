package com.hcl.mi.helper;

import java.util.LinkedList;
import java.util.List;

import com.hcl.mi.entities.InspectionLot;
import com.hcl.mi.entities.Material;
import com.hcl.mi.entities.MaterialInspectionCharacteristics;
import com.hcl.mi.requestdtos.MaterialCharDto;
import com.hcl.mi.responsedtos.DateRangeLotResponseDto;
import com.hcl.mi.utils.StringUtil;

public class Transformers {
	
	public static List<DateRangeLotResponseDto> ConvertInspectionLotListToDateRangeResponseDto(List<InspectionLot> list) {
		List<DateRangeLotResponseDto> responseList = new LinkedList<>();
		
		for (InspectionLot lot : list) {
 
			DateRangeLotResponseDto respLot = DateRangeLotResponseDto.builder()
					.createdOn(lot.getCreationDate())
					.endOn(lot.getInspectionEndDate())
					.startOn(lot.getInspectionStartDate())
					.result(lot.getResult())
					.inspectedBy(lot.getCreatedBy())
					.vendor(lot.getVendor().getName())
					.plant(lot.getPlant().getPlantName())
					.material(lot.getMaterial()
					.getMaterialDesc())
					.lotId(lot.getLotId())
					.build();

			responseList.add(respLot);
		}
		 
		return responseList;
		 
	}
	 
	public static MaterialInspectionCharacteristics convertMaterialCharDtoToMaterialInspectionCharObj(MaterialCharDto matChar, Material material) {
		MaterialInspectionCharacteristics matCharObj = new MaterialInspectionCharacteristics();

		matCharObj.setCharacteristicDescription(StringUtil.removeExtraSpaces(matChar.getCharDesc()).toUpperCase());
		matCharObj.setUnitOfMeasure(StringUtil.removeAllSpaces(matChar.getUom()));
		matCharObj.setLowerToleranceLimit(matChar.getLtl());
		matCharObj.setUpperToleranceLimit(matChar.getUtl());
		matCharObj.setMaterial(material);
		
		return matCharObj;
	}

}
