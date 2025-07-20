package com.signup.service;

import com.signup.database.DatabaseService;
import com.signup.dto.AuthResponse;
import com.signup.dto.LoginRequest;
import com.signup.dto.SignupRequest;
import com.signup.model.User;
import com.signup.util.JwtUtil;
import com.signup.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import com.signup.dto.AuthResponse.UserInfo;

@Service
public class AuthService {

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private JwtUtil jwtUtil;

    // Store the latest access token for each user (in-memory)
    private final Map<String, String> userTokenStore = new ConcurrentHashMap<>();
    
    // Store refresh tokens for each user (in-memory)
    private final Map<String, String> userRefreshTokenStore = new ConcurrentHashMap<>();

    /**
     * Register a new user
     * @param signupRequest signup request data
     * @return true if registration is successful, false otherwise
     */
    public boolean signup(SignupRequest signupRequest) {
        // Validate password strength
        if (!PasswordUtil.isValidPassword(signupRequest.getPassword())) {
            return false;
        }

        // Check if user already exists
        if (databaseService.emailExists(signupRequest.getEmail())) {
            return false;
        }

        // Create new user with salted password
        User user = new User(
            signupRequest.getName(),
            signupRequest.getContactNumber(),
            signupRequest.getEmail(),
            PasswordUtil.hashPassword(signupRequest.getPassword())
        );

        return databaseService.saveUser(user);
    }

    /**
     * Login user
     * @param loginRequest login request data
     * @return authentication response
     */
    public AuthResponse login(LoginRequest loginRequest) {
        // Find user by email
        var userOptional = databaseService.findUserByEmail(loginRequest.getEmail());
        
        if (userOptional.isEmpty()) {
            return new AuthResponse(false, "Invalid email or password", null, null, null);
        }

        User user = userOptional.get();

        // Verify password
        if (!PasswordUtil.verifyPassword(loginRequest.getPassword(), user.getPassword())) {
            return new AuthResponse(false, "Invalid email or password", null, null, null);
        }

        // Generate JWT token and refresh token
        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        
        userTokenStore.put(user.getEmail(), accessToken);
        userRefreshTokenStore.put(user.getEmail(), refreshToken);
        
        return new AuthResponse(true, "Login successful", accessToken, refreshToken, new UserInfo(user.getEmail(), user.getName()));
    }

    /**
     * Refresh access token using refresh token
     * @param refreshToken refresh token
     * @return authentication response with new access token and new refresh token
     */
    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return new AuthResponse(false, "Refresh token is required", null, null, null);
        }

        // Validate refresh token
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            return new AuthResponse(false, "Invalid or expired refresh token", null, null, null);
        }

        // Extract email from refresh token
        String email = jwtUtil.extractEmailFromRefreshToken(refreshToken);
        if (email == null) {
            return new AuthResponse(false, "Invalid refresh token", null, null, null);
        }

        // Check if refresh token matches stored one
        String storedRefreshToken = userRefreshTokenStore.get(email);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            return new AuthResponse(false, "Invalid refresh token", null, null, null);
        }

        // Generate new access token AND new refresh token (rotation)
        String newAccessToken = jwtUtil.generateToken(email);
        String newRefreshToken = jwtUtil.generateRefreshToken(email);
        
        // Update both tokens in storage
        userTokenStore.put(email, newAccessToken);
        userRefreshTokenStore.put(email, newRefreshToken);

        return new AuthResponse(true, "Token refreshed successfully", newAccessToken, newRefreshToken, null);
    }

    /**
     * Logout user
     * @param token JWT token
     * @return authentication response
     */
    public AuthResponse logout(String token) {
        String email = getUserEmailFromToken(token);
        if (email != null) {
            userTokenStore.remove(email); // Invalidate access token
            userRefreshTokenStore.remove(email); // Invalidate refresh token
        }
        return new AuthResponse(true, "Logout successful", null, null);
    }

    /**
     * Forgot password - send reset token to user's email
     * @param forgotPasswordRequest forgot password request
     * @return password reset response
     */
//    public PasswordResetResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
//        String email = forgotPasswordRequest.getEmail();
//
//        // Check if user exists
//        var userOptional = databaseService.findUserByEmail(email);
//        if (userOptional.isEmpty()) {
//            // Don't reveal if email exists or not for security
//            return new PasswordResetResponse(true, "If the email exists, a reset link has been sent");
//        }
//
//        User user = userOptional.get();
//
//        // Generate reset token
//        String resetToken = UUID.randomUUID().toString();
//        LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(1); // Token expires in 1 hour
//
//        // Update user with reset token
//        user.setResetToken(resetToken);
//        user.setResetTokenExpiry(tokenExpiry);
//
//        if (databaseService.updateUser(user)) {
//            // In a real application, send email here
//            // For now, we'll log the reset link
//            String resetLink = "http://localhost:8080/api/auth/reset-password?token=" + resetToken;
//            System.out.println("Password reset link for " + email + ": " + resetLink);
//
//            return new PasswordResetResponse(true, "If the email exists, a reset link has been sent");
//        } else {
//            return new PasswordResetResponse(false, "Failed to process password reset request");
//        }
//    }

    /**
     * Reset password using reset token
     * @param resetPasswordRequest reset password request
     * @return password reset response
     */
//    public PasswordResetResponse resetPassword(ResetPasswordRequest resetPasswordRequest) {
//        String token = resetPasswordRequest.getToken();
//        String newPassword = resetPasswordRequest.getNewPassword();
//
//        // Validate password strength
//        if (!PasswordUtil.isValidPassword(newPassword)) {
//            return new PasswordResetResponse(false, "Password must be at least 8 characters with uppercase, lowercase, and digit");
//        }
//
//        // Find user by reset token
//        var userOptional = databaseService.findUserByResetToken(token);
//        if (userOptional.isEmpty()) {
//            return new PasswordResetResponse(false, "Invalid or expired reset token");
//        }
//
//        User user = userOptional.get();
//
//        // Check if token is expired
//        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
//            return new PasswordResetResponse(false, "Reset token has expired");
//        }
//
//        // Update password and clear reset token
//        user.setPassword(PasswordUtil.hashPassword(newPassword));
//        user.setResetToken(null);
//        user.setResetTokenExpiry(null);
//        user.setUpdatedAt(LocalDateTime.now());
//
//        if (databaseService.updateUser(user)) {
//            // Invalidate any existing sessions for this user
//            userTokenStore.remove(user.getEmail());
//            userRefreshTokenStore.remove(user.getEmail());
//
//            return new PasswordResetResponse(true, "Password reset successfully");
//        } else {
//            return new PasswordResetResponse(false, "Failed to reset password");
//        }
//    }

    /**
     * Validate JWT token
     * @param token JWT token
     * @return true if token is valid and is the latest for the user
     */
    public boolean validateToken(String token) {
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return false;
        }
        String email = jwtUtil.extractEmail(token);
        return token.equals(userTokenStore.get(email)); // Only latest token is valid
    }

    /**
     * Get user email from token
     * @param token JWT token
     * @return user email or null if invalid
     */
    public String getUserEmailFromToken(String token) {
        if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
            return jwtUtil.extractEmail(token);
        }
        return null;
    }
} 