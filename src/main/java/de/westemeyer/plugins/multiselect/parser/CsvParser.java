package de.westemeyer.plugins.multiselect.parser;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import de.westemeyer.plugins.multiselect.Messages;
import de.westemeyer.plugins.multiselect.MultiselectDecisionItem;
import de.westemeyer.plugins.multiselect.MultiselectDecisionTree;
import de.westemeyer.plugins.multiselect.MultiselectVariableDescriptor;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Parser implementation to use for CSV configuration type.
 */
public class CsvParser implements ConfigParser {
    /** Logger for csv parser. */
    private static final Logger LOGGER = Logger.getLogger(CsvParser.class.getName());

    /** Form validation result, null if everything is OK. */
    private String validationResult = null;

    /**
     * Analyze configuration string and transform it into a tree representation of values.
     * @param config configuration input stream
     * @return tree of variable values
     */
    @Override
    public MultiselectDecisionTree analyzeConfiguration(InputStream config) {
        // create a new input stream reader
        InputStreamReader reader = new InputStreamReader(config, StandardCharsets.UTF_8);

        // create a new csv reader object
        CSVReader csvReader = new CSVReaderBuilder(reader).build();

        // references for different kinds of rows
        List<String> headers = null;
        List<String> variableNames = null;
        List<String> titles = null;

        // return value instance
        MultiselectDecisionTree decisionTree = new MultiselectDecisionTree();

        // helper object used for lookup tables
        ValueConstructionHelper constructionHelper = new ValueConstructionHelper(null);

        // row index
        int index = 1;

        // iterate rows in configuration
        for (String[] row : csvReader) {
            // need at least two column entries for reasonable configuration
            if (row.length < 2) {
                continue;
            }

            // first character in row (first column) declares the type of content that follows
            RowType type = RowType.of(row[0]);

            // convert row into list of strings, starting from column two
            List<String> subList = Arrays.stream(row).skip(1).collect(Collectors.toList());

            // handle each row type separately
            switch(type) {
            case HEADER:
                // store row in headers
                headers = subList;
                break;
            case VARIABLENAME:
                // store row in variable names used in build environment variables
                variableNames = subList;
                break;
            case TITLE:
                // store row in titles
                titles = subList;
                // row length should not be longer than length of headers
                if (variableNames != null && subList.size() > variableNames.size()) {
                    validationResult = Messages.FormValidation_NotEnoughColumns(index);
                }
                break;
            case CONTENT:
                // row length should not be longer than length of headers
                if (variableNames != null && subList.size() > variableNames.size()) {
                    validationResult = Messages.FormValidation_NotEnoughColumns(index);
                }
                // combine items in tree
                addItems(constructionHelper, titles, subList);
                // reset titles
                titles = null;
                break;
            default:
                LOGGER.log(Level.INFO, "Invalid configuration value");
            }

            // increment row number
            ++index;
        }

        // create storage for variable descriptor objects
        List<MultiselectVariableDescriptor> variableDescriptions = new ArrayList<>();

        // variable names could be missing in configuration...
        if (variableNames == null) {
            validationResult = Messages.FormValidation_NoVariablesDefined();
        }

        // names or headers should be present
        if (variableNames != null || headers != null) {
            int variableNamesSize = size(variableNames);
            int headersSize = size(headers);
            // ... otherwise create a new descriptor per variable
            for (int i = 0; i < Math.max(variableNamesSize, headersSize); ++i) {
                // create a label for the dropdown box
                String label = get(headers, i);

                // create variable name
                String variable = get(variableNames, i);

                // create descriptor and add it to the list of variable descriptors
                variableDescriptions.add(new MultiselectVariableDescriptor(label, variable, i));
            }
        }

        // set item list in result
        decisionTree.setItemList(constructionHelper.createItemList());

        // set variable descriptions in result
        decisionTree.setVariableDescriptions(variableDescriptions);

        return decisionTree;
    }

    private int size(List<String> list) {
        return list == null ? 0 : list.size();
    }

    private String get(List<String> list, int index) {
        return (list != null && index < list.size()) ? list.get(index) : null;
    }

    @Override
    public String getValidationResult() {
        return validationResult;
    }

    /**
     * Add content items to list of items.
     * @param rootHelper root construction helper with lookup tables
     * @param headers list of headers
     * @param values list of values
     */
    private void addItems(ValueConstructionHelper rootHelper, List<String> headers, List<String> values) {
        // initialize helper object iterator with root object
        ValueConstructionHelper currentHelper = rootHelper;

        // iterate list of values
        for (int i = 0;i < values.size(); ++i) {
            // get value for column of index i
            String value = values.get(i);

            // default title is empty
            String title = "";

            // if a title is found for column index i...
            if (headers != null && headers.size() > i) {
                // use it as title
                title = headers.get(i);
            }

            // try to find value helper for current value
            ValueConstructionHelper valueHelper = currentHelper.getValueHelper(value);

            // if no value helper has been found...
            if (valueHelper == null) {
                // ... create a new one
                valueHelper = new ValueConstructionHelper(new MultiselectDecisionItem(currentHelper.getDecisionItem(), title, value));

                // ... and add it to lookup table of current helper
                currentHelper.addValueHelper(value, valueHelper);
            }

            // then advance in list of columns by using lookup table
            currentHelper = valueHelper;
        }
    }

    /**
     * Enum for allowed types of row content.
     */
    private enum RowType {
        /** Single header row, containing labels for variable names. */
        HEADER("H"),
        /** List of variable names. */
        VARIABLENAME("V"),
        /** Optional titles for a following content-row. */
        TITLE("T"),
        /** Content row containing values for variables, defaulting also as titles in dropdown boxes in case titles are missing. */
        CONTENT("C"),
        /** Invalid row content detected. */
        UNKNOWN("");

        /** The individual marker character for a row type. */
        private final String marker;

        /**
         * Private constructor for this enum.
         * @param marker the individual marker character for a row type
         */
        RowType(String marker) {
            this.marker = marker;
        }

        /**
         * Determine the content type of a row by its marker character.
         * @param marker the individual marker character for a row type
         * @return the matching enum value or UNKNOWN
         */
        static RowType of(String marker) {
            // iterate all enum values
            for (RowType rowType : values()) {
                // if a marker character matches...
                if (rowType.marker.equals(marker)) {
                    // return its enum value
                    return rowType;
                }
            }
            return UNKNOWN;
        }
    }
}
