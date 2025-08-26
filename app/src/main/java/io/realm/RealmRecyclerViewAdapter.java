package io.realm;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

public class RealmRecyclerViewAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private RealmResults<T> mData;

    public RealmRecyclerViewAdapter(OrderedRealmCollection<T> data, boolean autoUpdate) { this.mData = (RealmResults<T>) data; }
    public RealmRecyclerViewAdapter(OrderedRealmCollection<T> data, Context context, boolean isShareAdapter) { this.mData = (RealmResults<T>) data; }
    @Override
    public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) { return null; }
    @Override
    public void onBindViewHolder(VH holder, int position) {}
    @Override
    public int getItemCount() { return 0; }

    // helper used by app code
    public T getItem(int position) { return null; }

    public RealmResults<T> getData() { return mData; }
}
