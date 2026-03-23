package ga.mvet.geddemo.repository;

import ga.mvet.geddemo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCaseAndDepartmentId(String name, Long departmentId);

    List<Category> findByActiveTrue();

    List<Category> findByDepartmentIdOrderByNameAsc(Long departmentId);

    List<Category> findByDepartmentIdAndActiveTrueOrderByNameAsc(Long departmentId);

    boolean existsByDepartmentId(Long departmentId);
}