package com.g4t1.account.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accounts")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Account {

    @Id
    @Column(name = "account_id", updatable = false, nullable = false)
    private String id;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(name = "account_status", nullable = false)
    private String accountStatus;

    @Column(name = "opening_date", nullable = false)
    private LocalDate openingDate;

    @Column(name = "initial_deposit", nullable = false)
    private BigDecimal initialDeposit;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "branch_id", nullable = false)
    private String branchId;
}
