package com.bank.bank_rest.controller;

import com.bank.bank_rest.dto.card.CardCreateRequest;
import com.bank.bank_rest.dto.card.CardFilterRequest;
import com.bank.bank_rest.dto.card.CardResponse;
import com.bank.bank_rest.model.User;
import com.bank.bank_rest.model.enums.CardStatus;
import com.bank.bank_rest.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
public class CardController {
    
    private final CardService cardService;
    
    @GetMapping
    @Operation(summary = "Get cards", description = "Get paginated list of cards with filtering")
    public ResponseEntity<Page<CardResponse>> getCards(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String mockedNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = getCurrentUserId();
        CardFilterRequest filter = new CardFilterRequest();
        
        if (status != null) {
            try {
                filter.setStatus(CardStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Invalid status - ignore filter
            }
        }
        filter.setMaskedNumber(mockedNumber);
        filter.setPage(page);
        filter.setSize(size);
        
        Page<CardResponse> cards = cardService.getCards(filter, userId);
        return ResponseEntity.ok(cards);
    }
    
    @GetMapping("/{cardId}")
    @Operation(summary = "Get card details", description = "Get details of a specific card")
    public ResponseEntity<CardResponse> getCard(@PathVariable Long cardId) {
        Long userId = getCurrentUserId();
        CardResponse card = cardService.getCardById(cardId, userId);
        return ResponseEntity.ok(card);
    }
    
    @PostMapping
    @Operation(summary = "Create card", description = "Create a new card")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardCreateRequest request) {
        Long userId = getCurrentUserId();
        CardResponse card = cardService.createCard(request, userId);
        return ResponseEntity.ok(card);
    }
    
    @PutMapping("/{cardId}/status")
    @Operation(summary = "Update card status", description = "Update the status of a card")
    public ResponseEntity<CardResponse> updateCardStatus(
            @PathVariable Long cardId,
            @RequestParam String status) {
        Long userId = getCurrentUserId();
        
        CardStatus cardStatus;
        try {
            cardStatus = CardStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        
        CardResponse card = cardService.updateCardStatus(cardId, cardStatus, userId);
        return ResponseEntity.ok(card);
    }
    
    @DeleteMapping("/{cardId}")
    @Operation(summary = "Delete card", description = "Delete a card (Admin only)")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        Long userId = getCurrentUserId();
        cardService.deleteCard(cardId, userId);
        return ResponseEntity.noContent().build();
    }
    
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return ((User) auth.getPrincipal()).getId();
        }
        throw new RuntimeException("Unable to get current user");
    }
}
