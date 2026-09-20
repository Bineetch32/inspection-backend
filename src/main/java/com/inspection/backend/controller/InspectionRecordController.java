package com.inspection.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inspection.backend.model.InspectionRecordEntity;
import com.inspection.backend.service.InspectionRecordService;

@RestController
@RequestMapping("/api/inspections")
public class InspectionRecordController {

    private final InspectionRecordService service;

    public InspectionRecordController(InspectionRecordService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<InspectionRecordEntity>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}
