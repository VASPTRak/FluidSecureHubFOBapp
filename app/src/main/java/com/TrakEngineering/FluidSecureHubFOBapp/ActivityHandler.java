package com.TrakEngineering.FluidSecureHubFOBapp;

import android.app.Activity;

import java.util.HashMap;

/**
 * Created by Administrator on 7/5/2017.
 */

class ActivityHandler {
    public static HashMap<Integer, Activity> screenStack;

    // Add activity
    public static void addActivities(int actNo, Activity _activity) {
        if (screenStack == null) {
            screenStack = new HashMap<Integer, Activity>();
        }

        if (_activity != null && !screenStack.containsKey(actNo))
            screenStack.put(actNo, _activity);
    }

    // Remove Activity
    public static void removeActivity(int key) {
        if (screenStack != null && screenStack.size() > 0) {
            Activity _activity = screenStack.get(key);
            if (_activity != null  )
            {
                _activity.finish();
                screenStack.remove(key);
            }
        }
    }}