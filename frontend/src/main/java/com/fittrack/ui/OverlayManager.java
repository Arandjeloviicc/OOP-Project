package com.fittrack.ui;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public final class OverlayManager {

    private static StackPane overlayContainer;

    private static Runnable onCloseAction = () -> {};

    private OverlayManager() {}

    public static void initialize(StackPane container) {
        overlayContainer = container;
    }

    public static void show(Node content) {
        show(content, null);
    }

    public static void show(Node content, Runnable onClose) {
        if (overlayContainer == null) {
            throw new IllegalStateException("OverlayManager is not initialized.");
        }

        onCloseAction = onClose != null ? onClose : () -> {};

        overlayContainer.getChildren().setAll(content);
        overlayContainer.setManaged(true);
        overlayContainer.setVisible(true);
    }

    public static void close() {
        if (overlayContainer == null) {
            return;
        }

        overlayContainer.getChildren().clear();
        overlayContainer.setVisible(false);
        overlayContainer.setManaged(false);

        Runnable action = onCloseAction;
        onCloseAction = () -> {};

        action.run();
    }
}