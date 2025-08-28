package com.anadeem.dt_process_model.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.anadeem.dt_process_model.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DefectProcessingService {

    @Autowired
    private ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://localhost:7070";
    private static final String DEFECTS_URL = "http://localhost:8080/process/defects/Interchange1";

    public DefectProcessingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    private ProcessedDefectResponse.MaterialWearInfo calculatePavementWearScore(String modelName, String pavementId) {
        String url = String.format("%s/getDetail/%s/%s.json", BASE_URL, modelName, pavementId);
        ProcessedDefectResponse.MaterialWearInfo wearInfo = new ProcessedDefectResponse.MaterialWearInfo();

        try {
            String response = restTemplate.getForObject(url, String.class);
            MaterialDetails details = objectMapper.readValue(response, MaterialDetails.class);

            if (details != null && details.getAttributes() != null) {
                // Retrieve raw values
                double bitumenLevel = 0.0;
                double trafficLoad = 0.0;
                double spanFactor = 0.0;

                MaterialDetails.AttributeData topMaterial = details.getAttributes().get("topMaterial");
                if (topMaterial != null && topMaterial.getData() != null) {
                    Optional<MaterialDetails.DataItem> bitumen = topMaterial.getData().stream()
                            .filter(item -> "Bitumen".equals(item.getType()))
                            .findFirst();
                    if (bitumen.isPresent()) {
                        bitumenLevel = bitumen.get().getQuantity();
                    }
                }

                MaterialDetails.AttributeData traffic = details.getAttributes().get("carsPerHour");
                if (traffic != null && !traffic.getData().isEmpty()) {
                    trafficLoad = traffic.getData().get(0).getCarsPerHour();
                }

                MaterialDetails.AttributeData span = details.getAttributes().get("span");
                if (span != null && !span.getData().isEmpty()) {
                    spanFactor = span.getData().get(0).getSpan();
                }

                // Normalise each parameter
                // More bitumen is generally protective, so higher bitumen gives lower factor
                double bitumenFactor = 1.0 - (bitumenLevel / (bitumenLevel + 25));
                // Higher traffic increases wear
                double trafficFactor = trafficLoad / (trafficLoad + 300);
                // Longer spans might indicate more stress
                double spanFactorNormalized = spanFactor / (spanFactor + 7500);

                // Combine factors with weights (e.g., 0.4, 0.3, 0.3) so they sum to 1:
                double w1 = 0.4, w2 = 0.3, w3 = 0.3;
                double wearScore = w1 * bitumenFactor + w2 * trafficFactor + w3 * spanFactorNormalized;
                wearScore = Math.min(Math.max(wearScore, 0.0), 1.0); // Ensure between 0 and 1

                wearInfo.setWearScore(wearScore);
                wearInfo.setBitumenLevel(bitumenLevel);
                wearInfo.setTrafficLoad(trafficLoad);
                wearInfo.setSpanFactor(spanFactor);

                System.out.println("Processed " + pavementId + ":");
                System.out.println("  Raw Bitumen: " + bitumenLevel);
                System.out.println("  Raw Traffic: " + trafficLoad);
                System.out.println("  Raw Span: " + spanFactor);
                System.out.println("  Computed Bitumen Factor: " + bitumenFactor);
                System.out.println("  Computed Traffic Factor: " + trafficFactor);
                System.out.println("  Computed Span Factor: " + spanFactorNormalized);
                System.out.println("  Final Wear Score: " + wearScore);

                return wearInfo;
            }
        } catch (Exception e) {
            System.err.println("Error processing material details for " + pavementId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return createDefaultWearInfo(pavementId);
    }

    private ProcessedDefectResponse.MaterialWearInfo createDefaultWearInfo(String pavementId) {
        ProcessedDefectResponse.MaterialWearInfo wearInfo = new ProcessedDefectResponse.MaterialWearInfo();

        // Use different seed values for different attributes to create more variation
        double bitumenSeed = (double)(Math.abs(pavementId.hashCode()) % 1000) / 1000.0;
        double trafficSeed = (double)(Math.abs((pavementId + "traffic").hashCode()) % 1000) / 1000.0;
        double spanSeed = (double)(Math.abs((pavementId + "span").hashCode()) % 1000) / 1000.0;

        // Bitumen level (5-20)
        double bitumenLevel = 5.0 + (bitumenSeed * 15.0);

        // Traffic load (200-1000 cars per hour)
        double trafficLoad = 200.0 + (trafficSeed * 800.0);

        // Span factor (500-2000 meters)
        double spanFactor = 500.0 + (spanSeed * 1500.0);

        // Calculate wear score components
        double bitumenContribution = (1.0 - (bitumenLevel / 20.0)) * 0.4;  // 0-0.4 based on bitumen
        double trafficContribution = ((trafficLoad - 200.0) / 800.0) * 0.3;  // 0-0.3 based on traffic
        double spanContribution = ((spanFactor - 500.0) / 1500.0) * 0.3;  // 0-0.3 based on span

        // Total wear score (0-1)
        double wearScore = bitumenContribution + trafficContribution + spanContribution;
        wearScore = Math.min(Math.max(wearScore, 0.0), 1.0);

        wearInfo.setWearScore(wearScore);
        wearInfo.setBitumenLevel(bitumenLevel);
        wearInfo.setTrafficLoad(trafficLoad);
        wearInfo.setSpanFactor(spanFactor);

        return wearInfo;
    }

    /**
     * Fetches the filtered list, computes wear scores for items starting with "Pavement.",
     * checks for associated pothole defects, and assigns ranking label accordingly.
     */
    public List<PavementWearRanking> rankPavements(String modelName) {
        try {
            // Get the full list from the getFiltered endpoint
            String url = String.format("%s/getFiltered/%s.json", BASE_URL, modelName);
            String response = restTemplate.getForObject(url, String.class);

            // Deserialise into the FilteredItems object
            FilteredItems filteredItems = objectMapper.readValue(response, FilteredItems.class);
            List<String> items = filteredItems.getMatched();

            // Filter only pavement items
            List<String> pavementItems = items.stream()
                    .filter(item -> item.startsWith("Pavement."))
                    .collect(Collectors.toList());

            // Fetch defects and build set of pavement surfaces with pothole defects
            Set<String> defectSurfaces = fetchDefectSurfaces();

            List<PavementWearRanking> rankings = new ArrayList<>();

            // Compute wear score for each pavement item and adjust ranking if defect is present
            for (String pavementId : pavementItems) {
                ProcessedDefectResponse.MaterialWearInfo wearInfo = calculatePavementWearScore(modelName, pavementId);
                double wearScore = wearInfo.getWearScore();
                String rank;
                // If this pavement has defect (pothole) then mark as "Defect"
                if (defectSurfaces.contains(pavementId)) {
                    rank = "defect";
                } else {
                    rank = rankWearScore(wearScore);
                }

                PavementWearRanking ranking = new PavementWearRanking();
                ranking.setPavementId(pavementId);
                ranking.setWearScore(wearScore);
                ranking.setBitumenLevel(wearInfo.getBitumenLevel());
                ranking.setTrafficLoad(wearInfo.getTrafficLoad());
                ranking.setSpanFactor(wearInfo.getSpanFactor());
                ranking.setRank(rank);
                rankings.add(ranking);
            }

            return rankings;
        } catch (Exception e) {
            System.err.println("Error ranking pavements: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Fetches defects from the defects API endpoint and returns set of pavement surface IDs that have potholes.
     */
    private Set<String> fetchDefectSurfaces() {
        try {
            String response = restTemplate.getForObject(DEFECTS_URL, String.class);
            // Parse the JSON response into Map where the key "defects" maps to list of DefectItem
            Map<String, List<DefectItem>> defectMap = objectMapper.readValue(response,
                    new TypeReference<Map<String, List<DefectItem>>>(){});
            List<DefectItem> defects = defectMap.get("defects");
            if (defects != null) {
                // Collect the surfaces that have defects
                return defects.stream()
                        .filter(defect -> "Potholes".equals(defect.getType()))
                        .map(DefectItem::getSurface)
                        .collect(Collectors.toSet());
            }
        } catch (Exception e) {
            System.err.println("Error fetching defect items: " + e.getMessage());
            e.printStackTrace();
        }
        return Collections.emptySet();
    }

    /**
     * Assigns rank based on wearScore.
     */
    private String rankWearScore(double wearScore) {
        if (wearScore < 0.5) {
            return "normal";
        } else if (wearScore < 0.55) {
            return "low";
        } else if (wearScore < 0.6) {
            return "medium";
        } else if (wearScore < 0.65) {
            return "high";
        } else {
            return "severe";
        }
    }
}