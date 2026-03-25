package ga.mvet.geddemo.controller;

import ga.mvet.geddemo.model.Document;
import ga.mvet.geddemo.service.DocumentConversionService;
import ga.mvet.geddemo.service.DocumentService;
import ga.mvet.geddemo.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;
    private final DocumentConversionService documentConversionService;

    public FileController(
            DocumentService documentService,
            FileStorageService fileStorageService,
            DocumentConversionService documentConversionService
    ) {
        this.documentService = documentService;
        this.fileStorageService = fileStorageService;
        this.documentConversionService = documentConversionService;
    }

    @GetMapping("/documents/{documentId}/preview")
    public ResponseEntity<?> previewDocument(
            @PathVariable Long documentId,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader
    ) {
        Document document = documentService.findEntityById(documentId);

        if (Boolean.TRUE.equals(document.getExternalDocument()) && hasText(document.getExternalUrl())) {
            return ResponseEntity.status(302)
                    .location(URI.create(document.getExternalUrl()))
                    .build();
        }

        if (!hasText(document.getStoredFileName())) {
            return ResponseEntity.badRequest().body("Aucun fichier disponible pour la prévisualisation.");
        }

        try {
            String storedFileName = document.getStoredFileName();
            String mimeType = document.getMimeType();

            if (fileStorageService.isPreviewableDirectly(mimeType, storedFileName)) {
                Resource resource = fileStorageService.loadAsResource(storedFileName);
                Path filePath = fileStorageService.resolveStoredFilePath(storedFileName);

                return serveResource(
                        resource,
                        filePath,
                        fileStorageService.resolveMediaType(storedFileName, mimeType),
                        resolveFileName(document),
                        true,
                        rangeHeader
                );
            }

            if (fileStorageService.isOfficeConvertible(storedFileName)) {
                if (!documentConversionService.isConversionAvailable()) {
                    return ResponseEntity.badRequest().body(
                            "La conversion Office vers PDF n'est pas disponible. Vérifiez LibreOffice."
                    );
                }

                Path previewPdf = documentConversionService.getOrCreatePdfPreview(storedFileName);
                Resource previewResource = new UrlResource(previewPdf.toUri());

                return serveResource(
                        previewResource,
                        previewPdf,
                        MediaType.APPLICATION_PDF,
                        buildPreviewFileName(resolveFileName(document)),
                        true,
                        rangeHeader
                );
            }

            return ResponseEntity.badRequest().body(
                    "Ce type de document n'est pas prévisualisable directement dans l'application."
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la prévisualisation : " + e.getMessage());
        }
    }

    @GetMapping("/documents/{documentId}/open")
    public ResponseEntity<?> openDocument(
            @PathVariable Long documentId,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader
    ) {
        Document document = documentService.findEntityById(documentId);
        return serveOriginalDocument(document, true, rangeHeader);
    }

    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<?> downloadDocument(
            @PathVariable Long documentId,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader
    ) {
        Document document = documentService.findEntityById(documentId);
        return serveOriginalDocument(document, false, rangeHeader);
    }

    private ResponseEntity<?> serveOriginalDocument(Document document, boolean inline, String rangeHeader) {
        if (Boolean.TRUE.equals(document.getExternalDocument()) && hasText(document.getExternalUrl())) {
            return ResponseEntity.status(302)
                    .location(URI.create(document.getExternalUrl()))
                    .build();
        }

        if (!hasText(document.getStoredFileName())) {
            if (hasText(document.getFilePath())) {
                return ResponseEntity.status(302)
                        .location(URI.create(document.getFilePath()))
                        .build();
            }

            return ResponseEntity.badRequest().body(
                    inline
                            ? "Aucun contenu document n'est disponible pour ouverture."
                            : "Aucun contenu document n'est disponible pour téléchargement."
            );
        }

        try {
            Resource resource = fileStorageService.loadAsResource(document.getStoredFileName());
            Path filePath = fileStorageService.resolveStoredFilePath(document.getStoredFileName());
            MediaType mediaType = fileStorageService.resolveMediaType(
                    document.getStoredFileName(),
                    document.getMimeType()
            );

            return serveResource(
                    resource,
                    filePath,
                    mediaType,
                    resolveFileName(document),
                    inline,
                    rangeHeader
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la lecture du fichier demandé : " + e.getMessage());
        }
    }

    private ResponseEntity<?> serveResource(
            Resource resource,
            Path filePath,
            MediaType mediaType,
            String fileName,
            boolean inline,
            String rangeHeader
    ) throws Exception {

        long contentLength = Files.size(filePath);
        ContentDisposition disposition = inline
                ? ContentDisposition.inline().filename(fileName, StandardCharsets.UTF_8).build()
                : ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build();

        if (isStreamableMedia(mediaType) && hasText(rangeHeader)) {
            List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);

            if (!ranges.isEmpty()) {
                ResourceRegion region = ranges.get(0).toResourceRegion(resource);
                long start = region.getPosition();
                long regionLength = region.getCount();
                long end = start + regionLength - 1;

                return ResponseEntity.status(206)
                        .header(HttpHeaders.CONTENT_TYPE, mediaType.toString())
                        .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + contentLength)
                        .contentLength(regionLength)
                        .body(region);
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, mediaType.toString())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .contentLength(contentLength)
                .body(resource);
    }

    private boolean isStreamableMedia(MediaType mediaType) {
        return mediaType != null
                && ("video".equalsIgnoreCase(mediaType.getType())
                || "audio".equalsIgnoreCase(mediaType.getType()));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String resolveFileName(Document document) {
        if (hasText(document.getOriginalFileName())) {
            return document.getOriginalFileName();
        }

        if (hasText(document.getStoredFileName())) {
            return document.getStoredFileName();
        }

        if (hasText(document.getTitle())) {
            return document.getTitle() + ".bin";
        }

        return "document.bin";
    }

    private String buildPreviewFileName(String originalName) {
        int dotIndex = originalName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? originalName.substring(0, dotIndex) : originalName;
        return baseName + ".preview.pdf";
    }
}