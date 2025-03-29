package com.ordering.blinkit_clone.ui.activity.delivery_partner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ordering.blinkit_clone.BlinkitApplication;
import com.ordering.blinkit_clone.databinding.DeliveryPartnerPortalLayoutBinding;
import com.ordering.blinkit_clone.http.APIClient;
import com.ordering.blinkit_clone.http.APIService;
import com.ordering.blinkit_clone.http.ApiCallback;
import com.ordering.blinkit_clone.http.RetrofitClient;
import com.ordering.blinkit_clone.ui.entity.LoggedUser;
import com.ordering.blinkit_clone.ui.entity.LoginRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import retrofit2.Call;

public class DeliveryPartnerPortal extends AppCompatActivity {
    private static final Logger log = LoggerFactory.getLogger(DeliveryPartnerPortal.class);
    DeliveryPartnerPortalLayoutBinding binding;
    APIService apiService = RetrofitClient.getClient().create(APIService.class);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Grocery App Opening Page -> ", "DeliveryPartner Portal");

        binding = DeliveryPartnerPortalLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.loginBtn.setOnClickListener(v -> {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.email = "crepro7185@gmail.com";
            loginRequest.password = "Anurag12!@";

            Call<LoggedUser> login = apiService.deliveryPartnerLogin(loginRequest);
            APIClient.makeRequest(login, new ApiCallback<>() {
                @Override
                public void onSuccess(LoggedUser response) {
                    if (response != null) {
                        Intent intent = new Intent(DeliveryPartnerPortal.this, DeliveryPartnerDashboard.class);
                        BlinkitApplication.setLoggedUser(response);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onError(String error) {

                }
            });

        });
    }
}
