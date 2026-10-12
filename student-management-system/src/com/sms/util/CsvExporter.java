package com.sms.util;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CsvExporter {

    public static void exportTableToCsv(JTable table, File file) throws IOException {
        TableModel model = table.getModel();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            // Write Column Headers
            for (int col = 0; col < model.getColumnCount(); col++) {
                bw.write(escapeCsv(model.getColumnName(col)));
                if (col < model.getColumnCount() - 1) {
                    bw.write(",");
                }
            }
            bw.newLine();

            // Write Row Data
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object val = model.getValueAt(row, col);
                    bw.write(escapeCsv(val != null ? val.toString() : ""));
                    if (col < model.getColumnCount() - 1) {
                        bw.write(",");
                    }
                }
                bw.newLine();
            }
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "\"\"";
        String clean = value.replace("\"", "\"\"");
        if (clean.contains(",") || clean.contains("\n") || clean.contains("\r") || clean.contains("\"")) {
            return "\"" + clean + "\"";
        }
        return "\"" + clean + "\"";
    }
}
