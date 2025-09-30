package com.bank.bank_rest.dto.card;

import com.bank.bank_rest.model.enums.CardStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardCreateRequest {
    
    @NotBlank(message = "Card number is required")
    private String cardNumber;
    
    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;
    
    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;
    
    private CardStatus status = CardStatus.ACTIVE;
    
    @PositiveOrZero(message = "Initial balance must be positive or zero")
    private BigDecimal initialBalance = BigDecimal.ZERO;
}
