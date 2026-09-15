package com.fittrack.controller.common;

import com.fittrack.ui.SceneManager;

public abstract class NavigableController extends BaseController {

    protected <T> T navigateTo(String fxml) {
        return SceneManager.switchTo(fxml);
    }
}