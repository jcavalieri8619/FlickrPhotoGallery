package com.bignerdranch.android.flickrphotogallery;

import android.content.Context;
import android.preference.PreferenceManager;

public class QueryPreferences {
    private static final String TAG = "QueryPreferences";
    private static final String PREF_SEARCH_QUERY = "searchQuery";
    private static final String PREF_LAST_RESULT_ID = "lastResultID";

    private static final String PREF_IS_ALARM_ON = "isAlarmOn";

    public static boolean isAlarmOn(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(PREF_IS_ALARM_ON, false);
    }

    public static void setAlarmOn(Context ctx, boolean isOn) {
        PreferenceManager.getDefaultSharedPreferences(ctx)
                .edit()
                .putBoolean(PREF_IS_ALARM_ON, isOn)
                .apply();

    }


    public static String getStoredQuery(Context ctx) {
        return PreferenceManager
                .getDefaultSharedPreferences(ctx)
                .getString(PREF_SEARCH_QUERY, null);
    }

    public static void setStoredQuery(Context ctx, String query) {
        PreferenceManager.getDefaultSharedPreferences(ctx)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }

    public static String getLastResultID(Context ctx) {

        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getString(PREF_LAST_RESULT_ID, null);

    }

    public static void setLastResultID(Context ctx, String lastResultID) {
        PreferenceManager.getDefaultSharedPreferences(ctx)
                .edit().putString(PREF_LAST_RESULT_ID, lastResultID)
                .apply();

    }


}
