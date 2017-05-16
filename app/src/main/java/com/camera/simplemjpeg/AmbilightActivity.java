package com.camera.simplemjpeg;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.a12096573.smarthouse.ControlsActivity;
import com.example.a12096573.smarthouse.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
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
import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class AmbilightActivity extends AppCompatActivity {
    private static final boolean DEBUG = false;
    private static final String TAG = "MJPEG";
    final Handler handler = new Handler();
    final String serverUri = "tcp://m21.cloudmqtt.com:14403";
    final String subscriptionTopic = "startambilight";
    final String publishTopic = "startambilight";
    UpdateAmbilightStatus mTask = null;
    String apiURL = "";
    String URL;
    TextView msgView;
    ImageView backButton;
    ToggleButton webcamToggle;
    ToggleButton ambilightToggle;
    ToggleButton lightsToggle;
    MqttAndroidClient mqttAndroidClient;
    String clientId = "1234566789";
    private MjpegView mv = null;
    private int width = 640;
    private int height = 480;
    private int ip_ad1 = 127;
    private int ip_ad2 = 0;
    private int ip_ad3 = 0;
    private int ip_ad4 = 1;
    private int ip_port = 8080;
    private String ip_command = "videofeed";
    private boolean suspending = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final int lightBlue = ContextCompat.getColor(getApplicationContext(), R.color.light_blue);
        StringBuilder sb = new StringBuilder();
        String s_http = "http://";
        String s_dot = ".";
        String s_colon = ":";
        String s_slash = "/";
        sb.append(s_http);
        sb.append(ip_ad1);
        sb.append(s_dot);
        sb.append(ip_ad2);
        sb.append(s_dot);
        sb.append(ip_ad3);
        sb.append(s_dot);
        sb.append(ip_ad4);
        sb.append(s_colon);
        sb.append(ip_port);
        sb.append(s_slash);
        sb.append(ip_command);
        URL = new String(sb);
        setContentView(R.layout.activity_ambilight);
        mv = (MjpegView) findViewById(R.id.mv);
        mv.setBackgroundColor(lightBlue);
        if (mv != null) {
            mv.setResolution(width, height);
        }
        msgView = (TextView) findViewById(R.id.msgView);
        backButton = (ImageView) findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(AmbilightActivity.this, ControlsActivity.class));
            }
        });

        webcamToggle = (ToggleButton) findViewById(R.id.toggleWebcamButton);

        webcamToggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                apiURL = "http://smarthome.gear.host/api/updateDeviceStatus?id=7&status=";
                if (webcamToggle.isChecked()) {
                    mv.stopPlayback();
                    mv.setBackgroundColor(lightBlue);
                    msgView.setText("Click a button below to begin a task:");
                    mTask = new UpdateAmbilightStatus(apiURL + "false");
                    mTask.execute();
                } else {
                    msgView.setText(R.string.title_connecting);
                    mv.setBackgroundColor(Color.TRANSPARENT);
                    mTask = new UpdateAmbilightStatus(apiURL + "true");
                    mTask.execute();
                    new DoRead().execute(URL);
                }
            }
        });
        lightsToggle = (ToggleButton) findViewById(R.id.toggleLightsButton);
        lightsToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                apiURL = "http://smarthome.gear.host/api/updateDeviceStatus?id=6&status=";
                if (lightsToggle.isChecked()) {
                    publishMessage("OFF");
                    mTask = new UpdateAmbilightStatus(apiURL + "false");
                    mTask.execute();
                } else {
                    publishMessage("ON");
                    mTask = new UpdateAmbilightStatus(apiURL + "true");
                    mTask.execute();
                }
            }
        });
        ambilightToggle = (ToggleButton) findViewById(R.id.toggleAmbilightButton);
        ambilightToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                apiURL = "http://smarthome.gear.host/api/updateDeviceStatus?id=5&status=";
                if (ambilightToggle.isChecked()) {
                    publishMessage("STOP");
                    lightsToggle.setEnabled(true);
                    mTask = new UpdateAmbilightStatus(apiURL + "false");
                    mTask.execute();
                } else {
                    publishMessage("START");
                    lightsToggle.setEnabled(false);
                    mTask = new UpdateAmbilightStatus(apiURL + "true");
                    mTask.execute();
                }
            }
        });

        clientId = clientId + System.currentTimeMillis();

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    //("Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {
                    //("Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                msgView.setText("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //("Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName("huseyin");
        mqttConnectOptions.setPassword(new char[]{'h', 'u', 's', 'e', 'y', 'i', 'n'});
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setConnectionTimeout(240000);

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
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
                    //("Failed to connect to: " + serverUri);
                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    public void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //("Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //("Failed to subscribe");
                }
            });

            // THIS DOES NOT WORK!
            mqttAndroidClient.subscribe(subscriptionTopic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    System.out.println("Message: " + topic + " : " + new String(message.getPayload()));
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishMessage(String msg) {

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
//            if(!mqttAndroidClient.isConnected()){
//            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onResume() {
        if (DEBUG) Log.d(TAG, "onResume()");
        super.onResume();
        if (mv != null) {
            if (suspending) {
                new DoRead().execute(URL);
                suspending = false;
            }
        }

    }

    public void onStart() {
        if (DEBUG) Log.d(TAG, "onStart()");
        super.onStart();
    }

    public void onPause() {
        if (DEBUG) Log.d(TAG, "onPause()");
        super.onPause();
        if (mv != null) {
            if (mv.isStreaming()) {
                mv.stopPlayback();
                suspending = true;
            }
        }
    }

    public void onStop() {
        if (DEBUG) Log.d(TAG, "onStop()");
        super.onStop();
    }

    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy()");
        apiURL = "http://smarthome.gear.host/api/updateDeviceStatus?id=5&status=";
        mTask = new UpdateAmbilightStatus(apiURL + "false");
        mTask.execute();
        apiURL = "http://smarthome.gear.host/api/updateDeviceStatus?id=6&status=";
        mTask = new UpdateAmbilightStatus(apiURL + "false");
        mTask.execute();
        apiURL = "http://smarthome.gear.host/api/updateDeviceStatus?id=7&status=";
        mTask = new UpdateAmbilightStatus(apiURL + "false");
        mTask.execute();
        if (mv != null) {
            mv.freeCameraMemory();
        }

        super.onDestroy();
    }

    public void setImageError() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                msgView.setText(R.string.title_imageerror);
                return;
            }
        });
    }

    public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
        protected MjpegInputStream doInBackground(String... url) {
            //TODO: if camera has authentication deal with it and don't just not work
            HttpResponse res = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpParams httpParams = httpclient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, 5 * 1000);
            if (DEBUG) Log.d(TAG, "1. Sending http request");
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                if (DEBUG)
                    Log.d(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());

                if (res.getStatusLine().getStatusCode() == 401) {
                    //You must turn off camera User Access Control before this will work
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());
            } catch (ClientProtocolException e) {
                if (DEBUG) {
                    e.printStackTrace();
                    Log.d(TAG, "Request failed-ClientProtocolException", e);
                }
                //Error connecting to camera
            } catch (IOException e) {
                if (DEBUG) {
                    e.printStackTrace();
                    Log.d(TAG, "Request failed-IOException", e);
                }
                //Error connecting to camera
            }
            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            mv.setSource(result);
            if (result != null) {
                result.setSkip(1);
                msgView.setText("Connected to stream!");
            } else {
                msgView.setText(R.string.title_disconnected);
            }
            mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            mv.showFps(false);
        }
    }

    private class UpdateAmbilightStatus extends AsyncTask<Void, Void, Void> {
        String url;

        UpdateAmbilightStatus(String url) {
            this.url = url;
        }

        protected Void doInBackground(Void... params) {
            try {
                String SensorLogData = contactServer(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(JSONArray result) {

        }

        private String contactServer(String urlStr) {
            java.net.URL url;
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


}