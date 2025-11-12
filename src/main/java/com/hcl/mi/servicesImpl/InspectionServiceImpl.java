package com.hcl.mi.servicesImpl;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.awt.Color;

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
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

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
//			if (optInsp.get().getUser() != null) {
// 				lot.setUserName(optInsp.get().getUser().getUsername()); 
//			} 

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
	public List<InspectionLotDto> getAllInspectionLots() {

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
 
		return responseList.stream().map(lot-> InspectionLotMapper.convertEntityToDto(lot)).toList();
	}
	
	
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

//		Optional<User> optUser = userRepo.findById(lot.getUserid());
//		if (optUser.isEmpty()) {
//			throw new GenericNotFoundException("Lot not found with id: " + lot.getUserid());    
//		}
 
		InspectionLot originalLot = optInsp.get();

		originalLot.setInspectionEndDate(lot.getDate());
		originalLot.setResult(StringUtil.removeExtraSpaces(lot.getResult()));

		originalLot.setRemarks(StringUtil.removeExtraSpaces(lot.getRemarks()));
//		originalLot.setUser(optUser.get());

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

//	public byte[] generateReportPdf(Integer lotId) {
//	    Optional<InspectionLot> optLot = inspectionLotRepo.findById(lotId);
//	    if (optLot.isEmpty()) {
//	        throw new GenericNotFoundException("Lot not found with id: " + lotId);
//	    }
//
//	    InspectionLot lot = optLot.get();
//	    Material material = lot.getMaterial();
//	    Vendor vendor = lot.getVendor();
//	    Plant plant = lot.getPlant();
//	    List<InspectionActuals> actuals = lot.getInspectionActuals();
//	    List<MaterialInspectionCharacteristics> characteristics =
//	            material != null ? material.getMaterialChar() : List.of();
//
//	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//	    Document document = new Document();
//
//	    try {
//	        PdfWriter.getInstance(document, baos);
//	        document.open();
//
//	        // ✅ Fonts
//	        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, Color.BLACK);
//	        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
//	        Font normalBold = new Font(Font.HELVETICA, 11, Font.BOLD, Color.BLACK);
//	        Font normal = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
//	        Font small = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
//
//	        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
//
//	        // Title
//	        Paragraph title = new Paragraph("Material Inspection Report", titleFont);
//	        title.setAlignment(Element.ALIGN_CENTER);
//	        document.add(title);
//	        document.add(new Paragraph(" "));
//
//	        // Header table
//	        PdfPTable headerTbl = new PdfPTable(2);
//	        headerTbl.setWidthPercentage(100);
//	        headerTbl.setSpacingBefore(6f);
//	        headerTbl.setSpacingAfter(10f);
//	        headerTbl.setWidths(new float[]{2f, 4f});
//
//	        addHeaderRow(headerTbl, "Report Date:", java.time.LocalDate.now().format(dtf), normalBold, normal);
//	        addHeaderRow(headerTbl, "Inspection Lot ID:", String.valueOf(lot.getLotId()), normalBold, normal);
//	        addHeaderRow(headerTbl, "Inspection Start Date:", lot.getInspectionStartDate() != null ? lot.getInspectionStartDate().format(dtf) : "-", normalBold, normal);
//	        addHeaderRow(headerTbl, "Inspection End Date:", lot.getInspectionEndDate() != null ? lot.getInspectionEndDate().format(dtf) : "-", normalBold, normal);
//	        addHeaderRow(headerTbl, "Final Decision:", lot.getResult() != null ? lot.getResult() : "-", normalBold, normal);
//	        document.add(headerTbl);
//
//	        // Material Details
//	        document.add(new Paragraph("Material Details", headerFont));
//	        PdfPTable matTbl = new PdfPTable(2);
//	        matTbl.setWidthPercentage(100);
//	        matTbl.setWidths(new float[]{2f, 5f});
//	        matTbl.setSpacingBefore(6f);
//	        matTbl.addCell(makeCell("Material ID", normalBold));
//	        matTbl.addCell(makeCell(material != null ? material.getMaterialId() : "-", normal));
//	        matTbl.addCell(makeCell("Material Description", normalBold));
//	        matTbl.addCell(makeCell(material != null ? material.getMaterialDesc() : "-", normal));
//	        document.add(matTbl);
//	        document.add(new Paragraph(" "));
//
//	        // Vendor & Plant details
//	        PdfPTable vpTbl = new PdfPTable(4);
//	        vpTbl.setWidthPercentage(100);
//	        vpTbl.setWidths(new float[]{2f, 3f, 2f, 3f});
//	        vpTbl.addCell(makeCell("Vendor ID", normalBold));
//	        vpTbl.addCell(makeCell(vendor != null ? String.valueOf(vendor.getVendorId()) : "-", normal));
//	        vpTbl.addCell(makeCell("Vendor Name", normalBold));
//	        vpTbl.addCell(makeCell(vendor != null ? vendor.getName() : "-", normal));
//	        vpTbl.addCell(makeCell("Plant ID", normalBold));
//	        vpTbl.addCell(makeCell(plant != null ? plant.getPlantId() : "-", normal));
//	        vpTbl.addCell(makeCell("Plant Name", normalBold));
//	        vpTbl.addCell(makeCell(plant != null ? plant.getPlantName() : "-", normal));
//	        document.add(vpTbl);
//	        document.add(new Paragraph(" "));
//
//	        // Characteristics Table
//	        document.add(new Paragraph("Inspection Characteristics", headerFont));
//	        PdfPTable charTbl = new PdfPTable(7);
//	        charTbl.setWidthPercentage(100);
//	        charTbl.setSpacingBefore(6f);
//	        charTbl.setWidths(new float[]{1f, 3f, 1.2f, 1.2f, 1.2f, 1.2f, 1.2f});
//
//	        // Headers
//	        charTbl.addCell(makeHeaderCell("#"));
//	        charTbl.addCell(makeHeaderCell("Characteristic"));
//	        charTbl.addCell(makeHeaderCell("UOM"));
//	        charTbl.addCell(makeHeaderCell("Lower Limit"));
//	        charTbl.addCell(makeHeaderCell("Upper Limit"));
//	        charTbl.addCell(makeHeaderCell("Actual Value"));
//	        charTbl.addCell(makeHeaderCell("Result"));
//
//	        int idx = 1;
//	        for (MaterialInspectionCharacteristics ch : characteristics) {
//	            Double lower = ch.getLowerToleranceLimit();
//	            Double upper = ch.getUpperToleranceLimit();
//	            String uom = ch.getUnitOfMeasure();
//
//	            InspectionActuals matchedActual = null;
//	            if (actuals != null) {
//	                for (InspectionActuals a : actuals) {
//	                    if (a.getMaterialInspectionCharacteristics() != null &&
//	                        a.getMaterialInspectionCharacteristics().getCharacteristicId().equals(ch.getCharacteristicId())) {
//	                        matchedActual = a;
//	                        break;
//	                    }
//	                }
//	            }
//
//	            String actualStr = "-";
//	            String result = "-";
//	            if (matchedActual != null) {
//	                Double actualVal = matchedActual.getMaximumMeasurement() != null ? matchedActual.getMaximumMeasurement()
//	                        : matchedActual.getMinimumMeasurement();
//	                actualStr = actualVal != null ? String.valueOf(actualVal) : "-";
//	                if (actualVal != null && lower != null && upper != null) {
//	                    if (actualVal >= lower && actualVal <= upper) result = "PASS";
//	                    else result = "FAIL";
//	                } else result = "N/A";
//	            } else result = "NOT RECORDED";
//
//	            charTbl.addCell(makeCell(String.valueOf(idx++), normal));
//	            charTbl.addCell(makeCell(ch.getCharacteristicDescription(), normal));
//	            charTbl.addCell(makeCell(uom != null ? uom : "-", normal));
//	            charTbl.addCell(makeCell(lower != null ? String.valueOf(lower) : "-", normal));
//	            charTbl.addCell(makeCell(upper != null ? String.valueOf(upper) : "-", normal));
//	            charTbl.addCell(makeCell(actualStr, normal));
//
//	            PdfPCell resultCell = new PdfPCell(new Phrase(result, normal));
//	            if ("PASS".equalsIgnoreCase(result)) resultCell.setBackgroundColor(new Color(200, 255, 200));
//	            else if ("FAIL".equalsIgnoreCase(result)) resultCell.setBackgroundColor(new Color(255, 200, 200));
//	            else resultCell.setBackgroundColor(Color.WHITE);
//	            charTbl.addCell(resultCell);
//	        }
//
//	        document.add(charTbl);
//	        document.add(new Paragraph(" "));
//
//	        document.add(new Paragraph("Generated by Material Inspection Module", small));
//	        document.close();
//
//	        return baos.toByteArray();
//
//	    } catch (DocumentException e) {
//	        throw new RuntimeException("Error generating PDF report: " + e.getMessage(), e);
//	    } finally {
//	        try { baos.close(); } catch (Exception ignored) {}
//	    }
//	}
//
//	private PdfPCell makeCell(String text, Font font) {
//	    PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", font));
//	    cell.setPadding(6f);
//	    return cell;
//	}
//
//	private PdfPCell makeHeaderCell(String text) {
//	    Font header = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
//	    PdfPCell cell = new PdfPCell(new Phrase(text, header));
//	    cell.setBackgroundColor(new Color(100, 100, 100));
//	    cell.setPadding(6f);
//	    return cell;
//	}
//
//	private void addHeaderRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
//	    PdfPCell cell = new PdfPCell(new Phrase(label, labelFont));
//	    cell.setBorder(PdfPCell.NO_BORDER);
//	    table.addCell(cell);
//	    cell = new PdfPCell(new Phrase(value, valueFont));
//	    cell.setBorder(PdfPCell.NO_BORDER);
//	    table.addCell(cell);
//	}
	
	
	
	
	
	
//	public byte[] generateReportPdf(Integer lotId) {
//	    Optional<InspectionLot> optLot = inspectionLotRepo.findById(lotId);
//	    if (optLot.isEmpty()) {
//	        throw new GenericNotFoundException("Lot not found with id: " + lotId);
//	    }
//
//	    InspectionLot lot = optLot.get();
//	    Material material = lot.getMaterial();
//	    Vendor vendor = lot.getVendor();
//	    Plant plant = lot.getPlant();
//	    List<InspectionActuals> actuals = lot.getInspectionActuals();
//	    List<MaterialInspectionCharacteristics> characteristics =
//	            material != null ? material.getMaterialChar() : List.of();
//
//	    // get inspector name from createdBy property (supports String or User)
//	    String inspectorName = "-";
//	    try {
//	        Object cb = null;
//	        // assume InspectionLot has a getCreatedBy method - adjust if different
//	        try {
//	            cb = InspectionLot.class.getMethod("getCreatedBy").invoke(lot);
//	        } catch (NoSuchMethodException nsme) {
//	            // fallback: maybe BaseEntity has createdBy; try super
//	            try {
//	                cb = lot.getClass().getMethod("getCreatedBy").invoke(lot);
//	            } catch (Exception ignore) {
//	            }
//	        }
//	        if (cb != null) {
//	            if (cb instanceof String) inspectorName = (String) cb;
//	            else {
//	                // if it's a User entity with getUsername()
//	                try {
//	                    Object username = cb.getClass().getMethod("getUsername").invoke(cb);
//	                    if (username != null) inspectorName = username.toString();
//	                } catch (Exception e) {
//	                    inspectorName = cb.toString();
//	                }
//	            }
//	        }
//	    } catch (Exception e) {
//	        // ignore - inspectorName stays "-"
//	    }
//
//	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//	    Document document = new Document(); // A4 default
//	    try {
//	        PdfWriter.getInstance(document, baos);
//	        document.open();
//
//	        // Fonts and styles
//	        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLACK);
//	        Font sectionFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
//	        Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
//	        Font normal = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
//	        Font smallItalic = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.DARK_GRAY);
//	        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
//
//	        // 1) Title area (large)
//	        Paragraph title = new Paragraph("MATERIAL INSPECTION REPORT", titleFont);
//	        title.setAlignment(Element.ALIGN_CENTER);
//	        document.add(title);
//	        document.add(new Paragraph(" ")); // spacer
//
//	        // Optional: company logo on top-right if available on classpath
//	        // Image logo = null;
//	        // try { logo = Image.getInstance(getClass().getResource("/static/images/logo.png")); logo.scaleToFit(90, 40); logo.setAlignment(Element.ALIGN_RIGHT); document.add(logo);} catch(Exception ignore){}
//
//	        // 2) Top metadata block resembling the form header
//	        PdfPTable meta = new PdfPTable(4);
//	        meta.setWidthPercentage(100);
//	        meta.setWidths(new float[]{2.0f, 3.5f, 2.0f, 3.5f});
//	        addMetaCell(meta, "MIR No.:", labelFont, normal);
//	        addMetaCell(meta, String.valueOf(lot.getLotId()), normal, normal);
//	        addMetaCell(meta, "Date:", labelFont, normal);
//	        addMetaCell(meta, lot.getCreationDate() != null ? lot.getCreationDate().format(dtf) : "-", normal, normal);
//
//	        addMetaCell(meta, "Project/Plant:", labelFont, normal);
//	        addMetaCell(meta, plant != null ? plant.getPlantName() + " (" + plant.getPlantId() + ")" : "-", normal, normal);
//	        addMetaCell(meta, "PO/WO/DO No.:", labelFont, normal);
//	        addMetaCell(meta, "-", normal, normal); // placeholder, populate if you have PO info
//
//	        addMetaCell(meta, "Vendor/Supplier:", labelFont, normal);
//	        addMetaCell(meta, vendor != null ? vendor.getName() + " (ID:" + vendor.getVendorId() + ")" : "-", normal, normal);
//	        addMetaCell(meta, "Inspected By:", labelFont, normal);
//	        addMetaCell(meta, inspectorName, normal, normal);
//
//	        document.add(meta);
//	        document.add(new Paragraph(" "));
//
//	        // 3) "What this report contains" descriptive paragraph (real-time content)
//	        Paragraph desc = new Paragraph("Report Contents: This inspection report captures the inspection lot metadata, vendor and material details, "
//	                + "the list of inspection characteristics (specification limits), actual measured values recorded during inspection, "
//	                + "a pass/fail result per characteristic, a summary (passed/failed counts), and authorization/remarks.", normal);
//	        desc.setAlignment(Element.ALIGN_JUSTIFIED);
//	        document.add(desc);
//	        document.add(new Paragraph(" "));
//
//	        // 4) Large characteristics table similar to the image (columns and many rows)
//	        PdfPTable table = new PdfPTable(7);
//	        table.setWidthPercentage(100);
//	        table.setWidths(new float[]{0.8f, 3f, 1.0f, 1.1f, 1.1f, 1.1f, 1.1f});
//	        // header row
//	        table.addCell(makeHeaderCell("S.no", labelFont));
//	        table.addCell(makeHeaderCell("Date of Delivery / Characteristic", labelFont));
//	        table.addCell(makeHeaderCell("UOM", labelFont));
//	        table.addCell(makeHeaderCell("Lower Limit", labelFont));
//	        table.addCell(makeHeaderCell("Upper Limit", labelFont));
//	        table.addCell(makeHeaderCell("Actual Value", labelFont));
//	        table.addCell(makeHeaderCell("Accepted/Rejected", labelFont));
//
//	        // Fill rows from characteristics and actuals. We will show as many rows as characteristics,
//	        // and pad to a minimum number of visible rows (like a form).
//	        int idx = 1;
//	        int minRows = Math.max(10, characteristics.size()); // show at least 10 rows for form look
//	        for (int r = 0; r < minRows; r++) {
//	            if (r < characteristics.size()) {
//	                MaterialInspectionCharacteristics ch = characteristics.get(r);
//	                // find matching actual
//	                InspectionActuals matched = null;
//	                if (actuals != null) {
//	                    for (InspectionActuals a : actuals) {
//	                        if (a.getMaterialInspectionCharacteristics() != null
//	                                && a.getMaterialInspectionCharacteristics().getCharacteristicId()
//	                                        .equals(ch.getCharacteristicId())) {
//	                            matched = a;
//	                            break;
//	                        }
//	                    }
//	                }
//	                String dateOfDelivery = lot.getCreationDate() != null ? lot.getCreationDate().format(dtf) : "-";
//	                String charName = ch.getCharacteristicDescription();
//	                String uom = ch.getUnitOfMeasure() != null ? ch.getUnitOfMeasure() : "-";
//	                String lower = ch.getLowerToleranceLimit() != null ? String.valueOf(ch.getLowerToleranceLimit()) : "-";
//	                String upper = ch.getUpperToleranceLimit() != null ? String.valueOf(ch.getUpperToleranceLimit()) : "-";
//	                String actualVal = "-";
//	                String decision = "NOT RECORDED";
//	                if (matched != null) {
//	                    Double v = matched.getMaximumMeasurement() != null ? matched.getMaximumMeasurement()
//	                            : matched.getMinimumMeasurement();
//	                    actualVal = v != null ? String.valueOf(v) : "-";
//	                    if (v != null && ch.getLowerToleranceLimit() != null && ch.getUpperToleranceLimit() != null) {
//	                        if (v >= ch.getLowerToleranceLimit() && v <= ch.getUpperToleranceLimit()) {
//	                            decision = "ACCEPTED";
//	                        } else {
//	                            decision = "REJECTED";
//	                        }
//	                    } else {
//	                        decision = "N/A";
//	                    }
//	                }
//
//	                table.addCell(makeCell(String.valueOf(idx++), normal));
//	                table.addCell(makeCell(charName + (charName.length() > 0 ? "": ""), normal));
//	                table.addCell(makeCell(uom, normal));
//	                table.addCell(makeCell(lower, normal));
//	                table.addCell(makeCell(upper, normal));
//	                table.addCell(makeCell(actualVal, normal));
//
//	                PdfPCell decCell = makeCell(decision, normal);
//	                if ("ACCEPTED".equalsIgnoreCase(decision)) decCell.setBackgroundColor(new Color(200,255,200));
//	                else if ("REJECTED".equalsIgnoreCase(decision)) decCell.setBackgroundColor(new Color(255,200,200));
//	                table.addCell(decCell);
//
//	            } else {
//	                // padding empty row to keep form look
//	                table.addCell(makeCell(String.valueOf(idx++), normal));
//	                table.addCell(makeCell("", normal));
//	                table.addCell(makeCell("", normal));
//	                table.addCell(makeCell("", normal));
//	                table.addCell(makeCell("", normal));
//	                table.addCell(makeCell("", normal));
//	                table.addCell(makeCell("", normal));
//	            }
//	        }
//
//	        document.add(table);
//	        document.add(new Paragraph(" "));
//
//	        // 5) Summary block
//	        int passCount = 0, failCount = 0;
//	        for (MaterialInspectionCharacteristics ch : characteristics) {
//	            // find actual
//	            InspectionActuals matched = null;
//	            if (actuals != null) {
//	                for (InspectionActuals a : actuals) {
//	                    if (a.getMaterialInspectionCharacteristics() != null
//	                            && a.getMaterialInspectionCharacteristics().getCharacteristicId()
//	                                    .equals(ch.getCharacteristicId())) {
//	                        matched = a;
//	                        break;
//	                    }
//	                }
//	            }
//	            if (matched != null) {
//	                Double v = matched.getMaximumMeasurement() != null ? matched.getMaximumMeasurement()
//	                        : matched.getMinimumMeasurement();
//	                if (v != null && ch.getLowerToleranceLimit() != null && ch.getUpperToleranceLimit() != null) {
//	                    if (v >= ch.getLowerToleranceLimit() && v <= ch.getUpperToleranceLimit()) passCount++;
//	                    else failCount++;
//	                }
//	            }
//	        }
//
//	        PdfPTable summary = new PdfPTable(4);
//	        summary.setWidthPercentage(60);
//	        summary.addCell(makeCell("Total Characteristics", labelFont));
//	        summary.addCell(makeCell(String.valueOf(characteristics.size()), normal));
//	        summary.addCell(makeCell("Passed", labelFont));
//	        summary.addCell(makeCell(String.valueOf(passCount), normal));
//	        summary.addCell(makeCell("Failed", labelFont));
//	        summary.addCell(makeCell(String.valueOf(failCount), normal));
//	        summary.addCell(makeCell("Final Decision", labelFont));
//	        String finalDecision = lot.getResult() != null ? lot.getResult() : (failCount > 0 ? "REJECTED" : "ACCEPTED");
//	        summary.addCell(makeCell(finalDecision, normal));
//	        document.add(summary);
//	        document.add(new Paragraph(" "));
//
//	        // 6) Remarks + Authorized by area (signature and inspector name)
//	        document.add(new Paragraph("Remarks", sectionFont));
//	        document.add(new Paragraph(lot.getRemarks() != null ? lot.getRemarks() : "-", normal));
//	        document.add(new Paragraph(" "));
//
//	        PdfPTable auth = new PdfPTable(2);
//	        auth.setWidthPercentage(100);
//	        auth.setWidths(new float[]{2f, 3f});
//	        // left empty area for prepared by etc (mirrors the form)
//	        auth.addCell(makeCell("Prepared By:", labelFont));
//	        PdfPCell rightCell = new PdfPCell();
//	        rightCell.setBorder(Rectangle.NO_BORDER);
//
//	        // Signature lines and inspector name below
//	        Paragraph sig = new Paragraph("Authorized By:", sectionFont);
//	        sig.add(new Paragraph("Inspector: ________________________________", normal));
//	        sig.add(new Paragraph("Signature: ________________________________", normal));
//	        // Add styled inspector name below signature
//	        Paragraph signedBy = new Paragraph("Signed by: " + inspectorName, smallItalic);
//	        signedBy.setSpacingBefore(6f);
//	        sig.add(signedBy);
//	        rightCell.addElement(sig);
//	        auth.addCell(rightCell);
//	        document.add(auth);
//
//	        document.add(new Paragraph(" "));
//	        document.add(new Paragraph("Generated by Material Inspection Module — Confidential", smallItalic));
//
//	        document.close();
//	        return baos.toByteArray();
//
//	    } catch (DocumentException dex) {
//	        LOG.error("Error generating PDF for lot id {} : {}", lotId, dex.getMessage());
//	        throw new RuntimeException("Unable to generate PDF report");
//	    } finally {
//	        try {
//	            baos.close();
//	        } catch (Exception e) {
//	            // ignore
//	        }
//	    }
//	}
//
//	// Helper cell maker methods (reuse these in your service class)
//	private PdfPCell makeCell(String text, Font font) {
//	    PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", font));
//	    cell.setPadding(6f);
//	    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
//	    return cell;
//	}
//
//	private PdfPCell makeHeaderCell(String text, Font font) {
//	    PdfPCell cell = new PdfPCell(new Phrase(text, font));
//	    cell.setBackgroundColor(new Color(100, 100, 100));
//	    cell.setPadding(6f);
//	    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//	    return cell;
//	}
//
//	// convenience overloaded call
//	private PdfPCell makeHeaderCell(String text) {
//	    Font header = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
//	    PdfPCell cell = new PdfPCell(new Phrase(text, header));
//	    cell.setBackgroundColor(new Color(100, 100, 100));
//	    cell.setPadding(6f);
//	    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//	    return cell;
//	}
//
//	private void addMetaCell(PdfPTable table, String label, Font labelFont, Font valueFont) {
//	    PdfPCell lcell = new PdfPCell(new Phrase(label, labelFont));
//	    lcell.setBorder(PdfPCell.NO_BORDER);
//	    lcell.setPadding(4f);
//	    table.addCell(lcell);
//
//	    PdfPCell vcell = new PdfPCell(new Phrase("", valueFont));
//	    vcell.setBorder(PdfPCell.NO_BORDER);
//	    vcell.setPadding(4f);
//	    table.addCell(vcell);
//	}
//
//	// overloaded to pass value as well
//	private void addMetaCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
//	    PdfPCell lcell = new PdfPCell(new Phrase(label, labelFont));
//	    lcell.setBorder(PdfPCell.NO_BORDER);
//	    lcell.setPadding(4f);
//	    table.addCell(lcell);
//
//	    PdfPCell vcell = new PdfPCell(new Phrase(value != null ? value : "-", valueFont));
//	    vcell.setBorder(PdfPCell.NO_BORDER);
//	    vcell.setPadding(4f);
//	    table.addCell(vcell);
//	}


	 
//	public byte[] generateReportPdf(Integer lotId) {
//	    Optional<InspectionLot> optLot = inspectionLotRepo.findById(lotId);
//	    if (optLot.isEmpty()) {
//	        throw new GenericNotFoundException("Lot not found with id: " + lotId);
//	    }
//	 
//	    InspectionLot lot = optLot.get();
//	    Material material = lot.getMaterial();
//	    Vendor vendor = lot.getVendor();
//	    Plant plant = lot.getPlant();
//	    List<InspectionActuals> actuals = lot.getInspectionActuals();
//	    List<MaterialInspectionCharacteristics> characteristics = material != null ? material.getMaterialChar() : List.of();
//	 
//	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//	    Document document = new Document(PageSize.A4, 36, 36, 54, 54);
//	 
//	    try {
//	        PdfWriter writer = PdfWriter.getInstance(document, baos);
//	 
//	        // Add footer with page numbers
//	        writer.setPageEvent(new PdfPageEventHelper() {
//	            public void onEndPage(PdfWriter writer, Document document) {
//	                PdfContentByte cb = writer.getDirectContent();
//	                Phrase footer = new Phrase(String.format("Page %d", writer.getPageNumber()),
//	                        new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY));
//	                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
//	                        footer,
//	                        (document.right() - document.left()) / 2 + document.leftMargin(),
//	                        document.bottom() - 10, 0);
//	            }
//	        });
//	 
//	        document.open();
//	 
//	        // Fonts
//	        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(0, 51, 153));
//	        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
//	        Font normal = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
//	        Font normalBold = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
//	 
//	        // Add MIMS logo at top right
//	        Paragraph logo = new Paragraph("MIMS", new Font(Font.HELVETICA, 22, Font.BOLD, new Color(0, 102, 204)));
//	        logo.setAlignment(Element.ALIGN_RIGHT);
//	        document.add(logo);
//	 
//	        // Title
//	        Paragraph title = new Paragraph("Material Inspection Report", titleFont);
//	        title.setAlignment(Element.ALIGN_CENTER);
//	        title.setSpacingAfter(10f);
//	        document.add(title);
//	 
//	        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
//	 
//	        // Header info table
//	        PdfPTable infoTbl = new PdfPTable(2);
//	        infoTbl.setWidthPercentage(100);
//	        infoTbl.getDefaultCell().setBorderColor(new Color(0, 102, 204));
//	        infoTbl.getDefaultCell().setPadding(5);
//	 
//	        infoTbl.addCell(makeCell("Report Date", normalBold));
//	        infoTbl.addCell(makeCell(java.time.LocalDate.now().format(dtf), normal));
//	 
//	        infoTbl.addCell(makeCell("Inspection Lot ID", normalBold));
//	        infoTbl.addCell(makeCell(String.valueOf(lot.getLotId()), normal));
//	 
//	        infoTbl.addCell(makeCell("Material", normalBold));
//	        infoTbl.addCell(makeCell(material != null ? material.getMaterialDesc() : "-", normal));
//	 
//	        infoTbl.addCell(makeCell("Vendor", normalBold));
//	        infoTbl.addCell(makeCell(vendor != null ? vendor.getName() : "-", normal));
//	 
//	        infoTbl.addCell(makeCell("Plant", normalBold));
//	        infoTbl.addCell(makeCell(plant != null ? plant.getPlantName() : "-", normal));
//	 
//	        infoTbl.addCell(makeCell("Final Decision", normalBold));
//	        infoTbl.addCell(makeCell(lot.getResult() != null ? lot.getResult() : "-", normal));
//	 
//	        document.add(infoTbl);
//	 
//	        document.add(Chunk.NEWLINE);
//	 
//	        // Remarks table
//	        PdfPTable remarksTbl = new PdfPTable(1);
//	        remarksTbl.setWidthPercentage(100);
//	        PdfPCell rhead = new PdfPCell(new Phrase("Remarks", headerFont));
//	        rhead.setBackgroundColor(new Color(0, 102, 204));
//	        rhead.setHorizontalAlignment(Element.ALIGN_CENTER);
//	        rhead.setPadding(6);
//	        rhead.setBorderColor(new Color(0, 102, 204));
//	        remarksTbl.addCell(rhead);
//	        PdfPCell rbody = new PdfPCell(new Phrase(lot.getRemarks() != null ? lot.getRemarks() : "-", normal));
//	        rbody.setPadding(5);
//	        rbody.setBorderColor(new Color(0, 102, 204));
//	        remarksTbl.addCell(rbody);
//	        document.add(remarksTbl);
//	 
//	        document.add(Chunk.NEWLINE);
//	 
//	        // Inspection characteristics table
//	        PdfPTable charTbl = new PdfPTable(8);
//	        charTbl.setWidthPercentage(100);
//	        charTbl.setWidths(new float[]{0.8f, 3f, 1f, 1.2f, 1.2f, 2.2f, 1f, 1.5f});
//	        charTbl.getDefaultCell().setBorderColor(new Color(0, 102, 204));
//	 
//	        String[] headers = {"#", "Characteristic", "UOM", "Lower", "Upper", "Actuals", "Result", "Remarks"};
//	        for (String h : headers) {
//	            PdfPCell c = new PdfPCell(new Phrase(h, normalBold));
//	            c.setBackgroundColor(new Color(0, 102, 204));
//	            c.setHorizontalAlignment(Element.ALIGN_CENTER);
//	            c.setPadding(5);
//	            c.setBorderColor(new Color(0, 102, 204));
//	            c.setPhrase(new Phrase(h, new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE)));
//	            charTbl.addCell(c);
//	        }
//	 
//	        int idx = 1;
//	        for (MaterialInspectionCharacteristics ch : characteristics) {
//	            String uom = ch.getUnitOfMeasure();
//	            Double lower = ch.getLowerToleranceLimit();
//	            Double upper = ch.getUpperToleranceLimit();
//	 
//	            // Find all matching actuals
//	            StringBuilder actualValues = new StringBuilder();
//	            String result = "NOT RECORDED";
//	            if (actuals != null) {
//	                for (InspectionActuals a : actuals) {
//	                    if (a.getMaterialInspectionCharacteristics() != null &&
//	                            a.getMaterialInspectionCharacteristics().getCharacteristicId()
//	                                    .equals(ch.getCharacteristicId())) {
//	                        if (a.getMinimumMeasurement() != null)
//	                            actualValues.append(a.getMinimumMeasurement()).append(", ");
//	                        if (a.getMaximumMeasurement() != null)
//	                            actualValues.append(a.getMaximumMeasurement()).append(", ");
//	                    }
//	                }
//	            }
//	 
//	            String actualStr = actualValues.length() > 0
//	                    ? actualValues.substring(0, actualValues.length() - 2)
//	                    : "-";
//	 
//	            // Determine result
//	            String overallResult = "N/A";
//	            try {
//	                for (String valStr : actualStr.split(",")) {
//	                    double val = Double.parseDouble(valStr.trim());
//	                    if (val >= lower && val <= upper) overallResult = "PASS";
//	                    else overallResult = "FAIL";
//	                }
//	            } catch (Exception e) { }
//	 
//	            // Fill table
//	            charTbl.addCell(makeCell(String.valueOf(idx++), normal));
//	            charTbl.addCell(makeCell(ch.getCharacteristicDescription(), normal));
//	            charTbl.addCell(makeCell(uom != null ? uom : "-", normal));
//	            charTbl.addCell(makeCell(lower != null ? String.valueOf(lower) : "-", normal));
//	            charTbl.addCell(makeCell(upper != null ? String.valueOf(upper) : "-", normal));
//	            charTbl.addCell(makeCell(actualStr, normal));
//	 
//	            PdfPCell resultCell = new PdfPCell(new Phrase(overallResult, normalBold));
//	            if ("PASS".equalsIgnoreCase(overallResult))
//	                resultCell.setBackgroundColor(new Color(200, 255, 200));
//	            else if ("FAIL".equalsIgnoreCase(overallResult))
//	                resultCell.setBackgroundColor(new Color(255, 200, 200));
//	            else resultCell.setBackgroundColor(Color.WHITE);
//	            resultCell.setBorderColor(new Color(0, 102, 204));
//	            charTbl.addCell(resultCell);
//	 
//	            charTbl.addCell(makeCell(lot.getRemarks() != null ? lot.getRemarks() : "-", normal));
//	        }
//	 
//	        document.add(charTbl);
//	 
//	        document.add(Chunk.NEWLINE);
//	 
//	        // Authorization section
//	        Paragraph authHeader = new Paragraph("Authorized By", headerFont);
//	        document.add(authHeader);
//	        document.add(new Paragraph("Inspector: " + (lot.getCreatedBy() != null ? lot.getCreatedBy() : "-"), normal));
//	        document.add(new Paragraph("Signature: ______________________", normal));
//	        document.add(new Paragraph("Date: " + java.time.LocalDate.now().format(dtf), normal));
//	 
//	        document.add(Chunk.NEWLINE);
//	 
//	        // Seal (Official Stamp)
//	        PdfContentByte canvas = writer.getDirectContentUnder();
//	        float x = document.right() - 150;
//	        float y = document.bottom() + 120;
//	        canvas.setColorStroke(new Color(0, 0, 128));
//	        canvas.setLineWidth(2f);
//	        canvas.circle(x, y, 50);
//	        canvas.stroke();
//	 
//	        canvas.beginText();
//	        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, false);
//	        canvas.setFontAndSize(bf, 10);
//	        canvas.setColorFill(new Color(0, 0, 128));
//	        canvas.showTextAligned(Element.ALIGN_CENTER,
//	                "AUTHORIZED BY " + (lot.getCreatedBy() != null ? lot.getCreatedBy().toUpperCase() : "INSPECTOR"),
//	                x, y, 25);
//	        canvas.endText();
//	 
//	        document.add(new Paragraph("\nGenerated by Material Inspection Module System", new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY)));
//	 
//	        document.close();
//	        return baos.toByteArray();
//	 
//	    } catch (Exception e) {
//	        throw new RuntimeException("Error generating report: " + e.getMessage());
//	    }
//	}
//	 
//	// helper
//	private PdfPCell makeCell(String text, Font font) {
//	    PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", font));
//	    cell.setPadding(5);
//	    cell.setBorderColor(new Color(0, 102, 204));
//	    return cell;
//
//	}}	 

	 
	public byte[] generateReportPdf(Integer lotId) {
	    Optional<InspectionLot> optLot = inspectionLotRepo.findById(lotId);
	    if (optLot.isEmpty()) {
	        throw new GenericNotFoundException("Lot not found with id: " + lotId);
	    }
	 
	    InspectionLot lot = optLot.get();
	    Material material = lot.getMaterial();
	    Vendor vendor = lot.getVendor();
	    Plant plant = lot.getPlant();
	    List<InspectionActuals> actuals = lot.getInspectionActuals();
	    List<MaterialInspectionCharacteristics> characteristics =
	            material != null ? material.getMaterialChar() : List.of();
	 
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    Document document = new Document(PageSize.A4, 36, 36, 54, 54);
	 
	    try {
	        PdfWriter writer = PdfWriter.getInstance(document, baos);
	 
	        // Footer with page numbers
	        writer.setPageEvent(new PdfPageEventHelper() {
	            public void onEndPage(PdfWriter writer, Document document) {
	                PdfContentByte cb = writer.getDirectContent();
	                Phrase footer = new Phrase("Page " + writer.getPageNumber(),
	                        new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY));
	                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
	                        footer,
	                        (document.right() - document.left()) / 2 + document.leftMargin(),
	                        document.bottom() - 10, 0);
	            }
	        });
	 
	        document.open();
	 
	        // Fonts
	        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(0, 51, 153));
	        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
	        Font normal = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
	        Font normalBold = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
	 
	        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
	 
	        // Logo (MIMS)
	        Paragraph logo = new Paragraph("MIMS", new Font(Font.HELVETICA, 22, Font.BOLD, new Color(0, 102, 204)));
	        logo.setAlignment(Element.ALIGN_RIGHT);
	        document.add(logo);
	 
	        // Title
	        Paragraph title = new Paragraph("MATERIAL INSPECTION REPORT", titleFont);
	        title.setAlignment(Element.ALIGN_CENTER);
	        title.setSpacingAfter(10f);
	        document.add(title);
	 
	        // Header Info Table
	        PdfPTable infoTbl = new PdfPTable(2);
	        infoTbl.setWidthPercentage(100);
	        infoTbl.getDefaultCell().setBorderColor(new Color(0, 102, 204));
	        infoTbl.getDefaultCell().setPadding(5);
	 
	        infoTbl.addCell(makeCell("Report Date", normalBold));
	        infoTbl.addCell(makeCell(java.time.LocalDate.now().format(dtf), normal));
	 
	        infoTbl.addCell(makeCell("Inspection Lot ID", normalBold));
	        infoTbl.addCell(makeCell(String.valueOf(lot.getLotId()), normal));
	 
	        infoTbl.addCell(makeCell("Material (ID - Name)", normalBold));
	        infoTbl.addCell(makeCell(material != null ?
	                material.getMaterialId() + " - " + material.getMaterialDesc() : "-", normal));
	 
	        infoTbl.addCell(makeCell("Vendor (ID - Name)", normalBold));
	        infoTbl.addCell(makeCell(vendor != null ?
	                vendor.getVendorId() + " - " + vendor.getName() : "-", normal));
	 
	        infoTbl.addCell(makeCell("Plant (ID - Name)", normalBold));
	        infoTbl.addCell(makeCell(plant != null ?
	                plant.getPlantId() + " - " + plant.getPlantName() : "-", normal));
	 
	        infoTbl.addCell(makeCell("Final Decision", normalBold));
	        infoTbl.addCell(makeCell(lot.getResult() != null ? lot.getResult() : "-", normal));
	 
	        infoTbl.addCell(makeCell("Created Date", normalBold));
	        infoTbl.addCell(makeCell(lot.getCreationDate() != null ? lot.getCreationDate().format(dtf) : "-", normal));
	 
	        infoTbl.addCell(makeCell("Inspection Start Date", normalBold));
	        infoTbl.addCell(makeCell(lot.getInspectionStartDate() != null ? lot.getInspectionStartDate().format(dtf) : "-", normal));
	 
	        infoTbl.addCell(makeCell("Inspection End Date", normalBold));
	        infoTbl.addCell(makeCell(lot.getInspectionEndDate() != null ? lot.getInspectionEndDate().format(dtf) : "-", normal));
	 
	        document.add(infoTbl);
	        document.add(Chunk.NEWLINE);
	 
	        // Remarks Section
	        PdfPTable remarksTbl = new PdfPTable(1);
	        remarksTbl.setWidthPercentage(100);
	        PdfPCell rhead = new PdfPCell(new Phrase("Remarks", headerFont));
	        rhead.setBackgroundColor(new Color(0, 102, 204));
	        rhead.setHorizontalAlignment(Element.ALIGN_CENTER);
	        rhead.setPadding(6);
	        rhead.setBorderColor(new Color(0, 102, 204));
	        remarksTbl.addCell(rhead);
	 
	        PdfPCell rbody = new PdfPCell(new Phrase(lot.getRemarks() != null ? lot.getRemarks() : "-", normal));
	        rbody.setPadding(5);
	        rbody.setBorderColor(new Color(0, 102, 204));
	        remarksTbl.addCell(rbody);
	        document.add(remarksTbl);
	 
	        document.add(Chunk.NEWLINE);
	 
	        // Report contents paragraph
	        Paragraph desc = new Paragraph(
	                "Report Contents: This inspection report captures the inspection lot metadata, vendor and material details, " +
	                        "the list of inspection characteristics (specification limits), actual measured values recorded during inspection, " +
	                        "a pass/fail result per characteristic, a summary (passed/failed counts), and authorization/remarks.",
	                new Font(Font.HELVETICA, 9, Font.ITALIC, Color.DARK_GRAY));
	        desc.setSpacingAfter(8f);
	        document.add(desc);
	 
	        // Characteristics Table
	        PdfPTable charTbl = new PdfPTable(8);
	        charTbl.setWidthPercentage(100);
	        charTbl.setWidths(new float[]{0.8f, 3f, 1f, 1.2f, 1.2f, 2.2f, 1f, 1.5f});
	        charTbl.getDefaultCell().setBorderColor(new Color(0, 102, 204));
	 
	        String[] headers = {"#", "Characteristic", "UOM", "Lower", "Upper", "Actuals", "Result", "Remarks"};
	        for (String h : headers) {
	            PdfPCell c = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE)));
	            c.setBackgroundColor(new Color(0, 102, 204));
	            c.setHorizontalAlignment(Element.ALIGN_CENTER);
	            c.setPadding(5);
	            c.setBorderColor(new Color(0, 102, 204));
	            charTbl.addCell(c);
	        }
	 
	        int idx = 1;
	        for (MaterialInspectionCharacteristics ch : characteristics) {
	            String uom = ch.getUnitOfMeasure();
	            Double lower = ch.getLowerToleranceLimit();
	            Double upper = ch.getUpperToleranceLimit();
	 
	            StringBuilder actualValues = new StringBuilder();
	            if (actuals != null) {
	                for (InspectionActuals a : actuals) {
	                    if (a.getMaterialInspectionCharacteristics() != null &&
	                            a.getMaterialInspectionCharacteristics().getCharacteristicId()
	                                    .equals(ch.getCharacteristicId())) {
	                        if (a.getMinimumMeasurement() != null)
	                            actualValues.append(a.getMinimumMeasurement()).append(", ");
	                        if (a.getMaximumMeasurement() != null)
	                            actualValues.append(a.getMaximumMeasurement()).append(", ");
	                    }
	                }
	            }
	 
	            String actualStr = actualValues.length() > 0
	                    ? actualValues.substring(0, actualValues.length() - 2)
	                    : "-";
	 
	            String overallResult = "N/A";
	            try {
	                for (String valStr : actualStr.split(",")) {
	                    double val = Double.parseDouble(valStr.trim());
	                    if (val >= lower && val <= upper) overallResult = "PASS";
	                    else overallResult = "FAIL";
	                }
	            } catch (Exception e) { }
	 
	            charTbl.addCell(makeCell(String.valueOf(idx++), normal));
	            charTbl.addCell(makeCell(ch.getCharacteristicDescription(), normal));
	            charTbl.addCell(makeCell(uom != null ? uom : "-", normal));
	            charTbl.addCell(makeCell(lower != null ? String.valueOf(lower) : "-", normal));
	            charTbl.addCell(makeCell(upper != null ? String.valueOf(upper) : "-", normal));
	            charTbl.addCell(makeCell(actualStr, normal));
	 
	            PdfPCell resultCell = new PdfPCell(new Phrase(overallResult, normalBold));
	            if ("PASS".equalsIgnoreCase(overallResult))
	                resultCell.setBackgroundColor(new Color(200, 255, 200));
	            else if ("FAIL".equalsIgnoreCase(overallResult))
	                resultCell.setBackgroundColor(new Color(255, 200, 200));
	            else resultCell.setBackgroundColor(Color.WHITE);
	            resultCell.setBorderColor(new Color(0, 102, 204));
	            charTbl.addCell(resultCell);
	 
	            charTbl.addCell(makeCell(lot.getRemarks() != null ? lot.getRemarks() : "-", normal));
	        }
	 
	        document.add(charTbl);
	        document.add(Chunk.NEWLINE);
	 
	        // Authorized Section
	        Paragraph authHeader = new Paragraph("Authorized By", headerFont);
	        document.add(authHeader);
	        document.add(new Paragraph("Inspector: " + (lot.getCreatedBy() != null ? lot.getCreatedBy() : "-"), normal));
	        document.add(new Paragraph("Signature: ______________________", normal));
	        document.add(new Paragraph("Date: " + java.time.LocalDate.now().format(dtf), normal));
	 
	        // Draw Circular Seal
	        PdfContentByte canvas = writer.getDirectContentUnder();
	        float x = document.right() - 120;
	        float y = document.bottom() + 120;
	        canvas.setColorStroke(new Color(0, 0, 128));
	        canvas.setLineWidth(2f);
	        canvas.circle(x, y, 55);
	        canvas.stroke();
	 
	        canvas.beginText();
	        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, false);
	        canvas.setFontAndSize(bf, 10);
	        canvas.setColorFill(new Color(0, 0, 128));
	        canvas.showTextAligned(Element.ALIGN_CENTER, "MIM PVT LTD", x, y + 30, 0);
	        canvas.showTextAligned(Element.ALIGN_CENTER, "Muddanur, Kadapa", x, y + 15, 0);
	        canvas.showTextAligned(Element.ALIGN_CENTER, "Andhra Pradesh - 516380", x, y, 0);
	        canvas.showTextAligned(Element.ALIGN_CENTER, "Authorized By: " +
	                (lot.getCreatedBy() != null ? lot.getCreatedBy().toUpperCase() : "INSPECTOR"), x, y - 20, 0);
	        canvas.endText();
	 
	        document.add(new Paragraph("\nGenerated by Material Inspection Module System",
	                new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY)));
	 
	        document.close();
	        return baos.toByteArray();
	 
	    } catch (Exception e) {
	        throw new RuntimeException("Error generating report: " + e.getMessage());
	    }
	}
	 
	// Helper
	private PdfPCell makeCell(String text, Font font) {
	    PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", font));
	    cell.setPadding(5);
	    cell.setBorderColor(new Color(0, 102, 204));
	    return cell;

	}}	

	 
