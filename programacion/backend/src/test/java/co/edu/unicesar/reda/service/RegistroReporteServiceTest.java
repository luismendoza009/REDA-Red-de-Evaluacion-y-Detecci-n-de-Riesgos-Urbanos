package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.dto.ReporteDtos.CrearIncidenteRequest;
import co.edu.unicesar.reda.dto.ReporteDtos.FallaRequest;
import co.edu.unicesar.reda.model.*;
import co.edu.unicesar.reda.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Prueba del caso de uso SIN base de datos. Es posible porque ReporteService depende de
 * abstracciones (DIP): los repositorios y el BuscadorAntecedentes se reemplazan por dobles de prueba.
 */
@ExtendWith(MockitoExtension.class)
class RegistroReporteServiceTest {

    @Mock ReporteRepository reportes;
    @Mock UsuarioRepository usuarios;
    @Mock CategoriaRepository categorias;
    @Mock ZonaRepository zonas;
    @Mock EstadoRepository estados;
    @Mock EntidadPublicaRepository entidades;
    @Mock ReporteRelacionadoRepository relaciones;
    @Mock BuscadorAntecedentes buscador;

    private RegistroReporteService servicio;

    @BeforeEach
    void preparar() {
        servicio = new RegistroReporteService(reportes, usuarios, categorias, zonas, estados, entidades, relaciones,
                buscador);
    }

    private void datosBasicos() {
        Estado pendiente = mock(Estado.class);
        when(pendiente.tipo()).thenReturn(NombreEstado.PENDIENTE);
        when(pendiente.getNombre()).thenReturn("Pendiente");
        when(estados.findByNombre("Pendiente")).thenReturn(Optional.of(pendiente));
        when(usuarios.findById(7)).thenReturn(Optional.of(mock(Usuario.class)));
        Categoria alcantarilla = mock(Categoria.class);
        when(alcantarilla.getId()).thenReturn(2);
        when(categorias.findById(2)).thenReturn(Optional.of(alcantarilla));
        when(zonas.findById(1)).thenReturn(Optional.of(mock(Zona.class)));
        when(reportes.save(any(Reporte.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void crearIncidenteVinculaLosAntecedentesQueEncuentraElBuscador() {
        datosBasicos();
        OffsetDateTime hecho = OffsetDateTime.now().minusHours(2);
        OffsetDateTime primerReporte = hecho.minusDays(150);
        when(buscador.buscar(eq(2), any(), any(), eq(hecho))).thenReturn(List.of(
                new BuscadorAntecedentes.Antecedente(11, primerReporte, "Validado", "Alcantarilla sin tapa", 12.3),
                new BuscadorAntecedentes.Antecedente(15, hecho.minusDays(40), "En revisión", "Sigue sin tapa", 30.0)));

        var falla = new FallaRequest(2, 1, "Peatón cayó en la alcantarilla", new BigDecimal("10.477100"),
                new BigDecimal("-73.244800"), (short) 5);
        var respuesta = servicio.crearIncidente(
                new CrearIncidenteRequest(falla, TipoDano.LESION, "Fractura de tibia", hecho, true, null, null), 7);

        assertEquals(2, respuesta.antecedentes().size());
        assertEquals(150L, respuesta.diasConocimientoPrevio(), "los días se cuentan desde el PRIMER antecedente");
        assertEquals("Pendiente", respuesta.reporte().estado(), "todo incidente nuevo queda Pendiente de revisión");
        assertEquals("LESION", respuesta.reporte().incidente().tipoDano());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReporteRelacionado>> vinculos = ArgumentCaptor.forClass(List.class);
        verify(relaciones).saveAll(vinculos.capture());
        assertEquals(2, vinculos.getValue().size());
        assertTrue(vinculos.getValue().stream().allMatch(v ->
                v.getTipo() == ReporteRelacionado.Tipo.ANTECEDENTE && v.getOrigen() == ReporteRelacionado.Origen.AUTOMATICO));
        assertEquals(List.of(11, 15), vinculos.getValue().stream().map(v -> v.getClave().getIdReporteRelacionado()).toList());
    }

    @Test
    void sinAntecedentesNoHayDiasDeConocimientoPrevio() {
        datosBasicos();
        when(buscador.buscar(anyInt(), any(), any(), any())).thenReturn(List.of());
        var falla = new FallaRequest(2, 1, "Peatón cayó en la alcantarilla", BigDecimal.ONE, BigDecimal.ONE, null);

        var respuesta = servicio.crearIncidente(new CrearIncidenteRequest(falla, TipoDano.LESION, "Fractura de tibia",
                OffsetDateTime.now().minusHours(1), false, null, null), 7);

        assertTrue(respuesta.antecedentes().isEmpty());
        assertNull(respuesta.diasConocimientoPrevio());
    }
}
