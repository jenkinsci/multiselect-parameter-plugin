package de.westemeyer.plugins.multiselect.parser;

import de.westemeyer.plugins.multiselect.MultiselectDecisionItem;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class ValueConstructionHelperTest {
    @Test
    void toStringTest() {
        MultiselectDecisionItem item = new MultiselectDecisionItem(null, "label", "value");
        ValueConstructionHelper helper = new ValueConstructionHelper(null);
        Assert.assertEquals("", helper.toString());
        helper = new ValueConstructionHelper(item);
        Assert.assertEquals("value -> ", helper.toString());
        helper.addValueHelper("key", new ValueConstructionHelper(null));
        Assert.assertEquals("value -> key", helper.toString());
    }
}
