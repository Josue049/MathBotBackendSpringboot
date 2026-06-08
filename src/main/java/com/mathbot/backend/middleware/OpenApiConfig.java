package com.mathbot.backend.middleware;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "MathBot API", version = "1.0.0", description = "API de MathBot para registro, autenticación y chat con IA", contact = @Contact(name = "MathBot")), servers = {
        @Server(url = "http://localhost:8080", description = "Servidor local")
})
public class OpenApiConfig {
}