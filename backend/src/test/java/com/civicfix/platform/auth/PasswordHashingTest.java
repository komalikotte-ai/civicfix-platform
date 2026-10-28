package com.civicfix.platform.auth;

import com.civicfix.platform.security.validation.PasswordConstraintValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHashingTest {

    private PasswordEncoder passwordEncoder;
    private PasswordConstraintValidator passwordValidator;

    @BeforeEach
    void setUp() {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.passwordValidator = new PasswordConstraintValidator();
    }

    @Test
    @DisplayName("Password Hashing - BCrypt hashes passwords and matches correctly")
    void testPasswordHashingAndMatching() {
        String rawPassword = "SecurePassword@2026";
        String encodedHash = passwordEncoder.encode(rawPassword);

        assertNotNull(encodedHash);
        assertNotEquals(rawPassword, encodedHash);
        assertTrue(encodedHash.startsWith("$2a$") || encodedHash.startsWith("$2b$"));

        // Matching
        assertTrue(passwordEncoder.matches(rawPassword, encodedHash));
        assertFalse(passwordEncoder.matches("WrongPassword@123", encodedHash));
    }

    @Test
    @DisplayName("Password Hashing - Different hashes generated for same password due to random salt")
    void testUniqueSaltsPerHash() {
        String rawPassword = "SecurePassword@2026";
        String hash1 = passwordEncoder.encode(rawPassword);
        String hash2 = passwordEncoder.encode(rawPassword);

        assertNotEquals(hash1, hash2);
        assertTrue(passwordEncoder.matches(rawPassword, hash1));
        assertTrue(passwordEncoder.matches(rawPassword, hash2));
    }

    @Test
    @DisplayName("Password Policy - Validates strong password patterns and rejects weak passwords")
    void testPasswordPolicyValidator() {
        // Valid passwords
        assertTrue(passwordValidator.isValid("StrongPass@123", null));
        assertTrue(passwordValidator.isValid("CivicFix#2026!Sec", null));
        assertTrue(passwordValidator.isValid("Admin_9988$$", null));

        // Invalid: too short (< 8 chars)
        assertFalse(passwordValidator.isValid("Sh0rt!", null));

        // Invalid: missing uppercase
        assertFalse(passwordValidator.isValid("lowercase123!", null));

        // Invalid: missing lowercase
        assertFalse(passwordValidator.isValid("UPPERCASE123!", null));

        // Invalid: missing digit
        assertFalse(passwordValidator.isValid("NoDigitsHere!@", null));

        // Invalid: missing special character
        assertFalse(passwordValidator.isValid("NoSpecialChar123", null));

        // Invalid: contains whitespace
        assertFalse(passwordValidator.isValid("Has Space@123", null));

        // Invalid: null
        assertFalse(passwordValidator.isValid(null, null));
    }
}
