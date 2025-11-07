package com.hcl.mi.servicesImpl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hcl.mi.entities.InspectionActuals;
import com.hcl.mi.entities.InspectionLot;
import com.hcl.mi.entities.Material;
import com.hcl.mi.entities.MaterialInspectionCharacteristics;
import com.hcl.mi.entities.Plant;
import com.hcl.mi.entities.User;
import com.hcl.mi.entities.Vendor;
import com.hcl.mi.exceptions.DuplicateCharacteristicException;
import com.hcl.mi.exceptions.GenericAlreadyExistsException;
import com.hcl.mi.exceptions.GenericNotFoundException;
import com.hcl.mi.helper.Transformers;
import com.hcl.mi.mapper.InspectionLotMapper;
import com.hcl.mi.mapper.MaterialMapper;
import com.hcl.mi.mapper.PlantMapper;
import com.hcl.mi.mapper.VendorMapper;
import com.hcl.mi.repositories.InspectionActualsRepository;
import com.hcl.mi.repositories.InspectionLotRepository;
import com.hcl.mi.repositories.MaterialRepository;
import com.hcl.mi.repositories.PlantRepository;
import com.hcl.mi.repositories.UserRepository;
import com.hcl.mi.repositories.VendorRepository;
import com.hcl.mi.requestdtos.DateRangeLotSearch;
import com.hcl.mi.requestdtos.EditLotDto;
import com.hcl.mi.requestdtos.LotActualDto;
import com.hcl.mi.requestdtos.LotCreationDto;
import com.hcl.mi.responsedtos.DateRangeLotResponseDto;
import com.hcl.mi.responsedtos.InspectionLotDto;
import com.hcl.mi.responsedtos.LotActualsAndCharacteristicsResponseDto;
import com.hcl.mi.responsedtos.MaterialDto;
import com.hcl.mi.responsedtos.PlantDto;
import com.hcl.mi.responsedtos.VendorDto;
import com.hcl.mi.services.InspectionService;
import com.hcl.mi.services.MaterialService;
import com.hcl.mi.services.PlantService;
import com.hcl.mi.services.VendorService;
import com.hcl.mi.utils.ApplicationConstants;
import com.hcl.mi.utils.StringUtil;

@Service
public class InspectionServiceImpl implements InspectionService {

	@Value("${date-range}")
	private long DATE_RANGE;

	private InspectionLotRepository inspectionLotRepo;

	private InspectionActualsRepository inspectionActRepo;

	private UserRepository userRepo;

//	private PlantService plantService;
//
//	private VendorService vendorService;
// 
//	private MaterialService materialService;
	
	private final MaterialRepository materialRepository;
	private final VendorRepository vendorRepository;
	private final PlantRepository plantRepository;
	

	private Logger LOG = LoggerFactory.getLogger(InspectionServiceImpl.class);

	public InspectionServiceImpl(InspectionLotRepository inspectionLotRepo,
			InspectionActualsRepository inspectionActRepo, UserRepository userRepo, VendorRepository vendorRepository,
			PlantRepository plantRepository, MaterialRepository materialRepository
			) {

		super();
		this.inspectionLotRepo = inspectionLotRepo;
		this.inspectionActRepo = inspectionActRepo;
		this.userRepo = userRepo;
		this.vendorRepository = vendorRepository; 
		this.plantRepository = plantRepository;
		this.materialRepository = materialRepository;
	}

	@Override 
	public InspectionLotDto getLotDetails(Integer id) {
		Optional<InspectionLot> optInsp = inspectionLotRepo.findById(id);

		if (optInsp.isPresent()) {

			LOG.info("Finding lot with id is success : {}", id);

			 InspectionLotDto lot = InspectionLotMapper.convertEntityToDto(optInsp.get());
			if (optInsp.get().getUser() != null) {
 				lot.setUserName(optInsp.get().getUser().getUsername()); 
			} 

			return lot;
		}
		throw new GenericNotFoundException("Lot not found with id: " + id); 
	} 
 
	@Override
	public List<LotActualsAndCharacteristicsResponseDto> getActualAndOriginalOfLot(Integer id) {

		Optional<InspectionLot> optLot = inspectionLotRepo.findById(id); 
		
		if(optLot.isEmpty()) {
		throw new GenericNotFoundException("Lot not found with id: " + id); 
		}
		 
		InspectionLot lot = optLot.get(); 

		Material material = lot.getMaterial();

		LOG.info("Getting lot charactesristics and actuals of lot id : {}", id);

		List<MaterialInspectionCharacteristics> characteristics = material.getMaterialChar();

		List<InspectionActuals> actuals = lot.getInspectionActuals();

		List<LotActualsAndCharacteristicsResponseDto> list = new LinkedList<>();

		for (int start = 0; start < characteristics.size(); start++) {

			LotActualsAndCharacteristicsResponseDto lotActOrg = LotActualsAndCharacteristicsResponseDto.builder()

					.lotId(id)

					.sNo(start + 1)

					.characteristicId(characteristics.get(start).getCharacteristicId())

					.characteristicDesc(characteristics.get(start).getCharacteristicDescription())

					.upperToleranceLimit(characteristics.get(start).getUpperToleranceLimit())

					.lowerToleranceLimit(characteristics.get(start).getLowerToleranceLimit())

					.unitOfMeasure(characteristics.get(start).getUnitOfMeasure()).build();

			list.add(lotActOrg);
		}

		LOG.info("Arrenging lot characteristics and actuals together of lot id : {}", id);

		for (int start = 0; start < list.size(); start++) {

			Integer charId = list.get(start).getCharacteristicId();

			for (int act = 0; act < actuals.size(); act++) {

				if (actuals.get(act).getMaterialInspectionCharacteristics().getCharacteristicId() == charId) {

					list.get(start).setActualUtl(actuals.get(act).getMaximumMeasurement());

					list.get(start).setActualLtl(actuals.get(act).getMinimumMeasurement());
				}
			}
		}

		LOG.info("returnig Lot Actuals and characteristics as a list");
		return list;
	}

	@Override
	public List<InspectionLot> getAllInspectionLots() {

		LOG.info("getting all lots");

		List<InspectionLot> lots = inspectionLotRepo.findAll();

		List<InspectionLot> responseList = new LinkedList<>();

		for (InspectionLot lot : lots) {

			if (lot.getMaterial().getMaterialChar().size() != lot.getInspectionActuals().size()) {

				LOG.info("adding lots those have not done all inspection actuals");

				responseList.add(lot);
			}
		}

		LOG.info("returing response list");
 
		return responseList;
	}

//	@Override
//	public boolean saveInspActuals(LotActualDto actualsDto) {
//
//		InspectionLot lot = getLotDetails(actualsDto.getLot());
//
//		List<MaterialInspectionCharacteristics> totalReqChar = lot.getMaterial().getMaterialChar();
//		List<InspectionActuals> actualChar = lot.getInspectionActuals();
//
//		InspectionActuals optActuals = inspectionActRepo
//				.findByInspectionLotAndmaterialInspectionCharacteristics(actualsDto.getLot(), actualsDto.getCharId());
//
//		if (optActuals == null) {
//
//			InspectionActuals actuals = InspectionActuals.builder().inspectionLot(lot)
//					.maximumMeasurement(actualsDto.getMaxMeas()).minimumMeasurement(actualsDto.getMinMeas()).build();
//
//			for (MaterialInspectionCharacteristics matChar : totalReqChar) {
//				if (matChar.getCharacteristicId() == actualsDto.getCharId()) {
//					actuals.setMaterialInspectionCharacteristics(matChar);
//					break;
//				}
//			}
//
//			lot.getInspectionActuals().add(actuals);
//		} else {
//			optActuals.setMaximumMeasurement(actualsDto.getMaxMeas());
//			optActuals.setMinimumMeasurement(actualsDto.getMinMeas());
//			lot.getInspectionActuals().add(optActuals);
//		}
//
//		inspectionLotRepo.save(lot);
//
//		LOG.info("new inpsection actuals saving for lot id : {}", actualsDto.getLot());
//
//		boolean result = false;
//
//		if (totalReqChar.size() == actualChar.size()) {
//
//			LOG.info("Evaluating lot for MARKING APPROVEL");
//
//			for (InspectionActuals actual : actualChar) {
//
//				Double actualUpperTolerance = actual.getMaximumMeasurement();
//
//				Double actualLowerTolerance = actual.getMinimumMeasurement();
//
//				for (int i = 0; i < totalReqChar.size(); i++) {
//
//					Double reqUpperTolerance = totalReqChar.get(i).getUpperToleranceLimit();
//
//					Double reqLowerTolerance = totalReqChar.get(i).getLowerToleranceLimit();
//
//					if (actual.getMaterialInspectionCharacteristics().getCharacteristicId() == totalReqChar.get(i)
//							.getCharacteristicId()) {
//
//						if (actualUpperTolerance > reqUpperTolerance || actualUpperTolerance < reqLowerTolerance) {
//
//							LOG.info(
//									"MARKING APPROVAL is rejected due to actuals did not meet charactestics of lot id : {}",
//									lot.getLotId());
//
//							result = true;
//						}
//						if (actualLowerTolerance < reqLowerTolerance || actualLowerTolerance > reqUpperTolerance) {
//
//							LOG.info(
//									"MARKING APPROVAL is rejected due to actuals did not meet charactestics of lot id : {}",
//									lot.getLotId());
//
//							result = true;
//						}
//					}
//				}
//			}
//			if (result == false) {
//
//				lot.setResult(ApplicationConstants.LOT_PASS_STATUS);
//
//				LOG.info("lot marked for approvel of id : {}", lot.getLotId());
//
//				inspectionLotRepo.save(lot);
//			} else {
//				lot.setResult(ApplicationConstants.LOT_INSPECTION_STATUS);
//				inspectionLotRepo.save(lot);
//			}
//		}
//
//		return true;
//	}
	
//	@Override
//	public void saveInspActuals(LotActualDto actualsDto) {
//
//	    Optional<InspectionLot> optLot = inspectionLotRepo.findById(actualsDto.getLotId());
//
//	    if (optLot.isEmpty()) {
//	        throw new GenericNotFoundException("Lot not found with id: " + actualsDto.getLotId());
//	    }
//
//	    InspectionLot lot = optLot.get();
//	    List<MaterialInspectionCharacteristics> totalReqChar = lot.getMaterial().getMaterialChar();
//
//	    InspectionActuals existingActual = inspectionActRepo
//	            .findByInspectionLotAndmaterialInspectionCharacteristics(actualsDto.getLotId(), actualsDto.getCharId());
//
//	    if (existingActual != null) {
//	        throw new GenericAlreadyExistsException("Characteristic already exists for this lot.");
//	    }
//
//	    // Create new actuals
//	    InspectionActuals actuals = InspectionActuals.builder()
//	            .inspectionLot(lot)
//	            .maximumMeasurement(actualsDto.getMaxMeas())
//	            .minimumMeasurement(actualsDto.getMinMeas())
//	            .build();
//
//	    for (MaterialInspectionCharacteristics matChar : totalReqChar) {
//	        if (matChar.getCharacteristicId() == actualsDto.getCharId()) {
//	            actuals.setMaterialInspectionCharacteristics(matChar);
//	            break;
//	        }
//	    }
//
//	    lot.getInspectionActuals().add(actuals);
//	    inspectionLotRepo.save(lot);
//
//	    LOG.info("New inspection actuals saved for lot ID: {}", actualsDto.getLotId());
//
//	    boolean hasFailures = false;
//	    List<String> failedCharacteristics = new ArrayList<>();
//
//	    if (totalReqChar.size() == lot.getInspectionActuals().size()) {
//
//	        LOG.info("Evaluating lot for MARKING APPROVAL");
//
//	        for (InspectionActuals actual : lot.getInspectionActuals()) {
//
//	            Double actualUpper = actual.getMaximumMeasurement();
//	            Double actualLower = actual.getMinimumMeasurement();
//
//	            MaterialInspectionCharacteristics reqChar = actual.getMaterialInspectionCharacteristics();
//	            Double reqUpper = reqChar.getUpperToleranceLimit();
//	            Double reqLower = reqChar.getLowerToleranceLimit();
//
//	            if (actualUpper > reqUpper || actualUpper < reqLower ||
//	                actualLower < reqLower || actualLower > reqUpper) {
//
//	                hasFailures = true;
//	                failedCharacteristics.add(reqChar.getCharacteristicName());
//
//	                LOG.info("MARKING APPROVAL rejected due to failed characteristic: {}", reqChar.getCharacteristicName());
//	            }
//	        }
//
//	        if (!hasFailures) {
//	            lot.setResult(ApplicationConstants.LOT_PASS_STATUS);
//	            lot.setRemarks("No remarks");
//	            lot.setInspectionEndDate(LocalDate.now());
//	            LOG.info("Lot marked for approval: {}", lot.getLotId());
//	        } else {
//	            String matrDesc = lot.getMaterial().getMaterialName(); // or getDescription()
//	            String failedChars = String.join(", ", failedCharacteristics);
//	            lot.setResult(ApplicationConstants.LOT_INSPECTION_STATUS);
//	            lot.setRemarks(matrDesc + " characteristics failed: " + failedChars);
//	        }
//
//	        inspectionLotRepo.save(lot);
//	    }
//	}
	
	
	@Override
	public void saveInspActuals(LotActualDto actualsDto) {

	    Optional<InspectionLot> optLot = inspectionLotRepo.findById(actualsDto.getLotId());

	    if (optLot.isEmpty()) {
	        throw new GenericNotFoundException("Lot not found with id: " + actualsDto.getLotId());
	    }

	    InspectionLot lot = optLot.get();
	    List<MaterialInspectionCharacteristics> totalReqChar = lot.getMaterial().getMaterialChar();

	    InspectionActuals existingActual = inspectionActRepo
	            .findByInspectionLotAndmaterialInspectionCharacteristics(actualsDto.getLotId(), actualsDto.getCharId());

	    if (existingActual != null) {
	        throw new GenericAlreadyExistsException("Characteristic already exists for this lot.");
	    }

	    // Create new actuals
	    InspectionActuals actuals = InspectionActuals.builder()
	            .inspectionLot(lot)
	            .maximumMeasurement(actualsDto.getMaxMeas())
	            .minimumMeasurement(actualsDto.getMinMeas())
	            .build();

	    for (MaterialInspectionCharacteristics matChar : totalReqChar) {
	        if (matChar.getCharacteristicId() == actualsDto.getCharId()) {
	            actuals.setMaterialInspectionCharacteristics(matChar);
	            break;
	        }
	    }

	    lot.getInspectionActuals().add(actuals);
	    inspectionLotRepo.save(lot);

	    LOG.info("New inspection actuals saved for lot ID: {}", actualsDto.getLotId());

	    boolean hasFailures = false;
	    List<String> failedCharacteristics = new ArrayList<>();

	    if (totalReqChar.size() == lot.getInspectionActuals().size()) {

	        LOG.info("Evaluating lot for MARKING APPROVAL");

	        for (InspectionActuals actual : lot.getInspectionActuals()) {

	            Double actualUpper = actual.getMaximumMeasurement();
	            Double actualLower = actual.getMinimumMeasurement();

	            MaterialInspectionCharacteristics reqChar = actual.getMaterialInspectionCharacteristics();
	            Double reqUpper = reqChar.getUpperToleranceLimit();
	            Double reqLower = reqChar.getLowerToleranceLimit();

	            if (actualUpper > reqUpper || actualUpper < reqLower ||
	                actualLower < reqLower || actualLower > reqUpper) {

	                hasFailures = true;
	                failedCharacteristics.add(reqChar.getCharacteristicDescription()); 

	                LOG.info("MARKING APPROVAL rejected due to failed characteristic: {}", reqChar.getCharacteristicDescription());
	            }
	        }

	        if (!hasFailures) { 
	            lot.setResult(ApplicationConstants.LOT_PASS_STATUS);
	            lot.setRemarks("No remarks");
	            lot.setInspectionEndDate(LocalDate.now());
	            LOG.info("Lot marked for approval: {}", lot.getLotId());
	        } else {
	            String matrDesc = lot.getMaterial().getMaterialDesc();
	            String failedChars = String.join(", ", failedCharacteristics);
	            lot.setResult(ApplicationConstants.LOT_INSPECTION_STATUS);
	            lot.setRemarks(matrDesc + " characteristics failed: " + failedChars);
	        }

	        inspectionLotRepo.save(lot);
	    }
	}

	@Override
	public List<DateRangeLotResponseDto> getAllLotsDetailsBetweenDateRange(DateRangeLotSearch obj) {

		boolean isValidDateRange = validateSearchDateRange(obj.getFromDate(), obj.getToDate());
		
		if(isValidDateRange == false) {
			throw new RuntimeException("Invalid date range for searching lots, Period should be : "+DATE_RANGE+" days range");
		}

		List<InspectionLot> inspList = inspectionLotRepo.findAllBycreationDateBetween(obj.getFromDate(),
				obj.getToDate());

		List<Predicate<InspectionLot>> searchCriteriaList = new LinkedList<>();

		if (obj.getMaterialId() != null) {

			LOG.info("filtering lots having material id as {}", obj.getMaterialId());
			searchCriteriaList.add(lot -> (lot.getMaterial().getMaterialId()
					.equals(StringUtil.removeAllSpaces(obj.getMaterialId()).toUpperCase())));
		}

		if (obj.getPlantId() != null) {

			LOG.info("filtering lots having plant id as {}", obj.getPlantId());
			searchCriteriaList.add(lot -> (lot.getPlant().getPlantId()
					.equals(StringUtil.removeExtraSpaces(obj.getPlantId()).toUpperCase())));
		}

		if (obj.getStatus() != null) {

			LOG.info("filtering lots having status as {}", obj.getStatus());
			searchCriteriaList.add(lot -> (lot.getResult().equals(obj.getStatus())));
		}

		if (obj.getVendorId() != 0) {

			LOG.info("filtering lots having vendor as {}", obj.getVendorId());
			searchCriteriaList.add(lot -> (lot.getVendor().getVendorId() == obj.getVendorId()));
		}

		Predicate<InspectionLot> searchCriteria = lot -> true;

		for (Predicate predicate : searchCriteriaList) {
			searchCriteria = searchCriteria.and(predicate);
		}

		List<InspectionLot> requiredList = inspList.stream().filter(searchCriteria).collect(Collectors.toList());

		List<DateRangeLotResponseDto> responseList = Transformers.ConvertInspectionLotListToDateRangeResponseDto(requiredList);

		LOG.info("Returing lots meets filter criteria of size : {}", responseList.size());
		return responseList;
	}

	private boolean validateSearchDateRange(LocalDate fromDate, LocalDate toDate) {
		long days = ChronoUnit.DAYS.between(fromDate, toDate);
		long requiredDays = DATE_RANGE;
		
		if (days > requiredDays) {
			return false;
		}
		
		return true;
	}

	@Override
	public void updateInspectionLot(EditLotDto lot) {

		Optional<InspectionLot> optInsp = inspectionLotRepo.findById(lot.getId());

		if (optInsp.isEmpty()) {
			throw new GenericNotFoundException("Lot not found with id: " + lot.getId());   
		}

		Optional<User> optUser = userRepo.findById(lot.getUserid());
		if (optUser.isEmpty()) {
			throw new GenericNotFoundException("Lot not found with id: " + lot.getUserid());    
		}
 
		InspectionLot originalLot = optInsp.get();

		originalLot.setInspectionEndDate(lot.getDate());
		originalLot.setResult(StringUtil.removeExtraSpaces(lot.getResult()));

		originalLot.setRemarks(StringUtil.removeExtraSpaces(lot.getRemarks()));
		originalLot.setUser(optUser.get());

		LOG.info("updating lot result is successfull of lot id : {}", lot.getId());
		inspectionLotRepo.save(originalLot);

	}

	@Override
	public void createInspectionLot(LotCreationDto lotDto) {
		
//		  if(!validateStDateAndCrDate(lotDto.getStDt(), lotDto.getCrDt())) {
//			  
//		  }

	    	    Material material = materialRepository.findById(StringUtil.removeAllSpaces(lotDto.getMatId()))
	        .orElseThrow(() -> new GenericNotFoundException("Material not found"));

	    Plant plant = plantRepository.findById(StringUtil.removeAllSpaces(lotDto.getPlantId()))
	        .orElseThrow(() -> new GenericNotFoundException("Plant not found"));

	    Vendor vendor = vendorRepository.findById(lotDto.getVendorId()) 
	        .orElseThrow(() -> new GenericNotFoundException("Vendor not found")); 

	    	    InspectionLot lot = InspectionLot.builder()
	        .result(ApplicationConstants.LOT_INSPECTION_STATUS)
	        .creationDate(lotDto.getCrDt())
	        .inspectionStartDate(lotDto.getStDt())
	        .material(material)
	        .plant(plant)
	        .vendor(vendor)
	        .build();

	  inspectionLotRepo.save(lot);
	}

	private boolean validateStDateAndCrDate(LocalDate stDt, LocalDate crDt) {
		if (stDt.isBefore(crDt)) {
			return false;
		}

		return true;
	}

//	@Override
//	public List<Vendor> getAllVendors() {
//		LOG.info("getting all vendors");
//
//		List<Vendor> vendorList = vendorService.getAllActiveVendor();
//
//		return vendorList;
//	}
//
//	@Override
//	public List<PlantDto> getAllPlants() {
//		LOG.info("getting all plants");
//
//		List<PlantDto> plantList = plantService.getAllPlants();
//
//		return plantList;
//	}
// 
//	@Override
//	public List<Material> getAllMaterials() {
//		LOG.info("finding all materials");
//
//		List<Material> materialList = materialService.getAllMaterials();
//
//		LOG.info("returing all materials list");
//
//		return materialList;
//	}

}
