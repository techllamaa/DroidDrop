package com.buildbuilds.headsortails;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.buildbuilds.headsortails.R;

import java.util.Random;

public class CoinActivity extends AppCompatActivity implements View.OnClickListener {

    //declare variables
    private ImageView ballIV;     //reference to ball imageview
    private TextView answerTV;    //reference to answer textview

    //array of the answers magic 8 ball will give
    private String[] answersArray = {"of course", "up to you", "idk", "naah"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin);

        //reference from xml file
        ballIV = findViewById(R.id.btn);
        answerTV = findViewById(R.id.answer);

        //so you can tap on image and something happens
        ballIV.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn:

                int rand = new Random().nextInt(answersArray.length);
                answerTV.setText(answersArray[rand]);

                break;

        }
    }

}