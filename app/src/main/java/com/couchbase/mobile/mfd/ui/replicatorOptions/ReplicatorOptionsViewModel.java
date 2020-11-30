package com.couchbase.mobile.mfd.ui.replicatorOptions;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.couchbase.mobile.mfd.lite.DatabaseWrapper;
import com.couchbase.mobile.mfd.lite.ReplicatorOptions;

public class ReplicatorOptionsViewModel extends ViewModel {


    private MutableLiveData<Boolean> mContinuous = new MutableLiveData<>();
    private MutableLiveData<DatabaseWrapper.SyncType> mSyncType = new MutableLiveData<>();
    private MutableLiveData<Boolean> mShowToasts = new MutableLiveData<>();


    public MutableLiveData<Boolean> getContinuous() {
        return mContinuous;
    }

    public MutableLiveData<DatabaseWrapper.SyncType> getSyncType() {
        return mSyncType;
    }

    public MutableLiveData<Boolean> getShowToasts() {
        return mShowToasts;
    }

    public void setSyncType(DatabaseWrapper.SyncType type) {
        mSyncType.setValue(type);
    }

    public ReplicatorOptions asReplicatorOptions() {
        return new ReplicatorOptions(
                mContinuous.getValue(),
                mSyncType.getValue(),
                mShowToasts.getValue());
    }

    public void reflectReplicatorOptions(ReplicatorOptions options) {
        mContinuous.setValue(options.isContinuous());
        mSyncType.setValue(options.getSyncType());
        mShowToasts.setValue(options.shouldShowToasts());
    }
}
