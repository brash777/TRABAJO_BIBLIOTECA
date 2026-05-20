package com.biblioteca.service.impl;

import com.biblioteca.dto.PrestamoRequest;
import com.biblioteca.dto.PrestamoResponse;
import com.biblioteca.model.Prestamo;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.UsuarioRepository;
import com.biblioteca.service.PrestamoService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrestamoServiceImpl implements PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;

    public PrestamoServiceImpl(PrestamoRepository prestamoRepository,
                               UsuarioRepository usuarioRepository,
                               LibroRepository libroRepository) {
        this.prestamoRepository = prestamoRepository;
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
    }

    @Override
    public PrestamoResponse crearPrestamo(PrestamoRequest request) {
        // Verificar que el usuario existe
        if (!usuarioRepository.existsById(request.getUsuarioId())) {
            throw new RuntimeException("Usuario no encontrado con id: " + request.getUsuarioId());
        }

        // Verificar que el libro existe
        if (!libroRepository.existsById(request.getLibroId())) {
            throw new RuntimeException("Libro no encontrado con id: " + request.getLibroId());
        }

        // Verificar que el libro no tenga un préstamo activo
        List<Prestamo> prestamosActivos = prestamoRepository
                .findByLibroIdAndEstado(request.getLibroId(), "ACTIVO");
        if (!prestamosActivos.isEmpty()) {
            throw new RuntimeException("El libro ya tiene un préstamo activo y no está disponible.");
        }

        // Crear el préstamo
        Prestamo prestamo = new Prestamo();
        prestamo.setUsuarioId(request.getUsuarioId());
        prestamo.setLibroId(request.getLibroId());
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaDevolucionEsperada(request.getFechaDevolucionEsperada());
        prestamo.setFechaDevolucionReal(null);
        prestamo.setEstado("ACTIVO");

        Prestamo guardado = prestamoRepository.save(prestamo);
        return mapToResponse(guardado);
    }

    @Override
    public PrestamoResponse registrarDevolucion(String id) {
        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado con id: " + id));

        if ("DEVUELTO".equals(prestamo.getEstado())) {
            throw new RuntimeException("Este préstamo ya fue devuelto.");
        }

        prestamo.setFechaDevolucionReal(LocalDate.now());
        prestamo.setEstado("DEVUELTO");

        Prestamo actualizado = prestamoRepository.save(prestamo);
        return mapToResponse(actualizado);
    }

    @Override
    public PrestamoResponse consultarPrestamo(String id) {
        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado con id: " + id));
        return mapToResponse(prestamo);
    }

    @Override
    public List<PrestamoResponse> listarPrestamos() {
        return prestamoRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PrestamoResponse> listarPrestamosDeUsuario(String usuarioId) {
        return prestamoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PrestamoResponse mapToResponse(Prestamo prestamo) {
        return new PrestamoResponse(
                prestamo.getId(),
                prestamo.getUsuarioId(),
                prestamo.getLibroId(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaDevolucionEsperada(),
                prestamo.getFechaDevolucionReal(),
                prestamo.getEstado()
        );
    }
}
