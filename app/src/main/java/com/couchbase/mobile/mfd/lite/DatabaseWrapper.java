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
import com.couchbase.lite.MutableDictionary;
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
import java.util.Date;
import java.util.Locale;

public class DatabaseWrapper {
    public static final String pReplicatorStatus = "Replicator Status";
    public static final String pContinuousMode = "Continuous Mode";
    public static final String pChangesPending = "Changes Pending";

    private String mDatabaseName;
    private String mDatabaseLocation;
    private String mUserName;
    private String mUserPassword;
    private Database mDatabase;
    private ListenerToken mLogListenerToken;
    private Replicator mReplicator;
    private ReplicatorConfiguration mReplicatorConfig;
    private ListenerToken mReplicatorListenerToken;
    private URI mSyncUrl = null;
    private boolean mInContinuousMode = false;
    private boolean mChangesPending = false;
    private SimpleDateFormat mSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());


    private AbstractReplicator.ActivityLevel mReplicatorStatus = AbstractReplicator.ActivityLevel.OFFLINE;

    private PropertyChangeSupport mSupport;

    DatabaseWrapper(String databaseName, Context context, String userName, String userPassword) {

        mDatabaseName = databaseName;
        mUserName = userName;
        mUserPassword = userPassword;
        mDatabaseLocation = String.format("%s/%s", context.getFilesDir(), mUserName);
        mSupport = new PropertyChangeSupport(this);


        init();
    }

    private void init() {

        DatabaseConfiguration config = new DatabaseConfiguration();
        config.setDirectory(mDatabaseLocation);

        Log.i("Demo-DB", "Will open CBLite for user " + mUserName + " in directory " + config.getDirectory());

        try {
            mDatabase = new Database(mDatabaseName, config);
            Log.i("Demo-DB", "Local user's CBLite database '" + mDatabaseName + "' contains " + mDatabase.getCount() + " records ");

            registerForDatabaseChanges();

            try {
                mSyncUrl = new URI(String.format("%s/%s", DatabaseManager.syncGatewayEndpoint, mDatabaseName));
            } catch (URISyntaxException e) {
                mSyncUrl = null;
                e.printStackTrace();
            }

            mReplicatorConfig = new ReplicatorConfiguration(mDatabase, new URLEndpoint(mSyncUrl));
            mReplicatorConfig.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL);

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

            for (String docId : change.getDocumentIDs()) {
                Document doc = mDatabase.getDocument(docId);
                if (doc != null) {
                    Log.i("Demo-DB-Change", "Document was added/updated: " + docId);
                } else {

                    Log.i("Demo-DB-Change", "Document was deleted: " + docId);
                }
            }
        });
    }


    public void startReplication(boolean continuous) {
        mReplicatorConfig.setContinuous(continuous);
        setInContinuousMode(continuous);
        mReplicator = new Replicator(mReplicatorConfig);


        mReplicatorListenerToken = mReplicator.addChangeListener(new ReplicatorChangeListener() {
            @Override
            public void changed(ReplicatorChange change) {
                AbstractReplicator.ActivityLevel newStatus = change.getReplicator().getStatus().getActivityLevel();
                switch (newStatus) {
                    case IDLE:
                        mSupport.firePropertyChange(pReplicatorStatus, mReplicatorStatus, AbstractReplicator.ActivityLevel.IDLE);
                        setChangesPending(false);
                        Log.i("Demo-DB-Replication", "Scheduler Completed, Idle status");
                        break;
                    case CONNECTING:
                        mSupport.firePropertyChange(pReplicatorStatus, mReplicatorStatus, AbstractReplicator.ActivityLevel.CONNECTING);
                        Log.i("Demo-DB-Replication", "Replicator Connecting");
                        break;
                    case BUSY:
                        mSupport.firePropertyChange(pReplicatorStatus, mReplicatorStatus, AbstractReplicator.ActivityLevel.BUSY);
                        Log.i("Demo-DB-Replication", "Replicator Transferring Data");
                        break;
                    case STOPPED:
                        mSupport.firePropertyChange(pReplicatorStatus, mReplicatorStatus, AbstractReplicator.ActivityLevel.STOPPED);
                        setChangesPending(false);
                        Log.i("Demo-DB-Replication", "Replication Completed");
                        break;
                    case OFFLINE:
                        mSupport.firePropertyChange(pReplicatorStatus, mReplicatorStatus, AbstractReplicator.ActivityLevel.OFFLINE);
                        Log.w("Demo-DB-Replication", "Replication Offline");
                        break;
                    default:
                        Log.e("Demo-DB-Replication", "Unexpected Replication Activity Level: " + newStatus.name());
                }
                mReplicatorStatus = newStatus;
            }
        });

        mReplicator.start();
        Log.i("Demo-DB", mDatabaseName + " connected to Sync Gateway at " + mSyncUrl.toString());

    }


    public void stopReplication() {
        Log.i("Demo-DB", mDatabaseName + " Stop Replication Requested");
        if (mReplicator != null) {
            mReplicator.stop();
            mReplicator.removeChangeListener(mReplicatorListenerToken);
            mReplicator = null;
        }
        setInContinuousMode(false);
        setpReplicatorStatus(AbstractReplicator.ActivityLevel.OFFLINE);
    }

    public AbstractReplicator.ActivityLevel getReplicatorStatus() {
        return mReplicatorStatus;
    }

    private void setpReplicatorStatus(AbstractReplicator.ActivityLevel status) {
        mSupport.firePropertyChange(pReplicatorStatus, mReplicatorStatus, status);
        mReplicatorStatus = status;

    }

    private void setInContinuousMode(boolean continuous) {
        if (continuous != mInContinuousMode) {
            mSupport.firePropertyChange(pContinuousMode, mInContinuousMode, continuous);
            mInContinuousMode = continuous;
        }
    }

    private void setChangesPending(boolean pending) {
        if(mChangesPending != pending) {
            mSupport.firePropertyChange(pChangesPending, mChangesPending, pending);
            mChangesPending = pending;
        }
    }

    public boolean isInContinuousMode() {
        return mInContinuousMode;
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
            // This added a 'lastUpdate' subDocument
            addUpdateEvent(document);

            mDatabase.save(document);
            setChangesPending(true);
            return true;
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void addUpdateEvent(MutableDocument document) {

        MutableDictionary event = new MutableDictionary();
        event.setString("region","Africa");
        // This call would require API 26
        // event.setString("timestamp", Instant.now().toString());
        event.setString("timestamp", mSdf.format(new Date()));
        event.setString("action", "FieldUpdate");
        event.setString("agent", mUserName);
        document.setDictionary("lastUpdate", event);

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
