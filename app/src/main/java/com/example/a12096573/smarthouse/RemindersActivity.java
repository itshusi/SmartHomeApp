package com.example.a12096573.smarthouse;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.example.a12096573.smarthouse.R.drawable.logs;

public class RemindersActivity extends AppCompatActivity {
    Handler handler;
    private ListView mListView;
    ArrayList<String> listItems=new ArrayList<String>();
    ArrayList<reminder> listReminders=new ArrayList<reminder>();
    ArrayAdapter adapter;
    UpdateTask mTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        ImageView backButton = (ImageView)findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
            startActivity(new Intent(RemindersActivity.this, MainActivity.class));
            }
        });

        adapter = new ArrayAdapter<String>(RemindersActivity.this,
                R.layout.activity_listview, listItems);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        mTask = new UpdateTask(adapter,listReminders);
        mTask.execute();
        registerClickCallback();




    }
    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.listView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            // TODO: CHANGE THE [[ to a less than, ]] to greater than.
            public void onItemClick(AdapterView<?> paret, View viewClicked, int position, long id) {
                Intent i = new Intent(RemindersActivity.this, SinglereminderActivity.class);
                i.putExtra("Reminder", listReminders.get(position));

                startActivity(i);
                TextView textView = (TextView) viewClicked;

                //String message = "You clicked # " + position + ", which is string: " + textView.getText().toString();
                // Toast.makeText(RemindersActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }


}

class UpdateTask extends AsyncTask<Void,Void,JSONArray> {
    ArrayAdapter adapter;
    ArrayList<reminder> listReminders;

    UpdateTask(ArrayAdapter adapter,ArrayList<reminder> listReminders){
        this.adapter = adapter;
        this.listReminders = listReminders;
    }

    protected JSONArray doInBackground(Void... params) {
        try {
            String SensorLogData = contactSensorServer("http://smarthome.gear.host/api/gettodos");
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
                reminder tempReminder = new reminder();
                String id = logObject.getString("id");
                String todo = logObject.getString("todo");
                String date = logObject.getString("dateAdded");
                String isComplete = logObject.getString("isComplete");
                tempReminder.setTodo(todo);
                tempReminder.setId(id);
                tempReminder.setDate(date);
                tempReminder.setIsDone(isComplete);
                listReminders.add(tempReminder);
                if(todo.length() > 20){
                    todo = todo.substring(0,20) + "...";
                }
                if(isComplete.equals("true")){
                    todo = todo + " (Completed)";
                }
                adapter.add(todo);
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
