package com.lau.api.repositorio;

import com.lau.api.dominio.Embarcacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public abstract class EmbarcacionRepositorio implements JpaRepository<Embarcacion, UUID>, JpaSpecificationExecutor<Embarcacion> {

}
