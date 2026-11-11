package org.delcom.ifs25014springbootcashflow.adapter.repository;

import org.delcom.ifs25014springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25014springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRepositoryTest {

    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
    }

    @Test
    void saveAssignsSequentialIdAndScopedByUser() {
        Transaction first = repository.save(1, "Satu", 10_000L, TransactionType.INCOME);
        Transaction second = repository.save(2, "Dua", 20_000L, TransactionType.EXPENSE);

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertEquals(2, repository.findAll().size());
        assertEquals(1, repository.findByUserId(1).size());
        assertEquals(1, repository.findByUserId(2).size());
    }

    @Test
    void saveExistingReplacesAtSamePosition() {
        Transaction first = repository.save(1, "Awal", 10_000L, TransactionType.INCOME);
        repository.save(1, "Kedua", 20_000L, TransactionType.EXPENSE);

        first.changeDescription("Diperbarui");
        repository.save(first);

        assertEquals(2, repository.findAll().size());
        assertEquals("Diperbarui", repository.findById(1).orElseThrow().getDescription());
    }

    @Test
    void deleteAndFindById() {
        Transaction saved = repository.save(1, "Hapus", 5_000L, TransactionType.EXPENSE);

        assertTrue(repository.findById(saved.getId()).isPresent());
        assertTrue(repository.deleteById(saved.getId()));
        assertFalse(repository.deleteById(saved.getId()));
        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}

