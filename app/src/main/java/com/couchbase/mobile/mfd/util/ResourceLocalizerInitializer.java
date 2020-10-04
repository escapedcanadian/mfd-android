package com.couchbase.mobile.mfd.util;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import com.couchbase.mobile.mfd.lite.DatabaseManager;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

public class ResourceLocalizerInitializer implements Initializer<ResourceLocalizer> {

    @NonNull
    @Override
    public ResourceLocalizer create(@NonNull Context context) {
        Log.d("ResourceLocalizer", "Initializing Database Manager");
        return new ResourceLocalizer(context);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.EMPTY_LIST;
    }
}
