package com.monese.transferMoneyAPI.service;

import com.monese.transferMoneyAPI.entity.Account;
import org.springframework.util.Assert;

class Validate {

    public static void validateArguments(Account originAccount, Account destinyAccount, double value) {
        Assert.notNull(originAccount, "The given ORIGIN ACCOUNT doesn't exist!");
        Assert.notNull(destinyAccount, "The given DESTINY ACCOUNT doesn't exist!");
        if (originAccount.equals(destinyAccount)) throw new IllegalArgumentException("The accounts must be different");
        if (value < 0) throw new IllegalArgumentException("The given VALUE can not be less than 0");
    }
}
