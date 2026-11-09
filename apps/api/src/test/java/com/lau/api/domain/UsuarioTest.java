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

class UsuarioTest {

    private final Embarcacion betanci = new Embarcacion("PAT-001", "Betancí", 40);
    private final Embarcadero rancho = new Embarcadero("RANCHO_GRANDE", "Rancho Grande", TipoEmbarcadero.EMBARCADERO, 1);
    private final Embarcadero verde = new Embarcadero("CENTRO_VERDE", "Centro Verde", TipoEmbarcadero.EMBARCADERO, 3);
    private final UsuarioSistema taquillero = new UsuarioSistema(
            new Personal(TipoIdentificacion.CC, "1", "Luis", "Díaz", Cargo.OPERADOR_TAQUILLA,
                    TipoVinculacion.LABORAL, LocalDate.of(2026, 10, 1)),
            "ldiaz", "hash", Rol.TAQUILLERO);

    @Test
    void distribucionesQueSumanElTotal() {
        Usuario u = nuevo(30);
        u.registrarSexo(Sexo.MUJER, 20);
        u.registrarSexo(Sexo.HOMBRE, 10);
        u.registrarGrupoEtario(GrupoEtario.PRIMERA_INFANCIA, 2);
        u.registrarGrupoEtario(GrupoEtario.JUVENTUD, 12);
        u.registrarGrupoEtario(GrupoEtario.ADULTEZ, 16);

        u.validar();

        assertThat(u.getPorSexo()).containsEntry(Sexo.MUJER, 20).containsEntry(Sexo.HOMBRE, 10);
        assertThat(u.getPorGrupoEtario()).hasSize(3);
    }

    @Test
    void distribucionPorSexoQueNoSumaElTotalSeRechaza() {
        Usuario u = nuevo(30);
        u.registrarSexo(Sexo.MUJER, 20);
        u.registrarGrupoEtario(GrupoEtario.ADULTEZ, 30);
        assertThatThrownBy(u::validar).isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("sexo suma 20 y debe sumar 30");
    }

    @Test
    void distribucionPorGrupoEtarioQueNoSumaElTotalSeRechaza() {
        Usuario u = nuevo(30);
        u.registrarSexo(Sexo.MUJER, 30);
        u.registrarGrupoEtario(GrupoEtario.ADULTEZ, 25);
        assertThatThrownBy(u::validar).hasMessageContaining("grupo etario suma 25");
    }

    @Test
    void cantidadCeroQuitaLaCategoria() {
        Usuario u = nuevo(5);
        u.registrarSexo(Sexo.MUJER, 3);
        u.registrarSexo(Sexo.MUJER, 0);
        assertThat(u.getPorSexo()).isEmpty();
    }

    @Test
    void noSuperaLaCapacidadDeLaEmbarcacion() {
        assertThatThrownBy(() -> nuevo(41)).hasMessageContaining("capacidad");
    }

    @Test
    void origenYDestinoDistintos() {
        assertThatThrownBy(() -> new Usuario(UUID.randomUUID(), Instant.now(), betanci, rancho, rancho, 5, taquillero))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void guiaNoPuedeTenerUsuarioDeSistema() {
        Personal guia = new Personal(TipoIdentificacion.CC, "2", "Eva", "Ruiz", Cargo.GUIA_TURISMO,
                TipoVinculacion.LABORAL, LocalDate.of(2026, 10, 1));
        assertThatThrownBy(() -> new UsuarioSistema(guia, "eruiz", "hash", Rol.PILOTO))
                .isInstanceOf(ReglaNegocioException.class);
    }

    private Usuario nuevo(int total) {
        return new Usuario(UUID.randomUUID(), Instant.now(), betanci, rancho, verde, total, taquillero);
    }
}
