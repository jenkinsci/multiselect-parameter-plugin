package de.westemeyer.plugins.multiselect.parser;

import de.westemeyer.plugins.multiselect.MultiselectDecisionTree;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;

class CsvParserTest {
    private static final String INPUT_CSV = "H,Type,Sport,Country,Team\nV,SELECTED_TYPE,SELECTED_SPORT,SELECTED_COUNTRY,SELECTED_TEAM\nC,Water,Wakeboarding,Germany,WSC Duisburg Rheinhausen\nC,Water,Wakeboarding,Germany,WSC Paderborn\nC,Water,Wakeboarding,Austria,WSC Wien\nT,,,,Alternative team name\nC,Water,Waterball,Germany,Waterball Team\nC,Water,Surfing,England,Bristol Surf Team\nC,Ball,Football,France,Paris St. Germain\nT,,,,Alternative team name\nC,Ball,Handball,Germany,THW Kiel\n";

    private static final String INPUT_NO_TITLES = "H,Type,Sport,Country,Team\nV,SELECTED_TYPE,SELECTED_SPORT,SELECTED_COUNTRY,SELECTED_TEAM\nC,Water,Wakeboarding,Germany,WSC Duisburg Rheinhausen\nC,Water,Wakeboarding,Germany,WSC Paderborn\nC,Water,Wakeboarding,Austria,WSC Wien\nC,Water,Waterball,Germany,Waterball Team\nC,Water,Surfing,England,Bristol Surf Team\nC,Ball,Football,France,Paris St. Germain\nC,Ball,Handball,Germany,THW Kiel\n";

    @ParameterizedTest
    @ValueSource(strings = {INPUT_CSV, INPUT_NO_TITLES, "", "V,A,B\n", "H,Hello,World\n", "C,a,b\n"})
    void testParser(String input) throws Exception {
        MultiselectDecisionTree decisionTree = getDecisionTree(input, true);

        Assert.assertEquals(input, decisionTree.toString());
    }

    @Test
    void onlyTitles() throws Exception {
        getDecisionTree("T,a,b\n", false);
    }

    private MultiselectDecisionTree getDecisionTree(String input, boolean assertEquality) throws Exception {
        // parse input stream to tree meta object
        MultiselectDecisionTree decisionTree = MultiselectDecisionTree.parse(input);

        // create string output stream
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            // create new CSV writer instance
            CsvWriter writer = new CsvWriter(",", "\n");

            // serialize the CSV result to output stream
            decisionTree.serialize(writer, byteArrayOutputStream);

            // assert symmetrical parsing/serialising
            String csvOutput = byteArrayOutputStream.toString();
            if (assertEquality) {
                Assert.assertEquals(input, csvOutput);
            } else {
                Assert.assertNotEquals(input, csvOutput);
            }
            System.out.println(csvOutput);
        }
        return decisionTree;
    }
}
