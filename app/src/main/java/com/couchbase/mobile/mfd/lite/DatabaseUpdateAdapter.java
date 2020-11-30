package com.couchbase.mobile.mfd.lite;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.couchbase.mobile.mfd.databinding.ItemDatabaseUpdateBinding;

import java.util.List;

public class DatabaseUpdateAdapter extends RecyclerView.Adapter<DatabaseUpdateAdapter.UpdateViewHolder>{

    private List<DatabaseUpdate> mUpdateList;

    public DatabaseUpdateAdapter(List<DatabaseUpdate> updateList) {
        this.mUpdateList = updateList;
    }

    @NonNull
    @Override
    public UpdateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemDatabaseUpdateBinding itemBinding = ItemDatabaseUpdateBinding.inflate(layoutInflater,parent, false);
        return new UpdateViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UpdateViewHolder holder, int position) {
        DatabaseUpdate update = mUpdateList.get(position);
        holder.bind(update);
    }

    @Override
    public int getItemCount() {
        return mUpdateList != null ? mUpdateList.size() : 0;
    }

    class UpdateViewHolder extends RecyclerView.ViewHolder {
        private ItemDatabaseUpdateBinding mBinding;

        public UpdateViewHolder(@NonNull ItemDatabaseUpdateBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind(DatabaseUpdate update) {
            mBinding.setUpdate(update);
            mBinding.executePendingBindings();
        }
    }
}
