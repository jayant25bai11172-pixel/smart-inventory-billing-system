package com.retail.inventory.gui;

import javafx.application.Application;

/**
 * Clean launcher entry point that avoids modular JavaFX runtime checks
 * when executed from standard classpath.
 */
public class GuiLauncher {
    public static void main(String[] args) {
        Application.launch(InventoryApp.class, args);
    }
}
