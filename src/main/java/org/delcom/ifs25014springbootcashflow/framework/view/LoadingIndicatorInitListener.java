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
        Element body = document.body();
        Element outlet = body.selectFirst("#outlet");
        if (outlet != null && !"main".equalsIgnoreCase(outlet.tagName())) {
            outlet.attr("role", "main");
            outlet.attr("aria-label", "Konten Utama Aplikasi Cashflow");
        }
        if (body.selectFirst("h1") == null) {
            Element srH1 = new Element("h1")
                    .attr("id", "app-sr-h1")
                    .attr("style", "position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0,0,0,0);white-space:nowrap;border:0;")
                    .text("Aplikasi Catatan Keuangan Cashflow");
            if (outlet != null) {
                outlet.prependChild(srH1);
            } else {
                body.prependChild(srH1);
            }
        }
        Element a11yScript = new Element("script").attr("type", "text/javascript").text(
                "(function(){"
                        + "function ensureA11y(){"
                        + "if(!document.title||!document.title.trim()){document.title='Cashflow - Aplikasi Catatan Keuangan';}"
                        + "var outlet=document.getElementById('outlet');"
                        + "if(outlet){"
                        + "var hasInnerMain=outlet.querySelector('main, [role=\"main\"]');"
                        + "if(hasInnerMain){outlet.removeAttribute('role');outlet.removeAttribute('aria-label');}"
                        + "else{outlet.setAttribute('role','main');outlet.setAttribute('aria-label','Konten Utama Aplikasi Cashflow');}"
                        + "var allH1s=outlet.querySelectorAll('h1');"
                        + "var srH1=document.getElementById('app-sr-h1');"
                        + "if(allH1s.length>1&&srH1){srH1.style.display='none';srH1.setAttribute('aria-hidden','true');}"
                        + "else if(allH1s.length===0){"
                        + "var h=document.createElement('h1');"
                        + "h.id='app-sr-h1';"
                        + "h.style.cssText='position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0,0,0,0);white-space:nowrap;border:0;';"
                        + "h.textContent='Aplikasi Catatan Keuangan Cashflow';"
                        + "outlet.insertBefore(h,outlet.firstChild);"
                        + "}"
                        + "}"
                        + "var indicators=document.querySelectorAll('.v-loading-indicator, vaadin-connection-indicator');"
                        + "for(var i=0;i<indicators.length;i++){"
                        + "if(!indicators[i].getAttribute('role')){indicators[i].setAttribute('role','status');indicators[i].setAttribute('aria-label','Memuat halaman');}"
                        + "}"
                        + "}"
                        + "ensureA11y();"
                        + "var obs=new MutationObserver(ensureA11y);"
                        + "obs.observe(document.documentElement,{childList:true,subtree:true,characterData:true});"
                        + "})();"
        );
        head.appendChild(a11yScript);
    }
}

