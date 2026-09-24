package com.example.financebackend.repository;

import com.example.financebackend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByUserUserIdAndTransactionDateBetween(Integer userId, LocalDate startDate, LocalDate endDate);
    List<Transaction> findByUserUserId(Integer userId);
}
