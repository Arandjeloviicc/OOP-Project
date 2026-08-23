package com.fittrack.ui;

import javafx.animation.PauseTransition;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class QuickAddFeedback {

    private static final PseudoClass PENDING = PseudoClass.getPseudoClass("pending");
    private static final PseudoClass SUCCESS = PseudoClass.getPseudoClass("success");

    private final Button button;
    private final PauseTransition successDelay =
            new PauseTransition(Duration.seconds(1));

    public QuickAddFeedback(Button button) {
        this.button = button;
    }

    public void start() {
        successDelay.stop();

        button.setText("…");
        button.pseudoClassStateChanged(SUCCESS, false);
        button.pseudoClassStateChanged(PENDING, true);

        button.setDisable(true);
    }

    public void success() {
        successDelay.stop();

        button.setText("✓");
        button.pseudoClassStateChanged(PENDING, false);
        button.pseudoClassStateChanged(SUCCESS, true);

        successDelay.setOnFinished(event -> reset());
        successDelay.playFromStart();
    }

    public void reset() {
        successDelay.stop();

        button.setText("+");
        button.pseudoClassStateChanged(PENDING, false);
        button.pseudoClassStateChanged(SUCCESS, false);

        button.setDisable(false);
    }
}