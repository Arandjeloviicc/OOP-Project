package com.fittrack.ui;

import com.fittrack.model.profile.Gender;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public final class GenderToggleConfigurer {

    private GenderToggleConfigurer() {}

    public static void configure(ToggleGroup group, ToggleButton maleButton, ToggleButton femaleButton) {
        maleButton.setToggleGroup(group);
        femaleButton.setToggleGroup(group);

        maleButton.setUserData(Gender.MALE);
        femaleButton.setUserData(Gender.FEMALE);

        group.selectedToggleProperty().addListener(
                (obs, oldToggle, newToggle) -> {
                    if (newToggle == null && oldToggle != null) {
                        group.selectToggle(oldToggle);
                    }
                }
        );
    }
}