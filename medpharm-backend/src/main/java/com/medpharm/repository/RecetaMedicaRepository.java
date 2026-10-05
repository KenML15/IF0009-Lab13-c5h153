package com.medpharm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.medpharm.model.RecetaMedica;

public interface RecetaMedicaRepository extends JpaRepository<RecetaMedica, Long> {

    @Override
    @EntityGraph(attributePaths = {"medico", "detalles", "detalles.medicamento"})
    List<RecetaMedica> findAll();

    @EntityGraph(attributePaths = {"medico", "detalles", "detalles.medicamento"})
    List<RecetaMedica> findByEstado(String estado);

    long count();
}