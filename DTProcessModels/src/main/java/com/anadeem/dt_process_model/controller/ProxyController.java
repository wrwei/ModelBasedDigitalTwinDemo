package com.anadeem.dt_process_model.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;

@Controller
@RequestMapping("/proxy")
public class ProxyController {

    private static final String JAR_SERVER = "http://localhost:7070";
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/mesh/{modelName}/{meshName}.glb")
    public ResponseEntity<byte[]> proxyMeshFile(
            @PathVariable String modelName,
            @PathVariable String meshName) {
        try {
            String originalUrl = String.format("%s/getMesh/%s/%s.glb", JAR_SERVER, modelName, meshName);
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    originalUrl,
                    HttpMethod.GET,
                    null,
                    byte[].class
            );
            HttpHeaders headers = new HttpHeaders();
            headers.putAll(response.getHeaders());
            headers.set("Content-Type", "model/gltf-binary");
            return new ResponseEntity<>(response.getBody(), headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}