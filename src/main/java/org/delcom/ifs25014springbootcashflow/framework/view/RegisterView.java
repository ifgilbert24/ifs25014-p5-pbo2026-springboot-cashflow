package org.delcom.ifs25014springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25014springbootcashflow.domain.entity.User;
import org.delcom.ifs25014springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25014springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25014springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25014springbootcashflow.usecase.AuthUseCase;

@Route("register")
@PageTitle("Daftar | Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;

    public RegisterView(AuthUseCase authUseCase, IUserRepository userRepository) {
        this.userRepository = userRepository;
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getStyle().set("background", "#EFF6FF");

        VerticalLayout card = new VerticalLayout();
        card.setWidth("420px");
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "#FFFFFF")
                .set("border", "1px solid #DBEAFE")
                .set("border-radius", "12px")
                .set("box-shadow", "0 10px 25px rgba(37, 99, 235, 0.12)")
                .set("padding", "2rem")
                .set("gap", "0.75rem");

        H2 title = new H2("Daftar Akun Baru");
        title.getStyle()
                .set("color", "#1E3A8A")
                .set("margin", "0 0 0.25rem 0")
                .set("font-size", "1.35rem");

        TextField fullNameField = new TextField("Nama Lengkap");
        fullNameField.setWidthFull();

        TextField usernameField = new TextField("Username");
        usernameField.setWidthFull();

        PasswordField passwordField = new PasswordField("Kata Sandi (min. 4 karakter)");
        passwordField.setWidthFull();

        Button registerButton = new Button("Daftar");
        registerButton.setWidthFull();
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setDisableOnClick(true);
        registerButton.getStyle()
                .set("margin-top", "0.5rem")
                .set("height", "2.6rem")
                .set("font-weight", "600");
        registerButton.addClickListener(event -> {
            try {
                User user = authUseCase.register(
                        usernameField.getValue(),
                        passwordField.getValue(),
                        fullNameField.getValue()
                );
                AuthSession.login(user);
                Notification.show("Pendaftaran berhasil!", 2500, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate(FinanceView.class);
            } catch (ValidationException e) {
                registerButton.setEnabled(true);
                Notification notification = Notification.show(e.getMessage(), 3000, Notification.Position.TOP_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        RouterLink loginLink = new RouterLink("Masuk di sini", LoginView.class);
        loginLink.getStyle()
                .set("color", "#2563EB")
                .set("font-weight", "600");

        Span footer = new Span(new Span("Sudah punya akun? "), loginLink);
        footer.getStyle()
                .set("font-size", "0.9rem")
                .set("color", "#4B5563")
                .set("margin-top", "0.25rem");

        card.add(title, fullNameField, usernameField, passwordField, registerButton, footer);
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.resolveUser(userRepository).isPresent()) {
            event.forwardTo(FinanceView.class);
        }
    }
}

