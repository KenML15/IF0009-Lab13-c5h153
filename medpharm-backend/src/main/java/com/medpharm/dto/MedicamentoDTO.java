package com.medpharm.dto;

import java.math.BigDecimal;

import com.medpharm.model.Medicamento;

public record MedicamentoDTO(Long id, String codigo, String nombre,
                             Integer stock, BigDecimal precioUnitario) {

    public static MedicamentoDTO from(Medicamento m) {
        return new MedicamentoDTO(m.getId(), m.getCodigo(), m.getNombre(),
                                  m.getStock(), m.getPrecioUnitario());
    }
}