package de.westemeyer.plugins.multiselect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultiselectVariableDescriptorTest {
    /** Constant for label. */
    private static final String LABEL = "label";
    /** Constant for name. */
    private static final String NAME = "name";

    @Test
    void constructor() {
        MultiselectVariableDescriptor descriptor = new MultiselectVariableDescriptor(LABEL, NAME);
        assertEquals(LABEL, descriptor.getLabel());
        assertEquals(NAME, descriptor.getVariableName());
        assertEquals(0, descriptor.getColumnIndex());
        assertEquals(30, descriptor.getUuid().length());
        Assertions.assertNull(descriptor.getInitialValues());
        MultiselectDecisionItem value1 = new MultiselectDecisionItem(LABEL, "value");
        List<MultiselectDecisionItem> value = Collections.singletonList(value1);
        descriptor.setInitialValues(value);
        assertEquals(value, descriptor.getInitialValues());
    }

    @Test
    void setLabel() {
        MultiselectVariableDescriptor descriptor = new MultiselectVariableDescriptor(LABEL, NAME);
        assertEquals(LABEL, descriptor.getLabel());
        descriptor.setLabel("different label");
        assertEquals("different label", descriptor.getLabel());
    }

    @Test
    void setVariableName() {
        MultiselectVariableDescriptor descriptor = new MultiselectVariableDescriptor(LABEL, NAME);
        assertEquals(NAME, descriptor.getVariableName());
        descriptor.setVariableName("different name");
        assertEquals("different name", descriptor.getVariableName());
    }
}
