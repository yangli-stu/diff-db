package com.diffdb.ui;

import com.diffdb.connection.ConnectionManager;
import com.diffdb.model.AuthType;
import com.diffdb.model.ConnectionConfig;
import com.diffdb.model.DatabaseType;
import com.diffdb.model.SshConfig;
import com.diffdb.service.CredentialService;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Edits a single {@link ConnectionConfig}. Secrets are written to PasswordSafe on OK.
 */
public class ConnectionDialog extends DialogWrapper {

    private final Project project;
    private final ConnectionConfig config;

    private final JBTextField nameField = new JBTextField();
    private final com.intellij.openapi.ui.ComboBox<DatabaseType> dbTypeCombo =
            new com.intellij.openapi.ui.ComboBox<>(DatabaseType.values());
    private final JBTextField hostField = new JBTextField();
    private final JBTextField portField = new JBTextField();
    private final JBTextField databaseField = new JBTextField();
    private final JBTextField schemaField = new JBTextField();
    private final JBTextField userField = new JBTextField();
    private final JBPasswordField passwordField = new JBPasswordField();
    private final TextFieldWithBrowseButton driverJarField = new TextFieldWithBrowseButton();

    private final JBCheckBox useSshCheck = new JBCheckBox("Use SSH tunnel");
    private final JBTextField sshHostField = new JBTextField();
    private final JBTextField sshPortField = new JBTextField();
    private final JBTextField sshUserField = new JBTextField();
    private final com.intellij.openapi.ui.ComboBox<AuthType> sshAuthCombo =
            new com.intellij.openapi.ui.ComboBox<>(AuthType.values());
    private final TextFieldWithBrowseButton sshKeyField = new TextFieldWithBrowseButton();
    private final JBPasswordField sshSecretField = new JBPasswordField();
    private final JBTextField sshDbHostField = new JBTextField();
    private final JBTextField sshDbPortField = new JBTextField();

    public ConnectionDialog(@Nullable Project project, ConnectionConfig config) {
        super(project);
        this.project = project;
        this.config = config;
        setTitle("Database Connection");
        init();
        load();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        driverJarField.addBrowseFolderListener("Select JDBC Driver Jar", null, project,
                FileChooserDescriptorFactory.createSingleFileDescriptor("jar"));
        sshKeyField.addBrowseFolderListener("Select Private Key", null, project,
                FileChooserDescriptorFactory.createSingleFileDescriptor());

        JButton testButton = new JButton("Test Connection");
        testButton.addActionListener(e -> testConnection());

        JPanel form = FormBuilder.createFormBuilder()
                .addLabeledComponent("Name:", nameField)
                .addLabeledComponent("Database type:", dbTypeCombo)
                .addLabeledComponent("Host:", hostField)
                .addLabeledComponent("Port:", portField)
                .addLabeledComponent("Database:", databaseField)
                .addLabeledComponent("Schema (optional):", schemaField)
                .addLabeledComponent("User:", userField)
                .addLabeledComponent("Password:", passwordField)
                .addLabeledComponent("Driver jar (override, optional):", driverJarField)
                .addSeparator()
                .addComponent(useSshCheck)
                .addLabeledComponent("SSH host:", sshHostField)
                .addLabeledComponent("SSH port:", sshPortField)
                .addLabeledComponent("SSH user:", sshUserField)
                .addLabeledComponent("SSH auth:", sshAuthCombo)
                .addLabeledComponent("Private key:", sshKeyField)
                .addLabeledComponent("SSH password / passphrase:", sshSecretField)
                .addLabeledComponent("DB host (from SSH host):", sshDbHostField)
                .addLabeledComponent("DB port (from SSH host):", sshDbPortField)
                .addComponent(testButton)
                .getPanel();

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.CENTER);
        return wrapper;
    }

    private void load() {
        nameField.setText(config.getName());
        dbTypeCombo.setSelectedItem(config.getDatabaseType());
        hostField.setText(config.getHost());
        portField.setText(String.valueOf(config.getPort()));
        databaseField.setText(config.getDatabase());
        schemaField.setText(config.getSchema());
        userField.setText(config.getUser());
        driverJarField.setText(config.getDriverJarPath());

        String dbPwd = CredentialService.getDbPassword(config.getId());
        if (dbPwd != null) passwordField.setText(dbPwd);

        useSshCheck.setSelected(config.isUseSsh());
        SshConfig ssh = config.getSshConfig();
        sshHostField.setText(ssh.getHost());
        sshPortField.setText(String.valueOf(ssh.getPort()));
        sshUserField.setText(ssh.getUser());
        sshAuthCombo.setSelectedItem(ssh.getAuthType());
        sshKeyField.setText(ssh.getPrivateKeyPath());
        sshDbHostField.setText(ssh.getDbHost());
        sshDbPortField.setText(String.valueOf(ssh.getDbPort()));

        String sshSecret = CredentialService.getSshSecret(config.getId());
        if (sshSecret != null) sshSecretField.setText(sshSecret);
    }

    /** Applies UI values to a config copy (without persisting secrets). */
    private ConnectionConfig collect() {
        ConnectionConfig c = config.copy();
        c.setName(nameField.getText().trim());
        c.setDatabaseType((DatabaseType) dbTypeCombo.getSelectedItem());
        c.setHost(hostField.getText().trim());
        c.setPort(parseInt(portField.getText(), c.getDatabaseType().getDefaultPort()));
        c.setDatabase(databaseField.getText().trim());
        c.setSchema(schemaField.getText().trim());
        c.setUser(userField.getText().trim());
        c.setDriverJarPath(driverJarField.getText().trim());

        c.setUseSsh(useSshCheck.isSelected());
        SshConfig ssh = c.getSshConfig();
        ssh.setHost(sshHostField.getText().trim());
        ssh.setPort(parseInt(sshPortField.getText(), 22));
        ssh.setUser(sshUserField.getText().trim());
        ssh.setAuthType((AuthType) sshAuthCombo.getSelectedItem());
        ssh.setPrivateKeyPath(sshKeyField.getText().trim());
        ssh.setDbHost(sshDbHostField.getText().trim());
        ssh.setDbPort(parseInt(sshDbPortField.getText(), c.getDatabaseType().getDefaultPort()));
        return c;
    }

    private void testConnection() {
        ConnectionConfig c = collect();
        String dbPwd = new String(passwordField.getPassword());
        String sshSecret = new String(sshSecretField.getPassword());
        String error = ConnectionManager.testConnection(c, dbPwd, sshSecret);
        if (error == null) {
            Messages.showInfoMessage(project, "Connection successful.", "DiffDB");
        } else {
            Messages.showErrorDialog(project, "Connection failed:\n" + error, "DiffDB");
        }
    }

    @Override
    protected void doOKAction() {
        ConnectionConfig collected = collect();
        // Apply onto the original config instance.
        config.setName(collected.getName());
        config.setDatabaseType(collected.getDatabaseType());
        config.setHost(collected.getHost());
        config.setPort(collected.getPort());
        config.setDatabase(collected.getDatabase());
        config.setSchema(collected.getSchema());
        config.setUser(collected.getUser());
        config.setDriverJarPath(collected.getDriverJarPath());
        config.setUseSsh(collected.isUseSsh());
        config.setSshConfig(collected.getSshConfig());

        CredentialService.setDbPassword(config.getId(), new String(passwordField.getPassword()));
        CredentialService.setSshSecret(config.getId(), new String(sshSecretField.getPassword()));

        super.doOKAction();
    }

    private static int parseInt(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
