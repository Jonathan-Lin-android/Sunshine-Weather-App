/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;
import java.io.IOException;
import java.net.URL;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity implements Adapter.ListItemClickListener {

    private Toast mToast;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;
    private RecyclerView mWeatherDataList;
    private Adapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        /*
         * Using findViewById, we get a reference to our TextView from xml. This allows us to
         * do things like set the text of the TextView.
         */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        //set up recycler view
        mWeatherDataList = (RecyclerView) findViewById(R.id.rv_weather_data_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mWeatherDataList.setLayoutManager(layoutManager);
        mWeatherDataList.setHasFixedSize(true);
        mAdapter = new Adapter(this);
        mWeatherDataList.setAdapter(mAdapter);
        //set up adapter?
        loadWeatherData();
    }

    void loadWeatherData() {
        new FetchWeatherTask().execute(SunshinePreferences.getPreferredWeatherLocation(this));
}

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWeatherDataList.setVisibility(View.INVISIBLE);
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }
            String location = params[0];
            try {
                //location is first param
                URL url = NetworkUtils.buildUrl(location);
                String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(url);
              Log.d("testt", url.toString());
                String[] jsonWeatherData = OpenWeatherJsonUtils
                        .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);
                return jsonWeatherData;
            } catch (IOException | JSONException e) {
                showErrorMessage();
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] weatherData) {
            //clear text if there are any
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            showWeatherDataView();

            if (weatherData != null)
                mAdapter.setWeatherData(weatherData);
            else
                showErrorMessage();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.forecast, menu);
        return true;
    }

    @Override
    /*
    When you successfully handle a menu item, return true. If you don't handle the menu item,
    you should call the superclass implementation of onOptionsItemSelected() (the default implementation returns false).
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemThatWasSelected = item.getItemId();
        if (menuItemThatWasSelected == R.id.action_refresh) {
            //force reset
            mAdapter.setWeatherData(null);
            loadWeatherData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(String weather) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, weather, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void showWeatherDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mWeatherDataList.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mWeatherDataList.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

}