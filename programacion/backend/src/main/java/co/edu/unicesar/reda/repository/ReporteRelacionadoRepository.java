package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.model.ReporteRelacionado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReporteRelacionadoRepository extends JpaRepository<ReporteRelacionado, ReporteRelacionado.Clave> {
}
