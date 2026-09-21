package com.bank.service.impl;

import com.bank.dto.AdminDashboardDTO;
import com.bank.dto.PendingApprovalDTO;
import com.bank.dto.SystemHealthDTO;
import com.bank.dto.TransactionActivityDTO;
import com.bank.dto.UserActivityDTO;
import com.bank.dto.UserStatusUpdateDTO;
import com.bank.dto.UserSummaryDTO;
import com.bank.entity.LoanApplication;
import com.bank.entity.LoanStatus;
import com.bank.entity.LoginEvent;
import com.bank.entity.User;
import com.bank.repository.AdminDashboardRepository;
import com.bank.repository.AccountRepository;
import com.bank.repository.LoanApplicationRepository;
import com.bank.repository.LoginEventRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import com.bank.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardServiceImpl.class);

    private final AdminDashboardRepository adminDashboardRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final LoginEventRepository loginEventRepository;
    private final AccountRepository accountRepository;
    private final DataSource dataSource;

    private final Instant startTime = Instant.now();

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboard() {
        Long totalUsers = adminDashboardRepository.countUsers();
        Long totalActiveUsers = adminDashboardRepository.countActiveUsers();
        Long totalLoans = adminDashboardRepository.countLoans();
        Long pendingApprovals = adminDashboardRepository.countPendingLoans();
        Long totalTransactions = adminDashboardRepository.countTransactions();

        BigDecimal totalIncome = adminDashboardRepository.sumPlatformIncome();
        BigDecimal totalExpense = adminDashboardRepository.sumPlatformExpense();
        BigDecimal savingsRate = calculateSavingsRate(totalIncome, totalExpense);

        // Total accounts and bank-wide balance
        Long totalAccounts = accountRepository.count();
        BigDecimal totalBalance = accountRepository.findAll().stream()
                .filter(a -> a.getIsActive())
                .map(a -> a.getBalance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AdminDashboardDTO.builder()
                .totalUsers(totalUsers)
                .totalActiveUsers(totalActiveUsers)
                .totalLoans(totalLoans)
                .pendingApprovals(pendingApprovals)
                .totalTransactions(totalTransactions)
                .platformSavingsRate(savingsRate)
                .totalAccounts(totalAccounts)
                .totalBalance(totalBalance)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendingApprovalDTO> getPendingApprovals(String module) {
        if (module != null && !"LOAN".equalsIgnoreCase(module)) {
            return Collections.emptyList();
        }

        List<LoanApplication> pendingLoans = loanApplicationRepository.findByStatus(LoanStatus.PENDING);
        Set<Long> userIds = pendingLoans.stream()
                .map(LoanApplication::getUserId)
                .collect(Collectors.toSet());

        Map<Long, String> userNames = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getFullName));

        return pendingLoans.stream()
                .sorted((a, b) -> a.getAppliedAt().compareTo(b.getAppliedAt()))
                .map(app -> PendingApprovalDTO.builder()
                        .id(app.getId())
                        .type("LOAN_APPLICATION")
                        .applicantName(userNames.getOrDefault(app.getUserId(), "Unknown"))
                        .productName(app.getLoanProduct() != null ? app.getLoanProduct().getProductName() : "N/A")
                        .requestedAmount(app.getAmount())
                        .appliedAt(app.getAppliedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SystemHealthDTO getSystemHealth() {
        String dbStatus = checkDatabase();
        long uptimeSeconds = Duration.between(startTime, Instant.now()).getSeconds();

        return SystemHealthDTO.builder()
                .dbStatus(dbStatus)
                .activeSessions(0L)
                .serverUptimeSeconds(uptimeSeconds)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryDTO> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserSummaryDTO.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .isActive(user.getIsActive())
                        .createdAt(user.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, UserStatusUpdateDTO request, Long actingAdminId) {
        if (request == null || request.getIsActive() == null) {
            throw new IllegalArgumentException("Missing isActive value");
        }

        if (actingAdminId != null && actingAdminId.equals(userId) && !request.getIsActive()) {
            throw new IllegalArgumentException("Admin cannot deactivate own account");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setIsActive(request.getIsActive());
        user.setApproved(request.getIsActive());
        userRepository.save(user);

        // Also activate/deactivate user's bank accounts
        accountRepository.findByUser(user).forEach(account -> {
            account.setIsActive(request.getIsActive());
            accountRepository.save(account);
        });

        logger.info("ADMIN_ACTION userStatusUpdate adminId={} targetUserId={} isActive={}",
                actingAdminId, userId, request.getIsActive());
    }

    @Override
    @Transactional(readOnly = true)
    public UserActivityDTO getUserActivity(Long userId) {
        var page = PageRequest.of(0, 20);
        List<TransactionActivityDTO> transactions = transactionRepository
                .findByAccount_User_IdOrderByTransactionDateDesc(userId, page)
                .stream()
                .map(tx -> TransactionActivityDTO.builder()
                        .id(tx.getId())
                        .type(tx.getType())
                        .amount(tx.getAmount())
                        .transactionDate(tx.getTransactionDate())
                        .description(tx.getDescription())
                        .build())
                .toList();

                    List<LoginEvent> loginEvents = loginEventRepository
                        .findByUserIdOrderByLoginAtDesc(userId, PageRequest.of(0, 10));

        return UserActivityDTO.builder()
                .recentTransactions(transactions)
                        .recentLogins(loginEvents.stream()
                            .map(LoginEvent::getLoginAt)
                            .toList())
                .build();
    }

    private BigDecimal calculateSavingsRate(BigDecimal income, BigDecimal expense) {
        if (income == null || income.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal savings = income.subtract(expense == null ? BigDecimal.ZERO : expense);
        return savings.multiply(BigDecimal.valueOf(100))
            .divide(income, 2, RoundingMode.HALF_UP);
    }

    private String checkDatabase() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement("SELECT 1")) {
            statement.execute();
            return "UP";
        } catch (Exception ex) {
            logger.warn("Database health check failed", ex);
            return "DOWN";
        }
    }
}
