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
        Element criticalStyle = new Element("style").attr("id", "app-critical-shell").text(
                "html,body{margin:0;padding:0;background:#EFF6FF;font-family:system-ui,-apple-system,Segoe UI,Roboto,sans-serif;}"
                        + "#app-fcp-shell{position:fixed;inset:0;display:flex;align-items:center;justify-content:center;background:#EFF6FF;z-index:0;pointer-events:none;transition:opacity 0.12s ease;}"
                        + "#app-fcp-card{width:400px;max-width:92vw;background:#FFFFFF;border:1px solid #DBEAFE;border-radius:12px;box-shadow:0 10px 25px rgba(37,99,235,0.12);padding:2rem;box-sizing:border-box;}"
                        + "#app-fcp-title{color:#1E3A8A;margin:0 0 0.75rem 0;font-size:1.35rem;font-weight:700;line-height:1.3;}"
                        + "#app-fcp-sub{color:#4B5563;margin:0;font-size:0.9rem;line-height:1.4;}"
                        + "#outlet{position:relative;z-index:1;min-height:100vh;}"
        );
        head.appendChild(criticalStyle);

        Element body = document.body();
        Element outlet = body.selectFirst("#outlet");
        if (outlet != null && !"main".equalsIgnoreCase(outlet.tagName())) {
            outlet.attr("role", "main");
            outlet.attr("aria-label", "Konten Utama Aplikasi Cashflow");
        }
        if (body.selectFirst("#app-fcp-shell") == null) {
            Element shell = new Element("div").attr("id", "app-fcp-shell");
            Element card = shell.appendElement("div").attr("id", "app-fcp-card");
            card.appendElement("h1").attr("id", "app-fcp-title").text("Masuk Aplikasi Cashflow");
            card.appendElement("p").attr("id", "app-fcp-sub").text("Manajemen Catatan Keuangan Pribadi (Pemasukan & Pengeluaran)");
            if (outlet != null) {
                outlet.prependChild(shell);
            } else {
                body.prependChild(shell);
            }
        }
        Element a11yScript = new Element("script").attr("type", "text/javascript").text(
                "(function(){"
                        + "var scheduled=false;"
                        + "function ensureA11y(){"
                        + "scheduled=false;"
                        + "if(!document.title||!document.title.trim()){document.title='Cashflow - Aplikasi Catatan Keuangan';}"
                        + "var outlet=document.getElementById('outlet');"
                        + "var fcpShell=document.getElementById('app-fcp-shell');"
                        + "if(outlet){"
                        + "var hasInnerMain=outlet.querySelector('main, [role=\"main\"]');"
                        + "if(hasInnerMain){"
                        + "outlet.removeAttribute('role');outlet.removeAttribute('aria-label');"
                        + "if(fcpShell&&fcpShell.parentNode){fcpShell.parentNode.removeChild(fcpShell);}"
                        + "}else{"
                        + "outlet.setAttribute('role','main');outlet.setAttribute('aria-label','Konten Utama Aplikasi Cashflow');"
                        + "}"
                        + "}"
                        + "var indicators=document.querySelectorAll('.v-loading-indicator, vaadin-connection-indicator');"
                        + "for(var i=0;i<indicators.length;i++){"
                        + "if(!indicators[i].getAttribute('role')){indicators[i].setAttribute('role','status');indicators[i].setAttribute('aria-label','Memuat halaman');}"
                        + "}"
                        + "if('serviceWorker' in navigator){navigator.serviceWorker.getRegistrations().then(function(r){for(var j=0;j<r.length;j++){r[j].unregister();}});}"
                        + "}"
                        + "function scheduleA11y(){if(!scheduled){scheduled=true;requestAnimationFrame(ensureA11y);}}"
                        + "ensureA11y();"
                        + "var obs=new MutationObserver(scheduleA11y);"
                        + "obs.observe(document.documentElement,{childList:true,subtree:true});"
                        + "})();"
        );
        head.appendChild(a11yScript);
    }
}

