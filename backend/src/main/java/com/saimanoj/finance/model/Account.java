package com.saimanoj.finance.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String type; // CHECKING, SAVINGS, CREDIT_CARD, CASH

    @NotNull
    @Column(precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    protected Account() {}

    public Account(String name, String type, BigDecimal balance) {
        this.name = name;
        this.type = type;
        this.balance = balance;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
