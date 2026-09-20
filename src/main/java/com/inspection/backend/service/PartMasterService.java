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

    public PartMaster savePart(PartMaster partMaster) {

        List<PartMaster> existingParts =
                partMasterRepository.findAll();

        for (PartMaster existing : existingParts) {

            if (same(existing.getPartNo(), partMaster.getPartNo())
                    && same(existing.getPartName(), partMaster.getPartName())
                    && same(existing.getVendorCode(), partMaster.getVendorCode())
                    && same(existing.getVendorName(), partMaster.getVendorName())
                    && same(existing.getModel(), partMaster.getModel())
                    && same(existing.getPackaging(), partMaster.getPackaging())) {

                throw new IllegalArgumentException(
                        "Duplicate Part Master entry already exists."
                );
            }
        }

        return partMasterRepository.save(partMaster);
    }

    public void deletePart(Long id) {

        partMasterRepository.deleteById(id);
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
                                .replaceAll("\\s+", " ")
                );
    }
}