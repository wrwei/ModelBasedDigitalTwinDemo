package com.anadeem.dt_process_model.repair;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RepairHistoryManager {

    // Maps repairId to list of repair steps
    private final Map<String, List<RepairStep>> repairHistory = new ConcurrentHashMap<>();

    /**
     * Log repair step
     *
     * @param repairId The repair ID
     * @param stepCode The step code
     * @param description Text description of the step
     */
    public void logRepairStep(String repairId, int stepCode, String description) {
        RepairStep step = new RepairStep();
        step.setRepairId(repairId);
        step.setStepCode(stepCode);
        step.setDescription(description);
        step.setTimestamp(new Date());

        // Get or create history list
        List<RepairStep> steps = repairHistory.computeIfAbsent(repairId, k -> new ArrayList<>());
        steps.add(step);

        System.out.println("Logged repair step for " + repairId + ": " + description);
    }

    /**
     * Get report for repair
     *
     * @param repairId The repair ID
     * @return A RepairReport object
     */
    public RepairReport getRepairReport(String repairId) {
        List<RepairStep> steps = repairHistory.getOrDefault(repairId, new ArrayList<>());

        RepairReport report = new RepairReport();
        report.setRepairId(repairId);
        report.setSteps(steps);

        // Determine current status based on last step
        if (!steps.isEmpty()) {
            RepairStep lastStep = steps.get(steps.size() - 1);
            report.setCurrentStatus(lastStep.getDescription());
        } else {
            report.setCurrentStatus("No status available");
        }

        return report;
    }


    public static class RepairStep {
        private String repairId;
        private int stepCode;
        private String description;
        private Date timestamp;

        // Getters and setters

        public String getRepairId() {
            return repairId;
        }

        public void setRepairId(String repairId) {
            this.repairId = repairId;
        }

        public int getStepCode() {
            return stepCode;
        }

        public void setStepCode(int stepCode) {
            this.stepCode = stepCode;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }
    }
}