package org.delcom.ifs25014springbootcashflow.domain.repository;

import org.delcom.ifs25014springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25014springbootcashflow.domain.entity.TransactionType;

import java.util.List;

public interface ITransactionRepository extends IRepository<Transaction, Integer> {

    Transaction save(int userId, String description, long amount, TransactionType type);

    List<Transaction> findByUserId(int userId);
}

