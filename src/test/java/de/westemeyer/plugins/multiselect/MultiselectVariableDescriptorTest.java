package de.westemeyer.plugins.multiselect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

class MultiselectVariableDescriptorTest {
    /** Constant for label. */
    private static final String LABEL = "label";
    /** Constant for name. */
    private static final String NAME = "name";

    @Test
    void constructor() {
        MultiselectVariableDescriptor descriptor = new MultiselectVariableDescriptor(LABEL, NAME, 0);
        Assertions.assertEquals(LABEL, descriptor.getLabel());
        Assertions.assertEquals(NAME, descriptor.getVariableName());
        Assertions.assertEquals(0, descriptor.getColumnIndex());
        Assertions.assertEquals(30, descriptor.getUuid().length());
        Assertions.assertNull(descriptor.getInitialValues());
        List<MultiselectDecisionItem> value = Collections.singletonList(new MultiselectDecisionItem(null, LABEL, "value"));
        descriptor.setInitialValues(value);
        Assertions.assertEquals(value, descriptor.getInitialValues());
    }
}
