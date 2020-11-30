package com.couchbase.mobile.mfd;

import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.couchbase.lite.AbstractReplicator;
import com.couchbase.mobile.mfd.data.model.User;
import com.couchbase.mobile.mfd.lite.DatabaseManager;
import com.couchbase.mobile.mfd.lite.DatabaseUpdate;
import com.couchbase.mobile.mfd.lite.DatabaseUpdateListener;
import com.couchbase.mobile.mfd.lite.DatabaseWrapper;
import com.couchbase.mobile.mfd.lite.ReplicatorOptions;
import com.couchbase.mobile.mfd.util.AppGlobals;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {
    private static String LOG_TAG = "MFD_MainViewModel";

    private MutableLiveData<ReplicatorOptions> mReplicatorOptions = new MutableLiveData<>();
    private MutableLiveData<AbstractReplicator.ActivityLevel> mReplicatorStatus = new MutableLiveData<>();
    private MutableLiveData<Boolean> mConnected = new MutableLiveData<>(false);
    private List<DatabaseUpdate> mUpdateList = new ArrayList<>();


    private User mLoggedInUser;
    private DatabaseWrapper mGameInfoDB;

    private PropertyChangeListener mReplicatorStatusListener = (event) -> {
        switch(event.getPropertyName()) {
            case DatabaseWrapper.pReplicatorStatus:
                mReplicatorStatus.setValue((AbstractReplicator.ActivityLevel) event.getNewValue());
                break;
            default:
                Log.e(LOG_TAG, "Unexpected property change from DB wrapper: " + event.getPropertyName());
        }
    };


    public void setLoggedInUser(User user) {
        mGameInfoDB = DatabaseManager.getSharedInstance().getWrapperFor(AppGlobals.gameInfoDB);
        if(mGameInfoDB ==null) {
            Log.e(LOG_TAG, "No gameInfo DB wrapper found in manager");
            return;
        }
        Log.i(LOG_TAG, "Connected to gameInfoDB");
        mReplicatorOptions.setValue(mGameInfoDB.getReplicatorOptions());
        mGameInfoDB.registerListener(mReplicatorStatusListener);
    }

    public MutableLiveData<ReplicatorOptions> getReplicatorOptions() {
        return mReplicatorOptions;
    }

    public MutableLiveData<Boolean> getConnected() {
        return mConnected;
    }

    public void startReplication() {
        mGameInfoDB.startReplication();
    }

    public MutableLiveData<AbstractReplicator.ActivityLevel> getReplicatorStatus() {
        return mReplicatorStatus;
    }

    public List<DatabaseUpdate> getUpdateList() {
        return mUpdateList;
    }

    public void addDatabaseUpdateListener(LifecycleOwner owner, DatabaseUpdateListener listener) {
        // This needs to be reworked so that the lifecycle owner doesn't get notified if inactive
        if(mGameInfoDB != null) {
            mGameInfoDB.addUpdateListener(listener);
        } else {
            Log.e(LOG_TAG, "Attempt to add a listener when the database was null");
        }
    }

    public void testUpdateList() {
        DatabaseUpdate update = new DatabaseUpdate(mGameInfoDB, "test:0", null);
        mUpdateList.add(update);
    }
}
