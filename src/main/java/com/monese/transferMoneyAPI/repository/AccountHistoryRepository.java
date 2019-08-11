package com.monese.transferMoneyAPI.repository;

import com.monese.transferMoneyAPI.entity.AccountHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountHistoryRepository extends JpaRepository<AccountHistory, Long> {
    List<AccountHistory> findAccountHistoriesByMainAccount_IdOrderByDateTimeOperationDesc(long acocuntId);
}
