package com.fittrack.ui.form;

import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class AppDatePickerConfigurer {

    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("d.M.uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.uuuu");

    protected AppDatePickerConfigurer() {}

    public static void configure(DatePicker picker) {
        configure(picker, null);
    }

    protected static void configure(DatePicker datePicker, LocalDate latestAllowedDate) {
        datePicker.setDayCellFactory(p -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                setDisable(
                        empty ||
                        (
                            latestAllowedDate != null &&
                                    date.isAfter(latestAllowedDate)
                        )
                );
            }
        });

        datePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null
                        ? ""
                        : date.format(DISPLAY_FORMATTER);
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) {
                    return null;
                }

                try {
                    return LocalDate.parse(
                            text.trim(),
                            INPUT_FORMATTER
                    );
                } catch (DateTimeParseException _) {
                    return null;
                }
            }
        });

        datePicker.setShowWeekNumbers(false);

        datePicker.addEventFilter(
                KeyEvent.KEY_PRESSED,
                event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        System.out.println("DATE PICKER ENTER");

                        commitEditorValue(datePicker);

                        System.out.println("AFTER COMMIT: " + datePicker.getValue());

                        event.consume();
                    }
                }
        );

        datePicker.addEventFilter(
                KeyEvent.KEY_RELEASED,
                event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        event.consume();
                    }
                }
        );
    }

    public static void commitEditorValue(DatePicker datePicker) {
        String text = datePicker.getEditor().getText();

        if (text == null || text.isBlank()) {
            datePicker.setValue(null);
            return;
        }

        try {
            LocalDate date = LocalDate.parse(
                    text.trim(),
                    INPUT_FORMATTER
            );

            datePicker.setValue(date);

        } catch (DateTimeParseException _) {
            datePicker.setValue(null);
        }
    }

    public static DateTimeFormatter inputFormatter() {
        return INPUT_FORMATTER;
    }

    public static DateTimeFormatter displayFormatter() {
        return DISPLAY_FORMATTER;
    }
}