package com.fittrack.controller.common;

import javafx.scene.Node;
import javafx.scene.control.Button;

public abstract class BaseController {

    private static final Object LOADING_STATE = new Object();

    private record ButtonState(
            String text,
            boolean disabled
    ) {}

    // ── Button Disable/Enable for Api call ────────────────────────────────────────────
    protected void setLoading(Button button, String loadingText) {
        if (!button.getProperties().containsKey(LOADING_STATE)) {
            button.getProperties().put(
                    LOADING_STATE,
                    new ButtonState(
                            button.getText(),
                            button.isDisable()
                    )
            );
        }

        if (button.isFocused() && button.getScene() != null) {
            button.getScene()
                    .getRoot()
                    .requestFocus();
        }

        button.setDisable(true);
        button.setText(loadingText);
    }

    protected void resetLoading(Button button) {
        Object value = button.getProperties().remove(LOADING_STATE);

        if (value instanceof ButtonState(String text, boolean disabled)) {
            button.setText(text);
            button.setDisable(disabled);
        }
    }

    protected boolean isLoading(Button button) {
        return button.getProperties().containsKey(LOADING_STATE);
    }

    // ── Visibility ────────────────────────────────────────────
    protected void setVisible(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }
}