package com.monese.transferMoneyAPI.service;

import com.monese.transferMoneyAPI.dtos.LineOfStatement;
import com.monese.transferMoneyAPI.dtos.StatementResponse;
import com.monese.transferMoneyAPI.dtos.TransferResponse;
import com.monese.transferMoneyAPI.entity.Account;
import com.monese.transferMoneyAPI.entity.AccountHistory;
import com.monese.transferMoneyAPI.repository.AccountHistoryRepository;
import com.monese.transferMoneyAPI.repository.AccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.monese.transferMoneyAPI.entity.TypeOperation.CREDIT;
import static com.monese.transferMoneyAPI.entity.TypeOperation.DEBIT;
import static com.monese.transferMoneyAPI.service.Validate.validateArguments;

@Service
@AllArgsConstructor
public class TransferMoneyService {

    private final AccountRepository accountRepository;
    private final AccountHistoryRepository accountHistoryRepository;

    @Transactional
    public TransferResponse transfer(long originAccountId, long destinyAccountId, double value) {
        try{
            Account originAccount = accountRepository.getOne(originAccountId);
            Account destinyAccount = accountRepository.getOne(destinyAccountId);
            validateArguments(originAccount, destinyAccount, value);
            final BigDecimal amount = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN);
            if (originAccount.getBalance().compareTo(amount) >= 0) {

                AccountHistory originAccountHistory = new AccountHistory();
                originAccountHistory.setMainAccount(originAccount);
                originAccountHistory.setOtherAccount(destinyAccount);
                originAccountHistory.setBalanceBeforeOperation(originAccount.getBalance());
                originAccountHistory.setValueOfOperation(amount.negate());
                originAccountHistory.setTypeOperation(DEBIT);

                AccountHistory destinyAccountHistory = new AccountHistory();
                destinyAccountHistory.setMainAccount(destinyAccount);
                destinyAccountHistory.setOtherAccount(originAccount);
                destinyAccountHistory.setBalanceBeforeOperation(destinyAccount.getBalance());
                destinyAccountHistory.setValueOfOperation(amount);
                destinyAccountHistory.setTypeOperation(CREDIT);

                originAccount.setBalance(originAccount.getBalance().subtract(amount));
                originAccountHistory.setBalanceAfterOperation(originAccount.getBalance());

                destinyAccount.setBalance(destinyAccount.getBalance().add(amount));
                destinyAccountHistory.setBalanceAfterOperation(destinyAccount.getBalance());

                LocalDateTime dateTimeOperation = LocalDateTime.now();
                originAccountHistory.setDateTimeOperation(dateTimeOperation);
                destinyAccountHistory.setDateTimeOperation(dateTimeOperation);

                accountRepository.save(originAccount);
                accountRepository.save(destinyAccount);

                accountHistoryRepository.save(originAccountHistory);
                accountHistoryRepository.save(destinyAccountHistory);
            } else {
                throw new IllegalArgumentException("Insufficient funds.");
            }
        } finally {
            accountRepository.flush();
            accountHistoryRepository.flush();
        }

        return new TransferResponse("Transfer completed.");
    }

    public StatementResponse getBankStatement(long accountId) {
        try {
            List<AccountHistory> statement =
                    accountHistoryRepository.findAccountHistoriesByMainAccount_IdOrderByDateTimeOperationDesc(accountId);

            if (statement.isEmpty()) {
                throw new IllegalArgumentException("Statement empty");
            }

            StatementResponse response = new StatementResponse();
            List<LineOfStatement> lines = new ArrayList<>();
            response.setAccount(accountRepository.getOne(accountId));
            for (AccountHistory accountHistory : statement) {
                lines.add(new LineOfStatement(
                        accountHistory.getBalanceAfterOperation(),
                        accountHistory.getTypeOperation(),
                        accountHistory.getValueOfOperation(),
                        accountHistory.getBalanceBeforeOperation(),
                        accountHistory.getOtherAccount().getId(),
                        accountHistory.getDateTimeOperation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                ));
            }
            response.setOperations(lines);
            return response;
        } catch (EntityNotFoundException ex) {
                throw new IllegalArgumentException("ACCOUNT not found.");
        } finally {
            accountHistoryRepository.flush();
        }
    }
}
