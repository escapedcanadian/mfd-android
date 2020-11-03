package com.couchbase.mobile.mfd.ui.register;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.couchbase.mobile.mfd.data.LoginRepository;

public class RegisterViewModelFactory implements ViewModelProvider.Factory {
    LoginRepository mLoginRepository;

    public RegisterViewModelFactory(LoginRepository repository){ mLoginRepository = repository;}

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(RegisterViewModel.class)) {
            return (T) new RegisterViewModel(mLoginRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel Class");
    }
}
