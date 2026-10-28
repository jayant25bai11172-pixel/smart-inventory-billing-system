package com.retail.inventory.gui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Standardized dialog helper for the SmartRetail GUI.
 */
public class AlertHelper {

    public static void showInfo(Window owner, String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleDialog(alert);
        alert.showAndWait();
    }

    public static void showWarning(Window owner, String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(owner);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleDialog(alert);
        alert.showAndWait();
    }

    public static void showError(Window owner, String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(owner);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleDialog(alert);
        alert.showAndWait();
    }

    public static boolean showConfirmation(Window owner, String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(owner);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleDialog(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void styleDialog(Alert alert) {
        DialogPane pane = alert.getDialogPane();
        pane.getStyleClass().add("card");
        try {
            String cssPath = AlertHelper.class.getResource("/com/retail/inventory/gui/css/styles.css").toExternalForm();
            pane.getStylesheets().add(cssPath);
        } catch (Exception ignored) {
        }
    }
}
