package com.ordering.blinkit_clone.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ordering.blinkit_clone.databinding.SuccessfulOrderScreenBinding;

public class SuccessfulOrderScreen extends AppCompatActivity {
    SuccessfulOrderScreenBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Grocery App Opening Page -> ", "" + "SuccessfulOderScreen");
        binding = SuccessfulOrderScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Fade-In Effect
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1500);
        binding.orderPlaced.startAnimation(fadeIn);
        binding.deliveryHome.startAnimation(fadeIn);
        binding.address.startAnimation(fadeIn);

        // Scale-Up Animation for Checkmark
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.5f, 1.0f, 0.5f, 1.0f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(1000);
        binding.lotAnim.startAnimation(scaleAnimation);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Bundle bundle = getIntent().getBundleExtra("bundle");

            Intent intent = new Intent(SuccessfulOrderScreen.this, OrderDetailActivity.class);
            if (bundle != null) {
                intent.putExtra("bundle", bundle);
            }
            startActivity(intent);
            finish();
        }, 3000);
    }
}
