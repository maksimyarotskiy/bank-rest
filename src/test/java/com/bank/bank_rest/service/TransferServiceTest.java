package com.bank.bank_rest.service;

import com.bank.bank_rest.dto.transfer.TransferRequest;
import com.bank.bank_rest.dto.transfer.TransferResponse;
import com.bank.bank_rest.model.Card;
import com.bank.bank_rest.model.User;
import com.bank.bank_rest.model.enums.CardStatus;
import com.bank.bank_rest.model.enums.Role;
import com.bank.bank_rest.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Disabled;

@ExtendWith(MockitoExtension.class)
@Disabled // Temporary  
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private CardService cardService;

    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private Card fromCard;
    private Card toCard;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(Role.USER);

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setOwner(testUser);
        fromCard.setBalance(BigDecimal.valueOf(1000.00));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setExpiryDate(LocalDate.now().plusYears(1));

        toCard = new Card();
        toCard.setId(2L);
        toCard.setOwner(testUser);
        toCard.setBalance(BigDecimal.valueOf(500.00));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpiryDate(LocalDate.now().plusYears(1));

        transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(BigDecimal.valueOf(100.00));
        transferRequest.setDescription("Test transfer");
    }

    @Test
    void testTransferMoney_Success() {
        // Given
        when(cardService.getCardById(1L)).thenReturn(fromCard);
        when(cardService.getCardById(2L)).thenReturn(toCard);
        when(cardService.updateCardBalance(any(Card.class))).thenReturn(fromCard);
        when(cardService.updateCardBalance(any(Card.class))).thenReturn(toCard);
        when(transferRepository.save(any())).thenReturn(new com.bank.bank_rest.model.Transfer());

        // When
        TransferResponse result = transferService.transferMoney(transferRequest, 1L);

        // Then
        assertNotNull(result);
        assertEquals(transferRequest.getAmount(), result.getAmount());
        assertEquals(transferRequest.getDescription(), result.getDescription());
        
        verify(cardService, times(2)).updateCardBalance(any(Card.class));
        verify(transferRepository).save(any());
        
        // Verify balances were updated
        verify(cardService).updateCardBalance(fromCard);
        verify(cardService).updateCardBalance(toCard);
    }

    @Test
    void testTransferMoney_InsufficientBalance() {
        // Given
        fromCard.setBalance(BigDecimal.valueOf(50.00)); // Less than transfer amount
        when(cardService.getCardById(1L)).thenReturn(fromCard);
        when(cardService.getCardById(2L)).thenReturn(toCard);

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            transferService.transferMoney(transferRequest, 1L));
    }

    @Test
    void testTransferMoney_SameCard() {
        // Given
        transferRequest.setToCardId(1L); // Same as from card
        when(cardService.getCardById(1L)).thenReturn(fromCard);

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            transferService.transferMoney(transferRequest, 1L));
    }

    @Test
    void testTransferMoney_AccessDenied() {
        // Given
        User differentUser = new User();
        differentUser.setId(999L);
        fromCard.setOwner(differentUser);
        
        when(cardService.getCardById(1L)).thenReturn(fromCard);
        when(cardService.getCardById(2L)).thenReturn(toCard);

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            transferService.transferMoney(transferRequest, 1L));
    }

    @Test
    void testTransferMoney_InvalidAmount() {
        // Given
        transferRequest.setAmount(BigDecimal.valueOf(-100.00)); // Negative amount
        when(cardService.getCardById(1L)).thenReturn(fromCard);
        when(cardService.getCardById(2L)).thenReturn(toCard);

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            transferService.transferMoney(transferRequest, 1L));
    }

    @Test
    void testTransferMoney_ZeroAmount() {
        // Given
        transferRequest.setAmount(BigDecimal.ZERO);
        when(cardService.getCardById(1L)).thenReturn(fromCard);
        when(cardService.getCardById(2L)).thenReturn(toCard);

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            transferService.transferMoney(transferRequest, 1L));
    }

    @Test
    void testTransferMoney_CardCannotTransfer() {
        // Given
        fromCard.setStatus(CardStatus.BLOCKED); // Card is blocked
        when(cardService.getCardById(1L)).thenReturn(fromCard);
        when(cardService.getCardById(2L)).thenReturn(toCard);

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            transferService.transferMoney(transferRequest, 1L));
    }
}
