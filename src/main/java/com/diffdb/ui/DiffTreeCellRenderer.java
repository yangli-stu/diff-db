package com.diffdb.ui;

import com.diffdb.diff.DiffCategory;
import com.diffdb.diff.DiffNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.Color;

/**
 * Renders diff tree nodes, colouring by category. Drops (UNEXPECTED) are shown in
 * red as a destructive-operation warning.
 */
public class DiffTreeCellRenderer extends ColoredTreeCellRenderer {

    private static final Color GREEN = new JBColor(new Color(0x59A869), new Color(0x6A9955));
    private static final Color RED = new JBColor(new Color(0xC75450), new Color(0xCE5B57));
    private static final Color ORANGE = new JBColor(new Color(0xCC7832), new Color(0xCC7832));

    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value,
                                      boolean selected, boolean expanded,
                                      boolean leaf, int row, boolean hasFocus) {
        if (!(value instanceof DefaultMutableTreeNode)) {
            return;
        }
        Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
        if (!(userObject instanceof DiffNode)) {
            append(String.valueOf(userObject));
            return;
        }
        DiffNode node = (DiffNode) userObject;
        DiffCategory category = node.getCategory();

        if (category == DiffCategory.CONTAINER) {
            append(node.displayLabel(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
            return;
        }

        Color color = switch (category) {
            case MISSING -> GREEN;
            case UNEXPECTED -> RED;
            case CHANGED -> ORANGE;
            default -> UIUtil.getLabelForeground();
        };
        append(node.displayLabel(), new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, color));
    }
}
