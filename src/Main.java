
import classfile.Aron;
import classfile.FileWatcher;
import classfile.Print;
import classfile.Ut;
import com.google.common.base.Strings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.*;
//import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
class MyTextFlow extends TextFlow {
    //private final String fontFamily = "Helvetica";
    private final String fontFamily = "Courier New";
    private final double fontSize = 14;
    private DropShadow dropShadow;
    private javafx.scene.paint.Color textColor;
    private double preWidth;
    private double preHeight;
    private String setStyleStr;
    private final List<String> list;
    public MyTextFlow(List<String> list){
        this.list = list;
        init();
    }
    private void init(){
        dropShadow = new DropShadow();
        dropShadow.setOffsetX(2);
        dropShadow.setOffsetY(4);
        dropShadow.setColor(javafx.scene.paint.Color.GRAY);


        textColor = javafx.scene.paint.Color.BLACK;
        preWidth  = 1000;
        preHeight = 300;
        setStyleStr = "-fx-background-color: white;";
        setLineSpacing(4);
        setLayoutX(10);
        setLayoutY(10);
    }
    public MyTextFlow createTextFlow(){
        for(String s : list) {
            Text text = new Text(s + "\n");
            text.setFont(javafx.scene.text.Font.font(fontFamily, FontPosture.REGULAR, fontSize));
            text.setFill(textColor);
            text.setEffect(dropShadow);
            text.setLineSpacing(100);
            getChildren().add(text);
        }
        setPrefSize(preWidth, preHeight);
        setStyle(setStyleStr);

        return this;
    }
}

/**
 * convert a text file to a two dimension list
 * The first of the each list contains the key and file type are separated by colon
 */
final class ProcessList {
    private String fName;
    private Map<String, Set<String>> prefixSuffixMap = new HashMap<>();
    final Map<String, List<List<String>>> mapList = new HashMap<>();
    final Map<String, Set<String>> prefixFullKeyMap = new HashMap<>();
    Map<String, Set<List<String>>> prefixWordMap = new HashMap<>();
    final Map<String, Set<String>> wordsCompletion = new HashMap<>();

    public ProcessList(String fName) {
//        String fName = "/Users/cat/myfile/github/snippets/snippet_test.m";
        this.fName = fName;
        List<List<String>> list2d = readCodeFile(this.fName);
        buildAutoCompletionKeyCodeMap(list2d);
    }
    public ProcessList(List<String> listFile) {
//        String fName = "/Users/cat/myfile/github/snippets/snippet_test.m";

        List<List<String>> list2d = new ArrayList<>();

        for(String fName : listFile) {
            List<List<String>> lists1 = readCodeFile(fName);
            list2d = Aron.mergeLists(list2d, lists1);
        }
        buildAutoCompletionKeyCodeMap(list2d);
    }

    /**
     * read the contents of file and store it in a two dimension array
     * one or more empty lines separates each "block of code" in fileName
     *
     * @param fileName is name of file
     * @return a two dimension array contains the contents of fName
     *
     * Note: each List<String> contains one "block of code"
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

                    String abbreviation = splitKeys.get(0).toLowerCase().trim();
                    Print.pbl("key=" + abbreviation);

                    if(splitKeys.size() > 2) {
                        Map<String, Set<List<String>>> oneBlockMap = prefixWordMap(splitKeys.get(2).toLowerCase().trim(), list);
                        prefixWordMap = Aron.mergeMapSet(prefixWordMap, oneBlockMap);
                    }

                    listKeys.add(abbreviation);

                    // Given a string as abbreviation , generate all prefixes as keys,
                    // use abbreviation as value to create a map: mapList. <prefix, abbreviation>
                    //
                    // Example:
                    // key = "cmd"
                    // mapList = c->[cmd], cm->[cmd], cmd->[cmd]
                    //
                    for(int i=0; i<abbreviation.length(); i++){
                        String prefix = abbreviation.substring(0, i+1);
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

    /**
     * Example:
     * jlist_list : * : java list, java cool
     * my awesome code1
     *
     * str = "java list, java cool"
     *
     * listCode contains two lines
     *
     * @param str is used to generate prefixes
     * @param listCode contains (code or string), including the first line
     * @return map contains
     *          {"j" -> listCode.sublist[1, listCode.size()]}
     *          {"ja" -> listCode.sublist[1, listCode.size()]}
     *          {"jav" -> listCode.sublist[1, listCode.size()]}
     *          {"java" -> listCode.sublist[1, listCode.size()]}
     *          {"java " -> listCode.sublist[1, listCode.size()]}
     *          ...
     */
    private Map<String, Set<List<String>>> prefixWordMap(String str, List<String> listCode){
        Map<String, Set<List<String>>> mapSet = new HashMap<>();

        if(listCode.size() > 1) {
            List<String> list = Aron.splitTrim(str, ",");
            for (String words : list) {
                //
                // map: prefixes -> abbreviation
                // Example: keyWords= "vi cmd"
                // "v" -> "vi cmd"
                // "vi" -> "vi cmd"
                // "vi " -> "vi cmd"
                // "vi c" -> "vi cmd"
                // "vi cm" -> "vi cmd"
                // "vi cmd" -> "vi cmd"
                //
                for(int i=0; i<words.length(); i++){
                    String prefix = words.substring(0, i+1);
                    Set<String> value = wordsCompletion.get(prefix);
                    if(value != null ){
                        value.add(words);
                    }else{
                        Set<String> set = new HashSet<>();
                        set.add(words);
                        wordsCompletion.put(prefix, set);
                    }
                }

                List<String> listWord = Aron.split(words, "\\s+");
                String prefixKey = "";

                for(String word : listWord){
                    prefixKey = prefixKey + " " + word;
                    prefixKey = prefixKey.trim().toLowerCase();
                    Set<List<String>> value = mapSet.get(prefixKey);
                    if (value != null) {
                        value.add(listCode.subList(1, listCode.size()));

                        mapSet.put(prefixKey, value);
                    } else {
                        Set<List<String>> tmpSet = new HashSet<>();

                        tmpSet.add(listCode.subList(1, listCode.size()));
                        mapSet.put(prefixKey, tmpSet);

                    }
                    Print.pbl("------------------");
                }
                Print.pbl("==============");
            }
        }else{
            Print.pbl("ERROR: invalid file format. listCode.size()=" + listCode.size());
        }
        return mapSet;
    }
}


public class Main  extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    private List<String> configure(){
        return Arrays.asList(
                "/Users/cat/myfile/github/snippets/snippet.m",
                "/Users/cat/myfile/private/secret.m"
        );
    }

    @Override
    public void start(final Stage primaryStage) {
        final double lineHeight = 16.0;
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        final ProcessList[] processList = {new ProcessList(configure())};

        watchModifiedFile(processList);

        final ScrollPane scrollPane = new ScrollPane();
        final double WINDOW_WIDTH = 1000;
        final double WINDOW_HEIGHT = 800;
        final double comboboxWith = 300;
        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        gridpane.setHgap(40);
        gridpane.setVgap(2);

        final Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Error");

//        List<ScrollFreeTextArea> textAreaList = new ArrayList<>();
        List<TextArea> textAreaList = new ArrayList<>();


        final ComboBox<String> comboboxAbbreSearch = new ComboBox<>();
        comboboxAbbreSearch.setEditable(true);
        comboboxAbbreSearch.setPrefWidth(300);

        final ComboBox<String> comboboxKeyWordSearch = new ComboBox<>();
        comboboxKeyWordSearch.setEditable(true);
        comboboxKeyWordSearch.setPrefWidth(300);


        VBox vboxComboboxSearch = new VBox();
        vboxComboboxSearch.setAlignment(Pos.TOP_CENTER);
        vboxComboboxSearch.setSpacing(4);
        vboxComboboxSearch.getChildren().add(comboboxAbbreSearch);
        vboxComboboxSearch.getChildren().add(comboboxKeyWordSearch);

        VBox vboxTextFieldFile = new VBox();

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        vboxTextFieldFile.setSpacing(4);

        vboxTextFieldFile.setAlignment(Pos.TOP_CENTER);
        vboxTextFieldFile.setPadding(new Insets(1, 1, 10, 1));

        comboboxAbbreSearch.getSelectionModel().selectedItemProperty().addListener((obValue, previous, current) -> {
            Print.pbl("timetochange: current item:=" + comboboxAbbreSearch.getEditor().getText());
            Print.pbl("obValue=" + obValue + " previous=" + previous + " current=" + current);

            if(current != null && !Strings.isNullOrEmpty(current.trim())) {
                String inputKey = Aron.trimLeading(current);
                List<List<String>> lists = processList[0].mapList.get(Aron.trimLeading(inputKey));

                if(lists != null && lists.size() > 0) {
                    vboxTextFieldFile.getChildren().clear();
                    textAreaList.clear();

                    for (List<String> list : lists) {
                        MyTextFlow codeTextFlow = new MyTextFlow(list.subList(1, list.size()));
                        vboxTextFieldFile.getChildren().add(new FlowPane(codeTextFlow.createTextFlow()));

                        addFlowPaneToVBox(vboxTextFieldFile, list);

                        TextArea textArea = appendStringToTextAre(list.subList(0, list.size()));
                        //createListTextAreas(vboxTextFieldFile, textAreaList, textArea, lineHeight);
                    }
                    addContentToClipBoard(content, clipboard, lists);
                }
            }else{
                if(current == null){
                    Print.pbl("current is null");
                }else {
                    Print.pbl("current is not null");
                }
                Print.pbl("ERROR: current=" + current);
            }
        });

        comboboxKeyWordSearch.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> obValue, String previous, String current) -> {
            Print.pbl("timetochange: current item:=" + comboboxKeyWordSearch.getEditor().getText());
            Print.pbl("obValue=" + obValue + " previous=" + previous + " current=" + current);

            if(current != null && !Strings.isNullOrEmpty(current.trim())) {
                String inputKey = Aron.trimLeading(current).toLowerCase();
                Set<List<String>> setCode = processList[0].prefixWordMap.get(inputKey);
                if(setCode != null && setCode.size() > 0) {
                    vboxTextFieldFile.getChildren().clear();
                    //-----------------------------------------------------------------
                    // TODO: /Users/cat/myfile/github/java/TextAreaSimple
                    // TODO: change textflow here

                    textAreaList.clear();
                    for (List<String> list : setCode) {

                        // TODO: call method to pass list of string
//                            ScrollFreeTextArea textArea = new ScrollFreeTextArea();
                        TextArea textArea = new TextArea();
                        textArea.setFont(javafx.scene.text.Font.font ("Verdana", 20));

                        for(String word : list){
                            String line = word + "\n";
                            textArea.appendText(line);
                            Print.pbl("s=" + word);
                        }

                        // TODO: return textflow or flowpane here
                        vboxTextFieldFile.getChildren().add(textArea);
                        textAreaList.add(textArea);
                        int lineCount = textArea.getText().split("\n").length;
                        Print.pbl("lineCount=" + lineCount);
                        textArea.setPrefSize( Double.MAX_VALUE, lineHeight*(lineCount + 3) );
                    }
                    content.putString(textAreaList.get(0).getText());
                    clipboard.setContent(content);
                    //-----------------------------------------------------------------

                }
            }else{
                if(current == null){
                    Print.pbl("current is null");
                }else {
                    Print.pbl("current is not null");
                }
                Print.pbl("ERROR: current=" + current);
            }
        });

        comboboxKeyWordSearch.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            Print.pbl("KEY_PRESSED: KeyEvent       :=" + comboboxKeyWordSearch.getEditor().getText());

            if (event.getCode() == KeyCode.ENTER) {
                Print.pbl("ENTER KEY: selected item:=" + comboboxKeyWordSearch.getEditor().getText());
                comboboxKeyWordSearch.hide();
            }else if(event.getCode() == KeyCode.DOWN) {
                if(comboboxKeyWordSearch.getItems().size() > 0){
                    if(!comboboxKeyWordSearch.isShowing()){
                        comboboxKeyWordSearch.show();
                    }
                }else {
                    String prefix =  Aron.trimLeading(comboboxKeyWordSearch.getEditor().getText());
                    Print.pbl("DOWN KEY: selected item:=" + comboboxKeyWordSearch.getEditor().getText());

                    if (!Strings.isNullOrEmpty(prefix)) {
                        Print.pbl("prefix=" + prefix);
                        Set<String> setWords = processList[0].wordsCompletion.get(prefix);
                        if (setWords != null && setWords.size() > 0) {
                            comboboxKeyWordSearch.getItems().addAll(new ArrayList<>(setWords));
                            if (!comboboxKeyWordSearch.isShowing()) {
                                comboboxKeyWordSearch.show();
                            }
                        } else {
                            Print.pbl("prefix= is null or empty");
                        }
                    }
                }
            }else if(event.getCode() == KeyCode.SPACE) {
                Print.pbl("space bar");
            }else if(event.getCode() == KeyCode.RIGHT) {
                Print.pbl("right key");
            }else if(event.getCode() == KeyCode.LEFT) {
                Print.pbl("left key");
            }else if(event.getCode() == KeyCode.UP) {
                Print.pbl("up key");
            }else if(event.getCode() == KeyCode.TAB) {
                Print.pbl("tab key");
                clipboard.setContent(content);
            }else{
                Print.pbl("getEditor().getText()=" + comboboxKeyWordSearch.getEditor().getText());
                Print.pbl("      event.getText()=" + event.getText());
                Print.pbl(" event.getCharacter()=" + event.getCharacter());

                String input = comboboxKeyWordSearch.getEditor().getText() + event.getText();
                if (!Strings.isNullOrEmpty(input)) {
                    Print.pbl("input=" + input);
                    Set<String> setWords = processList[0].wordsCompletion.get(input);
                    if (setWords != null && setWords.size() > 0) {
                        comboboxKeyWordSearch.getItems().clear();
                        List<String> list = new ArrayList<>(setWords);
                        comboboxKeyWordSearch.getItems().addAll(list);
                        if (!comboboxKeyWordSearch.isShowing()) {
                            comboboxKeyWordSearch.show();
                        }
                    } else {
                        Print.pbl("input= is null or empty");
                        comboboxKeyWordSearch.getItems().clear();
                        comboboxKeyWordSearch.hide();
                    }
                }
            }
        });


        comboboxAbbreSearch.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            Print.pbl("KEY_PRESSED: KeyEvent       :=" + comboboxAbbreSearch.getEditor().getText());
            if (event.getCode() == KeyCode.ENTER) {
                Print.pbl("ENTER KEY: selected item:=" + comboboxAbbreSearch.getEditor().getText());
                comboboxAbbreSearch.hide();
            }else if(event.getCode() == KeyCode.DOWN) {
                if(comboboxAbbreSearch.getItems().size() > 0){
                    if(!comboboxAbbreSearch.isShowing()) {
                        comboboxAbbreSearch.show();
                    }
                }else {
                    String prefix =  Aron.trimLeading( comboboxAbbreSearch.getEditor().getText());
                    Print.pbl("DOWN KEY: selected item:=" + comboboxAbbreSearch.getEditor().getText());
                    Print.pbl("prefix  : selected item:=" + comboboxAbbreSearch.getEditor().getText());

                    if (!Strings.isNullOrEmpty(prefix)) {
                        Print.pbl("prefix=" + prefix);
                        Set<String> setWords = processList[0].prefixFullKeyMap.get(prefix);
                        if (setWords != null && setWords.size() > 0) {
                            List<String> list = new ArrayList<>(setWords);
                            comboboxAbbreSearch.getItems().addAll(list);
                            if (!comboboxAbbreSearch.isShowing()) {
                                comboboxAbbreSearch.show();
                            }
                        } else {
                            Print.pbl("prefix= is null or empty");
                        }
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
                clipboard.setContent(content);
            }else{
                Print.pbl("line 342");
                String input = comboboxAbbreSearch.getEditor().getText() + event.getText();
                if (!Strings.isNullOrEmpty(input)) {
                    Print.pbl("input=" + input);
                    Set<String> setWords = processList[0].prefixFullKeyMap.get(input);
                    if (setWords != null && setWords.size() > 0) {
                        comboboxAbbreSearch.getItems().clear();
                        List<String> list = new ArrayList<>(setWords);
                        comboboxAbbreSearch.getItems().addAll(list);
                        if (!comboboxAbbreSearch.isShowing()) {
                            comboboxAbbreSearch.show();
                        }
                    } else {
                        Print.pbl("input= is null or empty");
                        comboboxAbbreSearch.getItems().clear();
                        comboboxAbbreSearch.hide();
                    }
                }
            }
        });

        terminateProgram(comboboxAbbreSearch);
        terminateProgram(comboboxKeyWordSearch);

        gridpane.add(vboxComboboxSearch, 0, 0);

        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(WINDOW_WIDTH,  WINDOW_HEIGHT);
        scrollPane.setContent(vboxTextFieldFile);

        gridpane.add(scrollPane, 1, 0);
        Scene scene = new Scene(gridpane, comboboxWith + WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();


        //test2();
//      test5();
//        test6();
        //test7();
        test8();
    }
    public static void terminateProgram(Control control){
        final KeyCombination keyCombinationShiftC = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
        control.setOnKeyPressed(event -> {
            if (keyCombinationShiftC.match(event)) {
                Print.pbl("CTRL + C Pressed" + " hashCode=" + control.hashCode());

                // the line CAN NOT close all the running threads
                Platform.exit();

                // the line need to terminate all the running threads.
                System.exit(0);
            }
        });
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

    private void watchModifiedFile(ProcessList[] processList){
        TimerTask task = new FileWatcher( new File("/Users/cat/myfile/github/snippets/snippet.m") ) {
            protected void onChange( File file ) {
                System.out.println( "File="+ file.getAbsolutePath() +" have change !" );
                processList[0] = new ProcessList(configure());
            }
        };
        Timer timer = new Timer();
        timer.schedule( task , new Date(), 2000 );

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

    private TextArea appendStringToTextAre(List<String> list){
        TextArea textArea = new TextArea();
        textArea.setFont(Font.font ("Verdana", 20));
        for(String line : list){
            textArea.appendText(line + "\n");
            Print.pbl("s=" + line + "\n");
        }
        return textArea;
    }

//    final Clipboard clipboard = Clipboard.getSystemClipboard();
//    final ClipboardContent content = new ClipboardContent();

    /**
     * add the FIRST "code block" to clipboard including the header: e.g. jlist_file : * : java list
     *
     * @param content is the content of clipboard
     * @param clipboard contains the data that is copied
     * @param lists contains list of "code block"
     */
    private static  void addContentToClipBoard(ClipboardContent content, Clipboard clipboard, List<List<String>> lists){
        content.putString(Aron.listToStringNewLine(lists.get(0)));
        clipboard.setContent(content);
    }

    public static void createListTextAreas(VBox vbox, List<TextArea> textAreaList,  TextArea textArea, double lineHeight){
        int lineCount = textArea.getText().split("\n").length;
        Print.pbl("lineCount=" + lineCount);
        textArea.setPrefSize( Double.MAX_VALUE, lineHeight*(lineCount + 3) );
        textAreaList.add(textArea);
        vbox.getChildren().add(textArea);
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


    static void test0_splitImageList(){
        Aron.beg();
        String header =" img";
        List<String> imgList = splitImageStrLine(header);
        Aron.printList(imgList);

        Aron.end();
    }
    static void test1_splitImageList(){
        Aron.beg();
        String header =" img, file://dog.png, /dog/cat.png, http://dog.png";
        List<String> imgList = splitImageStrLine(header);
        Aron.printList(imgList);

        Aron.end();
    }
    static void test2_splitImageList(){
        Aron.beg();
        String header =" img, ";
        List<String> imgList = splitImageStrLine(header);
        Aron.printList(imgList);

        Aron.end();
    }

    /**
     * all the image files in one line,
     * e.g img, /dog.png, /cat.png
     *
     * parse lastLine string, create a list of ImageViews from the string if image files are found and add to VBox
     *
     * @param vbox contains list of ImageView objects
     * @param lastLine contains the file path to image file
     */
    private static void addFlowPaneToVBox(VBox vbox, String lastLine){
        List<String> list = splitImageStrLine(lastLine);
        List<ImageView> imageViewList = imageFileToImageView(list);
        for(ImageView iv : imageViewList) {
            BorderPane borderPane = new BorderPane();
            borderPane.setCenter(iv);
            vbox.getChildren().add(borderPane);
        }
    }

    /**
     * create FlowPane with ImageView object and add the FlowPane to VBox.
     *
     * each line contains ONLY one image file
     * e.g. img, /dog.png
     *
     * add ImageViews from different lines
     *
     * @param vbox contains list of ImageView objects
     * @param list contains the "code block"
     */
    private static void addFlowPaneToVBox(VBox vbox, List<String> list){
        // extract image file names in reverse order
        List<String> imgList = extractImageFiles(list);
        List<ImageView> imageViewList = imageFileToImageView(imgList);
        for(ImageView iv : imageViewList) {
            BorderPane borderPane = new BorderPane();
            borderPane.setCenter(iv);
            vbox.getChildren().add(borderPane);
        }
    }


    /**
     *  split images path/uri with (",")delimiter
     *
     * img, file://dog.png, /dog/cat.png, http://dog.png
     *
     * @param lastLine string in the last line of "block code"
     * @return a list contains all the image paths/URIs or an empty list
     */
    private static List<String> splitImageStrLine(String lastLine) {
        List<String> imgList = new ArrayList<>();
        List<String> list = Aron.splitTrim(lastLine, ",");
        if(list.size() > 0){
            if(list.get(0).equals("img")){
                imgList = list.subList(1, list.size());
            }
        }
        return imgList;
    }

    private static List<String> extractImageFiles(List<String> list) {
        List<String> imgList = new ArrayList<>();

        for(int i=list.size()-1; i>= 0; i--){
            List<String> ll = Aron.splitTrim(list.get(i), ",");
            if(ll.size() > 1 && ll.get(0).equals("img")){
                imgList.add(ll.get(1));
            }else{
                break;
            }
        }
        Collections.reverse(imgList);
        return imgList;
    }


    public void test0_imageFileToImageView(Stage stage){
        Aron.beg();

        final ScrollPane sp = new ScrollPane();
        final Image[] images = new Image[5];
        final ImageView[] pics = new ImageView[5];
        final VBox vb = new VBox();
        final Label fileName = new Label();
        final String [] imageNames = new String [] {
                "/Users/cat/try/draw10.png",
                "/Users/cat/try/draw11.png",
                "/Users/cat/try/draw12.png",
                "/Users/cat/try/draw13.png",
                "/Users/cat/try/draw14.png",
                "/Users/cat/try/draw15.png"
        };

        VBox box = new VBox();
        Scene scene = new Scene(box, 400, 400);
        stage.setScene(scene);
        stage.setTitle("Scroll Pane");
        box.getChildren().addAll(sp, fileName);
        VBox.setVgrow(sp, Priority.ALWAYS);

        fileName.setLayoutX(30);
        fileName.setLayoutY(160);

        List<ImageView> imageViewList = imageFileToImageView(Arrays.asList(imageNames));
        for(ImageView iv : imageViewList) {
            vb.getChildren().add(iv);
        }

        sp.setVmax(440);
        sp.setPrefSize(400, 400);
        sp.setContent(vb);
        sp.vvalueProperty().addListener((ov, old_val, new_val) -> fileName.setText(imageNames[(new_val.intValue() - 1)/100]));
        stage.show();

        Aron.end();
    }

    /**
     * read image files from input and create a list of ImageViews
     *
     * @param listImgNames is a list of image names
     * @return a list of ImageViews or an empty list
     */
    private static List<ImageView> imageFileToImageView(List<String> listImgNames){
        List<ImageView> imageList = new ArrayList<>();
        for (String imgPath : listImgNames) {
            //TODO: Add getResource to get the resources/images
            //images[i] = new Image(getClass().getResourceAsStream(imageNames[i]));
            //Print.pbl(images[i].toString());
            ImageView imageView = null;
            if(fileType(imgPath).equals("IMG")){
                imageView = new ImageView(new File(imgPath).toURI().toString());
            }else if(fileType(imgPath).equals("PDF")){
                try {
                    imageView = pdfToImage(imgPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(imageView != null) {
                imageList.add(imageView);
                imageView.setFitHeight(600);
                imageView.setFitWidth(600);
                imageView.setPreserveRatio(true);
                imageList.add(imageView);
            }
        }
        return imageList;
    }

    /**
     * detect file types from file extensions: image(.png, .jpeg, .jpg) and PDF(.pdf)
     *
     * @param fName is name of file.
     * @return image file: "IMG" or pdf file: "PDF", empty otherwise
     *
     */
    private static String fileType(String fName){
        String type = "";
        Pattern pdfPattern = Pattern.compile("\\.pdf$", Pattern.CASE_INSENSITIVE);
        Matcher pdfMatcher = pdfPattern.matcher(fName);

        Pattern pattern = Pattern.compile("\\.png|\\.jpeg|\\.jpg$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(fName);

        if(matcher.find()){
            Print.pbl("fName=" + fName);
            type = "IMG";
        }else if(pdfMatcher.find()){
            Print.pbl("fName=" + fName);
            type = "PDF";
        }
        return type;
    }

    /**
     * Convert PDF file to ImageView
     *
     * @param fName is name of PDF file
     * @return ImageView for the PDF file
     * @throws Exception
     */
    private static ImageView pdfToImage(String fName) throws Exception{
        int pdfScale = 1;
        File file = new File(fName);
        PDDocument doc = PDDocument.load(file);
        if(doc == null){
            Print.pbl("doc is null");
        }

        PDFRenderer renderer = new PDFRenderer(doc);
        BufferedImage img = renderer.renderImage(0, pdfScale);
        WritableImage fxImage = SwingFXUtils.toFXImage(img, null);
        return new ImageView(fxImage);
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
        List<String> list = new ArrayList<>();
        List<String> fontsList = javafx.scene.text.Font.getFamilies();
        Aron.printList(fontsList);

        Print.pbl(b);
    }
}
