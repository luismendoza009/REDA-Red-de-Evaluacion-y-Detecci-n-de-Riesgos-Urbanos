package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.model.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Integer> {

    Optional<TokenRecuperacion> findByHashToken(String hashToken);

    /** Al pedir un enlace nuevo, los anteriores sin usar dejan de servir. */
    @Modifying
    @Query("DELETE FROM TokenRecuperacion t WHERE t.usuario.id = :idUsuario AND t.fechaUso IS NULL")
    void borrarPendientesDe(Integer idUsuario);
}
