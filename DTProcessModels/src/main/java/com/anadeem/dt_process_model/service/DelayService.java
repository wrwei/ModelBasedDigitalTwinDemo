package com.anadeem.dt_process_model.service;

import org.springframework.stereotype.Service;
import com.anadeem.dt_process_model.config.BpmnControlConfig.ControlMode;
import com.anadeem.dt_process_model.config.BpmnControlConfig.ControlProperties;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to add configurable delays to workflow tasks for demo
 */
@Service
public class DelayService {

    private final Random random = new Random();
    private final ControlProperties controlProperties;

    private static final int MIN_SHORT_DELAY = 2000;  // 2 seconds
    private static final int MAX_SHORT_DELAY = 5000;  // 5 seconds

    private static final int MIN_MEDIUM_DELAY = 5000;  // 5 seconds
    private static final int MAX_MEDIUM_DELAY = 10000; // 10 seconds

    private static final int MIN_LONG_DELAY = 10000;  // 10 seconds
    private static final int MAX_LONG_DELAY = 20000;  // 20 seconds

    // For robot control mode
    private final Map<String, Lock> repairLocks = new HashMap<>();
    private final Map<String, Condition> repairConditions = new HashMap<>();
    private final Map<String, Boolean> repairFeedbackReceived = new HashMap<>();

    private boolean delaysEnabled = true;

    public DelayService() {
        this.controlProperties = null;
    }

    public DelayService(ControlProperties controlProperties) {
        this.controlProperties = controlProperties;
    }

    /**
     * Add short delay (2-5 seconds)
     * @param taskName Name of the task
     */
    public void addShortDelay(String taskName) {
        if (controlProperties == null) {
            if (delaysEnabled) {
                addDelay(taskName, MIN_SHORT_DELAY, MAX_SHORT_DELAY);
            }
            return;
        }
        addShortDelay(taskName, null);
    }

    /**
     * Add short delay (2-5 seconds) with repairId
     * @param taskName Name of the task
     * @param repairId The repair ID
     */
    public void addShortDelay(String taskName, String repairId) {
        if (controlProperties == null) {
            if (delaysEnabled) {
                addDelay(taskName, MIN_SHORT_DELAY, MAX_SHORT_DELAY);
            }
            return;
        }

        // Check control mode
        if (controlProperties.getMode() == ControlMode.AUTO) {
            // No delay in automatic mode
            System.out.println("Skipping delay for task: " + taskName + " (AUTO mode)");
            return;
        } else if (controlProperties.getMode() == ControlMode.DELAYED) {
            // Add the configured delay
            addDelay(taskName, MIN_SHORT_DELAY, MAX_SHORT_DELAY);
        }
    }

    /**
     * Add medium delay (5-10 seconds)
     * @param taskName Name of the task
     */
    public void addMediumDelay(String taskName) {
        if (controlProperties == null) {
            if (delaysEnabled) {
                addDelay(taskName, MIN_MEDIUM_DELAY, MAX_MEDIUM_DELAY);
            }
            return;
        }
        addMediumDelay(taskName, null);
    }

    /**
     * Add medium delay (5-10 seconds) with repairId
     * @param taskName Name of the task
     * @param repairId The repair ID
     */
    public void addMediumDelay(String taskName, String repairId) {
        if (controlProperties == null) {
            if (delaysEnabled) {
                addDelay(taskName, MIN_MEDIUM_DELAY, MAX_MEDIUM_DELAY);
            }
            return;
        }

        // Check control mode
        if (controlProperties.getMode() == ControlMode.AUTO) {
            // No delay in automatic mode
            System.out.println("Skipping delay for task: " + taskName + " (AUTO mode)");
            return;
        } else if (controlProperties.getMode() == ControlMode.DELAYED) {
            // Add the configured delay
            addDelay(taskName, MIN_MEDIUM_DELAY, MAX_MEDIUM_DELAY);
        }
    }

    /**
     * Add long delay (10-20 seconds)
     * @param taskName Name of the task
     */
    public void addLongDelay(String taskName) {
        if (controlProperties == null) {
            if (delaysEnabled) {
                addDelay(taskName, MIN_LONG_DELAY, MAX_LONG_DELAY);
            }
            return;
        }

        addLongDelay(taskName, null);
    }

    /**
     * Add long delay (10-20 seconds) with repairId
     * @param taskName Name of the task
     * @param repairId The repair ID
     */
    public void addLongDelay(String taskName, String repairId) {
        if (controlProperties == null) {
            if (delaysEnabled) {
                addDelay(taskName, MIN_LONG_DELAY, MAX_LONG_DELAY);
            }
            return;
        }

        // Check control mode
        if (controlProperties.getMode() == ControlMode.AUTO) {
            // No delay in automatic mode
            System.out.println("Skipping delay for task: " + taskName + " (AUTO mode)");
            return;
        } else if (controlProperties.getMode() == ControlMode.DELAYED) {
            // Add the configured delay
            addDelay(taskName, MIN_LONG_DELAY, MAX_LONG_DELAY);
        }
    }


    /**
     * Helper method to perform the delay
     */
    private void addDelay(String taskName, int minMs, int maxMs) {
        int delayMs = minMs + random.nextInt(maxMs - minMs + 1);

        System.out.println("Adding " + (delayMs / 1000.0) + " second delay to: " + taskName);

        try {
            TimeUnit.MILLISECONDS.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Delay interrupted for task: " + taskName);
        }

        System.out.println("Delay completed for: " + taskName);
    }


    /**
     * Enable or disable delays
     * @param enabled true to enable, false to disable
     */
    public void setDelaysEnabled(boolean enabled) {
        this.delaysEnabled = enabled;
    }

}