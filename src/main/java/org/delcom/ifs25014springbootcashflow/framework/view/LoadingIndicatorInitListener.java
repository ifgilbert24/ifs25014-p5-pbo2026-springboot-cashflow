package org.delcom.ifs25014springbootcashflow.framework.view;

import com.vaadin.flow.component.page.LoadingIndicatorConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Mengaktifkan indikator loading Vaadin secara cepat untuk setiap aksi yang
 * menunggu respons server, dengan styling kustom di {@code styles.css}, serta
 * menyertakan atribut aksesibilitas dan SEO pada dokumen HTML awal.
 */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addIndexHtmlRequestListener(this::configureIndexHtml);
        event.getSource().addUIInitListener(uiEvent -> {
            uiEvent.getUI().setLocale(Locale.forLanguageTag("id-ID"));
            LoadingIndicatorConfiguration conf = uiEvent.getUI().getLoadingIndicatorConfiguration();
            conf.setApplyDefaultTheme(false);
            conf.setFirstDelay(80);
            conf.setSecondDelay(1200);
            conf.setThirdDelay(4000);
        });
    }

    private void configureIndexHtml(IndexHtmlResponse response) {
        Document document = response.getDocument();
        Element html = document.selectFirst("html");
        if (html != null && html.attr("lang").isBlank()) {
            html.attr("lang", "id");
        }
        Element head = document.head();
        if (head.selectFirst("title") == null) {
            head.appendElement("title").text("Cashflow - Aplikasi Catatan Keuangan");
        } else if (head.selectFirst("title").text().isBlank()) {
            head.selectFirst("title").text("Cashflow - Aplikasi Catatan Keuangan");
        }
        if (head.selectFirst("meta[name=description]") == null) {
            head.appendElement("meta")
                    .attr("name", "description")
                    .attr("content", "Aplikasi Manajemen Catatan Keuangan (Cashflow) berbasis Spring Boot, Vaadin, dan PostgreSQL.");
        }
    }
}

