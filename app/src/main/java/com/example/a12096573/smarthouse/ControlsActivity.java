package com.example.a12096573.smarthouse;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.camera.simplemjpeg.AmbilightActivity;

import java.util.ArrayList;

public class ControlsActivity extends AppCompatActivity {
    ArrayAdapter adapter;
    ArrayList<String> listItems=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);

        ImageView backButton = (ImageView)findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(ControlsActivity.this, MainActivity.class));
            }
        });


        adapter = new ArrayAdapter<String>(ControlsActivity.this,
                R.layout.activity_listview, listItems);

        ListView listView = (ListView) findViewById(R.id.listView);
        listItems.add("Front Door");
        listItems.add("TV Ambilight");
        listView.setAdapter(adapter);
        registerClickCallback();

}
    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.listView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            // TODO: CHANGE THE [[ to a less than, ]] to greater than.
            public void onItemClick(AdapterView<?> paret, View viewClicked, int position, long id) {
                if(position == 0) {
                    startActivity(new Intent(ControlsActivity.this, DoorActivity.class));
                }else if( position == 1){
                    Intent i = new Intent(ControlsActivity.this, AmbilightActivity.class);
                     startActivity(i);
                }
                TextView textView = (TextView) viewClicked;

                //String message = "You clicked # " + position + ", which is string: " + textView.getText().toString();
                // Toast.makeText(RemindersActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}

