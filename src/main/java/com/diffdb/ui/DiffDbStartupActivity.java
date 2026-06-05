package com.diffdb.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.startup.StartupManager;
import org.jetbrains.annotations.NotNull;

/**
 * Opens the DiffDB tool window after project initialization so it is visible in the sandbox.
 */
public class DiffDbStartupActivity implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        StartupManager.getInstance(project).runWhenProjectIsInitialized(
                () -> DiffDbUi.showToolWindow(project));
    }
}
