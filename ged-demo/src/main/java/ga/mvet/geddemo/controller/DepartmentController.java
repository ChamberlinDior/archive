package ga.mvet.geddemo.controller;

import ga.mvet.geddemo.dto.CategoryResponse;
import ga.mvet.geddemo.dto.DepartmentRequest;
import ga.mvet.geddemo.dto.DepartmentResponse;
import ga.mvet.geddemo.dto.DocumentResponse;
import ga.mvet.geddemo.service.CategoryService;
import ga.mvet.geddemo.service.DepartmentService;
import ga.mvet.geddemo.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final CategoryService categoryService;
    private final DocumentService documentService;

    public DepartmentController(DepartmentService departmentService,
                                CategoryService categoryService,
                                DocumentService documentService) {
        this.departmentService = departmentService;
        this.categoryService = categoryService;
        this.documentService = documentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse create(@Valid @RequestBody DepartmentRequest request) {
        return departmentService.create(request);
    }

    @GetMapping
    public List<DepartmentResponse> getAll() {
        return departmentService.getAll();
    }

    @GetMapping("/{id}")
    public DepartmentResponse getById(@PathVariable Long id) {
        return departmentService.getById(id);
    }

    @GetMapping("/{departmentId}/categories")
    public List<CategoryResponse> getDepartmentCategories(@PathVariable Long departmentId,
                                                          @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        return activeOnly
                ? categoryService.getActiveByDepartment(departmentId)
                : categoryService.getByDepartment(departmentId);
    }

    @GetMapping("/{departmentId}/documents")
    public List<DocumentResponse> getDepartmentDocuments(@PathVariable Long departmentId,
                                                         @RequestParam(required = false) Long categoryId,
                                                         @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        if (categoryId != null) {
            return activeOnly
                    ? documentService.getActiveByDepartmentAndCategory(departmentId, categoryId)
                    : documentService.getByDepartmentAndCategory(departmentId, categoryId);
        }

        return activeOnly
                ? documentService.getActiveByDepartment(departmentId)
                : documentService.getByDepartment(departmentId);
    }

    @PutMapping("/{id}")
    public DepartmentResponse update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return departmentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        departmentService.delete(id);
    }
}