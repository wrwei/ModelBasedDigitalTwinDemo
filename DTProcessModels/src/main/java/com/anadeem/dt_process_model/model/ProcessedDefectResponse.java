package com.anadeem.dt_process_model.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ProcessedDefectResponse {
    private List<DefectItem> defects;
    private Map<String, MaterialWearInfo> pavementWearData;

    @Data
    public static class MaterialWearInfo {
        private double wearScore;
        private double bitumenLevel;
        private double trafficLoad;
        private double spanFactor;
    }
}