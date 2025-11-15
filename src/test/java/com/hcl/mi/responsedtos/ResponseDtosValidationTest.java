package com.hcl.mi.responsedtos;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ResponseDtosValidationTest {

    private Validator validator;

    @BeforeEach
    void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    @Test
    void materialDto_valid_ok() {
        MaterialDto dto = new MaterialDto();
        dto.setMaterialId("M100");
        dto.setMaterialDesc("High grade steel rods");
        dto.setType("RAW");
        dto.setStatus(true);

        Set<ConstraintViolation<MaterialDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void materialDto_descTooShort_violatesSize() {
        MaterialDto dto = new MaterialDto();
        dto.setMaterialId("M100");
        dto.setMaterialDesc("abcd");
        dto.setType("RAW");

        Set<ConstraintViolation<MaterialDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("materialDesc") &&
                "material description should be greater than 5 char and less than 256 char".equals(v.getMessage())
        ));
    }

    @Test
    void materialDto_blankType_violatesNotBlank() {
        MaterialDto dto = new MaterialDto();
        dto.setMaterialId("M100");
        dto.setMaterialDesc("Some valid description");
        dto.setType("   ");

        Set<ConstraintViolation<MaterialDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("type") &&
                "invalid material type".equals(v.getMessage())
        ));
    }


    @Test
    void plantDto_valid_ok() {
        PlantDto dto = new PlantDto();
        dto.setPlantId("PL01");
        dto.setPlantName("Pune Plant");
        dto.setStatus(true);
        dto.setState("Maharashtra");
        dto.setCity("Pune");

        Set<ConstraintViolation<PlantDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void plantDto_blankPlantId_violatesNotBlank() {
        PlantDto dto = new PlantDto();
        dto.setPlantId("  ");
        dto.setPlantName("Pune Plant");
        dto.setState("Maharashtra");
        dto.setCity("Pune");

        Set<ConstraintViolation<PlantDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("plantId") &&
                "please provide valid plantId".equals(v.getMessage())
        ));
    }

    @Test
    void plantDto_blankCityAndState_violatesNotBlank() {
        PlantDto dto = new PlantDto();
        dto.setPlantId("PL01");
        dto.setPlantName("Plant");
        dto.setState("  ");
        dto.setCity("");

        Set<ConstraintViolation<PlantDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("state") &&
                "please provide valid state".equals(v.getMessage())
        ));
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("city") &&
                "please provide valid city".equals(v.getMessage())
        ));
    }

    @Test
    void plantDto_blankName_violatesNotBlank() {
        PlantDto dto = new PlantDto();
        dto.setPlantId("PL01");
        dto.setPlantName(" ");
        dto.setState("MH");
        dto.setCity("Pune");

        Set<ConstraintViolation<PlantDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("plantName") &&
                "invalid plant name".equals(v.getMessage())
        ));
    }


    @Test
    void vendorDto_valid_ok() {
        VendorDto dto = new VendorDto();
        dto.setVendorId(10);
        dto.setName("Reliable Vendor Pvt Ltd");
        dto.setEmail("vendor@example.com");
        dto.setPhoneNumber("9999999999");
        dto.setStatus(true);
        dto.setState("MH");
        dto.setCity("Pune");

        Set<ConstraintViolation<VendorDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void vendorDto_nameTooShort_violatesSize() {
        VendorDto dto = new VendorDto();
        dto.setName("ab"); 
        dto.setEmail("vendor@example.com");

        Set<ConstraintViolation<VendorDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("name") &&
                "vendor name should be min 5 char and max 50".equals(v.getMessage())
        ));
    }

    @Test
    void vendorDto_invalidEmail_violatesEmail() {
        VendorDto dto = new VendorDto();
        dto.setName("Some Vendor");
        dto.setEmail("not-an-email");

        Set<ConstraintViolation<VendorDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("email") &&
                "please provide valid email".equals(v.getMessage())
        ));
    }


    @Test
    void micDto_valid_ok() {
        MaterialInspectionCharacteristicsDto dto = new MaterialInspectionCharacteristicsDto();
        dto.setCharacteristicId(1);
        dto.setCharacteristicDescription("Hardness Test");
        dto.setUpperToleranceLimit(50.0);
        dto.setLowerToleranceLimit(10.0);
        dto.setUnitOfMeasure("HRC");
        dto.setMatId("M101");

        Set<ConstraintViolation<MaterialInspectionCharacteristicsDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
        assertTrue(dto.isToleranceRangeValid());
    }

    @Test
    void micDto_nullDescription_violatesNotNull() {
        MaterialInspectionCharacteristicsDto dto = new MaterialInspectionCharacteristicsDto();
        dto.setCharacteristicDescription(null);
        dto.setUpperToleranceLimit(10.0);
        dto.setLowerToleranceLimit(5.0);

        Set<ConstraintViolation<MaterialInspectionCharacteristicsDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("characteristicDescription") &&
                "Characteristic description must not be null".equals(v.getMessage())
        ));
    }

    @Test
    void micDto_emptyDescription_violatesSize_min1() {
        MaterialInspectionCharacteristicsDto dto = new MaterialInspectionCharacteristicsDto();
        dto.setCharacteristicDescription(""); // size < 1
        dto.setUpperToleranceLimit(10.0);
        dto.setLowerToleranceLimit(5.0);

        Set<ConstraintViolation<MaterialInspectionCharacteristicsDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("characteristicDescription") &&
                "Characteristic description must not be empty".equals(v.getMessage())
        ));
    }

    @Test
    void micDto_nullUpperLower_violatesNotNull() {
        MaterialInspectionCharacteristicsDto dto = new MaterialInspectionCharacteristicsDto();
        dto.setCharacteristicDescription("Test");
        dto.setUpperToleranceLimit(null);
        dto.setLowerToleranceLimit(null);

        Set<ConstraintViolation<MaterialInspectionCharacteristicsDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("upperToleranceLimit") &&
                "Upper tolerance limit must not be null".equals(v.getMessage())
        ));
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("lowerToleranceLimit") &&
                "Lower tolerance limit must not be null".equals(v.getMessage())
        ));
    }

    @Test
    void micDto_negativeLimits_violatesPositiveOrZero() {
        MaterialInspectionCharacteristicsDto dto = new MaterialInspectionCharacteristicsDto();
        dto.setCharacteristicDescription("Test");
        dto.setUpperToleranceLimit(-1.0);
        dto.setLowerToleranceLimit(-2.0);

        Set<ConstraintViolation<MaterialInspectionCharacteristicsDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("upperToleranceLimit") &&
                "Upper tolerance limit must be zero or positive".equals(v.getMessage())
        ));
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("lowerToleranceLimit") &&
                "Lower tolerance limit must be zero or positive".equals(v.getMessage())
        ));
    }

    @Test
    void micDto_toleranceAssertTrue_violatesWhenLowerGtUpper() {
        MaterialInspectionCharacteristicsDto dto = new MaterialInspectionCharacteristicsDto();
        dto.setCharacteristicDescription("Test");
        dto.setUpperToleranceLimit(5.0);
        dto.setLowerToleranceLimit(10.0); 

        Set<ConstraintViolation<MaterialInspectionCharacteristicsDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("toleranceRangeValid") &&
                "Lower tolerance limit must be less than or equal to upper tolerance limit".equals(v.getMessage())
        ));
    }


    @Test
    void responseDto_gettersSetters_ok() {
        ResponseDto dto = new ResponseDto();
        dto.setStatusCode("201");
        dto.setStatusMsg("Created");

        assertEquals("201", dto.getStatusCode());
        assertEquals("Created", dto.getStatusMsg());

        ResponseDto dto2 = new ResponseDto("200", "OK");
        assertEquals("200", dto2.getStatusCode());
        assertEquals("OK", dto2.getStatusMsg());
    }


    @Test
    void lotActualsAndCharsResponseDto_builder_ok() {
        LotActualsAndCharacteristicsResponseDto dto = LotActualsAndCharacteristicsResponseDto.builder()
                .lotId(101)
                .sNo(1)
                .characteristicId(55)
                .characteristicDesc("Hardness")
                .upperToleranceLimit(50.0)
                .lowerToleranceLimit(10.0)
                .unitOfMeasure("HRC")
                .actualUtl(48.5)
                .actualLtl(12.3)
                .build(); 

        assertEquals(101, dto.getLotId());
        assertEquals(1, dto.getSNo());
        assertEquals(55, dto.getCharacteristicId());
        assertEquals("Hardness", dto.getCharacteristicDesc());
        assertEquals(50.0, dto.getUpperToleranceLimit());
        assertEquals(10.0, dto.getLowerToleranceLimit());
        assertEquals("HRC", dto.getUnitOfMeasure());
        assertEquals(48.5, dto.getActualUtl());
        assertEquals(12.3, dto.getActualLtl());
    }
}