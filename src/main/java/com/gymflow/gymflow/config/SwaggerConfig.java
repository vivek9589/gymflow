package com.gymflow.gymflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server localServer = new Server()
                .url("http://localhost:8082")
                .description("Local development server");

        Server deployedServer = new Server()
                .url("https://fitness-zen-desk.vercel.app") // your deployed frontend URL
                .description("Production frontend (Vercel)");

        Server backendServer = new Server()
                .url("http://80.225.243.194:8080") // your backend server (Ubuntu instance)
                .description("Production backend (Ubuntu server)");

        return new OpenAPI()
                .info(new Info()
                        .title("GymFlow API Documentation")
                        .version("1.0")
                        .description("SaaS Backend for Gym Management"))
                .addServersItem(localServer)
                .addServersItem(deployedServer)
                .addServersItem(backendServer)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
