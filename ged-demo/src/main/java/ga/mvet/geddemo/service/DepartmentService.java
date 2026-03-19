package ga.mvet.geddemo.service;

import ga.mvet.geddemo.dto.DepartmentRequest;
import ga.mvet.geddemo.dto.DepartmentResponse;
import ga.mvet.geddemo.exception.ResourceNotFoundException;
import ga.mvet.geddemo.model.Department;
import ga.mvet.geddemo.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public DepartmentResponse create(DepartmentRequest request) {
        departmentRepository.findByNameIgnoreCase(request.getName())
                .ifPresent(d -> {
                    throw new IllegalArgumentException("Ce département existe déjà");
                });

        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setActive(request.getActive() != null ? request.getActive() : true);

        return mapToResponse(departmentRepository.save(department));
    }

    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DepartmentResponse getById(Long id) {
        return mapToResponse(findEntityById(id));
    }

    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = findEntityById(id);

        departmentRepository.findByNameIgnoreCase(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(d -> {
                    throw new IllegalArgumentException("Un autre département porte déjà ce nom");
                });

        department.setName(request.getName());
        department.setDescription(request.getDescription());

        if (request.getActive() != null) {
            department.setActive(request.getActive());
        }

        return mapToResponse(departmentRepository.save(department));
    }

    public void delete(Long id) {
        Department department = findEntityById(id);
        departmentRepository.delete(department);
    }

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