package com.example.financebackend.repository;

import com.example.financebackend.model.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Integer> {
    List<RecurringTransaction> findByUserUserId(Integer userId);
    List<RecurringTransaction> findByIsActiveTrueAndNextRunDateLessThanEqual(LocalDate date);
}
