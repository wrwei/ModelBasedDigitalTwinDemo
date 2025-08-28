package com.anadeem.dt_process_model.service;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkflowManagerService {

    private final RuntimeService runtimeService;

    public WorkflowManagerService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    /**
     * Enum for the different types of repair workflows
     */
    public enum WorkflowType {
        STANDARD("StandardRepairProcess"),
        EXPRESS("ExpressRepairProcess"),
        EMERGENCY("EmergencyRepairProcess");

        private final String processKey;

        WorkflowType(String processKey) {
            this.processKey = processKey;
        }

        public String getProcessKey() {
            return processKey;
        }
    }

    /**
     * Start repair process with the specified workflow type
     *
     * @param repairId The repair ID
     * @param workflowType The type of workflow to use
     * @return A string with process instance details
     */
    public String startRepairWorkflow(String repairId, WorkflowType workflowType) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("repairId", repairId);
        variables.put("workflowType", workflowType.name());

        String businessKey = "REPAIR-" + repairId;

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                workflowType.getProcessKey(),
                businessKey,
                variables
        );

        return String.format("Started %s with ID: %s and business key: %s",
                workflowType.name(),
                processInstance.getId(),
                businessKey);
    }

    /**
     * Get details of running workflow
     *
     * @param repairId The repair ID
     * @return Map with process details including workflow type
     */
    public Map<String, Object> getWorkflowDetails(String repairId) {
        Map<String, Object> result = new HashMap<>();

        // Get the process instance for this repair ID
        String businessKey = "REPAIR-" + repairId;

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey)
                .singleResult();

        if (processInstance == null) {
            List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                    .variableValueEquals("repairId", repairId)
                    .list();

            if (!instances.isEmpty()) {
                processInstance = instances.get(0);
            }
        }

        if (processInstance == null) {
            result.put("status", "Not found");
            result.put("message", "No workflow found for repair ID: " + repairId);
            return result;
        }

        // Get process definition details
        String processDefinitionId = processInstance.getProcessDefinitionId();
        String processKey = processDefinitionId.split(":")[0];

        // Determine workflow type from process key
        WorkflowType workflowType = null;
        for (WorkflowType type : WorkflowType.values()) {
            if (type.getProcessKey().equals(processKey)) {
                workflowType = type;
                break;
            }
        }

        result.put("processInstanceId", processInstance.getId());
        result.put("processDefinitionId", processDefinitionId);
        result.put("workflowType", workflowType != null ? workflowType.name() : "UNKNOWN");
        result.put("isActive", !processInstance.isEnded());
        result.put("isSuspended", processInstance.isSuspended());

        return result;
    }

    /**
     * Migrate repair workflow from one type to another
     *
     * @param repairId The repair ID
     * @param newWorkflowType The new workflow type to migrate to
     * @return A status message
     */
    public String migrateWorkflow(String repairId, WorkflowType newWorkflowType) {
        // Get current workflow details
        Map<String, Object> currentWorkflow = getWorkflowDetails(repairId);

        if ("Not found".equals(currentWorkflow.get("status"))) {
            return "No existing workflow found for repair ID: " + repairId;
        }

        String processInstanceId = (String) currentWorkflow.get("processInstanceId");

        // Delete the old process and start a new one
        runtimeService.deleteProcessInstance(processInstanceId, "Migration to " + newWorkflowType.name());

        // Start the new workflow
        return startRepairWorkflow(repairId, newWorkflowType);
    }
}