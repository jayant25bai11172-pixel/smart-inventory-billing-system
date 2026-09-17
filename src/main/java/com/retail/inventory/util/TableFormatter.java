package com.retail.inventory.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to render structured ASCII tables in terminal consoles.
 */
public class TableFormatter {
    private final List<String> headers;
    private final List<List<String>> rows;
    private final List<Integer> colWidths;

    public TableFormatter(String... headers) {
        this.headers = new ArrayList<>();
        this.rows = new ArrayList<>();
        this.colWidths = new ArrayList<>();

        for (String h : headers) {
            this.headers.add(h);
            this.colWidths.add(h.length());
        }
    }

    public TableFormatter addRow(Object... cells) {
        List<String> row = new ArrayList<>();
        for (int i = 0; i < cells.length; i++) {
            String val = cells[i] == null ? "" : cells[i].toString();
            row.add(val);
            if (i < colWidths.size()) {
                colWidths.set(i, Math.max(colWidths.get(i), val.length()));
            } else {
                colWidths.add(val.length());
            }
        }
        rows.add(row);
        return this;
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        String separator = buildSeparator();

        sb.append(separator).append("\n");

        // Header
        sb.append("|");
        for (int i = 0; i < headers.size(); i++) {
            sb.append(" ").append(padRight(headers.get(i), colWidths.get(i))).append(" |");
        }
        sb.append("\n");
        sb.append(separator).append("\n");

        // Rows
        for (List<String> row : rows) {
            sb.append("|");
            for (int i = 0; i < headers.size(); i++) {
                String val = i < row.size() ? row.get(i) : "";
                sb.append(" ").append(padRight(val, colWidths.get(i))).append(" |");
            }
            sb.append("\n");
        }

        sb.append(separator);
        return sb.toString();
    }

    private String buildSeparator() {
        StringBuilder sb = new StringBuilder("+");
        for (int w : colWidths) {
            for (int i = 0; i < w + 2; i++) {
                sb.append("-");
            }
            sb.append("+");
        }
        return sb.toString();
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }
}
