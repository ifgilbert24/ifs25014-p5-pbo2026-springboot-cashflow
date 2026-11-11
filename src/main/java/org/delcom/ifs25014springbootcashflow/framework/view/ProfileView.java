package org.delcom.ifs25014springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import org.delcom.ifs25014springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25014springbootcashflow.domain.entity.User;
import org.delcom.ifs25014springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25014springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25014springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25014springbootcashflow.usecase.UserUseCase;

import java.io.ByteArrayInputStream;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil | Cashflow")
public class ProfileView extends VerticalLayout {

    public ProfileView(UserUseCase userUseCase, FileStorage fileStorage) {
        getElement().setAttribute("role", "main");
        getElement().setAttribute("aria-label", "Profil Pengguna");
        setWidthFull();
        setPadding(true);
        setSpacing(false);

        Div card = PageCard.create();
        card.setMaxWidth("640px");

        H2 title = new H2("Profil Pengguna");
        title.getStyle()
                .set("color", "#1E3A8A")
                .set("margin", "0")
                .set("font-size", "1.35rem");

        User current = AuthSession.getUser().orElse(null);
        if (current == null) {
            card.add(title);
            add(card);
            return;
        }

        Image photo = new Image();
        photo.setWidth("96px");
        photo.setHeight("96px");
        photo.getStyle()
                .set("border-radius", "50%")
                .set("object-fit", "cover")
                .set("border", "3px solid #2563EB")
                .set("background", "#DBEAFE");
        refreshPhoto(photo, current.getPhotoPath());
        photo.addClickListener(e -> {
            User latest = AuthSession.getUser().orElse(current);
            if (latest.getPhotoPath() != null && !latest.getPhotoPath().isBlank()) {
                ImagePreviewDialog.open("/uploads/" + latest.getPhotoPath(), latest.getFullName());
            }
        });

        Upload upload = new Upload(UploadHandler.inMemory((metadata, bytes) -> {
            String storedPath = fileStorage.store(
                    "profiles",
                    metadata.fileName(),
                    new ByteArrayInputStream(bytes)
            );
            try {
                User updated = userUseCase.updatePhoto(current.getId(), storedPath);
                AuthSession.login(updated);
                refreshPhoto(photo, updated.getPhotoPath());
                showSuccess("Foto profil diperbarui.");
            } catch (EntityNotFoundException | ValidationException e) {
                showError(e.getMessage());
            }
        }));
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/webp");
        upload.setMaxFiles(1);
        upload.setUploadButton(new Button("Unggah Foto Profil"));

        HorizontalLayout photoSection = new HorizontalLayout(photo, upload);
        photoSection.setAlignItems(FlexComponent.Alignment.CENTER);
        photoSection.getStyle()
                .set("padding", "1rem")
                .set("background", "#F8FAFC")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "10px")
                .set("gap", "1.25rem");

        Span infoLine = new Span("Username: " + current.getUsername() + "  •  Peran: " + current.getRole());
        infoLine.getStyle()
                .set("color", "#475569")
                .set("font-size", "0.9rem")
                .set("font-weight", "600");

        TextField fullNameField = new TextField("Nama Lengkap");
        fullNameField.setValue(current.getFullName());
        fullNameField.setWidthFull();

        PasswordField newPasswordField = new PasswordField("Kata Sandi Baru (kosongkan jika tidak diubah)");
        newPasswordField.setWidthFull();

        Button saveBtn = new Button("Simpan Profil");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.setDisableOnClick(true);
        saveBtn.addClickListener(event -> {
            try {
                User updated = userUseCase.updateProfile(
                        current.getId(),
                        fullNameField.getValue(),
                        newPasswordField.getValue()
                );
                AuthSession.login(updated);
                newPasswordField.clear();
                showSuccess("Profil berhasil disimpan.");
            } catch (EntityNotFoundException | ValidationException e) {
                showError(e.getMessage());
            } finally {
                saveBtn.setEnabled(true);
            }
        });

        HorizontalLayout actions = new HorizontalLayout(saveBtn);
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        card.add(title, photoSection, infoLine, fullNameField, newPasswordField, actions);
        add(card);
    }

    private void refreshPhoto(Image photo, String photoPath) {
        if (photoPath != null && !photoPath.isBlank()) {
            photo.setSrc("/uploads/" + photoPath);
            photo.setAlt("Foto profil");
            photo.setTitle("Klik untuk memperbesar");
            photo.getStyle().set("cursor", "pointer");
        } else {
            photo.setSrc("https://api.dicebear.com/9.x/initials/svg?seed=User");
            photo.setAlt("Belum ada foto");
            photo.setTitle("");
            photo.getStyle().set("cursor", "default");
        }
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

