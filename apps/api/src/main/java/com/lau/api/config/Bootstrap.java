package com.lau.api.config;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.Operacion;
import com.lau.api.dominio.Personal;
import com.lau.api.dominio.Usuario;
import com.lau.api.dominio.VerificacionPrevia;
import com.lau.api.dominio.enums.*;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.repositorio.EmbarcacionRepositorio;
import com.lau.api.repositorio.OperacionRepositorio;
import com.lau.api.repositorio.PersonalRepositorio;
import com.lau.api.repositorio.UsuarioRepositorio;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Datos de prueba (ficticios). Solo se cargan con la base vacía y nunca con el perfil "prod".
 * Se inicia sesión con el número de identificación; contraseña de todos: Businu2026*
 */
@Component
@Profile("!prod")
public class Bootstrap implements ApplicationRunner {

    private static final ZoneId COLOMBIA = ZoneId.of("America/Bogota");
    private static final LocalDate INICIO = LocalDate.of(2026, 10, 1);

    private final EmbarcacionRepositorio embarcaciones;
    private final PersonalRepositorio personal;
    private final OperacionRepositorio operaciones;
    private final UsuarioRepositorio usuarios;
    private final PasswordEncoder encoder;

    public Bootstrap(EmbarcacionRepositorio embarcaciones, PersonalRepositorio personal,
                     OperacionRepositorio operaciones, UsuarioRepositorio usuarios, PasswordEncoder encoder) {
        this.embarcaciones = embarcaciones;
        this.personal = personal;
        this.operaciones = operaciones;
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (embarcaciones.count() > 0) {
            return;
        }
        Embarcacion betanci = embarcacion(Embarcaciones.BETANCI, "FL-MTR-0001", "SZK-41A001", "SZK-41A002");
        Embarcacion yapel = embarcacion(Embarcaciones.YAPEL, "FL-MTR-0002", "SZK-41B003", "SZK-41B004");
        embarcaciones.saveAll(List.of(betanci, yapel));

        crearPersonal();
        crearViajes(betanci, yapel);
    }

    // ---------------------------------------------------------------- Embarcaciones

    private static Embarcacion embarcacion(Embarcaciones nombre, String patente, String serialBabor, String serialEstribor) {
        Embarcacion e = new Embarcacion(patente, nombre, 40);
        e.setConstructor("COTECMAR");
        e.setAnioConstruccion(2025);
        e.agregarMotor("Suzuki", "DF200 XF", serialBabor, 200);
        e.agregarMotor("Suzuki", "DF200 XF", serialEstribor, 200);
        return e;
    }

    // ---------------------------------------------------------------- Personal (§7.3)

    private void crearPersonal() {
        String clave = encoder.encode("Businu2026*");
        String[][] equipo = {
                {"1067900001", "Andrés Felipe", "Berrío Martínez", "SUPERVISOR"},
                {"1067900011", "Carlos Mario", "Petro Doria", "OPERADOR_EMBARCACION"},
                {"1067900012", "Jhon Jairo", "Negrete Pacheco", "OPERADOR_EMBARCACION"},
                {"1067900013", "Luis Alberto", "Hoyos Lora", "OPERADOR_EMBARCACION"},
                {"1067900014", "Rafael Antonio", "Galeano Ruiz", "OPERADOR_EMBARCACION"},
                {"1067900015", "Edwin José", "Montes Sierra", "OPERADOR_EMBARCACION"},
                {"1067900016", "Yeison David", "Ortega Padilla", "OPERADOR_EMBARCACION"},
                {"1067900017", "Wilmer Enrique", "Salgado Cogollo", "OPERADOR_EMBARCACION"},
                {"1067900018", "Óscar Iván", "Correa Pérez", "OPERADOR_EMBARCACION"},
                {"1067900021", "María Fernanda", "Lugo Arrieta", "GUIA_TURISTICA"},
                {"1067900022", "Daniela", "Espitia Ramos", "GUIA_TURISTICA"},
                {"1067900023", "Camilo Andrés", "Villadiego Soto", "GUIA_TURISTICA"},
                {"1067900024", "Valentina", "Pérez Almanza", "GUIA_TURISTICA"},
                {"1067900031", "Luz Dary", "Banda Agámez", "TAQUILLERO"},       // Rancho Grande
                {"1067900032", "Yuliana", "Durango Vergara", "TAQUILLERO"},     // Rancho Grande
                {"1067900033", "Sandra Milena", "Causil Rivero", "TAQUILLERO"}, // Calle 22
                {"1067900034", "Kelly Johana", "Mestra Polo", "TAQUILLERO"},    // Calle 22
                {"1067900035", "José Luis", "Arteaga Díaz", "TAQUILLERO"},      // Centro Verde
                {"1067900036", "Paola Andrea", "Ayala Benítez", "TAQUILLERO"},  // Centro Verde
        };
        List<Personal> lista = new ArrayList<>();
        int i = 0;
        for (String[] f : equipo) {
            Rol rol = Rol.valueOf(f[3]);
            i++;
            Personal p = new Personal(TipoIdentificacion.CC, f[0], f[1], f[2], rol,
                    rol == Rol.GUIA_TURISTICA ? TipoVinculacion.PRESTACION_SERVICIOS : TipoVinculacion.LABORAL,
                    "personal" + i + "@businu.test",
                    INICIO,
                    rol.usaSistema() ? clave : null);   // los guías no tienen contraseña
            lista.add(p);
        }
        personal.saveAll(lista);
    }

    private Personal buscar(String documento) {
        return personal.findByNumeroIdentificacion(documento).orElseThrow(() -> new NotFoundException("No se ha encontrado personal con el número de identificación"));
    }

    // ---------------------------------------------------------------- Viajes de ayer (§4.1 y §4.2)

    private void crearViajes(Embarcacion betanci, Embarcacion yapel) {
        LocalDate ayer = diaHabilAnterior(LocalDate.now(COLOMBIA));
        List<Operacion> viajes = List.of(
                // Betancí: Rancho Grande → Centro Verde y regreso
                viaje(betanci, true, ayer, 6, 30, 22, 10, "1067900011", "1067900012", "1067900021"),
                viaje(betanci, false, ayer, 8, 25, 0, 12, "1067900011", "1067900012", "1067900021"),
                viaje(betanci, true, ayer, 14, 40, 15, 18, "1067900015", "1067900016", "1067900023"),
                // Yapel: sentido contrario
                viaje(yapel, false, ayer, 6, 18, 6, 9, "1067900013", "1067900014", "1067900022"),
                viaje(yapel, true, ayer, 8, 35, 10, 14, "1067900013", "1067900014", "1067900022")
        );

        // Un viaje suspendido por tormenta (§7.1.7), sin pasajeros
        Operacion suspendido = nuevaOperacion(yapel, false, ayer.atTime(14, 30), "1067900017", "1067900018", "1067900024");
        suspendido.actualizarBitacora(CondicionClimatica.TORMENTA_ELECTRICA, NivelRio.NORMAL, Operacion.SIN_NOVEDADES, Operacion.SIN_NOVEDADES);
        suspendido.suspender(suspendido.getFechaHoraInicio().plusSeconds(600), "Tormenta eléctrica: suspensión preventiva");

        List<Operacion> todas = new ArrayList<>(viajes);
        todas.add(suspendido);
        operaciones.saveAll(todas);
        todas.forEach(op -> usuarios.saveAll(op.getUsuarios()));
    }

    /**
     * @param abordanOrigen  cuántos suben en el embarcadero de origen
     * @param bajanEnCalle22 de los anteriores, cuántos van hasta Calle 22
     * @param abordanCalle22 cuántos suben en Calle 22 (van hasta el destino final)
     */
    private Operacion viaje(Embarcacion embarcacion, boolean haciaCentroVerde, LocalDate dia, int hora,
                            int abordanOrigen, int bajanEnCalle22, int abordanCalle22,
                            String operador, String auxiliar, String guia) {
        Operacion op = nuevaOperacion(embarcacion, haciaCentroVerde, dia.atTime(hora, 30), operador, auxiliar, guia);
        Instant inicio = op.getFechaHoraInicio();
        Embarcaderos origen = op.getEmbarcaderoOrigen();
        Embarcaderos destino = op.getEmbarcaderoDestino();

        abordaje(op, origen, abordanOrigen, taquillero(origen),
                Map.of(Embarcaderos.CENTRO_CALLE_22, bajanEnCalle22, destino, abordanOrigen - bajanEnCalle22), inicio);

        Instant llegada22 = inicio.plusSeconds(25 * 60);
        op.registrarParada(Embarcaderos.CENTRO_CALLE_22, llegada22, llegada22.plusSeconds(5 * 60));
        if (abordanCalle22 > 0) {
            abordaje(op, Embarcaderos.CENTRO_CALLE_22, abordanCalle22, taquillero(Embarcaderos.CENTRO_CALLE_22),
                    Map.of(destino, abordanCalle22), llegada22);
        }

        op.finalizar(inicio.plusSeconds(55 * 60), null);
        return op;
    }

    private Operacion nuevaOperacion(Embarcacion embarcacion, boolean haciaCentroVerde, java.time.LocalDateTime salida,
                                     String operador, String auxiliar, String guia) {
        Embarcaderos origen = haciaCentroVerde ? Embarcaderos.RANCHO_GRANDE : Embarcaderos.CENTRO_VERDE;
        Embarcaderos destino = haciaCentroVerde ? Embarcaderos.CENTRO_VERDE : Embarcaderos.RANCHO_GRANDE;
        Personal piloto = buscar(operador);

        Operacion op = new Operacion(embarcacion, origen, destino, salida.atZone(COLOMBIA).toInstant(),
                EstadoNave.BUENO, new VerificacionPrevia(true, true, true, true, true, true, true), null,
                CondicionClimatica.DESPEJADO, NivelRio.NORMAL, Set.of(CondicionesMecanicas.SIN_NOVEDAD), "",piloto);
        op.agregarTripulante(piloto);
        op.agregarTripulante(buscar(auxiliar));
        op.agregarTripulante(buscar(guia));
        return op;
    }

    /** Taquillero del turno de la mañana en cada embarcadero. */
    private Personal taquillero(Embarcaderos embarcadero) {
        return buscar(switch (embarcadero) {
            case RANCHO_GRANDE -> "1067900031";
            case CENTRO_CALLE_22 -> "1067900033";
            default -> "1067900035";
        });
    }

    /** Registro de abordaje: las distribuciones por sexo, grupo etario y destino suman el total. */
    private static void abordaje(Operacion op, Embarcaderos embarcadero, int total, Personal taquillero,
                                 Map<Embarcaderos, Integer> porDestino, Instant fechaHora) {
        Usuario u = new Usuario(fechaHora, op, embarcadero, total, taquillero);

        int mujeres = total / 2;
        u.registrarSexo(Sexo.FEMENINO, mujeres);
        u.registrarSexo(Sexo.MASCULINO, total - mujeres);

        int menores = total / 5;
        int mayores = total / 10;
        u.registrarGrupoEtario(GrupoEtario.INFANCIA, menores);
        u.registrarGrupoEtario(GrupoEtario.PERSONA_MAYOR, mayores);
        u.registrarGrupoEtario(GrupoEtario.ADULTEZ, total - menores - mayores);

        porDestino.forEach(u::registrarDestino);   // cantidad 0 se ignora
    }

    private static LocalDate diaHabilAnterior(LocalDate hoy) {
        LocalDate d = hoy.minusDays(1);
        while (d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY) {
            d = d.minusDays(1);
        }
        return d;
    }
}