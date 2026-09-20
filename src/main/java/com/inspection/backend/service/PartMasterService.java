package com.inspection.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.inspection.backend.model.PartMaster;
import com.inspection.backend.repository.PartMasterRepository;

@Service
public class PartMasterService {

    private final PartMasterRepository partMasterRepository;

    public PartMasterService(PartMasterRepository partMasterRepository) {
        this.partMasterRepository = partMasterRepository;
    }

    public PartMaster findByPartNo(String partNo) {
        if (partNo == null) {
            return null;
        }

        return partMasterRepository
                .findByPartNoIgnoreCase(partNo.trim())
                .filter(PartMaster::isActive)
                .orElse(null);
    }

    public List<PartMaster> getAllParts() {
        return partMasterRepository.findAll();
    }

    public PartMaster getById(Long id) {
        return partMasterRepository.findById(id).orElse(null);
    }

    public PartMaster savePart(PartMaster partMaster) {
        validateRequiredFields(partMaster);

        List<PartMaster> existingParts = partMasterRepository.findAll();

        for (PartMaster existing : existingParts) {
            if (isSameBusinessPart(existing, partMaster)) {
                throw new IllegalArgumentException(
                        "Duplicate Part Master entry already exists.");
            }
        }

        return partMasterRepository.save(partMaster);
    }

    public PartMaster updatePart(Long id, PartMaster partMaster) {
        validateRequiredFields(partMaster);

        PartMaster existingPart = getById(id);

        if (existingPart == null) {
            return null;
        }

        List<PartMaster> allParts = partMasterRepository.findAll();

        for (PartMaster existing : allParts) {
            if (!existing.getId().equals(id)
                    && isSameBusinessPart(existing, partMaster)) {

                throw new IllegalArgumentException(
                        "Duplicate Part Master entry already exists.");
            }
        }

        existingPart.setPartNo(partMaster.getPartNo());
        existingPart.setPartName(partMaster.getPartName());
        existingPart.setVendorCode(partMaster.getVendorCode());
        existingPart.setVendorName(partMaster.getVendorName());
        existingPart.setModel(partMaster.getModel());
        existingPart.setPackaging(partMaster.getPackaging());
        existingPart.setActive(partMaster.isActive());

        return partMasterRepository.save(existingPart);
    }

    public void deletePart(Long id) {
        partMasterRepository.deleteById(id);
    }

    private void validateRequiredFields(PartMaster partMaster) {
        if (partMaster == null
                || isBlank(partMaster.getPartNo())
                || isBlank(partMaster.getPartName())
                || isBlank(partMaster.getVendorCode())
                || isBlank(partMaster.getVendorName())
                || isBlank(partMaster.getModel())
                || isBlank(partMaster.getPackaging())) {

            throw new IllegalArgumentException(
                    "Part No, Part Name, Vendor Code, Vendor Name, Model and Packaging are required.");
        }
    }

    private boolean isSameBusinessPart(
            PartMaster first,
            PartMaster second) {

        return same(first.getPartNo(), second.getPartNo())
                && same(first.getPartName(), second.getPartName())
                && same(first.getVendorCode(), second.getVendorCode())
                && same(first.getVendorName(), second.getVendorName())
                && same(first.getModel(), second.getModel())
                && same(first.getPackaging(), second.getPackaging());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean same(String first, String second) {
        if (first == null && second == null) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return first.trim()
                .replaceAll("\\s+", " ")
                .equalsIgnoreCase(
                        second.trim()
                                .replaceAll("\\s+", " "));
    }
}
