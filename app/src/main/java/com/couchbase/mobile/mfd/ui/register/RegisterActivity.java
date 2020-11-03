package com.couchbase.mobile.mfd.ui.register;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.ClientError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.couchbase.mobile.mfd.R;
import com.couchbase.mobile.mfd.data.LoginRepository;
import com.couchbase.mobile.mfd.data.model.User;
import com.couchbase.mobile.mfd.databinding.ActivityRegisterBinding;
import com.couchbase.mobile.mfd.ui.connect.ConnectActivity;
import com.couchbase.mobile.mfd.util.AppGlobals;
import com.couchbase.mobile.mfd.util.Result;

import org.json.JSONException;
import org.json.JSONObject;


public class RegisterActivity extends AppCompatActivity {

    private static String LOG_TAG = "MDF_RegisterActivity";

    private RegisterViewModel mRegisterViewModel;
    private ActivityResultLauncher<Intent> mConnectLauncher;
    private int mStatusCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_register);

        // The logic for this activity is implemented in the ViewModel and connected via data bindings
        RegisterViewModelFactory factory = new RegisterViewModelFactory((LoginRepository.getInstance()));
        mRegisterViewModel = new ViewModelProvider(this, factory).get(RegisterViewModel.class);
        // See if there is a stored username from a previous session
        mRegisterViewModel.getUsername().setValue(AppGlobals.getInstance().getLastUser());
        ActivityRegisterBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        binding.setViewModel(mRegisterViewModel);
        binding.setLifecycleOwner(this);

        // Create a launcher for the connect activity if needed
        mConnectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Log.d(LOG_TAG, "Returned from connect activity OK");
                        } else {
                            Log.d(LOG_TAG, "Returned from connect activity CANCELLED");
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    }
                }
        );

        // Make sure we are connected before displaying this activity
        if (!AppGlobals.getInstance().hasApplicationServer()) {
            Intent connectIntent = new Intent(this, ConnectActivity.class);
            mConnectLauncher.launch(connectIntent);
        }

        findViewById(R.id.register_button).setOnClickListener((button) -> {
            attemptRegistration();
        });

    }

    private void attemptRegistration() {
        // TODO Add spinner or progress bar

        int port = getBaseContext().getResources().getInteger(R.integer.game_server_port);
        String host = AppGlobals.getInstance().getApplicationServer();
        @SuppressLint("DefaultLocale") String url = String.format("http://%s:%d/user/registration/", host, port);
        JSONObject candidate;
        try {
           candidate = mRegisterViewModel.asJSON();
        } catch (JSONException err) {
            Log.e(LOG_TAG, "Error parsing JSON", err);
            String msg = getString(R.string.registration_failure);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            return;
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, candidate,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(LOG_TAG, "Response from registration server:" + response);
                        try {
                            if (!response.getBoolean("success")) {
                                Log.e(LOG_TAG, "Bad response from registration: " + response);
                                String msg = getString(R.string.registration_failure);
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                                return;
                            }
                        } catch (JSONException err) {
                            Log.e(LOG_TAG, " Request for registration failed", err);
                            String msg = getString(R.string.internal_server_error, getString(R.string.communication_error));
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                            return;
                        }
                        Log.d(LOG_TAG, "Successful remote registration: " + response);
                        Result<User> localUser = mRegisterViewModel.registerLocally();
                        localUser.render(
                                (user) -> {
                                    Log.d(LOG_TAG, "Successful local registration");
                                    String msg = getString(R.string.registration_success);
                                    AppGlobals.getInstance().setLastUser(mRegisterViewModel.getUsername().getValue());
                                    runOnUiThread(()->{
                                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                                        setResult(RESULT_OK);
                                        finish();
                                    });
                                },
                                (message, ex) -> {
                                    Log.e(LOG_TAG, message, ex);
                                    String msg = getString(R.string.internal_server_error, getString(R.string.communication_error));
                                    runOnUiThread(()->{
                                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                                    });
                                });
                            return;
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(LOG_TAG, " Request for registration failed", error);

                        if(error instanceof TimeoutError) {
                            String msg = getString(R.string.server_timeout_error, host);
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                            return;
                        } else if(error instanceof ClientError) {
                            if(error.networkResponse.statusCode == 409) {
                                String msg = getString(R.string.registration_conflict, RegisterActivity.this.mRegisterViewModel.getUsername().getValue());
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        Log.e(LOG_TAG,"Unhandled Volley error ", error);
                        String msg = getString(R.string.internal_server_error, getString(R.string.communication_error));
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

                    }
                }){
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                mStatusCode = response.statusCode;
                return super.parseNetworkResponse(response);
            }
        };

        // Add the request to the RequestQueue.
        AppGlobals.getInstance().requestQueue().add(stringRequest);
    }


}