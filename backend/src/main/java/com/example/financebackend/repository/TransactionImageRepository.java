package com.example.financebackend.repository;

import com.example.financebackend.model.TransactionImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionImageRepository extends JpaRepository<TransactionImage, Integer> {
    List<TransactionImage> findByTransactionTransactionId(Integer transactionId);
}
