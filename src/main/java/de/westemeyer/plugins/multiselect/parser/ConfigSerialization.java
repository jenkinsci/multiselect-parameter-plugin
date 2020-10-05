package de.westemeyer.plugins.multiselect.parser;

import de.westemeyer.plugins.multiselect.MultiselectDecisionTree;

import java.io.OutputStream;

public interface ConfigSerialization {
    void serialize(MultiselectDecisionTree decisionTree, OutputStream outputStream) throws Exception;
}
