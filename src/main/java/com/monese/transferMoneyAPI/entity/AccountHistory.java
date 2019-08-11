package com.monese.transferMoneyAPI.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class AccountHistory {

    @GeneratedValue
    @Id
    private long id;

    @ManyToOne
    @JoinColumn(nullable=false, updatable=false)
    private Account mainAccount;

    @ManyToOne
    @JoinColumn(nullable=false, updatable=false)
    private Account otherAccount;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TypeOperation typeOperation;

    @Column(nullable = false, updatable = false)
    private BigDecimal valueOfOperation;

    @Column(nullable = false, updatable = false)
    private BigDecimal balanceBeforeOperation;

    @Column(nullable = false, updatable = false)
    private BigDecimal balanceAfterOperation;

    @Column(nullable = false)
    private LocalDateTime dateTimeOperation;

    public AccountHistory(Account mainAccount,
                          Account otherAccount,
                          TypeOperation typeOperation,
                          BigDecimal valueOfOperation,
                          BigDecimal balanceBeforeOperation,
                          BigDecimal balanceAfterOperation,
                          LocalDateTime dateTimeOperation) {
        this.mainAccount = mainAccount;
        this.otherAccount = otherAccount;
        this.typeOperation = typeOperation;
        this.valueOfOperation = valueOfOperation;
        this.balanceBeforeOperation = balanceBeforeOperation;
        this.balanceAfterOperation = balanceAfterOperation;
        this.dateTimeOperation = dateTimeOperation;
    }
}
