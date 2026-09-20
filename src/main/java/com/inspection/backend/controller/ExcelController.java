package com.inspection.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.inspection.backend.model.InspectionRecord;
import com.inspection.backend.service.ExcelService;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    private final ExcelService excelService;

    public ExcelController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @PostMapping("/upload")
    public ResponseEntity<List<InspectionRecord>> uploadExcel(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null ||
                !(fileName.toLowerCase().endsWith(".xlsx")
                        || fileName.toLowerCase().endsWith(".xls"))) {

            return ResponseEntity.badRequest().build();
        }

        try {
            List<InspectionRecord> records =
                    excelService.readInspectionData(file);

            return ResponseEntity.ok(records);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}