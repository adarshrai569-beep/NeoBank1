package com.bank.service.impl;

import com.bank.dto.LoanProductDTO;
import com.bank.entity.LoanProduct;
import com.bank.repository.LoanProductRepository;
import com.bank.service.LoanProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LoanProductServiceImpl implements LoanProductService {

    private final LoanProductRepository repository;

    @Override
    public LoanProductDTO create(LoanProductDTO dto) {

        // ✅ ADMIN ROLE CHECK (manual check like your style)
        String role = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .toString();

        if (!role.contains("ADMIN")) {
            throw new ResponseStatusException(FORBIDDEN, "Only admin can create loan products");
        }

        if (dto.getMinAmount().compareTo(dto.getMaxAmount()) >= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Min amount must be less than max amount");
        }

        if (repository.findByProductName(dto.getProductName()).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "Product already exists");
        }

        LoanProduct product = LoanProduct.builder()
                .productName(dto.getProductName())
                .minAmount(dto.getMinAmount())
                .maxAmount(dto.getMaxAmount())
                .annualInterestRate(dto.getAnnualInterestRate())
                .allowedTenures(dto.getAllowedTenures())
                .build();

        LoanProduct saved = repository.save(product);

        return mapToDTO(saved);
    }

    @CacheEvict(value = "loanProducts", allEntries = true)
    @Override
    @Cacheable(value = "loanProducts", key = "'all'")
    public List<LoanProductDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LoanProductDTO getById(Long id) {
        LoanProduct product = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
        return mapToDTO(product);
    }

    private LoanProductDTO mapToDTO(LoanProduct p) {
        return LoanProductDTO.builder()
                .id(p.getId())
                .productName(p.getProductName())
                .minAmount(p.getMinAmount())
                .maxAmount(p.getMaxAmount())
                .annualInterestRate(p.getAnnualInterestRate())
                .allowedTenures(p.getAllowedTenures())
                .build();
    }
}