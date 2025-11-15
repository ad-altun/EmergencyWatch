package de.denizaltun.vehiclesimulator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class SimulatorConfig {

    private Vehicles vehicles = new Vehicles();
    private Telemetry telemetry = new Telemetry();

    @Data
    public static class Vehicles {
        private int count = 5;      // default value
    }

    @Data
    public static class Telemetry {
        private int intervalSeconds = 3;        // default value
    }
}
