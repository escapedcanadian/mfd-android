package com.couchbase.mobile.mfd.lite;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import java.util.Collections;
import java.util.List;

public class DatabaseInitializer implements Initializer<DatabaseManager> {

    @NonNull
    @Override
    public DatabaseManager create(@NonNull Context context) {
        Log.d(DatabaseManager.LOG_TAG, "Initializing Database Manager");
        DatabaseManager.initializeCouchbaseLite(context);
        DatabaseManager dbMgr = DatabaseManager.getSharedInstance();
        return dbMgr;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.EMPTY_LIST;
    }
}
