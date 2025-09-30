package com.bank.bank_rest.repository;

import com.bank.bank_rest.model.Card;
import com.bank.bank_rest.model.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    
    @Query("SELECT t FROM Transfer t WHERE " +
           "(t.fromCard.owner.id = :ownerId OR t.toCard.owner.id = :ownerId)")
    Page<Transfer> findByOwnerInvolvedCards(@Param("ownerId") Long ownerId, Pageable pageable);
    
    @Query("SELECT t FROM Transfer t WHERE " +
           "((t.fromCard.id = :cardId AND t.fromCard.owner.id = :ownerId) OR " +
           "(t.toCard.id = :cardId AND t.toCard.owner.id = :ownerId))")
    Page<Transfer> findByCardAndOwner(@Param("cardId") Long cardId, 
                                     @Param("ownerId") Long ownerId, 
                                     Pageable pageable);
    
    @Query("SELECT t FROM Transfer t WHERE " +
           "((t.fromCard = :fromCard AND t.fromCard.owner.id = :ownerId) OR " +
           "(t.toCard = :toCard AND t.toCard.owner.id = :ownerId))")
    Page<Transfer> findByCardsAndOwner(@Param("fromCard") Card fromCard, 
                                       @Param("toCard") Card toCard, 
                                       @Param("ownerId") Long ownerId, 
                                       Pageable pageable);
}
