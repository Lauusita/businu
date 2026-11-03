package com.lau.api.repositories;

import com.lau.api.domain.Embarcadero;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmbarcaderoRepository extends JpaRepository<Embarcadero, UUID> {

    List<Embarcadero> findByActivoTrueOrderByOrdenRuta();
}
