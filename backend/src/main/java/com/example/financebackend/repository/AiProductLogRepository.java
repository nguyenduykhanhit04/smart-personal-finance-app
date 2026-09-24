package com.example.financebackend.repository;

import com.example.financebackend.model.AiProductLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiProductLogRepository extends JpaRepository<AiProductLog, Integer> {
}
