package com.lau.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** /pqrs muestra el formulario público (la URL del código QR). */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/pqrs").setViewName("forward:/pqrs/index.html");
    }
}
