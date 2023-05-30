package de.westemeyer.plugins.multiselect.parser;

import de.westemeyer.plugins.multiselect.MultiselectConfigurationFormat;
import de.westemeyer.plugins.multiselect.MultiselectDecisionTree;
import org.apache.tools.ant.filters.StringInputStream;

import java.io.IOException;

/**
 * Parameter parser object, delegating to a specific format parser, e.g. CSV.
 */
public class MultiselectParameterParser {
    /** The selected format. */
    private final MultiselectConfigurationFormat format;

    /** A validation result if anything appears to be off. */
    private String validationResult;

    /**
     * Create a new parameter parser instance for a specific format.
     * @param format the format to read
     */
    public MultiselectParameterParser(MultiselectConfigurationFormat format) {
        this.format = format;
    }

    /**
     * Get parser validation result.
     * @return parser validation result
     */
    public String getValidationResult() {
        return validationResult;
    }

    /**
     * Parse configuration string.
     * @param input string to parse as CSV
     * @return variable selection tree
     * @throws IOException in case an error occurs while reading string
     */
    public MultiselectDecisionTree parseConfiguration(String input) throws IOException {
        // open string input stream
        try (StringInputStream inputStream = new StringInputStream(input)) {
            // parse configuration
            return parseConfiguration(inputStream);
        }
    }

    /**
     * Parse the configuration stream.
     * @param inputStream input stream to read
     * @return variable selection tree
     */
    private MultiselectDecisionTree parseConfiguration(StringInputStream inputStream) {
        // create a new parser for the specific format
        ConfigParser parser = format.createParser();

        // Analyze the configuration
        MultiselectDecisionTree multiselectDecisionTree = parser.analyzeConfiguration(inputStream);

        // keep validation result
        validationResult = parser.getValidationResult();

        multiselectDecisionTree.updateInitialValues();

        // return variable content tree
        return multiselectDecisionTree;
    }

    /**
     * Get the format that is being used.
     * @return the format that is being used
     */
    public MultiselectConfigurationFormat getFormat() {
        return format;
    }
}
