package de.westemeyer.plugins.multiselect;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.ParameterValue;
import hudson.model.Run;
import hudson.util.VariableResolver;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Map;
import java.util.Objects;

/**
 * Parameter value is a map of keys and values, representing the build environment variables and
 * their content.
 */
public class MultiselectParameterValue extends ParameterValue {
    /** Serial version UID. */
    private static final long serialVersionUID = -5612496743376284422L;

    /** The values selected in "build with parameters" step. */
    private final Map<String, String> selectedValues;

    /**
     * Create a new MultiselectParameterValue object.
     * @param name name of parameter
     * @param selectedValues selected values in select boxes
     */
    @DataBoundConstructor
    public MultiselectParameterValue(String name, Map<String, String> selectedValues) {
        super(name, null);
        this.selectedValues = selectedValues;
    }

    @Override
    public void buildEnvironment(Run<?, ?> build, EnvVars env) {
        // iterate all properties
        for(Map.Entry<String, String> entry : getSelectedValues().entrySet()) {
            // copy key/value combination to target map by applying consumer
            env.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Copy properties into EnvVars object. Note, that this method is used from "value.jelly" as well!
     * @return parsed properties
     */
    public Map<String, String> getSelectedValues() {
        return selectedValues;
    }

    @Override
    public Object getValue() {
        return getSelectedValues();
    }

    @Override
    public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) {
        // Hide the default single build variable by supplying null as a value
        return s -> null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        MultiselectParameterValue that = (MultiselectParameterValue) o;
        return selectedValues.equals(that.selectedValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), selectedValues);
    }
}
