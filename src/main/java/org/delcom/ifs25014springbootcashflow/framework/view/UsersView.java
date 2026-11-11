package org.delcom.ifs25014springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25014springbootcashflow.domain.entity.User;
import org.delcom.ifs25014springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25014springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25014springbootcashflow.usecase.UserUseCase;

import java.util.Optional;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Daftar Pengguna | Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;

    public UsersView(UserUseCase userUseCase, IUserRepository userRepository) {
        this.userRepository = userRepository;
        getElement().setAttribute("role", "main");
        getElement().setAttribute("aria-label", "Daftar Pengguna");
        setWidthFull();
        setPadding(true);
        setSpacing(false);

        Div card = PageCard.create();

        H2 title = new H2("Daftar Pengguna");
        title.getStyle()
                .set("color", "#1E3A8A")
                .set("margin", "0")
                .set("font-size", "1.35rem");

        Grid<User> grid = new Grid<>(User.class, false);
        grid.setAllRowsVisible(true);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        GridStyles.apply(grid);

        grid.addComponentColumn(user -> {
            if (user.getPhotoPath() == null || user.getPhotoPath().isBlank()) {
                return new Span("-");
            }
            String imageUrl = "/uploads/" + user.getPhotoPath();
            Image img = new Image(imageUrl, user.getFullName());
            img.setWidth("36px");
            img.setHeight("36px");
            img.setTitle("Klik untuk memperbesar");
            img.getStyle()
                    .set("border-radius", "50%")
                    .set("object-fit", "cover")
                    .set("cursor", "pointer");
            img.addClickListener(e -> ImagePreviewDialog.open(imageUrl, user.getFullName()));
            return img;
        }).setHeader("Foto").setWidth("80px").setFlexGrow(0);

        grid.addColumn(User::getId).setHeader("ID").setWidth("80px").setFlexGrow(0).setSortable(true);
        grid.addColumn(User::getUsername).setHeader("Username").setSortable(true);
        grid.addColumn(User::getFullName).setHeader("Nama Lengkap").setSortable(true);
        grid.addColumn(user -> user.getRole().name()).setHeader("Peran").setWidth("120px").setFlexGrow(0);

        grid.addComponentColumn(user -> {
            Button viewBtn = new Button("Lihat Transaksi",
                    e -> UI.getCurrent().navigate(UserTransactionsView.class, user.getId()));
            viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            return viewBtn;
        }).setHeader("Tindakan").setAutoWidth(true).setFlexGrow(0);

        grid.setItems(userUseCase.listUsers());
        card.add(title, grid);
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<User> current = AuthSession.resolveUser(userRepository);
        if (current.isEmpty() || !current.get().isAdmin()) {
            event.forwardTo(FinanceView.class);
        }
    }
}

