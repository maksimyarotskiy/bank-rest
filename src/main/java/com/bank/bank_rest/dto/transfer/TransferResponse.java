package com.bank.bank_rest.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private Long id;
    private Long fromCardId;
    private String fromCardNumber;
    private Long toCardId;
    private String toCardNumber;
    private BigDecimal amount;
    private String description;
    private LocalDateTime transferDate;
    private boolean successful;
}
