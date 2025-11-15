package com.hcl.mi.requestdtos;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

class DateRangeLotSearchValidationTest {

    private Validator validator;

    @BeforeEach
    void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validDates_noViolations() {
        DateRangeLotSearch dto = new DateRangeLotSearch();
        dto.setFromDate(LocalDate.of(2025, 1, 1));
        dto.setToDate(LocalDate.of(2025, 1, 31));
        dto.setMaterialId("M101");
        dto.setVendorId(10); // primitive int, has no @NotNull, but value set
        dto.setPlantId("PL01");
        dto.setStatus("OPEN");

        Set<ConstraintViolation<DateRangeLotSearch>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullFromDate_violatesNotNull() {
        DateRangeLotSearch dto = new DateRangeLotSearch();
        dto.setFromDate(null);
        dto.setToDate(LocalDate.of(2025, 1, 31));

        Set<ConstraintViolation<DateRangeLotSearch>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("fromDate") &&
                "please provide valid date format of yyyy-MM-dd".equals(v.getMessage())
        ));
    }

    @Test
    void nullToDate_violatesNotNull() {
        DateRangeLotSearch dto = new DateRangeLotSearch();
        dto.setFromDate(LocalDate.of(2025, 1, 1));
        dto.setToDate(null);

        Set<ConstraintViolation<DateRangeLotSearch>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("toDate") &&
                "please provide valid date format of yyyy-MM-dd".equals(v.getMessage())
        ));
    }
}
