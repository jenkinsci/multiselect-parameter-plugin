package de.westemeyer.plugins.multiselect.parser;

import com.opencsv.CSVWriter;
import de.westemeyer.plugins.multiselect.MultiselectDecisionItem;
import de.westemeyer.plugins.multiselect.MultiselectDecisionItemVisitor;
import de.westemeyer.plugins.multiselect.MultiselectVariableDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Visitor class to write tree to CSV. Traverse tree until leaf node is reached before going all the way back to the
 * root node to construct two complete lines of labels and values.
 */
class CvwWriterVisitor implements MultiselectDecisionItemVisitor {
    /** Writer to use. */
    private final CSVWriter writer;

    /**
     * Create new visitor object.
     * @param writer writer to use
     */
    CvwWriterVisitor(CSVWriter writer) {
        this.writer = writer;
    }

    @Override
    public boolean visit(MultiselectDecisionItem item, MultiselectVariableDescriptor column) {
        // wait until a leaf node is found before acting
        if (item.isLeaf()) {
            // initialise empty rows
            List<String> labels = new ArrayList<>();
            List<String> values = new ArrayList<>();

            // variable keeps track, whether at least one label in row is not empty
            boolean hasLabels = false;

            do {
                // append values/labels while going back to root of tree
                hasLabels |= appendValue(labels, item.getLabel());
                appendValue(values, item.getValue());

                // advance down the tree to its root
                item = item.getParent();
            } while (item != null);

            // print both results to output writer
            if (hasLabels) {
                reverseAndWrite("T", labels);
            }
            reverseAndWrite("C", values);
        }

        // never stop visiting branches (except when exception occurs)
        return true;
    }

    /**
     * Reverse input list and write CSV row.
     * @param type   row type to prepend in first column of CSV row
     * @param values column values
     */
    private void reverseAndWrite(String type, List<String> values) {
        // append type at end of list
        values.add(type);

        // reverse list, as it has been filled from leaf to root node
        Collections.reverse(values);

        // convert to array
        String[] labelsArray = values.toArray(new String[0]);

        // write row to stream
        writer.writeNext(labelsArray, false);
    }

    /**
     * Append string values.
     * @param columns list of values for columns
     * @param value   value to append
     * @return whether appended value was not null or empty
     */
    boolean appendValue(List<String> columns, String value) {
        columns.add(value);
        return !(value == null || value.isEmpty());
    }
}
