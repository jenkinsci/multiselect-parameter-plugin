package de.westemeyer.plugins.multiselect.parser;

import de.westemeyer.plugins.multiselect.MultiselectDecisionItem;
import de.westemeyer.plugins.multiselect.MultiselectDecisionItemVisitor;
import de.westemeyer.plugins.multiselect.MultiselectDecisionTree;
import de.westemeyer.plugins.multiselect.MultiselectVariableDescriptor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.function.Function;

/**
 * Writer class to print tree to CSV.
 */
public class CsvWriter implements ConfigSerialization {
    /** Delimiter char to use. */
    private final String delimiter;

    /** The end of line character to use. */
    private final String endOfLineCharacter;

    /**
     * Create new default CSV writer object.
     */
    public CsvWriter() {
        this(",", "\n");
    }

    /**
     * Create new CSV writer object.
     * @param delimiter delimiter char to use.
     * @param endOfLineCharacter the end of line character to use
     */
    public CsvWriter(String delimiter, String endOfLineCharacter) {
        this.delimiter = delimiter;
        this.endOfLineCharacter = endOfLineCharacter;
    }

    /**
     * Serialize a variable content tree as CSV text.
     * @param decisionTree the content tree to write as CSV
     * @param outputStream the output stream to write to
     * @throws Exception in case writing to the output stream fails
     */
    @Override
    public void serialize(MultiselectDecisionTree decisionTree, OutputStream outputStream) throws Exception {
        // wrap output stream in writer object
        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
            // write header row content
            writeList("H", decisionTree.getVariableLabels(), writer);

            // write second row
            writeList("V", decisionTree.getVariableNames(), writer);

            // print tree to CSV by applying visitor
            decisionTree.visitSubTree(new CvwWriterVisitor(writer, delimiter, endOfLineCharacter));
        }
    }

    /**
     * Write a list of entries only if variables have been defined already.
     * @param prefix row prefix "H" or "V"
     * @param values list of string values
     * @param writer output stream writer to write to
     * @throws IOException in case writing to writer fails
     */
    private void writeList(String prefix, List<String> values, OutputStreamWriter writer) throws IOException {
        // only write row in case variables are defined
        if (values != null && !values.isEmpty()) {
            // write first character in CSV
            writer.write(prefix);

            // iterate variable descriptors
            for (String value : values) {
                // write header row content
                writer.write(delimiter + value);
            }

            // terminate header row
            writer.write(endOfLineCharacter);
        }
    }

    /**
     * Visitor class to write tree to CSV. Traverse tree until leaf node is reached before
     * going all the way back to the root node to construct two complete lines of labels
     * and values.
     */
    private static class CvwWriterVisitor implements MultiselectDecisionItemVisitor {
        /** Writer to use. */
        private final Writer writer;

        /** Delimiter character for CSV. */
        private final String delimiter;

        /** The end of line character to use. */
        private final String endOfLineCharacter;

        /**
         * Create new visitor object.
         * @param writer writer to use
         * @param delimiter delimiter character for CSV
         * @param endOfLineCharacter the end of line character to use
         */
        private CvwWriterVisitor(Writer writer, String delimiter, String endOfLineCharacter) {
            this.writer = writer;
            this.delimiter = delimiter;
            this.endOfLineCharacter = endOfLineCharacter;
        }

        @Override
        public boolean visit(MultiselectDecisionItem item, MultiselectVariableDescriptor column) throws Exception {
            // wait until a leaf node is found before acting
            if (item.isLeaf()) {
                // initialise empty rows
                String labels = "";
                String values = "";

                do {
                    // prepend values/labels while going back to root of tree
                    labels = concatenateValue(item, labels, MultiselectDecisionItem::getLabel);
                    values = concatenateValue(item, values, MultiselectDecisionItem::getValue);

                    // advance down the tree to its root
                    item = item.getParent();
                } while (item != null);

                // print both results to output writer
                if (column != null && labels.length() > column.getColumnIndex()) {
                    writer.write("T" + delimiter + labels + endOfLineCharacter);
                }
                writer.write("C" + delimiter + values + endOfLineCharacter);
            }

            // never stop visiting branches (except when exception occurs)
            return true;
        }

        /**
         * Concatenate string values.
         * @param item multiselect item to get value or title from
         * @param value current value string to prepend to
         * @param function decide whether to use value or label/title
         * @return original string, prepended with item content
         */
        private String concatenateValue(MultiselectDecisionItem item, String value, Function<MultiselectDecisionItem, String> function) {
            return function.apply(item) + (item.isLeaf() ? "" : (delimiter + value));
        }
    }
}
