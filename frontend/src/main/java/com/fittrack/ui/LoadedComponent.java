package com.fittrack.ui;

import javafx.scene.Parent;

public record LoadedComponent<T>(
        Parent root,
        T controller
) {
}
