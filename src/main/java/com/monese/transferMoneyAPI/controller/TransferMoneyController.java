package com.monese.transferMoneyAPI.controller;

import com.monese.transferMoneyAPI.service.TransferMoneyService;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Log
public class TransferMoneyController {

    private final TransferMoneyService service;

    @PostMapping(value = "/transfer/from/{originAccountId}/to/{destinyAccountId}/value/{value}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> transfer (@PathVariable("originAccountId") long originAccount,
                                                      @PathVariable("destinyAccountId") long destinyAccountId,
                                                      @PathVariable("value") double value) {
        try {
            return new ResponseEntity<>(service.transfer(originAccount, destinyAccountId, value), HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            log.info(ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(),HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.severe(e.getMessage());
            return new ResponseEntity<>("Unexpected error, contact the support and provide the used request.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getBankStatement/{accountId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getBankStatement (@PathVariable int accountId) {
        try {
            return new ResponseEntity<>(service.getBankStatement(accountId), HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            log.info(ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(),HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.severe(e.getMessage());
            return new ResponseEntity<>("Unexpected error, contact the support and provide the used request.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
