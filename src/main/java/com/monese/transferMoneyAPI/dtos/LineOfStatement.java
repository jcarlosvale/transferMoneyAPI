package com.monese.transferMoneyAPI.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.monese.transferMoneyAPI.entity.Account;
import com.monese.transferMoneyAPI.entity.TypeOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineOfStatement {

    private BigDecimal laterBalance;
    private TypeOperation operation;
    private BigDecimal amount;
    private BigDecimal previousBalance;
    private Long otherAccountId;
    private String dateTime;

}
