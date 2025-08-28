package com.anadeem.dt_process_model.controller;

import com.anadeem.dt_process_model.service.WorkflowManagerService;
import com.anadeem.dt_process_model.service.WorkflowManagerService.WorkflowType;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    private final WorkflowManagerService workflowManagerService;
    private final TaskService taskService;

    public WorkflowController(WorkflowManagerService workflowManagerService, TaskService taskService) {
        this.workflowManagerService = workflowManagerService;
        this.taskService = taskService;
    }

    @PostMapping("/start/{type}")
    public Map<String, String> startWorkflow(
            @PathVariable String type,
            @RequestParam String repairId) {

        WorkflowType workflowType;
        try {
            workflowType = WorkflowType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid workflow type. Must be one of: STANDARD, EXPRESS, EMERGENCY");
            return error;
        }

        String result = workflowManagerService.startRepairWorkflow(repairId, workflowType);

        System.out.println(result);

        Map<String, String> response = new HashMap<>();
        response.put("result", result);
        return response;
    }

    @GetMapping("/details/{repairId}")
    public Map<String, Object> getWorkflowDetails(@PathVariable String repairId) {
        return workflowManagerService.getWorkflowDetails(repairId);
    }

    @PostMapping("/migrate")
    public Map<String, String> migrateWorkflow(
            @RequestParam String repairId,
            @RequestParam String newType) {

        WorkflowType workflowType;
        try {
            workflowType = WorkflowType.valueOf(newType.toUpperCase());
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid workflow type. Must be one of: STANDARD, EXPRESS, EMERGENCY");
            return error;
        }

        String result = workflowManagerService.migrateWorkflow(repairId, workflowType);

        Map<String, String> response = new HashMap<>();
        response.put("result", result);
        return response;
    }

    @GetMapping("/tasks/{processInstanceId}")
    public List<Map<String, Object>> getWorkflowTasks(@PathVariable String processInstanceId) {
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> taskInfo = new HashMap<>();
            taskInfo.put("id", task.getId());
            taskInfo.put("name", task.getName());
            taskInfo.put("assignee", task.getAssignee());
            taskInfo.put("createTime", task.getCreateTime());
            taskInfo.put("priority", task.getPriority());
            result.add(taskInfo);
        }

        return result;
    }

    @GetMapping("/types")
    public List<Map<String, String>> getWorkflowTypes() {
        List<Map<String, String>> types = new ArrayList<>();

        for (WorkflowType type : WorkflowType.values()) {
            Map<String, String> typeInfo = new HashMap<>();
            typeInfo.put("id", type.name());
            typeInfo.put("name", type.name().charAt(0) + type.name().substring(1).toLowerCase());
            typeInfo.put("processKey", type.getProcessKey());
            types.add(typeInfo);
        }

        return types;
    }

    @PostMapping("/complete-task")
    public Map<String, String> completeTask(
            @RequestParam String taskId,
            @RequestParam(required = false) Map<String, Object> variables) {

        try {
            if (variables == null) {
                taskService.complete(taskId);
            } else {
                taskService.complete(taskId, variables);
            }

            Map<String, String> response = new HashMap<>();
            response.put("result", "Task completed successfully");
            return response;
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to complete task: " + e.getMessage());
            return error;
        }
    }
}