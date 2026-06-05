package com.diffdb.ui;

import com.diffdb.diff.LiquibaseSchemaDiffService;
import com.diffdb.diff.SchemaDiffResult;
import com.diffdb.diff.SchemaDiffService;
import com.diffdb.migration.LiquibaseMigrationSqlGenerator;
import com.diffdb.migration.MigrationOptions;
import com.diffdb.migration.MigrationSqlGenerator;
import com.diffdb.model.ConnectionConfig;
import com.diffdb.service.ConnectionStorageService;
import com.diffdb.service.CredentialService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Main DiffDB tool window panel: pick source/target connections, run the diff,
 * and generate migration SQL.
 */
public class DiffDbPanel extends JPanel {

    private final Project project;

    private final com.intellij.openapi.ui.ComboBox<ConnectionConfig> sourceCombo =
            new com.intellij.openapi.ui.ComboBox<>();
    private final com.intellij.openapi.ui.ComboBox<ConnectionConfig> targetCombo =
            new com.intellij.openapi.ui.ComboBox<>();
    private final JBCheckBox includeDropsCheck = new JBCheckBox("Include DROP statements", false);

    private final DiffTreePanel treePanel = new DiffTreePanel();
    private final MigrationPreviewPanel previewPanel;

    private final SchemaDiffService diffService = new LiquibaseSchemaDiffService();
    private final MigrationSqlGenerator sqlGenerator = new LiquibaseMigrationSqlGenerator();

    private final AtomicReference<SchemaDiffResult> lastResult = new AtomicReference<>();

    public DiffDbPanel(Project project) {
        super(new BorderLayout());
        this.project = project;
        this.previewPanel = new MigrationPreviewPanel(project);

        add(buildToolbar(), BorderLayout.NORTH);

        JBSplitter splitter = new JBSplitter(true, 0.55f);
        splitter.setFirstComponent(treePanel);
        splitter.setSecondComponent(previewPanel);
        add(splitter, BorderLayout.CENTER);

        reloadConnections();
    }

    private JComponent buildToolbar() {
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.add(new JBLabel("Source:"));
        row1.add(sourceCombo);
        row1.add(new JBLabel("Target:"));
        row1.add(targetCombo);

        JButton newButton = new JButton("New");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        newButton.addActionListener(e -> onNew());
        editButton.addActionListener(e -> onEdit());
        deleteButton.addActionListener(e -> onDelete());
        row1.add(newButton);
        row1.add(editButton);
        row1.add(deleteButton);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton diffButton = new JButton("Compare");
        JButton sqlButton = new JButton("Generate Migration SQL");
        diffButton.addActionListener(e -> onCompare());
        sqlButton.addActionListener(e -> onGenerateSql());
        row2.add(diffButton);
        row2.add(sqlButton);
        row2.add(includeDropsCheck);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.add(row1, BorderLayout.NORTH);
        toolbar.add(row2, BorderLayout.SOUTH);
        return toolbar;
    }

    private void reloadConnections() {
        ConnectionConfig prevSource = (ConnectionConfig) sourceCombo.getSelectedItem();
        ConnectionConfig prevTarget = (ConnectionConfig) targetCombo.getSelectedItem();
        sourceCombo.removeAllItems();
        targetCombo.removeAllItems();
        for (ConnectionConfig c : ConnectionStorageService.getInstance().getConnections()) {
            sourceCombo.addItem(c);
            targetCombo.addItem(c);
        }
        if (prevSource != null) sourceCombo.setSelectedItem(prevSource);
        if (prevTarget != null) targetCombo.setSelectedItem(prevTarget);
    }

    private void onNew() {
        ConnectionConfig config = new ConnectionConfig();
        ConnectionDialog dialog = new ConnectionDialog(project, config);
        if (dialog.showAndGet()) {
            ConnectionStorageService.getInstance().save(config);
            reloadConnections();
            sourceCombo.setSelectedItem(config);
        }
    }

    private void onEdit() {
        ConnectionConfig selected = (ConnectionConfig) sourceCombo.getSelectedItem();
        if (selected == null) {
            Messages.showInfoMessage(project, "Select a connection first.", "DiffDB");
            return;
        }
        ConnectionConfig copy = selected.copy();
        ConnectionDialog dialog = new ConnectionDialog(project, copy);
        if (dialog.showAndGet()) {
            ConnectionStorageService.getInstance().save(copy);
            reloadConnections();
        }
    }

    private void onDelete() {
        ConnectionConfig selected = (ConnectionConfig) sourceCombo.getSelectedItem();
        if (selected == null) {
            return;
        }
        int answer = Messages.showYesNoDialog(project,
                "Delete connection \"" + selected.getName() + "\"?", "DiffDB", null);
        if (answer == Messages.YES) {
            ConnectionStorageService.getInstance().delete(selected.getId());
            CredentialService.clear(selected.getId());
            reloadConnections();
        }
    }

    private void onCompare() {
        ConnectionConfig source = (ConnectionConfig) sourceCombo.getSelectedItem();
        ConnectionConfig target = (ConnectionConfig) targetCombo.getSelectedItem();
        if (source == null || target == null) {
            Messages.showInfoMessage(project, "Select both source and target.", "DiffDB");
            return;
        }
        if (source.getId().equals(target.getId())) {
            Messages.showInfoMessage(project, "Source and target are the same connection.", "DiffDB");
            return;
        }

        new Task.Backgroundable(project, "Comparing schemas", true) {
            private SchemaDiffResult result;
            private Exception error;

            @Override
            public void run(ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                try {
                    result = diffService.diff(source, target, secretResolver());
                } catch (Exception e) {
                    error = e;
                }
            }

            @Override
            public void onFinished() {
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (error != null) {
                        Messages.showErrorDialog(project,
                                "Compare failed:\n" + safeMessage(error), "DiffDB");
                        return;
                    }
                    lastResult.set(result);
                    treePanel.showResult(result);
                    previewPanel.clear();
                });
            }
        }.queue();
    }

    private void onGenerateSql() {
        SchemaDiffResult result = lastResult.get();
        if (result == null) {
            Messages.showInfoMessage(project, "Run Compare first.", "DiffDB");
            return;
        }
        MigrationOptions options = new MigrationOptions();
        options.setIncludeDrops(includeDropsCheck.isSelected());

        new Task.Backgroundable(project, "Generating migration SQL", true) {
            private String sql;
            private Exception error;

            @Override
            public void run(ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                try {
                    sql = sqlGenerator.generate(result, options);
                } catch (Exception e) {
                    error = e;
                }
            }

            @Override
            public void onFinished() {
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (error != null) {
                        Messages.showErrorDialog(project,
                                "Generation failed:\n" + safeMessage(error), "DiffDB");
                        return;
                    }
                    previewPanel.setSql(sql);
                });
            }
        }.queue();
    }

    private SchemaDiffService.SecretResolver secretResolver() {
        return new SchemaDiffService.SecretResolver() {
            @Override
            public String dbPassword(ConnectionConfig config) {
                return CredentialService.getDbPassword(config.getId());
            }

            @Override
            public String sshSecret(ConnectionConfig config) {
                return CredentialService.getSshSecret(config.getId());
            }
        };
    }

    private static String safeMessage(Exception e) {
        return e.getMessage() == null ? e.toString() : e.getMessage();
    }
}
