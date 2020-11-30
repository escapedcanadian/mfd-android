package com.couchbase.mobile.mfd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.couchbase.lite.AbstractReplicator;
import com.couchbase.mobile.mfd.data.model.User;
import com.couchbase.mobile.mfd.databinding.ActivityMainBinding;
import com.couchbase.mobile.mfd.lite.DatabaseUpdate;
import com.couchbase.mobile.mfd.lite.DatabaseUpdateListener;
import com.couchbase.mobile.mfd.lite.DatabaseWrapper;
import com.couchbase.mobile.mfd.lite.ReplicatorOptions;
import com.couchbase.mobile.mfd.ui.connect.ConnectActivity;
import com.couchbase.mobile.mfd.ui.replicatorOptions.ReplicatorOptionsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private static String LOG_TAG = "MDF_MainActivity";

    private static int CONFIG_ICON = 0;
    private static int STATUS_ICON = 1;

    private MainViewModel mMainViewModel;
    private Menu mMenu;
    private Observer<Object> mReplicatorOptionsListener;
    private Observer<AbstractReplicator.ActivityLevel> mReplicatorStatusListener;
    private DatabaseUpdateListener mDatabaseUpdateListener;
    private ActivityResultLauncher<Intent> mConnectLauncher;
    private ActivityResultLauncher<Intent> mReplicatorOptionsLauncher;
    private User mLoggedInUser;
    private String mConnectedServer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setViewModel(mMainViewModel);
        binding.setLifecycleOwner(this);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // Set up a listener respond to fields that influence the replicator config
        mReplicatorOptionsListener =  new Observer<Object>() {
            @Override
            public void onChanged(Object ignored) {
                updateReplicatorConfigIcon();
            }
        };

        mReplicatorStatusListener = new Observer<AbstractReplicator.ActivityLevel>() {
            @Override
            public void onChanged(AbstractReplicator.ActivityLevel activityLevel) {
                updateReplicatorStatusIcon(activityLevel);
            }
        };

        // Create a launcher for the connect activity if needed
        mConnectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Log.d(LOG_TAG, "Returned from connect activity OK");
                            mMainViewModel.getConnected().setValue(true);
                        } else {
                            Log.d(LOG_TAG, "Returned from connect activity CANCELLED");
                            mMainViewModel.getConnected().setValue(false);
                        }
                    }
                }
        );

        // Create a launcher for the replicator mode activity if needed
        mReplicatorOptionsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Log.d(LOG_TAG, "Returned from replicator mode activity OK");
                            Intent incoming = result.getData();
                            ReplicatorOptions options = (ReplicatorOptions) incoming.getSerializableExtra(ReplicatorOptions.bundleTag);
                            mMainViewModel.getReplicatorOptions().setValue(options);
                        } else {
                            Log.d(LOG_TAG, "Returned from replicator activity CANCELLED");

                        }
                    }
                }
        );

        // Set up the database update listener
        mDatabaseUpdateListener = (update) -> {
            Log.i(LOG_TAG,"Database update for key " + update.getDocId());
        };

        // Set up the view model with information about the current connection and user
        Intent incomingData = getIntent();
        mLoggedInUser = (User) incomingData.getSerializableExtra("loggedInUser");
        mConnectedServer = incomingData.getStringExtra("connectedServer");
        if(mConnectedServer != null) {
            mMainViewModel.getConnected().setValue(true);
        }
        mMainViewModel.setLoggedInUser(mLoggedInUser);
        mMainViewModel.addDatabaseUpdateListener(this, mDatabaseUpdateListener);

        // TODO Remove this - testing only
        mMainViewModel.testUpdateList();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_app_bar_menu, menu);
        mMenu = menu;
        // The following fields all influence the replicator config
        mMainViewModel.getReplicatorOptions().observe(this, mReplicatorOptionsListener);
        mMainViewModel.getConnected().observe(this, mReplicatorOptionsListener);
        mMainViewModel.getReplicatorStatus().observe(this, mReplicatorStatusListener);
        return true;
    }


    public void changeReplicatorConfig(MenuItem mi) {
        Log.d(LOG_TAG, "Request to change replication options");
        Boolean isConnected = mMainViewModel.getConnected().getValue();
        if(!isConnected) {
            Intent connectIntent = new Intent(this, ConnectActivity.class);
            mConnectLauncher.launch(connectIntent);
        } else {
           Intent syncModeIntent = new Intent(this, ReplicatorOptionsActivity.class);
            syncModeIntent.putExtra(ReplicatorOptions.bundleTag, mMainViewModel.getReplicatorOptions().getValue());
            mReplicatorOptionsLauncher.launch(syncModeIntent);
        }
    }

    public void startReplication(MenuItem mi){
        Log.d(LOG_TAG, "Request to start replication");
        mMainViewModel.startReplication();
    }

    private void updateReplicatorConfigIcon() {
         boolean connected = mMainViewModel.getConnected().getValue();
        if(connected) {
            ReplicatorOptions options = mMainViewModel.getReplicatorOptions().getValue();
            boolean isContinuous = options.isContinuous();
            DatabaseWrapper.SyncType type = options.getSyncType();
            switch(type) {
                case PUSH:
                    if(isContinuous) {
                        mMenu.getItem(CONFIG_ICON).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_push_cont));
                    } else {
                        mMenu.getItem(CONFIG_ICON).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_push_once));
                    }
                    break;
                case PULL:
                    if(isContinuous) {
                        mMenu.getItem(CONFIG_ICON).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_pull_cont));
                    } else {
                        mMenu.getItem(CONFIG_ICON).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_pull_once));
                    }
                    break;
                case PUSH_AND_PULL:
                    if(isContinuous) {
                        mMenu.getItem(CONFIG_ICON).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_push_pull_cont));
                    } else {
                        mMenu.getItem(CONFIG_ICON).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_push_pull_once));
                    }
                    break;
            }
        } else {
            mMenu.getItem(CONFIG_ICON).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_not_connected));
        }
    }

    private void updateReplicatorStatusIcon(AbstractReplicator.ActivityLevel status) {
        switch(status) {
            case BUSY:
                mMenu.getItem(STATUS_ICON).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_busy));
                break;
            case IDLE:
                mMenu.getItem(STATUS_ICON).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_idle));
                break;
            case OFFLINE:
                mMenu.getItem(STATUS_ICON).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_disconnected));
                break;
            case STOPPED:
                mMenu.getItem(STATUS_ICON).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_stopped));
                break;
            case CONNECTING:
                mMenu.getItem(STATUS_ICON).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_connecting));
                break;
        }
    }

}