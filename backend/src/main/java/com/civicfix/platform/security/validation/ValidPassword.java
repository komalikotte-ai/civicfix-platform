package com.civicfix.platform.security.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "Password must be 8-100 characters and contain at least one uppercase, one lowercase, one digit, and one special character without whitespace.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
