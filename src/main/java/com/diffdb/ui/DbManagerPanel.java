package com.diffdb.ui;

import com.diffdb.model.ConnectionConfig;
import com.diffdb.service.ConnectionStorageService;
import com.diffdb.service.ConnectionYamlService;
import com.diffdb.service.CredentialService;
import com.diffdb.service.TableBrowserService;
import com.diffdb.service.YamlImportResult;
import com.diffdb.connection.ConnectionManager;
import com.diffdb.model.TableInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DbManagerPanel extends JPanel {

    private static final Logger LOG = Logger.getInstance(DbManagerPanel.class);
    private static final javax.swing.Icon CONNECTION_ICON =
            IconLoader.getIcon("/icons/database.svg", DbManagerPanel.class);

    private final Project project;
    private final Runnable onConnectionsChanged;
    private final ConnectionListModel listModel;
    private final JBList<ConnectionConfig> connectionList;

    public DbManagerPanel(Project project, Runnable onConnectionsChanged) {
        super(new BorderLayout());
        this.project = project;
        this.onConnectionsChanged = onConnectionsChanged;

        add(buildToolbar(), BorderLayout.NORTH);

        listModel = new ConnectionListModel();
        connectionList = new JBList<>(listModel);
        connectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        connectionList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JBLabel(value.getName(), CONNECTION_ICON, JLabel.LEFT);
            label.setOpaque(true);
            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }
            return label;
        });

        connectionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelected();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
        });

        add(new JBScrollPane(connectionList), BorderLayout.CENTER);
        refreshList();
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());

        JLabel titleLabel = new JBLabel("Connections");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        toolbar.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        JButton addBtn = new JButton("+ Add");
        addBtn.addActionListener(e -> onAdd());
        buttonPanel.add(addBtn);

        JButton importBtn = new JButton("Import");
        importBtn.addActionListener(e -> onImport());
        buttonPanel.add(importBtn);

        JButton exportBtn = new JButton("Export");
        exportBtn.addActionListener(e -> onExport());
        buttonPanel.add(exportBtn);

        toolbar.add(buttonPanel, BorderLayout.EAST);
        return toolbar;
    }

    private void showContextMenu(MouseEvent e) {
        int index = connectionList.locationToIndex(e.getPoint());
        if (index < 0) return;
        connectionList.setSelectedIndex(index);
        ConnectionConfig config = listModel.getElementAt(index);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Edit");
        editItem.addActionListener(ev -> editConnection(config));
        menu.add(editItem);

        JMenuItem tablesItem = new JMenuItem("Show Tables");
        tablesItem.addActionListener(ev -> showTables(config));
        menu.add(tablesItem);

        menu.addSeparator();

        JMenuItem deleteItem = new JMenuItem("Remove");
        deleteItem.addActionListener(ev -> deleteConnection(config));
        menu.add(deleteItem);

        menu.show(connectionList, e.getX(), e.getY());
    }

    private void onAdd() {
        ConnectionConfig config = new ConnectionConfig();
        ConnectionDialog dialog = new ConnectionDialog(project, config);
        if (dialog.showAndGet()) {
            ConnectionStorageService.getInstance().save(config);
            refreshList();
            onConnectionsChanged.run();
        }
    }

    private void editSelected() {
        ConnectionConfig config = connectionList.getSelectedValue();
        if (config != null) {
            editConnection(config);
        }
    }

    private void editConnection(ConnectionConfig config) {
        ConnectionConfig copy = config.copy();
        ConnectionDialog dialog = new ConnectionDialog(project, copy);
        if (dialog.showAndGet()) {
            ConnectionStorageService.getInstance().save(copy);
            refreshList();
            onConnectionsChanged.run();
        }
    }

    private void deleteConnection(ConnectionConfig config) {
        int answer = Messages.showYesNoDialog(project,
                "Delete connection \"" + config.getName() + "\"?", "DiffDB", null);
        if (answer == Messages.YES) {
            ConnectionStorageService.getInstance().delete(config.getId());
            CredentialService.clear(config.getId());
            refreshList();
            onConnectionsChanged.run();
        }
    }

    private void showTables(ConnectionConfig config) {
        new Task.Backgroundable(project, "Loading tables for " + config.getName(), true) {
            private List<TableInfo> tables;
            private String errorMessage;

            @Override
            public void run(ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                try {
                    String dbPwd = CredentialService.getDbPassword(config.getId());
                    String sshSecret = CredentialService.getSshSecret(config.getId());
                    try (var mc = ConnectionManager.open(config, dbPwd, sshSecret)) {
                        tables = TableBrowserService.listTables(mc.getConnection(), config.getSchema());
                    }
                } catch (Exception e) {
                    LOG.warn("Failed to list tables for " + config.getName(), e);
                    errorMessage = e.getMessage() == null ? e.toString() : e.getMessage();
                }
            }

            @Override
            public void onFinished() {
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (errorMessage != null) {
                        Messages.showErrorDialog(project,
                                "Failed to load tables:\n" + errorMessage, "DiffDB");
                    } else {
                        new ShowTablesDialog(project, config.getName(), tables).show();
                    }
                });
            }
        }.queue();
    }

    private void onImport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("YAML files", "yaml", "yml"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String yamlText;
        try {
            yamlText = Files.readString(chooser.getSelectedFile().toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Messages.showErrorDialog(project, "Failed to read file:\n" + e.getMessage(), "DiffDB");
            return;
        }

        YamlImportResult result = ConnectionYamlService.importConnections(yamlText);
        LOG.info("YAML import parsed " + result.getConnections().size() + " connections, "
                + result.getDbPasswords().size() + " db passwords, "
                + result.getSshSecrets().size() + " ssh secrets");
        if (!result.getWarnings().isEmpty()) {
            String warningText = String.join("\n", result.getWarnings());
            int proceed = Messages.showYesNoDialog(project,
                    warningText + "\n\nProceed with importing " + result.getConnections().size() + " connection(s)?",
                    "Import Warnings", null);
            if (proceed != Messages.YES) {
                return;
            }
        }

        ConnectionStorageService storage = ConnectionStorageService.getInstance();
        int passwordCount = 0;
        for (ConnectionConfig config : result.getConnections()) {
            String originalName = config.getName();
            if (storage.findByName(config.getName()) != null) {
                config.setName(config.getName() + " (imported)");
            }
            storage.save(config);

            String dbPassword = result.getDbPasswords().get(originalName);
            if (dbPassword != null && !dbPassword.isEmpty()) {
                LOG.info("Importing password for '" + originalName + "' (id=" + config.getId() + ")");
                CredentialService.setDbPassword(config.getId(), dbPassword);
                passwordCount++;
            } else {
                LOG.info("No password found in YAML for '" + originalName + "'");
            }
            String sshSecret = result.getSshSecrets().get(originalName);
            if (sshSecret != null && !sshSecret.isEmpty()) {
                CredentialService.setSshSecret(config.getId(), sshSecret);
            }
        }

        String message = "Imported " + result.getConnections().size() + " connection(s).";
        if (passwordCount > 0) {
            message += "\n" + passwordCount + " password(s) imported.";
        } else {
            message += "\nPlease edit connections to set passwords if needed.";
        }
        Messages.showInfoMessage(project, message, "DiffDB");
        refreshList();
        onConnectionsChanged.run();
    }

    private void onExport() {
        List<ConnectionConfig> connections = ConnectionStorageService.getInstance().getConnections();
        if (connections.isEmpty()) {
            Messages.showInfoMessage(project, "No connections to export.", "DiffDB");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("diffdb-connections.yaml"));
        chooser.setFileFilter(new FileNameExtensionFilter("YAML files", "yaml", "yml"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path path = chooser.getSelectedFile().toPath();
        String yaml = ConnectionYamlService.exportConnections(connections);
        try {
            Files.writeString(path, yaml, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Messages.showErrorDialog(project, "Failed to write file:\n" + e.getMessage(), "DiffDB");
            return;
        }

        Messages.showInfoMessage(project,
                "Exported " + connections.size() + " connection(s).\nPasswords are set to 'null' — edit the YAML to add them or set them after import.",
                "DiffDB");
    }

    public void refreshList() {
        listModel.refresh();
    }

    private static class ConnectionListModel extends AbstractListModel<ConnectionConfig> {
        private final List<ConnectionConfig> items = new ArrayList<>();

        void refresh() {
            items.clear();
            items.addAll(ConnectionStorageService.getInstance().getConnections());
            fireContentsChanged(this, 0, items.size());
        }

        @Override
        public int getSize() {
            return items.size();
        }

        @Override
        public ConnectionConfig getElementAt(int index) {
            return items.get(index);
        }
    }
}
