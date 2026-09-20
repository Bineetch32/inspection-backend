package com.inspection.backend.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.inspection.backend.model.InspectionRecord;
import com.inspection.backend.service.DuplicateInspectionService;
import com.inspection.backend.service.ExcelService;
import com.inspection.backend.service.PdfReportService;
import com.inspection.backend.service.ReportCalculationService;
import com.inspection.backend.service.ValidationService;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ExcelService excelService;
    private final ValidationService validationService;
    private final DuplicateInspectionService duplicateService;
    private final ReportCalculationService reportService;
    private final PdfReportService pdfReportService;

    public ReportController(
            ExcelService excelService,
            ValidationService validationService,
            DuplicateInspectionService duplicateService,
            ReportCalculationService reportService,
            PdfReportService pdfReportService) {

        this.excelService = excelService;
        this.validationService = validationService;
        this.duplicateService = duplicateService;
        this.reportService = reportService;
        this.pdfReportService = pdfReportService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<?> calculateReport(
            @RequestParam("file") MultipartFile file) {

        try {

            Map<String, Object> result =
                    buildReportData(file);

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body(
                            "Report calculation failed: "
                            + e.getMessage()
                    );
        }
    }

    @PostMapping("/pdf")
    public ResponseEntity<?> generatePdf(
            @RequestParam("file") MultipartFile file) {

        try {

            Map<String, Object> report =
                    buildReportData(file);

            boolean allowed =
                    Boolean.TRUE.equals(
                            report.get(
                                    "reportGenerationAllowed"));

            if (!allowed) {

                return ResponseEntity.badRequest()
                        .body(report);
            }

            byte[] pdf =
                    pdfReportService.generateReport(report);

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_PDF);

            headers.setContentDisposition(
                    ContentDisposition
                            .attachment()
                            .filename(
                                    "Inspection-Report.pdf")
                            .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf);

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body(
                            "PDF generation failed: "
                            + e.getMessage()
                    );
        }
    }

    private Map<String, Object> buildReportData(
            MultipartFile file)
            throws Exception {

        List<InspectionRecord> records =
                excelService.readInspectionData(file);

        // Duplicate check
        List<Map<String, Object>> duplicateWarnings =
                duplicateService.findDuplicates(records);

        List<InspectionRecord> uniqueRecords =
                duplicateService.removeDuplicates(records);

        // Validation
        List<InspectionRecord> validRecords =
                new ArrayList<>();

        List<Map<String, Object>> validationWarnings =
                new ArrayList<>();

        for (InspectionRecord record : uniqueRecords) {

            List<String> errors =
                    validationService.validate(record);

            if (errors.isEmpty()) {

                validRecords.add(record);

            } else {

                Map<String, Object> warning =
                        new LinkedHashMap<>();

                warning.put(
                        "partNo",
                        record.getPartNo());

                warning.put(
                        "sheetName",
                        record.getSheetName());

                warning.put(
                        "excelRow",
                        record.getExcelRowNumber());

                warning.put(
                        "inspectionDate",
                        record.getInspectionDate());

                warning.put(
                        "errors",
                        errors);

                validationWarnings.add(
                        warning);
            }
        }

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "reportGenerationAllowed",
                !validRecords.isEmpty());

        response.put(
                "totalRecords",
                records.size());

        response.put(
                "validRecords",
                validRecords.size());

        response.put(
                "invalidRecords",
                validationWarnings.size());

        response.put(
                "duplicateRecords",
                duplicateWarnings.size());

        response.put(
                "duplicateWarnings",
                duplicateWarnings);

        response.put(
                "validationWarnings",
                validationWarnings);

        if (!validRecords.isEmpty()) {

            Map<String, Object> report =
                    reportService.calculate(
                            validRecords);

            response.putAll(report);
        }

        return response;
    }
}