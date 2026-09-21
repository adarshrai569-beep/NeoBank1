package com.bank.service;

import com.bank.dto.AdminDashboardDTO;
import com.bank.dto.PendingApprovalDTO;
import com.bank.dto.SystemHealthDTO;
import com.bank.dto.UserActivityDTO;
import com.bank.dto.UserStatusUpdateDTO;
import com.bank.dto.UserSummaryDTO;

import java.util.List;

public interface AdminDashboardService {

    AdminDashboardDTO getDashboard();

    List<PendingApprovalDTO> getPendingApprovals(String module);

    SystemHealthDTO getSystemHealth();

    List<UserSummaryDTO> getUsers();

    void updateUserStatus(Long userId, UserStatusUpdateDTO request, Long actingAdminId);

    UserActivityDTO getUserActivity(Long userId);
}
