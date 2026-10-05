package com.medpharm.service;

import java.time.Year;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medpharm.dto.DetalleRecetaRequestDTO;
import com.medpharm.dto.DetalleRecetaResponseDTO;
import com.medpharm.dto.RecetaRequestDTO;
import com.medpharm.dto.RecetaResponseDTO;
import com.medpharm.exception.RecursoNoEncontradoException;
import com.medpharm.exception.ReglaNegocioException;
import com.medpharm.model.DetalleReceta;
import com.medpharm.model.Medicamento;
import com.medpharm.model.RecetaMedica;
import com.medpharm.model.Usuario;
import com.medpharm.repository.MedicamentoRepository;
import com.medpharm.repository.RecetaMedicaRepository;
import com.medpharm.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecetaService {

    private static final Set<String> ESTADOS = Set.of("PENDIENTE", "DESPACHADA", "CANCELADA");

    private final RecetaMedicaRepository recetaRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<RecetaResponseDTO> listar() {
        return recetaRepository.findAll().stream()
                .sorted(Comparator.comparing(RecetaMedica::getId).reversed())
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecetaResponseDTO> listarPorEstado(String estado) {
        String estadoNormalizado = estado.toUpperCase();
        if (!ESTADOS.contains(estadoNormalizado)) {
            throw new IllegalArgumentException("Estado inválido: " + estado
                    + ". Valores permitidos: " + ESTADOS);
        }
        return recetaRepository.findByEstado(estadoNormalizado).stream()
                .sorted(Comparator.comparing(RecetaMedica::getId).reversed())
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public RecetaResponseDTO crear(RecetaRequestDTO request, String usernameMedico) {
        Usuario medico = usuarioRepository.findByUsername(usernameMedico)
                .orElseThrow(() -> new RecursoNoEncontradoException("Médico no encontrado: " + usernameMedico));

        Map<Long, Integer> totalPorMedicamento = request.detalles().stream()
                .collect(Collectors.groupingBy(DetalleRecetaRequestDTO::medicamentoId,
                        Collectors.summingInt(DetalleRecetaRequestDTO::cantidad)));

        Map<Long, Medicamento> medicamentos = new HashMap<>();
        totalPorMedicamento.forEach((medId, total) -> {
            Medicamento med = buscarMedicamento(medId);
            validarStock(med, total);
            medicamentos.put(medId, med);
        });

        RecetaMedica receta = new RecetaMedica();
        receta.setCodigoReceta(generarCodigo());
        receta.setPacienteNombre(request.pacienteNombre().trim());
        receta.setMedico(medico);
        receta.setEstado("PENDIENTE");

        request.detalles().forEach(d -> {
            DetalleReceta detalle = new DetalleReceta();
            detalle.setMedicamento(medicamentos.get(d.medicamentoId()));
            detalle.setCantidad(d.cantidad());
            detalle.setDosisIndicada(d.dosisIndicada().trim());
            receta.agregarDetalle(detalle);
        });

        return toDTO(recetaRepository.save(receta)); 
    }

    @Transactional
    public RecetaResponseDTO cambiarEstado(Long id, String nuevoEstado) {
        RecetaMedica receta = recetaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la receta con id " + id));

        if (!"PENDIENTE".equals(receta.getEstado())) {
            throw new ReglaNegocioException("La receta " + receta.getCodigoReceta()
                    + " ya está " + receta.getEstado() + " y no puede modificarse");
        }

        if ("DESPACHADA".equals(nuevoEstado)) {
            receta.getDetalles().forEach(d -> validarStock(d.getMedicamento(), d.getCantidad()));
            receta.getDetalles().forEach(d -> {
                Medicamento m = d.getMedicamento();
                m.setStock(m.getStock() - d.getCantidad());
            });
        }

        receta.setEstado(nuevoEstado);
        return toDTO(receta); 
    }


    private Medicamento buscarMedicamento(Long id) {
        return medicamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el medicamento con id " + id));
    }

    private void validarStock(Medicamento med, int cantidadRequerida) {
        if (med.getStock() < cantidadRequerida) {
            throw new ReglaNegocioException(String.format(
                    "Stock insuficiente para %s: disponible %d, requerido %d",
                    med.getNombre(), med.getStock(), cantidadRequerida));
        }
    }

    private String generarCodigo() {
        return String.format("REC-%d-%03d", Year.now().getValue(), recetaRepository.count() + 1);
    }

    private RecetaResponseDTO toDTO(RecetaMedica r) {
        List<DetalleRecetaResponseDTO> detalles = r.getDetalles().stream()
                .map(d -> new DetalleRecetaResponseDTO(
                        d.getId(),
                        d.getMedicamento().getId(),
                        d.getMedicamento().getNombre(),
                        d.getCantidad(),
                        d.getDosisIndicada()))
                .toList();

        return new RecetaResponseDTO(r.getId(), r.getCodigoReceta(), r.getPacienteNombre(),
                r.getMedico().getNombreCompleto(), r.getEstado(), r.getFechaEmision(), detalles);
    }
}