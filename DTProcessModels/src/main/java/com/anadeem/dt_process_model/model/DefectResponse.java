package com.anadeem.dt_process_model.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
public class DefectResponse {
    @JsonProperty("defects")
    private List<DefectItem> defects;
}