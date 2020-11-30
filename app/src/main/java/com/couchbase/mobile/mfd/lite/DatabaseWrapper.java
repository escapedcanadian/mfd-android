package com.couchbase.mobile.mfd.lite;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.AbstractReplicator;
import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
import com.couchbase.lite.ListenerToken;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorChange;
import com.couchbase.lite.ReplicatorChangeListener;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.URLEndpoint;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseWrapper {
    public static final String pReplicatorStatus = "Replicator Status";
    public static final String pReplicatorOptions = "ReplicatorOptions";
    public static final String pChangesPending = "Changes Pending";

    private static final String LOG_TAG = "Lite_DatabaseWrapper";

    private String mDatabaseName;
    private String mDatabaseLocation;
    private String mUserName;
    private String mUserPassword;
    private Database mDatabase;
    private ListenerToken mLogListenerToken;
    private Replicator mReplicator;
    private ReplicatorOptions mReplicatorOptions;
    private ReplicatorConfiguration mReplicatorConfig;
    private ListenerToken mReplicatorListenerToken;
    private URI mSyncUrl = null;
    private boolean mChangesPending = false;
    private SimpleDateFormat mSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
    private List<DatabaseUpdateListener> mUpdateListeners = new ArrayList<>();


    private AbstractReplicator.ActivityLevel mReplicatorStatus = AbstractReplicator.ActivityLevel.OFFLINE;


    private PropertyChangeSupport mSupport;

    public enum SyncType {
        PUSH_AND_PULL, PUSH, PULL
    }

    DatabaseWrapper(String databaseName, Context context, String userName, String userPassword) {

        mDatabaseName = databaseName;
        mUserName = userName;
        mUserPassword = userPassword;
        mDatabaseLocation = String.format("%s/data/%s", context.getFilesDir(), mUserName);
        mSupport = new PropertyChangeSupport(this);


        init();
    }

    private void init() {

        DatabaseConfiguration config = new DatabaseConfiguration();
        config.setDirectory(mDatabaseLocation);

        Log.i(LOG_TAG, "Will open CBLite for user " + mUserName + " in directory " + config.getDirectory());

        try {
            mDatabase = new Database(mDatabaseName, config);
            Log.i(LOG_TAG, "Local user's CBLite database '" + mDatabaseName + "' contains " + mDatabase.getCount() + " records ");

            registerForDatabaseChanges();

            try {
                mSyncUrl = new URI(String.format("%s/%s", DatabaseManager.syncGatewayEndpoint, mDatabaseName));
            } catch (URISyntaxException e) {
                mSyncUrl = null;
                e.printStackTrace();
            }

            mReplicatorOptions = ReplicatorOptions.withDefaults();
            mReplicatorConfig = new ReplicatorConfiguration(mDatabase, new URLEndpoint(mSyncUrl));
            mReplicatorConfig.setReplicatorType(mReplicatorOptions.getCBReplicatorType());
            mReplicatorConfig.setContinuous(mReplicatorOptions.isContinuous());
            mReplicatorConfig.setAuthenticator(new BasicAuthenticator(mUserName, mUserPassword));
            //mReplicatorConfig.setChannels(Arrays.asList("channel." + mUserName));

            mReplicator = new Replicator(mReplicatorConfig);

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public Database getDatabase() {
        // Would love to remove this method, but it would complicate building queries etc
        // This could provide a leak to updates that would not be tracked by the wrapper
        return mDatabase;
    }

    public String getUserName() {
        return mUserName;
    }

    public SimpleDateFormat getDateFormatter() {
        return mSdf;
    }

    private void registerForDatabaseChanges() {
        // Add database change listener
        mLogListenerToken = mDatabase.addChangeListener(change -> {

            if(!mUpdateListeners.isEmpty()) {
                for (String docId : change.getDocumentIDs()) {
                    Document doc = mDatabase.getDocument(docId);
                    DatabaseUpdate update = new DatabaseUpdate(this, docId, doc);
                    for (DatabaseUpdateListener listener: mUpdateListeners) {
                        listener.onUpdate(update);
                    }
                }
            }
        });
    }



    public void startReplication() {
        mReplicator = new Replicator(mReplicatorConfig);

        mReplicatorListenerToken = mReplicator.addChangeListener(new ReplicatorChangeListener() {
            @Override
            public void changed(ReplicatorChange change) {
                AbstractReplicator.ActivityLevel newStatus = change.getReplicator().getStatus().getActivityLevel();
                switch (newStatus) {
                    case IDLE:
                        mSupport.firePropertyChange(pReplicatorStatus, mReplicatorStatus, AbstractReplicator.ActivityLevel.IDLE);
                        setChangesPending(false);
                        Log.i(LOG_TAG, "Scheduler Completed, Idle status");
                        break;
                    case CONNECTING:
                        mSupport.firePropertyChange(pReplicatorStatus, mReplicatorStatus, AbstractReplicator.ActivityLevel.CONNECTING);
                        Log.i(LOG_TAG, "Replicator Connecting");
                        break;
                    case BUSY:
                        mSupport.firePropertyChange(pReplicatorStatus, mReplicatorStatus, AbstractReplicator.ActivityLevel.BUSY);
                        Log.i(LOG_TAG, "Replicator Transferring Data");
                        break;
                    case STOPPED:
                        mSupport.firePropertyChange(pReplicatorStatus, mReplicatorStatus, AbstractReplicator.ActivityLevel.STOPPED);
                        setChangesPending(false);
                        Log.i(LOG_TAG, "Replication Completed");
                        break;
                    case OFFLINE:
                        mSupport.firePropertyChange(pReplicatorStatus, mReplicatorStatus, AbstractReplicator.ActivityLevel.OFFLINE);
                        Log.w(LOG_TAG, "Replication Offline");
                        break;
                    default:
                        Log.e(LOG_TAG, "Unexpected Replication Activity Level: " + newStatus.name());
                }
                mReplicatorStatus = newStatus;
            }
        });

        mReplicator.start(false);
        Log.i(LOG_TAG, mDatabaseName + " connected to Sync Gateway at " + mSyncUrl.toString());

    }

    public void addUpdateListener(DatabaseUpdateListener listener) {
        mUpdateListeners.add(listener);
    }

    public void stopReplication() {
        Log.i(LOG_TAG, mDatabaseName + " Stop Replication Requested");
        if (mReplicator != null) {
            mReplicator.stop();
            mReplicator.removeChangeListener(mReplicatorListenerToken);
            mReplicator = null;
        }
        mReplicatorOptions = mReplicatorOptions.withContinuous(false);
        setpReplicatorStatus(AbstractReplicator.ActivityLevel.OFFLINE);
    }

    public AbstractReplicator.ActivityLevel getReplicatorStatus() {
        return mReplicatorStatus;
    }


    private void setpReplicatorStatus(AbstractReplicator.ActivityLevel status) {
        mSupport.firePropertyChange(pReplicatorStatus, mReplicatorStatus, status);
        mReplicatorStatus = status;

    }

    public void setReplicatorOptions(ReplicatorOptions options) {
        mReplicatorOptions = options;
        // TODO change the replicator configuration

        mSupport.firePropertyChange(pReplicatorOptions, mReplicatorOptions, options);
    }

    public ReplicatorOptions getReplicatorOptions() {
        return mReplicatorOptions;
    }

    private void setChangesPending(boolean pending) {
        if(mChangesPending != pending) {
            mSupport.firePropertyChange(pChangesPending, mChangesPending, pending);
            mChangesPending = pending;
        }
    }

    public boolean hasChangesPending() {
        return mChangesPending;
    }

    public void close() {
        if (mLogListenerToken != null) {
            mDatabase.removeChangeListener(mLogListenerToken);
        }
        setpReplicatorStatus(AbstractReplicator.ActivityLevel.OFFLINE);
    }

    public void registerListener(PropertyChangeListener pcl) {
        mSupport.addPropertyChangeListener(pcl);
    }

    public void dergisterListener(PropertyChangeListener pcl) {
        mSupport.removePropertyChangeListener(pcl);
    }


    // Passthroughs for the database

    public Document getDocument(String id) {
        return mDatabase.getDocument(id);
    }

    public boolean saveDocument(MutableDocument document) {
        try {

            mDatabase.save(document);
            setChangesPending(true);
            return true;
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return false;
        }
    }


    public void reload() {
        try {
            mDatabase.delete();
            init();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }
}
