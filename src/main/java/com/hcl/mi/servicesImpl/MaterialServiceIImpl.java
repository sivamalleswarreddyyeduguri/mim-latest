package com.hcl.mi.servicesImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.hcl.mi.responsedtos.MaterialDto;
import com.hcl.mi.responsedtos.MaterialInspectionCharacteristicsDto;
import com.hcl.mi.services.MaterialService;
import com.hcl.mi.utils.StringUtil;

@Service
public class MaterialServiceIImpl implements MaterialService {

	private MaterialRepository materialRepository;

	private MaterialCharRepository materialCharReposotory;

	private InspectionLotRepository inspectionLotRepo;

	private Logger LOG = LoggerFactory.getLogger(MaterialServiceIImpl.class);

	public MaterialServiceIImpl(MaterialRepository materialRepository, MaterialCharRepository materialCharReposotory,
			InspectionLotRepository inspectionLotRepo) {
		super();
		this.materialRepository = materialRepository;

		this.materialCharReposotory = materialCharReposotory;

		this.inspectionLotRepo = inspectionLotRepo;

	} 

	@Override
	public List<Material> getAllMaterials() {

		LOG.info("finding all materials");

		List<Material> materialList = materialRepository.findAll();

		LOG.info("returing all materials list");

		return materialList;
	}
 
	@Override
	public MaterialDto getMaterial(String id) {

		LOG.info("finding material with id : {}", id);

		Optional<Material> optMaterial = materialRepository.findById(id.toUpperCase());

		if (optMaterial.isEmpty()) {

			LOG.info("no material associated with id : {}", id);

			throw new GenericNotFoundException("Material not found with id: " + id);
		} 

		return  MaterialMapper.convertEntityToDto(optMaterial.get());  
	}

	@Override
	public void deleteMaterial(String id) {

		LOG.info("finding material with id : {}", id);

		Optional<Material> optMaterial = materialRepository.findById(id.toUpperCase()); 

		if (optMaterial.isEmpty()) {

			LOG.info("no material associated with id : {}", id);
			
			throw new GenericNotFoundException("Material not found with id: " + id);

		}

		Material material = optMaterial.get();

		LOG.info("setting material status to INACTIVE");

		material.setStatus(false);

		LOG.info("saving material of id : {}", id);

		materialRepository.save(material);

		LOG.info("returning true");

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

	    LOG.info("New material saved with ID: {}", materialId);
	}

	@Override
	public void addNewMaterialCharacteristic(MaterialCharDto matChar) {

		Material material = isCharacteristicConditionSatisfy(matChar);
  
		MaterialInspectionCharacteristics matCharObj = Transformers
				.convertMaterialCharDtoToMaterialInspectionCharObj(matChar, material);

		LOG.info("new Material characteristic adding {}", matChar);

		 materialCharReposotory.save(matCharObj);

	}
 
	
	private Material isCharacteristicConditionSatisfy(MaterialCharDto matChar) {
		
	    MaterialDto materialDto;
	    
	    try {
	        materialDto = getMaterial(matChar.getMatId());
	    } catch (GenericNotFoundException e) {
	        LOG.error("Material not found in isCharacteristicConditionSatisfy for ID: {}", matChar.getMatId());
	        throw e;
	    }
 
	    Material material = MaterialMapper.convertDtoToEntity(materialDto);
	    System.out.println(material + "-".repeat(100)); 

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

					LOG.info("getting all material characteristics of lot {}", id);

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

		Material savedMaterial = materialRepository.save(MaterialMapper.convertDtoToEntity(materialDto)); 


		LOG.info("material updation saved with id : {}", savedMaterial.getMaterialId());

	}

	@Override
	public List<MaterialDto> getAllActiveMaterials() {
		return materialRepository.findAllByStatus(true)
				.stream()
				.map(material-> MaterialMapper.convertEntityToDto(material))
				.toList(); 
	}

	@SuppressWarnings("resource")
	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean addListOfCharacteristicsForMaterial(MultipartFile file) throws Exception {

		String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());

		ArrayList<String> list = new ArrayList<>();
		list.add("xls");
		list.add("xlsx");
		list.add("csv");

		if (!list.contains(fileExtension)) {
			throw new Exception("please provide .xls or .xlsx or .csv file");
		}

		try {
			XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
			XSSFSheet sheet_0 = workbook.getSheetAt(0);

			for (int row = 1; row < sheet_0.getPhysicalNumberOfRows(); row++) {
				XSSFRow currRow = sheet_0.getRow(row);

				String materialId = currRow.getCell(0).getStringCellValue();
				String charDesc = currRow.getCell(1).getStringCellValue();
				double utl = currRow.getCell(2).getNumericCellValue();
				double ltl = currRow.getCell(3).getNumericCellValue();
				String uom = currRow.getCell(4).getStringCellValue();

				MaterialCharDto materialCharDto = MaterialCharDto.builder()
					                                           	.matId(materialId)
						.charDesc(charDesc)
						.uom(uom).utl(utl)
						.ltl(ltl)
						.build();

				Material material = isCharacteristicConditionSatisfy(materialCharDto);
				if (material == null) {
					LOG.warn(
							"material characteristic {} is already available or materila is not available with id of {}",
							charDesc, materialId);
					throw new Exception(
							"material characteristic is already available or materila is not available with id of : "
									+ materialId);
				} else {
					MaterialInspectionCharacteristics matChar = Transformers
							.convertMaterialCharDtoToMaterialInspectionCharObj(materialCharDto, material);
					material.getMaterialChar().add(matChar);
					materialRepository.save(material);
					LOG.info("new material characteristic {} saved for material id : {}", charDesc, materialId);
				}
			}
		} catch (IOException e) {
			LOG.warn("unable to read uploaded document");
		}
		return true;
	}

}
