package org.delcom.ifs25014springbootcashflow.adapter.presenter;

import org.delcom.ifs25014springbootcashflow.domain.entity.Transaction;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String EMPTY_MESSAGE = "Data catatan keuangan belum tersedia";
    private static final String NOT_FOUND_MESSAGE = "Catatan keuangan tidak ditemukan";

    public String renderTransactionList(List<Transaction> transactions, String title) {
        if (transactions.isEmpty()) {
            return EMPTY_MESSAGE;
        }
        return buildRows(transactions, title);
    }

    public String renderSearchResults(List<Transaction> transactions, String keyword) {
        if (transactions.isEmpty()) {
            return NOT_FOUND_MESSAGE;
        }
        return buildRows(transactions, "Hasil Pencarian: \"" + keyword + "\"");
    }

    public String renderTransactionRow(Transaction transaction) {
        return "[" + transaction.getId() + "] "
                + transaction.getDescription()
                + " | "
                + formatCurrency(transaction.getAmount())
                + " | ["
                + transaction.getType().getLabel()
                + "]";
    }

    public String renderBalance(long balance) {
        return "Saldo: " + formatCurrency(balance);
    }

    public String formatCurrency(long amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,##0.00", symbols);
        if (amount < 0) {
            return "-Rp " + formatter.format(Math.abs(amount));
        }
        return "Rp " + formatter.format(amount);
    }

    public String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(DATE_TIME_FORMAT);
    }

    public String emptyMessage() {
        return EMPTY_MESSAGE;
    }

    public String notFoundMessage() {
        return NOT_FOUND_MESSAGE;
    }

    private String buildRows(List<Transaction> transactions, String title) {
        StringBuilder builder = new StringBuilder();
        builder.append("=== ").append(title).append(" ===\n");
        for (int i = 0; i < transactions.size(); i++) {
            builder.append(renderTransactionRow(transactions.get(i)));
            if (i < transactions.size() - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }
}

