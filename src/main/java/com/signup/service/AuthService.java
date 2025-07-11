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

    // Store the latest token for each user (in-memory)
    private final Map<String, String> userTokenStore = new ConcurrentHashMap<>();

    /**
     * Register a new user
     * @param signupRequest signup request data
     * @return authentication response
     */
    public AuthResponse signup(SignupRequest signupRequest) {
        // Validate password strength
        if (!PasswordUtil.isValidPassword(signupRequest.getPassword())) {
            return new AuthResponse(false, "Password must be at least 8 characters with uppercase, lowercase, and digit", null, null);
        }

        // Check if user already exists
        if (databaseService.emailExists(signupRequest.getEmail())) {
            return new AuthResponse(false, "Email already registered", null, null);
        }

        // Create new user with salted password
        User user = new User(
            signupRequest.getName(),
            signupRequest.getContactNumber(),
            signupRequest.getEmail(),
            PasswordUtil.hashPassword(signupRequest.getPassword())
        );

        if (databaseService.saveUser(user)) {
            String token = jwtUtil.generateToken(user.getEmail());
            userTokenStore.put(user.getEmail(), token); // Store latest token
            return new AuthResponse(true, "User registered successfully", token, new UserInfo(user.getEmail(), user.getName()));
        } else {
            return new AuthResponse(false, "Failed to register user", null, null);
        }
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
            return new AuthResponse(false, "Invalid email or password", null, null);
        }

        User user = userOptional.get();

        // Verify password
        if (!PasswordUtil.verifyPassword(loginRequest.getPassword(), user.getPassword())) {
            return new AuthResponse(false, "Invalid email or password", null, null);
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());
        userTokenStore.put(user.getEmail(), token); // Store latest token
        return new AuthResponse(true, "Login successful", token, new UserInfo(user.getEmail(), user.getName()));
    }

    /**
     * Logout user
     * @param token JWT token
     * @return authentication response
     */
    public AuthResponse logout(String token) {
        String email = getUserEmailFromToken(token);
        if (email != null) {
            userTokenStore.remove(email); // Invalidate token
        }
        return new AuthResponse(true, "Logout successful", null, null);
    }

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