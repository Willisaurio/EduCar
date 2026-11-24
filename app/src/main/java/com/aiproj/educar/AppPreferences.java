package com.aiproj.educar;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {

    private static final String PREFS_NAME = "EduCarPrefs";
    private static final String KEY_LOGGED = "logged";
    private static final String KEY_USER = "user";

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


    /**
     * Retrieves the username from SharedPreferences.
     *
     * @param context the application or activity context
     * @return the username as a string, or null if not found
     */
    public static String getUser(Context context) {
        return getPreferences(context).getString(KEY_USER, null);
    }
    /**
     * Saves the username in SharedPreferences.
     *
     * @param context the application or activity context
     * @param user    the username to save
     */
    public static void setUser(Context context, String user) {
        getPreferences(context).edit().putString(KEY_USER, user).apply();
    }
}
