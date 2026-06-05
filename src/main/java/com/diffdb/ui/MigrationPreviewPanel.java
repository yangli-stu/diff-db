package com.diffdb.ui;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Shows generated migration SQL in an editable editor, with copy / export actions.
 */
public class MigrationPreviewPanel extends JPanel {

    private final Project project;
    private final JBTextArea editor;

    public MigrationPreviewPanel(Project project) {
        super(new BorderLayout());
        this.project = project;

        editor = new JBTextArea();
        editor.setLineWrap(false);
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, editor.getFont().getSize()));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JBLabel("Migration SQL:"));
        JButton copyButton = new JButton("Copy");
        JButton exportButton = new JButton("Export .sql");
        copyButton.addActionListener(e -> copy());
        exportButton.addActionListener(e -> export());
        toolbar.add(copyButton);
        toolbar.add(exportButton);

        add(toolbar, BorderLayout.NORTH);
        add(new JBScrollPane(editor), BorderLayout.CENTER);
    }

    public void setSql(String sql) {
        editor.setText(sql == null ? "" : sql);
        editor.setCaretPosition(0);
    }

    public void clear() {
        editor.setText("");
    }

    private void copy() {
        String text = editor.getText();
        if (text == null || text.isBlank()) {
            return;
        }
        CopyPasteManager.getInstance().setContents(new StringSelection(text));
    }

    private void export() {
        String text = editor.getText();
        if (text == null || text.isBlank()) {
            Messages.showInfoMessage(project, "Nothing to export.", "DiffDB");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("migration.sql"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8));
                Messages.showInfoMessage(project, "Saved to " + file.getAbsolutePath(), "DiffDB");
            } catch (Exception ex) {
                Messages.showErrorDialog(project,
                        "Failed to save: " + ex.getMessage(), "DiffDB");
            }
        }
    }
}
