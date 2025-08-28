package com.anadeem.dt_process_model.service;

import com.anadeem.dt_process_model.repair.RepairItem;
import com.anadeem.dt_process_model.repair.RepairHistoryManager;
import com.anadeem.dt_process_model.model.PavementWearRanking;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
public class RepairService {

    private final DefectProcessingService defectProcessingService;

    // Cache of repair items
    private final Map<String, RepairItem> repairItemCache = new ConcurrentHashMap<>();

    // Random seed to ensure consistent selection of medium/low wear repairs
    private final long randomSeed = 12345L;

    // Flag indicating if initial data has been loaded
    private boolean initialDataLoaded = false;

    private final RestTemplate restTemplate;

    public RepairService(DefectProcessingService defectProcessingService,
                         RestTemplate restTemplate) {
        this.defectProcessingService = defectProcessingService;
        this.restTemplate = restTemplate;
    }

    /**
     * Get a prioritised list of repairs based on pavement wear rankings
     *
     * @return List of RepairItems sorted by priority
     */
    public List<RepairItem> getPrioritisedRepairs() {
        if (!initialDataLoaded) {
            loadInitialRepairData();
            initialDataLoaded = true;
        }

        // Get all active repair items from cache
        List<RepairItem> activeRepairs = repairItemCache.values().stream()
                .filter(item -> !"Completed".equals(item.getStatus()))
                .collect(Collectors.toList());

        // Sort by priority
        activeRepairs.sort(Comparator.comparing(RepairItem::getPriority).reversed());

        return activeRepairs;
    }

    /**
     * Identifies all repairs from pavement data
     */
    private void loadInitialRepairData() {
        System.out.println("Loading initial repair data...");

        Random seededRandom = new Random(randomSeed);

        // Get the pavement wear ranking data
        List<PavementWearRanking> pavementRankings = defectProcessingService.rankPavements("Interchange1");

        // Create repair items from pavement rankings
        for (PavementWearRanking ranking : pavementRankings) {
            // Only create repair items for pavements that need attention
            if (isRepairNeeded(ranking, seededRandom)) {
                String repairId = "R" + ranking.getPavementId().replace("Pavement.", "");

                RepairItem item = new RepairItem();
                item.setRepairId(repairId);
                item.setSurface(ranking.getPavementId());

                // Set severity based on wear score
                int severity = calculateSeverity(ranking.getWearScore(), "defect".equals(ranking.getRank()));
                item.setSeverity(severity);

                // Calculate priority based on severity
                int priority = calculatePriority(
                        severity,
                        "defect".equals(ranking.getRank()),
                        ranking.getTrafficLoad()
                );
                item.setPriority(priority);

                // Set type of defect
                item.setDefectType("defect".equals(ranking.getRank()) ? "Potholes" : "Wear");

                // Set initial status
                item.setStatus("Identified");

                // Add to cache
                repairItemCache.put(repairId, item);

                System.out.println("Created repair item: " + item);
            }
        }

        System.out.println("Loaded " + repairItemCache.size() + " initial repair items");
    }

    /**
     * Determine if a pavement needs repair based on its ranking and wear score
     * Uses seeded random to ensure consistent selection across runs
     */
    private boolean isRepairNeeded(PavementWearRanking ranking, Random seededRandom) {
        // Always repair defects
        if ("defect".equals(ranking.getRank())) {
            return true;
        }

        // Repair high/severe wear
        if ("high".equals(ranking.getRank()) || "severe".equals(ranking.getRank())) {
            return true;
        }

        // Repair medium wear with high probability
        if ("medium".equals(ranking.getRank()) && seededRandom.nextDouble() < 0.7) {
            return true;
        }

        // Repair low wear with low probability
        if ("low".equals(ranking.getRank()) && seededRandom.nextDouble() < 0.3) {
            return true;
        }

        // Don't repair normal wear
        return false;
    }

    /**
     * Calculate severity level (1-5) based on wear score and defect presence
     */
    private int calculateSeverity(double wearScore, boolean hasDefect) {
        if (hasDefect) {
            return 5; // Maximum severity for defects
        }

        // Convert wear score (0-1) to severity (1-5)
        return Math.min(5, Math.max(1, (int)(wearScore * 5) + 1));
    }

    /**
     * Calculate priority based on severity, defects, and traffic load
     *
     * @param severity Defect severity (1-5)
     * @param hasDefect Whether there's a pothole defect
     * @param trafficLoad Traffic volume
     * @return Priority value (higher is more urgent)
     */
    private int calculatePriority(int severity, boolean hasDefect, double trafficLoad) {
        int priority = severity * 2;

        if (hasDefect) {
            priority += 5; // Significant boost for defects
        }

        // Add traffic factor (0-3)
        int trafficFactor = (int)(Math.min(3, trafficLoad / 300));
        priority += trafficFactor;

        return priority;
    }

    /**
     * Get a repair item by ID
     *
     * @param repairId The repair ID to find
     * @return RepairItem if found, null otherwise
     */
    public RepairItem getRepairById(String repairId) {
        // First check the cache
        RepairItem cachedItem = repairItemCache.get(repairId);
        if (cachedItem != null) {
            return cachedItem;
        }

        // If not in cache, create a item
        RepairItem item = new RepairItem();
        item.setRepairId(repairId);
        item.setSurface("Pavement." + repairId.replace("R", ""));
        item.setSeverity(3);
        item.setPriority(6);
        item.setDefectType("Unknown");
        item.setStatus("Identified");

        // Add to cache for future stability
        repairItemCache.put(repairId, item);

        return item;
    }

    /**
     * Mark a repair as completed
     *
     * @param repairId The repair ID to mark as completed
     */
    public void markRepairAsCompleted(String repairId) {
        RepairItem item = getRepairById(repairId);
        item.setStatus("Completed");

        // Update in cache
        repairItemCache.put(repairId, item);

        System.out.println("Marked repair " + repairId + " as completed");
    }

    /**
     * Update the status of a repair
     *
     * @param repairId The repair ID
     * @param status The new status
     */
    public void updateRepairStatus(String repairId, String status) {
        RepairItem item = getRepairById(repairId);
        item.setStatus(status);

        // Update in cache
        repairItemCache.put(repairId, item);

        System.out.println("Updated repair " + repairId + " status to: " + status);
    }

    /**
     * Method to remove a defect from a model by calling the external API
     * @param modelName The name of the model from which to remove the defect
     * @param pavementId The pavement ID to form the defect name
     */
    public void removeDefect(String modelName, String pavementId) {
        // Call the API to remove the defect
        String apiUrl = "http://localhost:7070/removeDefect/" + modelName;

        // Format the defect name as D + pavementId
        String defectName = "D" + pavementId;

        // Create the JSON string
        String requestBodyJson = "{\"defectName\":\"" + defectName + "\"}";

        try {
            long requestTime = System.currentTimeMillis();

            // Using RestTemplate with HttpEntity to specify content type as application/json
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            org.springframework.http.HttpEntity<String> request =
                    new org.springframework.http.HttpEntity<>(requestBodyJson, headers);

            // Post the raw JSON string
            org.springframework.http.ResponseEntity<String> response =
                    restTemplate.postForEntity(apiUrl, request, String.class);

            // Log response details
            System.out.println("[" + requestTime + "] Remove defect response status: " + response.getStatusCode());
            System.out.println("[" + requestTime + "] Remove defect response body: " + response.getBody());
            System.out.println("[" + requestTime + "] Successfully removed defect " + defectName + " from model: " + modelName);
        } catch (Exception e) {
            System.err.println("Failed to remove defect " + defectName + " from model " + modelName + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to remove defect via API", e);
        }
    }

    /**
     * Method to update the top material of an item in a model
     * @param modelName The name of the model
     * @param itemName The name of the item within the model
     * @param jsonBody The JSON string
     */
    public void setTopMaterial(String modelName, String itemName, String jsonBody) {
        // Call the API to set the top material
        String apiUrl = "http://localhost:7070/setTopMaterial/" + modelName + "/" + itemName;

        try {
            long requestTime = System.currentTimeMillis();

            // Using RestTemplate with HttpEntity to specify content type as application/json
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            org.springframework.http.HttpEntity<String> request =
                    new org.springframework.http.HttpEntity<>(jsonBody, headers);

            // Post the raw JSON string
            restTemplate.postForEntity(apiUrl, request, String.class);

            System.out.println("[" + requestTime + "] Successfully updated top material for " + modelName + "/" + itemName);
        } catch (Exception e) {
            System.err.println("Failed to update top material for " + modelName + "/" + itemName +
                    ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update top material via API", e);
        }
    }
}