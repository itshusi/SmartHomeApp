package com.example.a12096573.smarthouse;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class LogsActivity extends AppCompatActivity {
    ArrayAdapter adapter;
    ArrayList<String> listItems=new ArrayList<String>();
    UpdateLogsTask mTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        ImageView backButton = (ImageView)findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(LogsActivity.this, MainActivity.class));
            }
        });


        adapter = new ArrayAdapter<String>(LogsActivity.this,
                R.layout.log_list, listItems);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        mTask = new UpdateLogsTask(adapter);
        mTask.execute();
    }
}

class UpdateLogsTask extends AsyncTask<Void,Void,JSONArray> {
    ArrayAdapter adapter;

    UpdateLogsTask(ArrayAdapter adapter){
        this.adapter = adapter;
    }

    protected JSONArray doInBackground(Void... params) {
        try {
            String SensorLogData = contactSensorServer("http://smarthome.gear.host/api/getlogs");
            return new JSONArray(SensorLogData);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(JSONArray result) {
        // Add code that loops through JSONArray and adds to adapter here

        for (int i = 0; i < result.length(); i++) {
            try {
                JSONObject logObject = result.getJSONObject(i);
                String id = logObject.getString("id");
                String desc = logObject.getString("description");
                String date = logObject.getString("timeStamp");
                String[] arrayDate = date.split("T");

                String logRecord = (arrayDate[0].substring(5) + " " + arrayDate[1].substring(0, 8)) +" - "+desc;

                adapter.add(logRecord);
            }catch (Exception e){

            }
        }
    }

    private String contactSensorServer(String urlStr) {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line = "";
        String result = "";
        try {
            url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // Issue the GET to send the data
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            // read the result to process response and ensure data sent
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
