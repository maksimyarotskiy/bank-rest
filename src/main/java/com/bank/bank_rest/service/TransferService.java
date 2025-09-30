package com.bank.bank_rest.service;

import com.bank.bank_rest.dto.transfer.TransferRequest;
import com.bank.bank_rest.dto.transfer.TransferResponse;
import com.bank.bank_rest.model.Card;
import com.bank.bank_rest.model.Transfer;
import com.bank.bank_rest.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {
    
    private final TransferRepository transferRepository;
    private final CardService cardService;
    
    @Transactional
    public TransferResponse transferMoney(TransferRequest request, Long ownerId) {
        // Validate from and to cards exist and belong to user
        Card fromCard = cardService.getCardById(request.getFromCardId());
        Card toCard = cardService.getCardById(request.getToCardId());
        
        // Security check - only allow transfers between user's own cards
        if (!fromCard.getOwner().getId().equals(ownerId) || !toCard.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Access denied - can only transfer between own cards");
        }
        
        // Basic validation
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer amount must be greater than 0");
        }
        
        if (fromCard.getId().equals(toCard.getId())) {
            throw new RuntimeException("Cannot transfer money to the same card");
        }
        
        // Check if both cards are active
        if (!fromCard.canTransfer()) {
            throw new RuntimeException("Source card is not available for transfers");
        }
        
        if (!toCard.isActive()) {
            throw new RuntimeException("Destination card is not active");
        }
        
        // Check sufficient balance
        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        
        Transfer transfer = new Transfer();
        transfer.setFromCard(fromCard);
        transfer.setToCard(toCard);
        transfer.setAmount(request.getAmount());
        transfer.setDescription(request.getDescription());
        
        try {
            // Perform the transfer
            fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
            toCard.setBalance(toCard.getBalance().add(request.getAmount()));
            
            // Save updated cards
            cardService.updateCardBalance(fromCard);
            cardService.updateCardBalance(toCard);
            
            // Save transfer record
            Transfer savedTransfer = transferRepository.save(transfer);
            
            log.info("Transfer completed: {} from card {} to card {}", 
                    request.getAmount(), fromCard.getId(), toCard.getId());
            
            return mapToTransferResponse(savedTransfer);
            
        } catch (Exception e) {
            // Create failed transfer record
            transfer.setSuccessful(false);
            transferRepository.save(transfer);
            log.error("Transfer failed: {}", e.getMessage());
            throw new RuntimeException("Transfer failed: " + e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public Page<TransferResponse> getTransfers(Long ownerId, int page, int size, Long cardId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transferDate").descending());
        
        Page<Transfer> transfers;
        
        if (cardId != null) {
            Card card = cardService.getCardById(cardId);
            // Ensure user owns the card
            if (!card.getOwner().getId().equals(ownerId)) {
                throw new RuntimeException("Access denied");
            }
            transfers = transferRepository.findByCardAndOwner(cardId, ownerId, pageable);
        } else {
            transfers = transferRepository.findByOwnerInvolvedCards(ownerId, pageable);
        }
        
        return transfers.map(this::mapToTransferResponse);
    }
    
    @Transactional(readOnly = true)
    public TransferResponse getTransferById(Long transferId, Long ownerId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));
        
        // Check if user is involved in this transfer
        if (!transfer.getFromCard().getOwner().getId().equals(ownerId) && 
            !transfer.getToCard().getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Access denied");
        }
        
        return mapToTransferResponse(transfer);
    }
    
    private TransferResponse mapToTransferResponse(Transfer transfer) {
        TransferResponse response = new TransferResponse();
        response.setId(transfer.getId());
        response.setFromCardId(transfer.getFromCard().getId());
        response.setFromCardNumber(transfer.getFromCard().getMaskedNumber());
        response.setToCardId(transfer.getToCard().getId());
        response.setToCardNumber(transfer.getToCard().getMaskedNumber());
        response.setAmount(transfer.getAmount());
        response.setDescription(transfer.getDescription());
        response.setTransferDate(transfer.getTransferDate());
        response.setSuccessful(transfer.isSuccessful());
        return response;
    }
}
