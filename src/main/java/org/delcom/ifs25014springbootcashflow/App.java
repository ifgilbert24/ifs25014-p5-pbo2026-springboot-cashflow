package org.delcom.ifs25014springbootcashflow;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet("styles.css")
@PageTitle("Cashflow - Aplikasi Catatan Keuangan")
@Meta(name = "description", content = "Aplikasi Manajemen Catatan Keuangan (Cashflow) berbasis Spring Boot, Vaadin, dan PostgreSQL.")
@PWA(
        name = "Cashflow",
        shortName = "Cashflow",
        description = "Aplikasi Manajemen Catatan Keuangan (Cashflow) berbasis Spring Boot, Vaadin, dan PostgreSQL.",
        iconPath = "icons/icon.png"
)
public class App implements AppShellConfigurator {

    public static void main(String[] args) {
        DotEnv.load();
        SpringApplication.run(App.class, args);
    }
}

