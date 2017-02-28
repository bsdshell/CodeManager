import classfile.Aron;
import classfile.Print;
import classfile.Ut;
import com.google.common.base.Strings;
import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;
import static java.awt.Event.ENTER;


/**
 * convert a text file to a two dimension list
 * The first of the each list contains the key and file type are separated by colon
 */
final class ProcessList {
    private String fName;
    Map<String, Set<String>> prefixSuffixMap = new HashMap<>();
    Map<String, List<List<String>>> mapList = new HashMap<>();
    Map<String, Set<String>> prefixFullKeyMap = new HashMap<>();



    public ProcessList(String fName) {
//        String fName = "/Users/cat/myfile/github/snippets/snippet_test.m";
        this.fName = fName;
        List<List<String>> list2d = readCodeFile(fName);
        buildAutoCompletionKeyCodeMap(list2d);
    }

    // return map that contains key and value [K, v], key is prefix of string before ":"
    // value is List<String> contains all code after the first line
    public Map<String, List<String>> getPrefixMap(){
        return null;
    }

    /**
     * read the contents of file and store it in a two dimension array
     *
     * @param fileName is name of file
     * @return a two dimension array contains the contents of fName
     */
    private List<List<String>> readCodeFile(String fileName){
        List<String> list = Aron.readFileWithWhiteSpace(fileName);
        List<List<String>> lists = new ArrayList<>();

        List<String> line = new ArrayList<>();
        for(String s : list){

            if(s.trim().length() > 0){
                line.add(s);
            }else{
                if(line.size() > 0) {
                    lists.add(line);
                    line = new ArrayList<>();
                }
            }
        }
        return lists;
    }

    /**
     *
     *
     * @param lists contains the contents of file
     *
     * [dog : *.java
     * my code]
     *
     * mapList contains following
     * d ->[....]
     * do ->[...]
     * dog ->[...]
     */
    private void buildAutoCompletionKeyCodeMap(List<List<String>> lists){
        List<String> listKeys = new ArrayList<>();
        for(List<String> list : lists){
            if(list.size() > 0){
                List<String> splitKeys = Aron.split(list.get(0), ":");
                if(splitKeys.size() > 0){

                    String key = splitKeys.get(0).trim();
                    Print.pbl("key=" + key);

                    listKeys.add(key);

                    for(int i=0; i<key.length(); i++){
                        String prefix = key.substring(0, i+1);
                        Print.pbl("prefix=" + prefix);
                        List<List<String>> values = mapList.get(prefix);

                        if(values != null){
                            values.add(list);
                        }else{
                            List<List<String>> tmpLists = new ArrayList<>();
                            tmpLists.add(list);
                            mapList.put(prefix, tmpLists);
                        }
                    }
                }
            }
        }
        prefixSuffixMap = buildPrefixMap(listKeys);
        buildFullKeyMap(prefixSuffixMap);

    }

    private void buildFullKeyMap(Map<String, Set<String>> map){
        Set<String> set = new HashSet<>();
        for(Map.Entry<String, Set<String>> entry : map.entrySet()){
            for(String s : entry.getValue()) {
                set.add(entry.getKey() + s);
            }
            prefixFullKeyMap.put(entry.getKey(), set);
            set = new HashSet<>();
        }
    }

    /**
     * The method splits the "search key" to prefix and suffix, and store
     * the prefix as key and the suffix as value in HashSet
     *
     * @param list is list of string that contains all the search string
     *
     * @return a map contains key which is prefix of the "search string" and
     *          value which is suffix of "search string".
     */
    private Map<String, Set<String>> buildPrefixMap(List<String> list){
        Map<String, Set<String>> map = new HashMap<>();
        for(String str : list) {
            for (int i = 0; i < str.length() - 1; i++) {
                String prefix = str.substring(0, i + 1);
                String suffix = str.substring(i + 1, str.length());
                Set<String> set = map.get(prefix);
                if (set == null)
                    set = new HashSet<>();

                set.add(suffix);
                map.put(prefix, set);
            }
        }
        return map;
    }
}


public class Main  extends Application {
    private AutoCompletionBinding<String> autoCompletionBinding;
    //private final String allPathsFileName = "/Users/cat/myfile/github/java/text/path.txt";
    //private final String fName = "/Users/cat/myfile/github/snippets/snippet_test.m";
    private final String fName = "/Users/cat/myfile/github/snippets/snippet.m";
    private ObservableList<String> data = FXCollections.observableArrayList();
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) {
        final KeyCombination keyCombinationShiftC = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
        final BooleanProperty spacePressed = new SimpleBooleanProperty(false);
        final BooleanProperty rightPressed = new SimpleBooleanProperty(false);
        final BooleanBinding spaceAndRightPressed = spacePressed.and(rightPressed);

        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
//        content.putString("Some text");
//        content.putHtml("<b>Some</b> text");
//        clipboard.setContent(content);

        final ProcessList processList = new ProcessList(fName);
        List<List<String>> list2d = readCode(fName);
        Group root = new Group();

        final double textFieldWidth = 600;
        final double textFieldHeight = 600;
        final double comboboxWith = 300;
        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(40);
        gridpane.setVgap(2);

        final Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Error");

        final TextArea textAreaFile = new TextArea();
        textAreaFile.setPrefWidth(textFieldWidth);

        final ComboBox<String> comboboxSearch = new ComboBox<>();
        comboboxSearch.setEditable(true);
        comboboxSearch.setPrefWidth(300);

        VBox vboxComboboxSearch = new VBox();
        vboxComboboxSearch.setAlignment(Pos.TOP_CENTER);
        vboxComboboxSearch.setSpacing(10);
        vboxComboboxSearch.getChildren().add(comboboxSearch);

        VBox vboxTextFieldFile = new VBox();
        VBox.setVgrow(textAreaFile, Priority.ALWAYS);

        vboxTextFieldFile.setPrefWidth(textFieldWidth);
        vboxTextFieldFile.setPrefHeight(textFieldHeight);

        vboxTextFieldFile.setAlignment(Pos.TOP_CENTER);
        vboxTextFieldFile.setPadding(new Insets(1, 1, 1, 1));
        vboxTextFieldFile.getChildren().add(textAreaFile);

        comboboxSearch.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue obValue, Object previous, Object current) {
                Print.pbl("Time to change!");
                Print.pbl("timetochange: current item:=" + comboboxSearch.getEditor().getText());
                Print.pbl("obValue=" + obValue);
                Print.pbl("previous=" + previous);
                Print.pbl("current=" + current);
                Print.pbl("Time to change2!");

                if(current != null && !Strings.isNullOrEmpty((String)current)) {
                    List<List<String>> lists = processList.mapList.get(current);
                    if(lists != null && lists.size() > 0) {
                        textAreaFile.clear();
                        textAreaFile.appendText("----------------------------------\n");
                        for (List<String> list : lists) {
                            for (String s : list) {
                                textAreaFile.appendText(s + "\n");
                                Print.pbl("s=" + s);
                            }
                            textAreaFile.appendText("----------------------------------\n");
                        }
                    }
                }else{
                    if(current == null){
                        Print.pbl("current is null");
                    }else {
                        Print.pbl("current is not null");
                    }
                    Print.pbl("ERROR: current=" + current);
                }
            }
        });


        comboboxSearch.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            Print.pbl("KEY_PRESSED: KeyEvent       :=" + comboboxSearch.getEditor().getText());


            if (event.getCode() == KeyCode.ENTER) {
                Print.pbl("ENTER KEY: selected item:=" + comboboxSearch.getEditor().getText());
                comboboxSearch.getItems().clear();
                comboboxSearch.hide();
            }else if(event.getCode() == KeyCode.DOWN) {
                String prefix = comboboxSearch.getEditor().getText();
                Print.pbl("DOWN KEY: selected item:=" + comboboxSearch.getEditor().getText());

                if (!Strings.isNullOrEmpty(prefix)) {
                    Print.pbl("prefix=" + prefix);
                    Set<String> setWords = processList.prefixFullKeyMap.get(prefix);
                    if (setWords != null && setWords.size() > 0) {
                        comboboxSearch.getItems().addAll(new ArrayList(setWords));
                        if (!comboboxSearch.isShowing()) {
                            comboboxSearch.show();
                        }
                    } else {
                        Print.pbl("prefix= is null or empty");
                    }
                }
            }else if(event.getCode() == KeyCode.RIGHT) {
                Print.pbl("right key");
            }else if(event.getCode() == KeyCode.LEFT) {
                Print.pbl("left key");
            }else if(event.getCode() == KeyCode.UP) {
                Print.pbl("up key");
            }else if(event.getCode() == KeyCode.TAB) {
                Print.pbl("tab key");
                content.putString(textAreaFile.getText());
                clipboard.setContent(content);
            }else{
                Print.pbl("line 342");

                //  input = dog = do + g
                String input = comboboxSearch.getEditor().getText() + event.getText();
                if (!Strings.isNullOrEmpty(input)) {
                    Print.pbl("input=" + input);
                    Set<String> setWords = processList.prefixFullKeyMap.get(input);
                    if (setWords != null && setWords.size() > 0) {
                        comboboxSearch.getItems().clear();
                        comboboxSearch.getItems().addAll(new ArrayList(setWords));
                        if (!comboboxSearch.isShowing()) {
                            comboboxSearch.show();
                        }
                    } else {
                        Print.pbl("input= is null or empty");
                        comboboxSearch.getItems().clear();
                        comboboxSearch.hide();
                    }
                }
            }
        });

        comboboxSearch.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (keyCombinationShiftC.match(event)) {

                    Print.pbl("CTRL + C Pressed");

                }
            }
        });


        gridpane.add(vboxComboboxSearch, 0, 0);
        gridpane.add(vboxTextFieldFile, 1, 0);

        Scene scene = new Scene(gridpane, comboboxWith + textFieldWidth, textFieldHeight);
        primaryStage.setScene(scene);
        primaryStage.show();

        //test2();
//      test5();
//        test6();
        //test7();
        test8();
    }

    public static  List<String> fileSearch(List<String> list, String pattern){
        Map<String, String> map = new HashMap<>();
        Pattern pat = Pattern.compile(pattern);
        List<String> matchList = new ArrayList<>();
        for(String s : list){
            Path path = Paths.get(s);
            Matcher match = pat.matcher(path.getFileName().toString());
            Print.pbl("fname=" + path.getFileName().toString());
            if(match.find()){
                Print.pbl(s);
                matchList.add(s);
            }
        }
        return matchList;
    }

    public static Map<String, List<List<String>>> buildAutoCompletionMap(List<List<String>> lists){
        for(List<String> list : lists){
            if(list.size() > 0){
                List<String> listToken = Aron.split(list.get(0), ":");
            }
        }
        return null;
    }



    /**
     * read the contents of file and store it in a two dimension array
     *
     * @param fName is name of file
     * @return a two dimension array contains the contents of fName
     */
    private static List<List<String>> readCode(String fName){
        final int MaxBuf = 200;
        List<String> list = Aron.readFileLineByte(fName, MaxBuf);
        List<List<String>> list2d = new ArrayList<>();


        List<String> line = new ArrayList<>();
        for(String s : list){

            if(s.trim().length() > 0){
                line.add(s);
            }else{
                if(line.size() > 0) {
                    list2d.add(line);
                    line = new ArrayList<>();
                }
            }
        }
        return list2d;
    }

    /**
     * The method open the given directory and add all files and directoreis
     * to a list.
     *
     * @param directory is to be read
     * @return a list of files or directories in the given directory.
     */
    private static List<String> fileList(String directory) {
        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
            for (Path path : directoryStream) {
                fileNames.add(path.getFileName().toString());
                //Print.pbl(path.getFileName().toString());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return fileNames;
    }

    /**
     * The method splits "search key" to prefix and suffix, and store
     * the prefix as key and the suffix as value in HashMap
     *
     * @param list is list of string that contains all the search string
     * @return a map contains key which is prefix of the "search string" and
     *          value which is suffix of "search string".
     */
    private static Map<String, Set<String>> buildPrefixMap(List<String> list){
        Map<String, Set<String>> map = new HashMap<>();

        for(String str : list) {
            for (int i = 0; i < str.length() - 1; i++) {
                String prefix = str.substring(0, i + 1);
                String suffix = str.substring(i + 1, str.length());
                Set<String> set = map.get(prefix);
                if (set == null)
                    set = new HashSet<>();

                set.add(suffix);
                map.put(prefix, set);
            }
        }

        return map;
    }

    /**
     * Test the getCurrentDir method
     */
    private static  void test1() {
        String dir = "/Users/cat/myfile/github/java";
        List<String> list = Aron.getCurrentDir(dir);
        Aron.printList(list);
    }

    /**
     * Test fileList method
     */
    private static  void test2() {
        String dir = "/Users/cat/myfile/github/java";
        List<String> list = fileList(dir);
        Aron.printList(list);
    }

    private static void test5(){
        String str = "Negotiable";
        Map<String, List<String>> map = new HashMap<>();
        for(int i=0; i<str.length(); i++){
            String prefix = str.substring(0, i+1);
            List<String> list = map.get(prefix);
            if(list != null){
                list.add("dog");
                map.put(prefix, list);
            }else{
                List<String> newList = new ArrayList<>();
                newList.add("cat");
                map.put(prefix, newList);
            }
            Print.pbl(prefix);
        }
    }

    private static void test6() {

        String line = "0123456789";
        for(int i=0; i<line.length(); i++){
            String prefix = line.substring(0, i);
            String suffix = line.substring(i, line.length());
            Print.pbl("prefix=" + prefix + " suffix=" + suffix);
        }
        // mutable list
        List<String> list = new ArrayList<>(Arrays.asList("cat", "dog", "cow"));
        for(int i=0; i<list.size() - 1; i++){
            List<String> preList = list.subList(0, i+1);
            List<String> subList = list.subList(i+1, list.size());
            Ut.l();
            Print.pbl(list.get(i));
            Aron.printList(subList);
        }
    }

    private static  void test7(){
        List<String> list = new ArrayList<>();

        Map<String, Set<String>> map = buildPrefixMap(list);
    }

    private static  void test8(){
        String str = "";
        boolean b = Strings.isNullOrEmpty(str);
        Print.pbl(b);
    }
}
