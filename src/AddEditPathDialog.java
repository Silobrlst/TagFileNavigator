import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AddEditPathDialog {
    private boolean editing;

    private Path editingPath;

    private List<Tag> tagsTemp;
    private FileChooser fileChooser;
    private DirectoryChooser directoryChooser;
    private File initialDirectory;

    @FXML
    private ListView<String> availableTags;
    @FXML
    private ListView<String> addedTags;
    @FXML
    private Button ok;
    @FXML
    private Button cancel;
    @FXML
    private Button exploreFile;
    @FXML
    private Button exploreDirectory;
    @FXML
    private Button addTags;
    @FXML
    private Button removeTags;
    @FXML
    private Button apply;
    @FXML
    private Label pathValidation;
    @FXML
    private TextField path;

    //<string names>======================
    private static final String settingsWindow = "settingsWindow";
    private static final String x = "x";
    private static final String y = "y";
    private static final String width = "width";
    private static final String height = "height";
    private static final String exploreCurrentDirectory = "exploreCurrentDirectory";
    private static final File guiSettings = new File("guiSettings.json");
    //</string names>=====================

    private Paths paths;

    private Tags tags;

    private Stage stage;

    @FXML
    public void initialize() {
        ok.setOnAction(event -> onOK());

        cancel.setOnAction(event -> onCancel());

        exploreFile.setOnAction(event -> {
            File file = fileChooser.showOpenDialog(stage);
            if(file != null){
                path.setText(file.getAbsolutePath());
                pathValidation.setText("");
                initialDirectory = new File(file.getAbsolutePath().split("[/\\\\][^\\\\/]*$")[0]);
            }
        });

        exploreDirectory.setOnAction(event -> {
            File file = directoryChooser.showDialog(stage);
            if(file != null){
                path.setText(file.getAbsolutePath());
                pathValidation.setText("");
                initialDirectory = new File(file.getAbsolutePath().split("[/\\\\][^\\\\/]*$")[0]);
            }
        });

        addTags.setOnAction(event -> {
            addedTags.getItems().addAll(availableTags.getSelectionModel().getSelectedItems());
            availableTags.getItems().removeAll(availableTags.getSelectionModel().getSelectedItems());
        });

        removeTags.setOnAction(event -> {
            availableTags.getItems().addAll(addedTags.getSelectionModel().getSelectedItems());
            addedTags.getItems().removeAll(addedTags.getSelectionModel().getSelectedItems());
        });

        availableTags.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        addedTags.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void setPathsTagsParent(Stage parentStageIn, Stage stageIn, Paths pathsIn, Tags tagsIn){
        paths = pathsIn;
        tags = tagsIn;

        stage = stageIn;

        stage.initOwner(parentStageIn);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setOnHidden(event -> saveGuiSettings());
        stage.setOnCloseRequest(event -> saveGuiSettings());

        stage.setOnShown(event -> {
            getTags(tagsIn);
            availableTags.getItems().clear();
            availableTags.getItems().addAll(getTagIds(getTags(tagsIn)));

            if(editing){
                addedTags.getItems().clear();
                addedTags.getItems().addAll(getTagIds(tagsTemp));
                availableTags.getItems().removeAll(addedTags.getItems());
            }else{
                path.setText("");
                addedTags.getItems().clear();

                if(tagsTemp != null){
                    addedTags.getItems().addAll(getTagIds(tagsTemp));
                    availableTags.getItems().removeAll(addedTags.getItems());
                }
            }

            path.requestFocus();
        });

        loadGuiSettings();

        setAddPath();
    }

    public AddEditPathDialog()throws Exception{
        fileChooser = new FileChooser();
        directoryChooser = new DirectoryChooser();
        initialDirectory = new File(System.getProperty("user.dir"));
    }

    private List<String> getTagIds(List<Tag> tagsIn){
        ArrayList<String> ids = new ArrayList<>();
        for(Tag tag: tagsIn){
            if(tag != tags){
                ids.add(tags.getTagId(tag));
            }
        }
        return ids;
    }

    private List<Tag> getTagsByIds(List<String> idsIn){
        ArrayList<Tag> tags_ = new ArrayList<>();

        for(String id: idsIn){
            tags_.add(tags.getTagById(id));
        }

        return tags_;
    }

    private void getTags(Tag parentIn, List<Tag> tagsOut){
        if(parentIn != tags){
            tagsOut.add(parentIn);
        }
        for(Tag tag: parentIn.getChildren()){
            getTags(tag, tagsOut);
        }
    }

    private List<Tag> getTags(Tag parentIn){
        ArrayList<Tag> allTags = new ArrayList<>();
        getTags(parentIn, allTags);
        return allTags;
    }

    //<GUI settings i/o>======================
    private void validateGuiSettings(JSONObject jsonIn) {
        if (!jsonIn.has(settingsWindow)) {
            jsonIn.put(settingsWindow, new JSONObject());
        }

        JSONObject settingsWindowJSON = jsonIn.getJSONObject(settingsWindow);
        if (!settingsWindowJSON.has(x)) {
            settingsWindowJSON.put(x, 0.d);
        }
        if (!settingsWindowJSON.has(x)) {
            settingsWindowJSON.put(y, 0.d);
        }
        if (!settingsWindowJSON.has(width)) {
            settingsWindowJSON.put(width, 0.d);
        }
        if (!settingsWindowJSON.has(height)) {
            settingsWindowJSON.put(height, 0.d);
        }
        if (!settingsWindowJSON.has(exploreCurrentDirectory)) {
            settingsWindowJSON.put(exploreCurrentDirectory, initialDirectory.getAbsolutePath());
        }
    }

    private void loadGuiSettings() {
        JSONObject guiJSON = JSONLoader.loadJSON(guiSettings);
        validateGuiSettings(guiJSON);

        JSONObject mainWindowJSON = guiJSON.getJSONObject(settingsWindow);
        if (mainWindowJSON.getDouble(width) > 0) {
            stage.setWidth(mainWindowJSON.getDouble(width));
        }
        if (mainWindowJSON.getDouble(height) > 0) {
            stage.setHeight(mainWindowJSON.getDouble(height));
        }

        new Timeline(new KeyFrame(Duration.millis(1000), e -> {
            if (mainWindowJSON.getString(exploreCurrentDirectory).length() > 0) {
                File file = new File(mainWindowJSON.getString(exploreCurrentDirectory));
                fileChooser.setInitialDirectory(file);
                directoryChooser.setInitialDirectory(file);
                initialDirectory = file;
            }
        })).play();
    }

    private void saveGuiSettings() {
        JSONObject guiJSON = new JSONObject();
        validateGuiSettings(guiJSON);

        JSONObject settingsWindowJSON = guiJSON.getJSONObject(settingsWindow);
        settingsWindowJSON.put(x, stage.getX());
        settingsWindowJSON.put(y, stage.getY());
        settingsWindowJSON.put(width, stage.getWidth());
        settingsWindowJSON.put(height, stage.getHeight());
        settingsWindowJSON.put(exploreCurrentDirectory, initialDirectory.getAbsolutePath());

        JSONLoader.saveJSON(guiSettings, guiJSON);
    }
    //</GUI settings i/o>=====================

    public void setAddPath(List<Tag> tagsIn){
        stage.setTitle("Add Path");
        apply.setDisable(true);
        editing = false;

        tagsTemp = tagsIn;
    }

    public void setAddPath(){
        setAddPath(null);
    }

    public void setEditPath(Path pathIn){
        stage.setTitle("Edit Path");
        apply.setDisable(false);
        editing = true;
        editingPath = pathIn;
        path.setText(editingPath.getPath());
        tagsTemp = pathIn.getTags();
    }

    private void onOK() {
        if(new File(path.getText()).exists()){
            if(editing){
                editingPath.setPath(path.getText());
                editingPath.addTags(getTagsByIds(addedTags.getItems()));
            }else{
                Path newPath = paths.newPath(path.getText());
                newPath.addTags(getTagsByIds(addedTags.getItems()));
            }

            stage.hide();
        }else{
            pathValidation.setText("Path does not exists");
        }
    }

    private void onCancel() {
        // add your code here if necessary
        stage.hide();
    }

    public void open(){
        stage.showAndWait();
    }
}
