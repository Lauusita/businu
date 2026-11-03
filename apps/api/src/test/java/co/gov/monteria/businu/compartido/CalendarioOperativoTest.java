package co.gov.monteria.businu.compartido;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CalendarioOperativoTest {

    private final CalendarioOperativo calendario = new CalendarioOperativo();

    @Test
    void festivosDeColombia2026() {
        assertThat(calendario.festivos(2026)).containsExactlyInAnyOrder(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 12), LocalDate.of(2026, 3, 23),
                LocalDate.of(2026, 4, 2), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 18), LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 29), LocalDate.of(2026, 7, 20), LocalDate.of(2026, 8, 7),
                LocalDate.of(2026, 8, 17), LocalDate.of(2026, 10, 12), LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 16), LocalDate.of(2026, 12, 8), LocalDate.of(2026, 12, 25));
    }

    @Test
    void pascua() {
        assertThat(CalendarioOperativo.domingoDePascua(2026)).isEqualTo(LocalDate.of(2026, 4, 5));
        assertThat(CalendarioOperativo.domingoDePascua(2027)).isEqualTo(LocalDate.of(2027, 3, 28));
    }

    @Test
    void sabadoDomingoYFestivoNoSonOperativos() {
        assertThat(calendario.esDiaOperativo(LocalDate.of(2026, 11, 7))).isFalse();  // sábado
        assertThat(calendario.esDiaOperativo(LocalDate.of(2026, 11, 8))).isFalse();  // domingo
        assertThat(calendario.esDiaOperativo(LocalDate.of(2026, 11, 16))).isFalse(); // Indep. Cartagena
        assertThat(calendario.esDiaOperativo(LocalDate.of(2026, 11, 17))).isTrue();
    }

    @Test
    void sumaDiasOperativosSaltandoFinesDeSemanaYFestivos() {
        // Desde el viernes 13 nov: lun 16 es festivo → 1.er día operativo es mar 17
        assertThat(calendario.sumarDiasOperativos(LocalDate.of(2026, 11, 13), 1))
                .isEqualTo(LocalDate.of(2026, 11, 17));
    }
}
