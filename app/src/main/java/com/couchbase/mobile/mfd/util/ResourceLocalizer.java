package com.couchbase.mobile.mfd.util;

import android.content.Context;

import java.lang.ref.WeakReference;

public class ResourceLocalizer {
    private WeakReference<android.content.Context> mContext;
    private static ResourceLocalizer instance;

   protected ResourceLocalizer(Context ctx) {
       mContext = new WeakReference<>(ctx);
       instance = this;
   }

    public static String getLocalizedString(int id) {
       return instance.mContext.get().getResources().getString(id);
    }
}
