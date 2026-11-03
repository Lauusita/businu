package com.lau.api.catalogos;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmbarcaderoRepository extends JpaRepository<Embarcadero, UUID> {

    List<Embarcadero> findByActivoTrueOrderByOrdenRuta();
}
