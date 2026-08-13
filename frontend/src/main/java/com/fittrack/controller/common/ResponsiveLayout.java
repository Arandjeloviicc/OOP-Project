package com.fittrack.controller.common;

import javafx.application.Platform;
import javafx.scene.layout.Region;

public interface ResponsiveLayout {
    void updateLayout(boolean narrow);

    default void initializeResponsiveLayout(Region observedRegion, double breakpoint) {
        observedRegion.widthProperty().addListener(
                (observable, oldWidth, newWidth) -> {
                    double width = newWidth.doubleValue();
                    if (width > 50) {
                        updateLayout(width < breakpoint);
                    }
                }
        );

        Platform.runLater(() ->
                Platform.runLater(() -> {
                    double width = observedRegion.getWidth();
                    if (width > 50) {
                        updateLayout(width < breakpoint);
                    }
                })
        );
    }
}
