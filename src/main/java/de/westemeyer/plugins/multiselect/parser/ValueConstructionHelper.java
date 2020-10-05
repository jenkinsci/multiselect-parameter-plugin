package de.westemeyer.plugins.multiselect.parser;

import de.westemeyer.plugins.multiselect.MultiselectDecisionItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Value construction helper is not strictly necessary to construct
 * DecisionTree. But it helps creating the tree without the need to
 * keep a map inside the parameter values themselves.
 */
public class ValueConstructionHelper {
    /** Lookup table. */
    private final Map<String, ValueConstructionHelper> lookup = new LinkedHashMap<>();

    /** The parent decision item. */
    private final MultiselectDecisionItem decisionItem;

    /**
     * Constructor.
     * @param decisionItem the decision item to keep as parent item.
     */
    ValueConstructionHelper(MultiselectDecisionItem decisionItem) {
        this.decisionItem = decisionItem;
    }

    /**
     * Get a sub value construction helper from the lookup table.
     * @param value string value to find in lookup map
     * @return instance of value construction helper
     */
    ValueConstructionHelper getValueHelper(String value) {
        return lookup.get(value);
    }

    /**
     * Add a sub value construction helper
     * @param key    key for helper object
     * @param helper helper object
     */
    void addValueHelper(String key, ValueConstructionHelper helper) {
        lookup.put(key, helper);
    }

    /**
     * Get decision item.
     * @return decision item
     */
    public MultiselectDecisionItem getDecisionItem() {
        return decisionItem;
    }

    @Override
    public String toString() {
        if (decisionItem == null) {
            return String.join(", ", lookup.keySet());
        }

        return decisionItem.getValue() + " -> " + String.join(", ", lookup.keySet());
    }

    /**
     * Create item list from lookup table.
     * @return list of child elements
     */
    public List<MultiselectDecisionItem> createItemList() {
        // initialize list of child elements
        List<MultiselectDecisionItem> items = new ArrayList<>();

        // iterate lookup table
        lookup.values().forEach(helper -> {
            // get the value...
            MultiselectDecisionItem decisionItem = helper.getDecisionItem();

            // ... add it to list of child items
            items.add(decisionItem);

            // ... and set its children recursively
            decisionItem.setChildren(helper.createItemList());
        });

        // return list of items
        return items;
    }
}
