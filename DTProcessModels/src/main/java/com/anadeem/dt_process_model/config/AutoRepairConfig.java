package com.anadeem.dt_process_model.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration for automatic repair monitoring
 */
@Configuration
@PropertySource("classpath:application.properties")
public class AutoRepairConfig {

    /**
     * Enum for auto-repair process model selection
     */
    public enum ProcessModelType {
        RANDOM,     // Choose a process model randomly
        SEVERITY,   // Choose based on severity (default)
        STANDARD,   // Always use standard process
        EXPRESS,    // Always use express process
        EMERGENCY   // Always use emergency process
    }

    /**
     * Configuration properties for auto-repair monitoring
     */
    @ConfigurationProperties(prefix = "auto.repair")
    public static class AutoRepairProperties {
        private boolean enabled = false;
        private int checkIntervalSeconds = 60;  // Check interval is 60 seconds
        private ProcessModelType processModel = ProcessModelType.SEVERITY;  // Severity based
        private boolean alertOnNew = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getCheckIntervalSeconds() {
            return checkIntervalSeconds;
        }

        public void setCheckIntervalSeconds(int checkIntervalSeconds) {
            this.checkIntervalSeconds = checkIntervalSeconds;
        }

        public ProcessModelType getProcessModel() {
            return processModel;
        }

        public void setProcessModel(ProcessModelType processModel) {
            this.processModel = processModel;
        }

        public boolean isAlertOnNew() {
            return alertOnNew;
        }

        public void setAlertOnNew(boolean alertOnNew) {
            this.alertOnNew = alertOnNew;
        }
    }

    @Bean
    public AutoRepairProperties autoRepairProperties() {
        return new AutoRepairProperties();
    }
}