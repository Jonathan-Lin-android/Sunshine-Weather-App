package com.example.android.sunshine;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        String weatherStr = intent.getStringExtra(Intent.EXTRA_TEXT);

        TextView displayWeatherTV = findViewById(R.id.tv_display_weather);
        displayWeatherTV.setText(weatherStr);
    }
}
