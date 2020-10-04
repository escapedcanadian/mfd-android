package com.couchbase.mobile.mfd.ui.login;

import android.app.Activity;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.mobile.mfd.R;
import com.couchbase.mobile.mfd.data.LoginRepository;
import com.couchbase.mobile.mfd.data.model.LoggedInUser;
import com.couchbase.mobile.mfd.databinding.ActivityLoginBinding;
import com.couchbase.mobile.mfd.util.Result;
import com.couchbase.mobile.mfd.util.TaskRunner;
import com.google.android.material.textfield.TextInputLayout;


public class LoginActivity extends AppCompatActivity {
    private static final String LOG_TAG = "LoginActivity";

    private LoginViewModel loginViewModel;
    private Button mLoginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The logic for this activity is implemented in the ViewModel and connected via data bindings
        LoginViewModelFactory factory = new LoginViewModelFactory(LoginRepository.getInstance());
        loginViewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);
        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setViewModel(loginViewModel);
        binding.setLifecycleOwner(this);

        mLoginButton = findViewById(R.id.login);
        mLoginButton.setOnClickListener((button)->attemptLogin());
    }

    private void attemptLogin() {
        // TODO Add spinner or progress bar
        Log.d(LOG_TAG, "Attempting Login with <"+ loginViewModel.getUsername().getValue() + "/" + loginViewModel.getPassword().getValue() + ">");
        TaskRunner.executeSerialAsync(
                ()-> {
                    return loginViewModel.login();
                  //  return;
                    },
                (result)->{
                    Log.d(LOG_TAG,"Login async call returned");
                });
    }

}