package com.fittrack.ui;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.regex.Pattern;

public class TextFieldValidators {

    private static final Pattern DECIMAL_PATTERN = Pattern.compile("\\d*([,.]\\d*)?");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("\\d*");

    private TextFieldValidators() {}

    public static void applyDecimalFilter(TextField textField) {
        textField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            return DECIMAL_PATTERN.matcher(newText).matches() ? change : null;
        }));
    }

    public static void applyIntegerFilter(TextField textField) {
        textField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            return INTEGER_PATTERN.matcher(newText).matches() ? change : null;
        }));
    }
}
