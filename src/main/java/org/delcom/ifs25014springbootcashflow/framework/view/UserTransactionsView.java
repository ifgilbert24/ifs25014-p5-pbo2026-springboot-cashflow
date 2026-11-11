package org.delcom.ifs25014springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25014springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25014springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25014springbootcashflow.domain.entity.User;
import org.delcom.ifs25014springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25014springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25014springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25014springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25014springbootcashflow.usecase.UserUseCase;

import java.util.List;
import java.util.Optional;

@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna | Cashflow")
public class UserTransactionsView extends VerticalLayout implements HasUrlParameter<Integer>, BeforeEnterObserver {

    private final FinanceUseCase financeUseCase;
    private final UserUseCase userUseCase;
    private final IUserRepository userRepository;
    private final FinancePresenter presenter;
    private final H2 header = new H2();
    private final Paragraph statusMessage = new Paragraph();
    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);

    public UserTransactionsView(
            FinanceUseCase financeUseCase,
            UserUseCase userUseCase,
            IUserRepository userRepository,
            FinancePresenter presenter
    ) {
        this.financeUseCase = financeUseCase;
        this.userUseCase = userUseCase;
        this.userRepository = userRepository;
        this.presenter = presenter;

        setWidthFull();
        setPadding(true);
        setSpacing(false);

        Div card = PageCard.create();

        Button backBtn = new Button("← Kembali ke Daftar Pengguna",
                e -> UI.getCurrent().navigate(UsersView.class));
        backBtn.setWidth("fit-content");

        header.getStyle()
                .set("color", "#1E3A8A")
                .set("margin", "0")
                .set("font-size", "1.35rem");

        statusMessage.getStyle()
                .set("color", "#475569")
                .set("font-weight", "600")
                .set("margin", "0");

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
        }).setHeader("Bukti").setWidth("80px").setFlexGrow(0);
        grid.addColumn(Transaction::getId).setHeader("ID").setWidth("80px").setFlexGrow(0).setSortable(true);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setSortable(true);
        grid.addColumn(t -> presenter.formatCurrency(t.getAmount()))
                .setHeader("Jumlah")
                .setWidth("180px")
                .setFlexGrow(0)
                .setSortable(true);
        grid.addColumn(t -> t.getType().getLabel())
                .setHeader("Tipe")
                .setWidth("140px")
                .setFlexGrow(0)
                .setSortable(true);
        grid.addColumn(t -> presenter.formatDateTime(t.getCreatedAt()))
                .setHeader("Dibuat")
                .setWidth("180px")
                .setFlexGrow(0);

        card.add(backBtn, header, statusMessage, grid);
        add(card);
    }

    @Override
    public void setParameter(BeforeEvent event, Integer userId) {
        try {
            User target = userUseCase.getUser(userId);
            header.setText("Transaksi Milik: " + target.getFullName() + " (@" + target.getUsername() + ")");
            List<Transaction> transactions = financeUseCase.getTransactionsByUser(userId);
            grid.setItems(transactions);
            statusMessage.setText(transactions.isEmpty()
                    ? presenter.emptyMessage()
                    : presenter.renderBalance(financeUseCase.getBalance(userId)));
        } catch (EntityNotFoundException e) {
            header.setText("Pengguna tidak ditemukan");
            statusMessage.setText(e.getMessage());
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<User> current = AuthSession.resolveUser(userRepository);
        if (current.isEmpty() || !current.get().isAdmin()) {
            event.forwardTo(FinanceView.class);
        }
    }
}

