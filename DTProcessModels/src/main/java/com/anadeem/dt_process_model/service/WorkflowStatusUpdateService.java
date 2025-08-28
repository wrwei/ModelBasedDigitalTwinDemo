package com.anadeem.dt_process_model.service;

import org.springframework.stereotype.Service;
import com.anadeem.dt_process_model.repair.RepairStatusManager;
import com.anadeem.dt_process_model.config.BpmnControlConfig.ControlMode;
import com.anadeem.dt_process_model.config.BpmnControlConfig.ControlProperties;


@Service
public class WorkflowStatusUpdateService {

    private final RepairService repairService;
    private final RepairStatusManager repairStatusManager;

    public WorkflowStatusUpdateService(
            RepairService repairService,
            RepairStatusManager repairStatusManager) {
        this.repairService = repairService;
        this.repairStatusManager = repairStatusManager;
    }

    /**
     * Update repair status from workflow
     * @param repairId The repair ID
     * @param taskName The BPMN task name
     * @param status The workflow status (in-progress, completed, delayed)
     */
    public void updateWorkflowStatus(String repairId, String taskName, String status) {
        // Convert workflow task name and status to application status
        String appStatus = mapToApplicationStatus(taskName, status);

        // Update status in repair service for the repairs list
        repairService.updateRepairStatus(repairId, appStatus);

        // Also update in the status manager with numeric code
        int statusCode = mapToStatusCode(taskName, status);
        repairStatusManager.updateRepairStatus(repairId, statusCode, appStatus);

        System.out.println("📊 Updated status for repair " + repairId + ": " + appStatus +
                " (from workflow task: " + taskName + ", status: " + status + ")");
    }

    /**
     * Map workflow task name and status to application status
     */
    private String mapToApplicationStatus(String taskName, String status) {
        if ("completed".equalsIgnoreCase(status)) {
            if (taskName.contains("Close") || taskName.contains("Complete")) {
                return "Repair Completed";
            } else {
                return "Repair In Progress";
            }
        } else if ("delayed".equalsIgnoreCase(status)) {
            return "Repair Delayed";
        } else {
            // Map specific task names to statuses
            if (taskName.contains("Assess")) {
                return "Surface Inspection";
            } else if (taskName.contains("Dispatch")) {
                return "Vehicle Dispatched";
            } else if (taskName.contains("Monitor")) {
                return "Repair In Progress";
            } else if (taskName.contains("Verify")) {
                return "Quality Verification";
            } else {
                return "Repair In Progress";
            }
        }
    }

    /**
     * Map to status code (for RepairStatusManager)
     */
    private int mapToStatusCode(String taskName, String status) {
        if ("completed".equalsIgnoreCase(status)) {
            if (taskName.contains("Close") || taskName.contains("Complete")) {
                return 9; // Repair Completed
            } else {
                return 6; // In progress
            }
        } else if ("delayed".equalsIgnoreCase(status)) {
            return 7; // Delayed
        } else {
            // Map specific task names to codes
            if (taskName.contains("Assess")) {
                return 4; // Surface inspection
            } else if (taskName.contains("Dispatch")) {
                return 3; // Vehicle dispatched
            } else if (taskName.contains("Monitor")) {
                return 6; // In progress
            } else if (taskName.contains("Verify")) {
                return 8; // Quality verification
            } else {
                return 5; // Generic "in progress"
            }
        }
    }
}