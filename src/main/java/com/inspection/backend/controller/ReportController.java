package com.inspection.backend.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.inspection.backend.model.InspectionRecord;
import com.inspection.backend.service.DuplicateInspectionService;
import com.inspection.backend.service.ExcelService;
import com.inspection.backend.service.PdfReportService;
import com.inspection.backend.service.InspectionRecordService;
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
    private final InspectionRecordService inspectionRecordService;

    public ReportController(
            ExcelService excelService,
            ValidationService validationService,
            DuplicateInspectionService duplicateService,
            ReportCalculationService reportService,
            PdfReportService pdfReportService,
            InspectionRecordService inspectionRecordService) {

        this.excelService = excelService;
        this.validationService = validationService;
        this.duplicateService = duplicateService;
        this.reportService = reportService;
        this.pdfReportService = pdfReportService;
        this.inspectionRecordService = inspectionRecordService;
    }



    @GetMapping("/current")
    public ResponseEntity<?> getCurrentReport(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String model) {

        try {

            List<InspectionRecord> records =
                    filterRecords(
                            inspectionRecordService.getAllAsInspectionRecords(),
                            fromDate,
                            toDate,
                            model);

            if (records.isEmpty()) {
                return ResponseEntity.ok(
                        java.util.Map.of(
                                "reportGenerationAllowed", false,
                                "message", "No inspection records found for selected filters."));
            }

            Map<String, Object> report =
                    reportService.calculate(records);

            report.put("reportGenerationAllowed", true);
            report.put("reportType", "Receiving Inspection Report");
            report.put("generatedAt",
                    LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));

            return ResponseEntity.ok(report);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body("Unable to load report from MySQL: " + e.getMessage());
        }
    }

    @GetMapping("/pdf/current")
    public ResponseEntity<?> generateCurrentPdf(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String model) {

        try {

            List<InspectionRecord> records =
                    filterRecords(
                            inspectionRecordService.getAllAsInspectionRecords(),
                            fromDate,
                            toDate,
                            model);

            if (records.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("No inspection records found for selected filters.");
            }

            Map<String, Object> report =
                    reportService.calculate(records);

            report.put("reportType", "Receiving Inspection Report");
            report.put("generatedAt",
                    LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));

            byte[] pdf =
                    pdfReportService.generateReport(report);

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename("Inspection-Report.pdf")
                            .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body("PDF generation failed: " + e.getMessage());
        }
    }

    private List<InspectionRecord> filterRecords(
            List<InspectionRecord> records,
            String fromDateText,
            String toDateText,
            String model) {

        LocalDate fromDate = parseDate(fromDateText);
        LocalDate toDate = parseDate(toDateText);

        if (fromDate != null && toDate != null
                && fromDate.isAfter(toDate)) {

            throw new IllegalArgumentException(
                    "From Date cannot be after To Date.");
        }

        String selectedModel =
                model == null ? "" : model.trim();

        List<InspectionRecord> result =
                new ArrayList<>();

        for (InspectionRecord record : records) {

            LocalDate date = record.getInspectionDate();

            if (fromDate != null
                    && (date == null || date.isBefore(fromDate))) {
                continue;
            }

            if (toDate != null
                    && (date == null || date.isAfter(toDate))) {
                continue;
            }

            if (!selectedModel.isEmpty()
                    && !selectedModel.equalsIgnoreCase(
                            record.getModel() == null
                                    || record.getModel().trim().isEmpty()
                                    ? "UNKNOWN"
                                    : record.getModel().trim())) {
                continue;
            }

            result.add(record);
        }

        return result;
    }

    private LocalDate parseDate(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid date filter. Use YYYY-MM-DD.");
        }
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

        boolean reportAllowed =
                !validRecords.isEmpty()
                && validationWarnings.isEmpty()
                && duplicateWarnings.isEmpty();

        response.put(
                "reportGenerationAllowed",
                reportAllowed);

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

        if (reportAllowed) {

            inspectionRecordService.saveAll(validRecords);

            Map<String, Object> report =
                    reportService.calculate(
                            validRecords);

            response.putAll(report);
        }

        return response;
    }
}