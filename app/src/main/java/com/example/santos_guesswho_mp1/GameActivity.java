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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    // (All your existing variables are here and correct)
    private int pairCount;
    private GridLayout gridLayout;
    private ArrayList<Integer> cardImages = new ArrayList<>();
    private ImageView firstCard = null;
    private ImageView secondCard = null;
    private int matchedPairs = 0;
    private boolean isChecking = false;
    private TextView timerTextView;
    private long startTime = 0;
    private Handler timerHandler = new Handler();
    private boolean isPaused = false;
    private ProgressBar progressBar;
    private ImageButton buttonPause;
    private ImageButton buttonQuit;
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            timerTextView.setText(String.format(Locale.getDefault(), "Time: %02d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        pairCount = getIntent().getIntExtra("PAIR_COUNT", 4);
        gridLayout = findViewById(R.id.gridLayoutCards);
        timerTextView = findViewById(R.id.textViewTimer);
        progressBar = findViewById(R.id.progressBar);
        buttonPause = findViewById(R.id.buttonPause);
        buttonQuit = findViewById(R.id.buttonQuit);

        if (pairCount == 6) gridLayout.setColumnCount(4);
        else if (pairCount == 8) gridLayout.setColumnCount(4);
        else gridLayout.setColumnCount(4);

        buttonPause.setOnClickListener(v -> {
            if (isPaused) resumeGame();
            else showPauseDialog();
        });
        buttonQuit.setOnClickListener(v -> showQuitDialog());

        setupGame();
    }

    private void setupGame() {
        prepareCardImages();
        Collections.shuffle(cardImages);
        matchedPairs = 0;
        progressBar.setMax(pairCount);
        progressBar.setProgress(0);
        gridLayout.removeAllViews();

        // This loop has the critical changes
        for (int i = 0; i < pairCount * 2; i++) {
            // Inflate our custom SquareImageView
            SquareImageView card = (SquareImageView) LayoutInflater.from(this)
                    .inflate(R.layout.card_layout, gridLayout, false);

            card.setImageResource(R.drawable.card_back);
            card.setTag(cardImages.get(i));

            // Set up the Layout Parameters
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0; // This allows the columnSpec weight to work
            params.height = GridLayout.LayoutParams.WRAP_CONTENT; // This forces the grid to respect our onMeasure method
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Distribute width evenly
            // We DO NOT set a rowSpec weight, letting the height be natural.
            card.setLayoutParams(params);


            card.setOnClickListener(view -> {
                if (!isChecking && firstCard != view && (firstCard == null || secondCard == null)) {
                    flipCard((ImageView) view);
                }
            });
            gridLayout.addView(card);
        }
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
    }

    // --- PAUSE, RESUME, AND QUIT METHODS ---

    private void pauseGame() {
        if (!isPaused) {
            isPaused = true;
            timerHandler.removeCallbacks(timerRunnable);
            buttonPause.setImageResource(R.drawable.ic_play_arrow);
        }
    }

    private void resumeGame() {
        if (isPaused) {
            isPaused = false;
            timerHandler.postDelayed(timerRunnable, 0);
            buttonPause.setImageResource(R.drawable.ic_pause);
        }
    }

    private void showPauseDialog() {
        pauseGame();
        new AlertDialog.Builder(this)
                .setTitle("Game Paused")
                .setPositiveButton("Resume", (dialog, which) -> resumeGame())
                .setCancelable(false)
                .show();
    }

    private void showQuitDialog() {
        boolean wasRunning = !isPaused;
        pauseGame(); // Pause regardless

        new AlertDialog.Builder(this)
                .setTitle("Quit Game?")
                .setMessage("Are you sure you want to return to the main menu?")
                .setPositiveButton("Quit", (dialog, which) -> finish())
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (wasRunning) {
                        resumeGame(); // Only resume if it was running before
                    }
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }


    // --- LIFECYCLE AND GAME LOGIC METHODS (Mostly unchanged) ---
    @Override
    protected void onPause() {
        super.onPause();
        pauseGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Do not auto-resume here. The user must press the resume button.
    }

    // (The rest of your methods like prepareCardImages, flipCard, checkForMatch, etc., are unchanged and correct)
    private void prepareCardImages() {
        cardImages.clear();
        for (int i = 1; i <= pairCount; i++) {
            int resourceId = getResources().getIdentifier("card_" + i, "drawable", getPackageName());
            cardImages.add(resourceId);
            cardImages.add(resourceId);
        }
    }
    private void flipCard(ImageView card) {
        ObjectAnimator oa1 = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0f);
        ObjectAnimator oa2 = ObjectAnimator.ofFloat(card, "scaleX", 0f, 1f);
        oa1.setInterpolator(new DecelerateInterpolator());
        oa2.setInterpolator(new AccelerateDecelerateInterpolator());
        oa1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                card.setImageResource((Integer) card.getTag());
                oa2.start();
            }
        });
        oa1.start();
        card.setClickable(false);
        if (firstCard == null) {
            firstCard = card;
        } else {
            secondCard = card;
            isChecking = true;
            checkForMatch();
        }
    }
    private void checkForMatch() {
        if (firstCard.getTag().equals(secondCard.getTag())) {
            matchedPairs++;
            updateProgressBar();
            firstCard.setOnClickListener(null);
            secondCard.setOnClickListener(null);
            if (matchedPairs == pairCount) {
                winGame();
            }
            resetTurn();
        } else {
            new Handler().postDelayed(() -> {
                flipCardBack(firstCard);
                flipCardBack(secondCard);
                resetTurn();
            }, 800);
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
        card.setClickable(true);
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
        timerHandler.removeCallbacks(timerRunnable);
        String finalTime = timerTextView.getText().toString();
        new AlertDialog.Builder(this)
                .setTitle("You Win!")
                .setMessage("Congratulations! You found all the pairs.\n" + finalTime)
                .setPositiveButton("Play Again", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
