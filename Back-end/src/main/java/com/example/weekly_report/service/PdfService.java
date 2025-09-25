package com.example.weekly_report.service;

import com.example.weekly_report.entity.WeeklyReport;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfTemplate;
import java.awt.Color;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    public byte[] generateReportPdf(WeeklyReport report) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // A4 portrait with generous margins for printing
            Document document = new Document(PageSize.A4, 48, 48, 64, 64);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            // Page numbers
            writer.setPageEvent(new FooterPageXofY());
            document.open();

            // Typography (Times New Roman-like)
            Font titleFont = new Font(Font.TIMES_ROMAN, 18, Font.BOLD);
            Font headerFont = new Font(Font.TIMES_ROMAN, 16, Font.BOLD);
            Font sectionFont = new Font(Font.TIMES_ROMAN, 13, Font.BOLD);
            Font labelFont = new Font(Font.TIMES_ROMAN, 11, Font.BOLD);
            Font normalFont = new Font(Font.TIMES_ROMAN, 11, Font.NORMAL);

            // Header bar (no logo) - draw with PdfContentByte to avoid element issues
            PdfContentByte cbHeader = writer.getDirectContentUnder();
            cbHeader.saveState();
            cbHeader.setColorFill(new Color(59, 130, 246));
            cbHeader.rectangle(document.left(), document.top() + 10, document.right() - document.left(), 10);
            cbHeader.fill();
            cbHeader.restoreState();

            // Header: Company/Organization (centered), Title, Reference, Created date, Author
            Paragraph org = new Paragraph(companyName(report), headerFont);
            org.setAlignment(Element.ALIGN_CENTER);
            org.setSpacingBefore(8);
            document.add(org);

            String weekTitleLeft = report.getWeekStartDate() != null
                    ? report.getWeekStartDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                    : "Unknown";
            Paragraph pTitle = new Paragraph("Weekly Report - Week of " + weekTitleLeft, titleFont);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            pTitle.setSpacingBefore(8);
            pTitle.setSpacingAfter(6);
            document.add(pTitle);

            DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy");
            String week = String.format("%s → %s",
                    report.getWeekStartDate() != null ? df.format(report.getWeekStartDate()) : "-",
                    report.getWeekEndDate() != null ? df.format(report.getWeekEndDate()) : "-");
            String submittedBy = "Prepared by: ";
            String employeeNameLine = "-";
            if (report.getEmployee() != null) {
                try {
                    String fn = report.getEmployee().getFullName();
                    if (fn != null && !fn.isBlank()) employeeNameLine = fn;
                } catch (Exception ignored) {}
                if ("-".equals(employeeNameLine) && report.getEmployee().getUserAccount() != null) {
                    String u = report.getEmployee().getUserAccount().getUsername();
                    if (u != null && !u.isBlank()) employeeNameLine = u;
                }
            }
            submittedBy += employeeNameLine;

            String supervisorLine = "Assigned Supervisor: ";
            String supervisorNameLine = "-";
            if (report.getEmployee() != null && report.getEmployee().getSupervisor() != null) {
                try {
                    String sfn = report.getEmployee().getSupervisor().getFullName();
                    if (sfn != null && !sfn.isBlank()) supervisorNameLine = sfn;
                } catch (Exception ignored) {}
                if ("-".equals(supervisorNameLine) && report.getEmployee().getSupervisor().getUserAccount() != null) {
                    String su = report.getEmployee().getSupervisor().getUserAccount().getUsername();
                    if (su != null && !su.isBlank()) supervisorNameLine = su;
                }
            }
            supervisorLine += supervisorNameLine;

            String reference = "Reference #: " + (report.getId() != null ? report.getId() : "—");
            String createdLine = "Created: " + datetime(report.getCreatedAt());

            Paragraph metaWeek = new Paragraph(week, normalFont);
            metaWeek.setAlignment(Element.ALIGN_CENTER);
            document.add(metaWeek);
            Paragraph metaRef = new Paragraph(reference, normalFont);
            metaRef.setAlignment(Element.ALIGN_CENTER);
            document.add(metaRef);
            Paragraph metaCreated = new Paragraph(createdLine, normalFont);
            metaCreated.setAlignment(Element.ALIGN_CENTER);
            metaCreated.setSpacingAfter(8);
            document.add(metaCreated);
            Paragraph metaEmployee = new Paragraph(submittedBy, normalFont);
            document.add(metaEmployee);
            Paragraph metaSupervisor = new Paragraph(supervisorLine, normalFont);
            metaSupervisor.setSpacingAfter(8);
            document.add(metaSupervisor);

            // Employee information table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingAfter(12);
            // Employee Full Name (fallback to username)
            String employeeName = "-";
            if (report.getEmployee() != null) {
                try {
                    String fn = report.getEmployee().getFullName();
                    if (fn != null && !fn.isBlank()) employeeName = fn;
                } catch (Exception ignored) {}
                if ("-".equals(employeeName) && report.getEmployee().getUserAccount() != null) {
                    String u = report.getEmployee().getUserAccount().getUsername();
                    if (u != null && !u.isBlank()) employeeName = u;
                }
            }

            // Supervisor Full Name (fallback to username)
            String supervisorName = "-";
            if (report.getEmployee() != null && report.getEmployee().getSupervisor() != null) {
                try {
                    String sfn = report.getEmployee().getSupervisor().getFullName();
                    if (sfn != null && !sfn.isBlank()) supervisorName = sfn;
                } catch (Exception ignored) {}
                if (("-".equals(supervisorName)) && report.getEmployee().getSupervisor().getUserAccount() != null) {
                    String su = report.getEmployee().getSupervisor().getUserAccount().getUsername();
                    if (su != null && !su.isBlank()) supervisorName = su;
                }
            }

            table.addCell(cell("Name", labelFont));
            table.addCell(cell(employeeName, normalFont));
            table.addCell(cell("Username / ID", labelFont));
            table.addCell(cell(username(report), normalFont));
            table.addCell(cell("Supervisor", labelFont));
            table.addCell(cell(supervisorName, normalFont));
            table.addCell(cell("Role", labelFont));
            table.addCell(cell(role(report), normalFont));
            table.addCell(cell("Status", labelFont));
            table.addCell(statusCell(report.getStatus() != null ? report.getStatus().name() : "-", normalFont));
            // Timestamps
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String created = report.getCreatedAt() != null ? dtf.format(report.getCreatedAt()) : "-";
            String submitted = report.getSubmittedAt() != null ? dtf.format(report.getSubmittedAt()) : "-";
            String approved = report.getApprovedAt() != null ? dtf.format(report.getApprovedAt()) : "-";
            String rejected = report.getRejectedAt() != null ? dtf.format(report.getRejectedAt()) : "-";
            table.addCell(cell("Created", labelFont));
            table.addCell(cell(created, normalFont));
            table.addCell(cell("Submitted", labelFont));
            table.addCell(cell(submitted, normalFont));
            if (report.getStatus() != null && report.getStatus().name().equalsIgnoreCase("APPROVED") && approved != null && !approved.equals("-")) {
                table.addCell(cell("Approved", labelFont));
                table.addCell(cell(approved, normalFont));
            }
            if (report.getStatus() != null && report.getStatus().name().equalsIgnoreCase("REJECTED") && rejected != null && !rejected.equals("-")) {
                table.addCell(cell("Rejected", labelFont));
                table.addCell(cell(rejected, normalFont));
            }
            // Hours worked if available
            String hours = textFrom(report, "getHoursWorked", null);
            if (hours != null && !"—".equals(hours)) {
                table.addCell(cell("Hours Worked", labelFont));
                table.addCell(cell(hours, normalFont));
            }
            document.add(table);

            // Body Sections
            addSection(document, "Accomplishments", textFrom(report, "getAccomplishments", "getAccomplishedTasks"), sectionFont, normalFont);
            addSection(document, "Challenges", textFrom(report, "getChallenges", "getChallengesFaced"), sectionFont, normalFont);
            addSection(document, "Next Week Goals", textFrom(report, "getNextWeekGoals", "getNextWeekPlans"), sectionFont, normalFont);
            // Additional Notes if present (supports both additionalComments and additionalNotes naming)
            // Remove Additional Notes, Pending Tasks and Recommendations sections per request
            addSection(document, "Supervisor Feedback", textFrom(report, "getSupervisorFeedback", null), sectionFont, normalFont);

            // Footer: signature lines & contact info
            document.add(Chunk.NEWLINE);
            PdfPTable sign = new PdfPTable(2);
            sign.setWidthPercentage(100);
            PdfPCell left = new PdfPCell(new Phrase("Prepared by: \n\n_________________________\n" + employeeName, normalFont));
            left.setBorder(Rectangle.NO_BORDER);
            PdfPCell right = new PdfPCell(new Phrase("Approved by: \n\n_________________________\n" + supervisorName, normalFont));
            right.setBorder(Rectangle.NO_BORDER);
            sign.addCell(left);
            sign.addCell(right);
            document.add(sign);

            Paragraph contact = new Paragraph("Contact: " + contactEmail(report) + " | " + contactPhone(report), new Font(Font.TIMES_ROMAN, 9, Font.NORMAL, Color.GRAY));
            contact.setAlignment(Element.ALIGN_LEFT);
            document.add(contact);

            Paragraph sysNote = new Paragraph("This document was auto-generated from the reporting system database on " + now(), new Font(Font.TIMES_ROMAN, 9, Font.NORMAL, Color.GRAY));
            sysNote.setAlignment(Element.ALIGN_LEFT);
            sysNote.setSpacingBefore(4);
            document.add(sysNote);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private void addSection(Document doc, String title, String text, Font sectionFont, Font normalFont) throws DocumentException {
        Paragraph s = new Paragraph(title, sectionFont);
        s.setSpacingBefore(10);
        s.setSpacingAfter(6);
        doc.add(s);
        Paragraph t = new Paragraph(text != null && !text.isBlank() ? text : "—", normalFont);
        t.setLeading(14);
        doc.add(t);
    }

    private PdfPCell cell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "-", font));
        c.setPadding(6);
        return c;
    }

    private PdfPCell statusCell(String status, Font font) {
        // Do NOT mutate the incoming font; create a fresh one for the cell
        Color bg = new Color(243, 244, 246); // gray-100
        Color fg = new Color(31, 41, 55);   // gray-800
        if ("APPROVED".equalsIgnoreCase(status)) { bg = new Color(243, 244, 246); fg = Color.BLACK; }
        else if ("SUBMITTED".equalsIgnoreCase(status)) { bg = new Color(219, 234, 254); fg = new Color(30, 64, 175); }
        else if ("DRAFT".equalsIgnoreCase(status)) { bg = new Color(254, 243, 199); fg = new Color(146, 64, 14); }
        else if ("REJECTED".equalsIgnoreCase(status)) { bg = new Color(254, 226, 226); fg = new Color(153, 27, 27); }
        Font cellFont = new Font(font.getFamily(), font.getSize(), font.getStyle(), fg);
        PdfPCell c = new PdfPCell(new Phrase(status != null ? status : "-", cellFont));
        c.setPadding(6);
        c.setHorizontalAlignment(Element.ALIGN_LEFT);
        c.setBackgroundColor(bg);
        return c;
    }

    private String textFrom(WeeklyReport report, String primaryGetter, String fallbackGetter) {
        String value = null;
        try {
            if (primaryGetter != null) {
                Object v = report.getClass().getMethod(primaryGetter).invoke(report);
                value = v != null ? String.valueOf(v) : null;
            }
        } catch (Exception ignored) {}
        if ((value == null || value.isBlank()) && fallbackGetter != null) {
            try {
                Object v2 = report.getClass().getMethod(fallbackGetter).invoke(report);
                value = v2 != null ? String.valueOf(v2) : null;
            } catch (Exception ignored) {}
        }
        return value != null && !value.isBlank() ? value : "—";
    }

    // Footer with page numbers: "Page X"
    private static class FooterPageXofY extends PdfPageEventHelper {
        private final Font footerFont = new Font(Font.TIMES_ROMAN, 9, Font.NORMAL, Color.GRAY);
        private PdfTemplate total;

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(30, 16);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            String text = "Page " + writer.getPageNumber() + " of ";
            float x = (document.right() + document.left()) / 2;
            float y = document.bottom() - 10;
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(text, footerFont), x, y, 0);
            cb.addTemplate(total, x + 22, y - 3);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(total, Element.ALIGN_LEFT, new Phrase(String.valueOf(writer.getPageNumber() - 1), footerFont), 2, 2, 0);
        }
    }

    private String username(WeeklyReport report) {
        try { if (report.getEmployee() != null && report.getEmployee().getUserAccount() != null) return report.getEmployee().getUserAccount().getUsername(); } catch (Exception ignored) {}
        return "-";
    }

    private String department(WeeklyReport report) {
        try { if (report.getEmployee() != null) {
            Object v = report.getEmployee().getClass().getMethod("getDepartment").invoke(report.getEmployee());
            return v != null ? String.valueOf(v) : "-";
        }} catch (Exception ignored) {}
        return "-";
    }

    private String role(WeeklyReport report) {
        try { if (report.getEmployee() != null && report.getEmployee().getUserAccount() != null && report.getEmployee().getUserAccount().getRole() != null) return report.getEmployee().getUserAccount().getRole().name(); } catch (Exception ignored) {}
        return "-";
    }

    private String companyName(WeeklyReport report) {
        // If you later add org on report/company settings, map here; default label for now
        return "Weekly Report System";
    }

    private String contactEmail(WeeklyReport report) {
        try { if (report.getEmployee() != null && report.getEmployee().getUserAccount() != null) return report.getEmployee().getUserAccount().getEmail(); } catch (Exception ignored) {}
        return "support@example.com";
    }

    private String contactPhone(WeeklyReport report) {
        try { if (report.getEmployee() != null) {
            Object v = report.getEmployee().getClass().getMethod("getPhone").invoke(report.getEmployee());
            if (v != null) return String.valueOf(v);
        }} catch (Exception ignored) {}
        return "+000 000 000";
    }

    private String datetime(java.time.LocalDateTime dt) {
        if (dt == null) return "-";
        return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private String now() {
        return java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}


