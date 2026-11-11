package org.delcom.ifs25014springbootcashflow.framework.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
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

@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;

    public LoginView(AuthUseCase authUseCase, IUserRepository userRepository) {
        this.userRepository = userRepository;
        getElement().setAttribute("role", "main");
        getElement().setAttribute("aria-label", "Halaman Masuk");
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getStyle().set("background", "#EFF6FF");

        VerticalLayout card = new VerticalLayout();
        card.setWidth("400px");
        card.setMaxWidth("92vw");
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "#FFFFFF")
                .set("border", "1px solid #DBEAFE")
                .set("border-radius", "12px")
                .set("box-shadow", "0 10px 25px rgba(37, 99, 235, 0.12)")
                .set("padding", "2rem")
                .set("gap", "0.75rem");

        H1 title = new H1("Masuk Aplikasi Cashflow");
        title.getStyle()
                .set("color", "#1E3A8A")
                .set("margin", "0 0 0.25rem 0")
                .set("font-size", "1.35rem");

        TextField usernameField = new TextField("Username");
        usernameField.setWidthFull();
        usernameField.setId("login-username-field");
        setInnerInputId(usernameField, "login-username");

        PasswordField passwordField = new PasswordField("Kata Sandi");
        passwordField.setWidthFull();
        passwordField.setId("login-password-field");
        setInnerInputId(passwordField, "login-password");

        Button loginButton = new Button("Masuk");
        loginButton.setId("login-submit");
        loginButton.setWidthFull();
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setDisableOnClick(true);
        loginButton.getStyle()
                .set("margin-top", "0.5rem")
                .set("height", "2.6rem")
                .set("font-weight", "600");
        loginButton.addClickShortcut(Key.ENTER);
        loginButton.addClickListener(event -> {
            try {
                User user = authUseCase.login(usernameField.getValue(), passwordField.getValue());
                AuthSession.login(user);
                UI.getCurrent().navigate(FinanceView.class);
            } catch (ValidationException e) {
                loginButton.setEnabled(true);
                Notification notification = Notification.show(e.getMessage(), 3000, Notification.Position.TOP_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        RouterLink registerLink = new RouterLink("Daftar di sini", RegisterView.class);
        registerLink.getStyle()
                .set("color", "#2563EB")
                .set("font-weight", "600");

        Span footer = new Span(new Span("Belum punya akun? "), registerLink);
        footer.getStyle()
                .set("font-size", "0.9rem")
                .set("color", "#4B5563")
                .set("margin-top", "0.25rem");

        card.add(title, usernameField, passwordField, loginButton, footer);
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.resolveUser(userRepository).isPresent()) {
            event.forwardTo(FinanceView.class);
        }
    }

    private static void setInnerInputId(com.vaadin.flow.component.Component field, String id) {
        field.addAttachListener(e -> field.getElement().executeJs(
                "const input = this.inputElement || this.querySelector('input');"
                        + "if (input) { input.id = $0; input.name = $0; }",
                id
        ));
    }
}

