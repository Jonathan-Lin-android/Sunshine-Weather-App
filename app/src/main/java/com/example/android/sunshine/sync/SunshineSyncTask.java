package com.example.android.sunshine.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import com.example.android.sunshine.MainActivity;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;
import java.io.IOException;
import java.net.URL;
import org.json.JSONException;

public class SunshineSyncTask
{
    synchronized public static void syncWeather(Context context)
    {
        try {
            URL weatherRequestUrl = NetworkUtils.getUrl(context);

            //query database json
            String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);
            //parse json results
            ContentValues[] weatherValues = OpenWeatherJsonUtils.getWeatherContentValuesFromJson(context, jsonWeatherResponse);

            //valid results
            if(weatherValues != null || weatherValues.length != 0)
            {
                ContentResolver resolver = context.getContentResolver();
                //delte old data
                resolver.delete(WeatherEntry.CONTENT_URI, null, null);
                resolver.bulkInsert(WeatherEntry.CONTENT_URI, weatherValues);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
