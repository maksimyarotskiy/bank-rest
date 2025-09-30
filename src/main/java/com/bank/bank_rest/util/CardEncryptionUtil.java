package com.bank.bank_rest.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class CardEncryptionUtil {
    
    private final SecretKeySpec secretKey;
    
    public CardEncryptionUtil(@Value("${jwt.secret}") String secret) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(secret.getBytes());
        this.secretKey = new SecretKeySpec(key, "AES");
    }
    
    public String encryptCardNumber(String cardNumber) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(cardNumber.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting card number", e);
        }
    }
    
    public String decryptCardNumber(String encryptedCardNumber) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedCardNumber);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting card number", e);
        }
    }
    
    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        
        // Remove spaces and show only last 4 digits
        String cleanNumber = cardNumber.replaceAll("\\s+", "");
        if (cleanNumber.length() < 4) {
            return "****";
        }
        
        String lastFour = cleanNumber.substring(cleanNumber.length() - 4);
        return String.format("**** **** **** %s", lastFour);
    }
}
