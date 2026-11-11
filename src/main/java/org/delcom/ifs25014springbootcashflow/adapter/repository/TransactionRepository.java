package org.delcom.ifs25014springbootcashflow.adapter.repository;

import org.delcom.ifs25014springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25014springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25014springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.stream.Collectors;

public class TransactionRepository extends InMemoryRepository<Transaction, Integer> implements ITransactionRepository {

    private int nextId = 1;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type) {
        Transaction transaction = new Transaction(nextId++, userId, description, amount, type);
        return super.save(transaction);
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return storage.stream()
                .filter(t -> t.getUserId() == userId)
                .collect(Collectors.toList());
    }
}

