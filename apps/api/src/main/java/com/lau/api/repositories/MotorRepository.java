package com.lau.api.repositories;

import com.lau.api.domain.Motor;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MotorRepository extends JpaRepository<Motor, UUID> {

    List<Motor> findByEmbarcacionId(UUID embarcacionId);
}
