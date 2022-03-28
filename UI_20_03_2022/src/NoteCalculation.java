//Group T - ICT - A08
//This App is a tuner, it collects the frequency played by the instrument and visualizes it on the UI
//This file is for processing data from database

import java.math.BigDecimal;
import java.util.ArrayList;

public class NoteCalculation {
    private double frequency;
    private double nearestLowFreq;
    private double nearestHighFreq;
    private double nearestFreq;
    private int lowIndex;
    private int highIndex;
    private  int nearestIndex;
    private double percentage;//the deviation percentage
    private double[] frequencyCategoryToUse;
    private final double[] frequencyCategory440= {
            10,16.35,17.32,18.35,19.45,20.6,
            21.83,23.12,24.5,25.96,27.5,29.14,
            30.87,32.7,34.65,36.71,38.89,41.2,
            43.65,46.25,49,51.9,55,58.27
            ,61.74,65.41,69.3,73.42,77.78,82.41
            ,87.31,92.5,98,103.83,110,116.54,123.47
            ,130.81,138.59,146.83,155.56,164.81
            ,174.61,185,196,207.65,220,233.08
            ,246.94,261.63,277.18,293.66,311.13
            ,329.63,349.23,369.99,392,415.3,440
            ,466.16,493.88,523.25,554.37,587.33
            ,622.25,659.25,697.46,739.99,783.99
            ,830.61,880,932.33,987.77,1046.5
            ,1108.73,1175.66,1244.51,1318.51,1396.91
            ,1479.98,1567.97,1661.22,1760,1864.66
            ,1975.53,2093,2217.46,2349.32,2489.02
            ,2637.02,2793.83,2959.96,3135.96,3322.44
            ,3520,3729.31,3951.07,4186.01,4500 };

    public NoteCalculation(double frequency,double newA4){
        //initialization
        this.frequency=frequency;
        nearestHighFreq=frequency;
        frequencyCategoryToUse=new double[99];
        setA4(newA4);
        nearestLowFreq=0;
        nearestFreq=frequency;
        percentage=0;
        findNearest();
    }

    public double coFreq(){//the frequency corresponding to the nearest note
        return nearestFreq;
    }
    public String findNearest(){//find the nearest Note to the current frequency
        //out of range
        if(frequency<frequencyCategoryToUse[0]) {
            nearestLowFreq = 0;
            nearestHighFreq = frequencyCategoryToUse[0];
            nearestFreq=frequencyCategoryToUse[0];
            lowIndex=0;
            highIndex=0;
            nearestIndex=highIndex;
        }
        else if(frequency>frequencyCategoryToUse[98]){
            nearestLowFreq=frequencyCategoryToUse[98];
            nearestHighFreq=frequencyCategoryToUse[98];
            nearestFreq=frequencyCategoryToUse[98];
            lowIndex= frequencyCategoryToUse.length-1;
            highIndex=frequencyCategoryToUse.length-1;
            nearestIndex=lowIndex;
        }
        //in the range
        else {
            boolean finishZone = false;
            int i = 0;
            while (!finishZone && i < frequencyCategoryToUse.length) {
                if (frequency >= frequencyCategoryToUse[i]) {
                    nearestLowFreq = frequencyCategoryToUse[i];
                    lowIndex=i;
                }
                else {
                    finishZone = true;
                    nearestHighFreq = frequencyCategoryToUse[i];
                    highIndex=i;
                }
                i++;
                if((frequency-nearestLowFreq)<(nearestHighFreq-frequency)){
                    nearestFreq=nearestLowFreq;
                    nearestIndex =lowIndex;
                }
                else {
                    nearestFreq = nearestHighFreq;
                    nearestIndex = highIndex;
                }
            }
        }
        //System.out.println("nearestIndex "+nearestIndex);
        DBTest rc = new DBTest();
        String noteURL = "https://studev.groept.be/api/a21ib2a08/bbbb";
        String response = rc.makeGETRequest(noteURL);
        ArrayList<String>allNotes=rc.parseAllNotes(response);
        String nearestNote = allNotes.get(nearestIndex);
        nearestNote = nearestNote.replace("\"", "");//delete all " in the string
        if(nearestNote.length()>4)
            nearestNote = nearestNote.substring(0,4);
        nearestNote = nearestNote.replace("/"," ");
        //System.out.println(nearestNote);
        return nearestNote;
    }

    public int findLowHigh(){ //is the deviation positive or negative
        if (findDifference()>=0)
            return 1;
        else
            return -1;
    }
    public double findDifference(){//how much is the deviation
        double difference=frequency-nearestFreq;
        if(frequency==0)
            percentage=-1;
        else if(frequency<=frequencyCategoryToUse[98])
            percentage=difference/frequency;
        else
            percentage=1;
        //System.out.println("percentage"+percentage);
        return percentage;
    }

    //set the color according to deviation
    public char setColor(){
        findDifference();
        double abs=Math.abs(percentage);
        char color;
        if(abs<=0.01)
            color = 'G';
        else if(abs<=0.05)
            color = 'Y';
        else if(abs<=0.2)
            color = 'O';
        else
            color = 'R';
        return color;
    }

    //set new A4
    public void setA4(double A4){
        for(int i=0;i< frequencyCategory440.length;i++){
            double answer=A4/440 * frequencyCategory440[i];
            if((int)answer!=answer) {
                BigDecimal bg=new BigDecimal(answer);
                answer=bg.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
            }
            frequencyCategoryToUse[i]=answer;

        }
    }
}
