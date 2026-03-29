package com.sap.vcs.server.controller;

import com.sap.vcs.server.service.PdfExportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
public class DocumentPdfController {

    private final PdfExportService pdfExportService;

    public DocumentPdfController(PdfExportService pdfExportService) {
        this.pdfExportService = pdfExportService;
    }

    @GetMapping("/{id}/published-version/pdf")
    public ResponseEntity<byte[]> downloadPublishedVersionPdf(@PathVariable Integer id) {
        byte[] pdf = pdfExportService.exportPublishedVersionPdf(id);

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename("document-" + id + "-published.pdf")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }

    @GetMapping("/{documentId}/versions/{versionId}/pdf")
    public ResponseEntity<byte[]> downloadVersionPdf(
            @PathVariable Integer documentId,
            @PathVariable Integer versionId
    ) {
        byte[] pdf = pdfExportService.exportVersionPdf(documentId, versionId);

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename("document-" + documentId + "-version-" + versionId + ".pdf")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}