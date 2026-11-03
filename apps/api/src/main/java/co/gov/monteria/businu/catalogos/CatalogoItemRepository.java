package co.gov.monteria.businu.catalogos;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogoItemRepository extends JpaRepository<CatalogoItem, UUID> {

    List<CatalogoItem> findByTipoAndActivoTrueOrderByCategoriaAscOrdenAsc(TipoCatalogo tipo);
}
