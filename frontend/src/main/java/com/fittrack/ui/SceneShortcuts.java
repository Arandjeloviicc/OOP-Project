package com.fittrack.ui;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class SceneShortcuts {

    private Runnable onEscape;
    private Runnable onEnter;

    private Scene currentScene;

    private final EventHandler<KeyEvent> keyHandler = event -> {
        if (event.getCode() == KeyCode.ESCAPE && onEscape != null) {
            onEscape.run();
            event.consume();
            return;
        }

        if (event.getCode() == KeyCode.ENTER && onEnter != null) {
            onEnter.run();
            event.consume();
        }
    };

    private SceneShortcuts(Node owner) {

        owner.sceneProperty().addListener(
                (observable, oldScene, newScene) -> {
                    detach(oldScene);
                    attach(newScene);
                }
        );

        attach(owner.getScene());
    }

    public static SceneShortcuts forNode(Node owner) {
        return new SceneShortcuts(owner);
    }

    public SceneShortcuts onEscape(Runnable action) {
        this.onEscape = action;
        return this;
    }

    public SceneShortcuts onEnter(Runnable action) {
        this.onEnter = action;
        return this;
    }

    private void attach(Scene scene) {
        if (scene == null || scene == currentScene) {
            return;
        }

        currentScene = scene;

        scene.addEventFilter(
                KeyEvent.KEY_PRESSED,
                keyHandler
        );
    }

    private void detach(Scene scene) {
        if (scene == null) {
            return;
        }

        scene.removeEventFilter(
                KeyEvent.KEY_PRESSED,
                keyHandler
        );

        if (scene == currentScene) {
            currentScene = null;
        }
    }
}