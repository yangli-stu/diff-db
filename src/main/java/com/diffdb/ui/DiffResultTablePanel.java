package com.diffdb.ui;

import com.diffdb.diff.DiffCategory;
import com.diffdb.diff.DiffNode;
import com.diffdb.diff.SchemaDiffResult;
import com.intellij.ui.table.JBTable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

public class DiffResultTablePanel extends JPanel {

    private final DiffTableModel tableModel;
    private final JBTable table;

    public DiffResultTablePanel() {
        super(new BorderLayout());
        tableModel = new DiffTableModel();
        table = new JBTable(tableModel);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void showResult(SchemaDiffResult result) {
        tableModel.setData(result);
    }

    public void clear() {
        tableModel.clear();
    }

    private static class DiffTableModel extends AbstractTableModel {

        private final String[] columns = {"Object", "Type", "Change", "Detail"};
        private final List<Row> rows = new ArrayList<>();

        void setData(SchemaDiffResult result) {
            rows.clear();
            if (result != null && result.getRoots() != null) {
                for (DiffNode root : result.getRoots()) {
                    flatten(root);
                }
            }
            fireTableDataChanged();
        }

        void clear() {
            rows.clear();
            fireTableDataChanged();
        }

        private void flatten(DiffNode node) {
            if (node.getCategory() == DiffCategory.CONTAINER) {
                for (DiffNode child : node.getChildren()) {
                    flatten(child);
                }
                return;
            }

            rows.add(new Row(
                    node.getName(),
                    node.getObjectType(),
                    node.getCategory(),
                    node.getDetail()
            ));
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Row row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.object;
                case 1 -> row.type;
                case 2 -> changeLabel(row.category);
                case 3 -> row.detail;
                default -> null;
            };
        }

        private static String changeLabel(DiffCategory category) {
            return switch (category) {
                case MISSING -> "ADD";
                case UNEXPECTED -> "DROP";
                case CHANGED -> "MODIFY";
                default -> "";
            };
        }

        record Row(String object, String type, DiffCategory category, String detail) {}
    }
}
