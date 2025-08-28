package com.anadeem.dt_process_model.repair;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Component to track and manage repair status
 */
@Component
public class RepairStatusManager {

    // Maps repairId to its current status code
    private final Map<String, Integer> repairStatuses = new ConcurrentHashMap<>();

    // Maps repairId to retry count
    private final Map<String, Integer> retryCount = new ConcurrentHashMap<>();

    // Maximum number of retries allowed
    private static final int MAX_RETRIES = 3;

    /**
     * Update the status of repair-list.js repair
     *
     * @param repairId The repair ID
     * @param statusCode The status code
     * @param statusDescription Text description of the status
     */
    public void updateRepairStatus(String repairId, int statusCode, String statusDescription) {
        repairStatuses.put(repairId, statusCode);
        System.out.println("Updated repair " + repairId + " status to: " + statusDescription + " (code: " + statusCode + ")");
    }
}