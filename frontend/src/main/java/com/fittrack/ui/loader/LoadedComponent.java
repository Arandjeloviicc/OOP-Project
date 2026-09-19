package com.fittrack.ui.loader;

import javafx.scene.Parent;

public record LoadedComponent<T>(
        Parent root,
        T controller
) {}