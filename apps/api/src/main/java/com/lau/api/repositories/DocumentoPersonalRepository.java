package com.lau.api.repositories;

import com.lau.api.domain.DocumentoPersonal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoPersonalRepository extends JpaRepository<DocumentoPersonal, UUID> {

    List<DocumentoPersonal> findByPersonalId(UUID personalId);
}
