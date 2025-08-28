package com.anadeem.dt_process_model.model;

import lombok.Data;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaterialDetails {
    private List<String> section;
    private Map<String, AttributeData> attributes;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AttributeData {
        private List<DataItem> data;
        private Options options;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataItem {
        private Double quantity;
        private String type;
        private String subtype;
        private Double lanes;
        private Double carsPerHour;
        private Double span;
        private String file;
        private Double x;
        private Double y;
        private Double z;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Options {
        private List<String> quantity;
        private List<String> type;
        private List<String> subtype;
        private List<String> lanes;
        private List<String> carsPerHour;
        private List<String> span;
        private List<String> file;
        private List<String> x;
        private List<String> y;
        private List<String> z;
    }
}