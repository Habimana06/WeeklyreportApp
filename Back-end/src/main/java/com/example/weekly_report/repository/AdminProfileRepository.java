package com.example.weekly_report.repository;

import com.example.weekly_report.entity.AdminProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminProfileRepository extends JpaRepository<AdminProfile, Long> {
    
    Optional<AdminProfile> findByAdminId(String adminId);
    
    Optional<AdminProfile> findByUserAccountId(Long userAccountId);
}
