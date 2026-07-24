package com.kiosk.branch.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kiosk.branch.auth.dto.AuthRequest;
import com.kiosk.branch.auth.dto.AuthResponse;
import com.kiosk.branch.auth.repository.EmployeeMapper;
import com.kiosk.entity.Employee;
import com.kiosk.entity.Store;
import com.kiosk.entity.enums.EmployeeStatus;
import com.kiosk.entity.enums.Role;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private EmployeeMapper employeeMapper;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(
                employeeMapper,
                passwordEncoder
        );
    }

    @Test
    void logsInWithLegacyPlainTextPassword() {
        Employee employee = employeeWithPassword("1234");
        when(employeeMapper.findByLoginId("emp001"))
                .thenReturn(Optional.of(employee));

        AuthResponse response =
                authService.login(request(" emp001 ", "1234"));

        assertThat(response.getEmployeeId()).isEqualTo(1);
        assertThat(response.getRole()).isEqualTo("MANAGER");
        assertThat(response.getStoreId()).isEqualTo(1);
    }

    @Test
    void logsInWithBCryptPassword() {
        Employee employee =
                employeeWithPassword(
                        passwordEncoder.encode("1234")
                );
        when(employeeMapper.findByLoginId("emp001"))
                .thenReturn(Optional.of(employee));

        AuthResponse response =
                authService.login(request("emp001", "1234"));

        assertThat(response.getEmployeeId()).isEqualTo(1);
    }

    @Test
    void rejectsWrongPlainTextPassword() {
        Employee employee = employeeWithPassword("1234");
        when(employeeMapper.findByLoginId("emp001"))
                .thenReturn(Optional.of(employee));

        assertThatThrownBy(() ->
                authService.login(
                        request("emp001", "wrong")
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "아이디 또는 비밀번호가 일치하지 않습니다."
                );
    }

    private AuthRequest request(
            String loginId,
            String password
    ) {
        AuthRequest request = new AuthRequest();
        request.setLoginId(loginId);
        request.setPassword(password);
        return request;
    }

    private Employee employeeWithPassword(String password) {
        Store store = Store.builder()
                .id(1)
                .storeName("강남점")
                .region("서울")
                .address("서울")
                .build();

        return Employee.builder()
                .id(1)
                .store(store)
                .loginId("emp001")
                .password(password)
                .name("김점장")
                .role(Role.MANAGER)
                .status(EmployeeStatus.EMPLOYED)
                .build();
    }
}
