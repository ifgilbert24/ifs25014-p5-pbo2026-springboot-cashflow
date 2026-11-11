package org.delcom.ifs25014springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;

public final class ImagePreviewDialog {

    private ImagePreviewDialog() {
    }

    public static void open(String imageUrl, String title) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title == null || title.isBlank() ? "Pratinjau Gambar" : title);
        dialog.setWidth("min(90vw, 720px)");

        Image image = new Image(imageUrl, title == null ? "" : title);
        image.setWidthFull();
        image.getStyle()
                .set("max-height", "72vh")
                .set("object-fit", "contain")
                .set("border-radius", "8px")
                .set("display", "block")
                .set("margin", "0 auto");

        Button closeBtn = new Button("Tutup", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(image);
        dialog.getFooter().add(closeBtn);
        dialog.open();
    }
}

