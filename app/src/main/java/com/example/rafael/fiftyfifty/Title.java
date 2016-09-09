package com.example.rafael.fiftyfifty;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by pogi on 9/9/2016.
 */
public class Title extends AppCompatActivity {
    RelativeLayout next;
    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.titleshow);
        next = (RelativeLayout) findViewById(R.id.titlerelative);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Choose.class);
                startActivity(intent);
            }
        });
    }
}
