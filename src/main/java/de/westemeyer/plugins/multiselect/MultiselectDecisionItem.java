package de.westemeyer.plugins.multiselect;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Multiselect decision item will be the individual select box row to use in decision tree.
 */
public class MultiselectDecisionItem implements Serializable {
    /** Serial version UID. */
    private static final long serialVersionUID = -7959174754803921973L;

    /** Label for display in selection list. */
    private String label;

    /** Value for use in variable. */
    private String value;

    /** Child items in next selection list. */
    private List<MultiselectDecisionItem> children = new ArrayList<>();

    /** Parent item. */
    private MultiselectDecisionItem parent;

    /**
     * Create a new decision item with label and value.
     * @param parent parent item to traverse tree backwards
     * @param label label for display in selection list
     * @param value value for use in variable
     */
    @DataBoundConstructor
    public MultiselectDecisionItem(MultiselectDecisionItem parent, String label, String value) {
        this.parent = parent;
        this.label = label;
        this.value = value;
    }

    /**
     * Visitor pattern implementation to walk through the tree collecting information.
     * @param visitor visitor object or lambda collecting information
     * @param columns column descriptions to go along with the items
     * @throws Exception if an error occurs in visitor
     */
    public void visitSubTree(MultiselectDecisionItemVisitor visitor, Queue<MultiselectVariableDescriptor> columns) throws Exception {
        // iterate all items in list of children and then recurse
        visitSubTree(visitor, children, columns);
    }

    /**
     * Static visitor pattern implementation to walk through the tree collecting information.
     * @param visitor visitor object or lambda collecting information
     * @param items item list (column entries) to iterate
     * @param columns column descriptions to go along with the items
     * @throws Exception if an error occurs in visitor
     */
    public static void visitSubTree(MultiselectDecisionItemVisitor visitor, List<MultiselectDecisionItem> items, Queue<MultiselectVariableDescriptor> columns) throws Exception {
        // remove first item from queue
        MultiselectVariableDescriptor column = columns.poll();
        // iterate all items in given list and then recurse
        for (MultiselectDecisionItem subItem : items) {
            // apply visitor function/lambda
            if (visitor.visit(subItem, column)) {
                // recursion with copy of remaining items in queue
                subItem.visitSubTree(visitor, new ArrayDeque<>(columns));
            }
        }
    }

    /**
     * Visitor pattern implementation to walk through the tree collecting information. Select the column items
     * by their column index (no iteration involved).
     * @param visitor visitor object or lambda collecting information
     * @param columns column descriptions to go along with the items
     * @param itemPath indices of items in columns to select and walk through
     */
    public void visitSelectedItems(MultiselectDecisionItemVisitor visitor, Queue<MultiselectVariableDescriptor> columns, Queue<Integer> itemPath) {
        // walk through select items in list of children by their indices
        visitSelectedItems(visitor, children, columns, itemPath);
    }

    /**
     * Static visitor pattern implementation to walk through the tree collecting information. Select the column items
     * by their column index (no iteration involved).
     * @param visitor visitor object or lambda collecting information
     * @param items item list (column entries) to iterate
     * @param columns column descriptions to go along with the items
     * @param itemPath indices of items in columns to select and walk through
     */
    public static void visitSelectedItems(MultiselectDecisionItemVisitor visitor, List<MultiselectDecisionItem> items, Queue<MultiselectVariableDescriptor> columns, Queue<Integer> itemPath) {
        // pop first item index from queue
        Integer index = itemPath.poll();

        // index may (theoretically) be null
        if (index != null) {
            // select item by its position in list
            MultiselectDecisionItem subItem = items.get(index);

            // apply visitor function/lambda, removing first item from queue at the same time
            if(visitor.visit(subItem, columns.poll())) {
                // recursion with copy of remaining items in queue
                subItem.visitSelectedItems(visitor, new ArrayDeque<>(columns), itemPath);
            }
        }
    }

    /**
     * Get label for this item.
     * @return label for this item
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set label for this item.
     * @param label the new label
     */
    @DataBoundSetter
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Get value for this item.
     * @return value for this item
     */
    public String getValue() {
        return value;
    }

    /**
     * Set value for this item.
     * @param value value for this item
     */
    @DataBoundSetter
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get children for this item.
     * @return children for this item
     */
    public List<MultiselectDecisionItem> getChildren() {
        return children;
    }

    /**
     * Set children for this item.
     * @param children children for this item
     */
    @DataBoundSetter
    public void setChildren(List<MultiselectDecisionItem> children) {
        this.children = children;
        for (MultiselectDecisionItem child : children) {
            child.setParent(this);
        }
    }

    /**
     * Whether the item is at the root of the tree.
     * @return whether the item is the root item
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Whether the item is a leaf item at the rightmost column
     * @return whether the item is a leaf item at the rightmost column
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * Get parent for this item.
     * @return parent for this item
     */
    public MultiselectDecisionItem getParent() {
        return parent;
    }

    /**
     * Set parent for this item.
     * @param parent the new parent object
     */
    public void setParent(MultiselectDecisionItem parent) {
        this.parent = parent;
    }

    /**
     * Get display label for this item.
     * @return display label for this item
     */
    public String getDisplayLabel() {
        if (label == null || label.length() == 0) {
            return value;
        }
        return label;
    }

    @Override
    public String toString() {
        return "MultiselectDecisionItem{" + "label='" + nvl(label) + '\'' + ", value='" + value + '\'' + ", children=" + children + '}';
    }

    private String nvl(String input) {
        return input == null ? "" : input;
    }
}
