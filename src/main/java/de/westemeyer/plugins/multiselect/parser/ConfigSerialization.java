package de.westemeyer.plugins.multiselect.parser;

import de.westemeyer.plugins.multiselect.MultiselectDecisionTree;

import java.io.OutputStream;

/**
 * Config serialization can be used to write a configuration back to an output stream, i.e. the
 * job configuration input text field.
 */
public interface ConfigSerialization {
    /**
     * Method is called by writing operation to write the job configuration to an output stream.
     * @param decisionTree the parsed job configuration
     * @param outputStream the output stream to write to
     * @throws Exception in case an error occurs, trying to write to the output stream
     */
    void serialize(MultiselectDecisionTree decisionTree, OutputStream outputStream) throws Exception;
}
