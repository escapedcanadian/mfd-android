package com.couchbase.mobile.mfd.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import java.util.Collections;
import java.util.List;

public class AppGlobalsInitializer implements Initializer<AppGlobals> {

    @NonNull
    @Override
    public AppGlobals create(@NonNull Context context) {
        Log.d("MFD_AppGlobals", "Initializing AppGlobals");
        return new AppGlobals(context);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.EMPTY_LIST;
    }
}
