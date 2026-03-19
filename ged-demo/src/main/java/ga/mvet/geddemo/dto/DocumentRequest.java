package ga.mvet.geddemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class DocumentRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 180)
    private String title;

    @NotBlank(message = "La référence est obligatoire")
    @Size(max = 80)
    private String referenceCode;

    @Size(max = 2000)
    private String description;

    @NotBlank(message = "Le statut est obligatoire")
    @Size(max = 30)
    private String status;

    @NotNull(message = "La date du document est obligatoire")
    private LocalDate documentDate;

    @Size(max = 500)
    private String filePath;

    @Size(max = 1000)
    private String externalUrl;

    private Boolean externalDocument;

    private Boolean active;

    @NotNull(message = "La catégorie est obligatoire")
    private Long categoryId;

    @NotNull(message = "Le département est obligatoire")
    private Long departmentId;

    public DocumentRequest() {
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

    public String getExternalUrl() {
        return externalUrl;
    }

    public Boolean getExternalDocument() {
        return externalDocument;
    }

    public Boolean getActive() {
        return active;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getDepartmentId() {
        return departmentId;
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

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public void setExternalDocument(Boolean externalDocument) {
        this.externalDocument = externalDocument;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}