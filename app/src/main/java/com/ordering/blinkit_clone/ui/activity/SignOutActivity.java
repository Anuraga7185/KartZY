package com.ordering.blinkit_clone.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ordering.blinkit_clone.databinding.SignOutLayoutBinding;
import com.ordering.blinkit_clone.ui.adapter.SharedPrefManager;

public class SignOutActivity extends AppCompatActivity {
    SignOutLayoutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SignOutLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.logoutContainer.setOnClickListener(this::logout);
    }

    private void logout(View view) {
        SharedPrefManager prefManager = new SharedPrefManager(this);
        prefManager.removeKey("loggedInUser");
        prefManager.removeKey("userCred");
        Intent intent = new Intent(SignOutActivity.this, BlinkitStart.class);
        startActivity(intent);

    }
}
