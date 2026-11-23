package com.saimanoj.finance.repository;

import com.saimanoj.finance.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByMonthAndYear(int month, int year);
}
