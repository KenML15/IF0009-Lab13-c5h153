package com.medpharm.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.medpharm.dto.CambioEstadoDTO;
import com.medpharm.dto.RecetaRequestDTO;
import com.medpharm.dto.RecetaResponseDTO;
import com.medpharm.service.RecetaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/recetas")
@RequiredArgsConstructor
public class RecetaController {

    private final RecetaService recetaService;

    @GetMapping
    public List<RecetaResponseDTO> listar() {
        return recetaService.listar();
    }

    @GetMapping("/estado/{estado}")
    public List<RecetaResponseDTO> listarPorEstado(@PathVariable String estado) {
        return recetaService.listarPorEstado(estado);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecetaResponseDTO crear(@Valid @RequestBody RecetaRequestDTO request,
                                   Authentication authentication) {
        return recetaService.crear(request, authentication.getName());
    }

    @PatchMapping("/{id}/estado")
    public RecetaResponseDTO cambiarEstado(@PathVariable Long id,
                                           @Valid @RequestBody CambioEstadoDTO request) {
        return recetaService.cambiarEstado(id, request.estado());
    }
}