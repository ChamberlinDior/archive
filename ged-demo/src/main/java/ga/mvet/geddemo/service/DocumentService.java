package ga.mvet.geddemo.service;

import ga.mvet.geddemo.dto.DocumentCreateMultipartRequest;
import ga.mvet.geddemo.dto.DocumentRequest;
import ga.mvet.geddemo.dto.DocumentResponse;
import ga.mvet.geddemo.exception.ResourceNotFoundException;
import ga.mvet.geddemo.model.Category;
import ga.mvet.geddemo.model.Department;
import ga.mvet.geddemo.model.Document;
import ga.mvet.geddemo.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CategoryService categoryService;
    private final DepartmentService departmentService;
    private final FileStorageService fileStorageService;

    public DocumentService(DocumentRepository documentRepository,
                           CategoryService categoryService,
                           DepartmentService departmentService,
                           FileStorageService fileStorageService) {
        this.documentRepository = documentRepository;
        this.categoryService = categoryService;
        this.departmentService = departmentService;
        this.fileStorageService = fileStorageService;
    }

    public DocumentResponse create(DocumentRequest request) {
        validateUniqueReference(request.getReferenceCode(), null);

        Category category = categoryService.findEntityById(request.getCategoryId());
        Department department = departmentService.findEntityById(request.getDepartmentId());

        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setReferenceCode(request.getReferenceCode());
        document.setDescription(request.getDescription());
        document.setStatus(request.getStatus());
        document.setDocumentDate(request.getDocumentDate());
        document.setActive(request.getActive() != null ? request.getActive() : true);
        document.setCategory(category);
        document.setDepartment(department);

        if (Boolean.TRUE.equals(request.getExternalDocument()) || hasText(request.getExternalUrl())) {
            validateExternalUrl(request.getExternalUrl());
            document.setExternalDocument(true);
            document.setExternalUrl(request.getExternalUrl());
            document.setStorageType("EXTERNAL_LINK");
            document.setFilePath(request.getExternalUrl());
        } else if (hasText(request.getFilePath())) {
            document.setFilePath(request.getFilePath());
            document.setStorageType("MANUAL_PATH");
            document.setExternalDocument(false);
        } else {
            document.setStorageType("MANUAL_PATH");
            document.setExternalDocument(false);
        }

        Document saved = documentRepository.save(document);
        return mapToResponse(findEntityByIdWithRelations(saved.getId()));
    }

    public DocumentResponse createWithUpload(DocumentCreateMultipartRequest request) {
        validateMultipartCreateRequest(request);
        validateUniqueReference(request.getReferenceCode(), null);

        Category category = categoryService.findEntityById(request.getCategoryId());
        Department department = departmentService.findEntityById(request.getDepartmentId());

        FileUploadResponseInternal storedFile = fileStorageService.storeFile(request.getFile());

        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setReferenceCode(request.getReferenceCode());
        document.setDescription(request.getDescription());
        document.setStatus(request.getStatus());
        document.setDocumentDate(request.getDocumentDate());
        document.setActive(request.getActive() != null ? request.getActive() : true);
        document.setCategory(category);
        document.setDepartment(department);
        document.setOriginalFileName(storedFile.getOriginalFileName());
        document.setStoredFileName(storedFile.getStoredFileName());
        document.setMimeType(storedFile.getMimeType());
        document.setFileSize(storedFile.getFileSize());
        document.setFilePath(storedFile.getAbsolutePath());
        document.setExternalDocument(false);
        document.setStorageType("LOCAL_DISK");

        Document saved = documentRepository.save(document);
        return mapToResponse(findEntityByIdWithRelations(saved.getId()));
    }

    public DocumentResponse createExternalLink(DocumentRequest request) {
        request.setExternalDocument(true);
        return create(request);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getAll() {
        return documentRepository.findAllWithRelations()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getActiveDocuments() {
        return documentRepository.findByActiveTrueWithRelations()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DocumentResponse getById(Long id) {
        return mapToResponse(findEntityByIdWithRelations(id));
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getByStatus(String status) {
        return documentRepository.findByStatusIgnoreCaseWithRelations(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> searchByTitle(String keyword) {
        return documentRepository.findByTitleContainingIgnoreCaseWithRelations(keyword)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DocumentResponse update(Long id, DocumentRequest request) {
        Document document = findEntityById(id);
        validateUniqueReference(request.getReferenceCode(), id);

        Category category = categoryService.findEntityById(request.getCategoryId());
        Department department = departmentService.findEntityById(request.getDepartmentId());

        document.setTitle(request.getTitle());
        document.setReferenceCode(request.getReferenceCode());
        document.setDescription(request.getDescription());
        document.setStatus(request.getStatus());
        document.setDocumentDate(request.getDocumentDate());
        document.setCategory(category);
        document.setDepartment(department);

        if (request.getActive() != null) {
            document.setActive(request.getActive());
        }

        if (Boolean.TRUE.equals(request.getExternalDocument()) || hasText(request.getExternalUrl())) {
            validateExternalUrl(request.getExternalUrl());

            if ("LOCAL_DISK".equals(document.getStorageType()) && hasText(document.getStoredFileName())) {
                fileStorageService.deleteFileIfExists(document.getStoredFileName());
            }

            document.setExternalDocument(true);
            document.setExternalUrl(request.getExternalUrl());
            document.setFilePath(request.getExternalUrl());
            document.setStorageType("EXTERNAL_LINK");
            document.setOriginalFileName(null);
            document.setStoredFileName(null);
            document.setMimeType(null);
            document.setFileSize(null);
        } else if (hasText(request.getFilePath())) {
            if ("LOCAL_DISK".equals(document.getStorageType()) && hasText(document.getStoredFileName())) {
                fileStorageService.deleteFileIfExists(document.getStoredFileName());
            }

            document.setExternalDocument(false);
            document.setExternalUrl(null);
            document.setFilePath(request.getFilePath());
            document.setStorageType("MANUAL_PATH");
            document.setOriginalFileName(null);
            document.setStoredFileName(null);
            document.setMimeType(null);
            document.setFileSize(null);
        }

        Document saved = documentRepository.save(document);
        return mapToResponse(findEntityByIdWithRelations(saved.getId()));
    }

    public DocumentResponse updateWithUpload(Long id, DocumentCreateMultipartRequest request) {
        validateMultipartUpdateRequest(request);

        Document document = findEntityById(id);
        validateUniqueReference(request.getReferenceCode(), id);

        Category category = categoryService.findEntityById(request.getCategoryId());
        Department department = departmentService.findEntityById(request.getDepartmentId());

        document.setTitle(request.getTitle());
        document.setReferenceCode(request.getReferenceCode());
        document.setDescription(request.getDescription());
        document.setStatus(request.getStatus());
        document.setDocumentDate(request.getDocumentDate());
        document.setCategory(category);
        document.setDepartment(department);

        if (request.getActive() != null) {
            document.setActive(request.getActive());
        }

        MultipartFile multipartFile = request.getFile();
        if (multipartFile != null && !multipartFile.isEmpty()) {
            if ("LOCAL_DISK".equals(document.getStorageType()) && hasText(document.getStoredFileName())) {
                fileStorageService.deleteFileIfExists(document.getStoredFileName());
            }

            FileUploadResponseInternal storedFile = fileStorageService.storeFile(multipartFile);

            document.setOriginalFileName(storedFile.getOriginalFileName());
            document.setStoredFileName(storedFile.getStoredFileName());
            document.setMimeType(storedFile.getMimeType());
            document.setFileSize(storedFile.getFileSize());
            document.setFilePath(storedFile.getAbsolutePath());
            document.setExternalDocument(false);
            document.setExternalUrl(null);
            document.setStorageType("LOCAL_DISK");
        }

        Document saved = documentRepository.save(document);
        return mapToResponse(findEntityByIdWithRelations(saved.getId()));
    }

    public DocumentResponse updateExternalLink(Long id, DocumentRequest request) {
        request.setExternalDocument(true);
        return update(id, request);
    }

    public void delete(Long id) {
        Document document = findEntityById(id);

        if ("LOCAL_DISK".equals(document.getStorageType()) && hasText(document.getStoredFileName())) {
            fileStorageService.deleteFileIfExists(document.getStoredFileName());
        }

        documentRepository.delete(document);
    }

    @Transactional(readOnly = true)
    public Document findEntityById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document introuvable avec l'id : " + id));
    }

    @Transactional(readOnly = true)
    public Document findEntityByIdWithRelations(Long id) {
        return documentRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document introuvable avec l'id : " + id));
    }

    private void validateMultipartCreateRequest(DocumentCreateMultipartRequest request) {
        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new IllegalArgumentException("Le fichier est obligatoire pour cet endpoint d'upload.");
        }

        validateBaseMultipartRequest(request);
    }

    private void validateMultipartUpdateRequest(DocumentCreateMultipartRequest request) {
        validateBaseMultipartRequest(request);
    }

    private void validateBaseMultipartRequest(DocumentCreateMultipartRequest request) {
        if (!hasText(request.getTitle())) {
            throw new IllegalArgumentException("Le titre est obligatoire.");
        }
        if (!hasText(request.getReferenceCode())) {
            throw new IllegalArgumentException("La référence est obligatoire.");
        }
        if (!hasText(request.getStatus())) {
            throw new IllegalArgumentException("Le statut est obligatoire.");
        }
        if (request.getDocumentDate() == null) {
            throw new IllegalArgumentException("La date du document est obligatoire.");
        }
        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("La catégorie est obligatoire.");
        }
        if (request.getDepartmentId() == null) {
            throw new IllegalArgumentException("Le département est obligatoire.");
        }
    }

    private void validateUniqueReference(String referenceCode, Long currentDocumentId) {
        documentRepository.findByReferenceCode(referenceCode)
                .ifPresent(existing -> {
                    if (currentDocumentId == null || !existing.getId().equals(currentDocumentId)) {
                        throw new IllegalArgumentException("Une autre référence document existe déjà.");
                    }
                });
    }

    private void validateExternalUrl(String externalUrl) {
        if (!hasText(externalUrl)) {
            throw new IllegalArgumentException("L'URL externe est obligatoire.");
        }

        try {
            URI uri = URI.create(externalUrl);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("L'URL externe doit commencer par http:// ou https://");
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("L'URL externe fournie est invalide.");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private DocumentResponse mapToResponse(Document document) {
        String openUrl;
        String downloadUrl;

        if (Boolean.TRUE.equals(document.getExternalDocument()) && hasText(document.getExternalUrl())) {
            openUrl = document.getExternalUrl();
            downloadUrl = document.getExternalUrl();
        } else if ("LOCAL_DISK".equals(document.getStorageType()) && hasText(document.getStoredFileName())) {
            openUrl = "/api/files/documents/" + document.getId() + "/open";
            downloadUrl = "/api/files/documents/" + document.getId() + "/download";
        } else if (hasText(document.getFilePath())) {
            openUrl = document.getFilePath();
            downloadUrl = document.getFilePath();
        } else {
            openUrl = null;
            downloadUrl = null;
        }

        Long categoryId = null;
        String categoryName = null;
        if (document.getCategory() != null) {
            categoryId = document.getCategory().getId();
            categoryName = document.getCategory().getName();
        }

        Long departmentId = null;
        String departmentName = null;
        if (document.getDepartment() != null) {
            departmentId = document.getDepartment().getId();
            departmentName = document.getDepartment().getName();
        }

        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getReferenceCode(),
                document.getDescription(),
                document.getStatus(),
                document.getDocumentDate(),
                document.getFilePath(),
                document.getOriginalFileName(),
                document.getStoredFileName(),
                document.getMimeType(),
                document.getFileSize(),
                document.getExternalUrl(),
                document.getExternalDocument(),
                document.getStorageType(),
                document.getActive(),
                categoryId,
                categoryName,
                departmentId,
                departmentName,
                openUrl,
                downloadUrl,
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    public static class FileUploadResponseInternal {
        private final String originalFileName;
        private final String storedFileName;
        private final String mimeType;
        private final Long fileSize;
        private final String absolutePath;

        public FileUploadResponseInternal(String originalFileName, String storedFileName,
                                          String mimeType, Long fileSize, String absolutePath) {
            this.originalFileName = originalFileName;
            this.storedFileName = storedFileName;
            this.mimeType = mimeType;
            this.fileSize = fileSize;
            this.absolutePath = absolutePath;
        }

        public String getOriginalFileName() {
            return originalFileName;
        }

        public String getStoredFileName() {
            return storedFileName;
        }

        public String getMimeType() {
            return mimeType;
        }

        public Long getFileSize() {
            return fileSize;
        }

        public String getAbsolutePath() {
            return absolutePath;
        }
    }
}