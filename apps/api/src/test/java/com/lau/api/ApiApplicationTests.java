package com.lau.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/** Levanta el contexto completo; necesita PostgreSQL, por eso solo corre si DB_URL está definida. */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class ApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
