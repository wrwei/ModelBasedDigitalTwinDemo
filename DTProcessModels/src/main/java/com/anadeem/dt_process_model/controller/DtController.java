package com.anadeem.dt_process_model.controller;

import com.anadeem.dt_process_model.repair.*;
import com.anadeem.dt_process_model.robot.*;
import com.anadeem.dt_process_model.service.*;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;
import com.anadeem.dt_process_model.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/process")
public class DtController {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final DtApiService dtApiService;
    private final RepairService repairService;
    private final DefectProcessingService defectProcessingService;
    private final RepairTaskService repairTaskService;
    private final RepairCompletionService repairCompletionService;

    public DtController(RuntimeService runtimeService, TaskService taskService, DtApiService dtApiService,
                        DefectProcessingService defectProcessingService, RepairService repairService,
                        RepairTaskService repairTaskService,
                        RepairCompletionService repairCompletionService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.dtApiService = dtApiService;
        this.defectProcessingService = defectProcessingService;
        this.repairService = repairService;
        this.repairTaskService = repairTaskService;
        this.repairCompletionService = repairCompletionService;
    }

    @GetMapping(path = "/defects/{modelName}")
    public DefectResponse getDefects(@PathVariable String modelName) {
        return dtApiService.getDefects(modelName);
    }

    @GetMapping(path = "/wearRanking/{modelName}")
    public List<PavementWearRanking> getWearRanking(@PathVariable String modelName) {
        return defectProcessingService.rankPavements(modelName);
    }

    @GetMapping("/repairs-list")
    public List<RepairItem> getRepairsList() {
        return repairService.getPrioritisedRepairs();
    }

    @GetMapping("/report")
    public RepairReport getRepairReport(@RequestParam String repairId) {
        return repairCompletionService.getRepairReport(repairId);
    }
}