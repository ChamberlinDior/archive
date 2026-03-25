package ga.mvet.geddemo.service;

import ga.mvet.geddemo.exception.FileStorageException;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Tika TIKA = new Tika();

    private static final Set<String> OFFICE_EXTENSIONS = Set.of(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "ods", "odp", "rtf"
    );

    private final Path storagePath;

    public FileStorageService(@Value("${app.file-storage.location:uploads/documents}") String storageLocation) {
        this.storagePath = Paths.get(storageLocation).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            throw new FileStorageException("Impossible de créer le dossier de stockage des documents.", e);
        }
    }

    public DocumentService.FileUploadResponseInternal storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Le fichier à stocker est vide ou absent.");
        }

        String originalFileName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "document" : file.getOriginalFilename()
        );

        if (originalFileName.contains("..")) {
            throw new FileStorageException("Nom de fichier invalide.");
        }

        String extension = extractExtension(originalFileName);
        String storedFileName = UUID.randomUUID() + extension;

        try {
            Path targetLocation = storagePath.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String detectedMimeType = detectMimeType(targetLocation, originalFileName, file.getContentType());

            return new DocumentService.FileUploadResponseInternal(
                    originalFileName,
                    storedFileName,
                    detectedMimeType,
                    file.getSize(),
                    targetLocation.toString()
            );
        } catch (IOException e) {
            throw new FileStorageException("Impossible de stocker le fichier : " + originalFileName, e);
        }
    }

    public Resource loadAsResource(String storedFileName) {
        try {
            Path filePath = resolveStoredFilePath(storedFileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            }

            throw new FileStorageException("Le fichier demandé est introuvable ou illisible.");
        } catch (MalformedURLException e) {
            throw new FileStorageException("Erreur lors du chargement du fichier demandé.", e);
        }
    }

    public Path resolveStoredFilePath(String storedFileName) {
        if (!StringUtils.hasText(storedFileName)) {
            throw new FileStorageException("Nom de fichier stocké invalide.");
        }

        Path filePath = storagePath.resolve(storedFileName).normalize();

        if (!filePath.startsWith(storagePath)) {
            throw new FileStorageException("Accès au fichier refusé.");
        }

        return filePath;
    }

    public MediaType resolveMediaType(String storedFileName, String fallbackMimeType) {
        try {
            Path path = resolveStoredFilePath(storedFileName);
            String detected = Files.probeContentType(path);
            if (StringUtils.hasText(detected)) {
                return MediaType.parseMediaType(detected);
            }
        } catch (Exception ignored) {
        }

        if (StringUtils.hasText(fallbackMimeType)) {
            try {
                return MediaType.parseMediaType(fallbackMimeType);
            } catch (Exception ignored) {
            }
        }

        return MediaTypeFactory.getMediaType(storedFileName)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
    }

    public boolean isOfficeConvertible(String fileName) {
        String ext = extractExtension(fileName).replace(".", "").toLowerCase(Locale.ROOT);
        return OFFICE_EXTENSIONS.contains(ext);
    }

    public boolean isPreviewableDirectly(String mimeType, String fileName) {
        if (StringUtils.hasText(mimeType)) {
            try {
                MediaType mediaType = MediaType.parseMediaType(mimeType);

                if (MediaType.APPLICATION_PDF.includes(mediaType)) {
                    return true;
                }

                String type = mediaType.getType();
                if ("image".equalsIgnoreCase(type)
                        || "video".equalsIgnoreCase(type)
                        || "audio".equalsIgnoreCase(type)
                        || "text".equalsIgnoreCase(type)) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        String ext = extractExtension(fileName).replace(".", "").toLowerCase(Locale.ROOT);
        return Set.of(
                "pdf", "png", "jpg", "jpeg", "gif", "webp", "svg", "bmp",
                "txt", "log", "md", "csv", "json", "xml", "html", "htm"
        ).contains(ext);
    }

    public void deleteFileIfExists(String storedFileName) {
        if (!StringUtils.hasText(storedFileName)) {
            return;
        }

        try {
            Files.deleteIfExists(resolveStoredFilePath(storedFileName));
        } catch (IOException e) {
            throw new FileStorageException("Impossible de supprimer le fichier physique : " + storedFileName, e);
        }
    }

    private String detectMimeType(Path targetLocation, String originalFileName, String multipartMimeType) {
        try (InputStream inputStream = Files.newInputStream(targetLocation)) {
            String detected = TIKA.detect(inputStream, originalFileName);
            if (StringUtils.hasText(detected) && !"application/octet-stream".equalsIgnoreCase(detected)) {
                return detected;
            }
        } catch (Exception ignored) {
        }

        try {
            String detected = Files.probeContentType(targetLocation);
            if (StringUtils.hasText(detected)) {
                return detected;
            }
        } catch (IOException ignored) {
        }

        if (StringUtils.hasText(multipartMimeType)
                && !"application/octet-stream".equalsIgnoreCase(multipartMimeType)) {
            return multipartMimeType;
        }

        return guessMimeTypeByExtension(originalFileName);
    }

    private String guessMimeTypeByExtension(String filename) {
        String ext = extractExtension(filename).replace(".", "").toLowerCase(Locale.ROOT);

        return switch (ext) {
            case "pdf" -> "application/pdf";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            case "bmp" -> "image/bmp";
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "mov" -> "video/quicktime";
            case "avi" -> "video/x-msvideo";
            case "mkv" -> "video/x-matroska";
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";
            case "m4a" -> "audio/mp4";
            case "txt", "log", "md", "csv", "json", "xml", "html", "htm", "sql", "java", "js", "ts", "tsx", "jsx", "css", "yml", "yaml", "properties" -> "text/plain";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "zip" -> "application/zip";
            case "rar" -> "application/vnd.rar";
            case "7z" -> "application/x-7z-compressed";
            default -> "application/octet-stream";
        };
    }

    private String extractExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex);
    }
}