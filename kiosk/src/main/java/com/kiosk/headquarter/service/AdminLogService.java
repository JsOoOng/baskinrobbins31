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

/**
 * [코드 흐름 안내] AdminLogService
 *
 * <p>역할: 본사 관리의 본사 관리자 작업 로그 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> AdminActionLogRepository, HeadAuthMapper -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminLogService {

    private final AdminActionLogRepository adminActionLogRepository;
    private final HeadAuthMapper headAuthMapper;

    @Transactional
    /**
     * [메서드 흐름] logAction
     * Controller 또는 상위 서비스에서 호출되어 AdminActionLogRepository, HeadAuthMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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

    /**
     * [메서드 흐름] getRecentLogs
     * Controller 또는 상위 서비스에서 호출되어 AdminActionLogRepository, HeadAuthMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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
