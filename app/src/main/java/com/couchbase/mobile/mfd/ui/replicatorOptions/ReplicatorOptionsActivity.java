package com.couchbase.mobile.mfd.ui.replicatorOptions;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.couchbase.mobile.mfd.R;
import com.couchbase.mobile.mfd.databinding.ActivityReplicatorOptionsBinding;
import com.couchbase.mobile.mfd.lite.ReplicatorOptions;

public class ReplicatorOptionsActivity extends AppCompatActivity {

    private ReplicatorOptionsViewModel mSyncModeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_replicator_options);
        mSyncModeViewModel = new ViewModelProvider(this).get(ReplicatorOptionsViewModel.class);
        ActivityReplicatorOptionsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_replicator_options);
        binding.setViewModel(mSyncModeViewModel);
        binding.setLifecycleOwner(this);

        // Pull information from the calling intent
        Intent incoming = getIntent();
        ReplicatorOptions options = (ReplicatorOptions) incoming.getSerializableExtra(ReplicatorOptions.bundleTag);
        mSyncModeViewModel.reflectReplicatorOptions(options);
    }

    public void submitSettings(View view) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(ReplicatorOptions.bundleTag, mSyncModeViewModel.asReplicatorOptions());
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}