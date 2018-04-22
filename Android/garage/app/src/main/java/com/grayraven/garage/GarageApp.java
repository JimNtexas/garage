package com.grayraven.garage;

import android.app.Application;
import android.content.Context;

public class GarageApp extends Application {
    private static Context context;

    public void onCreate(){
        super.onCreate();
        GarageApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return GarageApp.context;
    }
}
