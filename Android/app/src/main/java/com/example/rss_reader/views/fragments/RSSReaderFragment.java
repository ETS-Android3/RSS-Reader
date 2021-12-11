package com.example.rss_reader.views.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.example.rss_reader.models.RSSArticle;
import com.example.rss_reader.models.RSSSite;
import com.example.rss_reader.networking.repositories.RSSRepository;
import com.example.rss_reader.utils.Converter;
import com.example.rss_reader.utils.RSSDialogFactory;
import com.example.rss_reader.utils.RSSToastFactory;
import com.example.rss_reader.views.widgets.adapters.RSSCardAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RSSReaderFragment extends Fragment {
    private ArrayList<RSSArticle> rssSet;
    private ArrayList<RSSArticle> rssReserveSet;
    private RecyclerView rssRecyclerView;
    private RSSCardAdapter rssCardAdapter;
    private NestedScrollView rssNestedScrollView;
    private ProgressBar rssProgressBar;
    private ArrayList<String> rssUrlList;
    private TextView empty;
    private volatile boolean loading = false;
    @SuppressLint("StaticFieldLeak")
    private static volatile RSSReaderFragment instance;

    private RSSReaderFragment() {
    }

    public static RSSReaderFragment getInstance() {
        if (instance == null)
            instance = new RSSReaderFragment();

        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rss_reader_fragment, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        rssProgressBar = view.findViewById(R.id.rssReader_progressBar);
        rssNestedScrollView = view.findViewById(R.id.rssReader_nestedScrollView);

        rssSet = new ArrayList<>();
        rssReserveSet = new ArrayList<>();
        rssUrlList = new ArrayList<>();

        rssRecyclerView = view.findViewById(R.id.rssReader_recycleView);
        rssCardAdapter = new RSSCardAdapter(rssSet);

        rssRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rssRecyclerView.setAdapter(rssCardAdapter);

        empty = view.findViewById(R.id.empty_textView_rss_reader_fragment);

        MaterialButton refresh = view.findViewById(R.id.refresh_rssReader);
        MaterialButton add = view.findViewById(R.id.add_rssReader);
        MaterialButton go = view.findViewById(R.id.goTop_rssReader);

        refresh.setOnClickListener(v -> loadFeeds());
        add.setOnClickListener(v -> addUrl());
        go.setOnClickListener(v -> goTop());

        rssNestedScrollView.setOnScrollChangeListener((View.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (!loading)
                if (scrollY == (rssNestedScrollView.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                    // in this method we are incrementing page number,
                    // making progress bar visible and calling get data method.
                    loadMore();
                }
        });

        loadFeeds();

        return view;
    }

    private void loadFeeds() {
        AlertDialog dialog = RSSDialogFactory.createDialog(RSSDialogFactory.RSSDialog.Loading, getContext(), null);
        dialog.show();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("RSS");
        reference.child("users").child(Objects.requireNonNull(SharedPreferencesHandler.getInstance(getContext()).getId())).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
                dialog.dismiss();
                Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getContext(), task.getException().getMessage())).show();
            } else {
                Log.d("firebase", String.valueOf(Objects.requireNonNull(task.getResult()).getValue()));
                rssUrlList.clear();

                if (task.getResult().getValue() != null)
                    task.getResult().getChildren().forEach(dataSnapshot -> {
                        if (dataSnapshot.getValue(boolean.class))
                            rssUrlList.add(Converter.pathToUrl(Objects.requireNonNull(dataSnapshot.getKey())));
                    });
                else
                    Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getContext(), "No RSS feed found")).show();

                refresh(dialog);

            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refresh(AlertDialog dialog) {
        rssSet.clear();
        rssReserveSet.clear();
        rssCardAdapter.notifyDataSetChanged();

        CountDownLatch latch = new CountDownLatch(rssUrlList.size());

        rssUrlList.forEach(url -> {
            try {
                RSSRepository repository = RSSRepository.getInstance(url).initializeService();

                repository.getRSS().enqueue(new Callback<RSSSite>() {
                    @Override
                    public void onResponse(@NonNull Call<RSSSite> call, @NonNull Response<RSSSite> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                rssReserveSet.addAll(response.body().getList());
                            }
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(@NonNull Call<RSSSite> call, @NonNull Throwable t) {
                        Log.e("Get RSS failed:", t.getMessage());
                        dialog.dismiss();
                        Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getContext(), "Load RSS failed!")).show();
                        latch.countDown();
                    }
                });
            } catch (Exception e) {
                Log.e("RSS", "Invalid RSS URL");
                dialog.dismiss();
                Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getContext(), "Invalid RSS URL!")).show();
                latch.countDown();
            }
        });

        new RSSUpdate().execute(latch, dialog);
    }

    public void addUrl() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomDialog);
        View urlDialogView = getLayoutInflater().inflate(R.layout.url_dialog, null);
        builder.setView(urlDialogView).setCancelable(true);

        EditText urlEditText = urlDialogView.findViewById(R.id.url_urlDialog);
        MaterialButton go = urlDialogView.findViewById(R.id.go_urlDialog);

        AlertDialog dialog = builder.create();

        AlertDialog loadingDialog = RSSDialogFactory.createDialog(RSSDialogFactory.RSSDialog.Loading, getContext(), null);

        go.setOnClickListener(v -> {
            dialog.dismiss();

            Objects.requireNonNull(loadingDialog).show();
            try {
                String url = urlEditText.getText().toString();
                RSSRepository repository = RSSRepository.getInstance(url).initializeService();
                repository.getRSS().enqueue(new Callback<RSSSite>() {
                    @Override
                    public void onResponse(@NonNull Call<RSSSite> call, @NonNull Response<RSSSite> response) {
                        if (response.isSuccessful()) {
                            try {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("RSS");
                                reference.child("users").child(Objects.requireNonNull(SharedPreferencesHandler.getInstance(getContext()).getId())).child(Converter.urlToPath(url)).setValue(true, (error, ref) -> {
                                    if (error == null) {
                                        loadingDialog.dismiss();
                                        loadFeeds();
                                    }

                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                loadingDialog.dismiss();
                                Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getContext(), "Add RSS URL!")).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<RSSSite> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        loadingDialog.dismiss();
                        Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getContext(), "Invalid RSS URL!")).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                loadingDialog.dismiss();
                Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getContext(), "Invalid RSS URL!")).show();
            }
        });

        dialog.show();
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
        Log.e("Load more", "Called");
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

    @SuppressLint("StaticFieldLeak")
    private class RSSUpdate extends AsyncTask<Object, Void, ArrayList<Object>> {
        @Override
        protected ArrayList<Object> doInBackground(Object... objects) {
            try {
                CountDownLatch latch = (CountDownLatch) objects[0];
                AlertDialog dialog = (AlertDialog) objects[1];
                latch.await();

                rssReserveSet.sort((first, second) -> {
                    try {
                        return Converter.timeToPresent(first.getPubDate()) > Converter.timeToPresent(second.getPubDate()) ? -1 : 1;
                    } catch (ParseException parseException) {
                        parseException.printStackTrace();
                        return -1;
                    }
                });

                int limit = 10;
                int position = rssSet.size();

                for (int index = rssReserveSet.size() - 1; index >= 0 && limit > 0; index--) {
                    rssSet.add(rssReserveSet.get(index));
                    rssReserveSet.remove(index);
                    limit--;
                }

                return new ArrayList<>(Arrays.asList(position, dialog));

            } catch (InterruptedException e) {
                e.printStackTrace();
                return new ArrayList<>(Arrays.asList(-1, objects[1]));

            }
        }

        @Override
        protected void onPostExecute(ArrayList<Object> objects) {
            int position = (int) objects.get(0);
            AlertDialog dialog = (AlertDialog) objects.get(1);
            dialog.dismiss();
            if (position != -1) {
                if (rssSet.size() == 0)
                    empty.setVisibility(View.VISIBLE);
                else
                    empty.setVisibility(View.GONE);

                rssCardAdapter.notifyItemInserted(position);
            } else {
                empty.setVisibility(View.VISIBLE);
                Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getContext(), "Update RSS failed!")).show();
            }

        }
    }

}
