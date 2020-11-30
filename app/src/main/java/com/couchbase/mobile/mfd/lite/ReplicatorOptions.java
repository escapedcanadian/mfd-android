package com.couchbase.mobile.mfd.lite;

import com.couchbase.lite.ReplicatorConfiguration;

import java.io.Serializable;

import static com.couchbase.mobile.mfd.lite.DatabaseWrapper.SyncType.PUSH_AND_PULL;

// The purpose of this class is essentially to be serializable subset of the options
// so that the activities can pass it
// back and forth and also to avoid having multiple listeners fire (and have order matter) when
// options are changed.

public class ReplicatorOptions implements Serializable {

    public static String bundleTag = "replicatorOptions";
     private boolean mContinuous;
    private DatabaseWrapper.SyncType mSyncType;
    private boolean mShowToasts;

    public ReplicatorOptions(boolean continuous, DatabaseWrapper.SyncType syncType, boolean showToasts) {
         this.mContinuous = continuous;
        this.mSyncType = syncType;
        this.mShowToasts = showToasts;
    }

    public static ReplicatorOptions withDefaults() {
        return new ReplicatorOptions(false, PUSH_AND_PULL, false);
    }

     public boolean isContinuous() {
        return mContinuous;
    }

    public DatabaseWrapper.SyncType getSyncType() {
        return mSyncType;
    }

    public boolean shouldShowToasts() {
        return mShowToasts;
    }


    public ReplicatorOptions withContinuous(boolean continuous) {
        return new ReplicatorOptions(continuous, mSyncType, mShowToasts);
    }
    public ReplicatorOptions withSyncType(DatabaseWrapper.SyncType type) {
        return new ReplicatorOptions( mContinuous, type, mShowToasts);
    }
    public ReplicatorOptions withShowToasts(boolean show) {
        return new ReplicatorOptions( mContinuous, mSyncType, show);
    }

    protected ReplicatorConfiguration.ReplicatorType getCBReplicatorType() {
        switch(mSyncType){
            case PUSH: return ReplicatorConfiguration.ReplicatorType.PUSH;
            case PULL: return ReplicatorConfiguration.ReplicatorType.PULL;
            default: return ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL;
        }
    }
}
