package de.westemeyer.plugins.multiselect;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
    void getDisplayLabel() {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 1, 0, 0);
        assertEquals(ALTERNATIVE_TEAM_NAME, item.getDisplayLabel());
        item = INPUT.getItemByCoordinates(0, 0, 0, 0);
        assertEquals(WSC_DUISBURG_RHEINHAUSEN, item.getDisplayLabel());
        item = createItem(null, null);
        assertNull(item.getDisplayLabel());
    }

    @NonNull
    private static MultiselectDecisionItem createItem(String label, String value) {
        return new MultiselectDecisionItem(label, value);
    }

    @Test
    void getParent() {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0);
        item = item.getParent();
        assertNotNull(item);
        assertNull(item.getParent());
    }

    @Test
    void nvl() {
        assertEquals("MultiselectDecisionItem{label='', value='null', children=[]}", createItem(null, null).toString());
        assertEquals("MultiselectDecisionItem{label='label', value='null', children=[]}", createItem("label", null).toString());
    }

    @Test
    void isRoot() {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0);
        assertFalse(item.isRoot());
        item = item.getParent();
        assertTrue(item.isRoot());
    }

    @Test
    void getChildren() {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0, 0);
        item = item.getChildren().get(0);
        assertEquals(0, item.getChildren().size());
    }

    @Test
    void isLeaf() {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0, 0);
        assertFalse(item.isLeaf());
        item = item.getChildren().get(0);
        assertTrue(item.isLeaf());
    }

    @Test
    void getLabel() {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0, 0, 0);
        assertEquals("", item.getLabel());
        item = INPUT.getItemByCoordinates(0, 1, 0, 0);
        assertEquals(ALTERNATIVE_TEAM_NAME, item.getLabel());
    }

    @Test
    void value() {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0, 0, 0);
        assertEquals(WSC_DUISBURG_RHEINHAUSEN, item.getValue());
        item.setValue(NEW_VALUE);
        assertEquals(NEW_VALUE, item.getValue());
    }

    @Test
    void testToString() {
        MultiselectDecisionItem item = INPUT.getItemByCoordinates(0, 0, 0, 0);
        assertEquals("MultiselectDecisionItem{label='', value='WSC Duisburg Rheinhausen', children=[]}", item.toString());
    }

    @Test
    void testSetter() {
        MultiselectDecisionItem item = createItem("Hello", "Value");
        assertEquals("Hello", item.getLabel());
        item.setLabel("Hullo");
        assertEquals("Hullo", item.getLabel());
    }

    @Test
    void visitSubTree() throws Exception {
        MultiselectDecisionItemVisitor visitor = (item, column) -> false;
        MultiselectVariableDescriptor descriptor = new MultiselectVariableDescriptor("", "");
        Queue<MultiselectVariableDescriptor> descriptors = new ArrayDeque<>();
        descriptors.add(descriptor);
        MultiselectDecisionItem item = mock(MultiselectDecisionItem.class);
        MultiselectDecisionItem.visitSubTree(visitor, Collections.singletonList(item), descriptors);
        verify(item, times(0)).visitSubTree(any(), any());
    }
}
