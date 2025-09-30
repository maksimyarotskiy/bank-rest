package com.bank.bank_rest.controller;

import com.bank.bank_rest.dto.transfer.TransferRequest;
import com.bank.bank_rest.dto.transfer.TransferResponse;
import com.bank.bank_rest.model.User;
import com.bank.bank_rest.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
public class TransferController {
    
    private final TransferService transferService;
    
    @PostMapping
    @Operation(summary = "Make transfer", description = "Transfer money between own cards")
    public ResponseEntity<?> makeTransfer(@Valid @RequestBody TransferRequest request) {
        try {
            Long userId = getCurrentUserId();
            TransferResponse transfer = transferService.transferMoney(request, userId);
            return ResponseEntity.ok(transfer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping
    @Operation(summary = "Get transfers", description = "Get user's transfer history")
    public ResponseEntity<Page<TransferResponse>> getTransfers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long cardId) {
        Long userId = getCurrentUserId();
        Page<TransferResponse> transfers = transferService.getTransfers(userId, page, size, cardId);
        return ResponseEntity.ok(transfers);
    }
    
    @GetMapping("/{transferId}")
    @Operation(summary = "Get transfer details", description = "Get details of a specific transfer")
    public ResponseEntity<TransferResponse> getTransfer(@PathVariable Long transferId) {
        Long userId = getCurrentUserId();
        TransferResponse transfer = transferService.getTransferById(transferId, userId);
        return ResponseEntity.ok(transfer);
    }
    
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return ((User) auth.getPrincipal()).getId();
        }
        throw new RuntimeException("Unable to get current user");
    }
}