package com.example.weekly_report.repository;

import com.example.weekly_report.entity.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {
    
    Optional<EmployeeProfile> findByEmployeeId(String employeeId);
    
    Optional<EmployeeProfile> findByUserAccountId(Long userAccountId);
    
    List<EmployeeProfile> findBySupervisorId(Long supervisorId);
    
    List<EmployeeProfile> findByDepartment(String department);
    
    @Query("SELECT e FROM EmployeeProfile e WHERE e.supervisor.id = :supervisorId")
    List<EmployeeProfile> findBySupervisorProfileId(@Param("supervisorId") Long supervisorId);
}

