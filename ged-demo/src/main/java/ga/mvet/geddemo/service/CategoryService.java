package ga.mvet.geddemo.service;

import ga.mvet.geddemo.dto.CategoryRequest;
import ga.mvet.geddemo.dto.CategoryResponse;
import ga.mvet.geddemo.exception.ResourceNotFoundException;
import ga.mvet.geddemo.model.Category;
import ga.mvet.geddemo.model.Department;
import ga.mvet.geddemo.repository.CategoryRepository;
import ga.mvet.geddemo.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final DepartmentService departmentService;
    private final DocumentRepository documentRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           DepartmentService departmentService,
                           DocumentRepository documentRepository) {
        this.categoryRepository = categoryRepository;
        this.departmentService = departmentService;
        this.documentRepository = documentRepository;
    }

    public CategoryResponse create(CategoryRequest request) {
        Department department = departmentService.findEntityById(request.getDepartmentId());

        categoryRepository.findByNameIgnoreCaseAndDepartmentId(request.getName().trim(), department.getId())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Cette catégorie existe déjà dans ce département.");
                });

        Category category = new Category();
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setActive(request.getActive() != null ? request.getActive() : true);
        category.setDepartment(department);

        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getByDepartment(Long departmentId) {
        departmentService.findEntityById(departmentId);

        return categoryRepository.findByDepartmentIdOrderByNameAsc(departmentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveByDepartment(Long departmentId) {
        departmentService.findEntityById(departmentId);

        return categoryRepository.findByDepartmentIdAndActiveTrueOrderByNameAsc(departmentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return mapToResponse(findEntityById(id));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findEntityById(id);
        Department targetDepartment = departmentService.findEntityById(request.getDepartmentId());

        boolean departmentChanged = category.getDepartment() == null
                || !category.getDepartment().getId().equals(targetDepartment.getId());

        if (departmentChanged && documentRepository.existsByCategoryId(id)) {
            throw new IllegalArgumentException(
                    "Impossible de déplacer cette catégorie vers un autre département car elle est déjà utilisée par des documents."
            );
        }

        categoryRepository.findByNameIgnoreCaseAndDepartmentId(request.getName().trim(), targetDepartment.getId())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Une autre catégorie porte déjà ce nom dans ce département.");
                });

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setDepartment(targetDepartment);

        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        return mapToResponse(categoryRepository.save(category));
    }

    public void delete(Long id) {
        Category category = findEntityById(id);

        if (documentRepository.existsByCategoryId(id)) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer cette catégorie car elle contient déjà des documents."
            );
        }

        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public Category findEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable avec l'id : " + id));
    }

    private CategoryResponse mapToResponse(Category category) {
        Long departmentId = null;
        String departmentName = null;

        if (category.getDepartment() != null) {
            departmentId = category.getDepartment().getId();
            departmentName = category.getDepartment().getName();
        }

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getActive(),
                departmentId,
                departmentName,
                category.getCreatedAt()
        );
    }
}