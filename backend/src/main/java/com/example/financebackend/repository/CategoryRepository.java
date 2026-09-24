package com.example.financebackend.repository;

import com.example.financebackend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByUserUserIdOrIsDefaultTrue(Integer userId);
}
