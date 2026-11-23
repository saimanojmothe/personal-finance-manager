package com.saimanoj.finance.repository;

import com.saimanoj.finance.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {}
