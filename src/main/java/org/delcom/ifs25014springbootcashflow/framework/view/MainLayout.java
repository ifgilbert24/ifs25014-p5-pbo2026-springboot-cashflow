package org.delcom.ifs25014springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25014springbootcashflow.domain.entity.User;
import org.delcom.ifs25014springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25014springbootcashflow.framework.auth.AuthSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainLayout extends AppLayout implements BeforeEnterObserver, AfterNavigationObserver {

    private final IUserRepository userRepository;
    private final HorizontalLayout headerRight = new HorizontalLayout();
    private final VerticalLayout drawerMenu = new VerticalLayout();
    private final List<NavEntry> navEntries = new ArrayList<>();

    public MainLayout(IUserRepository userRepository) {
        this.userRepository = userRepository;
        setPrimarySection(Section.DRAWER);
        getElement().setAttribute("role", "region");
        getElement().setAttribute("aria-label", "Tata Letak Aplikasi Cashflow");
        getStyle().set("background", "#F1F5F9");

        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Buka atau tutup menu navigasi");
        toggle.getStyle().set("color", "#FFFFFF");

        H1 brand = new H1("Cashflow");
        brand.getStyle()
                .set("font-size", "1.15rem")
                .set("font-weight", "700")
                .set("margin", "0")
                .set("color", "#FFFFFF");

        headerRight.setAlignItems(FlexComponent.Alignment.CENTER);
        headerRight.setSpacing(false);

        HorizontalLayout navbar = new HorizontalLayout(toggle, brand, headerRight);
        navbar.getElement().setAttribute("role", "banner");
        navbar.setWidthFull();
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.expand(brand);
        navbar.getStyle()
                .set("background", "#1E40AF")
                .set("padding", "0.5rem 1.25rem")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.15)");

        addToNavbar(navbar);

        drawerMenu.getElement().setAttribute("role", "navigation");
        drawerMenu.getElement().setAttribute("aria-label", "Menu Utama");
        drawerMenu.setPadding(false);
        drawerMenu.setSpacing(false);
        drawerMenu.setSizeFull();
        drawerMenu.getStyle()
                .set("background", "#FFFFFF")
                .set("border-right", "1px solid #E2E8F0")
                .set("padding", "1rem 0.75rem")
                .set("gap", "0.35rem");

        addToDrawer(drawerMenu);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<User> current = AuthSession.resolveUser(userRepository);
        if (current.isEmpty()) {
            event.forwardTo(LoginView.class);
            return;
        }
        rebuildNavigation(current.get());
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String activePath = event.getLocation().getPath();
        for (NavEntry entry : navEntries) {
            boolean active = entry.matches(activePath);
            applyNavStyle(entry.link, entry.icon, entry.text, active);
        }
    }

    private void rebuildNavigation(User user) {
        headerRight.removeAll();
        drawerMenu.removeAll();
        navEntries.clear();

        Span menuTitle = new Span("MENU UTAMA");
        menuTitle.getStyle()
                .set("font-size", "0.72rem")
                .set("font-weight", "700")
                .set("letter-spacing", "0.08em")
                .set("color", "#64748B")
                .set("padding", "0.25rem 0.75rem 0.5rem");
        drawerMenu.add(menuTitle);

        drawerMenu.add(createNavItem("Catatan Keuangan", VaadinIcon.WALLET, FinanceView.class, ""));
        drawerMenu.add(createNavItem("Profil", VaadinIcon.USER, ProfileView.class, "profile"));
        if (user.isAdmin()) {
            drawerMenu.add(createNavItem("Daftar Pengguna", VaadinIcon.USERS, UsersView.class, "users", "user-transactions"));
        }

        headerRight.add(buildAvatarMenu(user));
    }

    private RouterLink createNavItem(
            String label,
            VaadinIcon vaadinIcon,
            Class<? extends Component> target,
            String... matchPrefixes
    ) {
        Icon icon = vaadinIcon.create();
        icon.setSize("18px");

        Span text = new Span(label);

        RouterLink link = new RouterLink();
        link.setRoute(target);
        link.add(icon, text);
        link.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "0.75rem")
                .set("width", "100%")
                .set("box-sizing", "border-box")
                .set("padding", "0.65rem 0.85rem")
                .set("border-radius", "8px")
                .set("text-decoration", "none")
                .set("font-size", "0.92rem")
                .set("transition", "background-color 0.15s ease");

        applyNavStyle(link, icon, text, false);
        navEntries.add(new NavEntry(link, icon, text, matchPrefixes));
        return link;
    }

    private void applyNavStyle(RouterLink link, Icon icon, Span text, boolean active) {
        if (active) {
            link.getStyle()
                    .set("background", "#DBEAFE")
                    .set("color", "#1E40AF")
                    .set("font-weight", "700");
            icon.getStyle().set("color", "#1E40AF");
            text.getStyle().set("color", "#1E40AF");
        } else {
            link.getStyle()
                    .set("background", "transparent")
                    .set("color", "#334155")
                    .set("font-weight", "500");
            icon.getStyle().set("color", "#64748B");
            text.getStyle().set("color", "#334155");
        }
    }

    private MenuBar buildAvatarMenu(User user) {
        Avatar avatar = new Avatar(user.getFullName());
        if (user.getPhotoPath() != null && !user.getPhotoPath().isBlank()) {
            avatar.setImage("/uploads/" + user.getPhotoPath());
        }
        avatar.getStyle()
                .set("background", "#FFFFFF")
                .set("color", "#1E40AF")
                .set("font-weight", "700")
                .set("border", "2px solid #93C5FD");

        Span nameLabel = new Span(user.getFullName());
        nameLabel.getStyle()
                .set("color", "#FFFFFF")
                .set("font-weight", "600")
                .set("font-size", "0.9rem");

        Icon caret = VaadinIcon.CHEVRON_DOWN_SMALL.create();
        caret.setSize("14px");
        caret.getStyle().set("color", "#DBEAFE");

        Div trigger = new Div(avatar, nameLabel, caret);
        trigger.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "0.55rem")
                .set("padding", "0.2rem 0.5rem")
                .set("border-radius", "999px");

        MenuBar menuBar = new MenuBar();
        menuBar.getElement().setAttribute("aria-label", "Menu Akun Pengguna");
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);

        MenuItem rootItem = menuBar.addItem(trigger);
        SubMenu subMenu = rootItem.getSubMenu();

        subMenu.addItem(createDropdownRow(VaadinIcon.USER, "Profil", "#1E293B"),
                e -> UI.getCurrent().navigate(ProfileView.class));

        subMenu.addItem(createDropdownRow(VaadinIcon.SIGN_OUT, "Keluar", "#DC2626"), e -> {
            AuthSession.logout();
            UI.getCurrent().navigate(LoginView.class);
        });

        return menuBar;
    }

    private Div createDropdownRow(VaadinIcon vaadinIcon, String label, String color) {
        Icon icon = vaadinIcon.create();
        icon.setSize("16px");
        icon.getStyle().set("color", color).set("flex-shrink", "0");

        Span text = new Span(label);
        text.getStyle()
                .set("color", color)
                .set("font-weight", "500")
                .set("font-size", "0.9rem");

        Div row = new Div(icon, text);
        row.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("justify-content", "flex-start")
                .set("gap", "0.6rem");
        return row;
    }

    private record NavEntry(RouterLink link, Icon icon, Span text, String[] prefixes) {
        boolean matches(String path) {
            String cleaned = path == null ? "" : path.trim();
            for (String prefix : prefixes) {
                if (prefix.isEmpty() && cleaned.isEmpty()) {
                    return true;
                }
                if (!prefix.isEmpty() && (cleaned.equals(prefix) || cleaned.startsWith(prefix + "/"))) {
                    return true;
                }
            }
            return false;
        }
    }
}

