package com.inspection.backend.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inspection.backend.model.InspectionRecordEntity;

public interface InspectionRecordRepository
        extends JpaRepository<InspectionRecordEntity, Long> {

    boolean existsByInspectionDateAndPartNoAndSheetNameAndExcelRowNumber(
            LocalDate inspectionDate,
            String partNo,
            String sheetName,
            Integer excelRowNumber);
}
