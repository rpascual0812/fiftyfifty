package com.example.rafael.fiftyfifty;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by pogi on 9/6/2016.
 */
public class Choose extends AppCompatActivity{
    ImageView driver;
    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick);
        driver = (ImageView) findViewById(R.id.drive);
        driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driver.setImageResource(R.drawable.wheel2);
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                startActivity(intent);
            }
        });
    }
}
