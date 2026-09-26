package com.fittrack.ui.form;

import javafx.animation.PauseTransition;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.util.Locale;
import java.util.function.Consumer;

public final class AppSearchFieldConfigurer {

    private static final Duration DEFAULT_DEBOUNCE = Duration.millis(300);

    private AppSearchFieldConfigurer() {}

    @FunctionalInterface
    public interface SearchFieldHandle {
        void stop();
    }

    public static SearchFieldHandle configure(TextField searchField, Consumer<String> onSearch) {
        PauseTransition debounce = new PauseTransition(DEFAULT_DEBOUNCE);

        debounce.setOnFinished(
                event -> onSearch.accept(searchField.getText())
        );

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    debounce.stop();
                    debounce.playFromStart();
                }
        );

        return debounce::stop;
    }

    public static String normalize(String search) {
        return search == null
                ? ""
                : search.trim().toLowerCase(Locale.ROOT);
    }
}
