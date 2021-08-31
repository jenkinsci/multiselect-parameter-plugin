package de.westemeyer.plugins.multiselect.parser;

import com.opencsv.CSVWriter;
import de.westemeyer.plugins.multiselect.MultiselectDecisionItem;
import de.westemeyer.plugins.multiselect.MultiselectDecisionItemVisitor;
import de.westemeyer.plugins.multiselect.MultiselectDecisionTree;
import de.westemeyer.plugins.multiselect.MultiselectVariableDescriptor;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Writer class to print tree to CSV.
 */
public class CsvWriter implements ConfigSerialization {
    /**
     * Serialize a variable content tree as CSV text.
     *
     * @param decisionTree the content tree to write as CSV
     * @param outputStream the output stream to write to
     * @throws Exception in case writing to the output stream fails
     */
    @Override
    public void serialize(MultiselectDecisionTree decisionTree, OutputStream outputStream) throws Exception {
        // wrap output stream in writer object
        try (OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVWriter writer = new CSVWriter(streamWriter)) {
            // write header row content
            writeList("H", decisionTree.getVariableLabels(), writer);

            // write second row
            writeList("V", decisionTree.getVariableNames(), writer);

            // print tree to CSV by applying visitor
            decisionTree.visitSubTree(new CvwWriterVisitor(writer));
        }
    }

    /**
     * Write a list of entries only if variables have been defined already.
     *
     * @param prefix row prefix "H" or "V"
     * @param values list of string values
     * @param writer output stream writer to write to
     */
    private void writeList(String prefix, List<String> values, CSVWriter writer) {
        // only write row in case variables are defined
        if (values != null && !values.isEmpty()) {
            // create a new string array with enough space for all columns
            String[] line = new String[values.size() + 1];

            // first column is filled with prefix
            line[0] = prefix;

            // start with second column (index 1)
            int i = 1;

            // iterate variable descriptors
            for (String value : values) {
                // write header row content
                line[i] = value;
                ++i;
            }

            // write CSV row
            writer.writeNext(line, false);
        }
    }

    /**
     * Visitor class to write tree to CSV. Traverse tree until leaf node is reached before going all the way back to the
     * root node to construct two complete lines of labels and values.
     */
    private static class CvwWriterVisitor implements MultiselectDecisionItemVisitor {
        /** Writer to use. */
        private final CSVWriter writer;

        /**
         * Create new visitor object.
         *
         * @param writer writer to use
         */
        private CvwWriterVisitor(CSVWriter writer) {
            this.writer = writer;
        }

        @Override
        public boolean visit(MultiselectDecisionItem item, MultiselectVariableDescriptor column) throws Exception {
            // wait until a leaf node is found before acting
            if (item.isLeaf()) {
                // initialise empty rows
                List<String> labels = new ArrayList<>();
                List<String> values = new ArrayList<>();

                // variable keeps track, whether at least one label in row was is not empty
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
         *
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
         *
         * @param columns list of values for columns
         * @param value   value to append
         * @return whether appended value was not null or empty
         */
        boolean appendValue(List<String> columns, String value) {
            if (value != null) {
                columns.add(value);
                return !value.isEmpty();
            }
            return false;
        }
    }
}
