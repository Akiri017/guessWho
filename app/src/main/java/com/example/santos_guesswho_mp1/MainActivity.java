package com.example.santos_guesswho_mp1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonEasy = findViewById(R.id.buttonEasy);
        Button buttonMedium = findViewById(R.id.buttonMedium);
        Button buttonHard = findViewById(R.id.buttonHard);

        // Set listener for the Easy button
        buttonEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(4); // 4 pairs for easy level
            }
        });

        // Set listener for the Medium button
        buttonMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(6); // 6 pairs for medium level
            }
        });

        // Set listener for the Hard button
        buttonHard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(8); // 8 pairs for hard level
            }
        });
    }

    // This method starts the GameActivity with the selected level
    private void startGame(int pairCount) {
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra("PAIR_COUNT", pairCount);
        startActivity(intent);
    }
}