package com.anadeem.dt_process_model.controller;

import com.anadeem.dt_process_model.logging.RepairLogService;
import com.anadeem.dt_process_model.logging.RepairLogEntry;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/process")
public class RepairLogController {

    private final RepairLogService repairLogService;

    public RepairLogController(RepairLogService repairLogService) {
        this.repairLogService = repairLogService;
    }
    @GetMapping("/logs")
    public Map<String, List<RepairLogEntry>> getRepairLogs(@RequestParam String repairId) {
        return repairLogService.getLogs(repairId);
    }
}
