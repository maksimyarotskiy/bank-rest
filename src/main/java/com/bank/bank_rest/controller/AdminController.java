package com.bank.bank_rest.controller;

import com.bank.bank_rest.dto.card.CardCreateRequest;
import com.bank.bank_rest.dto.card.CardFilterRequest;
import com.bank.bank_rest.dto.card.CardResponse;
import com.bank.bank_rest.dto.user.UserRegistrationRequest;
import com.bank.bank_rest.dto.user.UserResponse;
import com.bank.bank_rest.model.Card;
import com.bank.bank_rest.model.User;
import com.bank.bank_rest.model.enums.CardStatus;
import com.bank.bank_rest.model.enums.Role;
import com.bank.bank_rest.repository.CardRepository;
import com.bank.bank_rest.repository.UserRepository;
import com.bank.bank_rest.service.AuthService;
import com.bank.bank_rest.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer-key")
public class AdminController {
    
    private final AuthService authService;
    private final CardService cardService;
    private final UserRepository userRepository;
    
    @PostMapping("/users")
    @Operation(summary = "Create user", description = "Create a new user (Admin only)")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse user = authService.registerUser(request);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Get list of all users (Admin only)")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
    
    @PostMapping("/cards")
    @Operation(summary = "Create card for any user", description = "Create card for any user (Admin only)")
    public ResponseEntity<CardResponse> createCardForUser(
            @Valid @RequestBody CardCreateRequest request,
            @RequestParam Long userId) {
        CardResponse card = cardService.createCard(request, userId);
        return ResponseEntity.ok(card);
    }
    
    @GetMapping("/cards")
    @Operation(summary = "Get all cards", description = "Get all cards (Admin only)")
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        CardFilterRequest filter = new CardFilterRequest();
        if (status != null) {
            try {
                filter.setStatus(CardStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Invalid status - ignore filter
            }
        }
        filter.setPage(page);
        filter.setSize(size);
        
        Page<CardResponse> cards = cardService.getCards(filter, null); // null means admin can see all
        return ResponseEntity.ok(cards);
    }
    
    @PutMapping("/cards/{cardId}/status")
    @Operation(summary = "Update card status", description = "Update card status (Admin only)")
    public ResponseEntity<CardResponse> updateCardStatus(
            @PathVariable Long cardId,
            @RequestParam String status) {
        
        CardStatus cardStatus;
        try {
            cardStatus = CardStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        
        CardResponse card = cardService.updateCardStatus(cardId, cardStatus, null); // null means admin
        return ResponseEntity.ok(card);
    }
    
    @DeleteMapping("/cards/{cardId}")
    @Operation(summary = "Delete card", description = "Delete a card (Admin only)")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId, null); // null means admin
        return ResponseEntity.noContent().build();
    }
}
