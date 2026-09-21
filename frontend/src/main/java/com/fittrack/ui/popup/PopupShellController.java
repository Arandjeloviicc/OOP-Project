package com.fittrack.ui.popup;

import com.fittrack.controller.common.ResponsiveLayout;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupShellController implements Initializable, ResponsiveLayout {

    // ── Responsive Configuration ────────────────────────────────────
    private static final double NARROW_BREAKPOINT = 760;
    private static final double VERTICAL_SHELL_MARGIN = 48;
    private static final double SHORT_TRIGGER_MARGIN = 150;

    // ── Pseudo Classes ──────────────────────────────────────────────
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");
    private static final PseudoClass SHORT = PseudoClass.getPseudoClass("short");
    private static final PseudoClass SCROLLING = PseudoClass.getPseudoClass("scrolling");

    // ── Popup Configuration ─────────────────────────────────────────
    private PopupAlignment narrowAlignment = PopupAlignment.CENTER;
    private PopupOverflow overflow = PopupOverflow.SHELL_SCROLL;

    private boolean fillHeightOnNarrow;
    private boolean contentRequestsTopAlignment;

    // ── Responsive State ────────────────────────────────────────────
    private boolean narrow;
    private boolean shortLayout;
    private boolean scrolling;

    // Responsive state for Main-View
    private boolean responsiveNarrow;
    private boolean forceNarrow;

    // ── Natural Content Size ────────────────────────────────────────
    private final DoubleProperty naturalContentHeight = new SimpleDoubleProperty(0);

    private Node content;
    private final InvalidationListener contentSizeListener = observable -> updateNaturalContentHeight();

    private BooleanBinding shortBinding;

    // ── FXML ────────────────────────────────────────────────────────
    @FXML private StackPane rootLayout;
    @FXML private VBox popupHost;
    @FXML private StackPane dialogContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private StackPane contentHost;

    // ── Initialize ─────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize Responsive
        initializeResponsiveWidthLayout(rootLayout, NARROW_BREAKPOINT);

        initializeShortBinding();

        // Add listeners
        addListeners();

        updateOverflow();
    }

    private void initializeShortBinding() {
        shortBinding = Bindings.createBooleanBinding(
                () -> {
                    double naturalHeight = naturalContentHeight.get();

                    double rootHeight = rootLayout.getHeight();

                    return naturalHeight > 0
                            && rootHeight > 0
                            && naturalHeight
                            > rootHeight - SHORT_TRIGGER_MARGIN;
                },
                naturalContentHeight,
                rootLayout.heightProperty()
        );

        shortBinding.addListener(
                (observable, oldValue, newValue) -> {
                    shortLayout = newValue;

                    rootLayout.pseudoClassStateChanged(SHORT, newValue);

                    updateAlignment();
                }
        );
    }

    private void addListeners() {
        scrollPane.viewportBoundsProperty().addListener(
                (observable, oldBounds, newBounds) ->
                        updateScrollingState()
        );

        contentHost.layoutBoundsProperty().addListener(
                (observable, oldBounds, newBounds) -> {
                    updateScrollingState();
                }
        );
    }

    // ── Responsive Width ────────────────────────────────────────────
    @Override
    public void updateWidthLayout(boolean narrow) {
        responsiveNarrow = narrow;
        updateNarrowState();
    }

    private void updateNarrowState() {
        narrow = forceNarrow || responsiveNarrow;

        rootLayout.pseudoClassStateChanged(NARROW, narrow);

        dialogContainer.setMaxWidth(
                narrow
                        ? Double.MAX_VALUE
                        : Region.USE_PREF_SIZE
        );

        updateVerticalSizing();
        updateAlignment();
    }

    // ── Sizing ─────────────────────────────────────────────────────
    private void updateVerticalSizing() {
        boolean fillHeight =
                narrow && fillHeightOnNarrow;

        VBox.setVgrow(
                dialogContainer,
                fillHeight
                        ? Priority.ALWAYS
                        : Priority.NEVER
        );

        dialogContainer.maxHeightProperty().unbind();

        if (fillHeight) {
            dialogContainer.setMaxHeight(Double.MAX_VALUE);
        } else {
            dialogContainer.maxHeightProperty().bind(
                    rootLayout.heightProperty()
                            .subtract(VERTICAL_SHELL_MARGIN)
            );
        }
    }

    // ── Alignment ──────────────────────────────────────────────────
    private void updateAlignment() {
        boolean topAlign =
                contentRequestsTopAlignment
                        || (narrow
                        && narrowAlignment == PopupAlignment.TOP_CENTER)
                        || (narrow && shortLayout);

        Pos alignment =
                topAlign
                        ? Pos.TOP_CENTER
                        : Pos.CENTER;

        popupHost.setAlignment(alignment);
        contentHost.setAlignment(alignment);
    }

    public void setNarrowAlignment(PopupAlignment alignment) {
        this.narrowAlignment = alignment;
        updateAlignment();
    }

    public void setContentTopAlignmentRequested(boolean requested) {
        if (contentRequestsTopAlignment == requested) {
            return;
        }

        contentRequestsTopAlignment = requested;
        updateAlignment();
    }

    public void setFillHeightOnNarrow(boolean fillHeightOnNarrow) {
        this.fillHeightOnNarrow = fillHeightOnNarrow;
        updateVerticalSizing();
    }

    // ── Overflow ───────────────────────────────────────────────────
    public void setOverflow(PopupOverflow overflow) {
        if (this.overflow == overflow) {
            return;
        }

        this.overflow = overflow;
        updateOverflow();
    }

    private void updateOverflow() {
        boolean shellScroll =
                overflow == PopupOverflow.SHELL_SCROLL;

        scrollPane.setVbarPolicy(
                shellScroll
                        ? ScrollPane.ScrollBarPolicy.AS_NEEDED
                        : ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.setFitToHeight(!shellScroll);

        if (!shellScroll) {
            setScrolling(false);
        }

        updateVerticalSizing();
    }

    // ── Scrolling State ─────────────────────────────────────────────
    private void updateScrollingState() {
        if (overflow != PopupOverflow.SHELL_SCROLL) {
            setScrolling(false);
            return;
        }

        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double contentHeight = contentHost.getLayoutBounds().getHeight();

        if (viewportHeight <= 0) {
            return;
        }

        setScrolling(contentHeight > viewportHeight + 1);
    }

    private void setScrolling(boolean scrolling) {
        if (this.scrolling == scrolling) {
            return;
        }

        this.scrolling = scrolling;

        rootLayout.pseudoClassStateChanged(SCROLLING, scrolling);
    }

    // ── Force Narrow ────────────────────────────────────────────────────
    public void setForceNarrow(boolean forceNarrow) {
        this.forceNarrow = forceNarrow;
        updateNarrowState();
    }

    // ── Content ────────────────────────────────────────────────────
    private void updateNaturalContentHeight() {
        if (content == null) {
            naturalContentHeight.set(0);
            return;
        }

        content.applyCss();

        naturalContentHeight.set(
                content.prefHeight(-1)
        );
    }

    public void setContent(Node content) {
        if (this.content != null) {
            this.content.layoutBoundsProperty().removeListener(contentSizeListener);
        }

        this.content = content;

        contentHost.getChildren().setAll(content);

        content.layoutBoundsProperty().addListener(contentSizeListener);

        Platform.runLater(
                this::updateNaturalContentHeight
        );
    }

    // ── Getters ──────────────────────────────────────────────────
    public StackPane getRoot() {
        return rootLayout;
    }
}