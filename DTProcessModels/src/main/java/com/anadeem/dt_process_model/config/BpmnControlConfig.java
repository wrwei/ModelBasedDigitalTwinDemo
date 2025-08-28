package com.anadeem.dt_process_model.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration for BPMN process execution control mode
 */
@Configuration
@PropertySource("classpath:application.properties")
public class BpmnControlConfig {

    /**
     * Enum of available control modes for BPMN execution
     */
    public enum ControlMode {
        AUTO,       // Automatic execution with no delays
        DELAYED,    // Execution with configured delays
    }

    /**
     * Configuration properties for BPMN process control
     */
    @ConfigurationProperties(prefix = "bpmn.control")
    public static class ControlProperties {
        private ControlMode mode = ControlMode.DELAYED;

        public ControlMode getMode() {
            return mode;
        }

        public void setMode(ControlMode mode) {
            this.mode = mode;
        }
    }

    @Bean
    public ControlProperties controlProperties() {
        return new ControlProperties();
    }
}