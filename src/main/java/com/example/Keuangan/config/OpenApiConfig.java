package com.example.Keuangan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Keuangan API")
                        .version("1.0.0")
                        .description("API untuk manajemen keuangan pribadi")
                        .contact(new Contact()
                                .name("Tim Keuangan")
                                .email("dev@contoh.com")));
    }
}