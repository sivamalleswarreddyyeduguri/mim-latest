package com.hcl.mi.servicesImpl;

 

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.hcl.mi.entities.InspectionLot;
import com.hcl.mi.entities.Material;
import com.hcl.mi.entities.MaterialInspectionCharacteristics;
import com.hcl.mi.entities.Vendor;
import com.hcl.mi.exceptions.DuplicateCharacteristicException;
import com.hcl.mi.exceptions.GenericAlreadyExistsException;
import com.hcl.mi.exceptions.GenericNotFoundException;
import com.hcl.mi.helper.Transformers;
import com.hcl.mi.mapper.MaterialInspectionCharacteristicsMapper;
import com.hcl.mi.mapper.MaterialMapper;
import com.hcl.mi.repositories.InspectionLotRepository;
import com.hcl.mi.repositories.MaterialCharRepository;
import com.hcl.mi.repositories.MaterialRepository;
import com.hcl.mi.requestdtos.MaterialCharDto;
import com.hcl.mi.requestdtos.MaterialCharUpdateDto;
import com.hcl.mi.responsedtos.MaterialDto;
import com.hcl.mi.responsedtos.MaterialInspectionCharacteristicsDto;
import com.hcl.mi.services.MaterialService;
import com.hcl.mi.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;
 
@Service
@Slf4j
public class MaterialServiceIImpl implements MaterialService {

	private MaterialRepository materialRepository;

	private MaterialCharRepository materialCharReposotory;

	private InspectionLotRepository inspectionLotRepo; 

 
	public MaterialServiceIImpl(MaterialRepository materialRepository, MaterialCharRepository materialCharReposotory,
			InspectionLotRepository inspectionLotRepo) {
		super();
		this.materialRepository = materialRepository;

		this.materialCharReposotory = materialCharReposotory;

		this.inspectionLotRepo = inspectionLotRepo;
 
	} 

	@Override
	public List<MaterialDto> getAllMaterials() {

		log.info("finding all materials");

		List<Material> materialList = materialRepository.findAll();

		log.info("returing all materials list");

		return materialList.stream()
				.map(mat-> MaterialMapper.convertEntityToDto(mat))
				.toList();
			
	}
 
	@Override
	public MaterialDto getMaterial(String id) {

		log.info("finding material with id : {}", id);

		Optional<Material> optMaterial = materialRepository.findById(id.toUpperCase());

		if (optMaterial.isEmpty()) {

			log.info("no material associated with id : {}", id);

			throw new GenericNotFoundException("Material not found with id: " + id);
		} 

		return  MaterialMapper.convertEntityToDto(optMaterial.get());  
	}

	@Override
	public void deleteMaterial(String id) {

		log.info("finding material with id : {}", id);

		Optional<Material> optMaterial = materialRepository.findById(id.toUpperCase()); 

		if (optMaterial.isEmpty()) {

			log.info("no material associated with id : {}", id);
			
			throw new GenericNotFoundException("Material not found with id: " + id);

		}

		Material material = optMaterial.get();

		log.info("setting material status to INACTIVE");

		material.setStatus(false);

		log.info("saving material of id : {}", id);

		materialRepository.save(material);

		log.info("returning true");

	}

	@Override
	public void addNewMaterial(MaterialDto materialDto) {
		
	    String materialId = StringUtil.removeAllSpaces(materialDto.getMaterialId()).toUpperCase();
	    String materialDesc = StringUtil.removeExtraSpaces(materialDto.getMaterialDesc()).toUpperCase();
	    String type = StringUtil.removeExtraSpaces(materialDto.getType()).toUpperCase();

	    Optional<Material> optMaterial = materialRepository.findById(materialId);
	    Optional<Material> optMaterialDesc = materialRepository.findByMaterialDesc(materialDesc);

	    if (optMaterial.isPresent() || optMaterialDesc.isPresent()) {
	        throw new GenericAlreadyExistsException("Material with the same ID or description already exists.");
	    }
 
	    Material material = MaterialMapper.convertDtoToEntity(materialDto);
	    material.setMaterialId(materialId);
	    material.setMaterialDesc(materialDesc);
	    material.setType(type);

	    materialRepository.save(material);

	    log.info("New material saved with ID: {}", materialId);
	}

	@Override
	public void addNewMaterialCharacteristic(MaterialCharDto matChar) {

		Material material = isCharacteristicConditionSatisfy(matChar);
  
		MaterialInspectionCharacteristics matCharObj = Transformers
				.convertMaterialCharDtoToMaterialInspectionCharObj(matChar, material);

		log.info("new Material characteristic adding {}", matChar);

		 materialCharReposotory.save(matCharObj);

	}
 
	
	public Material isCharacteristicConditionSatisfy(MaterialCharDto matChar) {
		
	    MaterialDto materialDto;
	    
	    try {
	        materialDto = getMaterial(matChar.getMatId());
	    } catch (GenericNotFoundException e) {
	        log.error("Material not found in isCharacteristicConditionSatisfy for ID: {}", matChar.getMatId());
	        throw e;
	    }
 
	    Material material = MaterialMapper.convertDtoToEntity(materialDto);
	   log.info(material + "-".repeat(100)); 

	    for (MaterialInspectionCharacteristicsDto matCharItem : getAllCharacteristicsOfMaterial(matChar.getMatId())) {
	        String existingCharDesc = matCharItem.getCharacteristicDescription(); 
	        String newCharDesc = matChar.getCharDesc().toUpperCase();

	        if (existingCharDesc.equals(newCharDesc)) {
	            throw new DuplicateCharacteristicException("Characteristic '" + matChar.getCharDesc() + "' already exists for material ID: " + matChar.getMatId());
	        }
	    } 

	    return material; 
	}

	@Override
	public List<InspectionLot> getAllInspectionLots() {

		log.info("getting all lots");

		List<InspectionLot> lots = inspectionLotRepo.findAll();

		List<InspectionLot> responseList = new LinkedList<>();

		for (InspectionLot lot : lots) {

			if (lot.getMaterial().getMaterialChar().size() != lot.getInspectionActuals().size()) {

				log.info("adding lots those have not done all inspection actuals");

				responseList.add(lot);
			}
		}

		log.info("returing response list");

		return responseList;
	}

	@Override
	public List<MaterialInspectionCharacteristicsDto> getMaterialCharByLotId(Integer id) {

		Optional<InspectionLot> optLot = inspectionLotRepo.findById(id);

		if (optLot.isEmpty()) {
			throw new GenericNotFoundException("Lot not found with id: " + id);
 
		} else { 

			Material material = optLot.get().getMaterial(); 

			List<Integer> inspActualsList = optLot.get().getInspectionActuals().stream()
					.map(inspAct -> inspAct.getMaterialInspectionCharacteristics().getCharacteristicId())
					.collect(Collectors.toList());

			List<MaterialInspectionCharacteristics> charList = material.getMaterialChar();

			List<MaterialInspectionCharacteristicsDto> repsonseList = new LinkedList<>();
			
			List<MaterialInspectionCharacteristicsDto> list = charList.stream()
			.map(ch-> MaterialInspectionCharacteristicsMapper.convertEntityToDto(ch))
			.toList(); 
 
			for (MaterialInspectionCharacteristicsDto item : list) {
				if (inspActualsList.contains(item.getCharacteristicId())) {

				} else {

					log.info("getting all material characteristics of lot {}", id);

					repsonseList.add(item);
				}
			}
			return repsonseList;
		}
	}
 
	@Override
	public List<MaterialInspectionCharacteristicsDto> getAllCharacteristicsOfMaterial(String id) {
	    String MaterialId = StringUtil.removeAllSpaces(id);
	    
	    Optional<Material> optionalMaterial = materialRepository.findById(MaterialId); 
	    if (optionalMaterial.isEmpty()) {
	        throw new GenericNotFoundException("Material with ID " + MaterialId + " not found.");
	    } 

	    return optionalMaterial.get().getMaterialChar()
	    		.stream()
	    		.map(characteristics-> MaterialInspectionCharacteristicsMapper.convertEntityToDto(characteristics))
	    		.toList(); 
	}   
 
	@Override
	public void saveEditMaterial(MaterialDto materialDto) {

		MaterialDto optMaterial = getMaterial(StringUtil.removeAllSpaces(materialDto.getMaterialId()).toUpperCase());

		if (optMaterial == null) { 
			throw new GenericNotFoundException("Material not found with given id: " + materialDto.getMaterialId());
		}

		optMaterial.setMaterialId(StringUtil.removeAllSpaces(materialDto.getMaterialId()).toUpperCase());

		optMaterial.setMaterialDesc(StringUtil.removeExtraSpaces(materialDto.getMaterialDesc()).toUpperCase());

		optMaterial.setType(StringUtil.removeExtraSpaces(materialDto.getType().toUpperCase()));
		
		optMaterial.setStatus(materialDto.isStatus());

		materialRepository.save(MaterialMapper.convertDtoToEntity(optMaterial)); 


	}
 
	@Override 
	public List<MaterialDto> getAllActiveMaterials() {
		return materialRepository.findAllByStatus(true)
				.stream()
				.map(material-> MaterialMapper.convertEntityToDto(material))
				.toList(); 
	}
 
	/**
	 *  Save Characteristics by using Multi-Part file
	 */
	
	@SuppressWarnings("resource")
	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean addListOfCharacteristicsForMaterial(MultipartFile file) throws Exception {
	 
	    String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
	    List<String> validExtensions = Arrays.asList("xls", "xlsx", "csv");
	 
	    if (fileExtension == null || !validExtensions.contains(fileExtension.toLowerCase())) {
	        throw new Exception("Please provide .xls, .xlsx, or .csv file");
	    }
	 
	    try {
	        if (fileExtension.equalsIgnoreCase("csv")) {
	            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
	                String line;
	                int row = 0;
	                while ((line = reader.readLine()) != null) {
	                    if (row++ == 0) continue; 
	 
	                    String[] values = line.split(",");
	                    if (values.length < 6) {
	                        log.warn("Skipping invalid row: {}", Arrays.toString(values));
	                        continue;
	                    }
	 
	                    String charDesc = values[1].trim();
	                    double utl = Double.parseDouble(values[2].trim());
	                    double ltl = Double.parseDouble(values[3].trim());
	                    String uom = values[4].trim();
	                    String matId = values[5].trim();
	 
	                    processMaterialCharacteristic(matId, charDesc, utl, ltl, uom);
	                }
	            }
	        } else {
	            try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
	                XSSFSheet sheet = workbook.getSheetAt(0);
	 
	                for (int row = 1; row < sheet.getPhysicalNumberOfRows(); row++) {
	                    XSSFRow currRow = sheet.getRow(row);
	                    if (currRow == null) continue;
	 
	                    String charDesc = getCellValueAsString(currRow.getCell(1)); 
	                    Double utl = getCellValueAsDouble(currRow.getCell(2));     
	                    Double ltl = getCellValueAsDouble(currRow.getCell(3));     
	                    String uom = getCellValueAsString(currRow.getCell(4));      
	                    String matId = getCellValueAsString(currRow.getCell(5));    
	 
	                    if (charDesc == null || utl == null || ltl == null || uom == null || matId == null) {
	                        log.warn("Skipping invalid row: {}", row);
	                        continue;
	                    }
	 
	                    processMaterialCharacteristic(matId, charDesc, utl, ltl, uom);
	                }
	            }
	        }
	 
	    } catch (IOException e) {
	        log.error("Unable to read uploaded document", e);
	        throw new Exception("Failed to read file content", e);
	    }
	 
	    return true;
	}
	 
	/**
	* Helper to safely convert Excel cell to String
	*/
	private String getCellValueAsString(Cell cell) {
	    if (cell == null) return null;
	    switch (cell.getCellType()) {
	        case STRING:
	            return cell.getStringCellValue().trim();
	        case NUMERIC:
	            double num = cell.getNumericCellValue();
	            if (num == Math.floor(num)) {
	                return String.valueOf((long) num);
	            } else {
	                return String.valueOf(num);
	            }
	        case BOOLEAN:
	            return String.valueOf(cell.getBooleanCellValue());
	        case FORMULA:
	            return cell.getCellFormula();
	        case BLANK:
	        default:
	            return null;
	    }
	}
	 
	/**
	* Helper to safely convert Excel cell to Double
	*/
	private Double getCellValueAsDouble(Cell cell) {
	    if (cell == null) return null;
	    try {
	        switch (cell.getCellType()) {
	            case NUMERIC:
	                return cell.getNumericCellValue();
	            case STRING:
	                String strVal = cell.getStringCellValue().trim();
	                return Double.parseDouble(strVal);
	            default:
	                return null;
	        }
	    } catch (NumberFormatException e) {
	        log.warn("Invalid numeric value in cell: {}", cell);
	        return null;
	    }
	}
	 
	/**
	* Centralized logic for characteristic creation and persistence
	*/
	private void processMaterialCharacteristic(String materialId, String charDesc, double utl, double ltl, String uom) throws Exception {
	    MaterialCharDto materialCharDto = MaterialCharDto.builder()
	            .matId(materialId)
	            .charDesc(charDesc)
	            .uom(uom)
	            .utl(utl)
	            .ltl(ltl)
	            .build();
	 
	    Material material = isCharacteristicConditionSatisfy(materialCharDto);
	    if (material == null) {
	        log.warn("Material characteristic {} already exists or material not found: {}", charDesc, materialId);
	        throw new Exception("Material characteristic already exists or material not found for id: " + materialId);
	    } else {
	        MaterialInspectionCharacteristics matChar =
	                Transformers.convertMaterialCharDtoToMaterialInspectionCharObj(materialCharDto, material);
	        material.getMaterialChar().add(matChar);
	        materialRepository.save(material);
	        log.info("New material characteristic '{}' saved for material id: {}", charDesc, materialId);
	    }
	}
	 

	@Override
	public MaterialInspectionCharacteristicsDto getCharacteristicsByChId(Integer id) {
		
		
		log.info("finding material with id : {}", id);

		Optional<MaterialInspectionCharacteristics> optMaterial = materialCharReposotory.findById(id);
 
		if (optMaterial.isEmpty()) { 

			log.info("no material associated with id : {}", id);

			throw new GenericNotFoundException("Material characteristcs not found with id: " + id);
		} 

		return  MaterialInspectionCharacteristicsMapper.convertEntityToDto(optMaterial.get());  
	}

	@Override
	public void update( MaterialCharUpdateDto charDto) {
		
		MaterialInspectionCharacteristics exisCharacteristics = materialCharReposotory.findById(charDto.getCharacteristicId()) 
	            .orElseThrow(() -> new GenericNotFoundException("Characteristics with ID " + charDto.getCharacteristicId() + " does not exist."));

		exisCharacteristics.setCharacteristicDescription(StringUtil.removeExtraSpaces(charDto.getCharDesc()).toUpperCase());
		exisCharacteristics.setLowerToleranceLimit(charDto.getLtl());
		exisCharacteristics.setUpperToleranceLimit(charDto.getUtl());
		exisCharacteristics.setUnitOfMeasure(charDto.getUom()); 

	      

	   materialCharReposotory.save(exisCharacteristics); 
		
	} 

	@Override
	public void deleteMaterialCharacteristics(Integer id) {
		
		 materialCharReposotory.findById(id)
		            .orElseThrow(() -> new GenericNotFoundException("Material Characteristic with ID " + id + " does not exist."));
		    materialCharReposotory.deleteById(id);
		
	}

	@Override
	public List<MaterialInspectionCharacteristicsDto> getAllCharacteristics() {
		
		return materialCharReposotory.findAll()
		 .stream()
 		.map(characteristics-> MaterialInspectionCharacteristicsMapper.convertEntityToDto(characteristics))
 		.toList(); 
		 
	}
 
}
 