package com.example.rss_reader.views.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rss_reader.R;
import com.example.rss_reader.databases.SharedPreferencesHandler;
import com.example.rss_reader.models.RSSFeed;
import com.example.rss_reader.utils.Converter;
import com.example.rss_reader.utils.RSSDialogFactory;
import com.example.rss_reader.utils.RSSToastFactory;
import com.example.rss_reader.views.widgets.adapters.RSSFeedCardAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class FeedArchiveFragment extends Fragment {
    private ArrayList<RSSFeed> rssSet;
    private ArrayList<RSSFeed> rssReserveSet;
    private RecyclerView rssRecyclerView;
    private RSSFeedCardAdapter rssCardAdapter;
    private NestedScrollView rssNestedScrollView;
    private ProgressBar rssProgressBar;
    private TextView empty;
    private volatile boolean loading = false;

    public FeedArchiveFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.archive_frame_fragment, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        rssProgressBar = view.findViewById(R.id.archive_frame_progressBar);
        rssNestedScrollView = view.findViewById(R.id.archive_frame_nestedScrollView);

        rssSet = new ArrayList<>();
        rssReserveSet = new ArrayList<>();

        rssRecyclerView = view.findViewById(R.id.archive_frame_RecyclerView);
        rssCardAdapter = new RSSFeedCardAdapter(rssSet);

        rssRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rssRecyclerView.setAdapter(rssCardAdapter);

        empty = view.findViewById(R.id.empty_textView_archive_frame_fragment);

        rssNestedScrollView.setOnScrollChangeListener((View.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (!loading)
                if (scrollY == (rssNestedScrollView.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                    // in this method we are incrementing page number,
                    // making progress bar visible and calling get data method.
                    loading = true;
                    loadMore();
                }
        });

        loadFeeds();

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void loadFeeds() {
        rssSet.clear();
        rssReserveSet.clear();
        rssCardAdapter.notifyDataSetChanged();
        AlertDialog loading = RSSDialogFactory.createDialog(RSSDialogFactory.RSSDialog.Loading, getContext(), null);
        loading.show();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("RSS");
        reference.child("users").child(Objects.requireNonNull(SharedPreferencesHandler.getInstance(getContext()).getId())).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
                loading.dismiss();
                Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getContext(), Objects.requireNonNull(task.getException()).getMessage())).show();
            } else {
                Log.d("firebase", String.valueOf(Objects.requireNonNull(task.getResult()).getValue()));

                if (task.isSuccessful()) {
                    rssReserveSet.clear();

                    if (task.getResult().getValue() != null) {
                        task.getResult().getChildren().forEach(dataSnapshot -> {
                            if (dataSnapshot.getValue(boolean.class))
                                rssReserveSet.add(new RSSFeed(Objects.requireNonNull(SharedPreferencesHandler.getInstance(getContext()).getId()), Converter.pathToUrl(Objects.requireNonNull(dataSnapshot.getKey()))));
                        });

                        int limit = 10;
                        int position = rssSet.size();

                        for (int index = rssReserveSet.size() - 1; index >= 0 && limit > 0; index--) {
                            rssSet.add(rssReserveSet.get(index));
                            rssReserveSet.remove(index);
                            limit--;
                        }

                        rssCardAdapter.notifyItemInserted(position);
                        if (rssSet.size() == 0)
                            empty.setVisibility(View.VISIBLE);
                        else
                            empty.setVisibility(View.GONE);

                        loading.dismiss();
                    } else {
                        loading.dismiss();
                        empty.setVisibility(View.VISIBLE);
                        Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getContext(), "No RSS feed found!")).show();
                    }


                } else {
                    loading.dismiss();
                    empty.setVisibility(View.VISIBLE);
                    Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getContext(), "Load feeds failed!")).show();
                }
            }
        });

    }

    public void goTop() {
        rssScroll(0);
        loadFeeds();
    }

    private void rssScroll(int position) {
        rssNestedScrollView.post(() -> {
            int x = rssRecyclerView.getScrollX() + rssRecyclerView.getChildAt(position).getScrollX();
            int y = rssRecyclerView.getScrollY() + rssRecyclerView.getChildAt(position).getScrollY();

            rssNestedScrollView.smoothScrollTo(x, y, 700);
        });
    }

    private void loadMore() {
        rssProgressBar.setVisibility(View.VISIBLE);

        int limit = 10;
        int position = rssSet.size();

        for (int index = rssReserveSet.size() - 1; index >= 0 && limit > 0; index--) {
            rssSet.add(rssReserveSet.get(index));
            rssReserveSet.remove(index);
            limit--;
        }

        rssCardAdapter.notifyItemRangeInserted(position, 10 - limit);

        rssProgressBar.setVisibility(View.GONE);

        loading = false;
    }
}
