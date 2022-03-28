//Group T - ICT - A08
//This App is a tuner, it collects the frequency played by the instrument and visualizes it on the UI
//This file is for user interface

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tuner {
    private DBTest db=new DBTest();
    private NoteCalculation temp;
    private String note;//the note to display
    private String frequency;//input frequency
    private boolean outing;//if the navigation bar is on the way
    private boolean isHide;//if the navigation is shown
    private final String frequencyURL="https://studev.groept.be/api/a21ib2a08/frequency";
    private String  coFreq;//the frequency correspond to the shown note
    private double A4;

    @FXML
    private Pane displayZone;

    @FXML
    private Pane navigation;

    @FXML
    private Pane narrowPane;

    @FXML
    private Pane inputA4Pane;

    @FXML
    private TextField a4TextField;

    @FXML
    private Label showFrequency;

    @FXML
    private Label showInfo;

    @FXML
    private Label showNote;

    @FXML
    private Label showCoFreq;

    @FXML
    private VBox showHintViolin;

    @FXML
    private VBox showHintGuitar;

    @FXML
    private VBox showHintUkulele;

    @FXML
    private Polygon narrow;

    //when mouse enters the button, the hint for adjusting the string will show
    //when mouse exits the button, the hint for adjusting the string will disappear
    @FXML
    void showViolinString(MouseEvent event) {
        if(!outing)
            showHintViolin.setVisible(true);}

    @FXML
    void notShowViolinString(MouseEvent event) {showHintViolin.setVisible(false);}

    @FXML
    void showGuitarString(MouseEvent event) {
        if(!outing)
            showHintGuitar.setVisible(true);}

    @FXML
    void notShowGuitarString(MouseEvent event) {showHintGuitar.setVisible(false);}

    @FXML
    void showUkuleleString(MouseEvent event) {
        if(!outing)
            showHintUkulele.setVisible(true);}

    @FXML
    void notShowUkuleleString(MouseEvent event) {showHintUkulele.setVisible(false);}

    
    //when mouse enters the detecting zone ,which is at the leftmost location of the interface, the navigation shows
    @FXML
    void showNavigation(MouseEvent event) {
        outing=true;//the navigation bar is executing the going out action
        Timeline timeLine = new Timeline();//animation for ui changing
        timeLine.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(navigation.layoutXProperty(), -210)
                ),
                new KeyFrame(new Duration(300),
                        new KeyValue(displayZone.layoutXProperty(),240)
                ),

                new KeyFrame(new Duration(300),
                        new KeyValue(navigation.layoutXProperty(), 0)
                )
        );
        timeLine.play();
        outing=false;//the action ends
        isHide=false;//the navigation bar is now shown
    }

    //when mouse leaves the navigation zone, the navigation isn't shown unless the bar is locked
    @FXML
    void notShowNavigation(MouseEvent event) {
        if(!lockPane.isVisible()&& !outing && !isHide) {//debounce
            Timeline timeLine = new Timeline();
            timeLine.getKeyFrames().addAll(
                    new KeyFrame(new Duration(300),
                            new KeyValue(displayZone.layoutXProperty(),150)
                    ),
                    new KeyFrame(new Duration(300),
                            new KeyValue(navigation.layoutXProperty(), -210)
                    )
            );
            timeLine.play();
            isHide=true;
            inputA4Pane.setVisible(false);
            a4TextField.setText("");
        }
    }

    //initializing the ui
    // every fix period, read new input from the database, now 1000ms
    // and call the function to adjust the information shown on the ui
    @FXML
    private AnchorPane background;
    @FXML
    public void initialize(){
        //initialization
        lockPane.setVisible(false);
        unlockPane.setVisible(true);
        showHintViolin.setVisible(false);
        showHintGuitar.setVisible(false);
        showHintUkulele.setVisible(false);
        outing=false;
        isHide=true;
        inputA4Pane.setVisible(false);
        A4=440;

        Timer timer=new Timer();//refresh every 1000ms
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run () {
                //get new current frequency and it's corresponding information
                frequency = db.parseLatestFrequency(db.makeGETRequest(frequencyURL));
                temp = new NoteCalculation(Double.parseDouble(frequency),A4);
                note = temp.findNearest();
                coFreq=String.valueOf(temp.coFreq());
                /*
                //this is for testing the code
                System.out.println("frequency:"+frequency+" note:"+note+" coFreq:"+coFreq);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
                */
                adjustUI();
                }
            },0,500);

    }

    //the function to refresh the information shown on the ui
    @FXML
    void adjustUI(){
        Platform.runLater(() -> {
            showNote.setText(note);
            showFrequency.setText(frequency+"Hz");
            showCoFreq.setText(coFreq+"Hz");
            adjustNarrow();
        });
    }

    //adjust the position and color of the narrow according to the deviation of the frequency
    @FXML
    public void adjustNarrow(){
        switch (temp.setColor()){
            case 'G': narrow.setFill(Color.GREEN);
                break;
            case 'Y':narrow.setFill(Color.YELLOW);
                break;
            case 'O':narrow.setFill(Color.ORANGE);
                break;
            case 'R':narrow.setFill(Color.RED);
            default:
                break;
        }
        Timeline timeLine = new Timeline();
        timeLine.getKeyFrames().addAll(
                new KeyFrame(new Duration(400),
                        new KeyValue(narrowPane.layoutXProperty(), 160+temp.findLowHigh()*Math.sqrt(Math.abs(temp.findDifference())
                )*100)
                )
        );
        timeLine.play();
    }

    //show the textfield for inputting the new A4 frequency
    @FXML
    void adjustA4(MouseEvent event) {
        inputA4Pane.setVisible(true);
    }

    //user can adjust the standard frequency according to the scenario they are playing at
    @FXML
    void setTheA4(MouseEvent event){
        String input=a4TextField.getText().trim();
        Pattern pattern= Pattern.compile("[0-9]*");
        Matcher isNum=pattern.matcher(input);
        if(isNum.matches()){//is the input a number
            if(Double.parseDouble(input)>0) {//is the number positive
                A4 = Double.parseDouble(input);
                Timeline timeLine = new Timeline();
                timeLine.getKeyFrames().addAll(
                        new KeyFrame(Duration.ZERO,
                                new KeyValue(showInfo.textProperty(), "Tunes reset successfully")
                        ),
                        new KeyFrame(new Duration(1200),
                                new KeyValue(showInfo.textProperty(), "")
                        )
                );
                timeLine.play();
            }
            else{
                Timeline timeLine = new Timeline();
                timeLine.getKeyFrames().addAll(
                        new KeyFrame(Duration.ZERO,
                                new KeyValue(showInfo.textProperty(), "Number should be positive")
                        ),
                        new KeyFrame(new Duration(1200),
                                new KeyValue(showInfo.textProperty(), "")
                        )
                );
                timeLine.play();
            }

        }
        else{
            Timeline timeLine = new Timeline();
            timeLine.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(showInfo.textProperty(), "Please input a number!")
                    ),
                    new KeyFrame(new Duration(1200),
                            new KeyValue(showInfo.textProperty(), "")
                    )
            );
            timeLine.play();
        }
    }


   @FXML
   private Pane unlockPane;

    @FXML
    private Pane lockPane;

    @FXML
    void lockBar(MouseEvent event) {
        lockPane.setVisible(false);
        unlockPane.setVisible(true);
    }
    @FXML
    void unlockBar(MouseEvent event) {
        unlockPane.setVisible(false);
        lockPane.setVisible(true);
    }

}
