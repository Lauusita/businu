package com.lau.api.repositorio;

import com.lau.api.dominio.InformeMensual;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InformeMensualRepositorio extends JpaRepository<InformeMensual, UUID> {
    Optional<InformeMensual> findByPeriodo(LocalDate periodo);
}
