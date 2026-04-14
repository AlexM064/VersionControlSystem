package com.sap.vcs.server.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.DocumentRepository;
import com.sap.vcs.server.repository.DocumentVersionRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UncheckedIOException;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfExportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;

    public PdfExportService(
            DocumentRepository documentRepository,
            DocumentVersionRepository documentVersionRepository
    ) {
        this.documentRepository = documentRepository;
        this.documentVersionRepository = documentVersionRepository;
    }

    public byte[] exportPublishedVersionPdf(Integer documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        DocumentVersion publishedVersion = document.getPublishedVersion();
        if (publishedVersion == null) {
            throw new ResourceNotFoundException("No published version found for document id: " + documentId);
        }

        return renderDocumentVersionPdf(document, publishedVersion, true);
    }

    public byte[] exportVersionPdf(Integer documentId, Integer versionId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        DocumentVersion version = documentVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found with id: " + versionId));

        if (version.getDocument() == null || version.getDocument().getId() == null ||
                !version.getDocument().getId().equals(document.getId())) {
            throw new ResourceNotFoundException(
                    "Version " + versionId + " does not belong to document " + documentId
            );
        }

        return renderDocumentVersionPdf(document, version, false);
    }

    private File copyClasspathFontToTempFile(String classpathLocation, String prefix, String suffix) {
    try {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        File tempFile = File.createTempFile(prefix, suffix);
        tempFile.deleteOnExit();

        try (java.io.InputStream in = resource.getInputStream();
             java.io.OutputStream out = new java.io.FileOutputStream(tempFile)) {
            in.transferTo(out);
        }

        return tempFile;
    } catch (java.io.IOException ex) {
        throw new UncheckedIOException("Could not load font from " + classpathLocation, ex);
    }
}

    private byte[] renderDocumentVersionPdf(Document document, DocumentVersion version, boolean published) {
        String html = buildHtml(document, version, published);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            File regularFont = copyClasspathFontToTempFile("fonts/DejaVuSans.ttf", "dejavu-regular", ".ttf");
            File boldFont = copyClasspathFontToTempFile("fonts/DejaVuSans-Bold.ttf", "dejavu-bold", ".ttf");
            builder.useFont(regularFont, "DejaVu Sans", 400, PdfRendererBuilder.FontStyle.NORMAL, false);
            builder.useFont(boldFont, "DejaVu Sans", 700, PdfRendererBuilder.FontStyle.NORMAL, false);

            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to generate PDF: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
        }
    }

    private String buildHtml(Document document, DocumentVersion version, boolean published) {
        String safeTitle = escapeHtml(nullToEmpty(document.getTitle()));
        String safeDescription = escapeHtml(nullToEmpty(document.getDescription()));
        String safeMessage = escapeHtml(nullToEmpty(version.getMessage()));
        String safeContent = convertPlainTextToHtml(version.getContent());

        String createdAt = version.getCreatedAt() != null
                ? version.getCreatedAt().format(DATE_TIME_FORMATTER)
                : "-";

        String owner = document.getOwner() != null
                ? escapeHtml(nullToEmpty(document.getOwner().getUsername()))
                : "-";

        String createdBy = version.getCreatedBy() != null
                ? escapeHtml(nullToEmpty(version.getCreatedBy().getUsername()))
                : "-";

        String badge = published ? "Published Version" : "Document Version";
        String documentStatus = escapeHtml(safeToString(document.getStatus()));
        String versionNumber = escapeHtml(safeToString(version.getVersionNumber()));
        String documentId = escapeHtml(safeToString(document.getId()));
        String versionId = escapeHtml(safeToString(version.getId()));

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        @page {
                            size: A4;
                            margin: 35px 30px 45px 30px;
                        }

                        body {
                            font-family: 'DejaVu Sans', Arial, sans-serif;
                            font-size: 12px;
                            color: #222222;
                            margin: 0;
                            padding: 0;
                        }

                        .header {
                            text-align: center;
                            font-size: 22px;
                            font-weight: bold;
                            margin-bottom: 10px;
                            padding-bottom: 10px;
                            border-bottom: 1px solid #dcdcdc;
                        }

                        .subheader {
                            text-align: center;
                            font-size: 11px;
                            color: #666666;
                            margin-bottom: 24px;
                        }

                        h1 {
                            font-size: 24px;
                            margin: 0 0 10px 0;
                            word-wrap: break-word;
                        }

                        .badge {
                            display: inline-block;
                            padding: 6px 12px;
                            font-size: 11px;
                            font-weight: bold;
                            border: 1px solid #999999;
                            border-radius: 4px;
                            margin-bottom: 18px;
                        }

                        .meta {
                            width: 100%%;
                            border-collapse: collapse;
                            margin-bottom: 24px;
                        }

                        .meta td {
                            border: 1px solid #dddddd;
                            padding: 8px;
                            vertical-align: top;
                            word-wrap: break-word;
                        }

                        .label {
                            width: 180px;
                            font-weight: bold;
                            background: #f7f7f7;
                        }

                        .section-title {
                            font-size: 16px;
                            font-weight: bold;
                            margin: 24px 0 8px 0;
                            border-bottom: 1px solid #dddddd;
                            padding-bottom: 6px;
                        }

                        .content {
                            line-height: 1.6;
                            word-wrap: break-word;
                        }

                        .muted {
                            color: #777777;
                        }

                        .footer {
                            margin-top: 30px;
                            padding-top: 10px;
                            border-top: 1px solid #dcdcdc;
                            font-size: 10px;
                            color: #777777;
                            text-align: center;
                        }
                    </style>
                </head>
                <body>
                    <div class="header">Version Control System</div>
                    <div class="subheader">Exported document PDF</div>

                    <h1>%s</h1>
                    <div class="badge">%s</div>

                    <table class="meta">
                        <tr>
                            <td class="label">Document ID</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td class="label">Version ID</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td class="label">Version Number</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td class="label">Status</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td class="label">Owner</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td class="label">Created By</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td class="label">Created At</td>
                            <td>%s</td>
                        </tr>
                    </table>

                    <div class="section-title">Description</div>
                    <div class="content">%s</div>

                    <div class="section-title">Version Message</div>
                    <div class="content">%s</div>

                    <div class="section-title">Content</div>
                    <div class="content">%s</div>

                    <div class="footer">
                        Generated by Version Control System
                    </div>
                </body>
                </html>
                """.formatted(
                safeTitle.isBlank() ? "Untitled Document" : safeTitle,
                badge,
                documentId,
                versionId,
                versionNumber,
                documentStatus,
                owner,
                createdBy,
                createdAt,
                safeDescription.isBlank() ? "<span class='muted'>-</span>" : safeDescription,
                safeMessage.isBlank() ? "<span class='muted'>-</span>" : safeMessage,
                safeContent.isBlank() ? "<span class='muted'>-</span>" : safeContent
        );
    }

    private String convertPlainTextToHtml(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return escapeHtml(text)
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\n", "<br/>");
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String safeToString(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}