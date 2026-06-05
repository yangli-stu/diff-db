package com.diffdb.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

/** Helpers for showing the DiffDB tool window. */
public final class DiffDbUi {

    private static final Logger LOG = Logger.getInstance(DiffDbUi.class);

    private DiffDbUi() {
    }

    public static void showToolWindow(@NotNull Project project) {
        ApplicationManager.getApplication().invokeLater(() -> openToolWindow(project));
    }

    private static void openToolWindow(@NotNull Project project) {
        if (project.isDisposed()) {
            return;
        }
        ToolWindowManager manager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = manager.getToolWindow(DiffDbToolWindowIds.TOOL_WINDOW_ID);
        if (toolWindow == null) {
            LOG.error("DiffDB tool window is not registered (id=" + DiffDbToolWindowIds.TOOL_WINDOW_ID + ")");
            Messages.showErrorDialog(project,
                    "DiffDB tool window is not registered.\n"
                            + "Please restart the sandbox IDE (./gradlew runIde) "
                            + "and confirm the DiffDB plugin is enabled under Settings → Plugins.",
                    "DiffDB");
            return;
        }
        if (!toolWindow.isAvailable()) {
            toolWindow.setAvailable(true);
        }
        toolWindow.show(() -> toolWindow.activate(null));
    }
}
