package com.bank.service;

import com.bank.dto.LoanAccountDTO;
import com.bank.entity.LoanAccount;
import com.bank.entity.LoanApplication;

import java.util.List;

public interface LoanAccountService {

    LoanAccount createAccountAndSchedule(LoanApplication application);

    List<LoanAccountDTO> getMyAccounts();
}
