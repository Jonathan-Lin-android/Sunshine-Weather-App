package com.example.android.sunshine.sync;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

public class SunshineSyncUtils
{
    public static void startImmediateSync(@NonNull Context context)
    {
        Intent syncWeatherIntent = new Intent(context, SunshineSyncIntentService.class);
        context.startService(syncWeatherIntent);
    }
}