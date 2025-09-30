package com.bank.bank_rest.repository;

import com.bank.bank_rest.model.Card;
import com.bank.bank_rest.model.User;
import com.bank.bank_rest.model.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByOwnerId(Long ownerId, Pageable pageable);
    Page<Card> findByOwnerAndStatus(User owner, CardStatus status, Pageable pageable);
    
    @Query("SELECT c FROM Card c WHERE c.owner.id = :ownerId " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:maskedNumber IS NULL OR c.maskedNumber LIKE %:maskedNumber%)")
    Page<Card> findByOwnerWithFilters(@Param("ownerId") Long ownerId, 
                                      @Param("status") CardStatus status,
                                      @Param("maskedNumber") String maskedNumber, 
                                      Pageable pageable);
    
    Optional<Card> findByEncryptedNumber(String encryptedNumber);
    
    @Query("SELECT c FROM Card c WHERE c.encryptedNumber = :encryptedNumber " +
           "AND c.owner.id = :ownerId")
    Optional<Card> findByEncryptedNumberAndOwnerId(@Param("encryptedNumber") String encryptedNumber,
                                                  @Param("ownerId") Long ownerId);
    
    Page<Card> findByStatus(CardStatus status, Pageable pageable);
}
