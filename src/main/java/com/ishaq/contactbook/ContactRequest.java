package com.ishaq.contactbook;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContactRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters") String name,
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "[+0-9() .-]{7,25}", message = "Use 7–25 phone characters: digits, +, spaces, (), . or -")
    @Pattern(regexp = ".*[0-9].*", message = "Phone must include a digit") String phone,
    @Email(message = "Enter a valid email address")
    @Size(max = 150, message = "Email must be at most 150 characters") String email,
    @Size(max = 300, message = "Address must be at most 300 characters") String address
) {
    public ContactRequest {
        name = clean(name);
        phone = clean(phone);
        email = clean(email);
        address = clean(address);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
