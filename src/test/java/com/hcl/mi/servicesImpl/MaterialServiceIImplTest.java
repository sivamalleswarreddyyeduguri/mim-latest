package com.hcl.mi.servicesImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.hcl.mi.entities.InspectionActuals;
import com.hcl.mi.entities.InspectionLot;
import com.hcl.mi.entities.Material;
import com.hcl.mi.entities.MaterialInspectionCharacteristics;
import com.hcl.mi.exceptions.DuplicateCharacteristicException;
import com.hcl.mi.exceptions.GenericAlreadyExistsException;
import com.hcl.mi.exceptions.GenericNotFoundException;
import com.hcl.mi.helper.Transformers;
import com.hcl.mi.repositories.InspectionLotRepository;
import com.hcl.mi.repositories.MaterialCharRepository;
import com.hcl.mi.repositories.MaterialRepository;
import com.hcl.mi.requestdtos.MaterialCharDto;
import com.hcl.mi.requestdtos.MaterialCharUpdateDto;
import com.hcl.mi.responsedtos.MaterialDto;
import com.hcl.mi.responsedtos.MaterialInspectionCharacteristicsDto;

class MaterialServiceIImplTest {

    @Mock
    private MaterialRepository materialRepository;
 
    @Mock
    private MaterialCharRepository materialCharReposotory;
 
    @Mock
    private InspectionLotRepository inspectionLotRepo;

    @InjectMocks
    private MaterialServiceIImpl materialService;

    private Material material;
    private MaterialDto materialDto; 

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        material = new Material();
        material.setMaterialId("M101");
        material.setMaterialDesc("STEEL ROD");
        material.setType("RAW");
        material.setStatus(true);
        material.setMaterialChar(new ArrayList<>());

        materialDto = new MaterialDto();
        materialDto.setMaterialId("  m101  ");
        materialDto.setMaterialDesc("  Steel    Rod  ");
        materialDto.setType(" raw   material ");
        materialDto.setStatus(true);
    }

    @Test
    void testGetAllMaterials() {
        when(materialRepository.findAll()).thenReturn(List.of(material));

        List<MaterialDto> result = materialService.getAllMaterials();

        assertEquals(1, result.size());
        assertEquals("M101", result.get(0).getMaterialId());
        assertEquals("STEEL ROD", result.get(0).getMaterialDesc());
        assertEquals("RAW", result.get(0).getType());
        assertTrue(result.get(0).isStatus());
        verify(materialRepository).findAll();
    }

    @Test
    void testGetMaterial_Success() {
        when(materialRepository.findById("M101")).thenReturn(Optional.of(material));

        MaterialDto dto = materialService.getMaterial("m101");

        assertEquals("M101", dto.getMaterialId());
        assertEquals("STEEL ROD", dto.getMaterialDesc());
        verify(materialRepository).findById("M101");
    }

    @Test
    void testGetMaterial_NotFound() {
        when(materialRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> materialService.getMaterial("unknown"));

        assertEquals("Material not found with id: unknown", ex.getMessage());
        verify(materialRepository).findById("UNKNOWN");
    }

    @Test
    void testDeleteMaterial_Success() {
        when(materialRepository.findById("M101")).thenReturn(Optional.of(material));

        materialService.deleteMaterial("m101");

        assertFalse(material.isStatus());
        verify(materialRepository).save(material);
        verify(materialRepository).findById("M101");
    }

    @Test
    void testDeleteMaterial_NotFound() {
        when(materialRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> materialService.deleteMaterial("unknown"));

        assertEquals("Material not found with id: unknown", ex.getMessage());
        verify(materialRepository, never()).save(any());
        verify(materialRepository).findById("UNKNOWN");
    }

    @Test
    void testAddNewMaterial_Success() {
        when(materialRepository.findById("M101")).thenReturn(Optional.empty());
        when(materialRepository.findByMaterialDesc("STEEL ROD")).thenReturn(Optional.empty());

        ArgumentCaptor<Material> captor = ArgumentCaptor.forClass(Material.class);

        materialService.addNewMaterial(materialDto);

        verify(materialRepository).findById("M101");
        verify(materialRepository).findByMaterialDesc("STEEL ROD");
        verify(materialRepository).save(captor.capture());

        Material saved = captor.getValue();
        assertEquals("M101", saved.getMaterialId());
        assertEquals("STEEL ROD", saved.getMaterialDesc());
        assertEquals("RAW MATERIAL", saved.getType()); // removeExtraSpaces + uppercase
        assertTrue(saved.isStatus());
    }

    @Test
    void testAddNewMaterial_AlreadyExists_ById() {
        when(materialRepository.findById("M101")).thenReturn(Optional.of(material));
        when(materialRepository.findByMaterialDesc("STEEL ROD")).thenReturn(Optional.empty());

        GenericAlreadyExistsException ex = assertThrows(GenericAlreadyExistsException.class,
                () -> materialService.addNewMaterial(materialDto));

        assertEquals("Material with the same ID or description already exists.", ex.getMessage());
        verify(materialRepository, never()).save(any());
    }

    @Test
    void testAddNewMaterial_AlreadyExists_ByDesc() {
        when(materialRepository.findById("M101")).thenReturn(Optional.empty());
        when(materialRepository.findByMaterialDesc("STEEL ROD")).thenReturn(Optional.of(material));

        GenericAlreadyExistsException ex = assertThrows(GenericAlreadyExistsException.class,
                () -> materialService.addNewMaterial(materialDto));

        assertEquals("Material with the same ID or description already exists.", ex.getMessage());
        verify(materialRepository, never()).save(any());
    }

    @Test
    void testAddNewMaterialCharacteristic_Success() {
        when(materialRepository.findById("MAT1")).thenReturn(Optional.of(buildMaterial("MAT1", "DESC", "TYPE", true)));
        when(materialRepository.findById("mat1")).thenReturn(Optional.of(buildMaterial("MAT1", "DESC", "TYPE", true)));

        MaterialCharDto charDto = MaterialCharDto.builder()
                .characteristicId(null)
                .charDesc("  length  ")
                .utl(10.0)
                .ltl(1.0)
                .uom(" mm ")
                .matId("mat1")
                .build();

        materialService.addNewMaterialCharacteristic(charDto);

        ArgumentCaptor<MaterialInspectionCharacteristics> captor = ArgumentCaptor.forClass(MaterialInspectionCharacteristics.class);
        verify(materialCharReposotory).save(captor.capture());

        MaterialInspectionCharacteristics saved = captor.getValue();
        assertEquals("LENGTH", saved.getCharacteristicDescription()); 
        assertEquals(1.0, saved.getLowerToleranceLimit());
        assertEquals(10.0, saved.getUpperToleranceLimit());
        assertEquals("mm", saved.getUnitOfMeasure()); 
        assertNotNull(saved.getMaterial());
        assertEquals("MAT1", saved.getMaterial().getMaterialId()); 
    }

    @Test
    void testAddNewMaterialCharacteristic_Duplicate() {
        Material existingMat = buildMaterial("MAT1", "DESC", "TYPE", true);
        MaterialInspectionCharacteristics existingChar = new MaterialInspectionCharacteristics();
        existingChar.setCharacteristicId(11);
        existingChar.setCharacteristicDescription("LENGTH"); 
        existingChar.setMaterial(existingMat);
        existingMat.setMaterialChar(List.of(existingChar));

        when(materialRepository.findById("MAT1")).thenReturn(Optional.of(existingMat));
        when(materialRepository.findById("mat1")).thenReturn(Optional.of(existingMat));

        MaterialCharDto charDto = MaterialCharDto.builder()
                .charDesc("length") 
                .utl(5.0)
                .ltl(1.0)
                .uom("mm")
                .matId("mat1")
                .build();

        DuplicateCharacteristicException ex = assertThrows(DuplicateCharacteristicException.class,
                () -> materialService.addNewMaterialCharacteristic(charDto));

        assertEquals("Characteristic 'length' already exists for material ID: mat1", ex.getMessage());
        verify(materialCharReposotory, never()).save(any());
    }

    @Test
    void testAddNewMaterialCharacteristic_MaterialNotFound() {
        when(materialRepository.findById("MAT404")).thenReturn(Optional.empty());

        MaterialCharDto charDto = MaterialCharDto.builder()
                .charDesc("width")
                .utl(5.0)
                .ltl(1.0)
                .uom("mm")
                .matId("mat404")
                .build();

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> materialService.addNewMaterialCharacteristic(charDto));

        assertEquals("Material not found with id: mat404", ex.getMessage());
        verify(materialCharReposotory, never()).save(any());
    }

    @Test
    void testGetAllInspectionLots_FiltersIncomplete() {
        Material matA = buildMaterial("MA", "A", "T", true);
        matA.setMaterialChar(new ArrayList<>());
        matA.getMaterialChar().add(new MaterialInspectionCharacteristics());
        matA.getMaterialChar().add(new MaterialInspectionCharacteristics());
        InspectionLot lotA = new InspectionLot();
        lotA.setLotId(1001);
        lotA.setMaterial(matA);
        lotA.setInspectionActuals(new ArrayList<>());
        lotA.getInspectionActuals().add(new InspectionActuals());

        Material matB = buildMaterial("MB", "B", "T", true);
        matB.setMaterialChar(new ArrayList<>());
        matB.getMaterialChar().add(new MaterialInspectionCharacteristics());
        matB.getMaterialChar().add(new MaterialInspectionCharacteristics());
        InspectionLot lotB = new InspectionLot();
        lotB.setLotId(1002);
        lotB.setMaterial(matB);
        lotB.setInspectionActuals(new ArrayList<>());
        lotB.getInspectionActuals().add(new InspectionActuals());
        lotB.getInspectionActuals().add(new InspectionActuals());

        when(inspectionLotRepo.findAll()).thenReturn(List.of(lotA, lotB));

        List<InspectionLot> result = materialService.getAllInspectionLots();

        assertEquals(1, result.size());
        assertEquals(1001, result.get(0).getLotId());
        verify(inspectionLotRepo).findAll();
    }

    @Test
    void testGetMaterialCharByLotId_Success() {
        Material mat = buildMaterial("MZ", "Z", "T", true);
        MaterialInspectionCharacteristics c1 = buildChar(1, "LEN", mat);
        MaterialInspectionCharacteristics c2 = buildChar(2, "WID", mat);
        MaterialInspectionCharacteristics c3 = buildChar(3, "HGT", mat);
        mat.setMaterialChar(List.of(c1, c2, c3));

        InspectionActuals act = new InspectionActuals();
        act.setMaterialInspectionCharacteristics(c2);
        InspectionLot lot = new InspectionLot();
        lot.setLotId(500);
        lot.setMaterial(mat);
        lot.setInspectionActuals(List.of(act));

        when(inspectionLotRepo.findById(500)).thenReturn(Optional.of(lot));

        List<MaterialInspectionCharacteristicsDto> out = materialService.getMaterialCharByLotId(500);

        assertEquals(2, out.size());
        List<Integer> ids = out.stream().map(MaterialInspectionCharacteristicsDto::getCharacteristicId).sorted().toList();
        assertEquals(List.of(1, 3), ids);
        verify(inspectionLotRepo).findById(500);
    }

    @Test
    void testGetMaterialCharByLotId_NotFound() {
        when(inspectionLotRepo.findById(999)).thenReturn(Optional.empty());

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> materialService.getMaterialCharByLotId(999));

        assertEquals("Lot not found with id: 999", ex.getMessage());
        verify(inspectionLotRepo).findById(999);
    }

    @Test
    void testGetAllCharacteristicsOfMaterial_Success() {
        Material mat = buildMaterial("MAT1", "DESC", "TYPE", true);
        MaterialInspectionCharacteristics c1 = buildChar(10, "LENGTH", mat);
        mat.setMaterialChar(List.of(c1));

        when(materialRepository.findById("mat1")).thenReturn(Optional.of(mat));

        List<MaterialInspectionCharacteristicsDto> list = materialService.getAllCharacteristicsOfMaterial("m a t 1");

        assertEquals(1, list.size());
        assertEquals(10, list.get(0).getCharacteristicId());
        assertEquals("LENGTH", list.get(0).getCharacteristicDescription());
        verify(materialRepository).findById("mat1");
    }

    @Test
    void testGetAllCharacteristicsOfMaterial_NotFound() {
        when(materialRepository.findById("Mat404")).thenReturn(Optional.empty());

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> materialService.getAllCharacteristicsOfMaterial("Mat 404"));

        assertEquals("Material with ID Mat404 not found.", ex.getMessage());
        verify(materialRepository).findById("Mat404");
    }

    @Test
    void testSaveEditMaterial_Success() {
        when(materialRepository.findById("M101")).thenReturn(Optional.of(material));

        MaterialDto input = new MaterialDto();
        input.setMaterialId("  m101 ");
        input.setMaterialDesc("  new   desc ");
        input.setType(" semi  finished ");
        input.setStatus(false);

        ArgumentCaptor<Material> captor = ArgumentCaptor.forClass(Material.class);

        materialService.saveEditMaterial(input);

        verify(materialRepository).save(captor.capture());
        Material saved = captor.getValue();

        assertEquals("M101", saved.getMaterialId());
        assertEquals("NEW DESC", saved.getMaterialDesc());     
        assertEquals("SEMI FINISHED", saved.getType());        
        assertFalse(saved.isStatus());
        verify(materialRepository).findById("M101");
    }

    @Test
    void testSaveEditMaterial_NotFound() {
        when(materialRepository.findById("M404")).thenReturn(Optional.empty());

        MaterialDto input = new MaterialDto();
        input.setMaterialId(" m404 ");
        input.setMaterialDesc("desc");
        input.setType("type");

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> materialService.saveEditMaterial(input));

        assertEquals("Material not found with id: M404", ex.getMessage());
        verify(materialRepository, never()).save(any());
    }

    @Test
    void testGetAllActiveMaterials() {
        Material active = buildMaterial("A1", "DESC A", "TYPE", true);
        Material inactive = buildMaterial("I1", "DESC I", "TYPE", false);
        when(materialRepository.findAllByStatus(true)).thenReturn(List.of(active));

        List<MaterialDto> result = materialService.getAllActiveMaterials();

        assertEquals(1, result.size());
        assertEquals("A1", result.get(0).getMaterialId());
        verify(materialRepository).findAllByStatus(true);
    }

    @Test
    void testGetCharacteristicsByChId_Success() {
        Material mat = buildMaterial("M101", "STEEL", "RAW", true);
        MaterialInspectionCharacteristics ch = buildChar(77, "THICKNESS", mat);
        when(materialCharReposotory.findById(77)).thenReturn(Optional.of(ch));

        MaterialInspectionCharacteristicsDto dto = materialService.getCharacteristicsByChId(77);

        assertEquals(77, dto.getCharacteristicId());
        assertEquals("THICKNESS", dto.getCharacteristicDescription());
        assertEquals("M101", dto.getMatId());
        verify(materialCharReposotory).findById(77);
    }

    @Test
    void testGetCharacteristicsByChId_NotFound() {
        when(materialCharReposotory.findById(909)).thenReturn(Optional.empty());

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> materialService.getCharacteristicsByChId(909));

        assertEquals("Material characteristcs not found with id: 909", ex.getMessage());
        verify(materialCharReposotory).findById(909);
    }

    @Test
    void testUpdateCharacteristic_Success() {
        MaterialInspectionCharacteristics existing = new MaterialInspectionCharacteristics();
        existing.setCharacteristicId(15);
        existing.setCharacteristicDescription("OLD");
        existing.setLowerToleranceLimit(0.5);
        existing.setUpperToleranceLimit(1.5);
        existing.setUnitOfMeasure("mm");

        when(materialCharReposotory.findById(15)).thenReturn(Optional.of(existing));

        MaterialCharUpdateDto upd = new MaterialCharUpdateDto();
        upd.setCharacteristicId(15);
        upd.setCharDesc("  new   desc  ");
        upd.setLtl(2.0);
        upd.setUtl(5.0);
        upd.setUom(" CM "); 
        materialService.update(upd);

        ArgumentCaptor<MaterialInspectionCharacteristics> captor = ArgumentCaptor.forClass(MaterialInspectionCharacteristics.class);
        verify(materialCharReposotory).save(captor.capture());

        MaterialInspectionCharacteristics saved = captor.getValue();
        assertEquals(15, saved.getCharacteristicId());
        assertEquals("NEW DESC", saved.getCharacteristicDescription()); 
        assertEquals(2.0, saved.getLowerToleranceLimit());
        assertEquals(5.0, saved.getUpperToleranceLimit());
        assertEquals(" CM ", saved.getUnitOfMeasure()); 
        verify(materialCharReposotory).findById(15);
    }

    @Test
    void testUpdateCharacteristic_NotFound() {
        when(materialCharReposotory.findById(404)).thenReturn(Optional.empty());

        MaterialCharUpdateDto upd = new MaterialCharUpdateDto();
        upd.setCharacteristicId(404);
        upd.setCharDesc("x");
        upd.setLtl(1.0);
        upd.setUtl(2.0);
        upd.setUom("mm");

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> materialService.update(upd));

        assertEquals("Characteristics with ID 404 does not exist.", ex.getMessage());
        verify(materialCharReposotory, never()).save(any());
    }
 
    private Material buildMaterial(String id, String desc, String type, boolean status) {
        Material m = new Material();
        m.setMaterialId(id);
        m.setMaterialDesc(desc);
        m.setType(type);
        m.setStatus(status);
        m.setMaterialChar(new ArrayList<>());
        return m;
    }

    private MaterialInspectionCharacteristics buildChar(int id, String desc, Material material) {
        MaterialInspectionCharacteristics c = new MaterialInspectionCharacteristics();
        c.setCharacteristicId(id);
        c.setCharacteristicDescription(desc);
        c.setMaterial(material);
        return c;
    }
    

@Test
    void testAddListOfCharacteristicsForMaterial_csv_success() throws Exception {
        byte[] fileContent = ("charId,charDesc,utl,ltl,uom,matId\n" +
                              "1,Hardness Test,50.0,10.0,HRC,M101")
                              .getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "chars.csv", "text/csv", fileContent);

        MaterialServiceIImpl spyService = Mockito.spy(materialService);
        doReturn(material).when(spyService).isCharacteristicConditionSatisfy(any());

        try (MockedStatic<Transformers> mocked = Mockito.mockStatic(Transformers.class)) {
            mocked.when(() -> Transformers.convertMaterialCharDtoToMaterialInspectionCharObj(any(), any()))
                  .thenAnswer(inv -> new MaterialInspectionCharacteristics());

            boolean result = spyService.addListOfCharacteristicsForMaterial(file);

            assertTrue(result, "Service should return true on successful CSV upload");
            verify(materialRepository, times(1)).save(eq(material));

            mocked.verify(() -> Transformers.convertMaterialCharDtoToMaterialInspectionCharObj(
                    argThat(dto -> "Hardness Test".equals(dto.getCharDesc())
                            && dto.getUom().equals("HRC")
                            && Double.valueOf(50.0).equals(dto.getUtl())
                            && Double.valueOf(10.0).equals(dto.getLtl())
                            && "M101".equals(dto.getMatId())),
                    eq(material)
            ), times(1));
        }
    }

    @Test
    void testAddListOfCharacteristicsForMaterial_xlsx_success() throws Exception {
        byte[] workbookBytes = buildWorkbookBytes(
                Arrays.asList("charId", "charDesc", "utl", "ltl", "uom", "matId"),
                Arrays.asList("1", "Tensile Test", "100.5", "30.2", "MPa", "M101")
        );
        MockMultipartFile file = new MockMultipartFile("file", "chars.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", workbookBytes);

        MaterialServiceIImpl spyService = Mockito.spy(materialService);
        doReturn(material).when(spyService).isCharacteristicConditionSatisfy(any());

        try (MockedStatic<Transformers> mocked = Mockito.mockStatic(Transformers.class)) {
            mocked.when(() -> Transformers.convertMaterialCharDtoToMaterialInspectionCharObj(any(), any()))
                  .thenAnswer(inv -> new MaterialInspectionCharacteristics());

            boolean result = spyService.addListOfCharacteristicsForMaterial(file);

            assertTrue(result, "Service should return true on successful XLSX upload");
            verify(materialRepository, times(1)).save(eq(material));

            mocked.verify(() -> Transformers.convertMaterialCharDtoToMaterialInspectionCharObj(
                    argThat(dto -> "Tensile Test".equals(dto.getCharDesc())
                            && dto.getUom().equals("MPa")
                            && Double.valueOf(100.5).equals(dto.getUtl())
                            && Double.valueOf(30.2).equals(dto.getLtl())
                            && "M101".equals(dto.getMatId())),
                    eq(material)
            ), times(1));
        }
    }

    @Test
    void testAddListOfCharacteristicsForMaterial_invalidExtension_throws() {
        MockMultipartFile file = new MockMultipartFile("file", "chars.txt", "text/plain", "x".getBytes());

        Exception ex = assertThrows(Exception.class, () -> materialService.addListOfCharacteristicsForMaterial(file));
        assertEquals("Please provide .xls, .xlsx, or .csv file", ex.getMessage());
        verify(materialRepository, never()).save(any());
    }

    @Test
    void testAddListOfCharacteristicsForMaterial_ioException_throws() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("chars.csv");
        when(file.getInputStream()).thenThrow(new IOException("boom"));

        Exception ex = assertThrows(Exception.class, () -> materialService.addListOfCharacteristicsForMaterial(file));
        assertTrue(ex.getMessage().contains("Failed to read file content"));
        verify(materialRepository, never()).save(any());
    }

    @Test
    void testAddListOfCharacteristicsForMaterial_duplicateOrNotFound_throws() throws Exception {
        byte[] fileContent = ("charId,charDesc,utl,ltl,uom,matId\n" +
                              "1,Hardness Test,50.0,10.0,HRC,M999")
                              .getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "chars.csv", "text/csv", fileContent);

        MaterialServiceIImpl spyService = Mockito.spy(materialService);
        doReturn(null).when(spyService).isCharacteristicConditionSatisfy(any());

        Exception ex = assertThrows(Exception.class, () -> spyService.addListOfCharacteristicsForMaterial(file));
        assertTrue(ex.getMessage().contains("Material characteristic already exists or material not found"),
                "Should throw when characteristic already exists or material not found");

        verify(materialRepository, never()).save(any());
    }

    @Test
    void testAddListOfCharacteristicsForMaterial_csv_invalidRowSkipped_noSave() throws Exception {
        byte[] fileContent = ("charId,charDesc,utl,ltl,uom,matId\n" +
                              "1,OnlyFiveColumns,50.0,10.0,HRC")
                              .getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "chars.csv", "text/csv", fileContent);

        MaterialServiceIImpl spyService = Mockito.spy(materialService);
        doReturn(material).when(spyService).isCharacteristicConditionSatisfy(any());

        try (MockedStatic<Transformers> mocked = Mockito.mockStatic(Transformers.class)) {
            mocked.when(() -> Transformers.convertMaterialCharDtoToMaterialInspectionCharObj(any(), any()))
                  .thenAnswer(inv -> new MaterialInspectionCharacteristics());

            boolean result = spyService.addListOfCharacteristicsForMaterial(file);

            assertTrue(result, "Service still returns true even if invalid rows are skipped");
            verify(materialRepository, never()).save(any());
            mocked.verifyNoInteractions();
        }
    }

    @Test
    void testAddListOfCharacteristicsForMaterial_xlsx_invalidCellsSkipped_noSave() throws Exception {
        byte[] workbookBytes = buildWorkbookBytesWithNulls();
        MockMultipartFile file = new MockMultipartFile("file", "chars.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", workbookBytes);

        MaterialServiceIImpl spyService = Mockito.spy(materialService);
        doReturn(material).when(spyService).isCharacteristicConditionSatisfy(any());

        try (MockedStatic<Transformers> mocked = Mockito.mockStatic(Transformers.class)) {
            mocked.when(() -> Transformers.convertMaterialCharDtoToMaterialInspectionCharObj(any(), any()))
                  .thenAnswer(inv -> new MaterialInspectionCharacteristics());

            boolean result = spyService.addListOfCharacteristicsForMaterial(file);

            assertTrue(result, "Service returns true; invalid XLSX rows are skipped");
            verify(materialRepository, never()).save(any());
            mocked.verifyNoInteractions();
        }
    }



    private byte[] buildWorkbookBytes(List<String> header, List<String> row) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Sheet1");

            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < header.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(header.get(i));
            }

            XSSFRow dataRow = sheet.createRow(1);
            for (int i = 0; i < row.size(); i++) {
                Cell cell = dataRow.createCell(i);
                if (i == 2 || i == 3) { 
                    cell.setCellValue(Double.parseDouble(row.get(i)));
                } else {
                    cell.setCellValue(row.get(i));
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] buildWorkbookBytesWithNulls() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Sheet1");

            XSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("charId");
            headerRow.createCell(1).setCellValue("charDesc");
            headerRow.createCell(2).setCellValue("utl");
            headerRow.createCell(3).setCellValue("ltl");
            headerRow.createCell(4).setCellValue("uom");
            headerRow.createCell(5).setCellValue("matId");

            XSSFRow dataRow = sheet.createRow(1);
            dataRow.createCell(1).setCellValue("Invalid Row");  

            workbook.write(out);
            return out.toByteArray();
        }
    }

}