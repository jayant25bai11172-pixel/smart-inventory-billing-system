package com.retail.inventory.util;

import java.util.Scanner;

/**
 * Utility class providing formatted CLI banners, colors, and robust user input reading.
 */
public class ConsoleUtils {
    // ANSI color escape sequences
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";

    public static void printHeader(String title) {
        System.out.println(CYAN + "\n================================================================================" + RESET);
        System.out.println(BOLD + CYAN + "  " + title.toUpperCase() + RESET);
        System.out.println(CYAN + "================================================================================" + RESET);
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + "[SUCCESS] " + message + RESET);
    }

    public static void printWarning(String message) {
        System.out.println(YELLOW + "[WARNING] " + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + "[ERROR] " + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(BLUE + "[INFO] " + message + RESET);
    }

    public static String formatCurrency(double amount) {
        return String.format("Rs. %.2f", amount);
    }

    public static String readString(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) {
                    return val;
                }
                printWarning(String.format("Please enter a number between %d and %d.", min, max));
            } catch (NumberFormatException e) {
                printWarning("Invalid integer input. Please try again.");
            }
        }
    }

    public static double readDouble(Scanner scanner, String prompt, double min) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double val = Double.parseDouble(input);
                if (val >= min) {
                    return val;
                }
                printWarning(String.format("Value must be at least %.2f.", min));
            } catch (NumberFormatException e) {
                printWarning("Invalid number format. Please try again.");
            }
        }
    }
}
