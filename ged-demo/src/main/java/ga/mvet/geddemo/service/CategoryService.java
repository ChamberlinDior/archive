package ga.mvet.geddemo.service;

import ga.mvet.geddemo.dto.CategoryRequest;
import ga.mvet.geddemo.dto.CategoryResponse;
import ga.mvet.geddemo.exception.ResourceNotFoundException;
import ga.mvet.geddemo.model.Category;
import ga.mvet.geddemo.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse create(CategoryRequest request) {
        categoryRepository.findByNameIgnoreCase(request.getName())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Cette catégorie existe déjà");
                });

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setActive(request.getActive() != null ? request.getActive() : true);

        return mapToResponse(categoryRepository.save(category));
    }

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getById(Long id) {
        return mapToResponse(findEntityById(id));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findEntityById(id);

        categoryRepository.findByNameIgnoreCase(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Une autre catégorie porte déjà ce nom");
                });

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        return mapToResponse(categoryRepository.save(category));
    }

    public void delete(Long id) {
        Category category = findEntityById(id);
        categoryRepository.delete(category);
    }

    public Category findEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable avec l'id : " + id));
    }

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getActive(),
                category.getCreatedAt()
        );
    }
}