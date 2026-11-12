package org.delcom.ifs25014springbootcashflow.framework.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import jakarta.servlet.http.HttpServletResponse;

@PageTitle("Halaman Tidak Ditemukan | Cashflow")
public class RouteNotFoundView extends VerticalLayout implements HasErrorParameter<NotFoundException> {

    private final Paragraph detail = new Paragraph();

    public RouteNotFoundView() {
        getElement().setAttribute("role", "main");
        getElement().setAttribute("aria-label", "Halaman Tidak Ditemukan");
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getStyle().set("background", "#EFF6FF");

        VerticalLayout card = new VerticalLayout();
        card.setWidth("420px");
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

        H1 title = new H1("Halaman Tidak Ditemukan");
        title.getStyle()
                .set("color", "#1E3A8A")
                .set("margin", "0")
                .set("font-size", "1.35rem");

        detail.getStyle()
                .set("color", "#475569")
                .set("margin", "0");

        RouterLink backLink = new RouterLink("Kembali ke Halaman Masuk", LoginView.class);
        backLink.getStyle()
                .set("color", "#2563EB")
                .set("font-weight", "600");

        card.add(title, detail, backLink);
        add(card);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        detail.setText("Halaman \"" + event.getLocation().getPath() + "\" tidak ditemukan pada aplikasi Cashflow.");
        return HttpServletResponse.SC_OK;
    }
}
