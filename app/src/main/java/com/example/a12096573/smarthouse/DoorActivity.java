package com.example.a12096573.smarthouse;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class DoorActivity extends AppCompatActivity {
    UpdateDoorStatus mTask = null;
    GetDoorStatus doorTask = null;
    TextView todoText;
    MqttAndroidClient mqttAndroidClient;

    final String serverUri = "tcp://m21.cloudmqtt.com:15386";
    ToggleButton status;
    ToggleButton statusDoor;
    String clientId = "1234566789";
    final String subscriptionTopic = "phidgetsmotor";
    final String publishTopic = "test";
    final String publishMessage = "Hello World!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_door);

        TextView date = (TextView)findViewById(R.id.dateadded);
        TextView todoText = (TextView)findViewById(R.id.message);
        status = (ToggleButton)findViewById(R.id.statusButton);
        statusDoor = status;

        String[] arrayDate = "2017-10-01T26:20:10".split("T");
        date.setText("Last Time Used: " + arrayDate[0] + " " + arrayDate[1].substring(0, 8));


        ImageView backButton = (ImageView)findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(DoorActivity.this, ControlsActivity.class));
            }
        });


        // MQTT

       mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
       mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
          public void connectComplete(boolean reconnect, String serverURI) {

              if (reconnect) {
                   // Because Clean Session is true, we need to re-subscribe
                   subscribeToTopic();
                } else {
                }
            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                setMessageText(new String(message.getPayload()));
                Log.d("tag","Received");
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

           }
        });
//
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName("ginny");
        mqttConnectOptions.setPassword(new char[]{'g','i','n','n','y'});
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setConnectionTimeout(240000);
        Log.d("tag","GETTING INTO TRY");
//
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("tag","SUCCESS");
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //setMessageText("Fail");
                    Log.e("tag","msg",exception);
                }
            });


        } catch (MqttException ex){
           // ex.printStackTrace();
        }
        doorTask = new GetDoorStatus(status, todoText,mqttAndroidClient);
        doorTask.execute();
    }

    public void setListenerToToggle(ToggleButton button){
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String url = "http://smarthome.gear.host/api/updateDeviceStatus?id=1&status=";
                Log.d("tag","POSTING");
                if(isChecked)
                {
                    mTask = new UpdateDoorStatus(url + "true");
                    mTask.execute();
                }
                else
                {
                    mTask = new UpdateDoorStatus(url + "false");
                    mTask.execute();
                }

            }
        });
    }

    public void setMessageText(String text){
        todoText.setText(text);
    }

    public void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });

            // THIS DOES NOT WORK!
            mqttAndroidClient.subscribe(subscriptionTopic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    System.out.println("Message: " + topic + " : " + new String(message.getPayload()));
                    String msg = new String(message.getPayload());
                    statusDoor.setOnCheckedChangeListener(null);
                    if(msg.equals("OPEN"))
                    {
                       // statusDoor.setChecked(false);
                        //todoText.setText("OPEN");
                        setMessageText("OPEN");
                        setMessageText("OPEN");
                    }else{
                        //statusDoor.setChecked(true);
                        todoText.setText("CLOSED");
                    }
                    setListenerToToggle(statusDoor);
                }
            });

        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishMessage(){

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(publishMessage.getBytes());
            mqttAndroidClient.publish(publishTopic, message);

            if(!mqttAndroidClient.isConnected()){

            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class UpdateDoorStatus extends AsyncTask<Void,Void,Void> {
    String url;

    UpdateDoorStatus(String url){
        this.url = url;
    }

    protected Void doInBackground(Void... params) {
        try {
            String SensorLogData = contactServer(url);
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
            httpCon.setRequestMethod("POST");
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

// this for first time loading activity, get device status

class GetDoorStatus extends AsyncTask<Void,Void,JSONArray> {
    ToggleButton doorStatus;
    TextView todoText;
    MqttAndroidClient mqttAndroidClient;

    GetDoorStatus(ToggleButton status, TextView todoText, MqttAndroidClient mqttAndroidClient){
        this.doorStatus = status;
        this.todoText = todoText;
        this.mqttAndroidClient= mqttAndroidClient;
    }

    protected JSONArray doInBackground(Void... params) {
        try {
            String SensorLogData = contactSensorServer("http://smarthome.gear.host/api/getdevices");
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
                String status = logObject.getString("status");
                doorStatus.setOnCheckedChangeListener(null);
                if(id.equals("1")){
                    if(status.equals("true")){
                        doorStatus.setChecked(false);
                        todoText.setText("OPEN");
                    }else{
                        doorStatus.setChecked(true);
                        todoText.setText("CLOSED");
                    }
                }
                setListenerToToggle(doorStatus);
            }catch (Exception e){

            }
        }
    }

    public void setListenerToToggle(ToggleButton button){
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String url = "http://smarthome.gear.host/api/updateDeviceStatus?id=1&status=";
                Log.d("tag","POSTED");
                if(isChecked)
                {
                    UpdateDoorStatus mTask = new UpdateDoorStatus(url + "false");
                    mTask.execute();
                    todoText.setText("CLOSED");
                    //publishMessage(mqttAndroidClient, "OPEN");
                }
                else
                {
                    UpdateDoorStatus mTask = new UpdateDoorStatus(url + "true");
                    mTask.execute();
                    todoText.setText("OPEN");
                    //publishMessage(mqttAndroidClient, "CLOSED");
                }

            }
        });
    }
    public void publishMessage( MqttAndroidClient mqttAndroidClient, String msg){

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload("Hello Android".getBytes());
            mqttAndroidClient.publish(msg, message);

            if(!mqttAndroidClient.isConnected()){

            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
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
