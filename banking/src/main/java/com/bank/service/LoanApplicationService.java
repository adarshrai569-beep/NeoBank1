package com.bank.service;

import com.bank.dto.*;

import java.util.List;

public interface LoanApplicationService {

    LoanApplicationResponseDTO apply(LoanApplicationRequestDTO request);

    List<LoanApplicationResponseDTO> getMyApplications();

    List<LoanApplicationResponseDTO> getAllApplications();

    List<LoanApplicationResponseDTO> getAllApplications(String status);

    LoanApplicationResponseDTO decide(Long id, LoanDecisionDTO decision);
    LoanEMIResponseDTO calculateEMI(Long applicationId);
}
