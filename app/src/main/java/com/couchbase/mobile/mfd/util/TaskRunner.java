package com.couchbase.mobile.mfd.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.couchbase.mobile.mfd.R;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskRunner {
    private static final String LOG_TAG = "TaskRunner";
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public interface Callback<R> {
        void onComplete(R result);
    }

    public static  <R> void executeSerialAsync(Callable<R> callable, Callback<R> callback) {
        executor.execute(() -> {
            try {
                final R result = callable.call();
                handler.post(() -> {
                    callback.onComplete(result);
                });
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error in executing async call", e);
            }
        });
    }
}
