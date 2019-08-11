package com.monese.transferMoneyAPI.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.monese.transferMoneyAPI.entity.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatementResponse {

    private Account account;
    private List<LineOfStatement> operations;

}
