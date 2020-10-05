package de.westemeyer.plugins.multiselect;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class MultiselectDecisionItemTest {
    private static final MultiselectDecisionTree INPUT = MultiselectDecisionTree.parse("H,Type,Sport,Country,Team\nV,SELECTED_TYPE,SELECTED_SPORT,SELECTED_COUNTRY,SELECTED_TEAM\nC,Water,Wakeboarding,Germany,WSC Duisburg Rheinhausen\nC,Water,Wakeboarding,Germany,WSC Paderborn\nC,Water,Wakeboarding,Austria,WSC Wien\nT,,,,Alternative team name\nC,Water,Waterball,Germany,Waterball Team\nC,Water,Surfing,England,Bristol Surf Team\nC,Ball,Football,France,Paris St. Germain\nT,,,,Alternative team name\nC,Ball,Handball,Germany,THW Kiel\n");

    @Test
    void getDisplayLabel() throws Exception {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 1, 0, 0);
        Assert.assertEquals("Alternative team name", item.getDisplayLabel());
        item = INPUT.getItemByCoordinates(0, 0, 0, 0);
        Assert.assertEquals("WSC Duisburg Rheinhausen", item.getDisplayLabel());
    }

    @Test
    void getParent() throws Exception {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0);
        item = item.getParent();
        Assert.assertNotNull(item);
        Assert.assertNull(item.getParent());
    }

    @Test
    void isRoot() throws Exception {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0);
        Assert.assertFalse(item.isRoot());
        item = item.getParent();
        Assert.assertTrue(item.isRoot());
    }

    @Test
    void getChildren() throws Exception {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0, 0);
        item = item.getChildren().get(0);
        Assert.assertEquals(0, item.getChildren().size());
    }

    @Test
    void isLeaf() throws Exception {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0, 0);
        Assert.assertFalse(item.isLeaf());
        item = item.getChildren().get(0);
        Assert.assertTrue(item.isLeaf());
    }

    @Test
    void getLabel() throws Exception {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0, 0, 0);
        Assert.assertEquals("", item.getLabel());
        item = INPUT.getItemByCoordinates(0, 1, 0, 0);
        Assert.assertEquals("Alternative team name", item.getLabel());
    }

    @Test
    void value() throws Exception {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0, 0, 0);
        Assert.assertEquals("WSC Duisburg Rheinhausen", item.getValue());
        item.setValue("New value");
        Assert.assertEquals("New value", item.getValue());
    }

    @Test
    void testToString() throws Exception {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0, 0, 0);
        Assert.assertEquals("MultiselectDecisionItem{label='', value='WSC Duisburg Rheinhausen', children=[]}", item.toString());
    }
}
