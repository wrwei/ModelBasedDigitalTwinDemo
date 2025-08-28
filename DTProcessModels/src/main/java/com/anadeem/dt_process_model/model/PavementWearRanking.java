package com.anadeem.dt_process_model.model;

import lombok.Data;

@Data
public class PavementWearRanking {
    private String pavementId;
    private double wearScore;
    private double bitumenLevel;
    private double trafficLoad;
    private double spanFactor;
    private String rank;  // One of: "normal", "low", "medium", "high", "severe"
}
