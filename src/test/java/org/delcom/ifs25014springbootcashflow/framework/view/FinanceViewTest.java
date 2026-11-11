package org.delcom.ifs25014springbootcashflow.framework.view;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import org.delcom.ifs25014springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25014springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25014springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25014springbootcashflow.domain.entity.Role;
import org.delcom.ifs25014springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25014springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25014springbootcashflow.domain.entity.User;
import org.delcom.ifs25014springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25014springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25014springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25014springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static com.github.mvysny.kaributesting.v10.GridKt._doubleClickItem;
import static com.github.mvysny.kaributesting.v10.GridKt._getCellComponent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.github.mvysny.kaributesting.v10.LocatorJ._setValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

class FinanceViewTest {

    private static final int USER_ID = 1;

    @TempDir
    Path tempDir;

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = spy(new FinanceUseCase(new TransactionRepository()));
        MockVaadin.setup();
        AuthSession.login(new User(USER_ID, "andi", "hash", "Andi", Role.USER, null));
        UI.getCurrent().add(new FinanceView(useCase, new FinancePresenter(), new FileStorage(tempDir.toString())));
    }

    @AfterEach
    void tearDown() {
        AuthSession.logout();
        MockVaadin.tearDown();
    }

    @Test
    @SuppressWarnings("unchecked")
    void addSearchSortEditAndDeleteFlow() throws Exception {
        assertTrue(_get(Paragraph.class).getText().contains("Data catatan keuangan belum tersedia"));

        clickButton("Tambah");
        Dialog addDialog = _get(Dialog.class);
        TextField descField = _get(addDialog, TextField.class, spec -> spec.withLabel("Keterangan"));
        TextField amtField = _get(addDialog, TextField.class, spec -> spec.withLabel("Jumlah (Rp)"));
        _setValue(descField, "Gaji Bulanan");
        _setValue(amtField, "5000000");
        clickDialogButton(addDialog, "Simpan");

        assertEquals(1, useCase.getTransactionsByUser(USER_ID).size());
        assertEquals("Daftar Transaksi", _get(Paragraph.class).getText());

        clickButton("Tambah");
        Dialog addDialog2 = _get(Dialog.class);
        TextField descField2 = _get(addDialog2, TextField.class, spec -> spec.withLabel("Keterangan"));
        TextField amtField2 = _get(addDialog2, TextField.class, spec -> spec.withLabel("Jumlah (Rp)"));
        Select<TransactionType> typeSelect2 = _get(addDialog2, Select.class);
        _setValue(descField2, "Makan Siang");
        _setValue(amtField2, "25000");
        _setValue(typeSelect2, TransactionType.EXPENSE);
        clickDialogButton(addDialog2, "Simpan");

        assertSummary("2", "Rp 5,000,000.00", "Rp 25,000.00", "Rp 4,975,000.00");

        TextField searchField = _get(TextField.class, spec -> spec.withPlaceholder("Cari keterangan..."));
        _setValue(searchField, "tidakada");
        clickButton("Cari");
        assertTrue(_get(Paragraph.class).getText().contains("Catatan keuangan tidak ditemukan"));

        _setValue(searchField, "Gaji");
        clickButton("Cari");
        assertEquals("Hasil Pencarian: \"Gaji\"", _get(Paragraph.class).getText());

        Grid<Transaction> grid = _get(Grid.class);
        assertEquals(1, grid.getListDataView().getItemCount());

        clickButton("Muat Ulang");
        assertEquals(2, grid.getListDataView().getItemCount());
        assertEquals("Daftar Transaksi", _get(Paragraph.class).getText());

        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("description"), SortDirection.DESCENDING)));
        List<Transaction> sortedDesc = grid.getListDataView().getItems().toList();
        assertEquals("Makan Siang", sortedDesc.get(0).getDescription());

        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("description"), SortDirection.ASCENDING)));
        List<Transaction> sortedAsc = grid.getListDataView().getItems().toList();
        assertEquals("Gaji Bulanan", sortedAsc.get(0).getDescription());

        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("id"), SortDirection.ASCENDING)));
        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("amount"), SortDirection.ASCENDING)));
        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("type"), SortDirection.ASCENDING)));
        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("createdAt"), SortDirection.ASCENDING)));

        int gajiRow = indexOfDescription(grid, "Gaji Bulanan");
        _doubleClickItem(grid, gajiRow);
        Dialog editDialog = _get(Dialog.class);
        TextField editDesc = _get(editDialog, TextField.class, spec -> spec.withLabel("Keterangan"));
        _setValue(editDesc, "Gaji Bulanan Updated");
        clickDialogButton(editDialog, "Simpan");

        assertTrue(useCase.getTransactionsByUser(USER_ID).stream()
                .anyMatch(t -> t.getDescription().contains("Updated")));

        int updatedRow = indexOfDescription(grid, "Gaji Bulanan Updated");
        HorizontalLayout actions = (HorizontalLayout) _getCellComponent(grid, updatedRow, "tindakan");
        _click(_get(actions, Button.class, spec -> spec.withCaption("Hapus")));
        Dialog deleteDialog = _get(Dialog.class);
        clickDialogButton(deleteDialog, "Hapus");
        assertEquals(1, useCase.getTransactionsByUser(USER_ID).size());
        assertSummary("1", "Rp 0.00", "Rp 25,000.00", "-Rp 25,000.00");
    }

    @Test
    @SuppressWarnings("unchecked")
    void handlesValidationCancelBlankSearchAndDeleteCancel() throws Exception {
        clickButton("Tambah");
        Dialog addDialog = _get(Dialog.class);
        TextField descField = _get(addDialog, TextField.class, spec -> spec.withLabel("Keterangan"));
        TextField amtField = _get(addDialog, TextField.class, spec -> spec.withLabel("Jumlah (Rp)"));
        _setValue(descField, " ");
        _setValue(amtField, "bukanangka");
        clickDialogButton(addDialog, "Simpan");
        assertTrue(useCase.getTransactionsByUser(USER_ID).isEmpty());
        clickDialogButton(addDialog, "Batal");

        TextField searchField = _get(TextField.class, spec -> spec.withPlaceholder("Cari keterangan..."));
        _setValue(searchField, "   ");
        clickButton("Cari");
        assertTrue(_get(Paragraph.class).getText().contains("Data catatan keuangan belum tersedia"));

        useCase.addIncome(USER_ID, "Ada", 10_000L);
        clickButton("Muat Ulang");

        Grid<Transaction> grid = _get(Grid.class);
        _doubleClickItem(grid, 0);

        Dialog editDialog = _get(Dialog.class);
        TextField editDesc = _get(editDialog, TextField.class, spec -> spec.withLabel("Keterangan"));
        _setValue(editDesc, " ");
        clickDialogButton(editDialog, "Simpan");
        clickDialogButton(editDialog, "Batal");

        HorizontalLayout actions = (HorizontalLayout) _getCellComponent(grid, 0, "tindakan");
        _click(_get(actions, Button.class, spec -> spec.withCaption("Hapus")));
        Dialog deleteDialog = _get(Dialog.class);
        clickDialogButton(deleteDialog, "Batal");
        assertEquals(1, useCase.getTransactionsByUser(USER_ID).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void handlesNotFoundOnEditAndDelete() throws Exception {
        useCase.addIncome(USER_ID, "Temp", 10_000L);
        clickButton("Muat Ulang");

        Transaction selected = useCase.getTransactionsByUser(USER_ID).get(0);

        Grid<Transaction> grid = _get(Grid.class);

        doThrow(new EntityNotFoundException(selected.getId()))
                .when(useCase).removeTransactionForUser(USER_ID, selected.getId());
        HorizontalLayout actions = (HorizontalLayout) _getCellComponent(grid, 0, "tindakan");
        _click(_get(actions, Button.class, spec -> spec.withCaption("Hapus")));
        Dialog deleteDialog = _get(Dialog.class);
        clickDialogButton(deleteDialog, "Hapus");

        doThrow(new EntityNotFoundException(selected.getId()))
                .when(useCase).updateTransactionForUser(eq(USER_ID), eq(selected.getId()), anyString(), anyLong(), any(), isNull(), eq(false));
        _doubleClickItem(grid, 0);
        Dialog editDialog = _get(Dialog.class);
        clickDialogButton(editDialog, "Simpan");
        clickDialogButton(editDialog, "Batal");

        doThrow(new ValidationException("bukan milik"))
                .when(useCase).removeTransactionForUser(anyInt(), anyInt());
        _click(_get(actions, Button.class, spec -> spec.withCaption("Hapus")));
        Dialog deleteDialog2 = _get(Dialog.class);
        clickDialogButton(deleteDialog2, "Hapus");
    }

    private int indexOfDescription(Grid<Transaction> grid, String description) {
        List<Transaction> items = grid.getListDataView().getItems().toList();
        for (int i = 0; i < items.size(); i++) {
            if (description.equals(items.get(i).getDescription())) {
                return i;
            }
        }
        throw new AssertionError("Transaksi tidak ditemukan: " + description);
    }

    private void assertSummary(String total, String income, String expense, String balance) {
        FinanceView view = _get(FinanceView.class);
        HorizontalLayout summary = findFirst(view, HorizontalLayout.class);
        List<String> values = summary.getChildren()
                .map(card -> ((Div) card).getChildren()
                        .skip(1)
                        .map(component -> ((Span) component).getText())
                        .findFirst()
                        .orElse(""))
                .toList();
        assertEquals(List.of(total, income, expense, balance), values);
    }

    private <T extends com.vaadin.flow.component.Component> T findFirst(
            com.vaadin.flow.component.Component root,
            Class<T> type
    ) {
        T found = findFirstOrNull(root, type);
        if (found == null) {
            throw new java.util.NoSuchElementException(type.getSimpleName() + " not found");
        }
        return found;
    }

    private <T extends com.vaadin.flow.component.Component> T findFirstOrNull(
            com.vaadin.flow.component.Component root,
            Class<T> type
    ) {
        if (type.isInstance(root)) {
            return type.cast(root);
        }
        return root.getChildren()
                .map(child -> findFirstOrNull(child, type))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private void clickButton(String caption) {
        _click(_get(Button.class, spec -> spec.withCaption(caption)));
    }

    private void clickDialogButton(Dialog dialog, String caption) {
        _click(_get(dialog, Button.class, spec -> spec.withCaption(caption)));
    }
}

