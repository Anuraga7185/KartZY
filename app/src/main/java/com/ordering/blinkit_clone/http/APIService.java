package com.ordering.blinkit_clone.http;

import com.ordering.blinkit_clone.ui.entity.Category;
import com.ordering.blinkit_clone.ui.entity.LoggedUser;
import com.ordering.blinkit_clone.ui.entity.LoginRequest;
import com.ordering.blinkit_clone.ui.entity.OrderCreateDetailResponse;
import com.ordering.blinkit_clone.ui.entity.OrderCreateRequest;
import com.ordering.blinkit_clone.ui.entity.OrderCreateResponse;
import com.ordering.blinkit_clone.ui.entity.OrderDetail;
import com.ordering.blinkit_clone.ui.entity.OrderUpdateResponse;
import com.ordering.blinkit_clone.ui.entity.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface APIService {
    @POST("api/auth/login")
    Call<LoggedUser> loginUser(@Body LoginRequest request);

    @POST("api/delivery/login")
    Call<LoggedUser> deliveryPartnerLogin(@Body LoginRequest request);


    @POST("api/order/{orderId}/confirm")
    Call<OrderUpdateResponse> confirmOrder(@Header("Authorization") String authToken, @Path("orderId") String orderId, @Body OrderCreateRequest orderDetail);

    @GET("api/categories")
    Call<List<Category>> getCategories();

    // Fetch categories
    @GET("api/products/{categoryId}")
    Call<List<Product>> getProductsByCategory(@Path("categoryId") String categoryId);

    @POST("api/order")
    Call<OrderCreateResponse> createOrder(@Header("Authorization") String authToken, @Body OrderCreateRequest orderDetail);
}
