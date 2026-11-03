package co.gov.monteria.businu.mantenimiento;

import static org.assertj.core.api.Assertions.assertThat;

import co.gov.monteria.businu.compartido.CalendarioOperativo;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CalculadoraProximoServicioTest {

    private static final BigDecimal SEIS_HORAS = new BigDecimal("6");
    private final CalculadoraProximoServicio calc = new CalculadoraProximoServicio(new CalendarioOperativo());

    @Test
    void servicioRecienHechoProyectaElSabadoAnteriorALas100Horas() {
        // Servicio el sábado 7 nov 2026 a 1200 h → próximo a 1300 h. Hoy es el mismo sábado.
        LocalDate hoy = LocalDate.of(2026, 11, 7);
        ProyeccionServicio p = calc.proyectar(new BigDecimal("1300"), new BigDecimal("1200"), SEIS_HORAS, hoy, hoy);

        // 100 h / 6 h = 17 días operativos; con el festivo del lun 16 nov se alcanza el mié 2 dic.
        assertThat(p.diasOperativosRestantes()).isEqualTo(17);
        assertThat(p.fechaAlcanza100h()).isEqualTo(LocalDate.of(2026, 12, 2));
        assertThat(p.fechaSugerida()).isEqualTo(LocalDate.of(2026, 11, 28));
        assertThat(p.fechaLimite()).isEqualTo(LocalDate.of(2026, 11, 28));
        assertThat(p.estado()).isEqualTo(EstadoProyeccion.AL_DIA);
    }

    @Test
    void faltandoMenosDe30HorasQuedaEnProximo() {
        LocalDate hoy = LocalDate.of(2026, 11, 23);
        ProyeccionServicio p = calc.proyectar(new BigDecimal("1300"), new BigDecimal("1276"), SEIS_HORAS,
                LocalDate.of(2026, 11, 7), hoy);
        assertThat(p.horasRestantes()).isEqualByComparingTo("24");
        assertThat(p.estado()).isEqualTo(EstadoProyeccion.PROXIMO);
        assertThat(p.fechaSugerida()).isEqualTo(LocalDate.of(2026, 11, 28));
    }

    @Test
    void superadoElHorometroQuedaVencido() {
        LocalDate hoy = LocalDate.of(2026, 12, 3);
        ProyeccionServicio p = calc.proyectar(new BigDecimal("1300"), new BigDecimal("1304.5"), SEIS_HORAS,
                LocalDate.of(2026, 11, 7), hoy);
        assertThat(p.estado()).isEqualTo(EstadoProyeccion.VENCIDO);
    }

    @Test
    void doceMesesSinServicioQuedaVencidoAunqueFaltenHoras() {
        LocalDate hoy = LocalDate.of(2027, 11, 8);
        ProyeccionServicio p = calc.proyectar(new BigDecimal("1300"), new BigDecimal("1210"), SEIS_HORAS,
                LocalDate.of(2026, 11, 7), hoy);
        assertThat(p.estado()).isEqualTo(EstadoProyeccion.VENCIDO);
    }
}
