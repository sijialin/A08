//Group T - ICT - A08
//This App is a tuner, it collects the frequency played by the instrument and visualizes it on the UI
//This file is for accessing data from database

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DBTest {
    private String frequency;//the latest frequency in table InputOutFreq
    private ArrayList<String> notes=new ArrayList<>();//to store all notes in table freqToNote

    public String makeGETRequest(String urlName){
        BufferedReader rd;
        StringBuilder sb;
        String line;
        try {
            URL url = new URL(urlName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            sb = new StringBuilder();
            while ((line = rd.readLine()) != null)
            {
                sb.append(line + '\n');
            }
            conn.disconnect();
            return sb.toString();
        } catch (IOException e){
            e.printStackTrace();
        }
        return "";
    }

    //get the latest frequency in table InputOutFreq
    public String parseLatestFrequency(String jsonString){
        String  var;
        try {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++)
            {
                JSONObject curObject = array.getJSONObject(i);
                var=curObject.getString("Frequency");
                frequency=var;
            }
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        return  frequency;
    }

    //get all the notes in table freqToNote
    public ArrayList<String> parseAllNotes(String jsonString){
        String  var;
        try {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++)
            {
                JSONObject curObject = array.getJSONObject(i);
                var=curObject.getString("Notename");
                var.replace("\"","");
                notes.add(var);
            }
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        return  notes;
    }

    /*
    //this is for test
    public static void main(String[] args) {
        DBTest rc = new DBTest();
        String response = rc.makeGETRequest("https://studev.groept.be/api/a21ib2a08/frequency");
        rc.testNote();
        System.out.println(response);
        System.out.println(rc.parseLatestFrequency(response));
    }
    */
}


