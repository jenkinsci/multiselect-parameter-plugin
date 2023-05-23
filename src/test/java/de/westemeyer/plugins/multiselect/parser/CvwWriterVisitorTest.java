package de.westemeyer.plugins.multiselect.parser;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CvwWriterVisitorTest {
    @Test
    void appendValue() {
        CvwWriterVisitor cvwWriterVisitor = new CvwWriterVisitor(null);
        List<String> result = new ArrayList<>();
        assertFalse(cvwWriterVisitor.appendValue(result, ""));
        assertEquals(1, result.size());
        assertTrue(cvwWriterVisitor.appendValue(result, "Hello"));
        assertEquals(2, result.size());
        assertFalse(cvwWriterVisitor.appendValue(result, null));
        assertEquals(3, result.size());
    }
}