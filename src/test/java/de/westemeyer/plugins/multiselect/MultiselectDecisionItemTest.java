package de.westemeyer.plugins.multiselect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MultiselectDecisionItemTest {
    /** Input to be used in tests. */
    private static final MultiselectDecisionTree INPUT = MultiselectDecisionTree.parse("H,Type,Sport,Country,Team\nV,SELECTED_TYPE,SELECTED_SPORT,SELECTED_COUNTRY,SELECTED_TEAM\nC,Water,Wakeboarding,Germany,WSC Duisburg Rheinhausen\nC,Water,Wakeboarding,Germany,WSC Paderborn\nC,Water,Wakeboarding,Austria,WSC Wien\nT,,,,Alternative team name\nC,Water,Waterball,Germany,Waterball Team\nC,Water,Surfing,England,Bristol Surf Team\nC,Ball,Football,France,Paris St. Germain\nT,,,,Alternative team name\nC,Ball,Handball,Germany,THW Kiel\n");

    /** Alternative team name used in input. */
    private static final String ALTERNATIVE_TEAM_NAME = "Alternative team name";

    /** Specific team name used in input. */
    private static final String WSC_DUISBURG_RHEINHAUSEN = "WSC Duisburg Rheinhausen";

    /** New value set for item. */
    private static final String NEW_VALUE = "New value";

    @Test
    void getDisplayLabel() throws Exception {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 1, 0, 0);
        Assertions.assertEquals(ALTERNATIVE_TEAM_NAME, item.getDisplayLabel());
        item = INPUT.getItemByCoordinates(0, 0, 0, 0);
        Assertions.assertEquals(WSC_DUISBURG_RHEINHAUSEN, item.getDisplayLabel());
    }

    @Test
    void getParent() throws Exception {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0);
        item = item.getParent();
        Assertions.assertNotNull(item);
        Assertions.assertNull(item.getParent());
    }

    @Test
    void isRoot() throws Exception {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0);
        Assertions.assertFalse(item.isRoot());
        item = item.getParent();
        Assertions.assertTrue(item.isRoot());
    }

    @Test
    void getChildren() throws Exception {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0, 0);
        item = item.getChildren().get(0);
        Assertions.assertEquals(0, item.getChildren().size());
    }

    @Test
    void isLeaf() throws Exception {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0, 0);
        Assertions.assertFalse(item.isLeaf());
        item = item.getChildren().get(0);
        Assertions.assertTrue(item.isLeaf());
    }

    @Test
    void getLabel() throws Exception {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0, 0, 0);
        Assertions.assertEquals("", item.getLabel());
        item = INPUT.getItemByCoordinates(0, 1, 0, 0);
        Assertions.assertEquals(ALTERNATIVE_TEAM_NAME, item.getLabel());
    }

    @Test
    void value() throws Exception {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0, 0, 0);
        Assertions.assertEquals(WSC_DUISBURG_RHEINHAUSEN, item.getValue());
        item.setValue(NEW_VALUE);
        Assertions.assertEquals(NEW_VALUE, item.getValue());
    }

    @Test
    void testToString() throws Exception {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0, 0, 0);
        Assertions.assertEquals("MultiselectDecisionItem{label='', value='WSC Duisburg Rheinhausen', children=[]}", item.toString());
    }
}
