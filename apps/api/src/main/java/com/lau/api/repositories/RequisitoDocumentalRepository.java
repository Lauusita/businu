package com.lau.api.repositories;

import com.lau.api.domain.RequisitoDocumental;
import com.lau.api.domain.enums.Cargo;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequisitoDocumentalRepository extends JpaRepository<RequisitoDocumental, UUID> {

    List<RequisitoDocumental> findByCargo(Cargo cargo);
}
