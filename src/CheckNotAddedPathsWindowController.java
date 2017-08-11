import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public class CheckNotAddedPathsWindowController {
    @FXML
    private TextField folder;
    @FXML
    private Label validation;
    @FXML
    private Button explore;
    @FXML
    private Button checkNotAddedPaths;
    @FXML
    private TextArea pathsList;
    @FXML
    private Label resultLabel;

    private Stage stage;
    private SavableStyledGui savableStyledGui;

    private Paths paths;

    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private File initialDirectory = new File(System.getProperty("user.dir"));

    @FXML
    public void initialize() {}

    public CheckNotAddedPathsWindowController(){}

    public void init(FXMLLoader loaderIn, Paths pathsIn){
        paths = pathsIn;

        String windowName = "checkNotAddedPathsDialog";

        Scene scene = new Scene(loaderIn.getRoot());
        scene.getRoot().setId(windowName + "Root");
        stage = new Stage();
        stage.setTitle("Check not added paths");
        stage.setScene(scene);
        stage.setOnShown(event -> onShown());
        stage.setOnHidden(event -> savableStyledGui.save());
        stage.getScene().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                stage.hide();
            }
        });

        checkNotAddedPaths.setOnAction(event -> onCheckNotAddedPaths());

        folder.setOnAction(event -> onCheckNotAddedPaths());

        explore.setOnAction(event -> {
            File file = directoryChooser.showDialog(stage);
            if(file != null){
                folder.setText(file.getAbsolutePath());
                validation.setText("");
                initialDirectory = new File(file.getAbsolutePath().split("[/\\\\][^\\\\/]*$")[0]);
            }
        });

        savableStyledGui = new SavableStyledGui(windowName, stage);
        savableStyledGui.load();
    }

    private void onShown(){
        validation.setText("");
        folder.setText("");
        pathsList.setText("");
        resultLabel.setText("");
    }

    private void onCheckNotAddedPaths(){
        File folderFile = new File(folder.getText());
        if(folderFile.exists()){
            validation.setText("");

            File[] listOfFiles = folderFile.listFiles();

            String pathsListText = "";

            ArrayList<String> pathsStr = new ArrayList<>();
            for (int i = 0; i < listOfFiles.length; i++) {
                pathsStr.add(listOfFiles[i].getAbsolutePath());
            }
            pathsStr.sort(Comparator.naturalOrder());

            for(String path: pathsStr){
                if(!paths.checkPathExist(path)){
                    pathsListText += path + "\n";
                }
            }

            resultLabel.setText("paths count: " + pathsStr.size());

            pathsList.setText(pathsListText);
        }else{
            validation.setText("Folder does not exist");
        }
    }

    public void setStyle(String styleFileNameIn){
        savableStyledGui.setStyle(styleFileNameIn);
    }

    public void open(){
        stage.show();
    }

    public void close(){
        stage.hide();
    }
}