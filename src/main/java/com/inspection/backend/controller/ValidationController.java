package com.inspection.backend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.inspection.backend.model.InspectionRecord;
import com.inspection.backend.service.ExcelService;
import com.inspection.backend.service.ValidationService;

@RestController
@RequestMapping("/api/validation")
public class ValidationController {

    private final ExcelService excelService;
    private final ValidationService validationService;

    public ValidationController(
            ExcelService excelService,
            ValidationService validationService) {

        this.excelService = excelService;
        this.validationService = validationService;
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateExcel(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Excel file is required.");
        }

        try {

            List<InspectionRecord> records =
                    excelService.readInspectionData(file);

            List<Map<String, Object>> results =
                    new ArrayList<>();

            int validCount = 0;
            int invalidCount = 0;

            for (InspectionRecord record : records) {

                List<String> errors =
                        validationService.validate(record);

                Map<String, Object> result =
                        new HashMap<>();

                result.put("partNo", record.getPartNo());
                result.put("inspectionDate",
                        record.getInspectionDate());
                result.put("status",
                        errors.isEmpty() ? "VALID" : "INVALID");
                result.put("errors", errors);

                results.add(result);

                if (errors.isEmpty()) {
                    validCount++;
                } else {
                    invalidCount++;
                }
            }

            Map<String, Object> response =
                    new HashMap<>();

            response.put("totalRecords", records.size());
            response.put("validRecords", validCount);
            response.put("invalidRecords", invalidCount);
            response.put("reportGenerationAllowed",
                    invalidCount == 0);
            response.put("records", results);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body("Validation failed: " + e.getMessage());
        }
    }
}