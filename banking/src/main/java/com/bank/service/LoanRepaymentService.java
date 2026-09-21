package com.bank.service;

import com.bank.dto.RepaymentScheduleDTO;

import java.util.List;

public interface LoanRepaymentService {

    List<RepaymentScheduleDTO> getSchedule(Long loanAccountId, String status);

    RepaymentScheduleDTO markPaid(Long loanAccountId, Long repaymentId);
}
