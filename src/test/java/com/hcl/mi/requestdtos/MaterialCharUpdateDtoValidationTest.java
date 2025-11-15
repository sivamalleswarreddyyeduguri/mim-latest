package com.hcl.mi.requestdtos;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

class MaterialCharUpdateDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validUpdate_noViolations() {
        MaterialCharUpdateDto dto = new MaterialCharUpdateDto();
        dto.setCharacteristicId(1);
        dto.setCharDesc("Updated Description");
        dto.setUtl(60.0);
        dto.setLtl(40.0);
        dto.setUom("HRC");

        Set<ConstraintViolation<MaterialCharUpdateDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
        assertTrue(dto.isToleranceRangeValid());
    }

    @Test
    void nullCharacteristicId_violatesNotNull() {
        MaterialCharUpdateDto dto = new MaterialCharUpdateDto();
        dto.setCharacteristicId(null);
        dto.setCharDesc("Updated Description");
        dto.setUtl(60.0);
        dto.setLtl(40.0);
        dto.setUom("HRC");

        Set<ConstraintViolation<MaterialCharUpdateDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("characteristicId") &&
                "Characteristic id is required".equals(v.getMessage())
        ));
    }

    @Test
    void uomNull_violatesNotNull() {
        MaterialCharUpdateDto dto = new MaterialCharUpdateDto();
        dto.setCharacteristicId(1);
        dto.setCharDesc("Updated Description");
        dto.setUtl(60.0);
        dto.setLtl(40.0);
        dto.setUom(null);

        Set<ConstraintViolation<MaterialCharUpdateDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("uom") &&
                "pls provide uom".equals(v.getMessage())
        ));
    }

    @Test
    void toleranceViolation_ltGreaterThanUtl_violatesAssertTrue() {
        MaterialCharUpdateDto dto = new MaterialCharUpdateDto();
        dto.setCharacteristicId(1);
        dto.setCharDesc("Updated Description");
        dto.setUtl(40.0);
        dto.setLtl(60.0); // invalid
        dto.setUom("HRC");

        Set<ConstraintViolation<MaterialCharUpdateDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("toleranceRangeValid") &&
                "Lower tolerance limit must be less than or equal to upper tolerance limit".equals(v.getMessage())
        ));
    }
}
