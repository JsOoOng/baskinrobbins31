package com.kiosk.headquarter.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.AdminActionLog;
import com.kiosk.headquarter.repository.AdminActionLogRepository;
import com.kiosk.headquarter.dto.admin.AdminLogResponseDto;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.kiosk.headquarter.repository.HeadAuthMapper;
import com.kiosk.headquarter.dto.auth.HeadLoginEmployeeDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminLogService {

    private final AdminActionLogRepository adminActionLogRepository;
    private final HeadAuthMapper headAuthMapper;

    @Transactional
    public void logAction(String type, String action) {
        try {
            String administrator = "관리자";
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                String role = authentication.getAuthorities().toString();
                String roleName = "관리자";
                if (role.contains("SUPER_ADMIN")) {
                    roleName = "최고 관리자";
                } else if (role.contains("HEAD_ADMIN") || role.contains("ADMIN")) {
                    roleName = "본사 관리자";
                }

                String name = "";
                if (authentication.getPrincipal() instanceof Integer) {
                    Integer employeeId = (Integer) authentication.getPrincipal();
                    HeadLoginEmployeeDTO employee = headAuthMapper.findEmployeeByEmployeeId(employeeId);
                    if (employee != null && employee.getName() != null) {
                        name = employee.getName() + " ";
                    }
                }
                
                administrator = name + "(" + roleName + ")";
            }

            AdminActionLog logEntry = AdminActionLog.builder()
                    .administrator(administrator)
                    .type(type)
                    .action(action)
                    .build();

            adminActionLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to save admin action log: {}", e.getMessage());
        }
    }

    public List<AdminLogResponseDto> getRecentLogs() {
        List<AdminActionLog> logs = adminActionLogRepository.findTop100ByOrderByCreatedAtDesc();
        
        return logs.stream()
                .map(log -> AdminLogResponseDto.from(log, formatDate(log.getCreatedAt())))
                .collect(Collectors.toList());
    }

    private String formatDate(LocalDateTime logDate) {
        if (logDate == null) {
            return "";
        }
        LocalDate today = LocalDate.now();
        LocalDate targetDate = logDate.toLocalDate();

        if (targetDate.equals(today)) {
            return "오늘 " + logDate.format(DateTimeFormatter.ofPattern("a hh:mm"));
        } else if (targetDate.equals(today.minusDays(1))) {
            return "어제 " + logDate.format(DateTimeFormatter.ofPattern("a hh:mm"));
        } else {
            return logDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd a hh:mm"));
        }
    }
}
