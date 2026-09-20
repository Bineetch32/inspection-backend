package com.inspection.backend.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

    private static final float MARGIN = 40;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);
    private static final float BOTTOM = 48;

    private final PDType1Font normalFont =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private final PDType1Font boldFont =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public byte[] generateReport(Map<String, Object> report)
            throws IOException {

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PdfCanvas canvas = new PdfCanvas(document);

            drawTitle(canvas, report);
            drawOverallSummary(canvas, report);
            drawModelSummary(canvas, report);

            drawNgParts(canvas, report);
            drawDefectSummary(canvas, report);
            drawPackagingSummary(canvas, report);
            drawPartSummary(canvas, report);

            canvas.close();

            document.save(output);
            return output.toByteArray();
        }
    }

    private void drawTitle(PdfCanvas canvas, Map<String, Object> report)
            throws IOException {

        canvas.ensureSpace(150);

        canvas.fillRect(
                MARGIN,
                canvas.y - 70,
                CONTENT_WIDTH,
                70,
                0.08f,
                0.24f,
                0.55f);

        canvas.text(
                "INSPECTION REPORT",
                MARGIN + 18,
                canvas.y - 28,
                boldFont,
                20,
                1f,
                1f,
                1f);

        canvas.text(
                clean(report.get("reportType")),
                MARGIN + 18,
                canvas.y - 47,
                normalFont,
                9,
                0.85f,
                0.92f,
                1f);

        canvas.text(
                "Generated: " + clean(report.get("generatedAt")),
                PAGE_WIDTH - MARGIN - 160,
                canvas.y - 47,
                normalFont,
                8,
                0.85f,
                0.92f,
                1f);

        canvas.y -= 86;

        canvas.sectionTitle("OVERALL INSPECTION SUMMARY");
    }

    private void drawOverallSummary(
            PdfCanvas canvas,
            Map<String, Object> report) throws IOException {

        Map<String, Object> overall =
                map(report.get("overallSummary"));

        float gap = 8;
        float cardWidth =
                (CONTENT_WIDTH - (gap * 4)) / 5f;

        String[] labels = {
                "PART GROUPS",
                "CHECKED QTY",
                "OK QTY",
                "NG QTY",
                "REJECTION"
        };

        String[] values = {
                clean(overall.get("totalParts")),
                clean(overall.get("totalQty")),
                clean(overall.get("okQty")),
                clean(overall.get("ngQty")),
                clean(overall.get("rejectionPercentage")) + "%"
        };

        for (int i = 0; i < labels.length; i++) {

            float x = MARGIN + i * (cardWidth + gap);

            float r = 0.96f;
            float g = 0.97f;
            float b = 0.99f;

            if (i == 2) {
                r = 0.93f;
                g = 0.98f;
                b = 0.95f;
            }

            if (i == 3 || i == 4) {
                r = 1f;
                g = 0.95f;
                b = 0.95f;
            }

            canvas.fillRect(
                    x,
                    canvas.y - 65,
                    cardWidth,
                    65,
                    r,
                    g,
                    b);

            canvas.text(
                    labels[i],
                    x + 8,
                    canvas.y - 17,
                    boldFont,
                    7,
                    0.35f,
                    0.42f,
                    0.55f);

            canvas.text(
                    values[i],
                    x + 8,
                    canvas.y - 40,
                    boldFont,
                    16,
                    i == 2 ? 0.08f : (i >= 3 ? 0.78f : 0.08f),
                    i == 2 ? 0.55f : (i >= 3 ? 0.15f : 0.24f),
                    i == 2 ? 0.25f : (i >= 3 ? 0.12f : 0.55f));
        }

        canvas.y -= 82;

        canvas.sectionTitle("OK VS NG - QUANTITY");

        int totalQty = intValue(overall.get("totalQty"));
        int okQty = intValue(overall.get("okQty"));
        int ngQty = intValue(overall.get("ngQty"));

        float barWidth = CONTENT_WIDTH - 100;
        float okWidth = totalQty == 0 ? 0 : barWidth * okQty / totalQty;
        float ngWidth = barWidth - okWidth;

        canvas.fillRect(
                MARGIN + 50,
                canvas.y - 18,
                barWidth,
                18,
                0.93f,
                0.95f,
                0.97f);

        if (okWidth > 0) {
            canvas.fillRect(
                    MARGIN + 50,
                    canvas.y - 18,
                    okWidth,
                    18,
                    0.12f,
                    0.62f,
                    0.34f);
        }

        if (ngWidth > 0) {
            canvas.fillRect(
                    MARGIN + 50 + okWidth,
                    canvas.y - 18,
                    ngWidth,
                    18,
                    0.86f,
                    0.18f,
                    0.18f);
        }

        canvas.text(
                "OK: " + okQty,
                MARGIN,
                canvas.y - 14,
                boldFont,
                8,
                0.12f,
                0.62f,
                0.34f);

        canvas.text(
                "NG: " + ngQty,
                PAGE_WIDTH - MARGIN - 45,
                canvas.y - 14,
                boldFont,
                8,
                0.86f,
                0.18f,
                0.18f);

        canvas.y -= 38;
    }

    private void drawModelSummary(
            PdfCanvas canvas,
            Map<String, Object> report) throws IOException {

        canvas.sectionTitle("MODEL-WISE SUMMARY");

        List<Map<String, Object>> models =
                list(report.get("modelWiseSummary"));

        List<String[]> rows = new ArrayList<>();

        for (Map<String, Object> model : models) {
            rows.add(new String[] {
                    clean(model.get("model")),
                    clean(model.get("totalParts")),
                    clean(model.get("totalQty")),
                    clean(model.get("okQty")),
                    clean(model.get("ngQty")),
                    clean(model.get("rejectionPercentage")) + "%"
            });
        }

        canvas.table(
                new String[] {
                        "Model",
                        "Parts",
                        "Checked",
                        "OK",
                        "NG",
                        "Reject %"
                },
                rows,
                new float[] {
                        155, 60, 75, 55, 55, 75
                });
    }

    private void drawNgParts(
            PdfCanvas canvas,
            Map<String, Object> report) throws IOException {

        canvas.sectionTitle("NG PARTS - DETAIL");

        List<Map<String, Object>> ngParts =
                list(report.get("ngPartsDetail"));

        List<String[]> rows = new ArrayList<>();

        for (Map<String, Object> item : ngParts) {
            rows.add(new String[] {
                    clean(item.get("partNo")),
                    clean(item.get("partName")),
                    clean(item.get("vendorName")),
                    clean(item.get("ngQty")),
                    clean(item.get("packaging")),
                    clean(item.get("defect"))
            });
        }

        if (rows.isEmpty()) {
            canvas.note("No NG parts recorded.");
            return;
        }

        canvas.table(
                new String[] {
                        "Part No",
                        "Part Name",
                        "Vendor",
                        "NG Qty",
                        "Packaging",
                        "Defect"
                },
                rows,
                new float[] {
                        92, 112, 122, 45, 62, 62
                });
    }

    private void drawDefectSummary(
            PdfCanvas canvas,
            Map<String, Object> report) throws IOException {

        canvas.sectionTitle("DEFECT SUMMARY");

        Map<String, Object> defects =
                map(report.get("defectSummary"));

        List<String[]> rows = new ArrayList<>();

        for (Map.Entry<String, Object> entry : defects.entrySet()) {
            rows.add(new String[] {
                    clean(entry.getKey()),
                    clean(entry.getValue())
            });
        }

        if (rows.isEmpty()) {
            canvas.note("No defects recorded.");
            return;
        }

        canvas.table(
                new String[] {
                        "Defect",
                        "NG Quantity"
                },
                rows,
                new float[] {
                        400, 95
                });
    }

    private void drawPackagingSummary(
            PdfCanvas canvas,
            Map<String, Object> report) throws IOException {

        canvas.sectionTitle("PACKAGING-WISE RESULT");

        List<Map<String, Object>> packaging =
                list(report.get("packagingWiseResult"));

        List<String[]> rows = new ArrayList<>();

        for (Map<String, Object> item : packaging) {
            rows.add(new String[] {
                    clean(item.get("packaging")),
                    clean(item.get("checkedQty")),
                    clean(item.get("okQty")),
                    clean(item.get("ngQty")),
                    clean(item.get("result"))
            });
        }

        canvas.table(
                new String[] {
                        "Packaging",
                        "Checked",
                        "OK",
                        "NG",
                        "Result"
                },
                rows,
                new float[] {
                        245, 75, 65, 65, 45
                });
    }

    private void drawPartSummary(
            PdfCanvas canvas,
            Map<String, Object> report) throws IOException {

        canvas.sectionTitle("PART-WISE SUMMARY");

        List<Map<String, Object>> parts =
                list(report.get("partWiseSummary"));

        List<String[]> rows = new ArrayList<>();

        for (Map<String, Object> part : parts) {
            rows.add(new String[] {
                    clean(part.get("partNo")),
                    clean(part.get("partName")),
                    clean(part.get("vendorName")),
                    clean(part.get("totalQty")),
                    clean(part.get("okQty")),
                    clean(part.get("ngQty")),
                    clean(part.get("rejectionPercentage")) + "%"
            });
        }

        canvas.table(
                new String[] {
                        "Part No",
                        "Part Name",
                        "Vendor",
                        "Total",
                        "OK",
                        "NG",
                        "Reject %"
                },
                rows,
                new float[] {
                        86, 118, 145, 42, 42, 42, 40
                });
    }

    private Map<String, Object> map(Object value) {
        if (value instanceof Map<?, ?> source) {
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : source.entrySet()) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return result;
        }
        return new java.util.LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> list(Object value) {
        if (value instanceof List<?>) {
            return (List<Map<String, Object>>) value;
        }
        return new ArrayList<>();
    }

    private int intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(clean(value));
        } catch (Exception e) {
            return 0;
        }
    }

    private String clean(Object value) {
        if (value == null) {
            return "";
        }

        return String.valueOf(value)
                .replace('\u00A0', ' ')
                .replace('\u2013', '-')
                .replace('\u2014', '-')
                .replace('\n', ' ')
                .trim();
    }

    private class PdfCanvas {

        private final PDDocument document;
        private PDPage page;
        private PDPageContentStream content;
        private float y;
        private int pageNumber = 0;

        PdfCanvas(PDDocument document) throws IOException {
            this.document = document;
            newPage();
        }

        void newPage() throws IOException {

            if (content != null) {
                drawFooter();
                content.close();
            }

            page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            content = new PDPageContentStream(
                    document,
                    page);

            pageNumber++;
            y = PAGE_HEIGHT - MARGIN;

            drawTopLine();
        }

        void close() throws IOException {
            drawFooter();
            content.close();
        }

        void ensureSpace(float required) throws IOException {
            if (y - required < BOTTOM) {
                newPage();
            }
        }

        void sectionTitle(String title) throws IOException {

            ensureSpace(42);

            fillRect(
                    MARGIN,
                    y - 24,
                    CONTENT_WIDTH,
                    24,
                    0.92f,
                    0.95f,
                    1f);

            text(
                    title,
                    MARGIN + 9,
                    y - 16,
                    boldFont,
                    9,
                    0.08f,
                    0.24f,
                    0.55f);

            y -= 34;
        }

        void note(String value) throws IOException {

            ensureSpace(28);

            text(
                    value,
                    MARGIN + 5,
                    y - 12,
                    normalFont,
                    9,
                    0.40f,
                    0.45f,
                    0.52f);

            y -= 24;
        }

        void table(
                String[] headers,
                List<String[]> rows,
                float[] widths) throws IOException {

            float headerHeight = 24;
            float rowHeight = 30;

            ensureSpace(headerHeight + rowHeight);

            drawTableHeader(headers, widths, headerHeight);

            for (String[] row : rows) {

                ensureSpace(rowHeight);

                drawTableRow(
                        row,
                        widths,
                        rowHeight);
            }

            y -= 10;
        }

        private void drawTableHeader(
                String[] headers,
                float[] widths,
                float height) throws IOException {

            float x = MARGIN;

            fillRect(
                    x,
                    y - height,
                    CONTENT_WIDTH,
                    height,
                    0.08f,
                    0.24f,
                    0.55f);

            for (int i = 0; i < headers.length; i++) {

                text(
                        headers[i],
                        x + 5,
                        y - 16,
                        boldFont,
                        7,
                        1f,
                        1f,
                        1f);

                x += widths[i];
            }

            y -= height;
        }

        private void drawTableRow(
                String[] row,
                float[] widths,
                float height) throws IOException {

            float x = MARGIN;

            fillRect(
                    x,
                    y - height,
                    CONTENT_WIDTH,
                    height,
                    0.985f,
                    0.987f,
                    0.992f);

            for (int i = 0; i < row.length; i++) {

                drawCellText(
                        row[i],
                        x + 5,
                        y - 11,
                        widths[i] - 10);

                x += widths[i];
            }

            strokeRect(
                    MARGIN,
                    y - height,
                    CONTENT_WIDTH,
                    height,
                    0.86f,
                    0.88f,
                    0.92f);

            y -= height;
        }

        private void drawCellText(
                String value,
                float x,
                float topY,
                float maxWidth) throws IOException {

            String[] lines =
                    wrap(value, normalFont, 7, maxWidth);

            for (int i = 0; i < Math.min(lines.length, 2); i++) {

                text(
                        lines[i],
                        x,
                        topY - (i * 9),
                        normalFont,
                        7,
                        0.18f,
                        0.22f,
                        0.30f);
            }
        }

        private String[] wrap(
                String value,
                PDType1Font font,
                float size,
                float maxWidth) throws IOException {

            if (value == null || value.isBlank()) {
                return new String[] { "" };
            }

            String[] words = value.split("\\s+");
            List<String> lines = new ArrayList<>();
            String current = "";

            for (String word : words) {

                String candidate =
                        current.isEmpty()
                                ? word
                                : current + " " + word;

                if (font.getStringWidth(candidate) / 1000f * size
                        <= maxWidth) {

                    current = candidate;

                } else {

                    if (!current.isEmpty()) {
                        lines.add(current);
                    }

                    current = word;
                }
            }

            if (!current.isEmpty()) {
                lines.add(current);
            }

            if (lines.size() > 2) {
                String second = lines.get(1);

                while (font.getStringWidth(second + "...")
                        / 1000f * size > maxWidth
                        && second.length() > 3) {

                    second =
                            second.substring(
                                    0,
                                    second.length() - 1);
                }

                lines.set(1, second + "...");
                return new String[] {
                        lines.get(0),
                        lines.get(1)
                };
            }

            return lines.toArray(new String[0]);
        }

        void text(
                String value,
                float x,
                float y,
                PDType1Font font,
                float size,
                float r,
                float g,
                float b) throws IOException {

            content.beginText();
            content.setFont(font, size);
            content.setNonStrokingColor(r, g, b);
            content.newLineAtOffset(x, y);
            content.showText(clean(value));
            content.endText();
        }

        void fillRect(
                float x,
                float y,
                float width,
                float height,
                float r,
                float g,
                float b) throws IOException {

            content.setNonStrokingColor(r, g, b);
            content.addRect(x, y, width, height);
            content.fill();
        }

        void strokeRect(
                float x,
                float y,
                float width,
                float height,
                float r,
                float g,
                float b) throws IOException {

            content.setStrokingColor(r, g, b);
            content.addRect(x, y, width, height);
            content.stroke();
        }

        private void drawTopLine() throws IOException {

            content.setStrokingColor(
                    0.08f,
                    0.24f,
                    0.55f);

            content.setLineWidth(2);
            content.moveTo(MARGIN, PAGE_HEIGHT - 28);
            content.lineTo(
                    PAGE_WIDTH - MARGIN,
                    PAGE_HEIGHT - 28);
            content.stroke();
        }

        private void drawFooter() throws IOException {

            content.setStrokingColor(
                    0.86f,
                    0.88f,
                    0.92f);

            content.setLineWidth(0.6f);
            content.moveTo(
                    MARGIN,
                    32);
            content.lineTo(
                    PAGE_WIDTH - MARGIN,
                    32);
            content.stroke();

            text(
                    "Inspection Management System",
                    MARGIN,
                    20,
                    normalFont,
                    7,
                    0.45f,
                    0.49f,
                    0.55f);

            text(
                    "Page " + pageNumber,
                    PAGE_WIDTH - MARGIN - 45,
                    20,
                    normalFont,
                    7,
                    0.45f,
                    0.49f,
                    0.55f);
        }
    }
}
