package de.westemeyer.plugins.multiselect.parser;

import de.westemeyer.plugins.multiselect.MultiselectDecisionTree;

import java.io.InputStream;

/**
 * Interface to be implemented by configuration parser objects.
 */
public interface ConfigParser {
    /**
     * Analyse configuration and transform it into config tree.
     * @param config configuration input stream
     * @return config tree
     */
    MultiselectDecisionTree analyzeConfiguration(InputStream config);

    /**
     * Get validation result, problems, etc.
     * @return validation result
     */
    String getValidationResult();
}
