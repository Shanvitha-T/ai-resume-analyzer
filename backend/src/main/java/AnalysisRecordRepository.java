package com.resume.backend;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRecordRepository extends JpaRepository<AnalysisRecord, Long> {

    List<AnalysisRecord> findTop20ByOrderByCreatedAtDesc();
}