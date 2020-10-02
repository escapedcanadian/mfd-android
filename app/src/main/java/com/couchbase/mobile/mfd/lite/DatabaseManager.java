package com.couchbase.mobile.mfd.lite;

import android.content.Context;

import com.couchbase.lite.CouchbaseLite;

import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {

    public static String LOG_TAG = "CBLite";
    private static DatabaseManager instance = null;
    static String syncGatewayEndpoint = "ws://192.168.1.81:4984";

    private Map<String, DatabaseWrapper> wrapperMap = new HashMap<>();

    private DatabaseManager() {

    }

    public static DatabaseManager getSharedInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public static void initializeCouchbaseLite(Context context) {
        CouchbaseLite.init(context);
    }

    public DatabaseWrapper openOrCreateDatabaseForUser(String databaseName, Context context, String userName, String userPassword) {
        DatabaseWrapper wrapper = new DatabaseWrapper(databaseName, context, userName, userPassword);
        wrapperMap.put(databaseName, wrapper);
        return wrapper;
    }

    public DatabaseWrapper getWrapperFor(String name) {
        return wrapperMap.get(name);
    }

    public void closeAll() {
        wrapperMap.forEach((key, wrapper) -> {
            wrapper.close();
            wrapperMap.remove(key);
        });
    }
}
