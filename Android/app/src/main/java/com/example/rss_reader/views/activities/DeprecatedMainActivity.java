//package com.example.rss_reader;
//
//import android.os.Bundle;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentTransaction;
//
//import com.example.rss_reader.views.fragments.ArchiveFragment;
//import com.example.rss_reader.views.fragments.RSSReaderFragment;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.android.material.navigation.NavigationBarView;
//
//
//public class MainActivity extends AppCompatActivity {
//    private int index = 0;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//
//        BottomNavigationView navigation = findViewById(R.id.main_bottomNavigationView);
//
//        navigation.setOnItemSelectedListener(onItemSelectedListener);
//
//        loadFragment(RSSReaderFragment.getInstance());
//    }
//
//
//    private final NavigationBarView.OnItemSelectedListener onItemSelectedListener
//            = item -> {
//        if (item.getItemId() != index) {
//            index = item.getItemId();
//
//            switch (index) {
//                case R.id.home:
//                    loadFragment(RSSReaderFragment.getInstance());
//                    return true;
//                case R.id.archive:
//                    loadFragment(ArchiveFragment.getInstance());
//                    return true;
//            }
//        }
//        return false;
//    };
//
//    private void loadFragment(Fragment fragment) {
//        // load fragment
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.main_frame, fragment);
//        transaction.show(fragment);
//        transaction.addToBackStack(null);
//        transaction.commit();
//    }
//
//    private void initFragments(){
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.commit();
//    }
//}