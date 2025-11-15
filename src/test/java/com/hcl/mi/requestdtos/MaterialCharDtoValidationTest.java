package com.hcl.mi.requestdtos;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

class MaterialCharDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validMaterialChar_noViolations() {
        MaterialCharDto dto = MaterialCharDto.builder()
                .characteristicId(1)
                .charDesc("Hardness Test")
                .utl(50.0)
                .ltl(10.0)
                .uom("HRC")
                .matId("M101")
                .build();

        Set<ConstraintViolation<MaterialCharDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
        assertTrue(dto.isToleranceRangeValid());
    }

    @Test
    void charDescTooShort_violatesSize() {
        MaterialCharDto dto = MaterialCharDto.builder()
                .characteristicId(1)
                .charDesc("abcd") // 4 chars < 5
                .utl(50.0)
                .ltl(10.0)
                .uom("HRC")
                .matId("M101")
                .build();

        Set<ConstraintViolation<MaterialCharDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("charDesc") &&
                "Characteristic description should be between 5 and 256 characters".equals(v.getMessage())
        ));
    }

    @Test
    void blankUom_violatesNotBlank() {
        MaterialCharDto dto = MaterialCharDto.builder()
                .characteristicId(1)
                .charDesc("Valid description")
                .utl(50.0)
                .ltl(10.0)
                .uom("   ")
                .matId("M101")
                .build();

        Set<ConstraintViolation<MaterialCharDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("uom") &&
                "Unit of measurement should not be empty".equals(v.getMessage())
        ));
    }

    @Test
    void blankMatId_violatesNotBlank() {
        MaterialCharDto dto = MaterialCharDto.builder()
                .characteristicId(1)
                .charDesc("Valid description")
                .utl(50.0)
                .ltl(10.0)
                .uom("HRC")
                .matId("")
                .build();

        Set<ConstraintViolation<MaterialCharDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("matId") &&
                "Invalid material ID".equals(v.getMessage())
        ));
    }

    @Test
    void toleranceViolation_ltGreaterThanUtl_violatesAssertTrue() {
        MaterialCharDto dto = MaterialCharDto.builder()
                .characteristicId(1)
                .charDesc("Valid description")
                .utl(10.0)
                .ltl(15.0) // ltl > utl
                .uom("HRC")
                .matId("M101")
                .build();

        Set<ConstraintViolation<MaterialCharDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("toleranceRangeValid") &&
                "Lower tolerance limit must be less than or equal to upper tolerance limit".equals(v.getMessage())
        ));
    }
}
