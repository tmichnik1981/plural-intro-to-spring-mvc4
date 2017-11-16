package com.pluralsight.view;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PhoneConstraintValidator implements ConstraintValidator<Phone, String> {

	@Override
	public void initialize(Phone arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isValid(String phoneField, ConstraintValidatorContext ctx) {
		if (phoneField == null) {
			return false;
		}
		return phoneField.matches("[0-9()-]*");
	}

}
