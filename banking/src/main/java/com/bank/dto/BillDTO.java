package com.bank.dto;

import java.time.LocalDate;
import com.bank.entity.Bill.Status;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillDTO {

    private Long id;
    private String billerName;
    private Double amount;
    private LocalDate dueDate;
    private String paymentMethod;
}
