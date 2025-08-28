package com.anadeem.dt_process_model.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import com.anadeem.dt_process_model.service.DelayService;

/**
 * Configuration for workflow delays
 */
@Configuration
@PropertySource("classpath:application.properties")
public class WorkflowDelayConfig {

    /**
     * Configuration properties for workflow delays
     */
    @ConfigurationProperties(prefix = "workflow.delays")
    public static class DelayProperties {
        private boolean enabled = true;
        private int shortMinMs = 2000;
        private int shortMaxMs = 5000;
        private int mediumMinMs = 5000;
        private int mediumMaxMs = 10000;
        private int longMinMs = 10000;
        private int longMaxMs = 20000;

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getShortMinMs() {
            return shortMinMs;
        }

        public void setShortMinMs(int shortMinMs) {
            this.shortMinMs = shortMinMs;
        }

        public int getShortMaxMs() {
            return shortMaxMs;
        }

        public void setShortMaxMs(int shortMaxMs) {
            this.shortMaxMs = shortMaxMs;
        }

        public int getMediumMinMs() {
            return mediumMinMs;
        }

        public void setMediumMinMs(int mediumMinMs) {
            this.mediumMinMs = mediumMinMs;
        }

        public int getMediumMaxMs() {
            return mediumMaxMs;
        }

        public void setMediumMaxMs(int mediumMaxMs) {
            this.mediumMaxMs = mediumMaxMs;
        }

        public int getLongMinMs() {
            return longMinMs;
        }

        public void setLongMinMs(int longMinMs) {
            this.longMinMs = longMinMs;
        }

        public int getLongMaxMs() {
            return longMaxMs;
        }

        public void setLongMaxMs(int longMaxMs) {
            this.longMaxMs = longMaxMs;
        }
    }

    @Bean
    public DelayProperties delayProperties() {
        return new DelayProperties();
    }

    @Bean
    public DelayService delayService(DelayProperties properties) {
        DelayService service = new DelayService();
        // Configure based on properties
        service.setDelaysEnabled(properties.isEnabled());
        return service;
    }
}