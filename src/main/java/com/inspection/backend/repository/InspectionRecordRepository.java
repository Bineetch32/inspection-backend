package com.inspection.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inspection.backend.model.InspectionRecordEntity;

public interface InspectionRecordRepository
        extends JpaRepository<InspectionRecordEntity, Long> {
}
