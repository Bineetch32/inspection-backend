package com.inspection.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.inspection.backend.model.InspectionRecord;

@Service
public class ExcelService {

    private static final int HEADER_ROW = 1;

    private static final String[] EXPECTED_HEADERS = {
            "SR No.",
            "Inspection Date",
            "Part No",
            "Part Name",
            "Vendor code",
            "Vendor Name",
            "Model",
            "Quantity checked",
            "OK Quantity",
            "NG Quantity",
            "Inspection status(OK/NG)",
            "Defect Description(If NG)",
            "Defect Photo",
            "Packaging status",
            "Packaging Photo",
            "Checked by",
            "Checked By",
            "Remarks"
    };

    public List<InspectionRecord> readInspectionData(
            MultipartFile file) throws IOException {

        List<InspectionRecord> records = new ArrayList<>();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload an Excel file.");
        }

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            DataFormatter formatter = new DataFormatter();

            boolean inspectionSheetFound = false;

            for (Sheet sheet : workbook) {

                if ("Summary".equalsIgnoreCase(
                        sheet.getSheetName())) {
                    continue;
                }

                inspectionSheetFound = true;

                validateHeaders(sheet, formatter);

                for (int rowIndex = HEADER_ROW + 1;
                     rowIndex <= sheet.getLastRowNum();
                     rowIndex++) {

                    Row row = sheet.getRow(rowIndex);

                    if (row == null
                            || isEmptyRow(row, formatter)) {
                        continue;
                    }

                    InspectionRecord record =
                            new InspectionRecord();

                    record.setSrNo(
                            getInteger(row, 0, formatter));

                    record.setInspectionDate(
                            getDate(row, 1));

                    record.setPartNo(
                            getString(row, 2, formatter));

                    record.setPartName(
                            getString(row, 3, formatter));

                    record.setVendorCode(
                            getString(row, 4, formatter));

                    record.setVendorName(
                            getString(row, 5, formatter));

                    record.setModel(
                            getString(row, 6, formatter));

                    record.setQuantityChecked(
                            getInteger(row, 7, formatter));

                    record.setOkQuantity(
                            getIntegerOrZero(row, 8, formatter));

                    record.setNgQuantity(
                            getIntegerOrZero(row, 9, formatter));

                    record.setInspectionStatus(
                            getString(row, 10, formatter));

                    record.setDefectDescription(
                            getString(row, 11, formatter));

                    record.setDefectPhoto(
                            getString(row, 12, formatter));

                    record.setPackagingStatus(
                            getString(row, 13, formatter));

                    record.setPackagingPhoto(
                            getString(row, 14, formatter));

                    record.setCheckedBy(
                            getString(row, 15, formatter));

                    record.setVerifiedBy(
                            getString(row, 16, formatter));

                    record.setRemarks(
                            getString(row, 17, formatter));

                    record.setSheetName(
                            sheet.getSheetName());

                    record.setExcelRowNumber(
                            rowIndex + 1);

                    records.add(record);
                }
            }

            if (!inspectionSheetFound) {
                throw new IllegalArgumentException(
                        "Invalid inspection Excel: no inspection data sheet was found.");
            }

            if (records.isEmpty()) {
                throw new IllegalArgumentException(
                        "Invalid inspection Excel: no inspection records were found.");
            }
        }

        return records;
    }

    private void validateHeaders(
            Sheet sheet,
            DataFormatter formatter) {

        Row headerRow = sheet.getRow(HEADER_ROW);

        if (headerRow == null) {
            throw new IllegalArgumentException(
                    "Invalid inspection Excel: headers are missing in sheet '"
                    + sheet.getSheetName() + "'.");
        }

        for (int i = 0; i < EXPECTED_HEADERS.length; i++) {

            String actual = formatter.formatCellValue(
                    headerRow.getCell(i));

            String expected = EXPECTED_HEADERS[i];

            if (!normalize(actual).equals(normalize(expected))) {

                throw new IllegalArgumentException(
                        "Invalid inspection Excel format in sheet '"
                        + sheet.getSheetName()
                        + "'. Expected column '"
                        + expected
                        + "' at position "
                        + (i + 1)
                        + " but found '"
                        + actual
                        + "'.");
            }
        }
    }

    private String getString(
            Row row,
            int columnIndex,
            DataFormatter formatter) {

        Cell cell = row.getCell(columnIndex);

        if (cell == null) {
            return null;
        }

        String value = formatter.formatCellValue(cell);

        return value == null || value.trim().isEmpty()
                ? null
                : value.trim();
    }

    private Integer getInteger(
            Row row,
            int columnIndex,
            DataFormatter formatter) {

        String value =
                getString(row, columnIndex, formatter);

        if (value == null) {
            return null;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid number at row "
                    + (row.getRowNum() + 1)
                    + ", column "
                    + (columnIndex + 1)
                    + ": " + value);
        }
    }

    private Integer getIntegerOrZero(
            Row row,
            int columnIndex,
            DataFormatter formatter) {

        Integer value =
                getInteger(
                        row,
                        columnIndex,
                        formatter);

        return value == null ? 0 : value;
    }

    private LocalDate getDate(
            Row row,
            int columnIndex) {

        Cell cell =
                row.getCell(columnIndex);

        if (cell == null
                || cell.getCellType()
                == CellType.BLANK) {

            return null;
        }

        if (cell.getCellType()
                == CellType.NUMERIC
                && org.apache.poi.ss.usermodel.DateUtil
                        .isCellDateFormatted(cell)) {

            return cell.getDateCellValue()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        throw new IllegalArgumentException(
                "Invalid inspection date at row "
                + (row.getRowNum() + 1));
    }

    private boolean isEmptyRow(
            Row row,
            DataFormatter formatter) {

        for (Cell cell : row) {

            if (!formatter.formatCellValue(cell)
                    .trim()
                    .isEmpty()) {

                return false;
            }
        }

        return true;
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
