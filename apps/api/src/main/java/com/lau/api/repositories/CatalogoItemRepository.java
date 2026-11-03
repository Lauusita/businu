package com.lau.api.repositories;

import com.lau.api.domain.CatalogoItem;
import com.lau.api.domain.enums.TipoCatalogo;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogoItemRepository extends JpaRepository<CatalogoItem, UUID> {

    List<CatalogoItem> findByTipoAndActivoTrueOrderByCategoriaAscOrdenAsc(TipoCatalogo tipo);
}
