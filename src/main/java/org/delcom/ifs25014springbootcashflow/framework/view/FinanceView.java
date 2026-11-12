package org.delcom.ifs25014springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import org.delcom.ifs25014springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25014springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25014springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25014springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25014springbootcashflow.domain.entity.User;
import org.delcom.ifs25014springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25014springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25014springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25014springbootcashflow.usecase.FinanceUseCase;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Catatan Keuangan | Cashflow")
public class FinanceView extends VerticalLayout {

    private final FinanceUseCase useCase;
    private final FinancePresenter presenter;
    private final FileStorage fileStorage;

    private final TextField searchField = new TextField();
    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final Paragraph statusMessage = new Paragraph();

    private final Span totalValue = new Span("0");
    private final Span incomeValue = new Span("Rp 0.00");
    private final Span expenseValue = new Span("Rp 0.00");
    private final Span balanceValue = new Span("Rp 0.00");

    public FinanceView(FinanceUseCase useCase, FinancePresenter presenter, FileStorage fileStorage) {
        this.useCase = useCase;
        this.presenter = presenter;
        this.fileStorage = fileStorage;

        getElement().setAttribute("role", "main");
        getElement().setAttribute("aria-label", "Catatan Keuangan Saya");
        setWidthFull();
        setPadding(true);
        setSpacing(false);

        Div card = PageCard.create();

        H1 header = new H1("Catatan Keuangan Saya");
        header.getStyle()
                .set("color", "#1E3A8A")
                .set("margin", "0")
                .set("font-size", "1.35rem");

        card.add(
                header,
                buildSummaryCards(),
                buildToolbar(),
                statusMessage,
                buildGrid()
        );
        add(card);

        refreshAll();
    }

    private HorizontalLayout buildSummaryCards() {
        Div totalCard = createSummaryCard("Total Transaksi", totalValue, "#1E40AF", "#EFF6FF", "#BFDBFE");
        Div incomeCard = createSummaryCard("Total Pemasukan", incomeValue, "#15803D", "#F0FDF4", "#BBF7D0");
        Div expenseCard = createSummaryCard("Total Pengeluaran", expenseValue, "#B91C1C", "#FEF2F2", "#FECACA");
        Div balanceCard = createSummaryCard("Saldo", balanceValue, "#0369A1", "#F0F9FF", "#BAE6FD");

        HorizontalLayout row = new HorizontalLayout(totalCard, incomeCard, expenseCard, balanceCard);
        row.setWidthFull();
        row.setSpacing(true);
        row.expand(totalCard, incomeCard, expenseCard, balanceCard);
        return row;
    }

    private Div createSummaryCard(String label, Span valueSpan, String textColor, String bgColor, String borderColor) {
        Span titleSpan = new Span(label);
        titleSpan.getStyle()
                .set("font-size", "0.85rem")
                .set("font-weight", "600")
                .set("color", textColor)
                .set("display", "block");

        valueSpan.getStyle()
                .set("font-size", "1.45rem")
                .set("font-weight", "700")
                .set("color", textColor)
                .set("display", "block")
                .set("margin-top", "0.25rem");

        Div card = new Div(titleSpan, valueSpan);
        card.getStyle()
                .set("background", bgColor)
                .set("border", "1px solid " + borderColor)
                .set("border-radius", "10px")
                .set("padding", "0.85rem 1.15rem")
                .set("box-sizing", "border-box");
        return card;
    }

    private HorizontalLayout buildToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setAriaLabel("Cari keterangan transaksi");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("280px");

        Button searchBtn = new Button("Cari");
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBtn.setDisableOnClick(true);
        searchBtn.addClickListener(e -> {
            try {
                handleSearch();
            } finally {
                searchBtn.setEnabled(true);
            }
        });

        Button resetBtn = new Button("Muat Ulang");
        resetBtn.setDisableOnClick(true);
        resetBtn.addClickListener(e -> {
            try {
                refreshAll();
            } finally {
                resetBtn.setEnabled(true);
            }
        });

        Button addBtn = new Button("Tambah", e -> openAddDialog(TransactionType.INCOME));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        HorizontalLayout left = new HorizontalLayout(searchField, searchBtn, resetBtn);
        left.setAlignItems(FlexComponent.Alignment.CENTER);
        left.setSpacing(true);

        HorizontalLayout bar = new HorizontalLayout(left, addBtn);
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return bar;
    }

    private Grid<Transaction> buildGrid() {
        grid.setWidthFull();
        grid.setAllRowsVisible(true);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        GridStyles.apply(grid);

        grid.addComponentColumn(transaction -> {
            if (transaction.getProofPath() == null || transaction.getProofPath().isBlank()) {
                return new Span("-");
            }
            String imageUrl = "/uploads/" + transaction.getProofPath();
            Image img = new Image(imageUrl, transaction.getDescription());
            img.setWidth("48px");
            img.setHeight("36px");
            img.setTitle("Klik untuk memperbesar");
            img.getStyle()
                    .set("object-fit", "cover")
                    .set("border-radius", "4px")
                    .set("cursor", "pointer");
            img.addClickListener(e -> ImagePreviewDialog.open(imageUrl, transaction.getDescription()));
            return img;
        }).setHeader("Bukti").setKey("bukti").setWidth("80px").setFlexGrow(0);

        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setWidth("80px")
                .setFlexGrow(0)
                .setSortable(true);

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setSortable(true)
                .setComparator((a, b) -> a.getDescription().compareToIgnoreCase(b.getDescription()));

        grid.addColumn(t -> presenter.formatCurrency(t.getAmount()))
                .setHeader("Jumlah")
                .setKey("amount")
                .setWidth("180px")
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(java.util.Comparator.comparingLong(Transaction::getAmount));

        grid.addComponentColumn(transaction -> {
            Span badge = new Span(transaction.getType().getLabel());
            boolean income = transaction.isIncome();
            badge.getStyle()
                    .set("padding", "0.2rem 0.65rem")
                    .set("border-radius", "999px")
                    .set("font-size", "0.8rem")
                    .set("font-weight", "600")
                    .set("background", income ? "#DCFCE7" : "#FEE2E2")
                    .set("color", income ? "#15803D" : "#B91C1C");
            return badge;
        })
                .setHeader("Tipe")
                .setKey("type")
                .setWidth("140px")
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator((a, b) -> Boolean.compare(b.isIncome(), a.isIncome()));

        grid.addColumn(t -> presenter.formatDateTime(t.getCreatedAt()))
                .setHeader("Dibuat")
                .setKey("createdAt")
                .setWidth("180px")
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(java.util.Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(transaction -> {
            Button deleteBtn = new Button("Hapus", e -> openDeleteDialog(transaction));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

            HorizontalLayout actions = new HorizontalLayout(deleteBtn);
            actions.setSpacing(true);
            return actions;
        }).setHeader("Tindakan").setKey("tindakan").setAutoWidth(true).setFlexGrow(0);

        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));

        statusMessage.getStyle()
                .set("color", "#475569")
                .set("font-weight", "600")
                .set("margin", "0");

        return grid;
    }

    private void openAddDialog(TransactionType defaultType) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Tambah Transaksi");
        dialog.setWidth("480px");

        ProgressBar progress = createDialogProgressBar();

        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();
        descriptionField.setPlaceholder("Contoh: Gaji bulanan");

        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setWidthFull();
        amountField.setPlaceholder("Contoh: 500000");

        Select<TransactionType> typeSelect = new Select<>();
        typeSelect.setLabel("Tipe Transaksi");
        typeSelect.setItems(TransactionType.values());
        typeSelect.setItemLabelGenerator(TransactionType::getLabel);
        typeSelect.setValue(defaultType);
        typeSelect.setWidthFull();

        AtomicReference<String> uploadedProof = new AtomicReference<>();
        Upload upload = createProofUpload(uploadedProof);

        VerticalLayout form = new VerticalLayout(
                progress,
                descriptionField,
                amountField,
                typeSelect,
                new Span("Bukti Transaksi (opsional)"),
                upload
        );
        form.setPadding(false);
        form.setSpacing(false);
        form.getStyle().set("gap", "0.75rem");
        dialog.add(form);

        Button cancelBtn = new Button("Batal", e -> dialog.close());
        Button saveBtn = new Button("Simpan");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.setDisableOnClick(true);
        saveBtn.addClickListener(e -> {
            progress.setVisible(true);
            cancelBtn.setEnabled(false);
            try {
                int userId = currentUserId();
                long parsedAmount = parseAmount(amountField.getValue());
                Transaction created = useCase.addTransaction(
                        userId,
                        descriptionField.getValue(),
                        parsedAmount,
                        typeSelect.getValue()
                );
                if (uploadedProof.get() != null) {
                    useCase.updateTransactionForUser(
                            userId,
                            created.getId(),
                            null,
                            null,
                            null,
                            uploadedProof.get(),
                            false
                    );
                }
                showSuccess("Transaksi berhasil ditambahkan.");
                dialog.close();
                refreshAll();
            } catch (ValidationException | EntityNotFoundException ex) {
                progress.setVisible(false);
                cancelBtn.setEnabled(true);
                saveBtn.setEnabled(true);
                showError(ex.getMessage());
            }
        });

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void openEditDialog(Transaction transaction) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Ubah Transaksi #" + transaction.getId());
        dialog.setWidth("480px");

        ProgressBar progress = createDialogProgressBar();

        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();
        descriptionField.setValue(transaction.getDescription());

        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setWidthFull();
        amountField.setValue(String.valueOf(transaction.getAmount()));

        Select<TransactionType> typeSelect = new Select<>();
        typeSelect.setLabel("Tipe Transaksi");
        typeSelect.setItems(TransactionType.values());
        typeSelect.setItemLabelGenerator(TransactionType::getLabel);
        typeSelect.setValue(transaction.getType());
        typeSelect.setWidthFull();

        AtomicReference<String> uploadedProof = new AtomicReference<>();
        Upload upload = createProofUpload(uploadedProof);

        Checkbox clearProofCheckbox = new Checkbox("Hapus gambar bukti");
        clearProofCheckbox.setVisible(transaction.getProofPath() != null && !transaction.getProofPath().isBlank());

        VerticalLayout form = new VerticalLayout(
                progress,
                descriptionField,
                amountField,
                typeSelect,
                new Span("Ganti Bukti Transaksi (opsional)"),
                upload,
                clearProofCheckbox
        );
        form.setPadding(false);
        form.setSpacing(false);
        form.getStyle().set("gap", "0.75rem");
        dialog.add(form);

        Button cancelBtn = new Button("Batal", e -> dialog.close());
        Button saveBtn = new Button("Simpan");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.setDisableOnClick(true);
        saveBtn.addClickListener(e -> {
            progress.setVisible(true);
            cancelBtn.setEnabled(false);
            try {
                long parsedAmount = parseAmount(amountField.getValue());
                useCase.updateTransactionForUser(
                        currentUserId(),
                        transaction.getId(),
                        descriptionField.getValue(),
                        parsedAmount,
                        typeSelect.getValue(),
                        uploadedProof.get(),
                        clearProofCheckbox.getValue()
                );
                showSuccess("Transaksi berhasil diperbarui.");
                dialog.close();
                refreshAll();
            } catch (EntityNotFoundException | ValidationException ex) {
                progress.setVisible(false);
                cancelBtn.setEnabled(true);
                saveBtn.setEnabled(true);
                showError(ex.getMessage());
            }
        });

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void openDeleteDialog(Transaction transaction) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Hapus Transaksi #" + transaction.getId());
        dialog.setWidth("420px");
        dialog.getElement().executeJs(
                "const h = this.$.overlay && this.$.overlay.shadowRoot && this.$.overlay.shadowRoot.querySelector('[part=\"header\"]');"
                        + "if (h) { h.style.background = '#DC2626'; h.style.borderBottomColor = '#B91C1C'; }"
        );

        ProgressBar progress = createDialogProgressBar();

        Paragraph confirmation = new Paragraph(
                "Apakah Anda yakin ingin menghapus transaksi \"" + transaction.getDescription() + "\"?"
        );
        confirmation.getStyle()
                .set("margin", "0")
                .set("color", "#1F2937");

        VerticalLayout body = new VerticalLayout(progress, confirmation);
        body.setPadding(false);
        body.setSpacing(false);
        body.getStyle().set("gap", "0.75rem");
        dialog.add(body);

        Button cancelBtn = new Button("Batal", e -> dialog.close());
        Button confirmBtn = new Button("Hapus");
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmBtn.setDisableOnClick(true);
        confirmBtn.addClickListener(e -> {
            progress.setVisible(true);
            cancelBtn.setEnabled(false);
            try {
                useCase.removeTransactionForUser(currentUserId(), transaction.getId());
                showSuccess("Transaksi berhasil dihapus.");
                refreshAll();
            } catch (EntityNotFoundException | ValidationException ex) {
                showError(ex.getMessage());
            } finally {
                dialog.close();
            }
        });

        dialog.getFooter().add(cancelBtn, confirmBtn);
        dialog.open();
    }

    private long parseAmount(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Jumlah transaksi tidak boleh kosong.");
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Jumlah transaksi harus berupa angka bulat.");
        }
    }

    private ProgressBar createDialogProgressBar() {
        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);
        progress.setWidthFull();
        progress.getStyle().set("margin", "0");
        return progress;
    }

    private Upload createProofUpload(AtomicReference<String> target) {
        Upload upload = new Upload(UploadHandler.inMemory((metadata, bytes) -> {
            String path = fileStorage.store(
                    "proofs",
                    metadata.fileName(),
                    new ByteArrayInputStream(bytes)
            );
            target.set(path);
        }));
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/webp");
        upload.setMaxFiles(1);
        upload.setUploadButton(new Button("Pilih Gambar"));
        return upload;
    }

    private void handleSearch() {
        String keyword = searchField.getValue();
        if (keyword == null || keyword.isBlank()) {
            refreshAll();
            return;
        }
        int userId = currentUserId();
        List<Transaction> results = useCase.searchTransactions(userId, keyword.trim());
        grid.setItems(results);
        updateSummary(userId);
        statusMessage.setText(results.isEmpty()
                ? presenter.notFoundMessage()
                : "Hasil Pencarian: \"" + keyword.trim() + "\"");
    }

    private void refreshAll() {
        int userId = currentUserId();
        List<Transaction> transactions = useCase.getTransactionsByUser(userId);
        grid.setItems(transactions);
        updateSummary(userId);
        statusMessage.setText(transactions.isEmpty() ? presenter.emptyMessage() : "Daftar Transaksi");
    }

    private void updateSummary(int userId) {
        List<Transaction> all = useCase.getTransactionsByUser(userId);
        totalValue.setText(String.valueOf(all.size()));
        incomeValue.setText(presenter.formatCurrency(useCase.getTotalIncome(userId)));
        expenseValue.setText(presenter.formatCurrency(useCase.getTotalExpense(userId)));
        balanceValue.setText(presenter.formatCurrency(useCase.getBalance(userId)));
    }

    private int currentUserId() {
        return AuthSession.getUser().map(User::getId).orElse(0);
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 2500, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3500, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}

