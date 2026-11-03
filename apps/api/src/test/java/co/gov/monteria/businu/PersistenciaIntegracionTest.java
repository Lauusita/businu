package co.gov.monteria.businu;

import static org.assertj.core.api.Assertions.assertThat;

import co.gov.monteria.businu.catalogos.CatalogoItemRepository;
import co.gov.monteria.businu.catalogos.Embarcadero;
import co.gov.monteria.businu.catalogos.EmbarcaderoRepository;
import co.gov.monteria.businu.catalogos.TipoCatalogo;
import co.gov.monteria.businu.flota.Embarcacion;
import co.gov.monteria.businu.flota.EmbarcacionRepository;
import co.gov.monteria.businu.flota.Motor;
import co.gov.monteria.businu.flota.PosicionMotor;
import co.gov.monteria.businu.mantenimiento.EstadoProyeccion;
import co.gov.monteria.businu.mantenimiento.Mantenimiento;
import co.gov.monteria.businu.mantenimiento.MantenimientoRepository;
import co.gov.monteria.businu.mantenimiento.ProximoServicioService;
import co.gov.monteria.businu.mantenimiento.Severidad;
import co.gov.monteria.businu.mantenimiento.SistemaIntervenido;
import co.gov.monteria.businu.mantenimiento.TipoMantenimiento;
import co.gov.monteria.businu.mantenimiento.TipoRecomendacion;
import co.gov.monteria.businu.pasajeros.GrupoEtario;
import co.gov.monteria.businu.pasajeros.RegistroPasajeros;
import co.gov.monteria.businu.pasajeros.RegistroPasajerosRepository;
import co.gov.monteria.businu.pasajeros.Sexo;
import co.gov.monteria.businu.personal.Cargo;
import co.gov.monteria.businu.personal.Personal;
import co.gov.monteria.businu.personal.PersonalRepository;
import co.gov.monteria.businu.personal.RequisitoDocumentalRepository;
import co.gov.monteria.businu.personal.Rol;
import co.gov.monteria.businu.personal.TipoIdentificacion;
import co.gov.monteria.businu.personal.TipoVinculacion;
import co.gov.monteria.businu.personal.UsuarioSistema;
import co.gov.monteria.businu.personal.UsuarioSistemaRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prueba contra PostgreSQL real: Flyway crea el esquema, Hibernate lo valida contra las entidades
 * y se guardan agregados completos. Se ejecuta solo si está definida DB_URL.
 */
@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class PersistenciaIntegracionTest {

    @Autowired EmbarcaderoRepository embarcaderos;
    @Autowired CatalogoItemRepository catalogo;
    @Autowired EmbarcacionRepository embarcaciones;
    @Autowired MantenimientoRepository mantenimientos;
    @Autowired PersonalRepository personal;
    @Autowired UsuarioSistemaRepository usuarios;
    @Autowired RegistroPasajerosRepository registros;
    @Autowired RequisitoDocumentalRepository requisitos;
    @Autowired ProximoServicioService proximoServicio;

    @Test
    void datosInicialesCargados() {
        assertThat(embarcaderos.findByActivoTrueOrderByOrdenRuta())
                .extracting(Embarcadero::getNombre)
                .containsExactly("Rancho Grande", "Centro – Calle 22", "Centro Verde");
        assertThat(catalogo.findByTipoAndActivoTrueOrderByCategoriaAscOrdenAsc(TipoCatalogo.REPUESTO_INSUMO)).hasSize(34);
        assertThat(catalogo.findByTipoAndActivoTrueOrderByCategoriaAscOrdenAsc(TipoCatalogo.NOVEDAD)).hasSize(23);
        assertThat(requisitos.findByCargo(Cargo.CONDUCTOR)).hasSize(11);
    }

    @Test
    void guardaHojaDeVidaYPasajeros() {
        Embarcacion betanci = new Embarcacion("PAT-PRUEBA-1", "Betancí-IT", 40);
        Motor babor = betanci.agregarMotor(PosicionMotor.BABOR, "Suzuki", "DF200", "IT-S1", 200);
        betanci.agregarMotor(PosicionMotor.ESTRIBOR, "Suzuki", "DF200", "IT-S2", 200);
        embarcaciones.saveAndFlush(betanci);

        Personal piloto = personal.save(new Personal(TipoIdentificacion.CC, "1001", "Carlos", "Mendoza",
                Cargo.CONDUCTOR, TipoVinculacion.LABORAL, LocalDate.of(2026, 10, 1)));

        var aceite = catalogo.findByTipoAndActivoTrueOrderByCategoriaAscOrdenAsc(TipoCatalogo.REPUESTO_INSUMO).stream()
                .filter(i -> i.getNombre().equals("Aceite de motor 4T")).findFirst().orElseThrow();
        var vibracion = catalogo.findByTipoAndActivoTrueOrderByCategoriaAscOrdenAsc(TipoCatalogo.NOVEDAD).stream()
                .filter(i -> i.getNombre().equals("Vibración")).findFirst().orElseThrow();

        Mantenimiento m = new Mantenimiento(betanci, TipoMantenimiento.PREVENTIVO, SistemaIntervenido.MOTOR,
                LocalDate.now().minusDays(1), "Servicio de 100 horas", piloto);
        m.agregarMotor(babor, new BigDecimal("1200"), true);
        m.agregarRepuesto(aceite, new BigDecimal("6"), "litros", new BigDecimal("180000"));
        m.agregarRepuestoOtro("Kit de juntas", BigDecimal.ONE, "kit", null);
        m.agregarNovedad(vibracion, Severidad.MEDIA, "Leve a altas RPM");
        m.recomendar(TipoRecomendacion.PREDICTIVA, "Vigilar vibración; revisar hélice en el próximo servicio");
        mantenimientos.saveAndFlush(m);

        assertThat(mantenimientos.findByEmbarcacionIdOrderByFechaIntervencionDesc(betanci.getId())).hasSize(1);
        assertThat(m.getCreadoPor()).isEqualTo("sistema");
        assertThat(m.getVersion()).isZero();

        var proyeccion = proximoServicio.proyectar(babor.getId());
        assertThat(proyeccion.proximoHorometro()).isEqualByComparingTo("1300");
        assertThat(proyeccion.estado()).isEqualTo(EstadoProyeccion.AL_DIA);

        Personal taquilla = personal.save(new Personal(TipoIdentificacion.CC, "1002", "Laura", "Gómez",
                Cargo.OPERADOR_TAQUILLA, TipoVinculacion.LABORAL, LocalDate.of(2026, 10, 1)));
        UsuarioSistema usuario = usuarios.save(new UsuarioSistema(taquilla, "lgomez", "{noop}x", Rol.TAQUILLERO));
        List<Embarcadero> e = embarcaderos.findByActivoTrueOrderByOrdenRuta();

        UUID idCliente = UUID.randomUUID();
        RegistroPasajeros r = new RegistroPasajeros(idCliente, Instant.now(), e.get(0), e.get(2), usuario);
        r.establecerCantidad(GrupoEtario.JUVENTUD, Sexo.MUJER, 4);
        r.establecerCantidad(GrupoEtario.ADULTEZ, Sexo.HOMBRE, 6);
        registros.saveAndFlush(r);

        assertThat(registros.findById(idCliente)).get().extracting(RegistroPasajeros::getTotal).isEqualTo(10);
    }
}
