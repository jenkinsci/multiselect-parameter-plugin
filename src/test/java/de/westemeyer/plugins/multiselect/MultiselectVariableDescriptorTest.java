package de.westemeyer.plugins.multiselect;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

public class MultiselectVariableDescriptorTest {
    @Test
    void constructor() {
        MultiselectVariableDescriptor descriptor = new MultiselectVariableDescriptor("label", "name", 0);
        Assert.assertEquals("label", descriptor.getLabel());
        Assert.assertEquals("name", descriptor.getVariableName());
        Assert.assertEquals(0, descriptor.getColumnIndex());
        Assert.assertEquals(30, descriptor.getUuid().length());
        Assert.assertNull(descriptor.getInitialValues());
        List<MultiselectDecisionItem> value = Collections.singletonList(new MultiselectDecisionItem(null, "label", "value"));
        descriptor.setInitialValues(value);
        Assert.assertEquals(value, descriptor.getInitialValues());
    }
}
