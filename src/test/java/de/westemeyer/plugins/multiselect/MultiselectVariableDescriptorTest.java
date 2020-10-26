package de.westemeyer.plugins.multiselect;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

public class MultiselectVariableDescriptorTest {
    /** Constant for label. */
    private static final String LABEL = "label";
    /** Constant for name. */
    private static final String NAME = "name";

    @Test
    void constructor() {
        MultiselectVariableDescriptor descriptor = new MultiselectVariableDescriptor(LABEL, NAME, 0);
        Assert.assertEquals(LABEL, descriptor.getLabel());
        Assert.assertEquals(NAME, descriptor.getVariableName());
        Assert.assertEquals(0, descriptor.getColumnIndex());
        Assert.assertEquals(30, descriptor.getUuid().length());
        Assert.assertNull(descriptor.getInitialValues());
        List<MultiselectDecisionItem> value = Collections.singletonList(new MultiselectDecisionItem(null, LABEL, "value"));
        descriptor.setInitialValues(value);
        Assert.assertEquals(value, descriptor.getInitialValues());
    }
}
