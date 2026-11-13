package org.delcom.ifs25014springbootcashflow.framework.view;

import com.vaadin.flow.component.page.LoadingIndicatorConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Mengaktifkan indikator loading Vaadin secara cepat untuk setiap aksi yang
 * menunggu respons server, mengoptimalkan performa FCP/LCP/TBT/CLS, caching
 * aset statis, serta menyertakan atribut aksesibilitas dan SEO pada dokumen HTML awal.
 */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener, Filter {

    private static final String INLINE_CUSTOM_CSS = loadCustomCss();

    private static String loadCustomCss() {
        try (InputStream in = LoadingIndicatorInitListener.class.getResourceAsStream("/META-INF/resources/styles.css")) {
            if (in == null) {
                return "";
            }
            String raw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return raw.replaceAll("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/", "")
                    .replaceAll("\\s+", " ")
                    .trim();
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        if (req instanceof HttpServletRequest httpReq && res instanceof HttpServletResponse httpRes) {
            String uri = httpReq.getRequestURI();
            if (uri != null && isCacheableStaticAsset(uri)) {
                String cacheValue = "public, max-age=31536000, immutable";
                httpRes.setHeader("Cache-Control", cacheValue);
                HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(httpRes) {
                    @Override
                    public void setHeader(String name, String value) {
                        if (isCacheHeader(name)) {
                            return;
                        }
                        super.setHeader(name, value);
                    }

                    @Override
                    public void addHeader(String name, String value) {
                        if (isCacheHeader(name)) {
                            return;
                        }
                        super.addHeader(name, value);
                    }

                    @Override
                    public void setDateHeader(String name, long date) {
                        if (isCacheHeader(name)) {
                            return;
                        }
                        super.setDateHeader(name, date);
                    }

                    @Override
                    public void addDateHeader(String name, long date) {
                        if (isCacheHeader(name)) {
                            return;
                        }
                        super.addDateHeader(name, date);
                    }
                };
                chain.doFilter(req, wrapper);
                httpRes.setHeader("Cache-Control", cacheValue);
                return;
            }
        }
        chain.doFilter(req, res);
    }

    private static boolean isCacheHeader(String name) {
        return "Cache-Control".equalsIgnoreCase(name)
                || "Pragma".equalsIgnoreCase(name)
                || "Expires".equalsIgnoreCase(name);
    }

    private static boolean isCacheableStaticAsset(String uri) {
        return uri.startsWith("/uploads/")
                || uri.startsWith("/lumo/")
                || uri.startsWith("/VAADIN/build/")
                || uri.startsWith("/VAADIN/static/")
                || uri.startsWith("/icons/")
                || uri.equals("/styles.css")
                || uri.equals("/favicon.ico");
    }

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
        if (html != null) {
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
        if (head.selectFirst("link[rel=icon]") == null) {
            head.appendElement("link")
                    .attr("rel", "icon")
                    .attr("href", "/icons/favicon.svg")
                    .attr("type", "image/svg+xml");
        }

        // Hapus elemen dan preload Copilot/DevTools agar tidak membebani main-thread & CLS
        document.select("vaadin-dev-tools, copilot-main").remove();
        document.select("link[href*=copilot], link[href*=early-project-state], link[href*=section-panel-ui-state], link[href*=track-active-mode-event], script[src*=copilot]").remove();
        for (Element inlineScript : head.select("script:not([src])")) {
            String scriptData = inlineScript.data();
            if (scriptData.contains("devToolsConf") && scriptData.contains("\"enable\":true")) {
                inlineScript.empty();
                inlineScript.appendChild(new DataNode(scriptData.replace("\"enable\":true", "\"enable\":false")));
            }
        }

        // Hilangkan render-blocking CSS & JS di <head>
        for (Element link : head.select("link[rel=stylesheet]")) {
            String href = link.attr("href");
            if (href.contains("styles.css")) {
                link.remove();
            } else if (href.contains("lumo.css")) {
                link.attr("media", "print");
                link.attr("onload", "this.media='all'");
                Element noscript = new Element("noscript");
                noscript.appendElement("link")
                        .attr("rel", "stylesheet")
                        .attr("href", href);
                head.appendChild(noscript);
            }
        }

        for (Element script : document.select("script[src]")) {
            String type = script.attr("type");
            if (!"module".equalsIgnoreCase(type)) {
                script.attr("defer", "");
            }
        }

        if (!INLINE_CUSTOM_CSS.isEmpty() && head.selectFirst("#app-custom-styles") == null) {
            Element customStyle = new Element("style")
                    .attr("id", "app-custom-styles")
                    .appendChild(new DataNode(INLINE_CUSTOM_CSS));
            head.appendChild(customStyle);
        }

        Element criticalStyle = new Element("style").attr("id", "app-critical-shell").appendChild(new DataNode(
                "html,body{margin:0;padding:0;background:#EFF6FF;font-family:system-ui,-apple-system,Segoe UI,Roboto,sans-serif;}"
                        + "#app-fcp-shell{position:fixed;inset:0;display:flex;align-items:center;justify-content:center;background:#EFF6FF;z-index:0;pointer-events:none;contain:layout paint style;}"
                        + "#app-fcp-card{width:400px;max-width:92vw;min-height:340px;background:#FFFFFF;border:1px solid #DBEAFE;border-radius:12px;box-shadow:0 10px 25px rgba(37,99,235,0.12);padding:2rem;box-sizing:border-box;display:flex;flex-direction:column;gap:0.75rem;}"
                        + "#app-fcp-title{color:#1E3A8A;margin:0 0 0.25rem 0;font-size:1.35rem;font-weight:700;line-height:1.3;}"
                        + ".app-fcp-field{height:3.5rem;border-radius:8px;background:#F8FAFC;border:1px solid #E2E8F0;}"
                        + ".app-fcp-btn{height:2.6rem;margin-top:0.5rem;border-radius:8px;background:#2563EB;color:#FFFFFF;display:flex;align-items:center;justify-content:center;font-weight:600;font-size:0.95rem;}"
                        + "#app-fcp-sub{color:#4B5563;margin:0.25rem 0 0 0;font-size:0.9rem;line-height:1.4;}"
                        + "#outlet{position:relative;z-index:1;min-height:100vh;}"
        ));
        head.appendChild(criticalStyle);

        Element body = document.body();
        Element outlet = body.selectFirst("#outlet");
        if (outlet != null && !"main".equalsIgnoreCase(outlet.tagName())) {
            outlet.attr("role", "main");
            outlet.attr("aria-label", "Konten Utama Aplikasi Cashflow");
        }
        if (body.selectFirst("#app-fcp-shell") == null) {
            String path = response.getVaadinRequest() != null && response.getVaadinRequest().getPathInfo() != null
                    ? response.getVaadinRequest().getPathInfo()
                    : "";
            String shellTitle;
            String shellSub;
            if ("/register".equals(path) || "register".equals(path)) {
                shellTitle = "Buat Akun Cashflow Baru";
                shellSub = "Daftar akun baru untuk mulai mencatat pemasukan dan pengeluaran";
            } else if ("/login".equals(path) || "login".equals(path)) {
                shellTitle = "Masuk Aplikasi Cashflow";
                shellSub = "Manajemen Catatan Keuangan Pribadi (Pemasukan & Pengeluaran)";
            } else {
                shellTitle = "Cashflow - Catatan Keuangan";
                shellSub = "Manajemen Catatan Keuangan Pribadi (Pemasukan & Pengeluaran)";
            }
            Element shell = new Element("div").attr("id", "app-fcp-shell");
            Element card = shell.appendElement("div").attr("id", "app-fcp-card");
            card.appendElement("h1").attr("id", "app-fcp-title").text(shellTitle);
            card.appendElement("div").attr("class", "app-fcp-field");
            card.appendElement("div").attr("class", "app-fcp-field");
            card.appendElement("div").attr("class", "app-fcp-btn").text("Memuat...");
            card.appendElement("p").attr("id", "app-fcp-sub").text(shellSub);
            if (outlet != null) {
                outlet.prependChild(shell);
            } else {
                body.prependChild(shell);
            }
        }
        Element a11yScript = new Element("script").attr("type", "text/javascript").appendChild(new DataNode(
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
                        + "if(outlet.hasAttribute('role')){outlet.removeAttribute('role');}"
                        + "if(outlet.hasAttribute('aria-label')){outlet.removeAttribute('aria-label');}"
                        + "}else{"
                        + "if(outlet.getAttribute('role')!=='main'){outlet.setAttribute('role','main');}"
                        + "if(outlet.getAttribute('aria-label')!=='Konten Utama Aplikasi Cashflow'){outlet.setAttribute('aria-label','Konten Utama Aplikasi Cashflow');}"
                        + "}"
                        + "var allH1s=outlet.getElementsByTagName('h1');"
                        + "if(fcpShell&&fcpShell.parentNode&&(hasInnerMain||allH1s.length>1)){"
                        + "fcpShell.parentNode.removeChild(fcpShell);"
                        + "}"
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
                        + "}"
                        + "var indicators=document.querySelectorAll('.v-loading-indicator, vaadin-connection-indicator');"
                        + "for(var i=0;i<indicators.length;i++){"
                        + "if(!indicators[i].getAttribute('role')){indicators[i].setAttribute('role','status');indicators[i].setAttribute('aria-label','Memuat halaman');}"
                        + "}"
                        + "var avatars=document.getElementsByTagName('vaadin-avatar');"
                        + "for(var k=0;k<avatars.length;k++){"
                        + "var sr=avatars[k].shadowRoot;"
                        + "if(sr){"
                        + "var aimg=sr.querySelector('img');"
                        + "if(aimg&&!aimg.hasAttribute('decoding')){"
                        + "aimg.setAttribute('width','36');aimg.setAttribute('height','36');aimg.setAttribute('decoding','async');"
                        + "}"
                        + "}"
                        + "}"
                        + "}"
                        + "ensureA11y();"
                        + "var timer=0;"
                        + "var outlet=document.getElementById('outlet');"
                        + "if(outlet){"
                        + "var obs=new MutationObserver(function(){"
                        + "if(!timer){timer=setTimeout(function(){timer=0;ensureA11y();},60);}"
                        + "});"
                        + "obs.observe(outlet,{childList:true,subtree:false});"
                        + "var root=outlet.querySelector('[id^=\"ROOT\"]');"
                        + "if(root){obs.observe(root,{childList:true,subtree:false});}"
                        + "}"
                        + "})();"
        ));
        body.appendChild(a11yScript);
    }
}
