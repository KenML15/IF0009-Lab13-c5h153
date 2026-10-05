package com.medpharm.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record RecetaRequestDTO(
        @NotBlank(message = "El nombre del paciente es requerido")
        @Size(min = 5, max = 120, message = "El nombre del paciente debe tener entre 5 y 120 caracteres")
        String pacienteNombre,

        @NotEmpty(message = "La receta debe tener al menos un medicamento")
        List<@Valid DetalleRecetaRequestDTO> detalles
) {}