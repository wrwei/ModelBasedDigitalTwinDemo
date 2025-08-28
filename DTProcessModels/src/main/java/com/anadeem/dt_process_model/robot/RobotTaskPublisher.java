package com.anadeem.dt_process_model.robot;

import com.anadeem.dt_process_model.repair.RepairItem;
import com.anadeem.dt_process_model.logging.RepairLogService;
import org.springframework.stereotype.Component;

@Component
public class RobotTaskPublisher {

    private final RepairLogService repairLogService;

    public RobotTaskPublisher(RepairLogService repairLogService) {
        this.repairLogService = repairLogService;
    }

    /**
     * Publish repair task to ROS
     */
    public void publishToRos(RepairItem repairItem) {
        // Log the ROS command
        repairLogService.logRobotEvent(repairItem.getRepairId(),
                "Publishing to ROS: /maintenance_task with item " + repairItem);

        System.out.println("Publishing to ROS: " + repairItem);
        System.out.println("Topic: /maintenance_task");
        System.out.println("Successfully published task: " + repairItem.getRepairId() +
                " for surface: " + repairItem.getSurface());
    }
}
