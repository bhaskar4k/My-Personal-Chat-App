package com.mypersonalchatapp;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private AnimationDrawable backgroundAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backgroundAnimation = (AnimationDrawable) root.getBackground();
        backgroundAnimation.setEnterFadeDuration(2500);
        backgroundAnimation.setExitFadeDuration(2500);

        // Onclick event to redirect to chatting page
        TextView startButton = findViewById(R.id.startChattingButton);
        startButton.setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class))
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        backgroundAnimation.start();
    }

    @Override
    protected void onStop() {
        backgroundAnimation.stop();
        super.onStop();
    }
}
