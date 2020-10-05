package de.westemeyer.plugins.multiselect;

/**
 * Visitor implementation to use when traversing item tree to collect information.
 */
@FunctionalInterface
public interface MultiselectDecisionItemVisitor {
    /**
     * The visit method is called in all traversed items in tree.
     * @param item the current item in tree
     * @param column column meta information
     * @return {@code false} in case walking through the item tree shall be aborted
     * @throws Exception in case an error occurred in this method (also aborting walk through items)
     */
    boolean visit(MultiselectDecisionItem item, MultiselectVariableDescriptor column) throws Exception;
}
