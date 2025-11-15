package com.hcl.mi.controllers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.hcl.mi.requestdtos.MaterialCharDto;
import com.hcl.mi.requestdtos.MaterialCharUpdateDto;
import com.hcl.mi.responsedtos.MaterialDto;
import com.hcl.mi.responsedtos.MaterialInspectionCharacteristicsDto;
import com.hcl.mi.responsedtos.ResponseDto;
import com.hcl.mi.services.MaterialService;

class MaterialControllerTest {

    @Mock
    private MaterialService materialService;

    @InjectMocks 
    private MaterialController materialController;

    private MaterialDto materialDto;
    private MaterialCharDto materialCharDto;
    private MaterialCharUpdateDto materialCharUpdateDto;
    private MaterialInspectionCharacteristicsDto charDto;

    @BeforeEach
    void setUp() { 
        MockitoAnnotations.openMocks(this);

        materialDto = new MaterialDto();
        materialDto.setMaterialId("M001");
        materialDto.setMaterialDesc("Steel Rods");
        materialDto.setType("Raw Material");
        materialDto.setStatus(true);

        materialCharDto = MaterialCharDto.builder()
                .characteristicId(1)
                .charDesc("Hardness Test")
                .utl(50.0)
                .ltl(10.0)
                .uom("HRC")
                .matId("M001")
                .build();

        materialCharUpdateDto = new MaterialCharUpdateDto();
        materialCharUpdateDto.setCharacteristicId(1);
        materialCharUpdateDto.setCharDesc("Updated Hardness Test");
        materialCharUpdateDto.setUtl(60.0);
        materialCharUpdateDto.setLtl(15.0);
        materialCharUpdateDto.setUom("HRC");

        charDto = new MaterialInspectionCharacteristicsDto();
        charDto.setCharacteristicId(1);
        charDto.setCharacteristicDescription("Test Description");
        charDto.setUpperToleranceLimit(50.0);
        charDto.setLowerToleranceLimit(10.0);
        charDto.setUnitOfMeasure("HRC");
        charDto.setMatId("M001");
    }
 

    @Test
    void testAddNewMaterial() {
        doNothing().when(materialService).addNewMaterial(any(MaterialDto.class));

        ResponseEntity<ResponseDto> response = materialController.addNewMaterial(materialDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("201", response.getBody().getStatusCode());
        assertEquals("Material saved successfully", response.getBody().getStatusMsg());
        verify(materialService, times(1)).addNewMaterial(any(MaterialDto.class));
    }

    @Test
    void testGetAllMaterials() {
        when(materialService.getAllMaterials()).thenReturn(List.of(materialDto));

        ResponseEntity<List<MaterialDto>> response = materialController.getAllMaterials();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(materialService, times(1)).getAllMaterials();
    }

    @Test
    void testGetMaterial() {
        when(materialService.getMaterial("M001")).thenReturn(materialDto);

        ResponseEntity<MaterialDto> response = materialController.getMaterial("M001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Steel Rods", response.getBody().getMaterialDesc());
        verify(materialService, times(1)).getMaterial("M001");
    }

    @Test
    void testEditMaterialSave() {
        doNothing().when(materialService).saveEditMaterial(any(MaterialDto.class));

        ResponseEntity<ResponseDto> response = materialController.editMaterialSave(materialDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Material Updated successfully", response.getBody().getStatusMsg());
        verify(materialService, times(1)).saveEditMaterial(any(MaterialDto.class));
    }

    @Test
    void testDeleteMaterial() {
        doNothing().when(materialService).deleteMaterial("M001");

        ResponseEntity<ResponseDto> response = materialController.deleteVendor("M001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("material deleted successfully", response.getBody().getStatusMsg());
        verify(materialService, times(1)).deleteMaterial("M001");
    }


    @Test
    void testViewCharacteristics() {
        when(materialService.getAllCharacteristicsOfMaterial("M001")).thenReturn(List.of(charDto));

        ResponseEntity<List<MaterialInspectionCharacteristicsDto>> response =
                materialController.viewCharacteristics("M001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(materialService, times(1)).getAllCharacteristicsOfMaterial("M001");
    }

    @Test
    void testAddMaterialCharacteristics() {
        doNothing().when(materialService).addNewMaterialCharacteristic(any(MaterialCharDto.class));

        ResponseEntity<ResponseDto> response = materialController.addMaterialCharacteristics(materialCharDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("201", response.getBody().getStatusCode());
        assertEquals("material characteristics added successfully", response.getBody().getStatusMsg());
        verify(materialService, times(1)).addNewMaterialCharacteristic(any(MaterialCharDto.class));
    }
 
    @Test
    void testGetCharacteristicsByChId() {
        when(materialService.getCharacteristicsByChId(1)).thenReturn(charDto);

        ResponseEntity<MaterialInspectionCharacteristicsDto> response = materialController.getCharacteristicsByChId(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Description", response.getBody().getCharacteristicDescription());
        verify(materialService, times(1)).getCharacteristicsByChId(1);
    }

    @Test
    void testGetLotCurrentCharacteristicsOfAssociatedMaterial() {
        when(materialService.getMaterialCharByLotId(1)).thenReturn(List.of(charDto));

        ResponseEntity<List<MaterialInspectionCharacteristicsDto>> response =
                materialController.getLotCurrentCharacteristicsOfAssociatedMaterial(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(materialService, times(1)).getMaterialCharByLotId(1);
    }

    @Test
    void testUpdateCharacteristics() {
        doNothing().when(materialService).update(any(MaterialCharUpdateDto.class));

        ResponseEntity<ResponseDto> response = materialController.updateCharcteristics(materialCharUpdateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("characteristics details updated Successfully", response.getBody().getStatusMsg());
        verify(materialService, times(1)).update(any(MaterialCharUpdateDto.class));
    }
    

    @Test
    void testUploadMaterialCharacteristics_captor() throws Exception {
        byte[] fileContent = "charId,charDesc,utl,ltl,uom,matId\n1,Hardness Test,50,10,HRC,M001".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "chars.csv", "text/csv", fileContent);

        when(materialService.addListOfCharacteristicsForMaterial(any())).thenReturn(true);

        materialController.addMaterialCharacteristics(file);

        ArgumentCaptor<MultipartFile> captor = ArgumentCaptor.forClass(MultipartFile.class);
        verify(materialService, times(1)).addListOfCharacteristicsForMaterial(captor.capture());

        MultipartFile passed = captor.getValue();
        assertEquals("file", passed.getName());
        assertEquals("chars.csv", passed.getOriginalFilename());
        assertEquals("text/csv", passed.getContentType());
        assertArrayEquals(fileContent, passed.getBytes());
    }


        @Test
        void testUploadMaterialCharacteristics_whenServiceThrowsException_bubblesUp() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "chars.csv", "text/csv", "bad,data".getBytes()
            );

            doThrow(new RuntimeException("Parse error"))
                    .when(materialService).addListOfCharacteristicsForMaterial(any());

            assertThrows(Exception.class, () -> materialController.addMaterialCharacteristics(file));

            verify(materialService, times(1)).addListOfCharacteristicsForMaterial(eq(file));
        }

        @Test
        void testDeleteMaterialCharacteristics_success() {
            Integer charId = 1;
            doNothing().when(materialService).deleteMaterialCharacteristics(charId);

            ResponseEntity<ResponseDto> response = materialController.deleteMaterialCharacteristics(charId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("200", response.getBody().getStatusCode());
            assertEquals("Characteristics deleted successfully", response.getBody().getStatusMsg());

            verify(materialService, times(1)).deleteMaterialCharacteristics(eq(charId));
        }
    }
     
