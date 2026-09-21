package com.bank.service.impl;
import com.bank.service.RewardService;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bank.entity.Bill;
import com.bank.repository.BillRepository;
import com.bank.service.BillService;
import com.bank.util.SecurityUtil;

@Service
public class BillServiceImpl implements BillService {

    @Autowired
    private BillRepository repo;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private RewardService rewardService;

    @Override
    public Bill create(Bill bill) {

        Long userId = securityUtil.getUserId();

        if (bill.getDueDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Due date cannot be in past");
        }

        bill.setUserId(userId);
        bill.setStatus(Bill.Status.PENDING);

        return repo.save(bill);
    }

    @Override
    public List<Bill> getAll() {
        Long userId = securityUtil.getUserId();

        System.out.println("✅ USER ID: " + userId);

        createSampleBillsIfEmpty(userId);

        List<Bill> bills = repo.findByUserId(userId);

        System.out.println("✅ Bills Count: " + bills.size());

        return bills;
    }
    
    @Override
    public Bill payBill(Long id, String method) {

        Bill bill = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        if (bill.getStatus() == Bill.Status.PAID) {
            throw new RuntimeException("Already paid");
        }

        Bill.PaymentMethod paymentMethod;

        try {
            paymentMethod = Bill.PaymentMethod.valueOf(method.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid payment method");
        }

        bill.setPaymentMethod(paymentMethod);
        bill.setStatus(Bill.Status.PAID);

        rewardService.addPoints(bill.getUserId(), 10);

        return repo.save(bill);
    }

    @Override
    public Bill updateStatus(Long id, String status) {

        Bill bill = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        Bill.Status enumStatus;

        try {
            enumStatus = Bill.Status.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid status");
        }

        if (enumStatus != Bill.Status.PAID && enumStatus != Bill.Status.OVERDUE) {
            throw new RuntimeException("Invalid status");
        }

        bill.setStatus(enumStatus);

        if (enumStatus == Bill.Status.PAID) {
            rewardService.addPoints(bill.getUserId(), 10);
        }

        return repo.save(bill);
    }
    public void createSampleBillsIfEmpty(Long userId) {

        List<Bill> existing = repo.findByUserId(userId);

        if (!existing.isEmpty()) {
            return; // already has bills
        }

        List<Bill> bills = List.of(
            Bill.builder()
                .userId(userId)
                .billerName("Airtel Mobile")
                .amount(499.0)
                .dueDate(LocalDate.now().plusDays(5))
                .status(Bill.Status.PENDING)
                .build(),

            Bill.builder()
                .userId(userId)
                .billerName("Dish TV Recharge")
                .amount(350.0)
                .dueDate(LocalDate.now().plusDays(3))
                .status(Bill.Status.PENDING)
                .build(),

            Bill.builder()
                .userId(userId)
                .billerName("Bharat Gas")
                .amount(950.0)
                .dueDate(LocalDate.now().plusDays(7))
                .status(Bill.Status.PENDING)
                .build(),

            Bill.builder()
                .userId(userId)
                .billerName("Electricity Bill")
                .amount(1200.0)
                .dueDate(LocalDate.now().plusDays(2))
                .status(Bill.Status.PENDING)
                .build()
        );

        repo.saveAll(bills);
    }
    
}
