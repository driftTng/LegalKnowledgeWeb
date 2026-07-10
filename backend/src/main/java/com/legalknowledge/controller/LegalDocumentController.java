package com.legalknowledge.controller;

import com.legalknowledge.dto.response.LegalDocumentDTO;
import com.legalknowledge.service.LegalDocumentService;
import com.legalknowledge.service.PdfService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class LegalDocumentController {

    private final LegalDocumentService documentService;
    private final PdfService pdfService;

    public LegalDocumentController(LegalDocumentService documentService, PdfService pdfService) {
        this.documentService = documentService;
        this.pdfService = pdfService;
    }

    /** 法规列表 + 搜索 */
    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(documentService.listAll(keyword, category));
    }

    /** 所有分类 */
    @GetMapping("/categories")
    public ResponseEntity<?> categories() {
        return ResponseEntity.ok(documentService.getCategories());
    }

    /** 法规详情 */
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(documentService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** PDF 下载 */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        try {
            LegalDocumentDTO doc = documentService.getById(id);
            byte[] pdfBytes = pdfService.generatePdf(doc);

            String filename = doc.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_") + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
