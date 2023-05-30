package de.westemeyer.plugins.multiselect.parser;

import de.westemeyer.plugins.multiselect.MultiselectDecisionTree;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvWriterTest {

    @Test
    void writeList() throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            MultiselectDecisionTree decisionTree = new MultiselectDecisionTree();
            decisionTree.setVariableDescriptions(new ArrayList<>());
            new CsvWriter().serialize(decisionTree, outputStream);
            assertEquals("", outputStream.toString());
        }
    }
}