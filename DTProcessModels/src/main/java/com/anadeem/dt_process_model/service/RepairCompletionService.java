package com.anadeem.dt_process_model.service;

import com.anadeem.dt_process_model.repair.RepairHistoryManager;
import com.anadeem.dt_process_model.repair.*;
import org.springframework.stereotype.Service;

@Service
public class RepairCompletionService {
    private final RepairHistoryManager repairHistoryManager;
    private final RepairService repairService;

    public RepairCompletionService(RepairHistoryManager repairHistoryManager, RepairService repairService) {
        this.repairHistoryManager = repairHistoryManager;
        this.repairService = repairService;
    }

    public String completeRepair(String repairId) {
        // Log the repair step
        repairHistoryManager.logRepairStep(repairId, 9, "Repair Completed");

        // Mark as completed
        repairService.markRepairAsCompleted(repairId);

        return "Repair task " + repairId + " marked as completed.";
    }

    public RepairReport getRepairReport(String repairId) {
        return repairHistoryManager.getRepairReport(repairId);
    }
}