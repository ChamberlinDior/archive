package ga.mvet.geddemo.dto;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public class DocumentCreateMultipartRequest {

    private String title;
    private String description;
    private String status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate documentDate;

    private Boolean active;
    private Long categoryId;
    private Long departmentId;
    private MultipartFile file;

    public DocumentCreateMultipartRequest() {
    }

    public String getTitle() {
        return title;
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

    public Boolean getActive() {
        return active;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}