package com.couchbase.mobile.mfd.lite;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class DatabaseViewModelFactory implements ViewModelProvider.Factory {

    private String mRepositoryName;
    private Application mApplication;


    public DatabaseViewModelFactory(Application app, String repositoryName) {
        mApplication = app;
        mRepositoryName = repositoryName;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new DatabaseViewModel(mApplication, mRepositoryName);
    }
}
