package ga.mvet.geddemo.service;

import ga.mvet.geddemo.exception.FileStorageException;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DocumentConversionService {

    private final FileStorageService fileStorageService;
    private final Path previewStoragePath;
    private final boolean conversionEnabled;
    private final String libreOfficeHome;

    public DocumentConversionService(
            FileStorageService fileStorageService,
            @Value("${app.file-storage.preview-location:uploads/previews}") String previewLocation,
            @Value("${app.document-conversion.enabled:false}") boolean conversionEnabled,
            @Value("${app.libreoffice.home:}") String libreOfficeHome
    ) {
        this.fileStorageService = fileStorageService;
        this.previewStoragePath = Paths.get(previewLocation).toAbsolutePath().normalize();
        this.conversionEnabled = conversionEnabled;
        this.libreOfficeHome = libreOfficeHome == null ? "" : libreOfficeHome.trim();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(previewStoragePath);
        } catch (IOException e) {
            throw new FileStorageException("Impossible de créer le dossier des aperçus convertis.", e);
        }
    }

    public boolean isConversionAvailable() {
        if (!conversionEnabled) {
            return false;
        }

        if (!StringUtils.hasText(libreOfficeHome)) {
            return false;
        }

        File officeHome = new File(libreOfficeHome);
        return officeHome.exists() && officeHome.isDirectory();
    }

    public Path getOrCreatePdfPreview(String storedFileName) {
        if (!StringUtils.hasText(storedFileName)) {
            throw new FileStorageException("Nom de fichier invalide pour la conversion.");
        }

        if (!isConversionAvailable()) {
            throw new FileStorageException(
                    "La conversion Office vers PDF n'est pas disponible. Vérifiez app.libreoffice.home."
            );
        }

        Path source = fileStorageService.resolveStoredFilePath(storedFileName);

        if (!Files.exists(source)) {
            throw new FileStorageException("Le fichier source à convertir est introuvable.");
        }

        String targetName = buildPreviewPdfName(storedFileName);
        Path target = previewStoragePath.resolve(targetName).normalize();

        if (!target.startsWith(previewStoragePath)) {
            throw new FileStorageException("Accès refusé au dossier de prévisualisation.");
        }

        try {
            if (Files.exists(target) && Files.size(target) > 0) {
                return target;
            }

            convertToPdf(source, target);

            if (!Files.exists(target) || Files.size(target) == 0) {
                throw new FileStorageException("La conversion du document en PDF a échoué.");
            }

            return target;

        } catch (Exception e) {
            throw new FileStorageException("Impossible de convertir le document en PDF : " + e.getMessage(), e);
        }
    }

    private void convertToPdf(Path source, Path target) throws OfficeException {
        OfficeManager officeManager = null;

        try {
            officeManager = LocalOfficeManager.builder()
                    .officeHome(new File(libreOfficeHome))
                    .install()
                    .build();

            officeManager.start();

            DocumentConverter converter = LocalConverter.make(officeManager);
            converter.convert(source.toFile())
                    .to(target.toFile())
                    .execute();

        } finally {
            if (officeManager != null) {
                try {
                    officeManager.stop();
                } catch (OfficeException ignored) {
                }
            }
        }
    }

    private String buildPreviewPdfName(String storedFileName) {
        int dotIndex = storedFileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? storedFileName.substring(0, dotIndex) : storedFileName;
        return baseName + ".preview.pdf";
    }
}