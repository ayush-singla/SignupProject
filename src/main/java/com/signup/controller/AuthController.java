package com.signup.controller;

import com.signup.dto.AuthResponse;
import com.signup.dto.LoginRequest;
import com.signup.dto.SignupRequest;
import com.signup.dto.ProfileResponse;
import com.signup.dto.LogoutResponse;
import com.signup.dto.RefreshTokenRequest;
import com.signup.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag; 
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.signup.dto.SignupResponse;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication APIs")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private com.signup.database.DatabaseService databaseService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
    boolean registered = authService.signup(signupRequest);
    if (registered) {
        return ResponseEntity.ok(new SignupResponse("User has been registered successfully"));
    } else {
        return ResponseEntity.badRequest().body(new SignupResponse("Failed to register user"));
    }
}

    @PostMapping("/login")
    @Operation(summary = "User login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

//    @PostMapping("/forgot-password")
//    @Operation(
//        summary = "Forgot password",
//        description = "Send a password reset link to the user's email address"
//    )
//    public ResponseEntity<PasswordResetResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
//        PasswordResetResponse response = authService.forgotPassword(forgotPasswordRequest);
//        if (response.isSuccess()) {
//            return ResponseEntity.ok(response);
//        } else {
//            return ResponseEntity.badRequest().body(response);
//        }
//    }

//    @PostMapping("/reset-password")
//    @Operation(
//        summary = "Reset password",
//        description = "Reset password using the token received via email"
//    )
//    public ResponseEntity<PasswordResetResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
//        PasswordResetResponse response = authService.resetPassword(resetPasswordRequest);
//        if (response.isSuccess()) {
//            return ResponseEntity.ok(response);
//        } else {
//            return ResponseEntity.badRequest().body(response);
//        }
//    }

    @PostMapping("/logout")
    @Operation(
        summary = "User logout",
        description = "Logout user. The JWT token will be invalidated.",
        parameters = {
            @Parameter(
                name = "token",
                description = "JWT token (with or without 'Bearer ' prefix). If not provided, will use Authorization header.",
                in = ParameterIn.QUERY,
                required = false
            )
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<LogoutResponse> logout(
        @RequestParam(value = "token", required = false) String queryToken,
        HttpServletRequest request) {
        String token = queryToken;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isEmpty()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        
        // Check if token is provided
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401)
                .body(new LogoutResponse(false, "You are not an authenticated user. Token is required for logout."));
        }
        
        // Check if token is valid
        if (!authService.validateToken(token)) {
            return ResponseEntity.status(401)
                .body(new LogoutResponse(false, "You are not an authenticated user. Invalid or expired token."));
        }
        
        // Perform logout
        authService.logout(token);
        return ResponseEntity.ok(new LogoutResponse(true, "User has been logged out successfully"));
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        description = "Get a new access token using a valid refresh token"
    )
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        AuthResponse response = authService.refreshToken(refreshTokenRequest.getRefreshToken());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    @GetMapping("/profile")
    @Operation(
        summary = "Get user profile",
        description = "Returns the authenticated user's profile details. Requires a valid JWT token.",
        parameters = {
            @Parameter(
                name = "token",
                description = "JWT token (with or without 'Bearer ' prefix). If not provided, will use Authorization header.",
                in = ParameterIn.QUERY,
                required = false
            )
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ProfileResponse> getProfile(
        @RequestParam(value = "token", required = false) String queryToken,
        HttpServletRequest request) {
        String token = queryToken;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isEmpty()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        
        // Check if token is provided
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401)
                .body(new ProfileResponse(false, "You are not an authenticated user. Token is required.", null));
        }
        
        // Check if token is valid
        if (!authService.validateToken(token)) {
            return ResponseEntity.status(401)
                .body(new ProfileResponse(false, "You are not an authenticated user. Invalid or expired token.", null));
        }
        
        // Extract user email from token
        String userEmail = authService.getUserEmailFromToken(token);
        if (userEmail == null) {
            return ResponseEntity.status(401)
                .body(new ProfileResponse(false, "You are not an authenticated user. Unable to extract user information from token.", null));
        }
        
        // Get user profile from database
        return databaseService.findUserByEmail(userEmail)
            .map(user -> {
                ProfileResponse.UserProfile profile = new ProfileResponse.UserProfile(
                    user.getId(),
                    user.getName(),
                    user.getContactNumber(),
                    user.getEmail()
                );
                return ResponseEntity.ok(new ProfileResponse(true, "Profile fetched successfully", profile));
            })
            .orElseGet(() -> ResponseEntity.status(404)
                .body(new ProfileResponse(false, "User profile not found in database", null)));
    }

    // @GetMapping("/test")
    // @Operation(summary = "Test protected endpoint", description = "Test JWT authentication. Use the 'Authorize' button above to add your JWT token. For testing without auth, use /test-public endpoint.")
    // @SecurityRequirement(name = "Bearer Authentication")
    // public ResponseEntity<String> test(
    //         @Parameter(description = "JWT token (with or without 'Bearer ' prefix)", in = ParameterIn.QUERY, required = false)
    //         @RequestParam(value = "token", required = false) String queryToken,
    //         HttpServletRequest request) {
        
    //     String token = queryToken;
        
    //     // If query parameter has "Bearer " prefix, remove it
    //     if (token != null && token.startsWith("Bearer ")) {
    //         token = token.substring(7);
    //     }
        
    //     // If no query parameter, try to get from Authorization header
    //     if (token == null || token.isEmpty()) {
    //         String authHeader = request.getHeader("Authorization");
    //         if (authHeader != null && authHeader.startsWith("Bearer ")) {
    //             token = authHeader.substring(7);
    //         }
    //     }
        
    //     if (token == null || token.isEmpty()) {
    //         return ResponseEntity.status(401).body("Token is required");
    //     }
        
    //     System.out.println("🔍 Validating token: " + token.substring(0, Math.min(20, token.length())) + "...");
        
    //     if (authService.validateToken(token)) {
    //         String userEmail = authService.getUserEmailFromToken(token);
    //         System.out.println("✅ Token valid for user: " + userEmail);
    //         return ResponseEntity.ok("Authentication successful! User: " + userEmail);
    //     } else {
    //         System.out.println("❌ Token validation failed");
    //         return ResponseEntity.status(401).body("Invalid or missing token");
    //     }
    // }

    // @GetMapping("/users")
    // @Operation(
    //     summary = "Get all users",
    //     description = "View all registered users in the database. Requires a valid JWT token.",
    //     parameters = {
    //         @Parameter(
    //             name = "token",
    //             description = "JWT token (with or without 'Bearer ' prefix). If not provided, will use Authorization header.",
    //             in = ParameterIn.QUERY,
    //             required = false
    //         )
    //     }
    // )
    // @SecurityRequirement(name = "Bearer Authentication")
    // public ResponseEntity<?> getAllUsers(
    //     @RequestParam(value = "token", required = false) String queryToken,
    //     HttpServletRequest request) {
    //     String token = queryToken;
    //     if (token != null && token.startsWith("Bearer ")) {
    //         token = token.substring(7);
    //     }
    //     if (token == null || token.isEmpty()) {
    //         String authHeader = request.getHeader("Authorization");
    //         if (authHeader != null && authHeader.startsWith("Bearer ")) {
    //             token = authHeader.substring(7);
    //         }
    //     }
    //     if (token == null || token.isEmpty() || !authService.validateToken(token)) {
    //         return ResponseEntity.status(401).body("You are not an authenticated user");
    //     }
    //     List<User> users = databaseService.getAllUsers();
    //     return ResponseEntity.ok(users);
    // }

    // @GetMapping("/test-public")
    // @Operation(summary = "Test public endpoint", description = "Public endpoint for testing - no authentication required")
    // public ResponseEntity<String> testPublic() {
    //     return ResponseEntity.ok("Public endpoint working! No authentication required.");
    // }
} 