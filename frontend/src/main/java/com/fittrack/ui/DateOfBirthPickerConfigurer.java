package com.fittrack.ui;

import com.fittrack.config.AppConstants;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateOfBirthPickerConfigurer {

    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("d.M.uuuu");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.uuuu");

    private DateOfBirthPickerConfigurer() {}

    public static void configure(DatePicker picker) {
        LocalDate latestAllowedDateOfBirth = LocalDate.now().minusYears(AppConstants.Validation.MIN_AGE);

        picker.setDayCellFactory(p -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(latestAllowedDateOfBirth));
            }
        });

        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : date.format(DISPLAY_FORMATTER);
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) {
                    return picker.getValue();
                }
                try {
                    return LocalDate.parse(text.trim(), INPUT_FORMATTER);
                } catch (DateTimeParseException _) {
                    return picker.getValue();
                }
            }
        });

        picker.setShowWeekNumbers(false);
    }

    public static DateTimeFormatter inputFormatter() {
        return INPUT_FORMATTER;
    }

    public static DateTimeFormatter displayFormatter() {
        return DISPLAY_FORMATTER;
    }
}