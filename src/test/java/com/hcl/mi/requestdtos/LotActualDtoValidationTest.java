package com.hcl.mi.requestdtos;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

class LotActualDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validValues_noViolations() {
        LotActualDto dto = new LotActualDto();
        dto.setLotId(1);
        dto.setCharId(2);
        dto.setMaxMeas(50.0);
        dto.setMinMeas(10.0);

        Set<ConstraintViolation<LotActualDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void primitives_withDefault_zeroValues_stillNoNotNullViolations() {
        // Because fields are primitive, @NotNull can't trigger
        LotActualDto dto = new LotActualDto(); // all zeros by default
        Set<ConstraintViolation<LotActualDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Primitives cannot violate @NotNull; consider using Integer/Double");
    }
}
