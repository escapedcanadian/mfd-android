package com.couchbase.mobile.mfd.lite;

import android.app.Application;
import android.util.Log;
import android.widget.CompoundButton;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.couchbase.lite.AbstractReplicator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class DatabaseViewModel extends AndroidViewModel implements PropertyChangeListener {

    private String mRepositoryName;
    private DatabaseWrapper mDatabaseWrapper;


    public DatabaseViewModel(Application application, String repo) {
        super(application);
        mRepositoryName = repo;
        init();

    }

    private MutableLiveData<AbstractReplicator.ActivityLevel> mReplicationStatus;

    public LiveData<AbstractReplicator.ActivityLevel> replicationStatus() {
        return mReplicationStatus;
    }

    private MutableLiveData<Boolean> mReplicationInProgress;

    public LiveData<Boolean> replicationInProgress() {
        return mReplicationInProgress;
    }

    private MutableLiveData<Boolean> mInContinuousMode;

    public LiveData<Boolean> inContinuousMode() {
        return mInContinuousMode;
    }

    private MutableLiveData<Boolean> mHasPendingChanges;
    public LiveData<Boolean> hasPendingChanges() {
        return mHasPendingChanges;
    }


    private void init() {

        mReplicationStatus = new MutableLiveData<>();
        mReplicationStatus.setValue(AbstractReplicator.ActivityLevel.OFFLINE);

        mReplicationInProgress = new MutableLiveData<>();
        mReplicationInProgress.setValue(false);

        mInContinuousMode = new MutableLiveData<>();
        mInContinuousMode.setValue(false);

        mHasPendingChanges = new MutableLiveData<>();
        mHasPendingChanges.setValue(false);

        mDatabaseWrapper = DatabaseManager.getSharedInstance().getWrapperFor(mRepositoryName);
        if (mDatabaseWrapper != null) {
            mDatabaseWrapper.registerListener(this);
            mReplicationStatus.setValue(mDatabaseWrapper.getReplicatorStatus());
            mHasPendingChanges.setValue(mDatabaseWrapper.hasChangesPending());
            mInContinuousMode.setValue((mDatabaseWrapper.isInContinuousMode()));


        } else {
            Log.e("Demo-DB", "No DatabaseWrapper found for " + mRepositoryName);
        }
    }


    public void onReplicateClicked() {
        Log.i("Demo-Sync", "One shot replication button pushed");
        mDatabaseWrapper.startReplication(false);
    }

    public void onResetClicked() {

        Log.i("Demo-Sync", "Reset button pushed");
        mDatabaseWrapper.reload();
    }

    public void onCancelClicked() {
        Log.i("Demo-Sync", "Cancel button pushed");
        mDatabaseWrapper.stopReplication();
    }

    public void onReplicationModeChanged(CompoundButton buttonView, boolean isChecked){

        if (isChecked) {
            mDatabaseWrapper.startReplication(true);
        } else {
            mDatabaseWrapper.stopReplication();
        }
    }

    // Process events from the DatabaseManager
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case DatabaseWrapper.pReplicatorStatus:
                AbstractReplicator.ActivityLevel status = (AbstractReplicator.ActivityLevel)evt.getNewValue();
                if ((status == AbstractReplicator.ActivityLevel.OFFLINE) || (status == AbstractReplicator.ActivityLevel.STOPPED)) {
                    mReplicationInProgress.setValue(false);
                } else {
                    mReplicationInProgress.setValue(true);
                }
                mReplicationStatus.setValue(status);

                break;
            case DatabaseWrapper.pContinuousMode:
                mInContinuousMode.setValue((boolean) evt.getNewValue());
                break;
            case DatabaseWrapper.pChangesPending:
                mHasPendingChanges.setValue((boolean) evt.getNewValue());
                break;
            default:
                Log.i("Demo-Sync", "Unexpected property change " + evt.getPropertyName() + " from " + mRepositoryName);
        }
    }


}
