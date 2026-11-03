package com.lau.api.repositories;

import com.lau.api.domain.Personal;
import com.lau.api.domain.enums.Cargo;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalRepository extends JpaRepository<Personal, UUID> {

    List<Personal> findByCargoAndActivoTrue(Cargo cargo);
}
