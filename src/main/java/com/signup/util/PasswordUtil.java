package com.signup.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Hash password using BCrypt (automatically handles salt)
     * @param password plain text password
     * @return hashed password
     */
    public static String hashPassword(String password) {
        return encoder.encode(password);
    }

    /**
     * Verify password against stored hash
     * @param password plain text password to verify
     * @param storedHash stored hash
     * @return true if password matches
     */
    public static boolean verifyPassword(String password, String storedHash) {
        return encoder.matches(password, storedHash);
    }

    /**
     * Validate password strength
     * @param password password to validate
     * @return true if password meets requirements
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        // Check for at least one uppercase letter
        boolean hasUpperCase = password.matches(".*[A-Z].*");
        
        // Check for at least one lowercase letter
        boolean hasLowerCase = password.matches(".*[a-z].*");
        
        // Check for at least one digit
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasUpperCase && hasLowerCase && hasDigit;
    }
} 