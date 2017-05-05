package com.example.a12096573.smarthouse;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton controlButton = (ImageButton)findViewById(R.id.controlsButton);
        ImageButton remindersButton = (ImageButton)findViewById(R.id.remindersButton);
        ImageButton statsButton = (ImageButton)findViewById(R.id.statsButton);
        ImageButton logsButton = (ImageButton)findViewById(R.id.logsButton);

        controlButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ControlsActivity.class));
            }
        });
        remindersButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RemindersActivity.class));
            }
        });
        statsButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, StatsActivity.class));
            }
        });
        logsButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LogsActivity.class));
            }
        });
    }
}
