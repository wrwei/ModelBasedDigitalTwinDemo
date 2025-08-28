package com.anadeem.dt_process_model.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for mapping between application repair statuses
 * and BPMN process statuses to ensure consistent integration
 */
@Service
public class RepairStatusMapperService {

    // Map from application status to BPMN status
    private static final Map<String, String> APP_TO_BPMN_STATUS = new HashMap<>();

    // Map from BPMN status to application status
    private static final Map<String, String> BPMN_TO_APP_STATUS = new HashMap<>();

    static {
        // Initialise status mappings

        // In-progress BPMN status
        addStatusMapping("Repair In Progress", "in-progress");
        addStatusMapping("Repair Initiated", "in-progress");
        addStatusMapping("Surface Inspection", "in-progress");
        addStatusMapping("Retrying Repair", "in-progress");

        // Delayed BPMN status
        addStatusMapping("Repair Delayed", "delayed");
        addStatusMapping("Human Intervention Required", "delayed");
        addStatusMapping("Escalated to Human Repair", "delayed");
        addStatusMapping("Vehicle Dispatched", "delayed");

        // Completed BPMN status
        addStatusMapping("Repair Completed", "completed");
    }

    private static void addStatusMapping(String appStatus, String bpmnStatus) {
        APP_TO_BPMN_STATUS.put(appStatus.toLowerCase(), bpmnStatus);
        // Only set the first app status as the primary one for reverse lookup
        if (!BPMN_TO_APP_STATUS.containsKey(bpmnStatus)) {
            BPMN_TO_APP_STATUS.put(bpmnStatus, appStatus);
        }
    }
}