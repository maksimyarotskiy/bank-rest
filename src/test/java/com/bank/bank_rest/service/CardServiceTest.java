package com.bank.bank_rest.service;

import com.bank.bank_rest.dto.card.CardCreateRequest;
import com.bank.bank_rest.dto.card.CardResponse;
import com.bank.bank_rest.model.Card;
import com.bank.bank_rest.model.User;
import com.bank.bank_rest.model.enums.CardStatus;
import com.bank.bank_rest.model.enums.Role;
import com.bank.bank_rest.repository.CardRepository;
import com.bank.bank_rest.repository.UserRepository;
import com.bank.bank_rest.util.CardEncryptionUtil;
import com.bank.bank_rest.util.CardNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Disabled;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Disabled // Temporary
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardEncryptionUtil cardEncryptionUtil;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private Card testCard;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);
        testUser.setEnabled(true);

        testCard = new Card();
        testCard.setId(1L);
        testCard.setMaskedNumber("**** **** **** 1234");
        testCard.setOwner(testUser);
        testCard.setCardHolderName("Test User");
        testCard.setExpiryDate(LocalDate.now().plusYears(1));
        testCard.setStatus(CardStatus.ACTIVE);
        testCard.setBalance(BigDecimal.valueOf(1000.00));
    }

    @Test
    void testCreateCard_Success() {
        // Given
        CardCreateRequest request = new CardCreateRequest();
        request.setCardNumber("1234567890123456");
        request.setCardHolderName("Test User");
        request.setExpiryDate(LocalDate.now().plusYears(1));
        request.setInitialBalance(BigDecimal.valueOf(1000.00));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardEncryptionUtil.encryptCardNumber("1234567890123456")).thenReturn("encrypted123");
        when(cardEncryptionUtil.maskCardNumber("1234567890123456")).thenReturn("**** **** **** 3456");
        when(cardNumberGenerator.isValidCardNumber("1234567890123456")).thenReturn(true);
        when(cardRepository.findByEncryptedNumber(anyString())).thenReturn(Optional.empty());
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // When
        CardResponse response = cardService.createCard(request, 1L);

        // Then
        assertNotNull(response);
        assertEquals(testCard.getId(), response.getId());
        verify(userRepository).findById(1L);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void testCreateCard_UserNotFound() {
        // Given
        CardCreateRequest request = new CardCreateRequest();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> cardService.createCard(request, 1L));
    }

    @Test
    void testGetCards_UserAsCurrentUser() {
        // Given
        mockUserAuthentication();
        when(cardRepository.findByOwnerWithFilters(anyLong(), eq(null), eq(null), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(testCard)));

        // When
        Page<CardResponse> result = cardService.getCards(null, 1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(cardRepository).findByOwnerWithFilters(eq(1L), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void testGetCardById_AccessDenied() {
        // Given
        mockUserAuthentication();
        testCard.setOwner(new User());
        testCard.getOwner().setId(999L); // Different user
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(RuntimeException.class, () -> cardService.getCardById(1L, 1L));
    }

    @Test
    void testUpdateCardStatus_Success() {
        // Given
        mockAdminAuthentication();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // When
        CardResponse result = cardService.updateCardStatus(1L, CardStatus.BLOCKED, 1L);

        // Then
        assertNotNull(result);
        verify(cardRepository).save(any(Card.class));
    }

    private void mockUserAuthentication() {
        GrantedAuthority userAuthority = new SimpleGrantedAuthority("USER");
        when(authentication.getAuthorities()).thenReturn(List.of(userAuthority));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockAdminAuthentication() {
        GrantedAuthority adminAuthority = new SimpleGrantedAuthority("ADMIN");
        when(authentication.getAuthorities()).thenReturn(List.of(adminAuthority));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
