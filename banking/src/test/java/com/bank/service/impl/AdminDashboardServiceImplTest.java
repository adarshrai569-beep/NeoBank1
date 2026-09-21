package com.bank.service.impl;

import com.bank.dto.AdminDashboardDTO;
import com.bank.dto.UserStatusUpdateDTO;
import com.bank.entity.User;
import com.bank.repository.AdminDashboardRepository;
import com.bank.repository.LoanApplicationRepository;
import com.bank.repository.LoginEventRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceImplTest {

    @Mock
    private AdminDashboardRepository adminDashboardRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LoginEventRepository loginEventRepository;

    @Mock
    private DataSource dataSource;

    private AdminDashboardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminDashboardServiceImpl(
                adminDashboardRepository,
                loanApplicationRepository,
                userRepository,
                transactionRepository,
                loginEventRepository,
                dataSource
        );
    }

    @Test
    void getDashboardCalculatesSavingsRate() {
        when(adminDashboardRepository.countUsers()).thenReturn(10L);
        when(adminDashboardRepository.countActiveUsers()).thenReturn(9L);
        when(adminDashboardRepository.countLoans()).thenReturn(5L);
        when(adminDashboardRepository.countPendingLoans()).thenReturn(2L);
        when(adminDashboardRepository.countTransactions()).thenReturn(100L);
        when(adminDashboardRepository.sumPlatformIncome()).thenReturn(new BigDecimal("1000.00"));
        when(adminDashboardRepository.sumPlatformExpense()).thenReturn(new BigDecimal("250.00"));

        AdminDashboardDTO result = service.getDashboard();

        assertThat(result.getPlatformSavingsRate()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(result.getTotalUsers()).isEqualTo(10L);
    }

    @Test
    void updateUserStatusPreventsSelfDeactivation() {
        UserStatusUpdateDTO request = new UserStatusUpdateDTO();
        request.setIsActive(false);

        assertThatThrownBy(() -> service.updateUserStatus(5L, request, 5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Admin cannot deactivate own account");
    }

    @Test
    void updateUserStatusPersistsChange() {
        UserStatusUpdateDTO request = new UserStatusUpdateDTO();
        request.setIsActive(false);

        User user = new User();
        user.setId(2L);
        user.setIsActive(true);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        service.updateUserStatus(2L, request, 99L);

        assertThat(user.getIsActive()).isFalse();
        verify(userRepository).save(user);
    }
}
