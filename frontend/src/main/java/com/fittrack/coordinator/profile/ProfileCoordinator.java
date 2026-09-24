package com.fittrack.coordinator.profile;

import com.fittrack.config.AppConstants;
import com.fittrack.ui.popup.PopupShellController;
import com.fittrack.controller.profile.editor.ProfileNutritionGoalEditorController;
import com.fittrack.controller.profile.editor.ProfilePersonalInfoEditorController;
import com.fittrack.dto.profile.editor.NutritionGoalUpdateRequest;
import com.fittrack.dto.profile.editor.PersonalInfoUpdateRequest;
import com.fittrack.model.profile.ProfileData;
import com.fittrack.ui.loader.FxmlComponentLoader;
import com.fittrack.ui.loader.LoadedComponent;
import com.fittrack.ui.overlay.OverlayManager;

import java.util.function.Consumer;

public class ProfileCoordinator {

    // Active Editors
    private ProfilePersonalInfoEditorController activePersonalInfoEditor;
    private ProfileNutritionGoalEditorController activeNutritionGoalEditor;

    // ── Personal Info ─────────────────────────────────────────────────
    public void openPersonalInfoEditor(ProfileData profile, Consumer<PersonalInfoUpdateRequest> onSave) {
        LoadedComponent<ProfilePersonalInfoEditorController> editor = FxmlComponentLoader.load(AppConstants.Popups.PROFILE_PERSONAL_INFO_EDITOR);

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

        PopupShellController shell =
                OverlayManager.showInPopup(
                        editor.root(),
                        () -> activePersonalInfoEditor = null
                );

        controller.setOnNarrowLayoutChanged(
                shell::setContentTopAlignmentRequested
        );

        controller.initializeResponsiveLayout(
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

    public void showPersonalInfoSaveError(String message) {
        if (activePersonalInfoEditor != null) {
            activePersonalInfoEditor.showActionError(message);
        }
    }

    // ── Nutrition Goal ─────────────────────────────────────────────────
    public void openNutritionGoalEditor(ProfileData profile, Consumer<NutritionGoalUpdateRequest> onSave) {
        LoadedComponent<ProfileNutritionGoalEditorController> editor = FxmlComponentLoader.load(AppConstants.Popups.PROFILE_NUTRITION_GOAL_EDITOR);

        ProfileNutritionGoalEditorController controller = editor.controller();

        activeNutritionGoalEditor = controller;

        controller.setData(profile);

        controller.setOnCancelAction(
                this::closeNutritionGoalEditor
        );

        controller.setOnSaveAction(request -> {
            if (onSave != null) {
                controller.setSaving(true);
                onSave.accept(request);
            }
        });

        PopupShellController shell =
                OverlayManager.showInPopup(
                        editor.root(),
                        () -> activeNutritionGoalEditor = null
                );

        controller.setOnNarrowLayoutChanged(
                shell::setContentTopAlignmentRequested
        );

        controller.initializeResponsiveLayout(
                shell.getRoot()
        );
    }

    public void setNutritionGoalSaving(boolean saving) {
        if (activeNutritionGoalEditor != null) {
            activeNutritionGoalEditor.setSaving(saving);
        }
    }

    public void closeNutritionGoalEditor() {
        activeNutritionGoalEditor = null;
        OverlayManager.close();
    }

    public void showNutritionGoalSaveError(String message) {
        if (activeNutritionGoalEditor != null) {
            activeNutritionGoalEditor.showActionError(message);
        }
    }
}