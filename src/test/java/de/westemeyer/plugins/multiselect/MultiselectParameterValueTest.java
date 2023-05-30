package de.westemeyer.plugins.multiselect;

import hudson.util.VariableResolver;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MultiselectParameterValueTest {

    private static final MultiselectParameterValue VALUE = new MultiselectParameterValue("Hello", Collections.emptyMap());

    @Test
    void createVariableResolver() {
        VariableResolver<String> variableResolver = VALUE.createVariableResolver(null);
        assertNull(variableResolver.resolve("NAME"));
    }

    @SuppressWarnings({"EqualsWithItself", "AssertBetweenInconvertibleTypes"})
    @Test
    void testEquals() {
        assertNotEquals(VALUE, this);
        assertEquals(VALUE, VALUE);
        assertNotEquals(VALUE, null);
        assertNotEquals(VALUE, new MultiselectParameterValue("Other name"));
        MultiselectParameterValue sameNameDifferentContent = new MultiselectParameterValue("Hello", Collections.singletonMap("key", "value"));
        assertNotEquals(VALUE, sameNameDifferentContent);
    }

    @Test
    void testHashCode() {
        assertEquals(-2137066224, VALUE.hashCode());
    }
}