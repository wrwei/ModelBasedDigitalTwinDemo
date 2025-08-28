package com.anadeem.dt_process_model.service;

import com.anadeem.dt_process_model.repair.RepairItem;
import com.anadeem.dt_process_model.robot.RobotTaskPublisher;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RepairTaskService {
    private final RobotTaskPublisher robotTaskPublisher;

    public RepairTaskService(RobotTaskPublisher robotTaskPublisher) {
        this.robotTaskPublisher = robotTaskPublisher;
    }

    public String assignToRobot(RepairItem repairItem) {
        robotTaskPublisher.publishToRos(repairItem);
        return "Task assigned to robot for repair: " + repairItem.getRepairId();
    }
}