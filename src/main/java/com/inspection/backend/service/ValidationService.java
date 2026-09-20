package com.inspection.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.inspection.backend.model.InspectionRecord;
import com.inspection.backend.model.PartMaster;

@Service
public class ValidationService {

    private final PartMasterService partMasterService;

    public ValidationService(PartMasterService partMasterService) {
        this.partMasterService = partMasterService;
    }

    public List<String> validate(InspectionRecord record) {

        List<String> errors = new ArrayList<>();

        PartMaster partMaster = null;

        // 1. Part No validation
        if (isBlank(record.getPartNo())) {

            errors.add("Part No is missing.");

        } else {

            partMaster =
                    partMasterService.findByPartNo(record.getPartNo());

            if (partMaster == null) {

                errors.add(
                    "Part No '" + record.getPartNo()
                    + "' is not available in Part Master."
                );
            }
        }

        // Stop master-related validation if Part No is not found
        if (partMaster != null) {

            // 2. Part Name validation
            if (!normalize(record.getPartName())
                    .equals(normalize(partMaster.getPartName()))) {

                errors.add(
                    "Part Name does not match Part Master."
                );
            }

            // 3. Vendor Code validation
            if (isBlank(record.getVendorCode())) {

                errors.add("Vendor Code is missing.");

            } else if (!normalize(partMaster.getVendorCode())
                    .equals(normalize(record.getVendorCode()))) {

                errors.add(
                    "Vendor Code does not match Part Master."
                );
            }

            // 4. Vendor Name validation
            if (isBlank(record.getVendorName())) {

                errors.add("Vendor Name is missing.");

            } else if (!normalize(partMaster.getVendorName())
                    .equals(normalize(record.getVendorName()))) {

                errors.add(
                    "Vendor Name does not match Part Master."
                );
            }

         // 5. Model validation
            if (isBlank(record.getModel())
                    && !isBlank(partMaster.getModel())) {

                errors.add("Model is missing.");

            } else if (!isBlank(record.getModel())
                    && !normalize(partMaster.getModel())
                            .equals(normalize(record.getModel()))) {

                errors.add(
                    "Model does not match Part Master."
                );
            }}

        // 6. Inspection Date validation
        if (record.getInspectionDate() == null) {

            errors.add("Inspection Date is missing.");
        }

        // 7. Quantity validation
        if (record.getQuantityChecked() == null
                || record.getQuantityChecked() <= 0) {

            errors.add(
                "Quantity Checked must be greater than 0."
            );
        }

        // 8. OK + NG validation
        if (record.getQuantityChecked() != null) {

            int ok = record.getOkQuantity() == null
                    ? 0
                    : record.getOkQuantity();

            int ng = record.getNgQuantity() == null
                    ? 0
                    : record.getNgQuantity();

            if (ok < 0 || ng < 0) {

                errors.add(
                    "OK Quantity and NG Quantity cannot be negative."
                );
            }

            if (record.getQuantityChecked() != ok + ng) {

                errors.add(
                    "Quantity Checked must equal OK Quantity + NG Quantity."
                );
            }
        }

        // 9. Inspection Status validation
        if (isBlank(record.getInspectionStatus())) {

            errors.add("Inspection Status is missing.");

        } else {

            String status =
                    record.getInspectionStatus()
                            .trim()
                            .toUpperCase();

            if (!status.equals("OK")
                    && !status.equals("NG")) {

                errors.add(
                    "Inspection Status must be OK or NG."
                );
            }
        }

        // 10. NG requires defect description
        if (record.getNgQuantity() != null
                && record.getNgQuantity() > 0
                && isBlank(record.getDefectDescription())) {

            errors.add(
                "Defect Description is required when NG Quantity is greater than 0."
            );
        }

        // 11. NG requires packaging status
        if (record.getNgQuantity() != null
                && record.getNgQuantity() > 0
                && isBlank(record.getPackagingStatus())) {

            errors.add(
                "Packaging Status is required when NG Quantity is greater than 0."
            );
        }

        return errors;
    }

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
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
}