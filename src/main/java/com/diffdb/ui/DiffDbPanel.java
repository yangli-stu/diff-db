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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.concurrent.atomic.AtomicReference;

public class DiffDbPanel extends JPanel {

    private static final Logger LOG = Logger.getInstance(DiffDbPanel.class);

    private final Project project;

    private final com.intellij.openapi.ui.ComboBox<ConnectionConfig> sourceCombo =
            new com.intellij.openapi.ui.ComboBox<>();
    private final com.intellij.openapi.ui.ComboBox<ConnectionConfig> targetCombo =
            new com.intellij.openapi.ui.ComboBox<>();

    private final DiffResultTablePanel diffTablePanel = new DiffResultTablePanel();
    private final MigrationPreviewPanel previewPanel;

    private final SchemaDiffService diffService = new LiquibaseSchemaDiffService();
    private final MigrationSqlGenerator sqlGenerator = new LiquibaseMigrationSqlGenerator();

    private final AtomicReference<SchemaDiffResult> lastResult = new AtomicReference<>();

    private DbManagerPanel dbManagerPanel;

    public DiffDbPanel(Project project) {
        super(new BorderLayout());
        this.project = project;
        this.previewPanel = new MigrationPreviewPanel(project);

        dbManagerPanel = new DbManagerPanel(project, this::reloadConnections);

        JBSplitter innerSplitter = new JBSplitter(true, 0.60f);
        innerSplitter.setFirstComponent(diffTablePanel);
        innerSplitter.setSecondComponent(previewPanel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(buildDiffToolbar(), BorderLayout.NORTH);
        centerPanel.add(innerSplitter, BorderLayout.CENTER);

        JBSplitter outerSplitter = new JBSplitter(true, 0.30f);
        outerSplitter.setFirstComponent(dbManagerPanel);
        outerSplitter.setSecondComponent(centerPanel);
        add(outerSplitter, BorderLayout.CENTER);

        reloadConnections();
    }

    private JPanel buildDiffToolbar() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row.add(new JBLabel("Source:"));
        row.add(sourceCombo);
        row.add(new JBLabel("Target:"));
        row.add(targetCombo);

        JButton compareBtn = new JButton("Compare");
        compareBtn.addActionListener(e -> onCompare());
        row.add(compareBtn);

        return row;
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
            private Throwable error;

            @Override
            public void run(ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                try {
                    LOG.info("Compare started: source=" + source.getName() + ", target=" + target.getName());
                    result = diffService.diff(source, target, secretResolver());
                    LOG.info("Compare finished: empty=" + (result != null && result.isEmpty()));
                } catch (Throwable t) {
                    error = t;
                    LOG.error("Compare failed", t);
                }
            }

            @Override
            public void onFinished() {
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (error != null) {
                        Messages.showErrorDialog(project,
                                "Compare failed:\n" + formatError(error), "DiffDB");
                        return;
                    }
                    lastResult.set(result);
                    diffTablePanel.showResult(result);
                    previewPanel.clear();

                    // Auto-generate SQL
                    onGenerateSql();
                });
            }
        }.queue();
    }

    private void onGenerateSql() {
        SchemaDiffResult result = lastResult.get();
        if (result == null) {
            return;
        }
        MigrationOptions options = new MigrationOptions();

        new Task.Backgroundable(project, "Generating migration SQL", true) {
            private String sql;
            private Throwable error;

            @Override
            public void run(ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                try {
                    sql = sqlGenerator.generate(result, options);
                } catch (Throwable t) {
                    error = t;
                    LOG.error("Migration SQL generation failed", t);
                }
            }

            @Override
            public void onFinished() {
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (error != null) {
                        Messages.showErrorDialog(project,
                                "Generation failed:\n" + formatError(error), "DiffDB");
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

    private static String formatError(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String msg = root.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = root.toString();
        }
        return msg;
    }
}
