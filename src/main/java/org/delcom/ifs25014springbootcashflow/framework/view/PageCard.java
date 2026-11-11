package org.delcom.ifs25014springbootcashflow.framework.view;

import com.vaadin.flow.component.html.Div;

public final class PageCard {

    private PageCard() {
    }

    public static Div create() {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("background", "#FFFFFF")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "12px")
                .set("box-shadow", "0 1px 3px rgba(15, 23, 42, 0.06)")
                .set("padding", "1.5rem")
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "1.1rem");
        return card;
    }
}

