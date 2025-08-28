package com.anadeem.dt_process_model.service;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.json.JSONObject;

@Component("checkRepairStatusBean")
public class CheckRepairStatusService implements JavaDelegate {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void execute(DelegateExecution execution) {
        String repairId = (String) execution.getVariable("repairId");
        System.out.println("Checking repair status for repairId: " + repairId);

        // Call the API to fetch repair status
        String currentStatus = fetchRepairStatus(repairId);

        // Map application status to BPMN process status
        String bpmnStatus = mapToBpmnStatus(currentStatus);

        // Set status variable for BPMN decision making
        execution.setVariable("status", bpmnStatus);
        System.out.println("Set BPMN status variable to: " + bpmnStatus);
    }

    private String fetchRepairStatus(String repairId) {
        try {
            String url = "http://localhost:8080/process/report?repairId=" + repairId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(response.getBody());
            String status = jsonResponse.optString("currentStatus", "unknown");

            System.out.println("Fetched application status: " + status);
            return status.toLowerCase(); // Convert to lowercase
        } catch (Exception e) {
            System.err.println("Error fetching repair status: " + e.getMessage());
            return "unknown";
        }
    }

    /**
     * Maps application status values to BPMN-expected status values
     */
    private String mapToBpmnStatus(String appStatus) {
        switch (appStatus.toLowerCase()) {
            case "repair in progress":
            case "repair initiated":
            case "surface inspection":
            case "retrying repair":
                return "in-progress";

            case "repair delayed":
            case "human intervention required":
            case "escalated to human repair":
            case "vehicle dispatched":
                return "delayed";

            case "repair completed":
                return "completed";

            default:
                System.out.println("Unknown status mapped to delayed: " + appStatus);
                return "delayed";
        }
    }
}