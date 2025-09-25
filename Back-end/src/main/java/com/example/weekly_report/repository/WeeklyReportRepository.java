package com.example.weekly_report.repository;

import com.example.weekly_report.entity.ReportStatus;
import com.example.weekly_report.entity.WeeklyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {
    
    List<WeeklyReport> findByEmployeeIdOrderByWeekStartDateDesc(Long employeeId);
    
    List<WeeklyReport> findByEmployeeSupervisorIdOrderByWeekStartDateDesc(Long supervisorId);
    
    List<WeeklyReport> findByStatusOrderByWeekStartDateDesc(ReportStatus status);
    
    List<WeeklyReport> findAllByOrderByWeekStartDateDesc();
    
    List<WeeklyReport> findByWeekStartDateBetween(LocalDate startDate, LocalDate endDate);
    
    Optional<WeeklyReport> findByEmployeeIdAndWeekStartDate(Long employeeId, LocalDate weekStartDate);
    
    @Query("SELECT wr FROM WeeklyReport wr WHERE wr.employee.department = :department ORDER BY wr.weekStartDate DESC")
    List<WeeklyReport> findByDepartmentOrderByWeekStartDateDesc(@Param("department") String department);
    
    @Query("SELECT COUNT(wr) FROM WeeklyReport wr WHERE wr.employee.id = :employeeId AND wr.status = :status")
    Long countByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") ReportStatus status);
    
    @Query("SELECT wr FROM WeeklyReport wr WHERE wr.weekStartDate >= :startDate AND wr.weekStartDate <= :endDate AND wr.status = :status")
    List<WeeklyReport> findByDateRangeAndStatus(@Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate, 
                                               @Param("status") ReportStatus status);
}
