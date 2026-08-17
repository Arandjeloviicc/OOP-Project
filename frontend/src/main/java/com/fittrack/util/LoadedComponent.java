package com.fittrack.util;

import javafx.scene.Parent;

public record LoadedComponent<T>(
        Parent root,
        T controller
) {
}
