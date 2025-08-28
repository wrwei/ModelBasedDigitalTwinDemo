package com.anadeem.dt_process_model.repair;

import java.util.List;

/**
 * Model class representing repair-list.js repair report
 */
public class RepairReport {

    private String repairId;
    private String currentStatus;
    private List<RepairHistoryManager.RepairStep> steps;

    public RepairReport() {
    }

    public String getRepairId() {
        return repairId;
    }

    public void setRepairId(String repairId) {
        this.repairId = repairId;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public List<RepairHistoryManager.RepairStep> getSteps() {
        return steps;
    }

    public void setSteps(List<RepairHistoryManager.RepairStep> steps) {
        this.steps = steps;
    }
}