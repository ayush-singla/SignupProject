package com.signup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(
    description = "Signup request payload",
    example = """
    {
      \"name\": \"John Doe\",
      \"contactNumber\": \"9876543210\",
      \"email\": \"john.doe@example.com\",
      \"password\": \"strongPassword123\"
    }
    """
)
public class SignupRequest {

    @Schema(description = "Full name of the user", example = "John Doe")
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @Schema(description = "10-digit contact number", example = "9876543210")
    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be exactly 10 digits")
    private String contactNumber;

    @Schema(description = "User's email address", example = "john.doe@example.com")
    @Email(message = "Please provide a valid email address")
    @Pattern(
            regexp = "^[a-z0-9._%+-]+@[a-z0-9]+(?:\\.[a-z0-9]+)+$",
            message = "Email must be lowercase and valid"
    )
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "Password (min 8 characters)", example = "strongPassword123")
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    public SignupRequest() {}

    // Constructor with fields
    public SignupRequest(String name, String contactNumber, String email, String password) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
} 