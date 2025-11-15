package com.hcl.mi.servicesImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import com.hcl.mi.entities.*;
import com.hcl.mi.exceptions.GenericAlreadyExistsException;
import com.hcl.mi.exceptions.GenericNotFoundException;
import com.hcl.mi.repositories.*;
import com.hcl.mi.requestdtos.DateRangeLotSearch;
import com.hcl.mi.requestdtos.EditLotDto;
import com.hcl.mi.requestdtos.LotActualDto;
import com.hcl.mi.requestdtos.LotCreationDto;
import com.hcl.mi.responsedtos.DateRangeLotResponseDto;
import com.hcl.mi.responsedtos.InspectionLotDto;
import com.hcl.mi.responsedtos.LotActualsAndCharacteristicsResponseDto;
import com.hcl.mi.utils.ApplicationConstants;

class InspectionServiceImplTest {

    @Mock
    private InspectionLotRepository inspectionLotRepo;

    @Mock
    private InspectionActualsRepository inspectionActRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private PlantRepository plantRepository;

    @InjectMocks
    private InspectionServiceImpl inspectionService;

    private Material material;
    private Plant plant;
    private Vendor vendor;
    private InspectionLot lot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(inspectionService, "DATE_RANGE", 30L);

        material = new Material();
        material.setMaterialId("MAT1");
        material.setMaterialDesc("STEEL ROD");
        material.setType("RAW");
        material.setStatus(true);
        material.setMaterialChar(new ArrayList<>());

        plant = new Plant();
        plant.setPlantId("PL1");
        plant.setPlantName("MAIN PLANT");
        plant.setCity("MUMBAI");
        plant.setState("MH");
        plant.setStatus(true);

        vendor = new Vendor();
        vendor.setVendorId(101);
        vendor.setName("ABC VENDOR");
        vendor.setStatus(true);

        lot = new InspectionLot();
        lot.setLotId(500);
        lot.setCreationDate(LocalDate.of(2025, 1, 1));
        lot.setInspectionStartDate(LocalDate.of(2025, 1, 2));
        lot.setInspectionEndDate(null);
        lot.setResult(null);
        lot.setRemarks(null);
        lot.setMaterial(material); 
        lot.setPlant(plant);
        lot.setVendor(vendor);
        lot.setInspectionActuals(new ArrayList<>());
    }

    @Test
    void testGetLotDetails_Success() {
        when(inspectionLotRepo.findById(500)).thenReturn(Optional.of(lot));

        InspectionLotDto dto = inspectionService.getLotDetails(500);

        assertEquals(500, dto.getLotId());
        assertEquals(LocalDate.of(2025, 1, 1), dto.getCreationDate());
        verify(inspectionLotRepo).findById(500);
    }

    @Test
    void testGetLotDetails_NotFound() {
        when(inspectionLotRepo.findById(404)).thenReturn(Optional.empty());

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> inspectionService.getLotDetails(404));

        assertEquals("Lot not found with id: 404", ex.getMessage());
        verify(inspectionLotRepo).findById(404);
    } 

    @Test
    void testGetActualAndOriginalOfLot_Success() {
        material.setMaterialChar(List.of(
                buildChar(1, "LENGTH", material, 10.0, 1.0, "mm"),
                buildChar(2, "WIDTH", material, 5.0, 1.0, "mm"),
                buildChar(3, "HEIGHT", material, 20.0, 10.0, "mm")
        ));
        InspectionActuals a2 = new InspectionActuals();
        a2.setMaterialInspectionCharacteristics(material.getMaterialChar().get(1));
        a2.setMinimumMeasurement(2.0);
        a2.setMaximumMeasurement(4.0);
        InspectionActuals a3 = new InspectionActuals();
        a3.setMaterialInspectionCharacteristics(material.getMaterialChar().get(2));
        a3.setMinimumMeasurement(12.0);
        a3.setMaximumMeasurement(18.0);
        lot.setInspectionActuals(List.of(a2, a3));

        when(inspectionLotRepo.findById(500)).thenReturn(Optional.of(lot));

        List<LotActualsAndCharacteristicsResponseDto> out = inspectionService.getActualAndOriginalOfLot(500);

        assertEquals(3, out.size(), "Should include an entry for each characteristic");
        Map<Integer, LotActualsAndCharacteristicsResponseDto> map = out.stream()
                .collect(java.util.stream.Collectors.toMap(LotActualsAndCharacteristicsResponseDto::getCharacteristicId, x -> x));
        assertNull(map.get(1).getActualLtl());
        assertEquals(2.0, map.get(2).getActualLtl());
        assertEquals(12.0, map.get(3).getActualLtl());
        verify(inspectionLotRepo).findById(500);
    }

    @Test
    void testGetActualAndOriginalOfLot_NotFound() {
        when(inspectionLotRepo.findById(999)).thenReturn(Optional.empty());

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> inspectionService.getActualAndOriginalOfLot(999));

        assertEquals("Lot not found with id: 999", ex.getMessage());
        verify(inspectionLotRepo).findById(999);
    }

    @Test
    void testGetAllInspectionLots_FiltersIncomplete() {
        Material ma = copyMaterial("MA", "DESC A");
        MaterialInspectionCharacteristics cA1 = buildChar(1, "A1", ma, 10.0, 1.0, "mm");
        MaterialInspectionCharacteristics cA2 = buildChar(2, "A2", ma, 10.0, 1.0, "mm");
        ma.setMaterialChar(new ArrayList<>(List.of(cA1, cA2)));
        InspectionLot lotA = copyLot(1001, ma, plant, vendor);
        lotA.getInspectionActuals().add(new InspectionActuals());

        Material mb = copyMaterial("MB", "DESC B");
        MaterialInspectionCharacteristics cB1 = buildChar(3, "B1", mb, 10.0, 1.0, "mm");
        MaterialInspectionCharacteristics cB2 = buildChar(4, "B2", mb, 10.0, 1.0, "mm");
        mb.setMaterialChar(new ArrayList<>(List.of(cB1, cB2)));
        InspectionLot lotB = copyLot(1002, mb, plant, vendor);
        lotB.getInspectionActuals().add(new InspectionActuals());
        lotB.getInspectionActuals().add(new InspectionActuals());

        when(inspectionLotRepo.findAll()).thenReturn(List.of(lotA, lotB));

        List<InspectionLotDto> result = inspectionService.getAllInspectionLots();

        assertEquals(1, result.size());
        assertEquals(1001, result.get(0).getLotId());
        verify(inspectionLotRepo).findAll();
    }

    @Test
    void testSaveInspActuals_Success_AddsActualWithoutCompletion() {
        MaterialInspectionCharacteristics ch1 = buildChar(1, "LEN", material, 10.0, 1.0, "mm");
        MaterialInspectionCharacteristics ch2 = buildChar(2, "WID", material, 5.0, 1.0, "mm");
        material.setMaterialChar(new ArrayList<>(List.of(ch1, ch2)));
        lot.setInspectionActuals(new ArrayList<>());

        when(inspectionLotRepo.findById(500)).thenReturn(Optional.of(lot));
        when(inspectionActRepo.findByInspectionLotAndmaterialInspectionCharacteristics(500, 1)).thenReturn(null);

        LotActualDto dto = new LotActualDto();
        dto.setLotId(500);
        dto.setCharId(1);
        dto.setMaxMeas(4.0);
        dto.setMinMeas(2.0);

        inspectionService.saveInspActuals(dto);

        assertEquals(1, lot.getInspectionActuals().size());
        assertNull(lot.getResult()); 
        verify(inspectionLotRepo, atLeastOnce()).save(lot);
    }

    @Test
    void testSaveInspActuals_Success_CompletesAndPasses() {
        MaterialInspectionCharacteristics ch1 = buildChar(1, "LEN", material, 10.0, 1.0, "mm");
        material.setMaterialChar(new ArrayList<>(List.of(ch1)));
        lot.setInspectionActuals(new ArrayList<>());

        when(inspectionLotRepo.findById(500)).thenReturn(Optional.of(lot));
        when(inspectionActRepo.findByInspectionLotAndmaterialInspectionCharacteristics(500, 1)).thenReturn(null);

        LotActualDto dto = new LotActualDto();
        dto.setLotId(500);
        dto.setCharId(1);
        dto.setMaxMeas(8.0);
        dto.setMinMeas(2.0); 

        inspectionService.saveInspActuals(dto);

        assertEquals(ApplicationConstants.LOT_PASS_STATUS, lot.getResult());
        assertEquals("No remarks", lot.getRemarks());
        assertEquals(LocalDate.now(), lot.getInspectionEndDate());
        verify(inspectionLotRepo, atLeastOnce()).save(lot);
    }

    @Test
    void testSaveInspActuals_Success_CompletesAndFails_WithRemarks() {
        material.setMaterialDesc("ALUMINIUM BAR");
        MaterialInspectionCharacteristics ch1 = buildChar(1, "LENGTH", material, 10.0, 1.0, "mm");
        material.setMaterialChar(new ArrayList<>(List.of(ch1)));
        lot.setInspectionActuals(new ArrayList<>());

        when(inspectionLotRepo.findById(500)).thenReturn(Optional.of(lot));
        when(inspectionActRepo.findByInspectionLotAndmaterialInspectionCharacteristics(500, 1)).thenReturn(null);

        LotActualDto dto = new LotActualDto();
        dto.setLotId(500);
        dto.setCharId(1);
        dto.setMaxMeas(11.0);
        dto.setMinMeas(0.5);  

        inspectionService.saveInspActuals(dto);

        assertEquals(ApplicationConstants.LOT_INSPECTION_STATUS, lot.getResult());
        assertTrue(lot.getRemarks().startsWith("ALUMINIUM BAR characteristics failed:"));
        assertTrue(lot.getRemarks().contains("LENGTH"));
        verify(inspectionLotRepo, atLeastOnce()).save(lot);
    }

    @Test
    void testSaveInspActuals_DuplicateCharacteristic() {
        when(inspectionLotRepo.findById(500)).thenReturn(Optional.of(lot));
        when(inspectionActRepo.findByInspectionLotAndmaterialInspectionCharacteristics(500, 1))
                .thenReturn(new InspectionActuals());

        LotActualDto dto = new LotActualDto();
        dto.setLotId(500);
        dto.setCharId(1);
        dto.setMaxMeas(8.0);
        dto.setMinMeas(2.0);

        GenericAlreadyExistsException ex = assertThrows(GenericAlreadyExistsException.class,
                () -> inspectionService.saveInspActuals(dto));

        assertEquals("Characteristic already exists for this lot.", ex.getMessage());
        verify(inspectionLotRepo, never()).save(any());
    }

    @Test
    void testSaveInspActuals_LotNotFound() {
        when(inspectionLotRepo.findById(404)).thenReturn(Optional.empty());

        LotActualDto dto = new LotActualDto();
        dto.setLotId(404);
        dto.setCharId(1);
        dto.setMaxMeas(8.0);
        dto.setMinMeas(2.0);

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> inspectionService.saveInspActuals(dto));

        assertEquals("Lot not found with id: 404", ex.getMessage());
        verify(inspectionLotRepo, never()).save(any());
    }

    @Test
    void testGetAllLotsDetailsBetweenDateRange_Success_NoFilters() {
        DateRangeLotSearch search = new DateRangeLotSearch(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 10),
                null, 0, null, null
        );

        when(inspectionLotRepo.findAllBycreationDateBetween(search.getFromDate(), search.getToDate()))
                .thenReturn(List.of(lot));

        List<DateRangeLotResponseDto> out = inspectionService.getAllLotsDetailsBetweenDateRange(search);

        assertEquals(1, out.size());
        DateRangeLotResponseDto first = out.get(0);
        assertEquals(500, first.getLotId());
        assertEquals("STEEL ROD", first.getMaterial());
        assertEquals("MAIN PLANT", first.getPlant());
        assertEquals("ABC VENDOR", first.getVendor());
        verify(inspectionLotRepo).findAllBycreationDateBetween(search.getFromDate(), search.getToDate());
    }

    @Test
    void testGetAllLotsDetailsBetweenDateRange_Success_WithFilters() {
        InspectionLot lot1 = copyLot(600, copyMaterial("MATX", "ITEM-X"), copyPlant("PLX", "PLANT-X"), copyVendor(201, "VEN-X"));
        lot1.setResult("PASS");
        InspectionLot lot2 = copyLot(601, copyMaterial("MATY", "ITEM-Y"), copyPlant("PLY", "PLANT-Y"), copyVendor(202, "VEN-Y"));
        lot2.setResult("FAIL");

        DateRangeLotSearch search = new DateRangeLotSearch(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 30),
                " mat x ",   
                201,         
                "  plx ",    
                "PASS"       
        );

        when(inspectionLotRepo.findAllBycreationDateBetween(search.getFromDate(), search.getToDate()))
                .thenReturn(List.of(lot1, lot2));

        List<DateRangeLotResponseDto> out = inspectionService.getAllLotsDetailsBetweenDateRange(search);

        assertEquals(1, out.size());
        assertEquals(600, out.get(0).getLotId());
    }

    @Test
    void testGetAllLotsDetailsBetweenDateRange_InvalidRange_Throws() {
        ReflectionTestUtils.setField(inspectionService, "DATE_RANGE", 5L);

        DateRangeLotSearch search = new DateRangeLotSearch(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 20),
                null, 0, null, null
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> inspectionService.getAllLotsDetailsBetweenDateRange(search));

        assertEquals("Invalid date range for searching lots, Period should be : 5 days range", ex.getMessage());
        verify(inspectionLotRepo, never()).findAllBycreationDateBetween(any(), any());
    }

    @Test
    void testUpdateInspectionLot_Success() {
        when(inspectionLotRepo.findById(500)).thenReturn(Optional.of(lot));

        EditLotDto edit = mock(EditLotDto.class);
        when(edit.getId()).thenReturn(500);
        when(edit.getDate()).thenReturn(LocalDate.of(2025, 2, 1));
        when(edit.getResult()).thenReturn("  PASS  ");
        when(edit.getRemarks()).thenReturn("  OK   ");

        inspectionService.updateInspectionLot(edit);

        assertEquals(LocalDate.of(2025, 2, 1), lot.getInspectionEndDate());
        assertEquals("PASS", lot.getResult());   
        assertEquals("OK", lot.getRemarks());     
        verify(inspectionLotRepo).save(lot);
    }

    @Test
    void testUpdateInspectionLot_NotFound() {
        when(inspectionLotRepo.findById(404)).thenReturn(Optional.empty());

        EditLotDto edit = mock(EditLotDto.class);
        when(edit.getId()).thenReturn(404);

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> inspectionService.updateInspectionLot(edit));

        assertEquals("Lot not found with id: 404", ex.getMessage());
        verify(inspectionLotRepo, never()).save(any());
    }

    @Test
    void testCreateInspectionLot_Success() {
        when(materialRepository.findById("MAT1")).thenReturn(Optional.of(material));
        when(plantRepository.findById("PL1")).thenReturn(Optional.of(plant));
        when(vendorRepository.findById(101)).thenReturn(Optional.of(vendor));

        LotCreationDto dto = new LotCreationDto();
        dto.setMatId("  M A T 1 ");
        dto.setPlantId("  P L 1 ");
        dto.setVendorId(101);
        dto.setCrDt(LocalDate.of(2025, 3, 1));
        dto.setStDt(LocalDate.of(2025, 3, 2));

        ArgumentCaptor<InspectionLot> captor = ArgumentCaptor.forClass(InspectionLot.class);

        inspectionService.createInspectionLot(dto);

        verify(inspectionLotRepo).save(captor.capture());
        InspectionLot saved = captor.getValue();

        assertEquals(ApplicationConstants.LOT_INSPECTION_STATUS, saved.getResult());
        assertEquals(LocalDate.of(2025, 3, 1), saved.getCreationDate());
        assertEquals(LocalDate.of(2025, 3, 2), saved.getInspectionStartDate());
        assertEquals("MAT1", saved.getMaterial().getMaterialId());
        assertEquals("PL1", saved.getPlant().getPlantId());
        assertEquals(101, saved.getVendor().getVendorId());
    }

    @Test
    void testCreateInspectionLot_MaterialNotFound() {
        when(materialRepository.findById("MATX")).thenReturn(Optional.empty());

        LotCreationDto dto = new LotCreationDto();
        dto.setMatId("MATX");
        dto.setPlantId("PL1");
        dto.setVendorId(101);
        dto.setCrDt(LocalDate.now());
        dto.setStDt(LocalDate.now());

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> inspectionService.createInspectionLot(dto));

        assertEquals("Material not found", ex.getMessage());
        verify(inspectionLotRepo, never()).save(any());
    }

    @Test
    void testCreateInspectionLot_PlantNotFound() {
        when(materialRepository.findById("MAT1")).thenReturn(Optional.of(material));
        when(plantRepository.findById("PLX")).thenReturn(Optional.empty());

        LotCreationDto dto = new LotCreationDto();
        dto.setMatId("MAT1");
        dto.setPlantId("PLX");
        dto.setVendorId(101);
        dto.setCrDt(LocalDate.now());
        dto.setStDt(LocalDate.now());

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> inspectionService.createInspectionLot(dto));

        assertEquals("Plant not found", ex.getMessage());
        verify(inspectionLotRepo, never()).save(any());
    }

    @Test
    void testCreateInspectionLot_VendorNotFound() {
        when(materialRepository.findById("MAT1")).thenReturn(Optional.of(material));
        when(plantRepository.findById("PL1")).thenReturn(Optional.of(plant));
        when(vendorRepository.findById(999)).thenReturn(Optional.empty());

        LotCreationDto dto = new LotCreationDto();
        dto.setMatId("MAT1");
        dto.setPlantId("PL1");
        dto.setVendorId(999);
        dto.setCrDt(LocalDate.now());
        dto.setStDt(LocalDate.now());

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> inspectionService.createInspectionLot(dto));

        assertEquals("Vendor not found", ex.getMessage());
        verify(inspectionLotRepo, never()).save(any());
    }

    private MaterialInspectionCharacteristics buildChar(int id, String desc, Material m,
                                                        Double utl, Double ltl, String uom) {
        MaterialInspectionCharacteristics c = new MaterialInspectionCharacteristics();
        c.setCharacteristicId(id);
        c.setCharacteristicDescription(desc);
        c.setUpperToleranceLimit(utl);
        c.setLowerToleranceLimit(ltl);
        c.setUnitOfMeasure(uom);
        c.setMaterial(m);
        return c;
    }

    private Material copyMaterial(String id, String desc) {
        Material m = new Material();
        m.setMaterialId(id);
        m.setMaterialDesc(desc);
        m.setType("T");
        m.setStatus(true);
        m.setMaterialChar(new ArrayList<>());
        return m;
    }

    private Plant copyPlant(String id, String name) {
        Plant p = new Plant();
        p.setPlantId(id);
        p.setPlantName(name);
        p.setStatus(true);
        return p;
    }

    private Vendor copyVendor(int id, String name) {
        Vendor v = new Vendor();
        v.setVendorId(id);
        v.setName(name);
        v.setStatus(true);
        return v;
    }

    private InspectionLot copyLot(int id, Material m, Plant p, Vendor v) {
        InspectionLot l = new InspectionLot();
        l.setLotId(id);
        l.setCreationDate(LocalDate.of(2025, 1, 1));
        l.setInspectionStartDate(LocalDate.of(2025, 1, 2));
        l.setMaterial(m);
        l.setPlant(p);
        l.setVendor(v); 
        l.setInspectionActuals(new ArrayList<>());
        return l;
    }
    

        @Test
        void testGenerateReportPdf_NotFound() {
            InspectionLotRepository inspectionLotRepo = mock(InspectionLotRepository.class);
            InspectionActualsRepository inspectionActRepo = mock(InspectionActualsRepository.class);
            MaterialRepository materialRepository = mock(MaterialRepository.class);
            VendorRepository vendorRepository = mock(VendorRepository.class);
            PlantRepository plantRepository = mock(PlantRepository.class);

            InspectionServiceImpl inspectionService = new InspectionServiceImpl(
                    inspectionLotRepo, inspectionActRepo, vendorRepository, plantRepository, materialRepository);

            when(inspectionLotRepo.findById(404)).thenReturn(Optional.empty());

            GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                    () -> inspectionService.generateReportPdf(404));

            assertEquals("Lot not found with id: 404", ex.getMessage());
            verify(inspectionLotRepo).findById(404);
        }

        @Test
        void testGenerateReportPdf_Success_WithData() {
            InspectionLotRepository inspectionLotRepo = mock(InspectionLotRepository.class);
            InspectionActualsRepository inspectionActRepo = mock(InspectionActualsRepository.class);
            MaterialRepository materialRepository = mock(MaterialRepository.class);
            VendorRepository vendorRepository = mock(VendorRepository.class);
            PlantRepository plantRepository = mock(PlantRepository.class);

            InspectionServiceImpl inspectionService = new InspectionServiceImpl(
                    inspectionLotRepo, inspectionActRepo,  vendorRepository, plantRepository, materialRepository);

            Material material = new Material();
            material.setMaterialId("MAT1");
            material.setMaterialDesc("STEEL ROD");
            material.setType("RAW");
            material.setStatus(true);

            Plant plant = new Plant();
            plant.setPlantId("PL1");
            plant.setPlantName("MAIN PLANT");
            plant.setStatus(true);

            Vendor vendor = new Vendor();
            vendor.setVendorId(101);
            vendor.setName("ABC VENDOR");
            vendor.setStatus(true);

            MaterialInspectionCharacteristics c1 = new MaterialInspectionCharacteristics();
            c1.setCharacteristicId(1);
            c1.setCharacteristicDescription("LENGTH");
            c1.setUnitOfMeasure("mm");
            c1.setLowerToleranceLimit(1.0);
            c1.setUpperToleranceLimit(10.0);
            c1.setMaterial(material);

            MaterialInspectionCharacteristics c2 = new MaterialInspectionCharacteristics();
            c2.setCharacteristicId(2);
            c2.setCharacteristicDescription("WIDTH");
            c2.setUnitOfMeasure("mm");
            c2.setLowerToleranceLimit(0.5);
            c2.setUpperToleranceLimit(5.0);
            c2.setMaterial(material);

            material.setMaterialChar(List.of(c1, c2));

            InspectionActuals a1 = new InspectionActuals();
            a1.setMaterialInspectionCharacteristics(c1);
            a1.setMinimumMeasurement(2.0);
            a1.setMaximumMeasurement(9.0);

            InspectionActuals a2 = new InspectionActuals();
            a2.setMaterialInspectionCharacteristics(c2);
            a2.setMinimumMeasurement(1.0);
            a2.setMaximumMeasurement(4.0);

            InspectionLot lot = new InspectionLot();
            lot.setLotId(500);
            lot.setCreationDate(LocalDate.of(2025, 1, 1));
            lot.setInspectionStartDate(LocalDate.of(2025, 1, 2));
            lot.setInspectionEndDate(LocalDate.of(2025, 1, 3));
            lot.setResult("PASS");
            lot.setRemarks("All good.");
            lot.setMaterial(material);
            lot.setPlant(plant);
            lot.setVendor(vendor);
            lot.setInspectionActuals(new ArrayList<>(List.of(a1, a2)));

            when(inspectionLotRepo.findById(500)).thenReturn(Optional.of(lot));

            byte[] pdf = inspectionService.generateReportPdf(500);

            assertNotNull(pdf);
            assertTrue(pdf.length > 100, "PDF should be non-empty and reasonably sized");

            String head = new String(pdf, 0, Math.min(pdf.length, 8), StandardCharsets.ISO_8859_1);
            assertTrue(head.startsWith("%PDF"), "Generated bytes should start with %PDF header");

            verify(inspectionLotRepo).findById(500);
        }

        @Test
        void testGenerateReportPdf_Success_NullMaterialVendorPlant() {
            InspectionLotRepository inspectionLotRepo = mock(InspectionLotRepository.class);
            InspectionActualsRepository inspectionActRepo = mock(InspectionActualsRepository.class);
            MaterialRepository materialRepository = mock(MaterialRepository.class);
            VendorRepository vendorRepository = mock(VendorRepository.class);
            PlantRepository plantRepository = mock(PlantRepository.class);

            InspectionServiceImpl inspectionService = new InspectionServiceImpl(
                    inspectionLotRepo, inspectionActRepo, vendorRepository, plantRepository, materialRepository);

            InspectionLot lot = new InspectionLot();
            lot.setLotId(600);
            lot.setCreationDate(LocalDate.of(2025, 2, 1));
            lot.setInspectionStartDate(LocalDate.of(2025, 2, 2));
            lot.setInspectionEndDate(LocalDate.of(2025, 2, 3));
            lot.setResult(null);
            lot.setRemarks(null);
            lot.setMaterial(null); 
            lot.setVendor(null);   
            lot.setPlant(null);   
            lot.setInspectionActuals(new ArrayList<>());

            when(inspectionLotRepo.findById(600)).thenReturn(Optional.of(lot));

            byte[] pdf = inspectionService.generateReportPdf(600);

            assertNotNull(pdf);
            assertTrue(pdf.length > 50);
            String head = new String(pdf, 0, Math.min(pdf.length, 8), StandardCharsets.ISO_8859_1);
            assertTrue(head.startsWith("%PDF"));
            verify(inspectionLotRepo).findById(600);
        }

        @Test
        void testGenerateReportPdf_Success_NullMeasurementsInActuals() {
            InspectionLotRepository inspectionLotRepo = mock(InspectionLotRepository.class);
            InspectionActualsRepository inspectionActRepo = mock(InspectionActualsRepository.class);
            MaterialRepository materialRepository = mock(MaterialRepository.class);
            VendorRepository vendorRepository = mock(VendorRepository.class);
            PlantRepository plantRepository = mock(PlantRepository.class);

            InspectionServiceImpl inspectionService = new InspectionServiceImpl(
                    inspectionLotRepo, inspectionActRepo,vendorRepository, plantRepository, materialRepository);

            Material material = new Material();
            material.setMaterialId("MAT2");
            material.setMaterialDesc("ALUMINIUM BAR");

            MaterialInspectionCharacteristics c1 = new MaterialInspectionCharacteristics();
            c1.setCharacteristicId(10);
            c1.setCharacteristicDescription("THICKNESS");
            c1.setUnitOfMeasure("mm");
            c1.setLowerToleranceLimit(1.0);
            c1.setUpperToleranceLimit(10.0);
            c1.setMaterial(material);
            material.setMaterialChar(List.of(c1));

            InspectionActuals a1 = new InspectionActuals();
            a1.setMaterialInspectionCharacteristics(c1);
            a1.setMinimumMeasurement(null);
            a1.setMaximumMeasurement(null);

            InspectionLot lot = new InspectionLot();
            lot.setLotId(700);
            lot.setCreationDate(LocalDate.of(2025, 3, 1));
            lot.setInspectionStartDate(LocalDate.of(2025, 3, 2));
            lot.setMaterial(material);
            lot.setVendor(null);
            lot.setPlant(null);
            lot.setInspectionActuals(new ArrayList<>(List.of(a1)));

            when(inspectionLotRepo.findById(700)).thenReturn(Optional.of(lot));

            byte[] pdf = inspectionService.generateReportPdf(700);

            assertNotNull(pdf);
            assertTrue(pdf.length > 50);
            String head = new String(pdf, 0, Math.min(pdf.length, 8), StandardCharsets.ISO_8859_1);
            assertTrue(head.startsWith("%PDF"));
            verify(inspectionLotRepo).findById(700);
        }
    
    
}
