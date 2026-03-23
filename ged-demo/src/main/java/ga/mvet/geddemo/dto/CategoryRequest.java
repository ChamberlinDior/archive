package ga.mvet.geddemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CategoryRequest {

    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    private Boolean active;

    @NotNull(message = "Le département est obligatoire pour une catégorie")
    private Long departmentId;

    public CategoryRequest() {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getActive() {
        return active;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}