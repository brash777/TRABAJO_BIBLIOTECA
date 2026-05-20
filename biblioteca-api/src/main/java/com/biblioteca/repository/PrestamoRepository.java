package com.biblioteca.repository;

import com.biblioteca.model.Prestamo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestamoRepository extends MongoRepository<Prestamo, String> {

    // Buscar todos los préstamos de un usuario
    List<Prestamo> findByUsuarioId(String usuarioId);

    // Buscar préstamos activos de un libro específico
    List<Prestamo> findByLibroIdAndEstado(String libroId, String estado);
}
