package com.fittrack.coordinator.profile;

import com.fittrack.config.AppConstants;
import com.fittrack.controller.popup.PopupShellController;
import com.fittrack.controller.profile.editor.ProfilePersonalInfoEditorController;
import com.fittrack.dto.profile.editor.PersonalInfoUpdateRequest;
import com.fittrack.model.profile.ProfileData;
import com.fittrack.ui.loader.FxmlComponentLoader;
import com.fittrack.ui.loader.LoadedComponent;
import com.fittrack.ui.overlay.OverlayManager;

import java.util.function.Consumer;

public class ProfileCoordinator {

    // Active Editors
    private ProfilePersonalInfoEditorController activePersonalInfoEditor;

    // ── Personal Info ─────────────────────────────────────────────────
    public void openPersonalInfoEditor(ProfileData profile, Consumer<PersonalInfoUpdateRequest> onSave) {
        LoadedComponent<ProfilePersonalInfoEditorController> editor = FxmlComponentLoader.load(AppConstants.Components.PROFILE_PERSONAL_INFO_EDITOR);

        ProfilePersonalInfoEditorController controller = editor.controller();

        activePersonalInfoEditor = controller;

        controller.setData(profile);

        controller.setOnCancelAction(
                this::closePersonalInfoEditor
        );

        controller.setOnSaveAction(request -> {
            if (onSave != null) {
                controller.setSaving(true);
                onSave.accept(request);
            }
        });

        PopupShellController shell = OverlayManager.showInPopup(editor.root());

        controller.setOnNarrowLayoutChanged(
                shell::setContentTopAlignmentRequested
        );

        editor.controller().initializeResponsiveLayout(
                shell.getRoot()
        );
    }

    public void setPersonalInfoSaving(boolean saving) {
        if (activePersonalInfoEditor != null) {
            activePersonalInfoEditor.setSaving(saving);
        }
    }

    public void closePersonalInfoEditor() {
        activePersonalInfoEditor = null;
        OverlayManager.close();
    }

    // ── Next ─────────────────────────────────────────────────

}