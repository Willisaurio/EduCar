package com.aiproj.educar;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {

    private static final String PREFS_NAME = "EduCarPrefs";
    private static final String KEY_LOGGED = "logged";

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Checks if the user is currently logged in.
     *
     * @param context the application or activity context
     * @return true if logged in, false otherwise (default is false)
     */
    public static boolean isLogged(Context context) {
        return getPreferences(context).getBoolean(KEY_LOGGED, false);
    }

    /**
     * Saves the logged status in SharedPreferences.
     *
     * @param context the application or activity context
     * @param logged  boolean indicating whether the user is logged in
     */
    public static void setLogged(Context context, boolean logged) {
        getPreferences(context).edit().putBoolean(KEY_LOGGED, logged).apply();
    }
}
