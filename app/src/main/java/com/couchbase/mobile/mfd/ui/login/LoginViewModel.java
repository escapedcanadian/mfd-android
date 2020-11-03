package com.couchbase.mobile.mfd.ui.login;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.couchbase.mobile.mfd.data.LoginRepository;
import com.couchbase.mobile.mfd.data.model.User;
import com.couchbase.mobile.mfd.lite.DatabaseManager;
import com.couchbase.mobile.mfd.lite.DatabaseWrapper;
import com.couchbase.mobile.mfd.util.AppGlobals;
import com.couchbase.mobile.mfd.util.Result;

public class LoginViewModel extends ViewModel {
    private static final String LOG_TAG = "MFD_LoginViewModel";

    private MutableLiveData<String> mUsername = new MutableLiveData<>();
    private MutableLiveData<String> mPassword = new MutableLiveData<>();
    private MutableLiveData<String> mLoginError = new MutableLiveData<>();
    private MutableLiveData<Boolean> mAttemptPossible = new MutableLiveData<>(false);
    private MutableLiveData<User> mLoggedInUser = new MutableLiveData<>();
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

    public MutableLiveData<String> getLoginError() {
        return mLoginError;
    }

    public MutableLiveData<User> getLoggedInUser() {
        return mLoggedInUser;
    }

    public LiveData<Boolean> isAttemptPossible() {
        return mAttemptPossible;
    }


    public Result<User> login() {
        Log.d(LOG_TAG, "LoginViewModel executing login");
        Result<User> result = mLoginRepository.login(mUsername.getValue(), mPassword.getValue());

        Log.d(LOG_TAG, "Login result: " + result.toString());
        result.render(
                (user)->{
                    DatabaseWrapper gameInfoDB = DatabaseManager.getSharedInstance().openOrCreateDatabaseForUser(
                            AppGlobals.gameInfoDB, mUsername.getValue(), mPassword.getValue());
        },
                (message, error)->{

                }
        );
        return result;
    }

    private void checkIfAttemptPossible() {
        String currentUsername = mUsername.getValue();
        String currentPassword = mPassword.getValue();
        if (currentUsername != null && currentPassword != null) {
            if ((currentUsername.length() >= 1) && (currentPassword.length() >= 1)) {
                mAttemptPossible.setValue(true);
                return;
            }
        }
        mAttemptPossible.setValue(false);

    }

}