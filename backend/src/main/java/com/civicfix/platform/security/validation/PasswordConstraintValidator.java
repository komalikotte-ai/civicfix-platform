package com.civicfix.platform.security.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 100;

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            return false;
        }

        if (WHITESPACE_PATTERN.matcher(password).find()) {
            return false;
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            return false;
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            return false;
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            return false;
        }

        return SPECIAL_CHAR_PATTERN.matcher(password).find();
    }
}
