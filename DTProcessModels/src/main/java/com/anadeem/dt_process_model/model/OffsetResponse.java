package com.anadeem.dt_process_model.model;

import lombok.Data;
import java.util.List;

@Data
public class OffsetResponse {
    private List<Double> offset; // x, y, z
}