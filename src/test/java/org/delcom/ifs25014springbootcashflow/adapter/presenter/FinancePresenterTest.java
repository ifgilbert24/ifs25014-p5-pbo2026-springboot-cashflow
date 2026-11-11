package org.delcom.ifs25014springbootcashflow.adapter.presenter;

import org.delcom.ifs25014springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25014springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancePresenterTest {

    private FinancePresenter presenter;

    @BeforeEach
    void setUp() {
        presenter = new FinancePresenter();
    }

    @Test
    void renderEmptyTransactionList() {
        assertEquals(
                "Data catatan keuangan belum tersedia",
                presenter.renderTransactionList(Collections.emptyList(), "Daftar Transaksi")
        );
        assertEquals("Data catatan keuangan belum tersedia", presenter.emptyMessage());
    }

    @Test
    void renderEmptySearchResults() {
        assertEquals(
                "Catatan keuangan tidak ditemukan",
                presenter.renderSearchResults(Collections.emptyList(), "xyz")
        );
        assertEquals("Catatan keuangan tidak ditemukan", presenter.notFoundMessage());
    }

    @Test
    void renderTransactionsAndSearchResults() {
        Transaction t1 = new Transaction(1, 1, "Gaji", 5_000_000L, TransactionType.INCOME);
        Transaction t2 = new Transaction(2, 1, "Makan Siang", 25_000L, TransactionType.EXPENSE);

        String listOutput = presenter.renderTransactionList(List.of(t1, t2), "Daftar Transaksi");
        assertTrue(listOutput.contains("=== Daftar Transaksi ==="));
        assertTrue(listOutput.contains("[1] Gaji | Rp 5,000,000.00 | [Pemasukan]"));
        assertTrue(listOutput.contains("[2] Makan Siang | Rp 25,000.00 | [Pengeluaran]"));

        String searchOutput = presenter.renderSearchResults(List.of(t1), "Gaji");
        assertTrue(searchOutput.contains("=== Hasil Pencarian: \"Gaji\" ==="));
    }

    @Test
    void formatCurrencyBalanceAndDateTime() {
        assertEquals("Rp 1,000,000.00", presenter.formatCurrency(1_000_000L));
        assertEquals("Rp 0.00", presenter.formatCurrency(0L));
        assertEquals("-Rp 50,000.00", presenter.formatCurrency(-50_000L));
        assertEquals("Saldo: Rp 4,975,000.00", presenter.renderBalance(4_975_000L));

        LocalDateTime dt = LocalDateTime.of(2026, 11, 1, 9, 5, 7);
        assertEquals("2026-11-01 09:05:07", presenter.formatDateTime(dt));
        assertEquals("-", presenter.formatDateTime(null));
    }
}

