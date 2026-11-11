package org.delcom.ifs25014springbootcashflow.usecase;

import org.delcom.ifs25014springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25014springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25014springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25014springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25014springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25014springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.stream.Collectors;

public class FinanceUseCase {

    private final ITransactionRepository repository;

    public FinanceUseCase(ITransactionRepository repository) {
        this.repository = repository;
    }

    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

    public List<Transaction> getTransactionsByUser(int userId) {
        return repository.findByUserId(userId);
    }

    public Transaction addIncome(int userId, String description, long amount) throws ValidationException {
        return addTransaction(userId, description, amount, TransactionType.INCOME);
    }

    public Transaction addExpense(int userId, String description, long amount) throws ValidationException {
        return addTransaction(userId, description, amount, TransactionType.EXPENSE);
    }

    public Transaction addTransaction(
            int userId,
            String description,
            long amount,
            TransactionType type
    ) throws ValidationException {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Keterangan transaksi tidak boleh kosong.");
        }
        if (amount <= 0) {
            throw new ValidationException("Jumlah transaksi harus lebih dari 0.");
        }
        if (type == null) {
            throw new ValidationException("Tipe transaksi wajib dipilih.");
        }
        return repository.save(userId, description.trim(), amount, type);
    }

    public void removeTransaction(int id) throws EntityNotFoundException {
        boolean deleted = repository.deleteById(id);
        if (!deleted) {
            throw new EntityNotFoundException(id);
        }
    }

    public void removeTransactionForUser(int userId, int id)
            throws EntityNotFoundException, ValidationException {
        Transaction transaction = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
        if (transaction.getUserId() != userId) {
            throw new ValidationException("Transaksi bukan milik pengguna ini.");
        }
        repository.deleteById(id);
    }

    public void updateTransaction(
            int id,
            String newDescription,
            Long newAmount,
            TransactionType newType,
            String newProofPath,
            boolean clearProof
    ) throws EntityNotFoundException, ValidationException {
        Transaction transaction = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
        applyUpdates(transaction, newDescription, newAmount, newType, newProofPath, clearProof);
        repository.save(transaction);
    }

    public void updateTransactionForUser(
            int userId,
            int id,
            String newDescription,
            Long newAmount,
            TransactionType newType,
            String newProofPath,
            boolean clearProof
    ) throws EntityNotFoundException, ValidationException {
        Transaction transaction = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
        if (transaction.getUserId() != userId) {
            throw new ValidationException("Transaksi bukan milik pengguna ini.");
        }
        applyUpdates(transaction, newDescription, newAmount, newType, newProofPath, clearProof);
        repository.save(transaction);
    }

    private void applyUpdates(
            Transaction transaction,
            String newDescription,
            Long newAmount,
            TransactionType newType,
            String newProofPath,
            boolean clearProof
    ) throws ValidationException {
        if (newDescription != null) {
            if (newDescription.isBlank()) {
                throw new ValidationException("Keterangan transaksi tidak boleh kosong.");
            }
            transaction.changeDescription(newDescription.trim());
        }
        if (newAmount != null) {
            if (newAmount <= 0) {
                throw new ValidationException("Jumlah transaksi harus lebih dari 0.");
            }
            transaction.changeAmount(newAmount);
        }
        if (newType != null) {
            transaction.changeType(newType);
        }
        if (clearProof) {
            transaction.changeProofPath(null);
        } else if (newProofPath != null) {
            transaction.changeProofPath(newProofPath);
        }
    }

    public List<Transaction> searchTransactions(int userId, String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return repository.findByUserId(userId).stream()
                .filter(t -> t.getDescription().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    public List<Transaction> sortTransactions(int userId, SortOption option) {
        return repository.findByUserId(userId).stream()
                .sorted(option.comparator())
                .collect(Collectors.toList());
    }

    public long getTotalIncome(int userId) {
        return repository.findByUserId(userId).stream()
                .filter(Transaction::isIncome)
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    public long getTotalExpense(int userId) {
        return repository.findByUserId(userId).stream()
                .filter(t -> !t.isIncome())
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    public long getBalance(int userId) {
        return getTotalIncome(userId) - getTotalExpense(userId);
    }
}

