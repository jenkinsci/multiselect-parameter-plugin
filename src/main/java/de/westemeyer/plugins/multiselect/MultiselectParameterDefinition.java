package de.westemeyer.plugins.multiselect;

import de.westemeyer.plugins.multiselect.parser.MultiselectParameterParser;
import hudson.Extension;
import hudson.Util;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Parameter definition object is responsible for all communication between Jenkins
 * and plugin code.
 */
public class MultiselectParameterDefinition extends ParameterDefinition {
    /** Serialization UID. */
    private static final long serialVersionUID = 3307975793661301522L;

    /** Logger for this object. */
    private static final Logger LOGGER = Logger.getLogger(MultiselectParameterDefinition.class.getName());

    /** Configured parameter name constant. */
    private static final String PARAMETER_NAME = "name";

    /** Job CSV configuration content. */
    private MultiselectDecisionTree decisionTree;

    /** Configuration format for a parameter definition. */
    private MultiselectConfigurationFormat format;

    /** UUID to be used to distinguish JavaScript values for multiple parameters from each other. */
    private final String uuid = UUIDGenerator.generateUUID(15);

    /**
     * Create new parameter definition object.
     * @param name parameter name
     * @param description parameter description
     * @param decisionTree parsed parameter definition
     * @param format format, currently only CSV
     */
    @DataBoundConstructor
    public MultiselectParameterDefinition(String name, String description, MultiselectDecisionTree decisionTree, MultiselectConfigurationFormat format) {
        super(name, description);
        this.decisionTree = decisionTree;
        this.format = format;
    }

    /**
     * Get item list for AJAX call from config.jelly.
     * @param coordinates coordinates in tree, i.e. item indices from columns
     * @return list of parameter values for given coordinates
     */
    @JavaScriptMethod
    public String[] getItemList(Integer[] coordinates) {

        Queue<Integer> itemPath = createCoordinates(coordinates);
        List<String> returnList = new ArrayList<>();
        try {
            decisionTree.visitSelectedItems(itemPath, (item, column) -> {
                // return values from last coordinate
                if (itemPath.isEmpty()) {
                    item.getChildren().forEach(child -> returnList.add(child.getDisplayLabel()));
                }
                return true;
            });
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
        }
        return returnList.toArray(new String[0]);
    }

    /**
     * Create coordinates queue from integer array.
     * @param coordinates integer array
     * @return queue for use in visitor method
     */
    public static Queue<Integer> createCoordinates(Integer... coordinates) {
        // create new queue instance
        Queue<Integer> itemPath = new ArrayDeque<>();

        // add all items to queue
        Collections.addAll(itemPath, coordinates);

        // return queue object
        return itemPath;
    }

    @Override
    @Nullable
    public ParameterValue getDefaultParameterValue() {
        return new MultiselectParameterValue(getName(), new HashMap<>());
    }

    @Override
    public ParameterValue createValue(StaplerRequest staplerRequest, JSONObject jsonObject) {
        return createValue(jsonObject);
    }

    /**
     * Extracted method reduced to use of Map instead of JSONObject for unit tests.
     * @param jsonObject map of parsed json contents
     * @return parameter value
     */
    public MultiselectParameterValue createValue(Map<String, Object> jsonObject) {
        // create storage for values
        Map<String, Integer> selectedValues = new HashMap<>();

        // convert json object to map of strings to integers (values from parameter form)
        jsonObject.forEach((key, value) -> {
            // exclude parameter name
            if (!key.equals(PARAMETER_NAME) && value instanceof String && ((String) value).length() > 0) {
                try {
                    // store new key combination in map
                    selectedValues.put(key, Integer.valueOf((String) value));
                } catch (NumberFormatException exception) {
                    LOGGER.log(Level.WARNING, "Invalid configuration index value sent from form", exception);
                }
            }
        });

        // create new parameter value
        try {
            return new MultiselectParameterValue(getName(), decisionTree.resolveValues(selectedValues));
        } catch (Exception e) {
            return new MultiselectParameterValue(getName(), new HashMap<>());
        }
    }

    @Override
    public ParameterValue createValue(StaplerRequest staplerRequest) {
        return getDefaultParameterValue();
    }

    /**
     * Get decision tree object containing all possible variable combinations.
     * @return decision tree object containing all possible variable combinations
     */
    public MultiselectDecisionTree getDecisionTree() {
        return decisionTree;
    }

    /**
     * Set decision tree object containing all possible variable combinations.
     * @param decisionTree decision tree object containing all possible variable combinations
     */
    public void setDecisionTree(MultiselectDecisionTree decisionTree) {
        this.decisionTree = decisionTree;
    }

    /**
     * Get content/parser format.
     * @return content/parser format
     */
    public MultiselectConfigurationFormat getFormat() {
        return format;
    }

    /**
     * Set content/parser format.
     * @param format content/parser format
     */
    public void setFormat(MultiselectConfigurationFormat format) {
        this.format = format;
    }

    /**
     * Get UUID to be used to distinguish JavaScript values for multiple parameters from each other.
     * @return UUID to be used to distinguish JavaScript values for multiple parameters from each other
     */
    public String getUuid() {
        return uuid;
    }

    @Extension
    @Symbol ({ "multiselect" })
    public static class DescriptorImpl extends ParameterDescriptor {
        /**
         * Validate configuration data entered in job configuration form when "configuration" text field loses focus.
         * @param value configuration text entered in text box
         * @return form validation result
         * @throws IOException in case a problem occurred while trying to read configuration
         */
        @POST
        public FormValidation doCheckConfiguration(@QueryParameter String value) throws IOException {
            // empty configuration is not useful, parameter can just as well be removed
            if (Util.fixEmptyAndTrim(value) == null) {
                return FormValidation.error(Messages.FormValidation_ConfigurationIsEmpty());
            }

            // create new parameter parser instance
            MultiselectParameterParser parser = new MultiselectParameterParser(MultiselectConfigurationFormat.CSV);

            // parse the configuration
            parser.parseConfiguration(value);

            // fetch validation result string
            String validation = parser.getValidationResult();

            // if a problem has been found...
            if (validation != null) {
                // ... create form validation using message string
                return FormValidation.warning(validation);
            }

            // otherwise parser did not find any problems
            return FormValidation.ok();
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.MultiselectParameterDefinition_DisplayName();
        }

        @Override
        public ParameterDefinition newInstance(@Nullable StaplerRequest req, @Nonnull JSONObject formData) {
            return newInstance(formData.getString("configuration"), formData.getString(PARAMETER_NAME), formData.getString("description"));
        }

        /**
         * Create new parameter definition object from configuration form.
         * @param configuration configuration data from job configuration page as string
         * @param name name of configuration parameter
         * @param description description of configuration parameter
         * @return new parameter definition
         */
        public MultiselectParameterDefinition newInstance(String configuration, String name, String description) {
            // currently only CSV configuration format is implemented
            MultiselectConfigurationFormat format = MultiselectConfigurationFormat.CSV;

            MultiselectDecisionTree multiselectDecisionTree;

            try {
                // parse configuration as object tree
                multiselectDecisionTree = new MultiselectParameterParser(format).parseConfiguration(configuration);
            } catch (IOException exception) {
                LOGGER.log(Level.WARNING, "Error trying to parse configuration format.");
                multiselectDecisionTree = new MultiselectDecisionTree();
            }

            // create parameter definition
            return new MultiselectParameterDefinition(name, description, multiselectDecisionTree, format);
        }
    }
}
