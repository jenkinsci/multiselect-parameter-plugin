package de.westemeyer.plugins.multiselect;

import de.westemeyer.plugins.multiselect.parser.MultiselectParameterParser;
import hudson.EnvVars;
import hudson.model.ParameterValue;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.Collections;

import static de.westemeyer.plugins.multiselect.MultiselectConfigurationFormat.CSV;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MultiselectParameterDefinitionTest {
    /** Input csv for tests. */
    private static final String INPUT_STRING = "H,Type,Sport,Country,Team\nV,SELECTED_TYPE,SELECTED_SPORT,SELECTED_COUNTRY,SELECTED_TEAM\nC,Water,Wakeboarding,Germany,WSC Duisburg Rheinhausen\nC,Water,Wakeboarding,Germany,WSC Paderborn\nC,Water,Wakeboarding,Austria,WSC Wien\nT,,,,Alternative team name\nC,Water,Waterball,Germany,Waterball Team\nC,Water,Surfing,England,Bristol Surf Team\nC,Ball,Football,France,Paris St. Germain\nT,,,,Alternative team name\nC,Ball,Handball,Germany,THW Kiel\n";

    /** Parsed decision tree for input CSV. */
    private static final MultiselectDecisionTree INPUT = MultiselectDecisionTree.parse(INPUT_STRING);

    /** Input CSV for validation tests. */
    private static final String VALIDATION1_STRING = "H,Type,Sport,Country,Team\nV,SELECTED_TYPE,SELECTED_SPORT,SELECTED_COUNTRY,SELECTED_TEAM\nC,Water,Wakeboarding,Germany,WSC Duisburg Rheinhausen,hello\nC,Water,Wakeboarding,Germany,WSC Paderborn\nC,Water,Wakeboarding,Austria,WSC Wien\nT,,,,Alternative team name\nC,Water,Waterball,Germany,Waterball Team\nC,Water,Surfing,England,Bristol Surf Team\nC,Ball,Football,France,Paris St. Germain\nT,,,,Alternative team name\nC,Ball,Handball,Germany,THW Kiel\n";

    /** Parsed decision tree for validation tests. */
    private static final MultiselectDecisionTree VALIDATION1 = MultiselectDecisionTree.parse(VALIDATION1_STRING);

    /** Constant for input CSV SELECTED_TYPE variable name. */
    private static final String SELECTED_TYPE = "SELECTED_TYPE";

    /** Constant for input CSV SELECTED_SPORT variable name. */
    private static final String SELECTED_SPORT = "SELECTED_SPORT";

    /** Constant for input CSV SELECTED_COUNTRY variable name. */
    private static final String SELECTED_COUNTRY = "SELECTED_COUNTRY";

    /** Constant for input CSV SELECTED_TEAM variable name. */
    private static final String SELECTED_TEAM = "SELECTED_TEAM";

    /** Constant for NAME. */
    private static final String NAME = "name";

    /** Constant for DESCRIPTION. */
    private static final String DESCRIPTION = "description";

    /** Constant for GERMANY. */
    private static final String GERMANY = "Germany";

    /** Constant for EMPTY_TO_STRING_RESULT. */
    private static final String EMPTY_TO_STRING_RESULT = "{}";

    @Test
    void constructor() {
        MultiselectParameterDefinition definition = new MultiselectParameterDefinition(NAME, DESCRIPTION, INPUT, CSV);
        assertEquals(CSV, definition.getFormat());
        assertEquals(INPUT, definition.getDecisionTree());
        definition.setDecisionTree(VALIDATION1);
        assertEquals(VALIDATION1, definition.getDecisionTree());
        // Currently there is only just one supported format. Add different value later, assert format change.
        definition.setFormat(CSV);
    }

    @Test
    void getItemList() {
        MultiselectParameterDefinition definition = new MultiselectParameterDefinition(NAME, DESCRIPTION, INPUT, CSV);
        Integer[] coordinates = new Integer[]{0, 0};
        String[] itemList = definition.getItemList(coordinates);
        assertEquals(2, itemList.length);
        assertEquals(GERMANY, itemList[0]);
        assertEquals("Austria", itemList[1]);
        definition.setDecisionTree(null);
        itemList = definition.getItemList(coordinates);
        assertEquals(0, itemList.length);
    }

    @Test
    void getDefaultParameterValue() {
        MultiselectParameterDefinition definition = new MultiselectParameterDefinition(NAME, DESCRIPTION, INPUT, CSV);
        ParameterValue defaultParameterValue = definition.getDefaultParameterValue();
        assertNotNull(defaultParameterValue);
        assertEquals(NAME, defaultParameterValue.getName());
        Object value = defaultParameterValue.getValue();
        assertNotNull(value);
        assertEquals(EMPTY_TO_STRING_RESULT, value.toString());
    }

    @Test
    void createValue() {
        MultiselectParameterDefinition definition = new MultiselectParameterDefinition(NAME, DESCRIPTION, INPUT, CSV);
        ParameterValue defaultParameterValue = definition.createValue((StaplerRequest) null);
        assertNotNull(defaultParameterValue);
        assertEquals(NAME, defaultParameterValue.getName());
        Object parameterValueContent = defaultParameterValue.getValue();
        assertNotNull(parameterValueContent);
        assertEquals(EMPTY_TO_STRING_RESULT, parameterValueContent.toString());

        JSONObject values = new JSONObject();
        values.put(SELECTED_TYPE, "0");
        values.put(SELECTED_SPORT, "1");
        values.put(SELECTED_COUNTRY, "0");
        values.put(SELECTED_TEAM, "0");
        values.put("name", "Hugo");
        values.put("integer", 1);
        values.put("empty", "");
        MultiselectParameterValue value = (MultiselectParameterValue) definition.createValue(null, values);
        assertNotNull(value);
        EnvVars vars = new EnvVars();
        value.buildEnvironment(null, vars);
        assertEquals("Water", vars.get(SELECTED_TYPE));
        assertEquals("Waterball", vars.get(SELECTED_SPORT));
        assertEquals(GERMANY, vars.get(SELECTED_COUNTRY));
        assertEquals("Waterball Team", vars.get(SELECTED_TEAM));

        assertEquals(4, value.getSelectedValues().size());

        definition.setDecisionTree(null);
        value = definition.createValue(values);
        assertEquals(0, value.getSelectedValues().size());

        values.put("go ahead", "throw an exception");
        assertDoesNotThrow(() -> definition.createValue(values));
    }

    @Test
    void doCheckConfiguration() throws IOException {
        MultiselectParameterDefinition.DescriptorImpl descriptor = new MultiselectParameterDefinition.DescriptorImpl();
        FormValidation formValidation = descriptor.doCheckConfiguration(INPUT_STRING);
        assertEquals(FormValidation.Kind.OK, formValidation.kind);
        assertNull(formValidation.getMessage());
        formValidation = descriptor.doCheckConfiguration(VALIDATION1_STRING);
        assertEquals(Messages.FormValidation_NotEnoughColumns(3), formValidation.getMessage());
        assertEquals(FormValidation.Kind.WARNING, formValidation.kind);
        formValidation = descriptor.doCheckConfiguration("");
        assertEquals(Messages.FormValidation_ConfigurationIsEmpty(), formValidation.getMessage());
        assertEquals(FormValidation.Kind.ERROR, formValidation.kind);
    }

    @Test
    void getDisplayName() {
        MultiselectParameterDefinition.DescriptorImpl descriptor = new MultiselectParameterDefinition.DescriptorImpl();
        assertEquals(Messages.MultiselectParameterDefinition_DisplayName(), descriptor.getDisplayName());
    }

    @Test
    void getUuid() {
        MultiselectParameterDefinition definition = new MultiselectParameterDefinition(NAME, DESCRIPTION, null, CSV);
        assertNotNull(definition.getUuid());
        assertEquals(15, definition.getUuid().length());
    }

    @Test
    void newInstance() {
        MultiselectParameterDefinition definition = MultiselectParameterDefinition.DescriptorImpl.newInstance(INPUT_STRING, "parametername", DESCRIPTION, new MultiselectParameterParser(CSV));
        assertNotNull(definition.getDecisionTree());
        assertEquals(INPUT_STRING, definition.getDecisionTree().toString());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("configuration", INPUT_STRING);
        jsonObject.put("name", "parametername");
        jsonObject.put("description", DESCRIPTION);
        MultiselectParameterDefinition.DescriptorImpl descriptor = new MultiselectParameterDefinition.DescriptorImpl();
        MultiselectParameterDefinition parameterDefinition = (MultiselectParameterDefinition) descriptor.newInstance(null, jsonObject);
        MultiselectDecisionTree decisionTree = parameterDefinition.getDecisionTree();
        assertNotNull(decisionTree);
        assertEquals(definition.getDecisionTree().toString(), decisionTree.toString());
        assertEquals(definition.getDescription(), parameterDefinition.getDescription());
        assertEquals(definition.getName(), parameterDefinition.getName());

        MultiselectParameterParser parser = new MultiselectParameterParser(CSV) {
            @Override
            public MultiselectDecisionTree parseConfiguration(String input) throws IOException {
                throw new IOException("Need to test the exception here!");
            }
        };
        assertDoesNotThrow(() -> MultiselectParameterDefinition.DescriptorImpl.newInstance(INPUT_STRING, "parametername", DESCRIPTION, parser));
    }

    @SuppressWarnings({"EqualsWithItself", "AssertBetweenInconvertibleTypes"})
    @Test
    void testEquals() {
        MultiselectParameterDefinition value = new MultiselectParameterDefinition(NAME, DESCRIPTION, INPUT, CSV);
        assertNotEquals(value, this);
        assertEquals(value, value);
        assertNotEquals(value, new MultiselectParameterDefinition("Other name", DESCRIPTION, INPUT, CSV));
        MultiselectParameterValue sameNameDifferentContent = new MultiselectParameterValue("Hello", Collections.singletonMap("key", "value"));
        assertNotEquals(value, sameNameDifferentContent);
        MultiselectParameterDefinition actual = new MultiselectParameterDefinition(NAME, DESCRIPTION, INPUT, CSV);
        actual.setUuid(value.getUuid());
        assertEquals(value, actual);
        actual.setDecisionTree(new MultiselectDecisionTree());
        assertNotEquals(value, actual);
    }

    @Test
    void testHashCode() {
        MultiselectParameterDefinition value = new MultiselectParameterDefinition(NAME, DESCRIPTION);
        value.setUuid("FDcYsiejIswOtJc");
        assertEquals(-234521635, value.hashCode());
    }
}
