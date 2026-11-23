package com.resume.backend;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisResult(
        @JsonAlias("overall_score") int overallScore,
        String summary,
        List<String> skills,
        List<String> strengths,
        List<String> weaknesses,
        List<String> suggestions,
        @JsonAlias("match_score") Integer matchScore,
        @JsonAlias("missing_keywords") List<String> missingKeywords
) {
}