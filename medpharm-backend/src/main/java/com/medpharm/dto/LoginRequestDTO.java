package com.medpharm.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "El username es requerido") String username,
        @NotBlank(message = "El password es requerido") String password
) {}