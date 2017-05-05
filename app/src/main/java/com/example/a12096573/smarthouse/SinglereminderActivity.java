package com.example.a12096573.smarthouse;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SinglereminderActivity extends AppCompatActivity {
    reminder reminder = new reminder();
    UpdateStatus mTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlereminder);

        TextView date = (TextView)findViewById(R.id.dateadded);
        TextView todoText = (TextView)findViewById(R.id.message);
        ToggleButton status = (ToggleButton)findViewById(R.id.statusButton);

        reminder = (reminder) getIntent().getSerializableExtra("Reminder");

        status.setChecked(Boolean.valueOf(reminder.getIsDone()));
        String[] arrayDate = reminder.getDate().split("T");
        date.setText("Date Added: " + arrayDate[0] + " " + arrayDate[1].substring(0, 8));
        todoText.setText(reminder.getTodo());


        ImageView backButton = (ImageView)findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(SinglereminderActivity.this, RemindersActivity.class));
            }
        });

        status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    mTask = new UpdateStatus(reminder,"true");
                    mTask.execute();
                }
                else
                {
                    mTask = new UpdateStatus(reminder,"false");
                    mTask.execute();
                }

            }
        });
    }
}

class UpdateStatus extends AsyncTask<Void,Void,Void> {
    reminder reminder;
    String status;
    ArrayList<reminder> listReminders;

    UpdateStatus(reminder reminder, String status){
        this.reminder = reminder;
        this.status = status;
    }

    protected Void doInBackground(Void... params) {
        try {
            String SensorLogData = contactServer("http://smarthome.gear.host/api/updateTodo?id=" + reminder.getId() + "&status="+status);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(JSONArray result) {

    }

    private String contactServer(String urlStr) {
        URL url;
        try {
            url = new URL(urlStr);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("PUT");
            OutputStreamWriter out = new OutputStreamWriter(
                    httpCon.getOutputStream());
            out.write("Resource content");
            out.close();
            httpCon.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
