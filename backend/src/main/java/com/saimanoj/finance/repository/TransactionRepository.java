package com.saimanoj.finance.repository;

import com.saimanoj.finance.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByDateBetweenOrderByDateDesc(LocalDate start, LocalDate end);

    List<Transaction> findByCategoryIdAndDateBetween(Long categoryId, LocalDate start, LocalDate end);

    @Query("select t from Transaction t where (:categoryId is null or t.category.id = :categoryId) " +
           "and (:start is null or t.date >= :start) and (:end is null or t.date <= :end) order by t.date desc, t.id desc")
    List<Transaction> search(@Param("categoryId") Long categoryId,
                             @Param("start") LocalDate start,
                             @Param("end") LocalDate end);
}
