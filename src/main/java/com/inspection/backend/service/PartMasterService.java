
package com.inspection.backend.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public Map<String, Object> importExcel(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select an Excel file.");
        }

        String name = file.getOriginalFilename() == null
                ? ""
                : file.getOriginalFilename().toLowerCase();

        if (!name.endsWith(".xlsx") && !name.endsWith(".xls")) {
            throw new IllegalArgumentException(
                    "Only .xlsx or .xls files are supported.");
        }

        List<Map<String, Object>> errors = new ArrayList<>();
        List<PartMaster> toSave = new ArrayList<>();
        Set<String> filePartNumbers = new HashSet<>();
        Set<String> existingPartNumbers = new HashSet<>();

        for (PartMaster part : getAllParts()) {
            if (!isBlank(part.getPartNo())) {
                existingPartNumbers.add(normalizeKey(part.getPartNo()));
            }
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("Excel file has no sheet.");
            }

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() < 2) {
                throw new IllegalArgumentException(
                        "Excel must contain header row and at least one data row.");
            }

            Row headerRow = sheet.getRow(0);

            Map<String, Integer> columns = readHeaders(headerRow);

            String[] requiredHeaders = {
                    "part no",
                    "part name",
                    "vendor code",
                    "vendor name",
                    "model",
                    "packaging"
            };

            for (String required : requiredHeaders) {
                if (!columns.containsKey(required)) {
                    throw new IllegalArgumentException(
                            "Missing required column: " + required);
                }
            }

            int totalRows = 0;
            int addedRows = 0;
            int skippedRows = 0;

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                totalRows++;
                int excelRow = rowIndex + 1;

                String partNo = cellValue(row, columns.get("part no"));
                String partName = cellValue(row, columns.get("part name"));
                String vendorCode = cellValue(row, columns.get("vendor code"));
                String vendorName = cellValue(row, columns.get("vendor name"));
                String model = cellValue(row, columns.get("model"));
                String packaging = cellValue(row, columns.get("packaging"));
                String activeText = columns.containsKey("active")
                        ? cellValue(row, columns.get("active"))
                        : "";

                List<String> rowErrors = new ArrayList<>();

                if (isBlank(partNo)) rowErrors.add("Part No is required.");
                if (isBlank(partName)) rowErrors.add("Part Name is required.");
                if (isBlank(vendorCode)) rowErrors.add("Vendor Code is required.");
                if (isBlank(vendorName)) rowErrors.add("Vendor Name is required.");
                if (isBlank(model)) rowErrors.add("Model is required.");
                if (isBlank(packaging)) rowErrors.add("Packaging is required.");

                boolean active = true;

                if (!isBlank(activeText)) {
                    String activeKey = activeText.trim().toLowerCase();

                    if (activeKey.equals("true")
                            || activeKey.equals("yes")
                            || activeKey.equals("1")
                            || activeKey.equals("active")) {
                        active = true;
                    } else if (activeKey.equals("false")
                            || activeKey.equals("no")
                            || activeKey.equals("0")
                            || activeKey.equals("inactive")) {
                        active = false;
                    } else {
                        rowErrors.add(
                                "Active must be TRUE/FALSE, YES/NO, 1/0 or ACTIVE/INACTIVE.");
                    }
                }

                String partNoKey = normalizeKey(partNo);

                if (!isBlank(partNo) && existingPartNumbers.contains(partNoKey)) {
                    rowErrors.add("Part No already exists.");
                }

                if (!isBlank(partNo) && filePartNumbers.contains(partNoKey)) {
                    rowErrors.add("Duplicate Part No in uploaded file.");
                }

                if (!rowErrors.isEmpty()) {

                    Map<String, Object> error = new LinkedHashMap<>();
                    error.put("row", excelRow);
                    error.put("partNo", partNo);
                    error.put("message", String.join(" ", rowErrors));

                    errors.add(error);
                    skippedRows++;
                    continue;
                }

                PartMaster part = new PartMaster();
                part.setPartNo(partNo.trim());
                part.setPartName(partName.trim());
                part.setVendorCode(vendorCode.trim());
                part.setVendorName(vendorName.trim());
                part.setModel(model.trim());
                part.setPackaging(packaging.trim());
                part.setActive(active);

                toSave.add(part);
                filePartNumbers.add(partNoKey);
                existingPartNumbers.add(partNoKey);
                addedRows++;
            }

            if (!toSave.isEmpty()) {
                partMasterRepository.saveAll(toSave);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("totalRows", totalRows);
            result.put("addedRows", addedRows);
            result.put("skippedRows", skippedRows);
            result.put("errors", errors);

            return result;
        }
    }

    public byte[] createExcelTemplate() throws IOException {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Part Master");

            String[] headers = {
                    "Part No",
                    "Part Name",
                    "Vendor Code",
                    "Vendor Name",
                    "Model",
                    "Packaging",
                    "Active"
            };

            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(output);
            return output.toByteArray();
        }
    }

    private Map<String, Integer> readHeaders(Row headerRow) {

        if (headerRow == null) {
            throw new IllegalArgumentException("Excel header row is missing.");
        }

        Map<String, Integer> columns = new LinkedHashMap<>();

        for (Cell cell : headerRow) {
            String header = normalizeHeader(cellValue(cell));

            if (!header.isEmpty()) {
                columns.put(header, cell.getColumnIndex());
            }
        }

        return columns;
    }

    private String cellValue(Row row, Integer index) {

        if (index == null) {
            return "";
        }

        Cell cell = row.getCell(index);

        return cellValue(cell);
    }

    private String cellValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        if (cell.getCellType() == CellType.FORMULA) {
            return cell.toString().trim();
        }

        return cell.toString().trim();
    }

    private boolean isEmptyRow(Row row) {

        for (Cell cell : row) {
            if (cell != null && !cell.toString().trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private String normalizeHeader(String value) {

        return value == null
                ? ""
                : value.trim()
                        .toLowerCase()
                        .replaceAll("\s+", " ");
    }

    private String normalizeKey(String value) {

        return value == null
                ? ""
                : value.trim().replaceAll("\s+", " ").toLowerCase();
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
