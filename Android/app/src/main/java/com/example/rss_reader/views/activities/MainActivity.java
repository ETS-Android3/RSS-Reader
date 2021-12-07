package com.example.rss_reader.views.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.rss_reader.R;
import com.example.rss_reader.views.fragments.ArchiveFragment;
import com.example.rss_reader.views.fragments.RSSReaderFragment;
import com.example.rss_reader.views.widgets.styles.ZoomOutPageTransformer;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends FragmentActivity {
    private static final int NUM_PAGES = 2;
    private ViewPager2 viewPager;


    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.secondary));

        setContentView(R.layout.activity_main);

        navigation = findViewById(R.id.main_bottomNavigationView);

        navigation.setOnItemSelectedListener(onItemSelectedListener);

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.main_frame);
        FragmentStateAdapter pagerAdapter = new MainPagerAdapter(this);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());
        viewPager.setAdapter(pagerAdapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        navigation.setSelectedItemId(R.id.home);
                        break;
                    case 1:
                        navigation.setSelectedItemId(R.id.archive);
                        break;
                }
            }
        });
    }


    @SuppressLint("NonConstantResourceId")
    private final NavigationBarView.OnItemSelectedListener onItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.home:
                viewPager.setCurrentItem(0);
                return true;
            case R.id.archive:
                viewPager.setCurrentItem(1);
                return true;
        }

        return false;
    };


    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    private static class MainPagerAdapter extends FragmentStateAdapter {
        public MainPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return RSSReaderFragment.getInstance();
                case 1:
                    return ArchiveFragment.getInstance();
            }

            return RSSReaderFragment.getInstance();
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
}