package com.medpharm.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medpharm.dto.MedicamentoDTO;
import com.medpharm.repository.MedicamentoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;

    @Transactional(readOnly = true)
    public List<MedicamentoDTO> listar() {
        return medicamentoRepository.findAll(Sort.by("nombre")).stream()
                .map(MedicamentoDTO::from)
                .toList();
    }
}