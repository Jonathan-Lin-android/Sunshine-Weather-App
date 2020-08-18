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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.example.android.sunshine.utilities.SunshineWeatherUtils;
import java.io.IOException;
import java.net.URL;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity implements Adapter.ListItemClickListener, LoaderManager.LoaderCallbacks<String []> {

    private Toast mToast;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;
    private RecyclerView mWeatherDataList;
    private Adapter mAdapter;

    private final int FORECAST_LOADER_ID = 22;
    private final String LOCATION_QUERY_URL_EXTRA = "query";
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

        getSupportLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
    }


    // queries weather site for data. returns result string[] parsed from json. below is background thread.
    @NonNull
    @Override
    public Loader<String []> onCreateLoader(final int id, @Nullable final Bundle bundle) {
        return new AsyncTaskLoader<String []>(this) {

            String[] mWeatherData = null;
            //Asynctask onPreExecute
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if(mWeatherData != null)
                    deliverResult(mWeatherData);
                else {
                    mWeatherDataList.setVisibility(View.INVISIBLE);
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            // used for when switching back and forth betweewn apps so there will be no reboots like configuration change from rotating device
            // happens after loadInBackground.
            // super.deliverResult(data) forces to skip loadInBackground
            @Override
            public void deliverResult(@Nullable final String [] data) {
                mWeatherData = data;
                super.deliverResult(data);
            }

            //Asynctask doInBackground
            @Nullable
            @Override
            public String [] loadInBackground() {
                //default location that is being queried
                String location = SunshinePreferences.getPreferredWeatherLocation(MainActivity.this);

                try {
                    //building url from location
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
        };
    }

    // parsed json result in string []. This happens on main thread.
    //Asynctask onPostExecute
    @Override
    public void onLoadFinished(@NonNull final Loader<String []> loader, final String[] weatherData) {
        //clear text if there are any
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        showWeatherDataView();

        if (weatherData != null)
            mAdapter.setWeatherData(weatherData);
        else
            showErrorMessage();
    }

    @Override
    public void onLoaderReset(@NonNull final Loader<String []> loader) {

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
            //force reset invalidates data
            mAdapter.setWeatherData(null);
            LoaderManager loaderManager = getSupportLoaderManager();
            loaderManager.restartLoader(FORECAST_LOADER_ID, null, this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(String weather) {
/*
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, weather, Toast.LENGTH_LONG);
        mToast.show();
         */
        Intent intent = new Intent(this, DetailActivity.class)
                .putExtra(Intent.EXTRA_TEXT, weather);
        if(intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
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