package com.resume.backend;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisResult(
        @JsonProperty("overall_score") int overallScore,
        String summary,
        List<String> skills,
        List<String> strengths,
        List<String> weaknesses,
        List<String> suggestions,
        @JsonProperty("match_score") Integer matchScore,
        @JsonProperty("missing_keywords") List<String> missingKeywords
) {
}