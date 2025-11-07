package com.hcl.mi.validators;

import org.springframework.beans.factory.annotation.Value;

import com.hcl.mi.customannotation.StrongPassword;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class PasswordValidator implements ConstraintValidator<StrongPassword, String> {
	
//	private static String REG_EXP = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*()]).{8,}$";
	
	@Value("${password-validation}")
	private String REG_EXP;
	
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		System.out.println(REG_EXP);
		return value.matches(REG_EXP);
	}

}   
