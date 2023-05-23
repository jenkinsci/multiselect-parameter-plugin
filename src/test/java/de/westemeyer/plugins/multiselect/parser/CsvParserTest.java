package de.westemeyer.plugins.multiselect.parser;

import com.opencsv.CSVReader;
import de.westemeyer.plugins.multiselect.MultiselectDecisionTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

class CsvParserTest {
    /** First input csv for tests. */
    private static final String INPUT_CSV = "H,Type,Sport,Country,Team\nV,SELECTED_TYPE,SELECTED_SPORT,SELECTED_COUNTRY,SELECTED_TEAM\nC,Water,Wakeboarding,Germany,WSC Duisburg Rheinhausen\nC,Water,Wakeboarding,Germany,WSC Paderborn\nC,Water,Wakeboarding,Austria,WSC Wien\nT,,,,Alternative team name\nC,Water,Waterball,Germany,Waterball Team\nC,Water,Surfing,England,Bristol Surf Team\nC,Ball,Football,France,Paris St. Germain\nT,,,,Alternative team name\nC,Ball,Handball,Germany,THW Kiel\n";

    /** Second input csv for tests without title rows. */
    private static final String INPUT_NO_TITLES = "H,Type,Sport,Country,Team\nV,SELECTED_TYPE,SELECTED_SPORT,SELECTED_COUNTRY,SELECTED_TEAM\nC,Water,Wakeboarding,Germany,WSC Duisburg Rheinhausen\nC,Water,Wakeboarding,Germany,WSC Paderborn\nC,Water,Wakeboarding,Austria,WSC Wien\nC,Water,Waterball,Germany,Waterball Team\nC,Water,Surfing,England,Bristol Surf Team\nC,Ball,Football,France,Paris St. Germain\nC,Ball,Handball,Germany,THW Kiel\n";

    /** Input from issue JENKINS-66486. */
    private static final String INPUT_QUOTED = "H,Component,Container,Machine\n"
            + "V,SELECTED_COMPONENT,SELECTED_CONTAINER,MACHINES\n"
            + "C,component1,container1,\"machine1,machine2\"\n"
            + "C,component2,container1,\"machine3,machine4\"\n"
            + "C,component3,container2,\"machine1,machine2\"\n";

    @ParameterizedTest
    @ValueSource(strings = {INPUT_CSV, INPUT_NO_TITLES, "", "V,A,B\n", "H,Hello,World\n", "C,a,b\n", INPUT_QUOTED})
    void testParser(String input) throws Exception {
        MultiselectDecisionTree decisionTree = getDecisionTree(input, true);

        Assertions.assertEquals(input, decisionTree.toString());
    }

    @Test
    void onlyTitles() throws Exception {
        getDecisionTree("T,a,b\n", false);
    }

    @Test
    void unknownRowType() throws Exception {
        getDecisionTree("X,a,b\n", false);
    }

    @Test
    void invalidColumnCount() throws Exception {
        MultiselectDecisionTree decisionTree = getDecisionTree("H\nT\n", false);
        Assertions.assertTrue(decisionTree.getVariableLabels().isEmpty());
    }

    @Test
    void testException() throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes())) {
            CsvParser csvParser = new CsvParser() {
                @Override
                protected CSVReader createCsvReader(InputStreamReader reader) {
                    return new CSVReader(new BufferedReader(new InputStreamReader(inputStream))) {
                        @Override
                        public void close() throws IOException {
                            throw new IOException("Ooops, I might get caught!");
                        }
                    };
                }
            };
            Assertions.assertDoesNotThrow(() -> csvParser.analyzeConfiguration(inputStream));
        }
    }

    private MultiselectDecisionTree getDecisionTree(String input, boolean assertEquality) throws Exception {
        // parse input stream to tree meta object
        MultiselectDecisionTree decisionTree = MultiselectDecisionTree.parse(input);

        // create string output stream
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            // create new CSV writer instance
            CsvWriter writer = new CsvWriter();

            // serialize the CSV result to output stream
            decisionTree.serialize(writer, byteArrayOutputStream);

            // assert symmetrical parsing/serialising
            String csvOutput = byteArrayOutputStream.toString();
            if (assertEquality) {
                Assertions.assertEquals(input, csvOutput);
            } else {
                Assertions.assertNotEquals(input, csvOutput);
            }
        }
        return decisionTree;
    }
}
