package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.model.Evidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenciaRepository extends JpaRepository<Evidencia, Integer> {

    List<Evidencia> findByReporteIdOrderByFechaCargaAsc(Integer idReporte);

    boolean existsByHashSha256(String hashSha256);
}
