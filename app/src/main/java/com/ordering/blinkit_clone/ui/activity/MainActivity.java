package com.ordering.blinkit_clone.ui.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.ordering.blinkit_clone.R;
import com.ordering.blinkit_clone.ui.adapter.BannerAdapter;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 bannerViewPager;
    private RecyclerView productRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Grocery App Opening Page -> ", "MainActivity");
        setContentView(R.layout.activity_main);

        bannerViewPager = findViewById(R.id.bannerViewPager);
        productRecyclerView = findViewById(R.id.productRecyclerView);

        // Setup banner
//        List<Integer> images = Arrays.asList(R.drawable.banner1, R.drawable.banner2);
        List<Integer> images = List.of(R.drawable.banner1);
        bannerViewPager.setAdapter(new BannerAdapter(images, this));

        // Setup product grid
        productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }
}
