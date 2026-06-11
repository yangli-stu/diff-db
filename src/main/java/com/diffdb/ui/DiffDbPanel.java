package com.diffdb.ui;

import com.diffdb.diff.SchemaDiffResult;
import com.diffdb.diff.SchemaDiffService;
import com.diffdb.diff.TwoStepDiffService;
import com.diffdb.migration.FastMigrationSqlGenerator;
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
import javax.swing.JProgressBar;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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

    private final SchemaDiffService diffService = new TwoStepDiffService();
    private final MigrationSqlGenerator sqlGenerator = new FastMigrationSqlGenerator();

    private final AtomicReference<SchemaDiffResult> lastResult = new AtomicReference<>();

    private DbManagerPanel dbManagerPanel;
    private JProgressBar progressBar;
    private JBLabel statusLabel;
    private JBLabel elapsedLabel;
    private long operationStartTime;

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
        centerPanel.add(buildProgressPanel(), BorderLayout.SOUTH);
        centerPanel.add(innerSplitter, BorderLayout.CENTER);

        JBSplitter outerSplitter = new JBSplitter(true, 0.30f);
        outerSplitter.setFirstComponent(dbManagerPanel);
        outerSplitter.setSecondComponent(centerPanel);
        add(outerSplitter, BorderLayout.CENTER);

        reloadConnections();
    }

    private JPanel buildDiffToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.add(Box.createRigidArea(new Dimension(8, 0)));

        toolbar.add(new JBLabel("Source:"));
        toolbar.add(Box.createRigidArea(new Dimension(4, 0)));

        // Set a preferred width but allow it to shrink/grow
        sourceCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, 26));
        sourceCombo.setPreferredSize(new Dimension(120, 26));
        toolbar.add(sourceCombo);
        toolbar.add(Box.createRigidArea(new Dimension(8, 0)));

        toolbar.add(new JBLabel("Target:"));
        toolbar.add(Box.createRigidArea(new Dimension(4, 0)));

        targetCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, 26));
        targetCombo.setPreferredSize(new Dimension(120, 26));
        toolbar.add(targetCombo);
        toolbar.add(Box.createRigidArea(new Dimension(8, 0)));

        JButton compareBtn = new JButton("Compare");
        compareBtn.setMaximumSize(new Dimension(120, 26));
        compareBtn.setMinimumSize(new Dimension(80, 26));
        compareBtn.addActionListener(e -> onCompare());
        toolbar.add(compareBtn);
        toolbar.add(Box.createRigidArea(new Dimension(8, 0)));

        toolbar.add(Box.createHorizontalGlue());
        return toolbar;
    }

    private JPanel buildProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        statusLabel = new JBLabel("Ready");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));
        left.add(statusLabel);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(120, 14));

        elapsedLabel = new JBLabel("");
        elapsedLabel.setFont(elapsedLabel.getFont().deriveFont(Font.PLAIN, 11f));

        left.add(progressBar);
        left.add(elapsedLabel);

        panel.add(left, BorderLayout.WEST);
        return panel;
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

    private void setStatus(String message) {
        ApplicationManager.getApplication().invokeLater(() -> {
            statusLabel.setText(message);
            progressBar.setVisible(message != null && !message.equals("Ready"));
            if (message != null && !message.equals("Ready")) {
                operationStartTime = System.currentTimeMillis();
                elapsedLabel.setText("");
            }
        });
    }

    private void updateElapsed() {
        long elapsed = System.currentTimeMillis() - operationStartTime;
        String text = String.format("  %.1fs", elapsed / 1000.0);
        ApplicationManager.getApplication().invokeLater(() -> elapsedLabel.setText(text));
    }

    private void clearStatus() {
        ApplicationManager.getApplication().invokeLater(() -> {
            statusLabel.setText("Ready");
            progressBar.setVisible(false);
            long elapsed = System.currentTimeMillis() - operationStartTime;
            elapsedLabel.setText(String.format("  %.1fs", elapsed / 1000.0));
        });
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

        setStatus("Comparing schemas...");
        javax.swing.Timer timer = new javax.swing.Timer(200, e -> updateElapsed());
        timer.start();

        new Task.Backgroundable(project, "Comparing schemas", true) {
            private SchemaDiffResult result;
            private Throwable error;

            @Override
            public void run(ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                try {
                    LOG.info("Compare started: source=" + source.getName() + ", target=" + target.getName());
                    SchemaDiffService.ProgressListener listener = msg -> {
                        setStatus(msg);
                        LOG.info("Diff step: " + msg);
                    };
                    result = diffService.diff(source, target, secretResolver(), listener);
                    LOG.info("Compare finished: empty=" + (result != null && result.isEmpty()));
                } catch (Throwable t) {
                    error = t;
                    LOG.error("Compare failed", t);
                }
            }

            @Override
            public void onFinished() {
                timer.stop();
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (error != null) {
                        clearStatus();
                        Messages.showErrorDialog(project,
                                "Compare failed:\n" + formatError(error), "DiffDB");
                        return;
                    }
                    lastResult.set(result);
                    diffTablePanel.showResult(result, source.getName(), target.getName());
                    previewPanel.clear();
                    clearStatus();

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
        setStatus("Generating SQL...");

        ConnectionConfig target = (ConnectionConfig) targetCombo.getSelectedItem();
        MigrationOptions options = new MigrationOptions();
        if (target != null) {
            String schema = target.getEffectiveSchema();
            if (schema != null && !schema.isBlank()) {
                options.setIncludeSchema(true);
                options.setTargetSchema(schema);
            }
            options.setTargetDatabaseType(target.getDatabaseType());
        }

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
                        clearStatus();
                        Messages.showErrorDialog(project,
                                "Generation failed:\n" + formatError(error), "DiffDB");
                        return;
                    }
                    previewPanel.setSql(sql);
                    clearStatus();
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
