import classfile.Aron;
import classfile.Print;
import classfile.Ut;
import com.google.common.base.Strings;
import com.sun.javafx.tk.*;
import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import jdk.nashorn.internal.ir.LiteralNode;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import javax.xml.soap.Text;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;
import static java.awt.Event.ENTER;


//class ScrollFreeTextArea extends StackPane {
//
//    private Label label;
//    private TextArea textArea;
//    private Character enterChar = new Character((char) 10);
//    private Region content;
//    private SimpleDoubleProperty contentHeight = new SimpleDoubleProperty();
//
//    private final double NEW_LINE_HEIGHT = 18D;
//    private final double TOP_PADDING = 3D;
//    private final double BOTTOM_PADDING = 6D;
//
//    public ScrollFreeTextArea() {
//        super();
//        configure();
//    }
//
//    private void configure() {
//        setAlignment(Pos.TOP_LEFT);
//
//        this.textArea = new TextArea() {
//            @Override
//            protected void layoutChildren() {
//                super.layoutChildren();
//                if (content == null) {
//                    content = (Region) lookup(".content");
//                    contentHeight.bind(content.heightProperty());
//                    content.heightProperty().addListener(new ChangeListener<Number>() {
//                        @Override
//                        public void changed(ObservableValue<? extends Number> paramObservableValue, Number paramT1, Number paramT2) {
//                            //System.out.println("Content View Height :"+paramT2.doubleValue());
//                        }
//                    });
//                }
//            };
//        };
//        this.textArea.setWrapText(true);
//
//        this.label = new Label();
//        this.label.setWrapText(true);
//        this.label.prefWidthProperty().bind(this.textArea.widthProperty());
//        label.textProperty().bind(new StringBinding() {
//            {
//                bind(textArea.textProperty());
//            }
//            @Override
//            protected String computeValue() {
//                if (textArea.getText() != null && textArea.getText().length() > 0) {
//                    if (!((Character)textArea.getText().charAt(textArea.getText().length() - 1)).equals(enterChar)) {
//                        return textArea.getText() + enterChar;
//                    }
//                }
//                return textArea.getText();
//            }
//        });
//
//        StackPane lblContainer = StackPaneBuilder.create()
//                .alignment(Pos.TOP_LEFT)
//                .padding(new Insets(4, 7, 7, 7))
//                .children(label)
//                .build();
//        // Binding the container width/height to the TextArea width.
//        lblContainer.maxWidthProperty().bind(textArea.widthProperty());
//
//        textArea.textProperty().addListener(new ChangeListener<String>() {
//            @Override
//            public void changed(ObservableValue<? extends String> paramObservableValue,	String paramT1, String value) {
//                layoutForNewLine(textArea.getText());
//            }
//        });
//
//        label.heightProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> paramObservableValue,	Number paramT1, Number paramT2) {
//                layoutForNewLine(textArea.getText());
//            }
//        });
//
//        getChildren().addAll(lblContainer, textArea);
//    }
//
//    private void layoutForNewLine(String text){
//        if (text != null && text.length() > 0 && ((Character)text.charAt(text.length() - 1)).equals(enterChar)) {
//            textArea.setPrefHeight(label.getHeight() + NEW_LINE_HEIGHT + TOP_PADDING + BOTTOM_PADDING);
//            textArea.setMinHeight(textArea.getPrefHeight());
//        }
//        else {
//            textArea.setPrefHeight(label.getHeight() + TOP_PADDING + BOTTOM_PADDING);
//            textArea.setMinHeight(textArea.getPrefHeight());
//        }
//    }
//
//    public TextArea getTextArea() {
//        return textArea;
//    }
//}

class MyTextFlow extends TextFlow {
    private String fontFamily = "Helvetica";
    private double fontSize = 14;
    private DropShadow dropShadow;
    private javafx.scene.paint.Color textColor;
    private double preWidth;
    private double preHeight;
    private String setStyleStr;
    private List<String> list;
    public MyTextFlow(List<String> list){
        this.list = list;
        init();
    }
    private void init(){
        dropShadow = new DropShadow();
        dropShadow.setOffsetX(4);
        dropShadow.setOffsetY(6);
        dropShadow.setColor(javafx.scene.paint.Color.BLACK);

        this.setStyle("-fx-background-color: gray;");

        textColor = javafx.scene.paint.Color.BLACK;
        preWidth  = 100;
        preHeight = 100;
        setStyleStr = "-fx-background-color: cyan;";
    }
    public MyTextFlow createTextFlow(){
        for(String s : list) {
            javafx.scene.text.Text text = new javafx.scene.text.Text(s);
            text.setFont(javafx.scene.text.Font.font(fontFamily, FontPosture.REGULAR, fontSize));
            text.setFill(textColor);
            text.setEffect(dropShadow);
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
    Map<String, List<List<String>>> mapList = new HashMap<>();
    Map<String, Set<String>> prefixFullKeyMap = new HashMap<>();
    Map<String, Set<List<String>>> prefixWordMap = new HashMap<>();
    Map<String, Set<String>> wordsCompletion = new HashMap<>();

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

                    String abbreviation = splitKeys.get(0).trim();
                    Print.pbl("key=" + abbreviation);

                    if(splitKeys.size() > 2) {
                        Map<String, Set<List<String>>> oneBlockMap = prefixWordMap(splitKeys.get(2).trim(), list);
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
                    prefixKey = prefixKey.trim();
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

    public List<String> mergeList(List<String> list1, List<String> list2){
        Set<String> set = new HashSet<>();
        for(String s : list1)
            set.add(s);
        for(String s : list2)
            set.add(s);

        return new ArrayList<>(set);
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
        final double lineHeight = 16.0;
        final KeyCombination keyCombinationShiftC = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
        final BooleanProperty spacePressed = new SimpleBooleanProperty(false);
        final BooleanProperty rightPressed = new SimpleBooleanProperty(false);
        final BooleanBinding spaceAndRightPressed = spacePressed.and(rightPressed);

        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();

        final ProcessList processList = new ProcessList(fName);
        List<List<String>> list2d = readCode(fName);
        Group root = new Group();

        final ScrollPane scrollPane = new ScrollPane();
        final double WINDOW_WIDTH = 1000;
        final double WINDOW_HEIGHT = 800;
        final double textFieldWidth = 600;
        final double textFieldHeight = 600;
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


        final ComboBox<String> comboboxSearch = new ComboBox<>();
        comboboxSearch.setEditable(true);
        comboboxSearch.setPrefWidth(300);

        final ComboBox<String> comboboxKeyWord = new ComboBox<>();
        comboboxKeyWord.setEditable(true);
        comboboxKeyWord.setPrefWidth(300);


        VBox vboxComboboxSearch = new VBox();
        vboxComboboxSearch.setAlignment(Pos.TOP_CENTER);
        vboxComboboxSearch.setSpacing(4);
        vboxComboboxSearch.getChildren().add(comboboxSearch);
        vboxComboboxSearch.getChildren().add(comboboxKeyWord);

        VBox vboxTextFieldFile = new VBox();

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        vboxTextFieldFile.setSpacing(4);

        vboxTextFieldFile.setAlignment(Pos.TOP_CENTER);
        vboxTextFieldFile.setPadding(new Insets(1, 1, 10, 1));





        comboboxSearch.getSelectionModel().selectedItemProperty().addListener((obValue, previous, current) -> {
            Print.pbl("timetochange: current item:=" + comboboxSearch.getEditor().getText());
            Print.pbl("obValue=" + obValue + " previous=" + previous + " current=" + current);


            if(current != null && !Strings.isNullOrEmpty(current)) {
                List<List<String>> lists = processList.mapList.get(current);
                if(lists != null && lists.size() > 0) {
                    vboxTextFieldFile.getChildren().clear();
                    textAreaList.clear();
                    for (List<String> list : lists) {
//                            ScrollFreeTextArea textArea = new ScrollFreeTextArea();
                        TextArea textArea = new TextArea();
                        textArea.setFont(javafx.scene.text.Font.font(Font.MONOSPACED));

                        // TODO: create one textFlow
                        for(int i=1; i<list.size(); i++){
                            String line = list.get(i) + "\n";
//                                textArea.getTextArea().appendText(line);
                            textArea.appendText(line);
                            Print.pbl("s=" + list.get(i));
                        }
                        // TODO: add textFlow to vbox

                        vboxTextFieldFile.getChildren().add(textArea);
                        textAreaList.add(textArea);
                        int lineCount = textArea.getText().split("\n").length;
                        Print.pbl("lineCount=" + lineCount);
                        textArea.setPrefSize( Double.MAX_VALUE, lineHeight*(lineCount + 3) );
                    }
                    //content.putString(textAreaList.get(0).getTextArea().getText());
                    content.putString(textAreaList.get(0).getText());
                    clipboard.setContent(content);
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

        comboboxKeyWord.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> obValue, String previous, String current) -> {
            Print.pbl("timetochange: current item:=" + comboboxKeyWord.getEditor().getText());
            Print.pbl("obValue=" + obValue + " previous=" + previous + " current=" + current);


            if(current != null && !Strings.isNullOrEmpty(current)) {
                Set<List<String>> setCode = processList.prefixWordMap.get(current);
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
                        textArea.setFont(javafx.scene.text.Font.font(Font.MONOSPACED));

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

        comboboxKeyWord.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            Print.pbl("KEY_PRESSED: KeyEvent       :=" + comboboxKeyWord.getEditor().getText());

            if (event.getCode() == KeyCode.ENTER) {
                Print.pbl("ENTER KEY: selected item:=" + comboboxKeyWord.getEditor().getText());
                comboboxKeyWord.hide();
            }else if(event.getCode() == KeyCode.DOWN) {
                if(comboboxKeyWord.getItems().size() > 0){
                    if(!comboboxKeyWord.isShowing()){
                        comboboxKeyWord.show();
                    }
                }else {
                    String prefix = comboboxKeyWord.getEditor().getText();
                    Print.pbl("DOWN KEY: selected item:=" + comboboxKeyWord.getEditor().getText());

                    if (!Strings.isNullOrEmpty(prefix)) {
                        Print.pbl("prefix=" + prefix);
                        Set<String> setWords = processList.wordsCompletion.get(prefix);
                        if (setWords != null && setWords.size() > 0) {
                            comboboxKeyWord.getItems().addAll(new ArrayList<>(setWords));
                            if (!comboboxKeyWord.isShowing()) {
                                comboboxKeyWord.show();
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
                Print.pbl("getEditor().getText()=" + comboboxKeyWord.getEditor().getText());
                Print.pbl("      event.getText()=" + event.getText());
                Print.pbl(" event.getCharacter()=" + event.getCharacter());

                String input = comboboxKeyWord.getEditor().getText() + event.getText();
                if (!Strings.isNullOrEmpty(input)) {
                    Print.pbl("input=" + input);
                    Set<String> setWords = processList.wordsCompletion.get(input);
                    if (setWords != null && setWords.size() > 0) {
                        comboboxKeyWord.getItems().clear();
                        List<String> list = new ArrayList<>(setWords);
                        comboboxKeyWord.getItems().addAll(list);
                        if (!comboboxKeyWord.isShowing()) {
                            comboboxKeyWord.show();
                        }
                    } else {
                        Print.pbl("input= is null or empty");
                        comboboxKeyWord.getItems().clear();
                        comboboxKeyWord.hide();
                    }
                }
            }
        });


        comboboxSearch.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            Print.pbl("KEY_PRESSED: KeyEvent       :=" + comboboxSearch.getEditor().getText());

            if (event.getCode() == KeyCode.ENTER) {
                Print.pbl("ENTER KEY: selected item:=" + comboboxSearch.getEditor().getText());
                //comboboxSearch.getItems().clear();
                comboboxSearch.hide();
            }else if(event.getCode() == KeyCode.DOWN) {
                if(comboboxSearch.getItems().size() > 0){
                    if(!comboboxSearch.isShowing()) {
                        comboboxSearch.show();
                    }
                }else {
                    String prefix = comboboxSearch.getEditor().getText();
                    Print.pbl("DOWN KEY: selected item:=" + comboboxSearch.getEditor().getText());

                    if (!Strings.isNullOrEmpty(prefix)) {
                        Print.pbl("prefix=" + prefix);
                        Set<String> setWords = processList.prefixFullKeyMap.get(prefix);
                        if (setWords != null && setWords.size() > 0) {
                            List<String> list = new ArrayList<>(setWords);
                            comboboxSearch.getItems().addAll(list);
                            if (!comboboxSearch.isShowing()) {
                                comboboxSearch.show();
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

                //  input = dog = do + g

                String input = comboboxSearch.getEditor().getText() + event.getText();
                if (!Strings.isNullOrEmpty(input)) {
                    Print.pbl("input=" + input);
                    Set<String> setWords = processList.prefixFullKeyMap.get(input);
                    if (setWords != null && setWords.size() > 0) {
                        comboboxSearch.getItems().clear();
                        List<String> list = new ArrayList<>(setWords);
                        comboboxSearch.getItems().addAll(list);
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

        comboboxSearch.setOnKeyPressed(event -> {
            if (keyCombinationShiftC.match(event)) {
                Print.pbl("CTRL + C Pressed");

            }
        });

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
