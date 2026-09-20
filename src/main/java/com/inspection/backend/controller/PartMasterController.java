package com.inspection.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
        return ResponseEntity.ok(partMasterService.getAllParts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPart(@PathVariable Long id) {
        PartMaster part = partMasterService.getById(id);

        if (part == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(part);
    }

    @PostMapping
    public ResponseEntity<?> addPart(@RequestBody PartMaster partMaster) {
        try {
            PartMaster savedPart = partMasterService.savePart(partMaster);
            return ResponseEntity.ok(savedPart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePart(
            @PathVariable Long id,
            @RequestBody PartMaster partMaster) {

        try {
            PartMaster updatedPart =
                    partMasterService.updatePart(id, partMaster);

            if (updatedPart == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(updatedPart);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePart(@PathVariable Long id) {
        if (partMasterService.getById(id) == null) {
            return ResponseEntity.notFound().build();
        }

        partMasterService.deletePart(id);
        return ResponseEntity.noContent().build();
    }
}
