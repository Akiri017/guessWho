package com.example.santos_guesswho_mp1;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    private int pairCount;
    private GridLayout gridLayout;
    private ArrayList<Integer> cardImages = new ArrayList<>();
    private ArrayList<ImageView> flippedCards = new ArrayList<>();
    private ImageView firstCard = null;
    private ImageView secondCard = null;
    private int matchedPairs = 0;
    private boolean isChecking = false; // Prevents clicking more than 2 cards at once

    // Timer components
    private TextView timerTextView;
    private long startTime = 0;
    private Handler timerHandler = new Handler();

    // Progress Bar
    private ProgressBar progressBar;

    // Timer logic
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            timerTextView.setText(String.format(Locale.getDefault(), "Time: %02d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500); // Update every half a second
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Get the number of pairs from MainActivity
        pairCount = getIntent().getIntExtra("PAIR_COUNT", 4); // Default to 4 (easy)

        gridLayout = findViewById(R.id.gridLayoutCards);
        timerTextView = findViewById(R.id.textViewTimer);
        progressBar = findViewById(R.id.progressBar);

        // Adjust grid columns for different levels
        if (pairCount == 6) { // Medium
            gridLayout.setColumnCount(4);
        } else if (pairCount == 8) { // Hard
            gridLayout.setColumnCount(4);
        } else { // Easy
            gridLayout.setColumnCount(4);
        }

        setupGame();
    }

    private void setupGame() {
        // Prepare the list of card images
        prepareCardImages();
        Collections.shuffle(cardImages); // Randomize the card order

        // Reset game state
        matchedPairs = 0;
        progressBar.setMax(pairCount);
        progressBar.setProgress(0);

        gridLayout.removeAllViews(); // Clear any existing views

        // Create and add cards to the grid
        for (int i = 0; i < pairCount * 2; i++) {
            // Inflate the card layout
            ImageView card = (ImageView) LayoutInflater.from(this)
                    .inflate(R.layout.card_layout, gridLayout, false);

            // Set card properties
            card.setImageResource(R.drawable.card_back);
            card.setTag(cardImages.get(i)); // Store the character image ID in the tag

            // Set layout params for grid distribution
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            card.setLayoutParams(params);


            // Set click listener
            card.setOnClickListener(view -> {
                if (!isChecking && flippedCards.size() < 2 && view != firstCard) {
                    flipCard((ImageView) view);
                }
            });

            gridLayout.addView(card);
        }

        // Start the timer
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void prepareCardImages() {
        cardImages.clear();
        for (int i = 1; i <= pairCount; i++) {
            int resourceId = getResources().getIdentifier("card_" + i, "drawable", getPackageName());
            cardImages.add(resourceId);
            cardImages.add(resourceId); // Add each image twice for pairs
        }
    }

    private void flipCard(ImageView card) {
        // Animation for flipping
        ObjectAnimator oa1 = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0f);
        ObjectAnimator oa2 = ObjectAnimator.ofFloat(card, "scaleX", 0f, 1f);
        oa1.setInterpolator(new DecelerateInterpolator());
        oa2.setInterpolator(new AccelerateDecelerateInterpolator());

        oa1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // Change image in the middle of the flip
                card.setImageResource((Integer) card.getTag());
                oa2.start();
            }
        });

        oa1.start();
        card.setClickable(false); // Disable clicking while flipped

        if (firstCard == null) {
            firstCard = card;
        } else {
            secondCard = card;
            isChecking = true; // We are now checking for a match
            checkForMatch();
        }
    }

    private void checkForMatch() {
        // Check if the tags (image IDs) are the same
        if (firstCard.getTag().equals(secondCard.getTag())) {
            // It's a match!
            matchedPairs++;
            updateProgressBar();

            // Disable matched cards
            firstCard.setOnClickListener(null);
            secondCard.setOnClickListener(null);

            // Check for win condition
            if (matchedPairs == pairCount) {
                winGame();
            }

            // Reset for the next turn
            resetTurn();
        } else {
            // Not a match, flip them back after a short delay
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                flipCardBack(firstCard);
                flipCardBack(secondCard);
                resetTurn();
            }, 800); // 0.8-second delay
        }
    }

    private void flipCardBack(ImageView card) {
        if (card == null) return;

        ObjectAnimator oa1 = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0f);
        ObjectAnimator oa2 = ObjectAnimator.ofFloat(card, "scaleX", 0f, 1f);
        oa1.setInterpolator(new DecelerateInterpolator());
        oa2.setInterpolator(new AccelerateDecelerateInterpolator());

        oa1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                card.setImageResource(R.drawable.card_back);
                oa2.start();
            }
        });

        oa1.start();
        card.setClickable(true); // Re-enable clicks
    }

    private void resetTurn() {
        firstCard = null;
        secondCard = null;
        isChecking = false;
    }

    private void updateProgressBar() {
        progressBar.setProgress(matchedPairs);
    }

    private void winGame() {
        timerHandler.removeCallbacks(timerRunnable); // Stop the timer
        String finalTime = timerTextView.getText().toString();

        new AlertDialog.Builder(this)
                .setTitle("You Win!")
                .setMessage("Congratulations! You found all the pairs.\n" + finalTime)
                .setPositiveButton("Play Again", (dialog, which) -> {
                    // Go back to main menu
                    finish(); // Closes the game activity
                })
                .setCancelable(false) // Prevent dismissing dialog by tapping outside
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable); // Stop timer if the user leaves the app
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (startTime != 0 && matchedPairs < pairCount) { // Resume timer if game is ongoing
            timerHandler.postDelayed(timerRunnable, 0);
        }
    }
}

