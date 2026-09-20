package com.inspection.backend.service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.inspection.backend.model.InspectionRecord;

@Service
public class ReportCalculationService {

    public Map<String, Object> calculate(List<InspectionRecord> records) {

        Map<String, Object> report = new LinkedHashMap<>();

        int totalChecked = 0;
        int totalOk = 0;
        int totalNg = 0;

        Set<String> overallPartKeys = new LinkedHashSet<>();

        Map<String, Map<String, Object>> modelSummary =
                new LinkedHashMap<>();

        Map<String, Set<String>> modelPartKeys =
                new LinkedHashMap<>();

        Map<String, Set<String>> modelNgPartKeys =
                new LinkedHashMap<>();

        Map<String, Map<String, Object>> partSummary =
                new LinkedHashMap<>();

        Map<String, Integer> defectSummary =
                new LinkedHashMap<>();

        Map<String, Map<String, Object>> packagingSummary =
                new LinkedHashMap<>();

        List<Map<String, Object>> ngParts =
                new ArrayList<>();

        Map<String, Map<String, Object>> monthlySummary =
                new TreeMap<>();

        Map<String, Set<String>> monthlyPartKeys =
                new TreeMap<>();

        Map<String, Set<String>> monthlyNgPartKeys =
                new TreeMap<>();

        for (InspectionRecord record : records) {

            int checked = getValue(record.getQuantityChecked());
            int ok = getValue(record.getOkQuantity());
            int ng = getValue(record.getNgQuantity());

            totalChecked += checked;
            totalOk += ok;
            totalNg += ng;

            String partKey =
                    normalize(record.getPartNo())
                    + "|" + normalize(record.getVendorCode())
                    + "|" + normalize(record.getModel());

            overallPartKeys.add(partKey);

            String model = getText(record.getModel());

            Map<String, Object> modelData =
                    modelSummary.computeIfAbsent(
                            model,
                            key -> createModelData(key));

            modelPartKeys
                    .computeIfAbsent(
                            model,
                            key -> new LinkedHashSet<>())
                    .add(partKey);

            if (ng > 0) {
                modelNgPartKeys
                        .computeIfAbsent(
                                model,
                                key -> new LinkedHashSet<>())
                        .add(partKey);
            }

            modelData.put(
                    "totalQty",
                    (int) modelData.get("totalQty") + checked);

            modelData.put(
                    "okQty",
                    (int) modelData.get("okQty") + ok);

            modelData.put(
                    "ngQty",
                    (int) modelData.get("ngQty") + ng);

            Map<String, Object> partData =
                    partSummary.computeIfAbsent(
                            partKey,
                            key -> createPartData(record));

            partData.put(
                    "totalQty",
                    (int) partData.get("totalQty") + checked);

            partData.put(
                    "okQty",
                    (int) partData.get("okQty") + ok);

            partData.put(
                    "ngQty",
                    (int) partData.get("ngQty") + ng);

            if (ng > 0 && !isBlank(record.getDefectDescription())) {

                String defect = record.getDefectDescription().trim();

                defectSummary.put(
                        defect,
                        defectSummary.getOrDefault(defect, 0) + ng);
            }

            String packaging = getText(record.getPackagingStatus());

            Map<String, Object> packagingData =
                    packagingSummary.computeIfAbsent(
                            packaging,
                            key -> createPackagingData(key));

            packagingData.put(
                    "checkedQty",
                    (int) packagingData.get("checkedQty") + checked);

            packagingData.put(
                    "okQty",
                    (int) packagingData.get("okQty") + ok);

            packagingData.put(
                    "ngQty",
                    (int) packagingData.get("ngQty") + ng);

            if ((int) packagingData.get("ngQty") > 0) {
                packagingData.put("result", "FAIL");
            }

            if (ng > 0) {

                Map<String, Object> ngPart =
                        new LinkedHashMap<>();

                ngPart.put("partNo", record.getPartNo());
                ngPart.put("partName", record.getPartName());
                ngPart.put("vendorCode", record.getVendorCode());
                ngPart.put("vendorName", record.getVendorName());
                ngPart.put("model", record.getModel());
                ngPart.put("ngQty", ng);
                ngPart.put("packaging", record.getPackagingStatus());
                ngPart.put("defect", record.getDefectDescription());
                ngPart.put("inspectionDate", record.getInspectionDate());

                ngParts.add(ngPart);
            }

            if (record.getInspectionDate() != null) {

                String month =
                        YearMonth.from(record.getInspectionDate()).toString();

                Map<String, Object> monthData =
                        monthlySummary.computeIfAbsent(
                                month,
                                key -> createMonthlyData(key));

                monthlyPartKeys
                        .computeIfAbsent(
                                month,
                                key -> new LinkedHashSet<>())
                        .add(partKey);

                if (ng > 0) {
                    monthlyNgPartKeys
                            .computeIfAbsent(
                                    month,
                                    key -> new LinkedHashSet<>())
                            .add(partKey);
                }

                monthData.put(
                        "totalQty",
                        (int) monthData.get("totalQty") + checked);

                monthData.put(
                        "okQty",
                        (int) monthData.get("okQty") + ok);

                monthData.put(
                        "ngQty",
                        (int) monthData.get("ngQty") + ng);
            }
        }

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
                calculatePercentage(totalNg, totalChecked));

        report.put("overallSummary", overallSummary);

        for (Map.Entry<String, Map<String, Object>> entry
                : modelSummary.entrySet()) {

            String model = entry.getKey();
            Map<String, Object> data = entry.getValue();

            int totalParts =
                    modelPartKeys.get(model).size();

            int ngPartsCount =
                    modelNgPartKeys
                            .getOrDefault(model, new LinkedHashSet<>())
                            .size();

            data.put("totalParts", totalParts);

            data.put(
                    "rejectionPercentage",
                    calculatePercentage(
                            (int) data.get("ngQty"),
                            (int) data.get("totalQty")));

            data.put(
                    "defectPercentage",
                    calculatePercentage(
                            ngPartsCount,
                            totalParts));
        }

        for (Map<String, Object> data : partSummary.values()) {

            data.put(
                    "rejectionPercentage",
                    calculatePercentage(
                            (int) data.get("ngQty"),
                            (int) data.get("totalQty")));
        }

        for (Map.Entry<String, Map<String, Object>> entry
                : monthlySummary.entrySet()) {

            String month = entry.getKey();
            Map<String, Object> data = entry.getValue();

            int totalParts =
                    monthlyPartKeys.get(month).size();

            int ngPartsCount =
                    monthlyNgPartKeys
                            .getOrDefault(month, new LinkedHashSet<>())
                            .size();

            data.put("totalParts", totalParts);
            data.put("ngParts", ngPartsCount);
            data.put("okParts", totalParts - ngPartsCount);

            data.put(
                    "rejectionPercentage",
                    calculatePercentage(
                            (int) data.get("ngQty"),
                            (int) data.get("totalQty")));
        }

        report.put(
                "modelWiseSummary",
                new ArrayList<>(modelSummary.values()));

        report.put(
                "modelWiseRejectPercentage",
                createModelRejectPercentage(modelSummary));

        report.put(
                "monthlySummary",
                new ArrayList<>(monthlySummary.values()));

        report.put("ngPartsDetail", ngParts);
        report.put("defectSummary", defectSummary);

        report.put(
                "packagingWiseResult",
                new ArrayList<>(packagingSummary.values()));

        report.put(
                "partWiseSummary",
                new ArrayList<>(partSummary.values()));

        return report;
    }

    private Map<String, Object> createModelData(String model) {

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("model", model);
        data.put("totalParts", 0);
        data.put("totalQty", 0);
        data.put("okQty", 0);
        data.put("ngQty", 0);
        data.put("rejectionPercentage", 0.0);
        data.put("defectPercentage", 0.0);

        return data;
    }

    private Map<String, Object> createPartData(
            InspectionRecord record) {

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("partNo", record.getPartNo());
        data.put("partName", record.getPartName());
        data.put("vendorCode", record.getVendorCode());
        data.put("vendorName", record.getVendorName());
        data.put("model", record.getModel());
        data.put("totalQty", 0);
        data.put("okQty", 0);
        data.put("ngQty", 0);
        data.put("rejectionPercentage", 0.0);

        return data;
    }

    private Map<String, Object> createPackagingData(
            String packaging) {

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("packaging", packaging);
        data.put("checkedQty", 0);
        data.put("okQty", 0);
        data.put("ngQty", 0);
        data.put("result", "PASS");

        return data;
    }

    private Map<String, Object> createMonthlyData(
            String month) {

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("month", month);
        data.put("totalParts", 0);
        data.put("okParts", 0);
        data.put("ngParts", 0);
        data.put("totalQty", 0);
        data.put("okQty", 0);
        data.put("ngQty", 0);
        data.put("rejectionPercentage", 0.0);

        return data;
    }

    private List<Map<String, Object>>
            createModelRejectPercentage(
                    Map<String, Map<String, Object>> modelSummary) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (Map<String, Object> modelData
                : modelSummary.values()) {

            Map<String, Object> item =
                    new LinkedHashMap<>();

            item.put("model", modelData.get("model"));
            item.put(
                    "rejectionPercentage",
                    modelData.get("rejectionPercentage"));
            item.put(
                    "defectPercentage",
                    modelData.get("defectPercentage"));

            result.add(item);
        }

        return result;
    }

    private double calculatePercentage(
            int value,
            int total) {

        if (total == 0) {
            return 0.0;
        }

        return Math.round(
                ((double) value / total) * 10000.0) / 100.0;
    }

    private int getValue(Integer value) {
        return value == null ? 0 : value;
    }

    private String getText(String value) {
        return isBlank(value) ? "UNKNOWN" : value.trim();
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
