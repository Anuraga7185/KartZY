package com.ordering.blinkit_clone.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.ScaleAnimation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.RenderMode;
import com.google.gson.Gson;
import com.ordering.blinkit_clone.BlinkitApplication;
import com.ordering.blinkit_clone.databinding.BlinkitSplashScreenBinding;
import com.ordering.blinkit_clone.ui.adapter.SharedPrefManager;
import com.ordering.blinkit_clone.ui.entity.LoggedUser;
import com.ordering.blinkit_clone.ui.util.Constants;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BlinkitStart extends AppCompatActivity {
    BlinkitSplashScreenBinding binding;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Grocery App Opening Page -> ", "BlinkitStart");
        binding = BlinkitSplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressAnim();
        prefManager = new SharedPrefManager(this);
        Handler handler = new Handler();
        boolean isData = getUserCreds();
        if (isData) {
            handler.postDelayed(this::fetchData, 3000);
        }
    }

    private void progressAnim() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(100);
        binding.progress.startAnimation(scaleAnimation);
        binding.progress.playAnimation();
    }

    private void fetchData() {

        JSONObject jsonObject = null;
        try {
            // Create JSON Object
            jsonObject = new JSONObject();
            jsonObject.put("phone", "7011341103");
            jsonObject.put("role", "Customer");
//            userCreds(jsonObject.toString());
        } catch (Exception e) {
            Log.d("Error USER Cred", "ERror Clearing User data");
        }
//        String url = "http://192.168.1.41:3000/api/customer/login";
        new Thread(() -> {
            try {

                URL urlObj = new URL(Constants.CUSTOMER_LOGIN_URL);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI2NzhiOTQxOGQ4ODE4NDNhNmZlZmM0ZDMiLCJyb2xlIjoiQ3VzdG9tZXIiLCJpYXQiOjE3NDE0MDUzMzEsImV4cCI6MTc0MTQ5MTczMX0.wOIs8i2Vw6FCqGd_DoXLVt_SYPwmcMj2IOgAF4R8vAo");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.connect();
                JSONObject payload = new JSONObject();
                payload.put("phone", "7011341103");
                payload.put("role", "Customer");
                // Write JSON data to request body
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                InputStreamReader inputStreamReader = new InputStreamReader(conn.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }

                runOnUiThread(() -> actionOnReceivingData(response.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }


    private void actionOnReceivingData(String data) {
        prefManager.saveJsonData("loggedInUser", data);
        Gson gson = new Gson();
        LoggedUser loggedUser = gson.fromJson(data, LoggedUser.class);
        BlinkitApplication.setLoggedUser(loggedUser);
        moveToDashboard(loggedUser != null);
    }

    private void moveToDashboard(boolean isLoggedUser) {
        Intent intent = new Intent(BlinkitStart.this, isLoggedUser ? DashboardActivity.class : LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void userCreds(String data) {
        prefManager.saveJsonData("userCred", data);
    }

    private boolean getUserCreds() {
        String data = prefManager.getJsonData("userCred");
        if (data == null || data.equals("{}")) {
            moveToDashboard(false);
            return false;
        } else {
            return true;
        }
    }
}
