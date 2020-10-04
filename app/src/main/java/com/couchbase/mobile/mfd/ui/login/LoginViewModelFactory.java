package com.couchbase.mobile.mfd.ui.login;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.couchbase.mobile.mfd.data.LoginRepository;

public class LoginViewModelFactory implements ViewModelProvider.Factory {

    LoginRepository mLoginRepository;

    public LoginViewModelFactory(LoginRepository repository) {
        mLoginRepository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(mLoginRepository);
        }
        //noinspection unchecked
        throw new IllegalArgumentException("Unknown ViewModel class");
    }

}
