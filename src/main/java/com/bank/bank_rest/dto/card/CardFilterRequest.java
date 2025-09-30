package com.bank.bank_rest.dto.card;

import com.bank.bank_rest.model.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardFilterRequest {
    private CardStatus status;
    private String maskedNumber;
    private int page = 0;
    private int size = 10;
}
