package com.anadeem.dt_process_model.logging;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RepairLogService {

    private final Map<String, List<RepairLogEntry>> humanLogs = new ConcurrentHashMap<>();

    private final Map<String, List<RepairLogEntry>> robotLogs = new ConcurrentHashMap<>();

    public void logHumanEvent(String repairId, String message) {
        List<RepairLogEntry> list = humanLogs.computeIfAbsent(repairId, k -> new ArrayList<>());
        list.add(new RepairLogEntry("HUMAN", message));
    }

    public void logRobotEvent(String repairId, String message) {
        List<RepairLogEntry> list = robotLogs.computeIfAbsent(repairId, k -> new ArrayList<>());
        list.add(new RepairLogEntry("ROBOT", message));
    }

    public Map<String, List<RepairLogEntry>> getLogs(String repairId) {
        Map<String, List<RepairLogEntry>> result = new HashMap<>();
        result.put("humanLog",
                humanLogs.getOrDefault(repairId, Collections.emptyList()));
        result.put("robotLog",
                robotLogs.getOrDefault(repairId, Collections.emptyList()));
        return result;
    }
}
