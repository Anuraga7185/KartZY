package com.ordering.blinkit_clone.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ordering.blinkit_clone.BlinkitApplication;
import com.ordering.blinkit_clone.database.OrderRepo;
import com.ordering.blinkit_clone.databinding.CartActivityLayoutBinding;
import com.ordering.blinkit_clone.http.APIClient;
import com.ordering.blinkit_clone.http.APIService;
import com.ordering.blinkit_clone.http.ApiCallback;
import com.ordering.blinkit_clone.http.RetrofitClient;
import com.ordering.blinkit_clone.ui.entity.OrderCreateRequest;
import com.ordering.blinkit_clone.ui.entity.OrderCreateResponse;
import com.ordering.blinkit_clone.ui.entity.OrderDetail;
import com.ordering.blinkit_clone.ui.entity.Product;

import java.util.List;

import retrofit2.Call;

public class CartActivity extends AppCompatActivity {
    CartActivityLayoutBinding binding;
    public static final String GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";
    OrderCreateRequest orderDetail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Grocery App Opening Page -> ", "CartActivity");

        Bundle bundle = getIntent().getBundleExtra("bundle");
        if (bundle != null && bundle.getSerializable("order") instanceof OrderCreateRequest) {
            orderDetail = (OrderCreateRequest) bundle.getSerializable("order");
            Toast.makeText(this, "GOt", Toast.LENGTH_LONG).show();
        }
        binding = CartActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.totalPrice.setText(orderDetail != null ? orderDetail.totalPrice + "" : "nil");
        binding.paymentBtn.setOnClickListener(this::successFullOrder);

    }

    APIService apiService = RetrofitClient.getClient().create(APIService.class);

    private void orderCreate(ApiCallback<OrderCreateResponse> listApiCallback) {
        String categoryId = getIntent().getStringExtra("category_id");
        orderDetail.branch = "678b95fbd881843a6fefc4e3";
        Log.d("CartActivity ", BlinkitApplication.loggedUser().accessToken);
        Call<OrderCreateResponse> productsCall = apiService.createOrder("Bearer " + BlinkitApplication.loggedUser().refreshToken, orderDetail);
        APIClient.makeRequest(productsCall, new ApiCallback<>() {
            @Override
            public void onSuccess(OrderCreateResponse response) {
                listApiCallback.onSuccess(response);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CartActivity.this, "Error Fetching Data From Server!!", Toast.LENGTH_LONG).show();
                listApiCallback.onError("Error Fetching Data!!");
            }
        });
    }

    private void successFullOrder(View view) {
        orderCreate(new ApiCallback<>() {
            @Override
            public void onSuccess(OrderCreateResponse response) {
                if (response != null && response.status.equals("Available")) {
                    OrderRepo.repoInstance(getApplicationContext()).orderDatabase().userDao().clearTable();
                    Intent intent = new Intent(CartActivity.this, SuccessfulOrderScreen.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onError(String error) {

            }
        });


    }


}
