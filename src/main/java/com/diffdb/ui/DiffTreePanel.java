package com.diffdb.ui;

import com.diffdb.diff.DiffNode;
import com.diffdb.diff.SchemaDiffResult;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.BorderLayout;

/**
 * Shows a {@link SchemaDiffResult} as a colour-coded tree.
 */
public class DiffTreePanel extends JPanel {

    private final Tree tree;
    private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Differences");
    private final DefaultTreeModel model = new DefaultTreeModel(root);

    public DiffTreePanel() {
        super(new BorderLayout());
        tree = new Tree(model);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new DiffTreeCellRenderer());
        add(new JBScrollPane(tree), BorderLayout.CENTER);
    }

    public void showResult(SchemaDiffResult result) {
        root.removeAllChildren();
        if (result == null || result.isEmpty()) {
            root.add(new DefaultMutableTreeNode(
                    DiffNode.container("No differences \u2014 schemas are identical")));
        } else {
            for (DiffNode node : result.getRoots()) {
                root.add(toTreeNode(node));
            }
        }
        model.reload();
        expandAll();
    }

    public void clear() {
        root.removeAllChildren();
        model.reload();
    }

    private DefaultMutableTreeNode toTreeNode(DiffNode node) {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node);
        for (DiffNode child : node.getChildren()) {
            treeNode.add(toTreeNode(child));
        }
        return treeNode;
    }

    private void expandAll() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }
}
