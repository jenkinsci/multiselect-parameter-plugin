package de.westemeyer.plugins.multiselect;

import java.io.Serializable;
import java.util.List;

/**
 * Descriptor object for a target variable.
 */
public class MultiselectVariableDescriptor implements Serializable {
    /** Serial version UID. */
    private static final long serialVersionUID = -3664707568849231781L;

    /** Variable label. */
    private final String label;

    /** Variable name. */
    private final String variableName;

    /** Column index of this column. */
    private final int columnIndex;

    /** UUID for use in HTML view. */
    private final String uuid = UUIDGenerator.generateUUID(30);

    /** List of initial values. */
    private List<MultiselectDecisionItem> initialValues;

    /**
     * Create new variable description object.
     * @param label variable label
     * @param variableName variable name
     * @param columnIndex index of this column
     */
    public MultiselectVariableDescriptor(String label, String variableName, int columnIndex) {
        this.label = label;
        this.variableName = variableName;
        this.columnIndex = columnIndex;
    }

    /**
     * Get variable label.
     * @return variable label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get variable name.
     * @return variable name
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * Set initial values for this variable.
     * @param initialValues initial values for this variable
     */
    public void setInitialValues(List<MultiselectDecisionItem> initialValues) {
        this.initialValues = initialValues;
    }

    /**
     * Get initial values for this variable.
     * @return initial values for this variable
     */
    public List<MultiselectDecisionItem> getInitialValues() {
        return initialValues;
    }

    /**
     * Get generated uuid for this variable.
     * @return generated uuid for this variable
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Get column index for this variable.
     * @return column index for this variable
     */
    public int getColumnIndex() {
        return columnIndex;
    }

}
