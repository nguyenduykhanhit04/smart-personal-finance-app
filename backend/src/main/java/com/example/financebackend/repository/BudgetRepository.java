package com.example.financebackend.repository;

import com.example.financebackend.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {
    List<Budget> findByUserUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Integer userId, LocalDate startDate, LocalDate endDate);
    List<Budget> findByUserUserId(Integer userId);
}
