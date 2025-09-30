package com.bank.bank_rest.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CardNumberGenerator {
    
    private static final Random RANDOM = new Random();
    
    public String generateCardNumber() {
        // Simple card number generator (for demo purposes)
        // In real system, would need more sophisticated algorithm
        StringBuilder sb = new StringBuilder();
        
        // Generate 16-digit card number
        for (int i = 0; i < 16; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        
        return sb.toString();
    }
    
    public boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            return false;
        }
        
        // Luhn algorithm validation
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10) == 0;
    }
}
