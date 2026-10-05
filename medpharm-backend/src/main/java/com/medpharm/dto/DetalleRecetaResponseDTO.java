package com.medpharm.dto;

public record DetalleRecetaResponseDTO(Long id, Long medicamentoId, String medicamentoNombre,
                                       Integer cantidad, String dosisIndicada) {}