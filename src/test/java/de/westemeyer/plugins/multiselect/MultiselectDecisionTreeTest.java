package de.westemeyer.plugins.multiselect;

import de.westemeyer.plugins.multiselect.parser.ConfigSerialization;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class MultiselectDecisionTreeTest {
    private static final String INPUT_CSV = "H,Type,Sport,Country,Team\nV,SELECTED_TYPE,SELECTED_SPORT,SELECTED_COUNTRY,SELECTED_TEAM\nC,Water,Wakeboarding,Germany,WSC Duisburg Rheinhausen\nC,Water,Wakeboarding,Germany,WSC Paderborn\nC,Water,Wakeboarding,Austria,WSC Wien\nT,,,,Alternative team name\nC,Water,Waterball,Germany,Waterball Team\nC,Water,Surfing,England,Bristol Surf Team\nC,Ball,Football,France,Paris St. Germain\nT,,,,Alternative team name\nC,Ball,Handball,Germany,THW Kiel\n";

    @Test
    void resolveValues() throws Exception {
        MultiselectDecisionTree tree = MultiselectDecisionTree.parse(INPUT_CSV);
        Map<String, Integer> selection = new HashMap<>();

        Map<String, String> properties = tree.resolveValues(selection);
        Assert.assertEquals(0, properties.size());

        selection.put("SELECTED_REPOSITORY", 0);
        properties = tree.resolveValues(selection);
        Assert.assertEquals(0, properties.size());

        selection.put("SELECTED_TYPE", 0);
        selection.put("SELECTED_SPORT", 0);
        selection.put("SELECTED_COUNTRY", 1);
        selection.put("SELECTED_TEAM", 1);
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> tree.resolveValues(selection));

        selection.put("SELECTED_TEAM", 0);
        properties = tree.resolveValues(selection);
        Assert.assertEquals(4, properties.size());
        Assert.assertEquals("Water", properties.get("SELECTED_TYPE"));
        Assert.assertEquals("Wakeboarding", properties.get("SELECTED_SPORT"));
        Assert.assertEquals("Austria", properties.get("SELECTED_COUNTRY"));
        Assert.assertEquals("WSC Wien", properties.get("SELECTED_TEAM"));
    }

    @Test
    void exceptionInToString() {
        MultiselectDecisionTree decisionTree = new MultiselectDecisionTree(){
            private static final long serialVersionUID = -2603343900904810385L;

            @Override
            public void serialize(ConfigSerialization serialization, OutputStream outputStream) throws Exception {
                throw new Exception("This serialization attempt failed!");
            }
        };

        Assert.assertEquals("", decisionTree.toString());
    }

    @ParameterizedTest
    @CsvSource ({"0,Water;Ball", "1,Wakeboarding;Waterball;Surfing", "2,Germany;Austria", "3,WSC Duisburg Rheinhausen;WSC Paderborn"})
    void initialValuesForColumn(int column, String values) {
        MultiselectDecisionTree tree = MultiselectDecisionTree.parse(INPUT_CSV);
        List<String> initialValuesForColumn = tree.getInitialValuesForColumn(column).stream().map(MultiselectDecisionItem::getValue).collect(Collectors.toList());
        Assert.assertEquals(Arrays.asList(values.split(";")), initialValuesForColumn);
    }

    @Test
    void emptyItemListInitialValues() {
        MultiselectDecisionTree decisionTree = new MultiselectDecisionTree(){
            private static final long serialVersionUID = -2603343900904810385L;

            @Override
            public List<MultiselectDecisionItem> getItemList() {
                return Collections.emptyList();
            }
        };

        Assert.assertEquals(0, decisionTree.getInitialValuesForColumn(0).size());
    }
}
