package org.delcom.ifs25014springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    @Test
    void constructorsAndGetters() {
        Transaction t1 = new Transaction(1, 10, "Gaji", 5_000_000L, TransactionType.INCOME);
        assertEquals(1, t1.getId());
        assertEquals(10, t1.getUserId());
        assertEquals("Gaji", t1.getDescription());
        assertEquals(5_000_000L, t1.getAmount());
        assertEquals(TransactionType.INCOME, t1.getType());
        assertEquals("Pemasukan", t1.getType().getLabel());
        assertTrue(t1.isIncome());
        assertNull(t1.getProofPath());
        assertNotNull(t1.getCreatedAt());

        Transaction t2 = new Transaction(2, 10, "Makan", 25_000L, TransactionType.EXPENSE, "proofs/a.png");
        assertEquals("proofs/a.png", t2.getProofPath());
        assertEquals("Pengeluaran", t2.getType().getLabel());
        assertFalse(t2.isIncome());

        LocalDateTime created = LocalDateTime.of(2026, 11, 1, 8, 0);
        Transaction nullDesc = new Transaction(3, 10, null, 10_000L, TransactionType.INCOME, null, created);
        assertEquals("", nullDesc.getDescription());
        assertEquals(created, nullDesc.getCreatedAt());
    }

    @Test
    void mutators() {
        Transaction t = new Transaction(1, 10, "Lama", 100L, TransactionType.INCOME);

        t.changeDescription("Baru");
        t.changeAmount(250L);
        t.changeType(TransactionType.EXPENSE);
        t.changeProofPath("proofs/x.png");

        assertEquals("Baru", t.getDescription());
        assertEquals(250L, t.getAmount());
        assertEquals(TransactionType.EXPENSE, t.getType());
        assertEquals("proofs/x.png", t.getProofPath());

        t.changeDescription(null);
        assertEquals("", t.getDescription());
    }
}

