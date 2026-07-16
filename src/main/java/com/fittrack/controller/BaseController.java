package com.fittrack.controller;

import com.fittrack.app.SceneManager;
import org.slf4j.Logger;

public abstract class BaseController {

    protected abstract Logger getLogger();

    protected <T> T navigateTo(String fxml) {
        return SceneManager.switchTo(fxml);
    }
}