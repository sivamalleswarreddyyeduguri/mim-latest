package com.hcl.mi.entities;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class EntitiesPojoAndRelationsTest {


    @Test
    void baseEntity_fields_areNull_byDefault() {
        Material material = new Material();
        assertNull(material.getCreatedAt(), "createdAt should be null before persistence/auditing");
        assertNull(material.getCreatedBy(), "createdBy should be null before persistence/auditing");
        assertNull(material.getUpdatedAt(), "updatedAt should be null before persistence/auditing");
        assertNull(material.getUpdatedBy(), "updatedBy should be null before persistence/auditing");

        LocalDateTime now = LocalDateTime.now();
        material.setCreatedAt(now);
        material.setCreatedBy("tester");
        material.setUpdatedAt(now);
        material.setUpdatedBy("tester2");

        assertEquals(now, material.getCreatedAt());
        assertEquals("tester", material.getCreatedBy());
        assertEquals(now, material.getUpdatedAt());
        assertEquals("tester2", material.getUpdatedBy());
    }


    @Test
    void material_entity_basic_fields_and_lists() {
        Material mat = new Material();
        mat.setMaterialId("M101");
        mat.setMaterialDesc("High grade steel rod");
        mat.setType("RAW");
        mat.setStatus(true);

        assertEquals("M101", mat.getMaterialId());
        assertEquals("High grade steel rod", mat.getMaterialDesc());
        assertEquals("RAW", mat.getType());
        assertTrue(mat.isStatus());

        assertNotNull(mat.getMaterialChar(), "materialChar list should be initialized");
        mat.getMaterialChar().add(new MaterialInspectionCharacteristics());
        assertEquals(1, mat.getMaterialChar().size());

        mat.setInspectionLot(new ArrayList<>());
        assertNotNull(mat.getInspectionLot());
        mat.getInspectionLot().add(new InspectionLot(4001));
        assertEquals(1, mat.getInspectionLot().size());
    }


    @Test
    void materialInspectionCharacteristics_basic_fields_and_link_to_material() {
        Material mat = new Material();
        mat.setMaterialId("M111");

        MaterialInspectionCharacteristics mic = new MaterialInspectionCharacteristics();
        mic.setCharacteristicId(101);
        mic.setCharacteristicDescription("Hardness Test");
        mic.setUpperToleranceLimit(50.0);
        mic.setLowerToleranceLimit(10.0);
        mic.setUnitOfMeasure("HRC");
        mic.setMaterial(mat);

        assertEquals(101, mic.getCharacteristicId());
        assertEquals("Hardness Test", mic.getCharacteristicDescription());
        assertEquals(50.0, mic.getUpperToleranceLimit());
        assertEquals(10.0, mic.getLowerToleranceLimit());
        assertEquals("HRC", mic.getUnitOfMeasure());
        assertEquals(mat, mic.getMaterial());

        assertNotNull(mic.getInspectionActuals());
        mic.getInspectionActuals().add(new InspectionActuals());
        assertEquals(1, mic.getInspectionActuals().size());
    }


    @Test
    void plant_entity_basic_fields() {
        Plant plant = new Plant();
        plant.setPlantId("PL01");
        plant.setPlantName("Pune Plant");
        plant.setStatus(true);
        plant.setState("MH");
        plant.setCity("Pune");

        assertEquals("PL01", plant.getPlantId());
        assertEquals("Pune Plant", plant.getPlantName());
        assertTrue(plant.isStatus());
        assertEquals("MH", plant.getState());
        assertEquals("Pune", plant.getCity());

        plant.setInspectionLot(new ArrayList<>());
        plant.getInspectionLot().add(new InspectionLot(4002));
        assertEquals(1, plant.getInspectionLot().size());
    }


    @Test
    void vendor_entity_basic_fields() {
        Vendor vendor = new Vendor(); 
        vendor.setVendorId(10);
        vendor.setName("Reliable Vendor Pvt Ltd");
        vendor.setStatus(true);
        vendor.setState("MH");
        vendor.setCity("Pune");

        assertEquals(10, vendor.getVendorId());
        assertEquals("Reliable Vendor Pvt Ltd", vendor.getName());
        assertTrue(vendor.isStatus());
        assertEquals("MH", vendor.getState());
        assertEquals("Pune", vendor.getCity());
    }


    @Test
    void inspectionLot_builderDefault_list_initialized_and_relationships() {
        Material mat = new Material();
        mat.setMaterialId("M200");

        Vendor vendor = new Vendor();
        vendor.setVendorId(55);
        vendor.setName("Test Vendor");

        Plant plant = new Plant();
        plant.setPlantId("PL02");
        plant.setPlantName("Plant Two");

        InspectionLot lot = InspectionLot.builder()
                .lotId(4005)
                .creationDate(LocalDate.of(2025, 1, 5))
                .inspectionStartDate(LocalDate.of(2025, 1, 6))
                .inspectionEndDate(LocalDate.of(2025, 1, 7))
                .result("PASS")
                .remarks("All measurements within tolerance")
                .material(mat)
                .vendor(vendor)
                .plant(plant)
                .build();

        assertEquals(4005, lot.getLotId());
        assertEquals(LocalDate.of(2025, 1, 5), lot.getCreationDate());
        assertEquals(LocalDate.of(2025, 1, 6), lot.getInspectionStartDate());
        assertEquals(LocalDate.of(2025, 1, 7), lot.getInspectionEndDate());
        assertEquals("PASS", lot.getResult());
        assertEquals("All measurements within tolerance", lot.getRemarks());
        assertEquals(mat, lot.getMaterial());
        assertEquals(vendor, lot.getVendor());
        assertEquals(plant, lot.getPlant());

        assertNotNull(lot.getInspectionActuals(), "inspectionActuals should be initialized by @Builder.Default");
        assertEquals(0, lot.getInspectionActuals().size());

        InspectionActuals act = InspectionActuals.builder()
                .actualId(999)
                .inspectionLot(lot)
                .materialInspectionCharacteristics(new MaterialInspectionCharacteristics())
                .maximumMeasurement(48.5)
                .minimumMeasurement(12.3)
                .build();

        lot.getInspectionActuals().add(act);
        assertEquals(1, lot.getInspectionActuals().size());
        assertEquals(lot, act.getInspectionLot());
    }


    @Test
    void inspectionActuals_builder_and_constructor_work() {
        InspectionLot lot = new InspectionLot(4001);
        MaterialInspectionCharacteristics mic = new MaterialInspectionCharacteristics();
        mic.setCharacteristicId(101);

        InspectionActuals built = InspectionActuals.builder()
                .actualId(1)
                .inspectionLot(lot)
                .materialInspectionCharacteristics(mic)
                .maximumMeasurement(50.0)
                .minimumMeasurement(10.0)
                .build();

        assertEquals(1, built.getActualId());
        assertEquals(lot, built.getInspectionLot());
        assertEquals(mic, built.getMaterialInspectionCharacteristics());
        assertEquals(50.0, built.getMaximumMeasurement());
        assertEquals(10.0, built.getMinimumMeasurement());

        InspectionActuals constructed = new InspectionActuals(2, lot, mic, 49.9, 12.1);
        assertEquals(2, constructed.getActualId());
        assertEquals(lot, constructed.getInspectionLot());
        assertEquals(mic, constructed.getMaterialInspectionCharacteristics());
        assertEquals(49.9, constructed.getMaximumMeasurement());
        assertEquals(12.1, constructed.getMinimumMeasurement());
    }


    @Test
    void inspectionLot_toString_contains_key_fields() {
        InspectionLot lot = new InspectionLot();
        lot.setLotId(4010);
        Material mat = new Material();
        mat.setMaterialId("M300");
        Vendor vendor = new Vendor();
        vendor.setVendorId(77);
        Plant plant = new Plant();
        plant.setPlantId("PL77");

        lot.setMaterial(mat);
        lot.setVendor(vendor);
        lot.setPlant(plant);
        lot.setCreationDate(LocalDate.of(2025, 1, 10));

        String ts = lot.toString();
        assertTrue(ts.contains("lotId=4010"));
        assertTrue(ts.contains("creationDate="));
        assertTrue(ts.contains("material="));
        assertTrue(ts.contains("vendor="));
        assertTrue(ts.contains("plant="));
    }
}
