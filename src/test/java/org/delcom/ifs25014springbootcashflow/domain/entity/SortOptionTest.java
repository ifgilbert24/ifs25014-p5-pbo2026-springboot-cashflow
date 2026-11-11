package org.delcom.ifs25014springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SortOptionTest {

    @Test
    void sortsByAmountAscendingAndDescending() {
        List<Transaction> list = new ArrayList<>();
        list.add(new Transaction(1, 1, "Besar", 500_000L, TransactionType.INCOME));
        list.add(new Transaction(2, 1, "Kecil", 10_000L, TransactionType.EXPENSE));

        list.sort(SortOption.AMOUNT_ASC.comparator());
        assertEquals(10_000L, list.get(0).getAmount());

        list.sort(SortOption.AMOUNT_DESC.comparator());
        assertEquals(500_000L, list.get(0).getAmount());
    }

    @Test
    void sortsByTransactionType() {
        List<Transaction> list = new ArrayList<>();
        list.add(new Transaction(1, 1, "Keluar", 10_000L, TransactionType.EXPENSE));
        list.add(new Transaction(2, 1, "Masuk", 50_000L, TransactionType.INCOME));

        list.sort(SortOption.INCOME_FIRST.comparator());
        assertTrue(list.get(0).isIncome());
        assertFalse(list.get(1).isIncome());

        list.sort(SortOption.EXPENSE_FIRST.comparator());
        assertFalse(list.get(0).isIncome());
        assertTrue(list.get(1).isIncome());
    }
}

