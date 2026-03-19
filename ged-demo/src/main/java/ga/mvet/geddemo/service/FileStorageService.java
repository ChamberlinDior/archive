package ga.mvet.geddemo.service;

import ga.mvet.geddemo.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

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

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "document" : file.getOriginalFilename());

        if (originalFileName.contains("..")) {
            throw new FileStorageException("Nom de fichier invalide.");
        }

        String extension = extractExtension(originalFileName);
        String storedFileName = UUID.randomUUID() + extension;

        try {
            Path targetLocation = storagePath.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return new DocumentService.FileUploadResponseInternal(
                    originalFileName,
                    storedFileName,
                    file.getContentType(),
                    file.getSize(),
                    targetLocation.toString()
            );
        } catch (IOException e) {
            throw new FileStorageException("Impossible de stocker le fichier : " + originalFileName, e);
        }
    }

    public Resource loadAsResource(String storedFileName) {
        try {
            Path filePath = storagePath.resolve(storedFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            }

            throw new FileStorageException("Le fichier demandé est introuvable ou illisible.");
        } catch (MalformedURLException e) {
            throw new FileStorageException("Erreur lors du chargement du fichier demandé.", e);
        }
    }

    public void deleteFileIfExists(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank()) {
            return;
        }

        try {
            Path target = storagePath.resolve(storedFileName).normalize();
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new FileStorageException("Impossible de supprimer le fichier physique : " + storedFileName, e);
        }
    }

    private String extractExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}