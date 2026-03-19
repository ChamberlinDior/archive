package ga.mvet.geddemo.dto;

public class FileUploadResponse {

    private String originalFileName;
    private String storedFileName;
    private String mimeType;
    private Long fileSize;
    private String absolutePath;

    public FileUploadResponse() {
    }

    public FileUploadResponse(String originalFileName, String storedFileName, String mimeType,
                              Long fileSize, String absolutePath) {
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

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }
}