package com.anadeem.dt_process_model.controller;

import com.anadeem.dt_process_model.repair.RepairItem;
import com.anadeem.dt_process_model.service.RepairService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.anadeem.dt_process_model.service.DefectProcessingService;
import com.anadeem.dt_process_model.model.PavementWearRanking;
import com.anadeem.dt_process_model.service.WorkflowManagerService;
import com.anadeem.dt_process_model.service.WorkflowManagerService.WorkflowType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/portal")
public class PageController {

    private final DefectProcessingService defectProcessingService;
    private final WorkflowManagerService workflowManagerService;
    private final RepairService repairService;

    public PageController(
            DefectProcessingService defectProcessingService,
            WorkflowManagerService workflowManagerService,
            RepairService repairService
    ) {
        this.defectProcessingService = defectProcessingService;
        this.workflowManagerService = workflowManagerService;
        this.repairService = repairService;
    }

    @GetMapping
    public String homePage(Model model) {
        model.addAttribute("message", "Welcome to the Digital Twin Process Model, please go to /road-conditions or /repair-list");
        return "home";
    }

    @GetMapping("/road-conditions")
    public String roadConditions(Model model) {
        String modelName = "Interchange1";
        List<PavementWearRanking> rankings = defectProcessingService.rankPavements(modelName);
        model.addAttribute("roads", rankings);
        return "road-conditions";
    }

    @GetMapping("/repair-list")
    public String repairList(Model model) {
        // Get the list of repairs
        List<RepairItem> repairs = repairService.getPrioritisedRepairs();
        model.addAttribute("repairs", repairs);
        return "repair-list";
    }
}