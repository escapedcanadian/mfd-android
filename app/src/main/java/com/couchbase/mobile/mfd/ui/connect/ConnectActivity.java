package com.couchbase.mobile.mfd.ui.connect;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.couchbase.mobile.mfd.R;
import com.couchbase.mobile.mfd.databinding.ActivityConnectBinding;
import com.couchbase.mobile.mfd.util.AppGlobals;

import org.json.JSONException;
import org.json.JSONObject;


public class ConnectActivity extends AppCompatActivity {

    public static String LOG_TAG = "MFD_ConnectActivity";
    public static String SUCCESS_KEY = "success";
    private Button mConnectButton;
    private EditText mServerAddress;

    private ConnectViewModel connectViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        connectViewModel = new ViewModelProvider(this).get(ConnectViewModel.class);
        ActivityConnectBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_connect);
        binding.setViewModel(connectViewModel);
        binding.setLifecycleOwner(this);

        mServerAddress = findViewById(R.id.game_server);
        mConnectButton = findViewById(R.id.connect_button);
        mConnectButton.setOnClickListener((button)->{ attemptConnection();});

    }

    protected void attemptConnection() {
        int port = getBaseContext().getResources().getInteger(R.integer.game_server_port);
        String url = String.format("http://%s:%d/services", mServerAddress.getText().toString(), port);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG,"Services available on server " + url + ": "+ response);
                        try {
                            JSONObject jResponse = new JSONObject(response);
                            if (!jResponse.getBoolean("active")) {
                                String msg = getString(R.string.inactive_server_msg, mServerAddress.getText().toString());
                                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                                return;
                            }
                            AppGlobals.getInstance().setApplicationServer(mServerAddress.getText().toString());
                            setResult(RESULT_OK);
                            finish();

                        } catch (JSONException err) {
                            Log.e(LOG_TAG, "Error creating JSON from REST response.", err);
                            String msg = getString(R.string.internal_server_error, getString(R.string.unrecognized_response));
                            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                            return;
                        };



                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG," Request for services failed", error);
                String msg = getString(R.string.internal_server_error, getString(R.string.communication_error));
                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
            }
        });

        // Add the request to the RequestQueue.
        AppGlobals.getInstance().requestQueue().add(stringRequest);
    }

}