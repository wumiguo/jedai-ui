package wizard.steps;

import com.google.inject.Inject;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.BlockCleaningCustomComparator;
import utils.JedaiOptions;
import wizard.Submit;
import wizard.Validate;
import wizard.WizardData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Step3Controller {
    public VBox containerVBox;
    public ListView<String> list;
    public ListView<String> selectedList;

    private BlockCleaningCustomComparator listComparator;
    private Map<String, SimpleBooleanProperty> optionsMap;
    private Logger log = LoggerFactory.getLogger(Step3Controller.class);

    @Inject
    private WizardData model;

    @FXML
    public void initialize() {
        // Create comparator object that will be used for list sorting later
        listComparator = new BlockCleaningCustomComparator();

        // Initialize block cleaning methods list
        model.setBlockCleaningMethods(FXCollections.observableList(new ArrayList<>()));

        // Create map with options
        optionsMap = new HashMap<>();
        optionsMap.put(JedaiOptions.SIZE_BASED_BLOCK_PURGING, new SimpleBooleanProperty(false));
        optionsMap.put(JedaiOptions.COMPARISON_BASED_BLOCK_PURGING, new SimpleBooleanProperty(false));
        optionsMap.put(JedaiOptions.BLOCK_FILTERING, new SimpleBooleanProperty(false));
//        optionsMap.put(JedaiOptions.BLOCK_SCHEDULING, new SimpleBooleanProperty(false));

        // Add items to the list
        list.getItems().addAll(optionsMap.keySet());
        list.getItems().sort(listComparator);

        // Set list cells to have checkboxes which use the map's boolean values
//        list.setCellFactory(CheckBoxListCell.forListView(optionsMap::get));
        Callback<ListView<String>, ListCell<String>> wrappedCellFactory = list.getCellFactory();

        list.setCellFactory(listView -> {
            CheckBoxListCell<String> cell = wrappedCellFactory != null ? (CheckBoxListCell<String>) wrappedCellFactory.call(listView) : new CheckBoxListCell<>();
            cell.setSelectedStateCallback(param -> optionsMap.get(param));

//            Platform.runLater(() -> {
//                if (cell.getItem() != null && cell.getItem().equals(JedaiOptions.BLOCK_SCHEDULING)) {
//                    // Add listener to disable the cell automatically if needed when the ER type changes
//                    model.erTypeProperty().addListener((observable, oldValue, newValue) -> cell.setDisable(newValue.equals(JedaiOptions.DIRTY_ER)));
//
//                    // Disable the cell if ER type is already Dirty ER
//                    cell.setDisable(model.getErType().equals(JedaiOptions.DIRTY_ER));
//                }
//            });

            return cell;
        });

        // Listen for changes in each BooleanProperty
        for (String s : optionsMap.keySet()) {
            optionsMap.get(s).addListener((observable, oldValue, newValue) -> {
                // Add/remove the string to/from the model
                if (newValue) {
                    selectedList.getItems().add(s);
                    model.getBlockCleaningMethods().add(s);
                } else {
                    model.getBlockCleaningMethods().remove(s);
                    selectedList.getItems().remove(s);
                }

                // Sort the list to the correct order using the custom comparator
                selectedList.getItems().sort(listComparator);
            });
        }

        // Listen for changes in the model, and change the values of the boolean properties
        model.blockCleaningMethodsProperty().addListener((observable, oldValue, newValue) -> {
            // Set the value of each checkbox to true or false depending on if it's in the list or not
            for (String method : optionsMap.keySet()) {
                optionsMap.get(method).setValue(
                        newValue.contains(method)
                );
            }
        });

        // Listen for ER type changes, and deselect Block Scheduling in Dirty ER
//        model.erTypeProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue.equals(JedaiOptions.DIRTY_ER)) {
//                optionsMap.get(JedaiOptions.BLOCK_SCHEDULING).setValue(false);
//            }
//        });
    }

    @Validate
    public boolean validate() throws Exception {
        return true;
    }

    @Submit
    public void submit() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("[SUBMIT] the user has completed step 3");
        }
    }
}
