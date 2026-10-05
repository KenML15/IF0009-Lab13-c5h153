package com.medpharm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medpharm.model.Medicamento;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
}