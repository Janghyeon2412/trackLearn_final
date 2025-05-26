package com.multi.tracklearn.service;

import com.multi.tracklearn.domain.Category;
import com.multi.tracklearn.dto.CategoryDTO;
import com.multi.tracklearn.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    public List<CategoryDTO> findAll() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryDTO(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }


}
