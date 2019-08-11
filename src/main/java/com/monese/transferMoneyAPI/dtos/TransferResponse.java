package com.monese.transferMoneyAPI.dtos;

import lombok.Data;

@Data
public class TransferResponse {
    private final String msg;

    public TransferResponse(String msg) {
        this.msg = msg;
    }
}
