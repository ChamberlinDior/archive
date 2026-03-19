package ga.mvet.geddemo.controller;

import ga.mvet.geddemo.model.Document;
import ga.mvet.geddemo.service.DocumentService;
import ga.mvet.geddemo.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;

    public FileController(DocumentService documentService, FileStorageService fileStorageService) {
        this.documentService = documentService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/documents/{documentId}/open")
    public ResponseEntity<?> openDocument(@PathVariable Long documentId) {
        Document document = documentService.findEntityById(documentId);

        if (Boolean.TRUE.equals(document.getExternalDocument()) && document.getExternalUrl() != null) {
            return ResponseEntity.status(302)
                    .location(URI.create(document.getExternalUrl()))
                    .build();
        }

        if (document.getStoredFileName() == null || document.getStoredFileName().isBlank()) {
            if (document.getFilePath() != null && !document.getFilePath().isBlank()) {
                return ResponseEntity.status(302)
                        .location(URI.create(document.getFilePath()))
                        .build();
            }

            return ResponseEntity.badRequest().body("Aucun contenu document n'est disponible pour ouverture.");
        }

        Resource resource = fileStorageService.loadAsResource(document.getStoredFileName());

        MediaType mediaType = resolveMediaType(document.getMimeType());

        ContentDisposition disposition = ContentDisposition.inline()
                .filename(resolveFileName(document), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<?> downloadDocument(@PathVariable Long documentId) {
        Document document = documentService.findEntityById(documentId);

        if (Boolean.TRUE.equals(document.getExternalDocument()) && document.getExternalUrl() != null) {
            return ResponseEntity.status(302)
                    .location(URI.create(document.getExternalUrl()))
                    .build();
        }

        if (document.getStoredFileName() == null || document.getStoredFileName().isBlank()) {
            if (document.getFilePath() != null && !document.getFilePath().isBlank()) {
                return ResponseEntity.status(302)
                        .location(URI.create(document.getFilePath()))
                        .build();
            }

            return ResponseEntity.badRequest().body("Aucun contenu document n'est disponible pour téléchargement.");
        }

        Resource resource = fileStorageService.loadAsResource(document.getStoredFileName());

        MediaType mediaType = resolveMediaType(document.getMimeType());

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(resolveFileName(document), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    private String resolveFileName(Document document) {
        if (document.getOriginalFileName() != null && !document.getOriginalFileName().isBlank()) {
            return document.getOriginalFileName();
        }

        if (document.getStoredFileName() != null && !document.getStoredFileName().isBlank()) {
            return document.getStoredFileName();
        }

        return document.getTitle() + ".bin";
    }

    private MediaType resolveMediaType(String mimeType) {
        try {
            if (mimeType != null && !mimeType.isBlank()) {
                return MediaType.parseMediaType(mimeType);
            }
        } catch (Exception ignored) {
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}