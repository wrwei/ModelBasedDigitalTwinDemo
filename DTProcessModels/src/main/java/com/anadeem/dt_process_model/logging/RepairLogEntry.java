package com.anadeem.dt_process_model.logging;

import java.time.LocalDateTime;

public class RepairLogEntry {
    private LocalDateTime timestamp;
    private String category;
    private String message;

    public RepairLogEntry() {
    }

    public RepairLogEntry(String category, String message) {
        this.timestamp = LocalDateTime.now();
        this.category = category;
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }
}
