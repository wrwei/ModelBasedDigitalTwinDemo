package com.anadeem.dt_process_model.config;

import com.anadeem.dt_process_model.repair.RepairItem;
import com.anadeem.dt_process_model.logging.*;
import com.anadeem.dt_process_model.service.*;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class that defines all the delegate beans needed for BPMN processes
 */
@Configuration
public class WorkflowDelegateConfig {

    private final WorkflowManagerService workflowManagerService;
    private final RepairService repairService;
    private final RepairTaskService repairTaskService;
    private final RepairCompletionService repairCompletionService;
    private final DelayService delayService;
    private final WorkflowStatusUpdateService workflowStatusUpdateService;
    private final RepairLogService repairLogService;

    // Constructor injection
    public WorkflowDelegateConfig(
            RepairService repairService,
            RepairTaskService repairTaskService,
            RepairCompletionService repairCompletionService,
            DelayService delayService,
            WorkflowStatusUpdateService workflowStatusUpdateService,
            RepairLogService repairLogService,
            WorkflowManagerService workflowManagerService
    ) {
        this.repairService = repairService;
        this.repairTaskService = repairTaskService;
        this.repairCompletionService = repairCompletionService;
        this.delayService = delayService;
        this.workflowStatusUpdateService = workflowStatusUpdateService;
        this.repairLogService = repairLogService;
        this.workflowManagerService = workflowManagerService;
    }

    @Bean
    public JavaDelegate assessRepairBean() {
        return new JavaDelegate() {
            @Override
            public void execute(DelegateExecution execution) {
                String repairId = (String) execution.getVariable("repairId");

                // Log for humans
                repairLogService.logHumanEvent(repairId,
                        "Assess Repair: starting assessment for " + repairId);

                System.out.println("Assessing repair needs for: " + repairId);

                // Update workflow status at beginning of task
                workflowStatusUpdateService.updateWorkflowStatus(
                        repairId, "Assess Repair", "in-progress");

                // Add medium delay for this task
                delayService.addMediumDelay("Assess Repair", repairId);

                execution.setVariable("assessmentCompleted", true);

                // Mark completed
                workflowStatusUpdateService.updateWorkflowStatus(
                        repairId, "Assess Repair", "completed");

                // Log done
                repairLogService.logHumanEvent(repairId,
                        "Assess Repair: completed for " + repairId);
            }
        };
    }

    @Bean
    public JavaDelegate dispatchRobotBean() {
        return new JavaDelegate() {
            @Override
            public void execute(DelegateExecution execution) {
                String originalId = (String) execution.getVariable("repairId");
                System.out.println("Dispatching robot for repair: " + originalId);

                // Log for humans
                repairLogService.logHumanEvent(originalId,
                        "Dispatch Robot: requesting robot dispatch for " + originalId);

                String repairId = originalId;
                if (repairId.startsWith("Pavement.")) {
                    repairId = "R" + repairId.replace("Pavement.", "");
                }
                String numeric = repairId.replace("R", "");

                // Add medium delay
                delayService.addMediumDelay("Dispatch Robot", repairId);

                // Create repair item
                RepairItem repairItem = new RepairItem();
                repairItem.setRepairId(repairId);
                repairItem.setPriority(1);
                repairItem.setSurface("Pavement." + numeric);

                // Assign to robot
                String result = repairTaskService.assignToRobot(repairItem);
                System.out.println(result);

                // Add log line about the result
                repairLogService.logHumanEvent(originalId,
                        "Dispatch Robot: " + result);

                execution.setVariable("robotDispatched", true);
            }
        };
    }

    @Bean
    public JavaDelegate monitorProgressBean() {
        return new JavaDelegate() {
            private final Map<String, Integer> monitorCounts = new HashMap<>();

            @Override
            public void execute(DelegateExecution execution) {
                String repairId = (String) execution.getVariable("repairId");
                System.out.println("Monitoring progress for repair: " + repairId);
                repairLogService.logHumanEvent(repairId,
                        "Monitor Progress: checking repair " + repairId);

                workflowStatusUpdateService.updateWorkflowStatus(
                        repairId, "Monitor Progress", "in-progress");

                // Add long delay
                delayService.addLongDelay("Monitor Progress", repairId);

                // Count how many times we have monitored
                int count = monitorCounts.getOrDefault(repairId, 0);
                monitorCounts.put(repairId, count + 1);

                String status;
                if (count >= 3) {
                    status = "completed";
                    System.out.println("Repair " + repairId
                            + " is now complete after " + count + " monitoring iterations");
                    monitorCounts.remove(repairId);
                    repairLogService.logHumanEvent(repairId,
                            "Monitor Progress: repair finished after " + count + " checks");
                } else {
                    status = "in-progress";
                    System.out.println("Repair " + repairId
                            + " still in progress (iteration " + count + ")");
                    repairLogService.logHumanEvent(repairId,
                            "Monitor Progress: still in progress (iteration " + count + ")");
                }

                execution.setVariable("status", status);
                workflowStatusUpdateService.updateWorkflowStatus(
                        repairId, "Monitor Progress", status);
            }
        };
    }

    @Bean
    public JavaDelegate verifyRepairBean() {
        return new JavaDelegate() {
            @Override
            public void execute(DelegateExecution execution) {
                String repairId = (String) execution.getVariable("repairId");
                System.out.println("Verifying repair quality for: " + repairId);

                repairLogService.logHumanEvent(repairId,
                        "Verify Repair: verifying repair " + repairId);

                delayService.addMediumDelay("Verify Repair");

                // Set variable so BPMN can see it's verified
                execution.setVariable("repairVerified", true);

                repairLogService.logHumanEvent(repairId,
                        "Verify Repair: done for " + repairId);
            }
        };
    }

    @Bean
    public JavaDelegate closeRepairBean() {
        return new JavaDelegate() {
            @Override
            public void execute(DelegateExecution execution) {
                String repairId = (String) execution.getVariable("repairId");
                System.out.println("Closing repair task: " + repairId);

                repairLogService.logHumanEvent(repairId,
                        "Close Repair: finalising repair " + repairId);

                delayService.addShortDelay("Close Repair");

                // Extract model and item names from repairId
                String modelName = (String) execution.getVariable("modelName");
                String itemName = (String) execution.getVariable("itemName");
                String numericPart = "";

                // If modelName is null, try to derive it from repairId
                if (modelName == null && repairId != null) {
                    modelName = "Interchange1";

                    // Handle repair ID in format "Rxxx"
                    if (repairId.startsWith("R") && repairId.length() > 1) {
                        numericPart = repairId.substring(1); // Extract the numeric portion
                        itemName = "Pavement." + numericPart;

                        repairLogService.logHumanEvent(repairId,
                                "Close Repair: set modelName to '" + modelName + "' and itemName to '" +
                                        itemName + "' for repairId " + repairId);
                    } else {
                        itemName = "Pavement." + repairId;
                        repairLogService.logHumanEvent(repairId,
                                "Close Repair: set modelName to '" + modelName + "' and itemName to '" +
                                        itemName + "' for repairId " + repairId);
                    }
                }

                try {
                    repairService.removeDefect(modelName, numericPart);
                    repairLogService.logHumanEvent(repairId,
                            "Close Repair: removed defect from " + modelName);

                    // Update top material layer
                    // Generate random quantities within specified ranges
                    double bitumenQuantity = 110 + Math.random() * 20; // Random between 110-130
                    double fineAggregatesQuantity = 1800 + Math.random() * 400; // Random between 1800-2200

                    // Round to 2 decimal places
                    bitumenQuantity = Math.round(bitumenQuantity * 100.0) / 100.0;
                    fineAggregatesQuantity = Math.round(fineAggregatesQuantity * 100.0) / 100.0;

                    // Create the request body
                    String materialsJson = String.format(
                            "{\"materials\":[{\"type\":\"Bitumen\",\"quantity\":%.2f},{\"type\":\"FineAggregates\",\"quantity\":%.2f}]}",
                            bitumenQuantity, fineAggregatesQuantity
                    );

                    // Call API to update top material
                    if (modelName != null && itemName != null) {
                        // Always try to remove defects regardless of repair type
                        // Extract the numeric part from itemName
                        String pavementId = itemName;
                        if (itemName.contains(".")) {
                            // Make sure we get only the numeric portion after the last dot
                            pavementId = itemName.substring(itemName.lastIndexOf(".") + 1);
                        }

                        try {
                            // Log what we're trying to remove
                            repairLogService.logHumanEvent(repairId,
                                    "Close Repair: attempting to remove defect D" + pavementId + " from " + modelName);

                            repairService.removeDefect(modelName, pavementId);

                            repairLogService.logHumanEvent(repairId,
                                    "Close Repair: successfully removed defect D" + pavementId + " from " + modelName);
                        } catch (Exception e) {
                            // Log the error
                            String errorMsg = "Failed to remove defect: " + e.getMessage();
                            System.err.println(errorMsg);
                            repairLogService.logHumanEvent(repairId, "Close Repair: ERROR - " + errorMsg);
                        }

                        try {
                            repairService.setTopMaterial(modelName, itemName, materialsJson);
                            repairLogService.logHumanEvent(repairId,
                                    "Close Repair: updated top material layer for " + modelName + "/" + itemName);
                        } catch (Exception e) {
                            // Log the error
                            String errorMsg = "Failed to update materials: " + e.getMessage();
                            System.err.println(errorMsg);
                            repairLogService.logHumanEvent(repairId, "Close Repair: ERROR - " + errorMsg);
                        }
                    } else {
                        // Log the error
                        String errorMsg = "Unable to update top material layer - missing modelName or itemName";
                        System.err.println(errorMsg);
                        repairLogService.logHumanEvent(repairId, "Close Repair: ERROR - " + errorMsg);
                    }

                    repairCompletionService.completeRepair(repairId);

                    repairLogService.logHumanEvent(repairId,
                            "Close Repair: marked " + repairId + " completed.");
                } catch (Exception e) {
                    // Log error
                    System.err.println("Error during repair closing: " + e.getMessage());
                    repairLogService.logHumanEvent(repairId,
                            "Close Repair: ERROR - failed to complete repairs: " + e.getMessage());
                    throw new RuntimeException("Failed to complete repair", e);
                }
            }
        };
    }

    // Express repair delegates

    @Bean
    public JavaDelegate quickAssessBean() {
        return new JavaDelegate() {
            @Override
            public void execute(DelegateExecution execution) {
                String repairId = (String) execution.getVariable("repairId");
                System.out.println("Quick assessment for express repair: " + repairId);

                repairLogService.logHumanEvent(repairId,
                        "QuickAssess: performing quick check for " + repairId);

                delayService.addShortDelay("Quick Assessment");

                execution.setVariable("quickAssessmentDone", true);

                repairLogService.logHumanEvent(repairId,
                        "QuickAssess: done for " + repairId);
            }
        };
    }

    @Bean
    public JavaDelegate expressDispatchBean() {
        return new JavaDelegate() {
            @Override
            public void execute(DelegateExecution execution) {
                String originalId = (String) execution.getVariable("repairId");
                System.out.println("Express dispatch for repair: " + originalId);

                repairLogService.logHumanEvent(originalId,
                        "ExpressDispatch: dispatching express robot for " + originalId);

                String repairId = originalId;
                if (repairId.startsWith("Pavement.")) {
                    repairId = "R" + repairId.replace("Pavement.", "");
                }
                String numeric = repairId.replace("R", "");

                delayService.addShortDelay("Express Dispatch");

                RepairItem repairItem = new RepairItem();
                repairItem.setRepairId(repairId);
                repairItem.setPriority(5);
                repairItem.setSurface("Pavement." + numeric);

                String result = repairTaskService.assignToRobot(repairItem);
                System.out.println(result);

                repairLogService.logHumanEvent(originalId,
                        "ExpressDispatch: " + result);
            }
        };
    }

    @Bean
    public JavaDelegate trackRepairBean() {
        return new JavaDelegate() {
            private final Map<String, Integer> trackCounts = new HashMap<>();

            @Override
            public void execute(DelegateExecution execution) {
                String repairId = (String) execution.getVariable("repairId");
                System.out.println("Tracking express repair: " + repairId);

                repairLogService.logHumanEvent(repairId,
                        "TrackRepair: tracking express repair " + repairId);

                delayService.addMediumDelay("Track Repair");

                int count = trackCounts.getOrDefault(repairId, 0);
                trackCounts.put(repairId, count + 1);

                String status;
                if (count >= 2) {
                    status = "completed";
                    System.out.println("Express repair " + repairId
                            + " is now complete after " + count + " tracking iterations");
                    repairLogService.logHumanEvent(repairId,
                            "TrackRepair: express done after " + count + " checks");
                    trackCounts.remove(repairId);
                } else {
                    status = "in-progress";
                    System.out.println("Express repair " + repairId
                            + " still in progress (iteration " + count + ")");
                    repairLogService.logHumanEvent(repairId,
                            "TrackRepair: still in progress (iteration " + count + ")");
                }
                execution.setVariable("status", status);
            }
        };
    }

    @Bean
    public JavaDelegate escalateRepairBean() {
        return new JavaDelegate() {
            @Override
            public void execute(DelegateExecution execution) {
                String repairId = (String) execution.getVariable("repairId");
                System.out.println("Escalating express repair to standard: " + repairId);

                repairLogService.logHumanEvent(repairId,
                        "EscalateRepair: converting express to standard for " + repairId);

                delayService.addShortDelay("Escalate Repair");

                String result = workflowManagerService.migrateWorkflow(repairId, WorkflowManagerService.WorkflowType.STANDARD);
                System.out.println("Migration result: " + result);

            }
        };
    }

    @Bean
    public JavaDelegate confirmCompletionBean() {
        return new JavaDelegate() {
            @Override
            public void execute(DelegateExecution execution) {
                String repairId = (String) execution.getVariable("repairId");
                System.out.println("Confirming completion of express repair: " + repairId);

                repairLogService.logHumanEvent(repairId,
                        "ConfirmCompletion: final check for express repair " + repairId);

                delayService.addShortDelay("Confirm Completion");

                repairCompletionService.completeRepair(repairId);

                repairLogService.logHumanEvent(repairId,
                        "ConfirmCompletion: repair " + repairId + " is done.");
            }
        };
    }

    // Emergency repair delegates

    @Bean
    public JavaDelegate emergencyAssessBean() {
        return new JavaDelegate() {
            @Override
            public void execute(DelegateExecution execution) {
                String repairId = (String) execution.getVariable("repairId");
                System.out.println("Emergency assessment for repair: " + repairId);

                repairLogService.logHumanEvent(repairId,
                        "EmergencyAssess: urgent check for " + repairId);

                delayService.addShortDelay("Emergency Assessment");

                execution.setVariable("emergencyAssessmentDone", true);

                repairLogService.logHumanEvent(repairId,
                        "EmergencyAssess: done for " + repairId);
            }
        };
    }

    @Bean
    public JavaDelegate emergencyDispatchBean() {
        return new JavaDelegate() {
            @Override
            public void execute(DelegateExecution execution) {
                String originalId = (String) execution.getVariable("repairId");
                System.out.println("Emergency team dispatch for repair: " + originalId);

                repairLogService.logHumanEvent(originalId,
                        "EmergencyDispatch: sending high-priority team for " + originalId);

                String repairId = originalId;
                if (repairId.startsWith("Pavement.")) {
                    repairId = "R" + repairId.replace("Pavement.", "");
                }
                String numeric = repairId.replace("R", "");

                delayService.addShortDelay("Emergency Dispatch");

                RepairItem repairItem = new RepairItem();
                repairItem.setRepairId(repairId);
                repairItem.setPriority(10);
                repairItem.setSurface("Pavement." + numeric);

                String result = repairTaskService.assignToRobot(repairItem);
                System.out.println(result);

                repairLogService.logHumanEvent(originalId,
                        "EmergencyDispatch: " + result);
            }
        };
    }

    @Bean
    public JavaDelegate notifyAuthoritiesBean() {
        return new JavaDelegate() {
            @Override
            public void execute(DelegateExecution execution) {
                String repairId = (String) execution.getVariable("repairId");
                System.out.println("Notifying authorities about emergency repair: " + repairId);

                repairLogService.logHumanEvent(repairId,
                        "NotifyAuthorities: local authorities informed for " + repairId);

                delayService.addShortDelay("Notify Authorities");
                execution.setVariable("authoritiesNotified", true);
            }
        };
    }

    @Bean
    public JavaDelegate trafficDiversionBean() {
        return new JavaDelegate() {
            @Override
            public void execute(DelegateExecution execution) {
                String repairId = (String) execution.getVariable("repairId");
                System.out.println("Setting up traffic diversion for emergency repair: " + repairId);

                repairLogService.logHumanEvent(repairId,
                        "TrafficDiversion: preparing traffic detour for " + repairId);

                delayService.addMediumDelay("Traffic Diversion");

                execution.setVariable("trafficDiversionSetup", true);
            }
        };
    }

    @Bean
    public JavaDelegate monitorEmergencyBean() {
        return new JavaDelegate() {
            private final Map<String, Integer> emergencyMonitorCounts = new HashMap<>();

            @Override
            public void execute(DelegateExecution execution) {
                String repairId = (String) execution.getVariable("repairId");
                System.out.println("Monitoring emergency repair: " + repairId);

                repairLogService.logHumanEvent(repairId,
                        "MonitorEmergency: checking critical repair " + repairId);

                delayService.addMediumDelay("Monitor Emergency");

                int count = emergencyMonitorCounts.getOrDefault(repairId, 0);
                emergencyMonitorCounts.put(repairId, count + 1);

                String status;
                if (count >= 4) {
                    status = "completed";
                    System.out.println("Emergency repair " + repairId
                            + " is now complete after " + count + " monitoring iterations");
                    emergencyMonitorCounts.remove(repairId);

                    repairLogService.logHumanEvent(repairId,
                            "MonitorEmergency: repair done after " + count + " checks");
                } else if (count == 2) {
                    status = "delayed";
                    System.out.println("Emergency repair " + repairId
                            + " is delayed - needs human attention");

                    repairLogService.logHumanEvent(repairId,
                            "MonitorEmergency: delayed, needs intervention");
                } else {
                    status = "in-progress";
                    System.out.println("Emergency repair " + repairId
                            + " still in progress (iteration " + count + ")");

                    repairLogService.logHumanEvent(repairId,
                            "MonitorEmergency: still in progress (iteration " + count + ")");
                }

                execution.setVariable("status", status);
            }
        };
    }

    @Bean
    public JavaDelegate escalateEmergencyBean() {
        return new JavaDelegate() {
            @Override
            public void execute(DelegateExecution execution) {
                String repairId = (String) execution.getVariable("repairId");
                System.out.println("Escalating emergency level for repair: " + repairId);

                repairLogService.logHumanEvent(repairId,
                        "EscalateEmergency: intensifying emergency response for " + repairId);

                delayService.addShortDelay("Escalate Emergency");
                execution.setVariable("emergencyEscalated", true);
            }
        };
    }

    @Bean
    public JavaDelegate verifyEmergencyBean() {
        return new JavaDelegate() {
            @Override
            public void execute(DelegateExecution execution) {
                String repairId = (String) execution.getVariable("repairId");
                System.out.println("Verifying emergency repair: " + repairId);

                repairLogService.logHumanEvent(repairId,
                        "VerifyEmergency: checking final quality for " + repairId);

                delayService.addMediumDelay("Verify Emergency Repair");
                execution.setVariable("emergencyRepairVerified", true);
            }
        };
    }

    @Bean
    public JavaDelegate restoreTrafficBean() {
        return new JavaDelegate() {
            @Override
            public void execute(DelegateExecution execution) {
                String repairId = (String) execution.getVariable("repairId");
                System.out.println("Restoring normal traffic flow after emergency repair: " + repairId);

                repairLogService.logHumanEvent(repairId,
                        "RestoreTraffic: traffic returning to normal for " + repairId);

                delayService.addMediumDelay("Restore Traffic");
                execution.setVariable("trafficRestored", true);
            }
        };
    }

    @Bean
    public JavaDelegate closeEmergencyRepairBean() {
        return new JavaDelegate() {
            @Override
            public void execute(DelegateExecution execution) {
                String repairId = (String) execution.getVariable("repairId");
                System.out.println("Closing emergency repair task: " + repairId);

                repairLogService.logHumanEvent(repairId,
                        "CloseEmergencyRepair: finalizing emergency repair " + repairId);

                delayService.addShortDelay("Close Emergency Repair");

                repairCompletionService.completeRepair(repairId);

                repairLogService.logHumanEvent(repairId,
                        "CloseEmergencyRepair: marked " + repairId + " completed.");

                execution.setVariable("emergencyRepairClosed", true);
            }
        };
    }
}
