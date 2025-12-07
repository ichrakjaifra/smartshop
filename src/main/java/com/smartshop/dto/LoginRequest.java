package com.smartshop.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "Username est obligatoire")
    private String username;

    @NotBlank(message = "Password est obligatoire")
    private String password;
}
