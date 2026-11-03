package com.lau.api.compartido;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RelojConfig {

    public static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");

    /** Las fechas operativas se calculan en hora de Colombia, aunque el servidor esté en us-east-1. */
    @Bean
    Clock reloj() {
        return Clock.system(ZONA_COLOMBIA);
    }
}
