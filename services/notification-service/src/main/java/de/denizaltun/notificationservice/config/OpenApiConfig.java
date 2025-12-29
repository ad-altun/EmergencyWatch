package de.denizaltun.notificationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EmergencyWatch Notification API")
                        .description("Alert management and notification system for emergency vehicle fleet")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Deniz Altun")
                                .url("https://denizaltun.de")));
    }
}
