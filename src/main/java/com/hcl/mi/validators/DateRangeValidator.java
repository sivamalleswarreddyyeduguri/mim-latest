package com.hcl.mi.validators;

import com.hcl.mi.customannotation.ValidDateRange;
import com.hcl.mi.requestdtos.LotCreationDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, LotCreationDto> {

    @Override
    public boolean isValid(LotCreationDto dto, ConstraintValidatorContext context) {
        if (dto.getStDt() == null || dto.getCrDt() == null) {
            return true;
        }
        return !dto.getStDt().isBefore(dto.getCrDt());
    }
}
 