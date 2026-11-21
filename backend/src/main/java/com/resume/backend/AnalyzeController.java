package com.resume.backend;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;

import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class AnalyzeController {

    private final AiClientService aiClient;
    private final AnalysisRecordRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalyzeController(AiClientService aiClient, AnalysisRecordRepository repository) {
        this.aiClient = aiClient;
        this.repository = repository;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "jobDescription", required = false) String jobDescription) {

        if (file.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "Please choose a resume file.");
        }

        try {
            AnalysisResult result = aiClient.analyze(file, jobDescription);
            save(file.getOriginalFilename(), result);
            return ResponseEntity.ok(result);
        } catch (HttpClientErrorException e) {
            return error(HttpStatus.valueOf(e.getStatusCode().value()), extractDetail(e.getResponseBodyAsString()));
        } catch (HttpServerErrorException e) {
            return error(HttpStatus.BAD_GATEWAY, "The AI service failed. Please try again.");
        } catch (ResourceAccessException e) {
            return error(HttpStatus.SERVICE_UNAVAILABLE, "The AI service is not reachable. Is it running on port 8000?");
        } catch (IOException e) {
            return error(HttpStatus.BAD_REQUEST, "Could not read the uploaded file.");
        }
    }

    @GetMapping("/history")
    public List<Map<String, Object>> history() {
        return repository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(r -> Map.<String, Object>of(
                        "id", r.getId(),
                        "fileName", r.getFileName() == null ? "resume" : r.getFileName(),
                        "overallScore", r.getOverallScore(),
                        "matchScore", r.getMatchScore() == null ? -1 : r.getMatchScore(),
                        "createdAt", r.getCreatedAt().toString()))
                .toList();
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<?> historyItem(@PathVariable Long id) {
        return repository.findById(id)
                .<ResponseEntity<?>>map(r -> {
                    try {
                        return ResponseEntity.ok(objectMapper.readValue(r.getResultJson(), AnalysisResult.class));
                    } catch (Exception e) {
                        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Saved result could not be read.");
                    }
                })
                .orElseGet(() -> error(HttpStatus.NOT_FOUND, "Analysis not found."));
    }

    private void save(String fileName, AnalysisResult result) {
        try {
            AnalysisRecord record = new AnalysisRecord();
            record.setFileName(fileName);
            record.setOverallScore(result.overallScore());
            record.setMatchScore(result.matchScore());
            String summary = result.summary();
            record.setSummary(summary != null && summary.length() > 2000 ? summary.substring(0, 2000) : summary);
            record.setResultJson(objectMapper.writeValueAsString(result));
            repository.save(record);
        } catch (Exception e) {
            // Saving history must never break the analysis itself
            System.err.println("Could not save history: " + e.getMessage());
        }
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }

    private String extractDetail(String body) {
        int i = body.indexOf("\"detail\":\"");
        if (i < 0) return "The file could not be processed.";
        int start = i + 10;
        int end = body.indexOf("\"", start);
        return end > start ? body.substring(start, end) : "The file could not be processed.";
    }
}