package com.bank.bank_rest.service;

import com.bank.bank_rest.dto.card.CardCreateRequest;
import com.bank.bank_rest.dto.card.CardFilterRequest;
import com.bank.bank_rest.dto.card.CardResponse;
import com.bank.bank_rest.model.Card;
import com.bank.bank_rest.model.User;
import com.bank.bank_rest.model.enums.CardStatus;
import com.bank.bank_rest.repository.CardRepository;
import com.bank.bank_rest.repository.UserRepository;
import com.bank.bank_rest.util.CardEncryptionUtil;
import com.bank.bank_rest.util.CardNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {
    
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardEncryptionUtil cardEncryptionUtil;
    private final CardNumberGenerator cardNumberGenerator;
    
    @Transactional
    public CardResponse createCard(CardCreateRequest request, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String cardNumber;
        
        // If card number is provided, use it; otherwise generate one
        if (request.getCardNumber() != null && !request.getCardNumber().isEmpty()) {
            if (!cardNumberGenerator.isValidCardNumber(request.getCardNumber())) {
                throw new RuntimeException("Invalid card number format");
            }
            cardNumber = request.getCardNumber();
        } else {
            cardNumber = cardNumberGenerator.generateCardNumber();
        }
        
        // Check if card number already exists
        String encryptedNumber = cardEncryptionUtil.encryptCardNumber(cardNumber);
        if (cardRepository.findByEncryptedNumber(encryptedNumber).isPresent()) {
            throw new RuntimeException("Card with this number already exists");
        }
        
        Card card = new Card();
        card.setEncryptedNumber(encryptedNumber);
        card.setMaskedNumber(cardEncryptionUtil.maskCardNumber(cardNumber));
        card.setOwner(owner);
        card.setCardHolderName(request.getCardHolderName());
        card.setExpiryDate(request.getExpiryDate());
        card.setStatus(request.getStatus());
        card.setBalance(request.getInitialBalance());
        
        Card savedCard = cardRepository.save(card);
        log.info("Card created successfully with ID: {}", savedCard.getId());
        
        return mapToCardResponse(savedCard);
    }
    
    @Transactional(readOnly = true)
    public Page<CardResponse> getCards(CardFilterRequest filter, Long ownerId) {
        boolean isAdmin = isCurrentUserAdmin();
        
        Pageable pageable = PageRequest.of(
            filter.getPage(), 
            filter.getSize(), 
            Sort.by("createdAt").descending()
        );
        
        Page<Card> cards;
        
        if (isAdmin) {
            // Admin can see all cards or filter by status
            if (filter.getStatus() != null) {
                cards = cardRepository.findByStatus(filter.getStatus(), pageable);
            } else {
                cards = cardRepository.findAll(pageable);
            }
        } else {
            // Regular user can only see their cards
            cards = cardRepository.findByOwnerWithFilters(
                ownerId, 
                filter.getStatus(), 
                filter.getMaskedNumber(), 
                pageable
            );
        }
        
        return cards.map(this::mapToCardResponse);
    }
    
    @Transactional(readOnly = true)
    public CardResponse getCardById(Long cardId, Long ownerId) {
        boolean isAdmin = isCurrentUserAdmin();
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!isAdmin && !card.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Access denied");
        }
        
        return mapToCardResponse(card);
    }
    
    @Transactional
    public CardResponse updateCardStatus(Long cardId, CardStatus status, Long ownerId) {
        boolean isAdmin = isCurrentUserAdmin();
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!isAdmin && !card.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Access denied");
        }
        
        card.setStatus(status);
        Card updatedCard = cardRepository.save(card);
        
        log.info("Card {} status updated to {}", cardId, status);
        return mapToCardResponse(updatedCard);
    }
    
    @Transactional
    public void deleteCard(Long cardId, Long ownerId) {
        boolean isAdmin = isCurrentUserAdmin();
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!isAdmin && !card.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Access denied");
        }
        
        cardRepository.delete(card);
        log.info("Card {} deleted successfully", cardId);
    }
    
    @Transactional(readOnly = true)
    public Card getCardById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
    }
    
    @Transactional
    public Card updateCardBalance(Card card) {
        return cardRepository.save(card);
    }
    
    private CardResponse mapToCardResponse(Card card) {
        CardResponse response = new CardResponse();
        response.setId(card.getId());
        response.setMaskedNumber(card.getMaskedNumber());
        response.setCardHolderName(card.getCardHolderName());
        response.setExpiryDate(card.getExpiryDate());
        response.setStatus(card.getStatus());
        response.setBalance(card.getBalance());
        response.setCreatedAt(card.getCreatedAt());
        response.setUpdatedAt(card.getUpdatedAt());
        response.setOwnerId(card.getOwner().getId());
        response.setOwnerName(card.getOwner().getFirstName() + " " + card.getOwner().getLastName());
        response.setExpired(card.isExpired());
        return response;
    }
    
    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));
    }
}
