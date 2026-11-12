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
                        + "#app-fcp-shell{position:fixed;inset:0;display:flex;align-items:center;justify-content:center;background:#EFF6FF;z-index:0;pointer-events:none;}"
                        + "#app-fcp-card{width:400px;max-width:92vw;min-height:340px;background:#FFFFFF;border:1px solid #DBEAFE;border-radius:12px;box-shadow:0 10px 25px rgba(37,99,235,0.12);padding:2rem;box-sizing:border-box;display:flex;flex-direction:column;gap:0.75rem;}"
                        + "#app-fcp-title{color:#1E3A8A;margin:0 0 0.25rem 0;font-size:1.35rem;font-weight:700;line-height:1.3;}"
                        + ".app-fcp-field{height:3.5rem;border-radius:8px;background:#F8FAFC;border:1px solid #E2E8F0;}"
                        + ".app-fcp-btn{height:2.6rem;margin-top:0.5rem;border-radius:8px;background:#2563EB;color:#FFFFFF;display:flex;align-items:center;justify-content:center;font-weight:600;font-size:0.95rem;}"
                        + "#app-fcp-sub{color:#4B5563;margin:0.25rem 0 0 0;font-size:0.9rem;line-height:1.4;}"
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
            card.appendElement("div").attr("class", "app-fcp-field");
            card.appendElement("div").attr("class", "app-fcp-field");
            card.appendElement("div").attr("class", "app-fcp-btn").text("Masuk");
            card.appendElement("p").attr("id", "app-fcp-sub").text("Manajemen Catatan Keuangan Pribadi (Pemasukan & Pengeluaran)");
            if (outlet != null) {
                outlet.prependChild(shell);
            } else {
                body.prependChild(shell);
            }
        }
        Element a11yScript = new Element("script").attr("type", "text/javascript").text(
                "(function(){"
                        + "function ensureA11y(){"
                        + "var docEl=document.documentElement;"
                        + "if(docEl&&docEl.getAttribute('lang')!=='id'){docEl.setAttribute('lang','id');}"
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
                        + "var allH1s=outlet.querySelectorAll('h1');"
                        + "var srH1=document.getElementById('app-sr-h1');"
                        + "if(allH1s.length>1&&srH1){srH1.parentNode.removeChild(srH1);}"
                        + "else if(allH1s.length===0){"
                        + "var h=document.createElement('h1');"
                        + "h.id='app-sr-h1';"
                        + "h.style.cssText='position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0,0,0,0);white-space:nowrap;border:0;';"
                        + "h.textContent='Aplikasi Catatan Keuangan Cashflow';"
                        + "if(hasInnerMain){hasInnerMain.insertBefore(h,hasInnerMain.firstChild);}"
                        + "else{outlet.insertBefore(h,outlet.firstChild);}"
                        + "}"
                        + "}else if(!document.querySelector('h1')){"
                        + "var bMain=document.querySelector('main,[role=\"main\"]')||document.body;"
                        + "if(bMain){"
                        + "if(bMain===document.body&&!bMain.getAttribute('role')){bMain.setAttribute('role','main');}"
                        + "var bh=document.createElement('h1');"
                        + "bh.id='app-sr-h1';"
                        + "bh.style.cssText='position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0,0,0,0);white-space:nowrap;border:0;';"
                        + "bh.textContent='Aplikasi Catatan Keuangan Cashflow';"
                        + "bMain.insertBefore(bh,bMain.firstChild);"
                        + "}"
                        + "}"
                        + "var indicators=document.querySelectorAll('.v-loading-indicator, vaadin-connection-indicator');"
                        + "for(var i=0;i<indicators.length;i++){"
                        + "if(!indicators[i].getAttribute('role')){indicators[i].setAttribute('role','status');indicators[i].setAttribute('aria-label','Memuat halaman');}"
                        + "}"
                        + "}"
                        + "if('serviceWorker' in navigator){navigator.serviceWorker.getRegistrations().then(function(r){for(var j=0;j<r.length;j++){r[j].unregister();}});}"
                        + "ensureA11y();"
                        + "var obs=new MutationObserver(ensureA11y);"
                        + "obs.observe(document.documentElement,{childList:true,subtree:true});"
                        + "})();"
        );
        head.appendChild(a11yScript);
    }
}

