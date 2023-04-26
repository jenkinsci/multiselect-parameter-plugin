package de.westemeyer.plugins.multiselect.parser;

import com.opencsv.CSVWriter;
import de.westemeyer.plugins.multiselect.MultiselectDecisionTree;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Writer class to print tree to CSV.
 */
public class CsvWriter implements ConfigSerialization {
    /**
     * Serialize a variable content tree as CSV text.
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
     * @param prefix row prefix "H" or "V"
     * @param values list of string values
     * @param writer output stream writer to write to
     */
    private void writeList(String prefix, @NonNull List<String> values, CSVWriter writer) {
        // only write row in case variables are defined
        if (!values.isEmpty()) {
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

}
