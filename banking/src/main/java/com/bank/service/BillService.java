package com.bank.service;

import java.util.List;
import com.bank.entity.Bill;

public interface BillService {

    Bill create(Bill bill);

    List<Bill> getAll();

    Bill updateStatus(Long id, String status);

    Bill payBill(Long id, String method); // ✅ added
}