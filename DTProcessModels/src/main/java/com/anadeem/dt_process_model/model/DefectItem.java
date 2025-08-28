package com.anadeem.dt_process_model.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
public class DefectItem {
    @JsonProperty("surface")
    private String surface;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("description")
    private String description;

    @JsonProperty("location")
    private List<Double> location;
}