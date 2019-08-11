package com.monese.transferMoneyAPI;

import com.monese.transferMoneyAPI.dtos.LineOfStatement;
import com.monese.transferMoneyAPI.dtos.StatementResponse;
import com.monese.transferMoneyAPI.entity.Account;
import com.monese.transferMoneyAPI.repository.AccountRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static com.monese.transferMoneyAPI.entity.TypeOperation.CREDIT;
import static com.monese.transferMoneyAPI.entity.TypeOperation.DEBIT;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransferMoneyApiApplicationTests {

    @LocalServerPort
    private int randomServerPort = 0;

    @Autowired
    AccountRepository accountRepository;

    private final String transferUrl = "/transfer/from/{originAccountId}/to/{destinyAccountId}/value/{value}";
    private final String statementUrl = "/getBankStatement/{accountId}";

    @Test
    @Transactional
    public void testTransferOK() {
        String url = "http://localhost:"+randomServerPort+transferUrl;
        List<Account> accountList = accountRepository.findAll();
        Account expectedOriginAccount = accountList.get(0);
        expectedOriginAccount.setBalance(expectedOriginAccount.getBalance().subtract(BigDecimal.TEN));
        Account expectedDestinyAccount = accountList.get(1);
        expectedDestinyAccount.setBalance(expectedDestinyAccount.getBalance().add(BigDecimal.TEN));

        ResponseEntity<Object> actualResponse =
                new RestTemplate().postForEntity(url, null, Object.class,
                        expectedOriginAccount.getId(), expectedDestinyAccount.getId(), BigDecimal.TEN.doubleValue());

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());

        Account actualOriginAccount = accountRepository.getOne(expectedOriginAccount.getId());
        Account actualDestinyAccount = accountRepository.getOne(expectedDestinyAccount.getId());

        assertEquals(expectedOriginAccount, actualOriginAccount);
        assertEquals(expectedDestinyAccount, actualDestinyAccount);
    }

    @Test(expected = HttpClientErrorException.BadRequest.class)
    @Transactional
    public void testTransferWithHighValue() {
        String url = "http://localhost:"+randomServerPort+transferUrl;
        BigDecimal amount = BigDecimal.valueOf(Double.MAX_VALUE);
        List<Account> accountList = accountRepository.findAll();
        Account expectedOriginAccount = accountList.get(0);
        Account expectedDestinyAccount = accountList.get(1);

       try {
           new RestTemplate().postForEntity(url, null, Object.class, expectedOriginAccount.getId(),
                   expectedDestinyAccount.getId(), amount.doubleValue());

        } finally {
           Account actualOriginAccount = accountRepository.getOne(expectedOriginAccount.getId());
           Account actualDestinyAccount = accountRepository.getOne(expectedDestinyAccount.getId());

           assertEquals(expectedOriginAccount, actualOriginAccount);
           assertEquals(expectedDestinyAccount, actualDestinyAccount);
        }
    }

    @Test(expected = HttpClientErrorException.BadRequest.class)
    @Transactional
    public void testTransferWithInvalidAccount() {
        String url = "http://localhost:"+randomServerPort+transferUrl;
        BigDecimal amount = BigDecimal.valueOf(Double.MAX_VALUE);
        List<Account> accountList = accountRepository.findAll();
        Account expectedOriginAccount = accountList.get(0);

        try {
            new RestTemplate().postForEntity(url, null, Object.class, expectedOriginAccount.getId(),
                    Integer.MAX_VALUE, amount.doubleValue());
        } finally {
            Account actualOriginAccount = accountRepository.getOne(expectedOriginAccount.getId());
            assertEquals(expectedOriginAccount, actualOriginAccount);
        }
    }

    @Test(expected = HttpClientErrorException.BadRequest.class)
    @Transactional
    public void testTransferWithInvalidAmmount() {
        String url = "http://localhost:"+randomServerPort+transferUrl;
        BigDecimal amount = BigDecimal.valueOf(-0.01);
        List<Account> accountList = accountRepository.findAll();
        Account expectedOriginAccount = accountList.get(0);
        Account expectedDestinyAccount = accountList.get(1);
        try {
            new RestTemplate().postForEntity(url, null, Object.class, expectedOriginAccount.getId(),
                    Integer.MAX_VALUE, amount.doubleValue());
        } finally {
            Account actualOriginAccount = accountRepository.getOne(expectedOriginAccount.getId());
            Account actualDestinyAccount = accountRepository.getOne(expectedDestinyAccount.getId());

            assertEquals(expectedOriginAccount, actualOriginAccount);
            assertEquals(expectedDestinyAccount, actualDestinyAccount);
        }
    }

    @Test
    @Transactional
    public void testStatementOK() throws IOException {
        String urlStatement = "http://localhost:"+randomServerPort+statementUrl;
        String urlTransfer = "http://localhost:"+randomServerPort+transferUrl;

        List<Account> accountList = accountRepository.findAll();
        Account expectedOriginAccount = accountList.get(0);
        expectedOriginAccount.setBalance(expectedOriginAccount.getBalance().subtract(BigDecimal.valueOf(30)));

        Account expectedDestinyAccount = accountList.get(1);
        expectedDestinyAccount.setBalance(expectedDestinyAccount.getBalance().add(BigDecimal.valueOf(30)));

        //execute 3 transfers
        for (int i = 0; i<3; i++) {
            new RestTemplate().postForEntity(urlTransfer, null, Object.class,
                    expectedOriginAccount.getId(), expectedDestinyAccount.getId(), BigDecimal.TEN.doubleValue());
        }

        ResponseEntity<StatementResponse> statementOrigin =
                new RestTemplate().getForEntity(urlStatement, StatementResponse.class, expectedOriginAccount.getId());

        ResponseEntity<StatementResponse> statementDestiny =
                new RestTemplate().getForEntity(urlStatement, StatementResponse.class, expectedDestinyAccount.getId());

        StatementResponse originStatement = statementOrigin.getBody();
        StatementResponse destinyStatement = statementDestiny.getBody();

        assertEquals(expectedOriginAccount, originStatement.getAccount());
        assertEquals(3, originStatement.getOperations().size());

        for (LineOfStatement operation : originStatement.getOperations()) {
            assertEquals(DEBIT, operation.getOperation());
            assertEquals(BigDecimal.TEN.negate().doubleValue(), operation.getAmount().doubleValue(),0);
        }

        assertEquals(expectedDestinyAccount, destinyStatement.getAccount());
        assertEquals(3, destinyStatement.getOperations().size());

        for (LineOfStatement operation : destinyStatement.getOperations()) {
            assertEquals(CREDIT, operation.getOperation());
            assertEquals(BigDecimal.TEN.doubleValue(), operation.getAmount().doubleValue(),0);
        }
    }

    @Test(expected = HttpClientErrorException.BadRequest.class)
    @Transactional
    public void testStatementWithInvalidAccount() {
        String url = "http://localhost:"+randomServerPort+statementUrl;
        new RestTemplate().getForEntity(url, StatementResponse.class, Long.MAX_VALUE);
    }

}
