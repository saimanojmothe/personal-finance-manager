package com.saimanoj.finance.repository;

import com.saimanoj.finance.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, Long> {}
