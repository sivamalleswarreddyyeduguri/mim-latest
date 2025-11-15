package com.hcl.mi.servicesImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.hcl.mi.entities.Plant;
import com.hcl.mi.exceptions.GenericAlreadyExistsException;
import com.hcl.mi.exceptions.GenericNotFoundException;
import com.hcl.mi.repositories.PlantRepository;
import com.hcl.mi.responsedtos.PlantDto;

class PlantServiceImplTest {

    @Mock
    private PlantRepository plantRepository;

    @InjectMocks
    private PlantServiceImpl plantService;

    private Plant plant;
    private PlantDto plantDto;

    @BeforeEach
    void setUp() { 
        MockitoAnnotations.openMocks(this);

        plant = new Plant();
        plant.setPlantId("PL-101");
        plant.setPlantName("GREEN PLANT");
        plant.setStatus(true);
        plant.setState("MAHARASHTRA");
        plant.setCity("MUMBAI");

        plantDto = new PlantDto();
        plantDto.setPlantId("  pl-101  ");
        plantDto.setPlantName("  Green   Plant  ");
        plantDto.setStatus(true);
        plantDto.setState("maharashtra");
        plantDto.setCity("mumbai");
    }

    @Test
    void testAddNewPlant_Success() {
        when(plantRepository.findById("  PL-101  ")).thenReturn(Optional.empty());
        when(plantRepository.findByPlantName("  GREEN   PLANT  ")).thenReturn(Optional.empty());
        when(plantRepository.save(any(Plant.class))).thenReturn(plant);

        assertDoesNotThrow(() -> plantService.addNewPlant(plantDto));

        verify(plantRepository).findById("  PL-101  ");
        verify(plantRepository).findByPlantName("  GREEN   PLANT  ");

        ArgumentCaptor<Plant> captor = ArgumentCaptor.forClass(Plant.class);
        verify(plantRepository).save(captor.capture());
        Plant saved = captor.getValue();
        assertEquals("PL-101", saved.getPlantId(), "Plant ID should be spaces-removed and uppercased");
        assertEquals("GREEN PLANT", saved.getPlantName(), "Plant Name should have extra spaces removed and be uppercased");
        assertTrue(saved.isStatus());
        assertEquals("maharashtra", saved.getState());
        assertEquals("mumbai", saved.getCity());
    }

    @Test
    void testAddNewPlant_AlreadyExists_ById() {
        when(plantRepository.findById("  PL-101  ")).thenReturn(Optional.of(plant));
        when(plantRepository.findByPlantName("  GREEN   PLANT  ")).thenReturn(Optional.empty());

        GenericAlreadyExistsException ex = assertThrows(GenericAlreadyExistsException.class,
                () -> plantService.addNewPlant(plantDto));

        assertEquals("Plant with given ID or Name already exists.", ex.getMessage());
        verify(plantRepository, never()).save(any());
    }

    @Test
    void testAddNewPlant_AlreadyExists_ByName() {
        when(plantRepository.findById("  PL-101  ")).thenReturn(Optional.empty());
        when(plantRepository.findByPlantName("  GREEN   PLANT  ")).thenReturn(Optional.of(plant));

        GenericAlreadyExistsException ex = assertThrows(GenericAlreadyExistsException.class,
                () -> plantService.addNewPlant(plantDto));

        assertEquals("Plant with given ID or Name already exists.", ex.getMessage());
        verify(plantRepository, never()).save(any());
    }

    @Test
    void testGetAllPlants() {
        when(plantRepository.findAll()).thenReturn(List.of(plant));

        List<PlantDto> result = plantService.getAllPlants();

        assertEquals(1, result.size());
        assertEquals("PL-101", result.get(0).getPlantId());
        assertEquals("GREEN PLANT", result.get(0).getPlantName());
        assertTrue(result.get(0).isStatus());
        assertEquals("MAHARASHTRA", result.get(0).getState());
        assertEquals("MUMBAI", result.get(0).getCity());
        verify(plantRepository).findAll();
    }

    @Test
    void testGetPlant_Success() {
        when(plantRepository.findById("PL-101")).thenReturn(Optional.of(plant));

        PlantDto result = plantService.getPlant("pl-101");

        assertEquals("PL-101", result.getPlantId());
        assertEquals("GREEN PLANT", result.getPlantName());
        verify(plantRepository).findById("PL-101");
    }

    @Test
    void testGetPlant_NotFound() {
        when(plantRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> plantService.getPlant("unknown"));

        assertEquals("Plant with ID 'unknown' not found.", ex.getMessage());
        verify(plantRepository).findById("UNKNOWN");
    }

    @Test
    void testSaveEditedPlant_Success() {
        when(plantRepository.findById("PL-101")).thenReturn(Optional.of(plant));

        plantDto.setPlantId("  pl-101  "); 
        plantDto.setPlantName("  New   Name  ");
        plantDto.setStatus(false);
        plantDto.setState("telangana");
        plantDto.setCity("hyderabad");

        plantService.saveEditedPlant(plantDto);

        ArgumentCaptor<Plant> captor = ArgumentCaptor.forClass(Plant.class);
        verify(plantRepository).save(captor.capture());
        Plant saved = captor.getValue();

        assertEquals("PL-101", saved.getPlantId());
        assertEquals("NEW NAME", saved.getPlantName());
        assertFalse(saved.isStatus());
        assertEquals("telangana", saved.getState());
        assertEquals("hyderabad", saved.getCity());

        verify(plantRepository).findById("PL-101");
    }

    @Test
    void testSaveEditedPlant_NotFound() {
        when(plantRepository.findById("PL-999")).thenReturn(Optional.empty());

        plantDto.setPlantId("  pl-999  ");
        plantDto.setPlantName("Whatever");

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> plantService.saveEditedPlant(plantDto));

        assertEquals("Plant with ID PL-999 does not exist.", ex.getMessage());
        verify(plantRepository, never()).save(any());
    }

    @Test
    void testDeletePlant_Success() {
        when(plantRepository.findById("PL-101")).thenReturn(Optional.of(plant));

        plantService.deletePlant("pl-101");

        ArgumentCaptor<Plant> captor = ArgumentCaptor.forClass(Plant.class);
        verify(plantRepository).save(captor.capture());
        Plant saved = captor.getValue();
        assertEquals("PL-101", saved.getPlantId());
        assertFalse(saved.isStatus());
        assertEquals("GREEN PLANT", saved.getPlantName());
        assertEquals("MAHARASHTRA", saved.getState());
        assertEquals("MUMBAI", saved.getCity());

        verify(plantRepository).findById("PL-101");
    }

    @Test
    void testDeletePlant_NotFound() {
        when(plantRepository.findById("PL-404")).thenReturn(Optional.empty());

        GenericNotFoundException ex = assertThrows(GenericNotFoundException.class,
                () -> plantService.deletePlant("pl-404"));

        assertEquals("Plant with ID 'pl-404' not found.", ex.getMessage());
        verify(plantRepository, never()).save(any());
        verify(plantRepository).findById("PL-404");
    }
}