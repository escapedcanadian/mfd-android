package com.couchbase.mobile.mfd.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.couchbase.mobile.mfd.ui.main.MainActivity;
import com.couchbase.mobile.mfd.R;
import com.couchbase.mobile.mfd.data.LoginRepository;
import com.couchbase.mobile.mfd.databinding.ActivityLoginBinding;
import com.couchbase.mobile.mfd.ui.register.RegisterActivity;
import com.couchbase.mobile.mfd.util.AppGlobals;
import com.couchbase.mobile.mfd.util.TaskRunner;


public class LoginActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MFD_LoginActivity";

    private LoginViewModel mLoginViewModel;
    private Button mLoginButton;
    private Button mRegisterButton;
    private EditText mUsernameEdit;
    private EditText mPasswordEdit;

    private ActivityResultLauncher<Intent> mRegistrationLauncher;
    private ActivityResultLauncher<Intent> mMainLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The logic for this activity is implemented in the ViewModel and connected via data bindings
        LoginViewModelFactory factory = new LoginViewModelFactory(LoginRepository.getInstance());
        mLoginViewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);
        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setViewModel(mLoginViewModel);
        binding.setLifecycleOwner(this);

        // Register a handler for login pressed to potentially show progress spinner
        mLoginButton = findViewById(R.id.login);
        mLoginButton.setOnClickListener((button) -> attemptLogin());

        // Erase any notification about invalid username/password if the values are changed
        TextWatcher eraseErrorWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mLoginViewModel.getLoginError().setValue(null);
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) {}
        };
        mUsernameEdit = findViewById(R.id.username);
        mUsernameEdit.addTextChangedListener(eraseErrorWatcher);
        mPasswordEdit = findViewById(R.id.password);
        mPasswordEdit.addTextChangedListener(eraseErrorWatcher);

        mRegisterButton = findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener((button)-> {
            registerNewUser();
        });

        // Create a launcher for the connect activity if needed
        mRegistrationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Log.d(LOG_TAG, "Returned from registration activity OK");
                            // Set the user on the login screen to match the name just registered
                            mLoginViewModel.getUsername().setValue(AppGlobals.getInstance().getLastUser());
                        } else {
                            Log.d(LOG_TAG, "Returned from registration activity CANCELLED");
                        }
                    }
                }
        );

        // Create a launcher for the main activity
        mMainLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Log.d(LOG_TAG, "Returned from main activity OK");
                        } else {
                            Log.d(LOG_TAG, "Returned from main activity CANCELLED");
                        }
                    }
                }
        );

        // This is temporary code to bypass the login screen at startup
        mLoginViewModel.getUsername().setValue("asd");
        mLoginViewModel.getPassword().setValue("asd");
        attemptLogin();
    }

    private void attemptLogin() {
        // TODO Add spinner or progress bar
        Log.d(LOG_TAG, "Attempting Login with <" + mLoginViewModel.getUsername().getValue() + "/" + mLoginViewModel.getPassword().getValue() + ">");
        TaskRunner.executeSerialAsync(
                () -> {
                    return mLoginViewModel.login();
                },
                (result) -> {
                    Log.d(LOG_TAG, "Login async call returned");
                    result.render(
                            (user) -> {
                                runOnUiThread(()->{
                                    mLoginViewModel.getLoggedInUser().setValue(user);});
                                    Intent mainIntent = new Intent(this, MainActivity.class);
                                    mMainLauncher.launch(mainIntent);
                            },
                            (message, ex) -> {
                                runOnUiThread(()->{
                                    mLoginViewModel.getLoginError().setValue(message);});
                                if (ex != null) {
                                    Log.e(LOG_TAG, "Error attempting login:", ex);
                                }
                            });
                });
    }

    private void registerNewUser() {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        mRegistrationLauncher.launch(registerIntent);
    }

}
