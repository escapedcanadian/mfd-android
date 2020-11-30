package com.couchbase.mobile.mfd.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.lang.ref.WeakReference;

public class AppGlobals {
    private static String LOG_TAG = "MFD_AppGlobals";
    private static String APP_PREFERENCES = "applicationPreferences";

    public static String gameInfoDB = "gameinfo";

    private WeakReference<Context> mContext;
    private static AppGlobals instance;
    private String mAppServer;
    private String mLastUser;
    private RequestQueue mRequestQueue;

    protected AppGlobals(Context ctx) {
        mContext = new WeakReference<>(ctx);
        instance = this;

        // Initialize the Volley request queue
        mRequestQueue = Volley.newRequestQueue(ctx);
    }

    public static AppGlobals getInstance() {
        return instance;
    }

    private SharedPreferences getAppPreferences() {
        if(mContext.get() == null) {
            Log.e(LOG_TAG, "Request for application preferences when context is null");
            throw new RuntimeException("Attempt to fetch application preferences when application context was null");
        };
        return mContext.get().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    };

    public RequestQueue requestQueue() {
        return mRequestQueue;
    }

    public boolean hasApplicationServer() {
        return getApplicationServer() != null;
    }

    public String getApplicationServer() {
        if(mAppServer != null) {return mAppServer;};
        String stored = getAppPreferences().getString("applicationServerURL", null);
        if(stored != null) {
            mAppServer = stored;
            return mAppServer;
        }
        return null;
    }

    public void setApplicationServer(String url) {
        mAppServer = url;
        getAppPreferences().edit().putString("applicationServerURL", url);
    }

    public String getLastUser() {
        if(mLastUser != null) {return mLastUser;};
        String stored = getAppPreferences().getString("lastUser", null);
        if(stored != null) {
            mLastUser = stored;
            return mLastUser;
        }
        return null;
    }

    public void setLastUser(String user) {
        mLastUser = user;
        getAppPreferences().edit().putString("lastUser", user);
    }
}
