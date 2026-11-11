package org.delcom.ifs25014springbootcashflow.usecase;

import org.delcom.ifs25014springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25014springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25014springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25014springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25014springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25014springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private static final int USER_A = 1;
    private static final int USER_B = 2;

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void addIncomeExpenseAndTotals() throws Exception {
        Transaction inc = useCase.addIncome(USER_A, "Gaji", 5_000_000L);
        Transaction exp = useCase.addExpense(USER_A, "Makan", 25_000L);

        assertEquals(1, inc.getId());
        assertTrue(inc.isIncome());
        assertFalse(exp.isIncome());

        assertEquals(2, useCase.getTransactionsByUser(USER_A).size());
        assertTrue(useCase.getTransactionsByUser(USER_B).isEmpty());
        assertEquals(2, useCase.getAllTransactions().size());

        assertEquals(5_000_000L, useCase.getTotalIncome(USER_A));
        assertEquals(25_000L, useCase.getTotalExpense(USER_A));
        assertEquals(4_975_000L, useCase.getBalance(USER_A));
    }

    @Test
    void addTransactionValidations() {
        assertThrows(ValidationException.class, () -> useCase.addTransaction(USER_A, null, 100L, TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> useCase.addTransaction(USER_A, "  ", 100L, TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> useCase.addTransaction(USER_A, "Tes", 0L, TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> useCase.addTransaction(USER_A, "Tes", -5L, TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> useCase.addTransaction(USER_A, "Tes", 100L, null));
    }

    @Test
    void removeTransactionSuccessNotFoundAndOwnerCheck() throws Exception {
        Transaction t = useCase.addIncome(USER_A, "Hapus", 10_000L);
        assertThrows(ValidationException.class, () -> useCase.removeTransactionForUser(USER_B, t.getId()));
        useCase.removeTransactionForUser(USER_A, t.getId());
        assertTrue(useCase.getTransactionsByUser(USER_A).isEmpty());

        Transaction t2 = useCase.addIncome(USER_A, "Hapus 2", 10_000L);
        useCase.removeTransaction(t2.getId());
        assertTrue(useCase.getTransactionsByUser(USER_A).isEmpty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(99));
        assertEquals(99, ex.getId());
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransactionForUser(USER_A, 99));
    }

    @Test
    void updateTransactionPartialAndValidation() throws Exception {
        Transaction t = useCase.addIncome(USER_A, "Lama", 100_000L);

        useCase.updateTransactionForUser(USER_A, t.getId(), "Baru", 250_000L, TransactionType.EXPENSE, "proofs/a.png", false);
        Transaction updated = useCase.getTransactionsByUser(USER_A).get(0);
        assertEquals("Baru", updated.getDescription());
        assertEquals(250_000L, updated.getAmount());
        assertEquals(TransactionType.EXPENSE, updated.getType());
        assertEquals("proofs/a.png", updated.getProofPath());

        useCase.updateTransactionForUser(USER_A, t.getId(), null, null, null, null, true);
        assertNull(useCase.getTransactionsByUser(USER_A).get(0).getProofPath());

        useCase.updateTransaction(t.getId(), "Hanya Deskripsi", null, null, null, false);
        assertEquals("Hanya Deskripsi", useCase.getTransactionsByUser(USER_A).get(0).getDescription());

        useCase.updateTransaction(t.getId(), null, null, null, null, false);
        assertEquals("Hanya Deskripsi", useCase.getTransactionsByUser(USER_A).get(0).getDescription());

        assertThrows(ValidationException.class, () -> useCase.updateTransaction(t.getId(), "  ", null, null, null, false));
        assertThrows(ValidationException.class, () -> useCase.updateTransaction(t.getId(), null, 0L, null, null, false));
        assertThrows(ValidationException.class, () -> useCase.updateTransaction(t.getId(), null, -10L, null, null, false));
        assertThrows(EntityNotFoundException.class, () -> useCase.updateTransaction(999, "X", null, null, null, false));
        assertThrows(ValidationException.class, () ->
                useCase.updateTransactionForUser(USER_B, t.getId(), "X", null, null, null, false));
        assertThrows(EntityNotFoundException.class, () ->
                useCase.updateTransactionForUser(USER_A, 999, "X", null, null, null, false));
    }

    @Test
    void searchAndSortTransactions() throws Exception {
        useCase.addIncome(USER_A, "Gaji Bulanan", 5_000_000L);
        useCase.addExpense(USER_A, "Makan Siang", 25_000L);

        List<Transaction> search = useCase.searchTransactions(USER_A, "gaji");
        assertEquals(1, search.size());
        assertEquals("Gaji Bulanan", search.get(0).getDescription());

        List<Transaction> amountAsc = useCase.sortTransactions(USER_A, SortOption.AMOUNT_ASC);
        assertEquals("Makan Siang", amountAsc.get(0).getDescription());

        List<Transaction> expenseFirst = useCase.sortTransactions(USER_A, SortOption.EXPENSE_FIRST);
        assertFalse(expenseFirst.get(0).isIncome());
    }
}

