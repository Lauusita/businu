package com.lau.api.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lau.api.domain.enums.Cargo;
import com.lau.api.domain.enums.GrupoEtario;
import com.lau.api.domain.enums.Rol;
import com.lau.api.domain.enums.Sexo;
import com.lau.api.domain.enums.TipoEmbarcadero;
import com.lau.api.domain.enums.TipoIdentificacion;
import com.lau.api.domain.enums.TipoVinculacion;
import com.lau.api.exceptions.ReglaNegocioException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegistroPasajerosTest {

    private final Embarcadero rancho = new Embarcadero("RANCHO_GRANDE", "Rancho Grande", TipoEmbarcadero.EMBARCADERO, 1);
    private final Embarcadero verde = new Embarcadero("CENTRO_VERDE", "Centro Verde", TipoEmbarcadero.EMBARCADERO, 3);
    private final UsuarioSistema taquillero = new UsuarioSistema(
            new Personal(TipoIdentificacion.CC, "1", "Luis", "Díaz", Cargo.OPERADOR_TAQUILLA,
                    TipoVinculacion.LABORAL, LocalDate.of(2026, 10, 1)),
            "ldiaz", "hash", Rol.TAQUILLERO);

    @Test
    void totalesSalenDeLaCuadricula() {
        RegistroPasajeros r = new RegistroPasajeros(UUID.randomUUID(), Instant.now(), rancho, verde, taquillero);
        r.establecerCantidad(GrupoEtario.ADOLESCENCIA, Sexo.MUJER, 3);
        r.establecerCantidad(GrupoEtario.ADOLESCENCIA, Sexo.HOMBRE, 2);
        r.establecerCantidad(GrupoEtario.ADULTEZ, Sexo.MUJER, 5);

        assertThat(r.getTotal()).isEqualTo(10);
        assertThat(r.totalPorGrupoEtario()).containsEntry(GrupoEtario.ADOLESCENCIA, 5).containsEntry(GrupoEtario.ADULTEZ, 5);
        assertThat(r.totalPorSexo()).containsEntry(Sexo.MUJER, 8).containsEntry(Sexo.HOMBRE, 2);

        r.establecerCantidad(GrupoEtario.ADULTEZ, Sexo.MUJER, 0);
        assertThat(r.getTotal()).isEqualTo(5);
        assertThat(r.getDetalles()).hasSize(2);
    }

    @Test
    void origenYDestinoDistintos() {
        assertThatThrownBy(() -> new RegistroPasajeros(UUID.randomUUID(), Instant.now(), rancho, rancho, taquillero))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void sinPasajerosNoSeGuarda() {
        RegistroPasajeros r = new RegistroPasajeros(UUID.randomUUID(), Instant.now(), rancho, verde, taquillero);
        assertThatThrownBy(r::validar).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void guiaNoPuedeTenerUsuario() {
        Personal guia = new Personal(TipoIdentificacion.CC, "2", "Eva", "Ruiz", Cargo.GUIA_TURISMO,
                TipoVinculacion.LABORAL, LocalDate.of(2026, 10, 1));
        assertThatThrownBy(() -> new UsuarioSistema(guia, "eruiz", "hash", Rol.PILOTO))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
