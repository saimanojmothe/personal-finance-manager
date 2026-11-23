package com.saimanoj.finance.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "holdings")
public class Holding {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String symbol;

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Shares must be greater than zero")
    @Column(precision = 19, scale = 4)
    private BigDecimal shares;

    @NotNull
    @DecimalMin(value = "0.01", message = "Purchase price must be greater than zero")
    @Column(precision = 19, scale = 2)
    private BigDecimal purchasePrice;

    @NotNull
    @DecimalMin(value = "0.01", message = "Current price must be greater than zero")
    @Column(precision = 19, scale = 2)
    private BigDecimal currentPrice; // Fixed mock price for the demo — not a live market price.

    protected Holding() {}

    public Holding(String symbol, String name, BigDecimal shares, BigDecimal purchasePrice, BigDecimal currentPrice) {
        this.symbol = symbol;
        this.name = name;
        this.shares = shares;
        this.purchasePrice = purchasePrice;
        this.currentPrice = currentPrice;
    }

    public Long getId() { return id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getShares() { return shares; }
    public void setShares(BigDecimal shares) { this.shares = shares; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
}
