package com.inspection.backend.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

@Service
public class PdfReportService {

    private final PDType1Font normalFont =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private final PDType1Font boldFont =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public byte[] generateReport(
            Map<String, Object> report) throws IOException {

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            PDPage page =
                    new PDPage(PDRectangle.A4);

            document.addPage(page);

            PDPageContentStream content =
                    new PDPageContentStream(
                            document,
                            page);

            float y = 800;

            // -----------------------------
            // TITLE
            // -----------------------------

            content.beginText();
            content.setFont(boldFont, 18);
            content.newLineAtOffset(40, y);
            content.showText("INSPECTION REPORT");
            content.endText();

            y -= 35;

            // -----------------------------
            // OVERALL SUMMARY
            // -----------------------------

            y = writeHeading(
                    content,
                    "Overall Summary",
                    y);

            Map<String, Object> overall =
                    (Map<String, Object>)
                            report.get("overallSummary");

            y = writeLine(
                    content,
                    "Total Part Groups: "
                            + overall.get("totalParts"),
                    y);

            y = writeLine(
                    content,
                    "Total Quantity: "
                            + overall.get("totalQty"),
                    y);

            y = writeLine(
                    content,
                    "OK Quantity: "
                            + overall.get("okQty"),
                    y);

            y = writeLine(
                    content,
                    "NG Quantity: "
                            + overall.get("ngQty"),
                    y);

            y = writeLine(
                    content,
                    "Rejection %: "
                            + overall.get(
                                    "rejectionPercentage")
                            + "%",
                    y);

            y -= 20;

            // -----------------------------
            // MODEL SUMMARY
            // -----------------------------

            y = writeHeading(
                    content,
                    "Model-wise Summary",
                    y);

            List<Map<String, Object>> models =
                    (List<Map<String, Object>>)
                            report.get("modelWiseSummary");

            for (Map<String, Object> model : models) {

                String line =
                        "Model: "
                        + model.get("model")
                        + " | Parts: "
                        + model.get("totalParts")
                        + " | Qty: "
                        + model.get("totalQty")
                        + " | OK: "
                        + model.get("okQty")
                        + " | NG: "
                        + model.get("ngQty")
                        + " | Rej%: "
                        + model.get(
                                "rejectionPercentage")
                        + "%";

                y = writeLine(
                        content,
                        line,
                        y);

                if (y < 60) {

                    content.close();

                    page =
                            new PDPage(
                                    PDRectangle.A4);

                    document.addPage(page);

                    content =
                            new PDPageContentStream(
                                    document,
                                    page);

                    y = 800;
                }
            }

            y -= 20;

            // -----------------------------
            // NG PART DETAILS
            // -----------------------------

            y = writeHeading(
                    content,
                    "NG Parts Detail",
                    y);

            List<Map<String, Object>> ngParts =
                    (List<Map<String, Object>>)
                            report.get("ngPartsDetail");

            for (Map<String, Object> ngPart
                    : ngParts) {

                String line =
                        "Part No: "
                        + ngPart.get("partNo")
                        + " | Part Name: "
                        + ngPart.get("partName")
                        + " | Vendor: "
                        + ngPart.get("vendorName")
                        + " | NG Qty: "
                        + ngPart.get("ngQty")
                        + " | Defect: "
                        + ngPart.get("defect");

                y = writeLine(
                        content,
                        line,
                        y);

                if (y < 60) {

                    content.close();

                    page =
                            new PDPage(
                                    PDRectangle.A4);

                    document.addPage(page);

                    content =
                            new PDPageContentStream(
                                    document,
                                    page);

                    y = 800;
                }
            }

            y -= 20;

            // -----------------------------
            // DEFECT SUMMARY
            // -----------------------------

            y = writeHeading(
                    content,
                    "Defect Summary",
                    y);

            Map<String, Integer> defects =
                    (Map<String, Integer>)
                            report.get("defectSummary");

            for (Map.Entry<String, Integer> entry
                    : defects.entrySet()) {

                y = writeLine(
                        content,
                        entry.getKey()
                                + " : "
                                + entry.getValue(),
                        y);
            }

            y -= 20;

            // -----------------------------
            // PACKAGING SUMMARY
            // -----------------------------

            y = writeHeading(
                    content,
                    "Packaging-wise Result",
                    y);

            List<Map<String, Object>> packaging =
                    (List<Map<String, Object>>)
                            report.get(
                                    "packagingWiseResult");

            for (Map<String, Object> item
                    : packaging) {

                String line =
                        "Packaging: "
                        + item.get("packaging")
                        + " | Checked: "
                        + item.get("checkedQty")
                        + " | OK: "
                        + item.get("okQty")
                        + " | NG: "
                        + item.get("ngQty")
                        + " | Result: "
                        + item.get("result");

                y = writeLine(
                        content,
                        line,
                        y);

                if (y < 60) {

                    content.close();

                    page =
                            new PDPage(
                                    PDRectangle.A4);

                    document.addPage(page);

                    content =
                            new PDPageContentStream(
                                    document,
                                    page);

                    y = 800;
                }
            }

            y -= 20;

            // -----------------------------
            // PART-WISE SUMMARY
            // -----------------------------

            y = writeHeading(
                    content,
                    "Part-wise Summary",
                    y);

            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>)
                            report.get(
                                    "partWiseSummary");

            for (Map<String, Object> part
                    : parts) {

                String line =
                        "Part No: "
                        + part.get("partNo")
                        + " | "
                        + part.get("partName")
                        + " | Vendor: "
                        + part.get("vendorName")
                        + " | Total: "
                        + part.get("totalQty")
                        + " | OK: "
                        + part.get("okQty")
                        + " | NG: "
                        + part.get("ngQty");

                y = writeLine(
                        content,
                        line,
                        y);

                if (y < 60) {

                    content.close();

                    page =
                            new PDPage(
                                    PDRectangle.A4);

                    document.addPage(page);

                    content =
                            new PDPageContentStream(
                                    document,
                                    page);

                    y = 800;
                }
            }

            content.close();

            document.save(output);

            return output.toByteArray();
        }
    }

    private float writeHeading(
            PDPageContentStream content,
            String text,
            float y) throws IOException {

        content.beginText();
        content.setFont(
                boldFont,
                13);
        content.newLineAtOffset(
                40,
                y);

        content.showText(text);

        content.endText();

        return y - 22;
    }

    private float writeLine(
            PDPageContentStream content,
            String text,
            float y) throws IOException {

        content.beginText();
        content.setFont(
                normalFont,
                9);
        content.newLineAtOffset(
                40,
                y);

        content.showText(
                cleanText(text));

        content.endText();

        return y - 15;
    }

    private String cleanText(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace('\u00A0', ' ')
                .replace('\u2013', '-')
                .replace('\u2014', '-');
    }
}