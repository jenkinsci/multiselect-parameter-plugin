package de.westemeyer.plugins.multiselect;

import de.westemeyer.plugins.multiselect.parser.ConfigSerialization;
import de.westemeyer.plugins.multiselect.parser.CsvWriter;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MultiselectDecisionTreeTest {
    /** Csv input content for tests. */
    private static final String INPUT_CSV = "H,Type,Sport,Country,Team\nV,SELECTED_TYPE,SELECTED_SPORT,SELECTED_COUNTRY,SELECTED_TEAM\nC,Water,Wakeboarding,Germany,WSC Duisburg Rheinhausen\nC,Water,Wakeboarding,Germany,WSC Paderborn\nC,Water,Wakeboarding,Austria,WSC Wien\nT,,,,Alternative team name\nC,Water,Waterball,Germany,Waterball Team\nC,Water,Surfing,England,Bristol Surf Team\nC,Ball,Football,France,Paris St. Germain\nT,,,,Alternative team name\nC,Ball,Handball,Germany,THW Kiel\n";

    /** Constant for SELECTED_TYPE content variable. */
    private static final String SELECTED_TYPE = "SELECTED_TYPE";

    /** Constant for SELECTED_SPORT content variable. */
    private static final String SELECTED_SPORT = "SELECTED_SPORT";

    /** Constant for SELECTED_COUNTRY content variable. */
    private static final String SELECTED_COUNTRY = "SELECTED_COUNTRY";

    /** Constant for SELECTED_TEAM content variable. */
    private static final String SELECTED_TEAM = "SELECTED_TEAM";

    @Test
    void resolveValues() {
        MultiselectDecisionTree tree = MultiselectDecisionTree.parse(INPUT_CSV);
        Map<String, Integer> selection = new HashMap<>();

        Map<String, String> properties = tree.resolveValues(selection);
        assertEquals(0, properties.size());

        selection.put("SELECTED_REPOSITORY", 0);
        properties = tree.resolveValues(selection);
        assertEquals(0, properties.size());

        selection.put(SELECTED_TYPE, 0);
        selection.put(SELECTED_SPORT, 0);
        selection.put(SELECTED_COUNTRY, 1);
        selection.put(SELECTED_TEAM, 1);
        assertThrows(IndexOutOfBoundsException.class, () -> tree.resolveValues(selection));

        selection.put(SELECTED_TEAM, 0);
        properties = tree.resolveValues(selection);
        assertEquals(4, properties.size());
        assertEquals("Water", properties.get(SELECTED_TYPE));
        assertEquals("Wakeboarding", properties.get(SELECTED_SPORT));
        assertEquals("Austria", properties.get(SELECTED_COUNTRY));
        assertEquals("WSC Wien", properties.get(SELECTED_TEAM));

        assertEquals(4, tree.getVariableDescriptions().size());
    }

    @Test
    void exceptionInToString() {
        MultiselectDecisionTree decisionTree = new MultiselectDecisionTree() {
            private static final long serialVersionUID = -2603343900904810385L;

            @Override
            public void serialize(ConfigSerialization serialization, OutputStream outputStream) throws Exception {
                throw new Exception("This serialization attempt failed!");
            }
        };

        assertEquals("", decisionTree.toString());
    }

    @ParameterizedTest
    @CsvSource({"0,Water;Ball", "1,Wakeboarding;Waterball;Surfing", "2,Germany;Austria", "3,WSC Duisburg Rheinhausen;WSC Paderborn"})
    void initialValuesForColumn(int column, String values) {
        MultiselectDecisionTree tree = MultiselectDecisionTree.parse(INPUT_CSV);
        List<String> initialValuesForColumn = tree.getInitialValuesForColumn(column).stream().map(MultiselectDecisionItem::getValue).collect(Collectors.toList());
        assertEquals(Arrays.asList(values.split(";")), initialValuesForColumn);
    }

    @Test
    void getInitialValuesForColumn() {
        MultiselectDecisionTree tree = MultiselectDecisionTree.parse("");
        List<MultiselectDecisionItem> initialValuesForColumn = tree.getInitialValuesForColumn(1);
        assertEquals(0, initialValuesForColumn.size());
    }

    @Test
    void emptyItemListInitialValues() {
        MultiselectDecisionTree decisionTree = new MultiselectDecisionTree() {
            private static final long serialVersionUID = -2603343900904810385L;

            @Override
            @NonNull
            public List<MultiselectDecisionItem> getItemList() {
                return Collections.emptyList();
            }
        };

        assertEquals(0, decisionTree.getInitialValuesForColumn(0).size());
    }

    @Test
    void serializationRoundTripTest() throws Exception {
        MultiselectDecisionTree decisionTree = new MultiselectDecisionTree();
        decisionTree.setVariableDescriptions(Arrays.asList(createDescriptor("Sport", "SELECTED_SPORT"), createDescriptor("Team", "SELECTED_TEAM")));
        decisionTree.setItemList(Arrays.asList(
                createItem(null, "Tennis", createItem(null, "Tennisclub Rumeln-Kaldenhausen e.V."), createItem("Alternative label", "Oppumer TC")),
                createItem(null, "Football", createItem(null, "Rumelner TV"), createItem(null, "FC Rumeln")),
                createItem("Very popular sport", "Wakeboard", createItem(null, "WSC Duisburg Rheinhausen"))));
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            decisionTree.serialize(new CsvWriter(), outputStream);
            assertEquals("H,Sport,Team\n" +
                    "V,SELECTED_SPORT,SELECTED_TEAM\n" +
                    "C,Tennis,Tennisclub Rumeln-Kaldenhausen e.V.\n" +
                    "T,,Alternative label\n" +
                    "C,Tennis,Oppumer TC\n" +
                    "C,Football,Rumelner TV\n" +
                    "C,Football,FC Rumeln\n" +
                    "T,Very popular sport,\n" +
                    "C,Wakeboard,WSC Duisburg Rheinhausen\n", outputStream.toString());
        }
    }

    private MultiselectDecisionItem createItem(String label, String value, MultiselectDecisionItem... children) {
        MultiselectDecisionItem item = new MultiselectDecisionItem(null, null);
        if (label != null) {
            item.setLabel(label);
        }
        if (value != null) {
            item.setValue(value);
        }
        if (children != null) {
            item.setChildren(Arrays.asList(children));
        }
        return item;
    }

    private MultiselectVariableDescriptor createDescriptor(String label, String variable) {
        MultiselectVariableDescriptor descriptor = new MultiselectVariableDescriptor(null, null);
        descriptor.setLabel(label);
        descriptor.setVariableName(variable);
        return descriptor;
    }
}
