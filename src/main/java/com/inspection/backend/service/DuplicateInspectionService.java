package com.inspection.backend.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.inspection.backend.model.InspectionRecord;

@Service
public class DuplicateInspectionService {

    // Find duplicate inspection rows
    public List<Map<String, Object>> findDuplicates(
            List<InspectionRecord> records) {

        List<Map<String, Object>> warnings =
                new ArrayList<>();

        Set<String> seen = new HashSet<>();

        for (InspectionRecord record : records) {

            String key = createKey(record);

            if (seen.contains(key)) {

                Map<String, Object> warning =
                        new LinkedHashMap<>();

                warning.put(
                        "partNo",
                        record.getPartNo());

                warning.put(
                        "sheetName",
                        record.getSheetName());

                warning.put(
                        "duplicateRow",
                        record.getExcelRowNumber());

                warning.put(
                        "message",
                        "Duplicate inspection entry found.");

                warnings.add(warning);

            } else {

                seen.add(key);
            }
        }

        return warnings;
    }

    // Remove duplicate rows.
    // First occurrence is kept.
    public List<InspectionRecord> removeDuplicates(
            List<InspectionRecord> records) {

        List<InspectionRecord> uniqueRecords =
                new ArrayList<>();

        Set<String> seen = new HashSet<>();

        for (InspectionRecord record : records) {

            String key = createKey(record);

            if (!seen.contains(key)) {

                seen.add(key);
                uniqueRecords.add(record);
            }
        }

        return uniqueRecords;
    }

    private String createKey(
            InspectionRecord record) {

        return normalize(record.getPartNo())
                + "|" + normalizeDate(record)
                + "|" + normalize(record.getPartName())
                + "|" + normalize(record.getVendorCode())
                + "|" + normalize(record.getVendorName())
                + "|" + normalize(record.getModel())
                + "|" + value(record.getQuantityChecked())
                + "|" + value(record.getOkQuantity())
                + "|" + value(record.getNgQuantity())
                + "|" + normalize(record.getInspectionStatus())
                + "|" + normalize(record.getDefectDescription())
                + "|" + normalize(record.getPackagingStatus());
    }

    private String normalizeDate(
            InspectionRecord record) {

        if (record.getInspectionDate() == null) {
            return "";
        }

        return record.getInspectionDate().toString();
    }

    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace('\u00A0', ' ')
                .replace("\u200B", "")
                .replace("\uFEFF", "")
                .replaceAll("[\\p{Z}\\s]+", " ")
                .trim()
                .toLowerCase();
    }

    private String value(Integer value) {

        return value == null
                ? ""
                : value.toString();
    }
}