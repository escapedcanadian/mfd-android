package com.couchbase.mobile.mfd.lite;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLite;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {

    public static String LOG_TAG = "Lite-DatabaseManager";
    private static DatabaseManager instance = null;
    static String syncGatewayEndpoint = "ws://192.168.1.81:4984";

    private Map<String, DatabaseWrapper> wrapperMap = new HashMap<>();
    private Database mLocalUserRepository;
    private static WeakReference<Context> wAppContext;

    private DatabaseManager() {

    }

    public static DatabaseManager getSharedInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public static void initializeCouchbaseLite(Context context) {

        wAppContext = new WeakReference<>(context);
        CouchbaseLite.init(context);
        DatabaseManager mgr = getSharedInstance();
        try {
            // Get or create an instance of database used for local user records (not synced)
            String userRepositoryLocation = String.format("%s/users", context.getFilesDir());
            DatabaseConfiguration config = new DatabaseConfiguration();
            config.setDirectory(userRepositoryLocation);
            Database repo = new Database("localUsers", config);
            mgr.setLocalUserRepository(repo);
        } catch (CouchbaseLiteException e) {
            Log.e(DatabaseManager.LOG_TAG, "Error accessing local user repo", e);
        };

    }

    public DatabaseWrapper openOrCreateDatabaseForUser(String databaseName, String userName, String userPassword) {
        assert wAppContext != null;
        assert wAppContext.get() != null;
        DatabaseWrapper wrapper = new DatabaseWrapper(databaseName, wAppContext.get(), userName, userPassword);
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


    public Database getLocalUserRepository() {
        return mLocalUserRepository;
    }

    private void setLocalUserRepository(Database mLocalUserRepository) {
        this.mLocalUserRepository = mLocalUserRepository;
    }
}
