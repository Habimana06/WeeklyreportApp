package com.example.weekly_report.repository;

import com.example.weekly_report.entity.SupervisorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupervisorProfileRepository extends JpaRepository<SupervisorProfile, Long> {
    
    Optional<SupervisorProfile> findBySupervisorId(String supervisorId);
    
    Optional<SupervisorProfile> findByUserAccountId(Long userAccountId);
}