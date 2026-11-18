package com.resume.backend;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class AnalyzeController {

    private final AiClientService aiClient;

    public AnalyzeController(AiClientService aiClient) {
        this.aiClient = aiClient;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "jobDescription", required = false) String jobDescription) {

        if (file.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "Please choose a resume file.");
        }

        try {
            return ResponseEntity.ok(aiClient.analyze(file, jobDescription));
        } catch (HttpClientErrorException e) {
            // Python rejected the file (wrong type, too big, no text)
            return error(HttpStatus.valueOf(e.getStatusCode().value()), extractDetail(e.getResponseBodyAsString()));
        } catch (HttpServerErrorException e) {
            return error(HttpStatus.BAD_GATEWAY, "The AI service failed. Please try again.");
        } catch (ResourceAccessException e) {
            return error(HttpStatus.SERVICE_UNAVAILABLE, "The AI service is not reachable. Is it running on port 8000?");
        } catch (IOException e) {
            return error(HttpStatus.BAD_REQUEST, "Could not read the uploaded file.");
        }
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }

    // Python errors look like {"detail":"..."}; pull out the message
    private String extractDetail(String body) {
        int i = body.indexOf("\"detail\":\"");
        if (i < 0) return "The file could not be processed.";
        int start = i + 10;
        int end = body.indexOf("\"", start);
        return end > start ? body.substring(start, end) : "The file could not be processed.";
    }
}