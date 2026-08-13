package com.fittrack.controller.common;

import com.fittrack.util.SceneManager;
import org.slf4j.Logger;

public abstract class BaseController {

    protected abstract Logger getLogger();

    protected <T> T navigateTo(String fxml) {
        return SceneManager.switchTo(fxml);
    }
}