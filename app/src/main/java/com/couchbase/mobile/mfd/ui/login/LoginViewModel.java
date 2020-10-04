package com.couchbase.mobile.mfd.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Patterns;

import com.couchbase.mobile.mfd.data.LoginRepository;
import com.couchbase.mobile.mfd.util.Result;
import com.couchbase.mobile.mfd.data.model.LoggedInUser;
import com.couchbase.mobile.mfd.R;

import java.lang.ref.WeakReference;

public class LoginViewModel extends ViewModel {
    private static final String LOG_TAG = "LoginViewModel";

    private MutableLiveData<String> mUsername = new MutableLiveData<>();
    private MutableLiveData<String> mPassword = new MutableLiveData<>();
    private MutableLiveData<Boolean> mAttemptPossible = new MutableLiveData<>(false);
    private MutableLiveData<LoggedInUser> mLoggedInUser = new MutableLiveData<>();
    private LoginRepository mLoginRepository;


    public LoginViewModel(LoginRepository loginRepository) {
        super();
        mLoginRepository = loginRepository;
        mUsername.observeForever((String s) -> {
            LoginViewModel.this.checkIfAttemptPossible();
        });
        mPassword.observeForever((String s) -> {
            LoginViewModel.this.checkIfAttemptPossible();
        });

    }

    public MutableLiveData<String> getUsername() {
        return mUsername;
    }

    public MutableLiveData<String> getPassword() {
        return mPassword;
    }

    public LiveData<Boolean> isAttemptPossible() {
        return mAttemptPossible;
    }

    public LiveData<LoggedInUser> isLoggedInUser() {
        return mLoggedInUser;
    }

    public Result<LoggedInUser> login() {
        Log.d(LOG_TAG, "LoginViewModel executing login");
        Result<LoggedInUser> result = mLoginRepository.login(mUsername.getValue(), mPassword.getValue());
        Log.d(LOG_TAG, "Login result: " + result.toString());
        return result;
    }

    private void checkIfAttemptPossible() {
        String currentUsername = mUsername.getValue();
        String currentPassword = mPassword.getValue();
        if (currentUsername != null && currentPassword != null) {
            if ((currentUsername.length() >= 5) && (currentPassword.length() >= 5)) {
                mAttemptPossible.setValue(true);
                return;
            }
        }
        mAttemptPossible.setValue(false);

    }

}