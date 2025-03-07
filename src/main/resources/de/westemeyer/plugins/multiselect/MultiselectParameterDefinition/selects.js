// Get the java-class behind the code
const getJavaBehindTheCode = (element) => {
    const javaClassNameInstance = element.dataset.codeclass;
    if (javaClassNameInstance !== undefined) return window[javaClassNameInstance];
    return null;
}

const fillInValues = (name, coordinates, selectBoxId) => ((selectElement) =>
        // get item list from java code, invoke anonymous function to fill received values into select list
        getJavaBehindTheCode(selectElement).getItemList(coordinates, (response) => {
            // get response values from "getItemList" call into javascript array
            const options = response.responseObject();
            console.log("getItemList Response: %o", options);

            // clear select list
            selectElement.options.length = 0;

            // iterate all options
            options.forEach((option, index, collection) => {
                const newOption = document.createElement("option");
                newOption.textContent = option;
                newOption.value = index;
                selectElement.appendChild(newOption);
            });

            if (selectElement.options.length > 0) {
                // invoke comboBoxValueChanged method, which will in turn call
                // fillInValues with the successor combo box
                comboBoxValueChanged(name, selectElement);
            }
        })
)(document.getElementById(selectBoxId));

// Get all elements in the group until itself and the next element
const getDependingVariableIds = (name, htmlElement) => {
    console.log("getDependingVariableIds for name %o", name);
    // get all elements that contain the data-select which starts with the name (this ensures that we only process the selects that are bound together)
    // receive element and the id of the element
    const selectsWithIds = [...document.querySelectorAll(`[data-select^='${name}']`)].map(e => ({element: e, id: e.id}));
    // the next element because we need it for the fillInValues
    const lastElementIndex = selectsWithIds.map(element => element.id).indexOf(htmlElement.id);
    // slice removes all elements after the wanted
    return {
        elements: selectsWithIds.slice(0, lastElementIndex + 1),
        nextId: selectsWithIds[lastElementIndex + 1]?.element
    };
}

const comboBoxValueChanged = (name, element) => {
    console.log("Change detected for %o: %o", name, element);
    const information = getDependingVariableIds(name, element);
    // since we need only the selected values we collect them from the elements
    const coordinates = information.elements.map(item => item.element.options[item.element.selectedIndex].value);

    // if we have the nextId we are not at the end
    if (information.nextId) {
        fillInValues(name, coordinates, information.nextId.id);
    }
}

Behaviour.specify(".select.div", 'select', 0, (listElement) => {
    // if empty element (jenkins creates an entry with no content!?) do not add listener
    if (listElement.innerText === '') return;

    // receive the name of the parameter-group
    const name = listElement.dataset.selectForm;

    // in this list group add listener to all selects
    listElement.querySelectorAll('select').forEach(selectElement => {
        console.log("Add listener for all selects that contains name %o", name);
        selectElement.addEventListener('change', (event) => {
            comboBoxValueChanged(name, event.currentTarget);
        });
    });
});