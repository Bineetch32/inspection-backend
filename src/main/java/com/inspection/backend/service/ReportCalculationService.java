package com.inspection.backend.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.inspection.backend.model.InspectionRecord;

@Service
public class ReportCalculationService {

    public Map<String, Object> calculate(
            List<InspectionRecord> records) {

        Map<String, Object> report =
                new LinkedHashMap<>();

        int totalChecked = 0;
        int totalOk = 0;
        int totalNg = 0;

        // Unique part groups
        Set<String> overallPartKeys =
                new LinkedHashSet<>();

        // Model-wise summary
        Map<String, Map<String, Object>> modelSummary =
                new LinkedHashMap<>();

        Map<String, Set<String>> modelPartKeys =
                new LinkedHashMap<>();

        // Part-wise summary
        Map<String, Map<String, Object>> partSummary =
                new LinkedHashMap<>();

        // Defect summary
        Map<String, Integer> defectSummary =
                new LinkedHashMap<>();

        // Packaging-wise summary
        Map<String, Map<String, Object>> packagingSummary =
                new LinkedHashMap<>();

        // NG details
        List<Map<String, Object>> ngParts =
                new ArrayList<>();


        // =====================================================
        // PROCESS RECORDS
        // =====================================================

        for (InspectionRecord record : records) {

            int checked =
                    getValue(record.getQuantityChecked());

            int ok =
                    getValue(record.getOkQuantity());

            int ng =
                    getValue(record.getNgQuantity());


            totalChecked += checked;
            totalOk += ok;
            totalNg += ng;


            // =================================================
            // PART KEY
            // Part No + Vendor Code + Model
            // =================================================

            String partKey =
                    normalize(record.getPartNo())
                    + "|"
                    + normalize(record.getVendorCode())
                    + "|"
                    + normalize(record.getModel());

            overallPartKeys.add(partKey);


            // =================================================
            // MODEL-WISE SUMMARY
            // =================================================

            String model =
                    getText(record.getModel());

            Map<String, Object> modelData =
                    modelSummary.get(model);

            if (modelData == null) {

                modelData =
                        new LinkedHashMap<>();

                modelData.put(
                        "model",
                        model);

                modelData.put(
                        "totalParts",
                        0);

                modelData.put(
                        "totalQty",
                        0);

                modelData.put(
                        "okQty",
                        0);

                modelData.put(
                        "ngQty",
                        0);

                modelData.put(
                        "rejectionPercentage",
                        0.0);

                modelSummary.put(
                        model,
                        modelData);

                modelPartKeys.put(
                        model,
                        new LinkedHashSet<>());
            }

            modelPartKeys
                    .get(model)
                    .add(partKey);

            modelData.put(
                    "totalQty",
                    (int) modelData.get("totalQty")
                            + checked);

            modelData.put(
                    "okQty",
                    (int) modelData.get("okQty")
                            + ok);

            modelData.put(
                    "ngQty",
                    (int) modelData.get("ngQty")
                            + ng);


            // =================================================
            // PART-WISE SUMMARY
            // =================================================

            Map<String, Object> partData =
                    partSummary.get(partKey);

            if (partData == null) {

                partData =
                        new LinkedHashMap<>();

                partData.put(
                        "partNo",
                        record.getPartNo());

                partData.put(
                        "partName",
                        record.getPartName());

                partData.put(
                        "vendorCode",
                        record.getVendorCode());

                partData.put(
                        "vendorName",
                        record.getVendorName());

                partData.put(
                        "model",
                        record.getModel());

                partData.put(
                        "totalQty",
                        0);

                partData.put(
                        "okQty",
                        0);

                partData.put(
                        "ngQty",
                        0);

                partData.put(
                        "rejectionPercentage",
                        0.0);

                partSummary.put(
                        partKey,
                        partData);
            }

            partData.put(
                    "totalQty",
                    (int) partData.get("totalQty")
                            + checked);

            partData.put(
                    "okQty",
                    (int) partData.get("okQty")
                            + ok);

            partData.put(
                    "ngQty",
                    (int) partData.get("ngQty")
                            + ng);


            // =================================================
            // DEFECT SUMMARY
            // =================================================

            if (ng > 0
                    && !isBlank(
                            record.getDefectDescription())) {

                String defect =
                        record.getDefectDescription().trim();

                defectSummary.put(
                        defect,
                        defectSummary.getOrDefault(
                                defect,
                                0) + ng);
            }


            // =================================================
            // PACKAGING-WISE SUMMARY
            // =================================================

            String packaging =
                    getText(
                            record.getPackagingStatus());

            Map<String, Object> packagingData =
                    packagingSummary.get(packaging);

            if (packagingData == null) {

                packagingData =
                        new LinkedHashMap<>();

                packagingData.put(
                        "packaging",
                        packaging);

                packagingData.put(
                        "checkedQty",
                        0);

                packagingData.put(
                        "okQty",
                        0);

                packagingData.put(
                        "ngQty",
                        0);

                packagingData.put(
                        "result",
                        "PASS");

                packagingSummary.put(
                        packaging,
                        packagingData);
            }

            packagingData.put(
                    "checkedQty",
                    (int) packagingData.get("checkedQty")
                            + checked);

            packagingData.put(
                    "okQty",
                    (int) packagingData.get("okQty")
                            + ok);

            packagingData.put(
                    "ngQty",
                    (int) packagingData.get("ngQty")
                            + ng);


            if ((int) packagingData.get("ngQty") > 0) {

                packagingData.put(
                        "result",
                        "FAIL");
            }


            // =================================================
            // NG PART DETAIL
            // =================================================

            if (ng > 0) {

                Map<String, Object> ngPart =
                        new LinkedHashMap<>();

                ngPart.put(
                        "partNo",
                        record.getPartNo());

                ngPart.put(
                        "partName",
                        record.getPartName());

                ngPart.put(
                        "vendorCode",
                        record.getVendorCode());

                ngPart.put(
                        "vendorName",
                        record.getVendorName());

                ngPart.put(
                        "model",
                        record.getModel());

                ngPart.put(
                        "ngQty",
                        ng);

                ngPart.put(
                        "packaging",
                        record.getPackagingStatus());

                ngPart.put(
                        "defect",
                        record.getDefectDescription());

                ngPart.put(
                        "inspectionDate",
                        record.getInspectionDate());

                ngParts.add(ngPart);
            }
        }


        // =====================================================
        // OVERALL SUMMARY
        // =====================================================

        Map<String, Object> overallSummary =
                new LinkedHashMap<>();

        overallSummary.put(
                "totalParts",
                overallPartKeys.size());

        overallSummary.put(
                "totalQty",
                totalChecked);

        overallSummary.put(
                "okQty",
                totalOk);

        overallSummary.put(
                "ngQty",
                totalNg);

        overallSummary.put(
                "rejectionPercentage",
                calculatePercentage(
                        totalNg,
                        totalChecked));

        report.put(
                "overallSummary",
                overallSummary);


        // =====================================================
        // MODEL-WISE FINAL VALUES
        // =====================================================

        for (Map.Entry<String, Map<String, Object>> entry
                : modelSummary.entrySet()) {

            String model =
                    entry.getKey();

            Map<String, Object> data =
                    entry.getValue();

            data.put(
                    "totalParts",
                    modelPartKeys
                            .get(model)
                            .size());

            data.put(
                    "rejectionPercentage",
                    calculatePercentage(
                            (int) data.get("ngQty"),
                            (int) data.get("totalQty")));
        }


        // =====================================================
        // PART-WISE FINAL VALUES
        // =====================================================

        for (Map<String, Object> data
                : partSummary.values()) {

            data.put(
                    "rejectionPercentage",
                    calculatePercentage(
                            (int) data.get("ngQty"),
                            (int) data.get("totalQty")));
        }


        // =====================================================
        // FINAL REPORT
        // =====================================================

        report.put(
                "modelWiseSummary",
                new ArrayList<>(
                        modelSummary.values()));

        report.put(
                "modelWiseRejectPercentage",
                createModelRejectPercentage(
                        modelSummary));

        report.put(
                "ngPartsDetail",
                ngParts);

        report.put(
                "defectSummary",
                defectSummary);

        report.put(
                "packagingWiseResult",
                new ArrayList<>(
                        packagingSummary.values()));

        report.put(
                "partWiseSummary",
                new ArrayList<>(
                        partSummary.values()));

        return report;
    }


    // =========================================================
    // MODEL REJECTION LIST
    // =========================================================

    private List<Map<String, Object>>
            createModelRejectPercentage(
                    Map<String, Map<String, Object>>
                            modelSummary) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (Map<String, Object> modelData
                : modelSummary.values()) {

            Map<String, Object> item =
                    new LinkedHashMap<>();

            item.put(
                    "model",
                    modelData.get("model"));

            item.put(
                    "rejectionPercentage",
                    modelData.get(
                            "rejectionPercentage"));

            result.add(item);
        }

        return result;
    }


    // =========================================================
    // PERCENTAGE
    // =========================================================

    private double calculatePercentage(
            int ng,
            int total) {

        if (total == 0) {
            return 0.0;
        }

        return Math.round(
                ((double) ng / total) * 10000.0
        ) / 100.0;
    }


    // =========================================================
    // INTEGER VALUE
    // =========================================================

    private int getValue(Integer value) {

        return value == null
                ? 0
                : value;
    }


    // =========================================================
    // TEXT VALUE
    // =========================================================

    private String getText(String value) {

        if (isBlank(value)) {
            return "UNKNOWN";
        }

        return value.trim();
    }


    // =========================================================
    // NORMALIZE
    // =========================================================

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


    // =========================================================
    // BLANK CHECK
    // =========================================================

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }
}