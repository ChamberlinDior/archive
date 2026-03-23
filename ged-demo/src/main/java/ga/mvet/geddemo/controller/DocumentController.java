package ga.mvet.geddemo.controller;

import ga.mvet.geddemo.dto.DocumentCreateMultipartRequest;
import ga.mvet.geddemo.dto.DocumentRequest;
import ga.mvet.geddemo.dto.DocumentResponse;
import ga.mvet.geddemo.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse create(@Valid @RequestBody DocumentRequest request) {
        return documentService.create(request);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createWithUpload(@ModelAttribute DocumentCreateMultipartRequest request) {
        return documentService.createWithUpload(request);
    }

    @PostMapping("/link")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createExternalLink(@Valid @RequestBody DocumentRequest request) {
        return documentService.createExternalLink(request);
    }

    @GetMapping
    public List<DocumentResponse> getAll() {
        return documentService.getAll();
    }

    @GetMapping("/active")
    public List<DocumentResponse> getActiveDocuments() {
        return documentService.getActiveDocuments();
    }

    @GetMapping("/next-reference")
    public Map<String, String> getNextReference() {
        return Map.of("referenceCode", documentService.getNextReferenceCodePreview());
    }

    @GetMapping("/{id}")
    public DocumentResponse getById(@PathVariable Long id) {
        return documentService.getById(id);
    }

    @GetMapping("/status/{status}")
    public List<DocumentResponse> getByStatus(@PathVariable String status) {
        return documentService.getByStatus(status);
    }

    @GetMapping("/search")
    public List<DocumentResponse> searchByTitle(@RequestParam String keyword) {
        return documentService.searchByTitle(keyword);
    }

    @PutMapping("/{id}")
    public DocumentResponse update(@PathVariable Long id, @Valid @RequestBody DocumentRequest request) {
        return documentService.update(id, request);
    }

    @PutMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentResponse updateWithUpload(@PathVariable Long id,
                                             @ModelAttribute DocumentCreateMultipartRequest request) {
        return documentService.updateWithUpload(id, request);
    }

    @PutMapping("/{id}/link")
    public DocumentResponse updateExternalLink(@PathVariable Long id,
                                               @Valid @RequestBody DocumentRequest request) {
        return documentService.updateExternalLink(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        documentService.delete(id);
    }
}