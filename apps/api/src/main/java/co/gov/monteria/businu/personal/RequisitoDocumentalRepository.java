package co.gov.monteria.businu.personal;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequisitoDocumentalRepository extends JpaRepository<RequisitoDocumental, UUID> {

    List<RequisitoDocumental> findByCargo(Cargo cargo);
}
