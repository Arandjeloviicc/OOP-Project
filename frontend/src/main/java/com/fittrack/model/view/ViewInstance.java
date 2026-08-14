package com.fittrack.model.view;

import javafx.scene.Parent;

public record ViewInstance(
        Parent root,
        Object controller
) {
}
