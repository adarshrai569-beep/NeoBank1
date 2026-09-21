package com.bank.service;

import com.bank.dto.LoanProductDTO;

import java.util.List;

public interface LoanProductService {

    LoanProductDTO create(LoanProductDTO dto);

    List<LoanProductDTO> getAll();

    LoanProductDTO getById(Long id);
}
