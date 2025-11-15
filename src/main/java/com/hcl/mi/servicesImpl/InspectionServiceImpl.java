package com.hcl.mi.servicesImpl;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.hcl.mi.entities.InspectionActuals;
import com.hcl.mi.entities.InspectionLot;
import com.hcl.mi.entities.Material;
import com.hcl.mi.entities.MaterialInspectionCharacteristics;
import com.hcl.mi.entities.Plant;
import com.hcl.mi.entities.Vendor;
import com.hcl.mi.exceptions.GenericAlreadyExistsException;
import com.hcl.mi.exceptions.GenericNotFoundException;
import com.hcl.mi.helper.InspectionLotSpecifications;
import com.hcl.mi.helper.Transformers;
import com.hcl.mi.mapper.InspectionLotMapper;
import com.hcl.mi.mapper.MaterialInspectionCharacteristicsMapper;
import com.hcl.mi.repositories.InspectionActualsRepository;
import com.hcl.mi.repositories.InspectionLotRepository;
import com.hcl.mi.repositories.MaterialRepository;
import com.hcl.mi.repositories.PlantRepository;
import com.hcl.mi.repositories.VendorRepository;
import com.hcl.mi.requestdtos.DateRangeLotSearch;
import com.hcl.mi.requestdtos.EditLotDto;
import com.hcl.mi.requestdtos.LotActualDto;
import com.hcl.mi.requestdtos.LotCreationDto;
import com.hcl.mi.responsedtos.DateRangeLotResponseDto;
import com.hcl.mi.responsedtos.InspectionLotDto;
import com.hcl.mi.responsedtos.LotActualsAndCharacteristicsResponseDto;
import com.hcl.mi.responsedtos.MaterialInspectionCharacteristicsDto;
import com.hcl.mi.services.InspectionService;
import com.hcl.mi.utils.ApplicationConstants;
import com.hcl.mi.utils.StringUtil;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InspectionServiceImpl implements InspectionService {

	@Value("${date-range}")
	private long DATE_RANGE;

	private InspectionLotRepository inspectionLotRepo;

	private InspectionActualsRepository inspectionActRepo;
	
	private final MaterialRepository materialRepository;
	private final VendorRepository vendorRepository;
	private final PlantRepository plantRepository;
	
 
	public InspectionServiceImpl(InspectionLotRepository inspectionLotRepo,
			InspectionActualsRepository inspectionActRepo, VendorRepository vendorRepository,
			PlantRepository plantRepository, MaterialRepository materialRepository
			) {
		this.inspectionLotRepo = inspectionLotRepo;
		this.inspectionActRepo = inspectionActRepo;
		this.vendorRepository = vendorRepository; 
		this.plantRepository = plantRepository;
		this.materialRepository = materialRepository;
	}

	@Override 
	public InspectionLotDto getLotDetails(Integer id) {
		Optional<InspectionLot> optInsp = inspectionLotRepo.findById(id);

		if (optInsp.isPresent()) {

			log.info("Finding lot with id is success : {}", id);

			 InspectionLotDto lot = InspectionLotMapper.convertEntityToDto(optInsp.get());

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

		log.info("Getting lot charactesristics and actuals of lot id : {}", id);

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

		log.info("Arrenging lot characteristics and actuals together of lot id : {}", id);

		for (int start = 0; start < list.size(); start++) {

			Integer charId = list.get(start).getCharacteristicId();

			for (int act = 0; act < actuals.size(); act++) {

				if (actuals.get(act).getMaterialInspectionCharacteristics().getCharacteristicId() == charId) {

					list.get(start).setActualUtl(actuals.get(act).getMaximumMeasurement());

					list.get(start).setActualLtl(actuals.get(act).getMinimumMeasurement());
				}
			}
		}
 
		log.info("returnig Lot Actuals and characteristics as a list");
		return list;
	}

	@Override
	public List<InspectionLotDto> getAllLotsWhoseInspectionActualNeedToAdded() {

		log.info("getting all lots");

		List<InspectionLot> lots = inspectionLotRepo.findAll();

		List<InspectionLot> responseList = new LinkedList<>();

		for (InspectionLot lot : lots) {

			if (lot.getMaterial().getMaterialChar().size() != lot.getInspectionActuals().size()) {

				log.info("adding lots those have not done all inspection actuals");

				responseList.add(lot);
			} 
		}
 
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

	    log.info("New inspection actuals saved for lot ID: {}", actualsDto.getLotId());

	    boolean hasFailures = false;
	    List<String> failedCharacteristics = new ArrayList<>();

	    if (totalReqChar.size() == lot.getInspectionActuals().size()) {

	        log.info("Evaluating lot for MARKING APPROVAL");

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

	                log.info("MARKING APPROVAL rejected due to failed characteristic: {}", reqChar.getCharacteristicDescription());
	            }
	        }

	        if (!hasFailures) { 
	            lot.setResult(ApplicationConstants.LOT_PASS_STATUS);
	            lot.setRemarks("No remarks");
	            lot.setInspectionEndDate(LocalDate.now());
	            log.info("Lot marked for approval: {}", lot.getLotId());
	        } else {
	            String matrDesc = lot.getMaterial().getMaterialDesc();
	            String failedChars = String.join(", ", failedCharacteristics);
	            lot.setResult(ApplicationConstants.LOT_INSPECTION_STATUS);
	            lot.setRemarks(matrDesc + " characteristics failed: " + failedChars);
	        }

	        inspectionLotRepo.save(lot);
	    }
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
 
		InspectionLot originalLot = optInsp.get();

		originalLot.setInspectionEndDate(lot.getDate());
		originalLot.setResult(StringUtil.removeExtraSpaces(lot.getResult()));

		originalLot.setRemarks(StringUtil.removeExtraSpaces(lot.getRemarks()));

		log.info("updating lot result is successfull of lot id : {}", lot.getId());
		inspectionLotRepo.save(originalLot);

	}
	

	@Override
	public void createInspectionLot(LotCreationDto lotDto) {

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

 	 @Override
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
	 
	        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(0, 51, 153));
	        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
	        Font normal = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
	        Font normalBold = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
	 
	        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
	 
	        Paragraph logo = new Paragraph("MIMS", new Font(Font.HELVETICA, 22, Font.BOLD, new Color(0, 102, 204)));
	        logo.setAlignment(Element.ALIGN_RIGHT);
	        document.add(logo);
	 
	        Paragraph title = new Paragraph("MATERIAL INSPECTION REPORT", titleFont);
	        title.setAlignment(Element.ALIGN_CENTER);
	        title.setSpacingAfter(10f);
	        document.add(title);
	 
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
	 
	        Paragraph desc = new Paragraph(
	                "Report Contents: This inspection report captures the inspection lot metadata, vendor and material details, " +
	                        "the list of inspection characteristics (specification limits), actual measured values recorded during inspection, " +
	                        "a pass/fail result per characteristic, a summary (passed/failed counts), and authorization/remarks.",
	                new Font(Font.HELVETICA, 9, Font.ITALIC, Color.DARK_GRAY));
	        desc.setSpacingAfter(8f);
	        document.add(desc);
	 
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
	            if ("PASS".equalsIgnoreCase(overallResult)) {
		            charTbl.addCell(makeCell("No remarks", normal));

	                resultCell.setBackgroundColor(new Color(200, 255, 200));
	            }
	                
	            else if ("FAIL".equalsIgnoreCase(overallResult))
	                resultCell.setBackgroundColor(new Color(255, 200, 200));
	            else resultCell.setBackgroundColor(Color.WHITE);
	            resultCell.setBorderColor(new Color(0, 102, 204));
	            charTbl.addCell(resultCell);
	 
	            charTbl.addCell(makeCell(lot.getRemarks() != null ? lot.getRemarks() : "-", normal));
	        }
	 
	        document.add(charTbl);
	        document.add(Chunk.NEWLINE);
	 
	        	        Paragraph authHeader = new Paragraph("Authorized By", headerFont);
	        document.add(authHeader);
	        document.add(new Paragraph("Inspector: " + (lot.getCreatedBy() != null ? lot.getCreatedBy() : "-"), normal));
	        document.add(new Paragraph("Signature: ______________________", normal));
	        document.add(new Paragraph("Date: " + java.time.LocalDate.now().format(dtf), normal));
	 
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
	 
		private PdfPCell makeCell(String text, Font font) {
	    PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", font));
	    cell.setPadding(5);
	    cell.setBorderColor(new Color(0, 102, 204));
	    return cell; 

	}
		


    @Override
    public List<DateRangeLotResponseDto> getAllLotsDetailsBetweenDateRange(DateRangeLotSearch obj) {

        if (!validateSearchDateRange(obj.getFromDate(), obj.getToDate())) {
            throw new RuntimeException(
                "Invalid date range for searching lots, Period should be: " + DATE_RANGE + " days range");
        }

        Specification<InspectionLot> spec =
            Specification.where(InspectionLotSpecifications.withinDate(obj.getFromDate(), obj.getToDate()))
                         .and(InspectionLotSpecifications.hasMaterialId(normalizeMaterialId(obj.getMaterialId())))
                         .and(InspectionLotSpecifications.hasPlantId(normalizePlantId(obj.getPlantId())))
                         .and(InspectionLotSpecifications.hasStatus(obj.getStatus()))
                         .and(InspectionLotSpecifications.hasVendorId(obj.getVendorId()));

        List<InspectionLot> lots = inspectionLotRepo.findAll(spec);

        List<DateRangeLotResponseDto> responseList =
            Transformers.ConvertInspectionLotListToDateRangeResponseDto(lots);

        log.info("Returning lots that meet filter criteria. Count: {}", responseList.size());
        return responseList;
    }


    private String normalizeMaterialId(String materialId) {
        if (materialId == null) return null;
        return materialId.replaceAll("\\s+", "").toUpperCase();
    }

    private String normalizePlantId(String plantId) {
        if (plantId == null) return null;
        return plantId.trim().toUpperCase();
    }

    @Override
	public List<InspectionLotDto> getAllInspectionLots() {
		return inspectionLotRepo.findAll()
				.stream()
				.map(lot -> InspectionLotMapper.convertEntityToDto(lot))
				.toList(); 
	}

	public List<InspectionLotDto> getAllPendingInspectionLots() {
		
		return inspectionLotRepo.findAllPendingNative()
				.stream()
				.map(lot-> InspectionLotMapper.convertEntityToDto(lot))
				.toList();
	}

	@Override
	public List<InspectionLotDto> getAllRejectedInspectionLots() {
		
		return inspectionLotRepo.findAllRejectedNative() 
				.stream()
				.map(lot-> InspectionLotMapper.convertEntityToDto(lot))
				.toList();
	}
	
	
	@Override
	public List<MaterialInspectionCharacteristicsDto> getListOfMaterialInspectionCharNeedToaddForLot(Integer lotId) {
	    InspectionLot lot = inspectionLotRepo.findById(lotId)
	            .orElseThrow(() -> new GenericNotFoundException("Lot not found with id: " + lotId));

	    List<MaterialInspectionCharacteristics> requiredChars =
	            new ArrayList<>(
	                Optional.ofNullable(lot.getMaterial())
	                        .map(Material::getMaterialChar)
	                        .orElse(Collections.emptyList())
	            );

	    List<InspectionActuals> actuals = inspectionActRepo.findAllByLotId(lotId);

	    Set<Integer> existingCharIds = actuals.stream()
	            .map(InspectionActuals::getMaterialInspectionCharacteristics)
	            .filter(Objects::nonNull)
	            .map(MaterialInspectionCharacteristics::getCharacteristicId)
	            .filter(Objects::nonNull)
	            .collect(Collectors.toSet());

	    List<MaterialInspectionCharacteristics> pendingChars = requiredChars.stream()
	            .filter(Objects::nonNull)
	            .filter(mc -> mc.getCharacteristicId() != null)
	            .filter(mc -> !existingCharIds.contains(mc.getCharacteristicId()))
	            .collect(Collectors.collectingAndThen(
	                    Collectors.toMap(
	                            MaterialInspectionCharacteristics::getCharacteristicId,
	                            mc -> mc,
	                            (a, b) -> a
	                    ),
	                    map -> new ArrayList<>(map.values())
	            ));

	    return pendingChars.stream()
	            .map(MaterialInspectionCharacteristicsMapper::convertEntityToDto)
	            .toList();
	}
	
 
}	

	   
