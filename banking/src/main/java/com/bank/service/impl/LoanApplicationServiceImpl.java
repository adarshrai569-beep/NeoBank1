package com.bank.service.impl;

import com.bank.dto.*;
import com.bank.entity.*;
import com.bank.repository.*;
import com.bank.service.LoanAccountService;
import com.bank.service.LoanApplicationService;
import com.bank.util.EmiCalculatorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanProductRepository productRepo;
    private final LoanApplicationRepository applicationRepo;
    private final UserRepository userRepository;
        private final LoanAccountService loanAccountService;

    // ✅ APPLY LOAN
    @Override
    public LoanApplicationResponseDTO apply(LoanApplicationRequestDTO request) {

        String email = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        LoanProduct product = productRepo.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));

        if (request.getAmount().compareTo(product.getMinAmount()) < 0
                || request.getAmount().compareTo(product.getMaxAmount()) > 0) {
                        throw new ResponseStatusException(BAD_REQUEST, "Invalid loan amount");
        }

        if (!product.getAllowedTenures().contains(request.getTenure())) {
                        throw new ResponseStatusException(BAD_REQUEST, "Invalid tenure");
        }

        boolean exists = applicationRepo
                .existsByUserIdAndLoanProduct_IdAndStatus(
                        user.getId(),
                        product.getId(),
                        LoanStatus.PENDING
                );

        if (exists) {
                        throw new ResponseStatusException(CONFLICT, "Pending application already exists");
        }

        LoanApplication app = LoanApplication.builder()
                .userId(user.getId())
                .loanProduct(product)
                .amount(request.getAmount())
                .tenure(request.getTenure())
                .status(LoanStatus.PENDING)
                .build();

        return mapToDTO(applicationRepo.save(app));
    }

    // ✅ GET MY APPLICATIONS
    @Override
    public List<LoanApplicationResponseDTO> getMyApplications() {

        String email = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        return applicationRepo.findByUserId(user.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
        public List<LoanApplicationResponseDTO> getAllApplications(String status) {
        checkAdmin();
                List<LoanApplication> applications;
                if (status == null || status.isBlank()) {
                        applications = applicationRepo.findAll();
                } else {
                        try {
                                LoanStatus parsed = LoanStatus.valueOf(status.toUpperCase());
                                applications = applicationRepo.findByStatus(parsed);
                        } catch (IllegalArgumentException ex) {
                                throw new ResponseStatusException(BAD_REQUEST, "Invalid status filter");
                        }
                }

                return applications.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
        @Transactional
        public LoanApplicationResponseDTO decide(Long id, LoanDecisionDTO decision) {
        checkAdmin();

        LoanApplication app = applicationRepo.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Application not found"));

        if (app.getStatus() != LoanStatus.PENDING) {
                        throw new ResponseStatusException(CONFLICT, "Application already decided");
        }

                if (decision == null || decision.getDecision() == null) {
                        throw new ResponseStatusException(BAD_REQUEST, "Decision is required");
                }

                String normalized = decision.getDecision().trim().toUpperCase();
                if (!normalized.equals("APPROVED") && !normalized.equals("REJECTED")) {
                        throw new ResponseStatusException(BAD_REQUEST, "Invalid decision");
                }

                if (normalized.equals("APPROVED")) {
                        app.setStatus(LoanStatus.APPROVED);
                } else {
                        app.setStatus(LoanStatus.REJECTED);
                }

                app.setAdminRemarks(decision.getRemarks());
                app.setDecidedAt(java.time.LocalDateTime.now());

                LoanApplication saved = applicationRepo.save(app);

                if (saved.getStatus() == LoanStatus.APPROVED) {
                        loanAccountService.createAccountAndSchedule(saved);
                }

                return mapToDTO(saved);
    }

    // ✅ ✅ EMI CALCULATION
    @Override
    public LoanEMIResponseDTO calculateEMI(Long applicationId) {

        LoanApplication app = applicationRepo.findById(applicationId)
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Application not found"));

        if (app.getStatus() != LoanStatus.APPROVED) {
                        throw new ResponseStatusException(BAD_REQUEST, "Loan must be approved to calculate EMI");
        }

        double principal = app.getAmount().doubleValue();
        double annualRate = app.getLoanProduct().getAnnualInterestRate();
        int tenure = app.getTenure();

        BigDecimal emiValue = EmiCalculatorUtil.calculateEmi(app.getAmount(), annualRate, tenure);
        double totalAmount = emiValue.doubleValue() * tenure;
        double totalInterest = totalAmount - principal;

        return LoanEMIResponseDTO.builder()
                .principal(app.getAmount())
                .annualInterestRate(annualRate)
                .tenureMonths(tenure)
                .monthlyEMI(emiValue)
                .totalAmount(BigDecimal.valueOf(totalAmount))
                .totalInterest(BigDecimal.valueOf(totalInterest))
                .build();
    }

    // ✅ ADMIN CHECK
    private void checkAdmin() {

        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
                        throw new ResponseStatusException(FORBIDDEN, "Only admin allowed");
        }
    }

    // ✅ DTO MAPPING
    private LoanApplicationResponseDTO mapToDTO(LoanApplication app) {

        return LoanApplicationResponseDTO.builder()
                .applicationId(app.getId())
                .userId(app.getUserId())
                .productName(app.getLoanProduct().getProductName())
                .amount(app.getAmount())
                .tenure(app.getTenure())
                .status(app.getStatus().name())
                .adminRemarks(app.getAdminRemarks())
                .appliedAt(app.getAppliedAt())
                .decidedAt(app.getDecidedAt())
                .build();
    }

	@Override
	public List<LoanApplicationResponseDTO> getAllApplications() {
		// TODO Auto-generated method stub
		return null;
	}

}
