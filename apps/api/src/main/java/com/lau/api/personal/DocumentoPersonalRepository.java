package com.lau.api.personal;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoPersonalRepository extends JpaRepository<DocumentoPersonal, UUID> {

    List<DocumentoPersonal> findByPersonalId(UUID personalId);
}
