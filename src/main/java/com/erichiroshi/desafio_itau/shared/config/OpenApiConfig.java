package com.erichiroshi.desafio_itau.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Desafio Itaú — API de Transações")
                        .description("API REST para registro de transações financeiras " +
                                "e cálculo de estatísticas em tempo real.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Eric Hiroshi")
                                .url("https://linkedin.com/in/eric-hiroshi")
                                .email("erichiroshi@hotmail.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://github.com/erichiroshi/desafio-itau-backend/blob/main/LICENSE")));
    }
}