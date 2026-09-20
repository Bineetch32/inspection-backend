package com.inspection.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inspection.backend.model.PartMaster;
import com.inspection.backend.service.PartMasterService;

@RestController
@RequestMapping("/api/parts")
public class PartMasterController {

    private final PartMasterService partMasterService;

    public PartMasterController(PartMasterService partMasterService) {
        this.partMasterService = partMasterService;
    }

    @GetMapping
    public ResponseEntity<List<PartMaster>> getAllParts() {

        return ResponseEntity.ok(
                partMasterService.getAllParts()
        );
    }

    @PostMapping
    public ResponseEntity<?> addPart(
            @RequestBody PartMaster partMaster) {

        try {

            PartMaster savedPart =
                    partMasterService.savePart(partMaster);

            return ResponseEntity.ok(savedPart);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePart(
            @PathVariable Long id) {

        partMasterService.deletePart(id);

        return ResponseEntity.noContent().build();
    }
}