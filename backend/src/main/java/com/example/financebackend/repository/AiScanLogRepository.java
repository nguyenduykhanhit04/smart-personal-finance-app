package com.example.financebackend.repository;

import com.example.financebackend.model.AiScanLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiScanLogRepository extends JpaRepository<AiScanLog, Integer> {
}
