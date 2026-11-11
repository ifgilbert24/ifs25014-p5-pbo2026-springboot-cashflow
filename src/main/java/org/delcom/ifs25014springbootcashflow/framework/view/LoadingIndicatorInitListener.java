package org.delcom.ifs25014springbootcashflow.framework.view;

import com.vaadin.flow.component.page.LoadingIndicatorConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/**
 * Mengaktifkan indikator loading Vaadin secara cepat untuk setiap aksi yang
 * menunggu respons server, dengan styling kustom di {@code styles.css}.
 */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            LoadingIndicatorConfiguration conf = uiEvent.getUI().getLoadingIndicatorConfiguration();
            conf.setApplyDefaultTheme(false);
            conf.setFirstDelay(80);
            conf.setSecondDelay(1200);
            conf.setThirdDelay(4000);
        });
    }
}

