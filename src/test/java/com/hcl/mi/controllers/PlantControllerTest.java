package com.hcl.mi.controllers;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hcl.mi.responsedtos.PlantDto;
import com.hcl.mi.responsedtos.ResponseDto;
import com.hcl.mi.services.PlantService;

class PlantControllerTest {

    @Mock
    private PlantService plantService;

    @InjectMocks
    private PlantController plantController;

    private PlantDto plantDto;

    @BeforeEach 
    void setUp() {
        MockitoAnnotations.openMocks(this);
        plantDto = new PlantDto();
        plantDto.setPlantId("P001");
        plantDto.setPlantName("Hyderabad Plant");
        plantDto.setState("Telangana");
        plantDto.setCity("Hyderabad");
        plantDto.setStatus(true);
    }

    @Test
    void testAddNewPlant() {
        doNothing().when(plantService).addNewPlant(any(PlantDto.class));

        ResponseEntity<ResponseDto> response = plantController.addNewPlant(plantDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("201", response.getBody().getStatusCode());
        assertEquals("Plant saved successfully", response.getBody().getStatusMsg());

        verify(plantService, times(1)).addNewPlant(any(PlantDto.class));
    }

    @Test
    void testGetPlantDetails() {
        when(plantService.getPlant("P001")).thenReturn(plantDto);

        ResponseEntity<PlantDto> response = plantController.getPlantDetails("P001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Hyderabad Plant", response.getBody().getPlantName());
        verify(plantService, times(1)).getPlant("P001");
    }

    @Test
    void testGetAllPlants() {
        when(plantService.getAllPlants()).thenReturn(List.of(plantDto));

        ResponseEntity<List<PlantDto>> response = plantController.getAllPlant();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(plantService, times(1)).getAllPlants();
    }

    @Test
    void testUpdatePlantDetails() {
        doNothing().when(plantService).saveEditedPlant(any(PlantDto.class));

        ResponseEntity<ResponseDto> response = plantController.updatePlantDetails(plantDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("200", response.getBody().getStatusCode());
        assertEquals("plant details are updated", response.getBody().getStatusMsg());

        verify(plantService, times(1)).saveEditedPlant(any(PlantDto.class));
    }

    @Test
    void testDeletePlant() {
        doNothing().when(plantService).deletePlant("P001");

        ResponseEntity<ResponseDto> response = plantController.deletePlant("P001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("200", response.getBody().getStatusCode());
        assertEquals("plant deleted successfully", response.getBody().getStatusMsg());

        verify(plantService, times(1)).deletePlant("P001");
    }
}