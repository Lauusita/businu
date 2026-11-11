package com.lau.api.repositorio;

import com.lau.api.dominio.Personal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public abstract class PersonalRepositorio implements JpaRepository<Personal, UUID>, JpaSpecificationExecutor<Personal> {
}
