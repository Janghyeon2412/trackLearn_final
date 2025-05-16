package com.multi.tracklearn.repository;

import com.multi.tracklearn.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
