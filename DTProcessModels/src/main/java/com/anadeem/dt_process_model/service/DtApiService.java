package com.anadeem.dt_process_model.service;

import org.springframework.web.client.RestTemplate;

import com.anadeem.dt_process_model.model.DefectResponse;
import com.anadeem.dt_process_model.model.OffsetResponse;

import org.springframework.beans.factory.annotation.Autowired;             // For @Autowired
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;                    // For ObjectMapper
import com.fasterxml.jackson.core.JsonProcessingException;             // For JSON parse exception

@Service
public class DtApiService {
    @Autowired
    private ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://localhost:7070";

    public DtApiService() {
        this.restTemplate = new RestTemplate();
    }

    public DefectResponse getDefects(String modelName) {
        String url = BASE_URL + "/getDefects/" + modelName + ".json";
        String plainResponse = restTemplate.getForObject(url, String.class);
        return parseDefectResponse(plainResponse);
    }

    private DefectResponse parseDefectResponse(String plainResponse) {
        try {
            return objectMapper.readValue(plainResponse, DefectResponse.class);
        } catch (JsonProcessingException e) {
            System.err.println("Failed to parse JSON: " + plainResponse);
            e.printStackTrace();
            return null;
        }
    }
}