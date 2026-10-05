package com.medpharm.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RecetaResponseDTO(Long id, String codigoReceta, String pacienteNombre,
                                String medicoNombre, String estado, LocalDateTime fechaEmision,
                                List<DetalleRecetaResponseDTO> detalles) {}