package ga.mvet.geddemo.service;

import ga.mvet.geddemo.dto.DepartmentRequest;
import ga.mvet.geddemo.dto.DepartmentResponse;
import ga.mvet.geddemo.exception.ResourceNotFoundException;
import ga.mvet.geddemo.model.Department;
import ga.mvet.geddemo.repository.CategoryRepository;
import ga.mvet.geddemo.repository.DepartmentRepository;
import ga.mvet.geddemo.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CategoryRepository categoryRepository;
    private final DocumentRepository documentRepository;

    public DepartmentService(DepartmentRepository departmentRepository,
                             CategoryRepository categoryRepository,
                             DocumentRepository documentRepository) {
        this.departmentRepository = departmentRepository;
        this.categoryRepository = categoryRepository;
        this.documentRepository = documentRepository;
    }

    public DepartmentResponse create(DepartmentRequest request) {
        departmentRepository.findByNameIgnoreCase(request.getName().trim())
                .ifPresent(d -> {
                    throw new IllegalArgumentException("Ce département existe déjà");
                });

        Department department = new Department();
        department.setName(request.getName().trim());
        department.setDescription(request.getDescription());
        department.setActive(request.getActive() != null ? request.getActive() : true);

        return mapToResponse(departmentRepository.save(department));
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Department::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        return mapToResponse(findEntityById(id));
    }

    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = findEntityById(id);

        departmentRepository.findByNameIgnoreCase(request.getName().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(d -> {
                    throw new IllegalArgumentException("Un autre département porte déjà ce nom");
                });

        department.setName(request.getName().trim());
        department.setDescription(request.getDescription());

        if (request.getActive() != null) {
            department.setActive(request.getActive());
        }

        return mapToResponse(departmentRepository.save(department));
    }

    public void delete(Long id) {
        Department department = findEntityById(id);

        if (categoryRepository.existsByDepartmentId(id)) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer ce département car il contient encore des catégories."
            );
        }

        if (documentRepository.existsByDepartmentId(id)) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer ce département car il contient encore des documents."
            );
        }

        departmentRepository.delete(department);
    }

    @Transactional(readOnly = true)
    public Department findEntityById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département introuvable avec l'id : " + id));
    }

    private DepartmentResponse mapToResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getActive(),
                department.getCreatedAt()
        );
    }
}