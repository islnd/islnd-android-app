package io.islnd.android.islnd.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;


import io.islnd.android.islnd.app.R;

public class SplashScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // TODO: Do start up stuff
        Handler handler = new Handler();
        Runnable runnable = () -> {
            startActivity(new Intent(SplashScreenActivity.this, NavBaseActivity.class));
        };

        handler.postDelayed(runnable, 1500);
    }
}
