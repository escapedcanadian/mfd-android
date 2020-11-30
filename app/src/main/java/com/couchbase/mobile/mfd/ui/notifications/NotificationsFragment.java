package com.couchbase.mobile.mfd.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.couchbase.mobile.mfd.MainViewModel;
import com.couchbase.mobile.mfd.databinding.FragmentNotificationsBinding;
import com.couchbase.mobile.mfd.lite.DatabaseUpdateAdapter;

public class NotificationsFragment extends Fragment {


    private MainViewModel mMainViewModel;
    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mMainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        //View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        binding = FragmentNotificationsBinding.inflate(inflater,container,false);
        binding.setLifecycleOwner(this);
        binding.setViewModel(mMainViewModel);

        // Set up the recycler view
        RecyclerView recyclerView = binding.updateList;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        DatabaseUpdateAdapter adapter = new DatabaseUpdateAdapter(mMainViewModel.getUpdateList());
        recyclerView.setAdapter(adapter);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}