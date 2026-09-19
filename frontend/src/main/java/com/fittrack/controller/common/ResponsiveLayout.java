package com.fittrack.controller.common;

import javafx.scene.layout.Region;

public interface ResponsiveLayout {

    // ── Width ─────────────────────────────────────────────────
    void updateWidthLayout(boolean narrow);

    default void initializeResponsiveWidthLayout(Region observedRegion, double breakpoint) {
        observedRegion.widthProperty().addListener(
                (observable, oldWidth, newWidth) -> {
                    double width = newWidth.doubleValue();

                    if (width > 50) {
                        updateWidthLayout(width < breakpoint);
                    }
                }
        );

        double width = observedRegion.getWidth();

        if (width > 50) {
            updateWidthLayout(width < breakpoint);
        }
    }

    // ── Height ─────────────────────────────────────────────────
    default void updateHeightLayout(boolean shortLayout) {}

    default void initializeResponsiveHeightLayout(Region observedRegion, double breakpoint) {
        observedRegion.heightProperty().addListener(
                (observable, oldHeight, newHeight) -> {
                    double height = newHeight.doubleValue();

                    if (height > 50) {
                        updateHeightLayout(height < breakpoint);
                    }
                }
        );

        double height = observedRegion.getHeight();

        if (height > 50) {
            updateHeightLayout(height < breakpoint);
        }
    }
}
