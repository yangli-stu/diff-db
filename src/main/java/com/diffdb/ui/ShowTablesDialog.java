package com.diffdb.ui;

import com.diffdb.model.TableInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.util.List;

public class ShowTablesDialog extends DialogWrapper {

    private final String connectionName;
    private final List<TableInfo> tables;

    public ShowTablesDialog(Project project, String connectionName, List<TableInfo> tables) {
        super(project);
        this.connectionName = connectionName;
        this.tables = tables;
        setTitle("Tables: " + connectionName);
        setModal(false);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JBLabel(tables.size() + " tables found"), BorderLayout.NORTH);

        JTable table = new JTable(new TableModel());
        table.setFillsViewportHeight(true);
        panel.add(new JBScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    @Override
    protected Action[] createActions() {
        setOKButtonText("Close");
        return new Action[]{getOKAction()};
    }

    private class TableModel extends AbstractTableModel {
        private final String[] columns = {"Table", "Columns"};

        @Override
        public int getRowCount() {
            return tables.size();
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
            TableInfo t = tables.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> t.getTableName();
                case 1 -> t.getColumnCount();
                default -> null;
            };
        }
    }
}
