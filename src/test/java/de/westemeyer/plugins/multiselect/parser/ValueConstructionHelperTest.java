package de.westemeyer.plugins.multiselect.parser;

import de.westemeyer.plugins.multiselect.MultiselectDecisionItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValueConstructionHelperTest {
    @Test
    void toStringTest() {
        MultiselectDecisionItem item = new MultiselectDecisionItem(null, "label", "value");
        ValueConstructionHelper helper = new ValueConstructionHelper(null);
        Assertions.assertEquals("", helper.toString());
        helper = new ValueConstructionHelper(item);
        Assertions.assertEquals("value -> ", helper.toString());
        helper.addValueHelper("key", new ValueConstructionHelper(null));
        Assertions.assertEquals("value -> key", helper.toString());
    }
}
