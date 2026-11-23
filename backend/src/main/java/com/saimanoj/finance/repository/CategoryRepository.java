package com.saimanoj.finance.repository;

import com.saimanoj.finance.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByType(String type);
}
