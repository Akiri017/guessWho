package com.example.santos_guesswho_mp1;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import java.util.concurrent.TimeUnit;

public class GameActivity extends AppCompatActivity {

    private int pairCount;
    private GridLayout gridLayout;
    private ArrayList<Integer> cardImages = new ArrayList<>();
    private ImageView firstCard = null;
    private ImageView secondCard = null;
    private int matchedPairs = 0;
    private boolean isChecking = false;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    private TextView timerTextView;
    private ProgressBar progressBar;
    private ImageButton buttonPause;
    private ImageButton buttonQuit;
    private boolean isPaused = false;


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

        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) gridLayout.getLayoutParams();
        if (pairCount == 4) {
            params.matchConstraintPercentWidth = 0.9f;
            gridLayout.setColumnCount(2);
            gridLayout.setRowCount(4);
        } else if (pairCount == 6) {
            params.matchConstraintPercentWidth = 0.98f;
            gridLayout.setColumnCount(3);
            gridLayout.setRowCount(4);
        } else {
            params.matchConstraintPercentWidth = 1.0f;
            gridLayout.setColumnCount(3);
            gridLayout.setRowCount(6);
        }
        gridLayout.setLayoutParams(params);

        buttonPause.setOnClickListener(v -> {
            if (isPaused) resumeGame();
            else showPauseDialog();
        });
        buttonQuit.setOnClickListener(v -> showQuitDialog());

        setupGame();
    }

    private void setupGame() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isChecking = false;
        prepareCardImages();
        Collections.shuffle(cardImages);
        matchedPairs = 0;
        progressBar.setMax(pairCount);
        progressBar.setProgress(0);
        gridLayout.removeAllViews();

        for (int i = 0; i < pairCount * 2; i++) {
            SquareImageView card = (SquareImageView) LayoutInflater.from(this)
                    .inflate(R.layout.card_layout, gridLayout, false);
            card.setImageResource(R.drawable.card_back);
            card.setTag(cardImages.get(i));
            GridLayout.LayoutParams params;
            if (pairCount == 8 && i == (pairCount * 2) - 1) {
                params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.columnSpec = GridLayout.spec(1, 1f);
                params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            } else {
                params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            }
            card.setLayoutParams(params);
            card.setOnClickListener(view -> {
                if (!isChecking && firstCard != view && (firstCard == null || secondCard == null)) {
                    flipCard((ImageView) view);
                }
            });
            gridLayout.addView(card);
        }

        long timeLimit;
        if (pairCount == 4) timeLimit = 30000; // 30 seconds
        else if (pairCount == 6) timeLimit = 60000; // 60 seconds
        else timeLimit = 90000; // 90 seconds
        timeLeftInMillis = timeLimit;
        startTimer();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(timeLeftInMillis) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(timeLeftInMillis) % 60);
                timerTextView.setText("Time: " + timeFormatted);
            }

            @Override
            public void onFinish() {
                isChecking = true;
                showLoseDialog();
            }
        }.start();
        isPaused = false;
        buttonPause.setImageResource(R.drawable.ic_pause);
    }

    private void showLoseDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Time's Up!")
                .setMessage("You ran out of time.")
                .setPositiveButton("Try Again", (dialog, which) -> setupGame()) // Restart the level
                .setNegativeButton("Quit", (dialog, which) -> finish()) // Go to main menu
                .setCancelable(false)
                .show();
    }

    private void pauseGame() {
        if (!isPaused) {
            isPaused = true;
            countDownTimer.cancel(); // Stop the timer
            buttonPause.setImageResource(R.drawable.ic_play_arrow);
        }
    }

    private void resumeGame() {
        if (isPaused) {
            startTimer();
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
        pauseGame(); // Pause the game first

        new AlertDialog.Builder(this)
                .setTitle("Quit Game?")
                .setMessage("Are you sure you want to return to the main menu?")
                .setPositiveButton("Quit", (dialog, which) -> finish())
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (wasRunning) {
                        resumeGame();
                    }
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            pauseGame();
        }
    }

    private void winGame() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        new AlertDialog.Builder(this)
                .setTitle("You Win!")
                .setMessage("Congratulations! You found all the pairs.")
                .setPositiveButton("Play Again", (dialog, which) -> setupGame())
                .setNegativeButton("Quit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

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
            if (matchedPairs == pairCount) {
                winGame();
            }
            firstCard.setOnClickListener(null);
            secondCard.setOnClickListener(null);
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
}
