package com.hcl.mi.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.hcl.mi.requestdtos.DateRangeLotSearch;
import com.hcl.mi.requestdtos.EditLotDto;
import com.hcl.mi.requestdtos.LotActualDto;
import com.hcl.mi.requestdtos.LotCreationDto;
import com.hcl.mi.responsedtos.DateRangeLotResponseDto;
import com.hcl.mi.responsedtos.InspectionLotDto;
import com.hcl.mi.responsedtos.LotActualsAndCharacteristicsResponseDto;
import com.hcl.mi.responsedtos.ResponseDto;
import com.hcl.mi.servicesImpl.InspectionServiceImpl;

class InspectionLotControllerTest {

    @Mock
    private InspectionServiceImpl inspectionService;

    @InjectMocks
    private InspectionLotController inspectionLotController;

    private LotCreationDto lotCreationDto;
    private LotActualDto lotActualDto;
    private EditLotDto editLotDto;
    private InspectionLotDto inspectionLotDto;
    private DateRangeLotSearch dateRangeSearch;
    private DateRangeLotResponseDto dateRangeResponse;
    private LotActualsAndCharacteristicsResponseDto actualsResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
 
        lotCreationDto = new LotCreationDto();
        lotCreationDto.setMatId("MAT001");
        lotCreationDto.setPlantId("PLANT01");
        lotCreationDto.setVendorId(1001);
        lotCreationDto.setStDt(LocalDate.now().minusDays(2));
        lotCreationDto.setCrDt(LocalDate.now());

        lotActualDto = new LotActualDto();
        lotActualDto.setLotId(1);
        lotActualDto.setCharId(10);
        lotActualDto.setMaxMeas(55.0);
        lotActualDto.setMinMeas(45.0);

        editLotDto = new EditLotDto();
        editLotDto.setId(1);
        editLotDto.setResult("Pass");
        editLotDto.setRemarks("Good quality");
        editLotDto.setDate(LocalDate.now());

        inspectionLotDto = new InspectionLotDto();
        inspectionLotDto.setLotId(1);
        inspectionLotDto.setResult("Pass");
        inspectionLotDto.setRemarks("Checked");
        inspectionLotDto.setUserName("Inspector1");

        dateRangeSearch = new DateRangeLotSearch(
                LocalDate.now().minusDays(10),
                LocalDate.now(),
                "MAT001",
                1001,
                "PLANT01",
                "Active"
        );

        dateRangeResponse = DateRangeLotResponseDto.builder()
                .lotId(1)
                .createdOn(LocalDate.now())
                .startOn(LocalDate.now().minusDays(1))
                .endOn(LocalDate.now())
                .result("Pass")
                .inspectedBy("Inspector1")
                .material("Steel")
                .plant("PlantA")
                .vendor("VendorA")
                .build();

        actualsResponse = LotActualsAndCharacteristicsResponseDto.builder()
                .lotId(1)
                .sNo(1)
                .characteristicId(10)
                .characteristicDesc("Hardness")
                .upperToleranceLimit(60.0)
                .lowerToleranceLimit(40.0)
                .unitOfMeasure("HRC")
                .actualUtl(55.0)
                .actualLtl(45.0)
                .build();
    }

    @Test
    void testAddInspectionLot() {
        doNothing().when(inspectionService).createInspectionLot(any(LotCreationDto.class));

        ResponseEntity<ResponseDto> response = inspectionLotController.addInspectionLot(lotCreationDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("201", response.getBody().getStatusCode());
        assertEquals("lot created successfully", response.getBody().getStatusMsg());
        verify(inspectionService, times(1)).createInspectionLot(any(LotCreationDto.class));
    }

    @Test
    void testFetchInspectionLotDetails() {
        when(inspectionService.getLotDetails(1)).thenReturn(inspectionLotDto);

        ResponseEntity<InspectionLotDto> response = inspectionLotController.fetchInspectionLotDetails(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pass", response.getBody().getResult());
        verify(inspectionService, times(1)).getLotDetails(1);
    }

    @Test
    void testGetActualsAndCharacteristicsOfLot() {
        when(inspectionService.getActualAndOriginalOfLot(1)).thenReturn(List.of(actualsResponse));

        ResponseEntity<List<LotActualsAndCharacteristicsResponseDto>> response =
                inspectionLotController.getActualsAndCharacteristicsOfLot(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(inspectionService, times(1)).getActualAndOriginalOfLot(1);
    }

    @Test
    void testAddInspectionActuals() {
        doNothing().when(inspectionService).saveInspActuals(any(LotActualDto.class));

        ResponseEntity<ResponseDto> response = inspectionLotController.addInspectionActuals(lotActualDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("201", response.getBody().getStatusCode());
        assertEquals("Inspection actuals saved successfully", response.getBody().getStatusMsg());
        verify(inspectionService, times(1)).saveInspActuals(any(LotActualDto.class));
    }

    @Test
    void testSaveEditedLot() {
        doNothing().when(inspectionService).updateInspectionLot(any(EditLotDto.class));

        ResponseEntity<ResponseDto> response = inspectionLotController.saveEditedLot(editLotDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Inspection lot updated successfully", response.getBody().getStatusMsg());
        verify(inspectionService, times(1)).updateInspectionLot(any(EditLotDto.class));
    }

    @Test
    void testDateRangeLotSearch() {
        when(inspectionService.getAllLotsDetailsBetweenDateRange(any(DateRangeLotSearch.class)))
                .thenReturn(List.of(dateRangeResponse));

        ResponseEntity<List<DateRangeLotResponseDto>> response =
                inspectionLotController.DateRangeLotSearch(dateRangeSearch);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Pass", response.getBody().get(0).getResult());
        verify(inspectionService, times(1)).getAllLotsDetailsBetweenDateRange(any(DateRangeLotSearch.class));
    } 

    @Test
    void testGetAllLotsWhoseInspectionActualNeedToAdded() {
        when(inspectionService.getAllInspectionLots()).thenReturn(List.of(inspectionLotDto));

        ResponseEntity<List<InspectionLotDto>> response =
                inspectionLotController.getAllLotsWhoseInspectionActualNeedToAdded();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(inspectionService, times(1)).getAllInspectionLots();
    }

    @Test
    void testEditInspectionActuals() {
        doNothing().when(inspectionService).saveInspActuals(any(LotActualDto.class));

        ResponseEntity<ResponseDto> response = inspectionLotController.editInspectionActuals(lotActualDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Inspection actuals updated successfully", response.getBody().getStatusMsg());
        verify(inspectionService, times(1)).saveInspActuals(any(LotActualDto.class));
    }

    @Test
    void testDownloadInspectionReportPdf() {
        byte[] pdfData = "PDF_CONTENT".getBytes(StandardCharsets.UTF_8);
        when(inspectionService.generateReportPdf(1)).thenReturn(pdfData);

        ResponseEntity<byte[]> response = inspectionLotController.downloadInspectionReportPdf(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(pdfData, response.getBody());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("InspectionReport_1.pdf"));
        verify(inspectionService, times(1)).generateReportPdf(1);
    }
}