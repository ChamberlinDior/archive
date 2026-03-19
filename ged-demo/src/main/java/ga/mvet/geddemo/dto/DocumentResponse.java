package ga.mvet.geddemo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DocumentResponse {

    private Long id;
    private String title;
    private String referenceCode;
    private String description;
    private String status;
    private LocalDate documentDate;
    private String filePath;
    private String originalFileName;
    private String storedFileName;
    private String mimeType;
    private Long fileSize;
    private String externalUrl;
    private Boolean externalDocument;
    private String storageType;
    private Boolean active;
    private Long categoryId;
    private String categoryName;
    private Long departmentId;
    private String departmentName;
    private String openUrl;
    private String downloadUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DocumentResponse() {
    }

    public DocumentResponse(Long id, String title, String referenceCode, String description, String status,
                            LocalDate documentDate, String filePath, String originalFileName,
                            String storedFileName, String mimeType, Long fileSize, String externalUrl,
                            Boolean externalDocument, String storageType, Boolean active,
                            Long categoryId, String categoryName, Long departmentId, String departmentName,
                            String openUrl, String downloadUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
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
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.openUrl = openUrl;
        this.downloadUrl = downloadUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getOpenUrl() {
        return openUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
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

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public void setOpenUrl(String openUrl) {
        this.openUrl = openUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}