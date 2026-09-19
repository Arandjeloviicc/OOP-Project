package com.fittrack.ui.overlay;

import com.fittrack.config.AppConstants;
import com.fittrack.controller.popup.PopupShellController;
import com.fittrack.ui.loader.FxmlComponentLoader;
import com.fittrack.ui.loader.LoadedComponent;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public final class OverlayManager {

    private static StackPane overlayContainer;

    private static Node activeModal;

    private static Runnable onCloseAction = () -> {};

    private OverlayManager() {}

    public static void initialize(StackPane container) {
        overlayContainer = container;
    }

    // ── Show ───────────────────────────────────────────────────────
    public static void show(Node content) {
        show(content, null);
    }

    public static void show(Node content, Runnable onClose) {
        ensureInitialized();

        onCloseAction = onClose != null ? onClose : () -> {};

        replace(content);
    }

    // ── Popup ──────────────────────────────────────────────────────
    public static PopupShellController showInPopup(Node content) {
        return showInPopup(content, null);
    }

    public static PopupShellController showInPopup(Node content, Runnable onClose) {
        ensureInitialized();

        PopupShellController shell = createPopupShell(content);

        show(shell.getRoot(), onClose);

        return shell;
    }

    public static PopupShellController replaceInPopup(Node content) {
        ensureInitialized();

        PopupShellController shell = createPopupShell(content);

        replace(shell.getRoot());

        return shell;
    }

    private static PopupShellController createPopupShell(Node content) {
        LoadedComponent<PopupShellController> shell = FxmlComponentLoader.load(AppConstants.Components.POPUP_SHELL);

        shell.controller().setContent(content);

        return shell.controller();
    }

    // ── Replace ────────────────────────────────────────────────────
    private static void replace(Node content) {
        activeModal = null;

        overlayContainer.getChildren().setAll(content);
        overlayContainer.setManaged(true);
        overlayContainer.setVisible(true);
    }

    // ── Close ──────────────────────────────────────────────────────
    public static void close() {
        if (overlayContainer == null) {
            return;
        }

        activeModal = null;

        overlayContainer.getChildren().clear();
        overlayContainer.setVisible(false);
        overlayContainer.setManaged(false);

        Runnable action = onCloseAction;
        onCloseAction = () -> {};

        action.run();
    }

    // ── Modal ──────────────────────────────────────────────────────
    public static void showModal(Node content) {
        ensureInitialized();

        closeModal();

        activeModal = content;
        overlayContainer.getChildren().add(content);
    }

    public static void closeModal() {
        ensureInitialized();

        if (activeModal == null) {
            return;
        }

        overlayContainer.getChildren().remove(activeModal);
        activeModal = null;
    }

    // ── Helpers ────────────────────────────────────────────────────
    private static void ensureInitialized() {
        if (overlayContainer == null) {
            throw new IllegalStateException(
                    "OverlayManager is not initialized."
            );
        }
    }
}