package com.example.rss_reader.views.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.rss_reader.R;
import com.example.rss_reader.databases.SharedPreferencesHandler;
import com.example.rss_reader.models.RSSSite;
import com.example.rss_reader.networking.repositories.RSSRepository;
import com.example.rss_reader.utils.Converter;
import com.example.rss_reader.utils.RSSDialogFactory;
import com.example.rss_reader.utils.RSSToastFactory;
import com.example.rss_reader.views.activities.AuthenticateActivity;
import com.example.rss_reader.views.activities.MainActivity;
import com.facebook.login.LoginManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArchiveFragment extends Fragment {
    private MaterialButton refresh;
    private MaterialButton up;
    private TabLayout tabs;
    private static volatile ArchiveFragment instance;

    private ArchiveFragment() {
    }

    public static ArchiveFragment getInstance() {
        if (instance == null)
            instance = new ArchiveFragment();

        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.archive_fragment, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        tabs = view.findViewById(R.id.archive_tabs);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        loadFragment(RSSArchiveFragment.getInstance(up, refresh));
                        break;
                    case 1:
                        loadFragment(FeedArchiveFragment.getInstance(up, refresh));
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        MaterialButton logout = view.findViewById(R.id.logout_archive);
        MaterialButton add = view.findViewById(R.id.add_archive);
        refresh = view.findViewById(R.id.refresh_archive);
        up = view.findViewById(R.id.goTop_archive);

        logout.setOnClickListener(v -> new AlertDialog.Builder(v.getContext(), R.style.DialogFactory).setMessage("Are you sure to log out").setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();


            if (SharedPreferencesHandler.getInstance(getContext()).saveID(null)) {
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(getActivity(), AuthenticateActivity.class);
                startActivity(intent);
                requireActivity().finishAffinity();
            } else
                RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getContext(), "Log out failed");

        }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).create().show());

        add.setOnClickListener(v -> addUrl());

        refresh.setOnClickListener(v -> {
            switch (tabs.getSelectedTabPosition()) {
                case 0:
                    loadFragment(RSSArchiveFragment.getInstance(up, refresh));
                    break;
                case 1:
                    loadFragment(FeedArchiveFragment.getInstance(up, refresh));
                    break;
                default:
                    break;
            }
        });

        loadFragment(RSSArchiveFragment.getInstance(up, refresh));

        return view;
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.archive_frame, fragment);
        transaction.show(fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void addUrl() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomDialog);
        View urlDialogView = getLayoutInflater().inflate(R.layout.url_dialog, null);
        builder.setView(urlDialogView).setCancelable(true);

        EditText urlEditText = urlDialogView.findViewById(R.id.url_urlDialog);
        MaterialButton go = urlDialogView.findViewById(R.id.go_urlDialog);


        AlertDialog dialog = builder.create();

        go.setOnClickListener(v -> {
            dialog.dismiss();

            final AlertDialog loading = RSSDialogFactory.createDialog(RSSDialogFactory.RSSDialog.Loading, getContext(), null);
            Objects.requireNonNull(loading).show();

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
                                        loading.dismiss();

                                        switch (tabs.getSelectedTabPosition()) {
                                            case 0:
                                                Objects.requireNonNull(tabs.getTabAt(1)).select();
                                                break;
                                            case 1:
                                                refresh.performClick();
                                                break;
                                        }
                                    }

                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                loading.dismiss();
                                Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getContext(), "Add RSS feed failed!")).show();
                            }

                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<RSSSite> call, @NonNull Throwable t) {
                        loading.dismiss();
                        Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getContext(), "Invalid RSS URL!")).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                loading.dismiss();

                Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getContext(), "Invalid RSS URL!")).show();
            }


        });

        dialog.show();
    }
}
