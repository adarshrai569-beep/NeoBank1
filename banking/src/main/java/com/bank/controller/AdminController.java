package com.bank.controller;

import com.bank.dto.AdminDashboardDTO;
import com.bank.dto.PendingApprovalDTO;
import com.bank.dto.SystemHealthDTO;
import com.bank.dto.UserActivityDTO;
import com.bank.dto.UserStatusUpdateDTO;
import com.bank.dto.UserSummaryDTO;
import com.bank.entity.Account;
import com.bank.entity.Role;
import com.bank.entity.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;
import com.bank.service.AdminDashboardService;
import com.bank.util.AuditLogger;
import com.bank.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin dashboard and control endpoints")
public class AdminController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AdminDashboardService adminDashboardService;
    private final SecurityUtil securityUtil;
    private final AuditLogger auditLogger;
    private final com.bank.repository.TransactionRepository transactionRepository;

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Returns a list of registered users for admin management")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User list returned", content = @Content(schema = @Schema(implementation = UserSummaryDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public List<UserSummaryDTO> getAllUsers() {
        return adminDashboardService.getUsers();
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard metrics", description = "Returns platform-wide KPI metrics")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard metrics returned", content = @Content(schema = @Schema(implementation = AdminDashboardDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public AdminDashboardDTO getDashboard() {
        return adminDashboardService.getDashboard();
    }

    @GetMapping("/pending-approvals")
        @Operation(summary = "Get pending approvals", description = "Returns pending approval items, optionally filtered by module")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pending approvals returned", content = @Content(schema = @Schema(implementation = PendingApprovalDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
        })
    public List<PendingApprovalDTO> getPendingApprovals(
            @RequestParam(required = false) String module
    ) {
        return adminDashboardService.getPendingApprovals(module);
    }

    @GetMapping("/system-health")
    @Operation(summary = "Get system health", description = "Returns database status, session count, and uptime")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "System health returned", content = @Content(schema = @Schema(implementation = SystemHealthDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public SystemHealthDTO getSystemHealth() {
        return adminDashboardService.getSystemHealth();
    }

    @GetMapping("/users/{id}/activity")
    @Operation(summary = "Get user activity", description = "Returns recent transactions and login timestamps for a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User activity returned", content = @Content(schema = @Schema(implementation = UserActivityDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public UserActivityDTO getUserActivity(@PathVariable Long id) {
        return adminDashboardService.getUserActivity(id);
    }

    @PatchMapping("/users/{id}/status")
        @Operation(summary = "Update user status", description = "Activate or deactivate a user")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
        })
    public void updateUserStatusPatch(
            @PathVariable Long id,
            @RequestBody UserStatusUpdateDTO request
    ) {
        adminDashboardService.updateUserStatus(id, request, securityUtil.getUserId());
        auditLogger.logAdminAction(securityUtil.getUserId(), "USER_STATUS_PATCH", "USER", id);
    }

    @PutMapping("/users/{id}/status")
    public void updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {
        User user = userRepository.findById(id).orElseThrow();
        user.setIsActive(active);
        userRepository.save(user);
        auditLogger.logAdminAction(securityUtil.getUserId(), "USER_STATUS_PUT", "USER", id);
    }

    @PutMapping("/users/{id}/role")
    public void updateUserRole(
            @PathVariable Long id,
            @RequestParam Role role
    ) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(role);
        userRepository.save(user);
        auditLogger.logAdminAction(securityUtil.getUserId(), "USER_ROLE_UPDATE", "USER", id);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();

        // deactivate user
        user.setIsActive(false);
        user.setApproved(false);

        // ✅ Use findByUserEmail instead of findByUser
        List<Account> accounts = accountRepository.findByUserEmail(user.getEmail());
        accounts.forEach(acc -> acc.setIsFrozen(true));

        accountRepository.saveAll(accounts);
        userRepository.save(user);
        auditLogger.logAdminAction(securityUtil.getUserId(), "USER_DEACTIVATE", "USER", id);
    }

    @PutMapping("/users/{id}/approve")
    public void approveUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setApproved(true);
        user.setIsActive(true);
        userRepository.save(user);

        // ✅ Use findByUserEmail instead of findByUser
        List<Account> accounts = accountRepository.findByUserEmail(user.getEmail());
        accounts.forEach(acc -> {
            acc.setIsActive(true);
            acc.setIsFrozen(false);
        });
        accountRepository.saveAll(accounts);
        auditLogger.logAdminAction(securityUtil.getUserId(), "USER_APPROVE", "USER", id);
    }

    @PutMapping("/accounts/{id}/freeze")
    public void freezeAccount(@PathVariable Long id) {
        Account account = accountRepository.findById(id).orElseThrow();
        account.setIsFrozen(true);
        accountRepository.save(account);
        auditLogger.logAdminAction(securityUtil.getUserId(), "ACCOUNT_FREEZE", "ACCOUNT", id);
    }

    @PutMapping("/accounts/{id}/unfreeze")
    public void unfreezeAccount(@PathVariable Long id) {
        Account account = accountRepository.findById(id).orElseThrow();
        account.setIsFrozen(false);
        accountRepository.save(account);
        auditLogger.logAdminAction(securityUtil.getUserId(), "ACCOUNT_UNFREEZE", "ACCOUNT", id);
    }

    // ✅ ADD endpoint to get all accounts for admin
    @GetMapping("/accounts")
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    // ✅ ADD endpoint to get accounts by user
    @GetMapping("/users/{id}/accounts")
    public List<Account> getUserAccounts(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        return accountRepository.findByUserEmail(user.getEmail());
    }

    // ✅ All bank transactions (admin view)
    @GetMapping("/all-transactions")
    public List<com.bank.entity.Transaction> getAllTransactions() {
        return transactionRepository.findAll(
            org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "transactionDate"
            )
        ).stream().limit(100).toList();
    }
}