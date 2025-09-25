package com.example.weekly_report.repository;

import com.example.weekly_report.entity.UserAccount;
import com.example.weekly_report.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    
    Optional<UserAccount> findByUsername(String username);
    
    Optional<UserAccount> findByEmail(String email);
    
    List<UserAccount> findByRole(UserRole role);
    
    List<UserAccount> findByEnabled(boolean enabled);
    
    @Query("SELECT u FROM UserAccount u WHERE u.role = :role AND u.enabled = true")
    List<UserAccount> findActiveUsersByRole(@Param("role") UserRole role);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}
