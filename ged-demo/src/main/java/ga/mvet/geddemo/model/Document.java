package ga.mvet.geddemo.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, unique = true, length = 80)
    private String referenceCode;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false)
    private LocalDate documentDate;

    @Column(length = 500)
    private String filePath;

    @Column(length = 255)
    private String originalFileName;

    @Column(length = 255, unique = true)
    private String storedFileName;

    @Column(length = 150)
    private String mimeType;

    private Long fileSize;

    @Column(length = 1000)
    private String externalUrl;

    @Column(nullable = false)
    private Boolean externalDocument = false;

    @Column(length = 30)
    private String storageType; // LOCAL_DISK, EXTERNAL_LINK, MANUAL_PATH

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Document() {
    }

    public Document(Long id, String title, String referenceCode, String description, String status,
                    LocalDate documentDate, String filePath, String originalFileName, String storedFileName,
                    String mimeType, Long fileSize, String externalUrl, Boolean externalDocument,
                    String storageType, Boolean active, Category category, Department department,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.referenceCode = referenceCode;
        this.description = description;
        this.status = status;
        this.documentDate = documentDate;
        this.filePath = filePath;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.externalUrl = externalUrl;
        this.externalDocument = externalDocument;
        this.storageType = storageType;
        this.active = active;
        this.category = category;
        this.department = department;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.active == null) {
            this.active = true;
        }

        if (this.externalDocument == null) {
            this.externalDocument = false;
        }

        if (this.storageType == null || this.storageType.isBlank()) {
            this.storageType = "MANUAL_PATH";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getDocumentDate() {
        return documentDate;
    }

    public String getFilePath() {
        return filePath;
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

    public String getExternalUrl() {
        return externalUrl;
    }

    public Boolean getExternalDocument() {
        return externalDocument;
    }

    public String getStorageType() {
        return storageType;
    }

    public Boolean getActive() {
        return active;
    }

    public Category getCategory() {
        return category;
    }

    public Department getDepartment() {
        return department;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDocumentDate(LocalDate documentDate) {
        this.documentDate = documentDate;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public void setExternalDocument(Boolean externalDocument) {
        this.externalDocument = externalDocument;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}