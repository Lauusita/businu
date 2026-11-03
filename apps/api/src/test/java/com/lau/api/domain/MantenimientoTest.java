package com.lau.api.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lau.api.domain.enums.Cargo;
import com.lau.api.domain.enums.PosicionMotor;
import com.lau.api.domain.enums.SistemaIntervenido;
import com.lau.api.domain.enums.TipoIdentificacion;
import com.lau.api.domain.enums.TipoMantenimiento;
import com.lau.api.domain.enums.TipoRecomendacion;
import com.lau.api.domain.enums.TipoVinculacion;
import com.lau.api.exceptions.ReglaNegocioException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class MantenimientoTest {

    private final Embarcacion betanci = new Embarcacion("PAT-001", "Betancí", 40);
    private final Motor babor = betanci.agregarMotor(PosicionMotor.BABOR, "Suzuki", "DF200", "S-1", 200);
    private final Motor estribor = betanci.agregarMotor(PosicionMotor.ESTRIBOR, "Suzuki", "DF200", "S-2", 200);
    private final Personal piloto = persona(Cargo.CONDUCTOR);

    @Test
    void servicioDe100HorasEnAmbosMotores() {
        Mantenimiento m = nuevo(SistemaIntervenido.MOTOR);
        m.agregarMotor(babor, new BigDecimal("1200"), true);
        m.agregarMotor(estribor, new BigDecimal("1198.5"), true);
        m.marcarSinNovedades();
        m.agregarRepuestoOtro("Kit Suzuki 100 h", BigDecimal.ONE, "kit", null);

        m.validar();

        assertThat(m.getMotores()).extracting(MantenimientoMotor::getProximoHorometro)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(new BigDecimal("1300"), new BigDecimal("1298.5"));
        assertThat(babor.getHorometroActual()).isEqualByComparingTo("1200");
    }

    @Test
    void correctivoNoReiniciaElConteoDe100Horas() {
        Mantenimiento m = nuevo(SistemaIntervenido.MOTOR);
        MantenimientoMotor mm = m.agregarMotor(babor, new BigDecimal("1250"), false);
        assertThat(mm.getProximoHorometro()).isNull();
    }

    @Test
    void exigeMarcarRepuestosONoAplica() {
        Mantenimiento m = nuevo(SistemaIntervenido.CASCO);
        m.marcarSinNovedades();
        assertThatThrownBy(m::validar).isInstanceOf(ReglaNegocioException.class).hasMessageContaining("repuestos");
        m.marcarSinRepuestos();
        m.validar();
    }

    @Test
    void responsableDebeSerPiloto() {
        Mantenimiento m = new Mantenimiento(betanci, TipoMantenimiento.PREVENTIVO, SistemaIntervenido.CASCO,
                LocalDate.now(), "Limpieza", persona(Cargo.OPERADOR_TAQUILLA));
        m.marcarSinRepuestos();
        m.marcarSinNovedades();
        assertThatThrownBy(m::validar).hasMessageContaining("piloto");
    }

    @Test
    void recomendacionUnaPorTipoYConDescripcion() {
        Mantenimiento m = nuevo(SistemaIntervenido.CASCO);
        m.recomendar(TipoRecomendacion.PREDICTIVA, "Vibración creciente: revisar hélice en 50 h");
        m.recomendar(TipoRecomendacion.PREDICTIVA, "Revisar hélice en 40 h");
        assertThat(m.getRecomendaciones()).singleElement()
                .extracting(MantenimientoRecomendacion::getDescripcion).isEqualTo("Revisar hélice en 40 h");
        assertThatThrownBy(() -> m.recomendar(TipoRecomendacion.CORRECTIVA, " "))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void motorDeOtraEmbarcacionNoSePermite() {
        Embarcacion yapel = new Embarcacion("PAT-002", "Yapel", 40);
        Motor ajeno = yapel.agregarMotor(PosicionMotor.BABOR, "Suzuki", "DF200", "S-3", 200);
        assertThatThrownBy(() -> nuevo(SistemaIntervenido.MOTOR).agregarMotor(ajeno, BigDecimal.TEN, true))
                .isInstanceOf(ReglaNegocioException.class);
    }

    private Mantenimiento nuevo(SistemaIntervenido sistema) {
        return new Mantenimiento(betanci, TipoMantenimiento.PREVENTIVO, sistema, LocalDate.of(2026, 11, 7),
                "Servicio", piloto);
    }

    private static Personal persona(Cargo cargo) {
        return new Personal(TipoIdentificacion.CC, String.valueOf(cargo.hashCode()), "Ana", "Pérez", cargo,
                TipoVinculacion.LABORAL, LocalDate.of(2026, 10, 1));
    }
}
