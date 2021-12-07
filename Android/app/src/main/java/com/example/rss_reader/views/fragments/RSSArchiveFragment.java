package com.example.rss_reader.views.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
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
import com.example.rss_reader.databases.RSSSQLiteHandler;
import com.example.rss_reader.models.RSSPost;
import com.example.rss_reader.utils.RSSDialogFactory;
import com.example.rss_reader.views.widgets.adapters.RSSArchiveCardAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class RSSArchiveFragment extends Fragment {
    private ArrayList<RSSPost> rssSet;
    private ArrayList<RSSPost> rssReserveSet;
    private RecyclerView rssRecyclerView;
    private RSSArchiveCardAdapter rssCardAdapter;
    private NestedScrollView rssNestedScrollView;
    private ProgressBar rssProgressBar;
    private TextView empty;
    private volatile boolean loading = false;

    private final MaterialButton up;
    private final MaterialButton refresh;

    @SuppressLint("StaticFieldLeak")
    private static volatile RSSArchiveFragment instance;

    private RSSArchiveFragment(MaterialButton up, MaterialButton refresh) {
        this.up = up;
        this.refresh = refresh;
    }

    public static RSSArchiveFragment getInstance(MaterialButton up, MaterialButton refresh) {
        if (instance == null)
            instance = new RSSArchiveFragment(up, refresh);

        return instance;
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
        rssCardAdapter = new RSSArchiveCardAdapter(rssSet);

        rssRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rssRecyclerView.setAdapter(rssCardAdapter);

        empty = view.findViewById(R.id.empty_textView_archive_frame_fragment);

        up.setOnClickListener(v -> goTop());
        refresh.setOnClickListener(v -> loadArchives());

        rssNestedScrollView.setOnScrollChangeListener((View.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (!loading)
                if (scrollY == (rssNestedScrollView.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                    // in this method we are incrementing page number,
                    // making progress bar visible and calling get data method.
                    loading = true;
                    loadMore();
                }
        });

        loadArchives();

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadArchives() {
        rssSet.clear();
        rssReserveSet.clear();
        rssCardAdapter.notifyDataSetChanged();
        AlertDialog loading = RSSDialogFactory.createDialog(RSSDialogFactory.RSSDialog.Loading, getContext(), null);
        loading.show();

        rssReserveSet.addAll(RSSSQLiteHandler.getInstance(getContext()).getRSSs());

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
    }

    public void goTop() {
        rssScroll(0);
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
