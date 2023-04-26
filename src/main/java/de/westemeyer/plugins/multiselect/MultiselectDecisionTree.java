package de.westemeyer.plugins.multiselect;

import de.westemeyer.plugins.multiselect.parser.ConfigSerialization;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Tree object accumulating all meta information entered in configuration of the build parameter
 * configuration form.
 */
public class MultiselectDecisionTree implements Serializable {
    /** Serial version UID. */
    private static final long serialVersionUID = -5015514196308288683L;

    /** Logger for this object. */
    private static final Logger LOGGER = Logger.getLogger(MultiselectDecisionTree.class.getName());

    /** List of items in first selection list. */
    @NonNull
    private List<MultiselectDecisionItem> itemList = new ArrayList<>();

    /** Meta information about build variables/columns. */
    @NonNull
    private List<MultiselectVariableDescriptor> variableDescriptions = new ArrayList<>();

    /**
     * Get initial values for column when first displaying list of select boxes in "build with parameters" view.
     * @param column column number
     * @return list of items to display in given column
     */
    public List<MultiselectDecisionItem> getInitialValuesForColumn(int column) {
        // initialise return value with first column
        List<MultiselectDecisionItem> items = getItemList();

        // traverse tree until column index is reached...
        for (int i = 1; i <= column && !items.isEmpty(); ++i) {
            // ... every time selecting the topmost element in column
            // to move on, collecting all its children
            items = items.get(0).getChildren();
        }

        // return list of items
        return items;
    }

    /**
     * Convenience method to get item by its position in tree, by
     * stepping along the tree branches using indices.
     * @param coordinates list of indices
     * @return item from tree
     */
    public MultiselectDecisionItem getItemByCoordinates(Integer... coordinates) {
        // convert list of integers into queue
        Queue<Integer> itemPath = MultiselectParameterDefinition.createCoordinates(coordinates);

        // need a reference to be able to set it inside lambda
        AtomicReference<MultiselectDecisionItem> ref = new AtomicReference<>();

        // run visitor method to identify the target item
        visitSelectedItems(itemPath, (item, column) -> {
            // stop at the very last coordinate part
            if (column.getColumnIndex() == (coordinates.length - 1)) {
                // set the item in reference
                ref.set(item);
                return false;
            }
            return true;
        });

        // return reference value
        return ref.get();
    }

    /**
     * Serialize the tree using a given serialization method.
     * @param serialization serialization method to use
     * @param outputStream  output stream to write to
     * @throws Exception in case an error occurs writing to output stream
     */
    public void serialize(ConfigSerialization serialization, OutputStream outputStream) throws Exception {
        serialization.serialize(this, outputStream);
    }

    /**
     * Use a visitor object/lambda to iterate all items in tree.
     * @param visitor lambda to execute for each item in tree
     * @throws Exception in case an error occurs in lambda
     */
    public void visitSubTree(MultiselectDecisionItemVisitor visitor) throws Exception {
        MultiselectDecisionItem.visitSubTree(visitor, itemList, new ArrayDeque<>(variableDescriptions));
    }

    /**
     * Use a visitor object/lambda to perform an action on one item per column.
     * @param itemPath the item indices to select from each column
     * @param visitor  lambda to execute for select items in tree
     */
    public void visitSelectedItems(Queue<Integer> itemPath, MultiselectDecisionItemVisitor visitor) {
        MultiselectDecisionItem.visitSelectedItems(visitor, itemList, new ArrayDeque<>(variableDescriptions), itemPath);
    }

    /**
     * Convenience method for parameterized unit tests.
     * @param csvInput csv input
     * @return new parsed instance
     */
    public static MultiselectDecisionTree parse(String csvInput) {
        // create input stream from example CSV
        InputStream resourceAsStream = new ByteArrayInputStream(csvInput.getBytes(StandardCharsets.UTF_8));

        // parse input stream to tree meta object
        return MultiselectConfigurationFormat.CSV.createParser().analyzeConfiguration(resourceAsStream);
    }

    /**
     * Create key value pairs of variable name and its value from its name and its selected column index.
     * For example: COLUMN1=3 resolves to COLUMN1=ValueInRow3.
     * @param selectedValues map of variable names and column indices (row numbers)
     * @return property list of table cell values with variable name as key
     */
    public Map<String, String> resolveValues(Map<String, Integer> selectedValues) {
        // mapping of variable names to row item indices in correct order of columns (which has first to be determined by variable names)
        Queue<Integer> indexOrder = variableDescriptions.stream().map(MultiselectVariableDescriptor::getVariableName).map(selectedValues::get).filter(
                Objects::nonNull).collect(Collectors.toCollection(ArrayDeque::new));

        // create new properties object to take parameter keys and values
        Map<String, String> properties = new HashMap<>();

        // run visitor lambda to collect values from column indices
        visitSelectedItems(indexOrder, (item, column) -> {
            // put key and value in map of properties
            properties.put(column.getVariableName(), item.getValue());
            return true;
        });

        // return property map
        return properties;
    }

    /**
     * Get item list of first column.
     * @return item list of first column
     */
    @NonNull
    public List<MultiselectDecisionItem> getItemList() {
        return itemList;
    }

    /**
     * Set item list of first column.
     * @param itemList item list of first column
     */
    @DataBoundSetter
    public void setItemList(@NonNull List<MultiselectDecisionItem> itemList) {
        this.itemList = itemList;
        updateInitialValues();
    }

    /**
     * Get meta information about build variables/columns.
     * @return meta information about build variables/columns.
     */
    @NonNull
    public List<MultiselectVariableDescriptor> getVariableDescriptions() {
        return variableDescriptions;
    }


    /**
     * Get list of variable labels from list of variable descriptors.
     * @return list of variable labels
     */
    public List<String> getVariableLabels() {
        return extractStrings(MultiselectVariableDescriptor::getLabel);
    }

    /**
     * Get list of variable names from list of variable descriptors.
     * @return list of variable names
     */
    public List<String> getVariableNames() {
        return extractStrings(MultiselectVariableDescriptor::getVariableName);
    }

    /**
     * Extract list of strings from variable descriptors.
     * @param extractor function to map a variable descriptor to string
     * @return list of strings
     */
    private List<String> extractStrings(Function<MultiselectVariableDescriptor, String> extractor) {
        return variableDescriptions.stream().map(extractor).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Set meta information about build variables/columns.
     * @param variableDescriptions meta information about build variables/columns
     */
    @DataBoundSetter
    public void setVariableDescriptions(@NonNull List<MultiselectVariableDescriptor> variableDescriptions) {
        this.variableDescriptions = variableDescriptions;
        int i = 0;
        for (MultiselectVariableDescriptor variableDescription : this.variableDescriptions) {
            variableDescription.setColumnIndex(i++);
        }
        updateInitialValues();
    }

    /**
     * Set up initial values for display in drop down boxes on web page.
     */
    public void updateInitialValues() {
        if (!(variableDescriptions.isEmpty() || itemList.isEmpty())) {
            // iterate all columns
            for (int i = 0; i < variableDescriptions.size(); ++i) {
                // get column descriptor for column index
                MultiselectVariableDescriptor columnDescriptor = variableDescriptions.get(i);

                // find initial values per column and keep them in variable descriptor
                columnDescriptor.setInitialValues(getInitialValuesForColumn(i));
            }

            variableDescriptions.get(0).setInitialValues(itemList);
        }
    }

    @Override
    public String toString() {
        // create string output stream
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            // create new CSV writer instance
            ConfigSerialization writer = MultiselectConfigurationFormat.CSV.createWriter();

            // serialize the CSV result to output stream
            serialize(writer, byteArrayOutputStream);

            // assert symmetrical parsing/serialising
            return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error serializing configuration", e);
            return "";
        }
    }
}
