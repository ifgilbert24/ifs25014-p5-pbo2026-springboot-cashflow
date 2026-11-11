package org.delcom.ifs25014springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;

public final class GridStyles {

    private GridStyles() {
    }

    public static void apply(Grid<?> grid) {
        grid.addClassName("app-grid");
    }
}

