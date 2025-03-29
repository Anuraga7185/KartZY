package com.ordering.blinkit_clone.ui.activity.delivery_partner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ordering.blinkit_clone.BlinkitApplication;
import com.ordering.blinkit_clone.R;
import com.ordering.blinkit_clone.databinding.DeliveryPartnerDashboardLayoutBinding;
import com.ordering.blinkit_clone.databinding.OrderItemLayoutBinding;
import com.ordering.blinkit_clone.ui.adapter.BlinkitRecyclerAdapter;
import com.ordering.blinkit_clone.ui.entity.Items;
import com.ordering.blinkit_clone.ui.entity.OrderCreateDetailResponse;
import com.ordering.blinkit_clone.ui.entity.OrderCreateResponse;
import com.ordering.blinkit_clone.ui.entity.OrderDetail;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import io.socket.client.IO;
import io.socket.client.Socket;

public class DeliveryPartnerDashboard extends AppCompatActivity {
    DeliveryPartnerDashboardLayoutBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private Socket socket;
    private static final String ORDER_ID = "ORDR00002"; // Example order ID
    private static final int LOCATION_UPDATE_INTERVAL = 3000; // 3 seconds
    private final Handler handler = new Handler();
    private Runnable locationUpdateRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Grocery App Opening Page -> ", "Delivery Partner Dashboard");

        binding = DeliveryPartnerDashboardLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupButtons();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

     /*   socketSetup();
        startSendingLocationUpdates();*/
        requestDataForAllOrders();
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestDataForAllOrders();
            }
        });
    }

    private void setupButtons() {
        binding.availableBtn.setSelected(true);
        binding.deliveredBtn.setSelected(false);
        binding.deliveredTxt.setTextColor(getColor(R.color.black));
        binding.availableTxt.setTextColor(getColor(R.color.white));

        binding.availableBtn.setOnClickListener(v -> {
            binding.availableBtn.setSelected(true);
            binding.availableTxt.setTextColor(getColor(R.color.white));
            binding.deliveredTxt.setTextColor(getColor(R.color.black));
            binding.deliveredBtn.setSelected(false);
        });

        binding.deliveredBtn.setOnClickListener(v -> {
            binding.availableBtn.setSelected(false);
            binding.availableTxt.setTextColor(getColor(R.color.black));
            binding.deliveredTxt.setTextColor(getColor(R.color.white));
            binding.deliveredBtn.setSelected(true);
        });
    }

    private void socketSetup() {

        IO.Options options = new IO.Options();
        options.forceNew = true;
        options.reconnection = true;
        options.secure = false;
        options.transports = new String[]{"websocket"}; // Force WebSocket transport

        try {
            socket = IO.socket("http://192.168.1.36:3000", options);

            socket.on(Socket.EVENT_CONNECT, args -> Log.d("SocketIO", "Connected to server"));
            socket.on(Socket.EVENT_CONNECT_ERROR, args -> Log.e("SocketIO", "Connection Error: " + Arrays.toString(args)));
            socket.on(Socket.EVENT_DISCONNECT, args -> Log.e("SocketIO", "Disconnected from server"));

            socket.connect();
            socket.emit("joinRoom", ORDER_ID);
        } catch (URISyntaxException e) {
            Log.e("SocketIO", "Socket connection error", e);
        }
    }

    public interface LocationCallback {
        void location(LatLng location);
    }

    private void getCurrentLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                callback.location(new LatLng(location.getLatitude(), location.getLongitude()));
            } else {
                // Fallback to a default location if the GPS fails
                callback.location(new LatLng(28.594761, 77.072715));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSendingLocationUpdates();
        }
    }


    private void startSendingLocationUpdates() {
        handler.postDelayed(locationUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                getCurrentLocation(location -> {
                    JSONObject locationData = new JSONObject();
                    try {
                        locationData.put("orderId", ORDER_ID);
                        JSONObject msg = new JSONObject();
                        msg.put("latitude", location.latitude);
                        msg.put("longitude", location.longitude);
                        locationData.put("message", msg);

                    } catch (JSONException e) {
                        Log.e("JSON ERROR", "Failed to create JSON", e);
                    }
                    socket.emit("message", locationData);
                    handler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
                });
            }
        }, 10);
    }

    private void stopSendingLocationUpdates() {
        handler.removeCallbacks(locationUpdateRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSendingLocationUpdates();
    }


    private void requestDataForAllOrders() {
        String url = "http://192.168.1.41:3000/api/order";
        new Thread(() -> {
            try {
                URL urlObj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + BlinkitApplication.loggedUser().refreshToken);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.connect();

                InputStreamReader inputStreamReader = new InputStreamReader(conn.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }

                runOnUiThread(() -> setOrderList(response.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setOrderList(String res) {
        Gson gson = new Gson();
        ArrayList<OrderCreateDetailResponse> orderDetails = gson.fromJson(res, new TypeToken<ArrayList<OrderCreateDetailResponse>>() {
        }.getType());
        binding.recyclerView.setVisibility(View.GONE);
        binding.noLayout.setVisibility(View.GONE);
        if (orderDetails == null || orderDetails.isEmpty()) {
            binding.noLayout.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            binding.recyclerView.setAdapter(new BlinkitRecyclerAdapter<>(orderDetails, R.layout.order_item_layout, this::createOrderItems));
            Log.d("LIST", res);
        }
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    private void createOrderItems(View view, OrderCreateDetailResponse orderDetail, int position) {
        OrderItemLayoutBinding orderItemLayoutBinding = OrderItemLayoutBinding.bind(view);
        orderItemLayoutBinding.orderId.setText(orderDetail.orderId);
        orderItemLayoutBinding.orderStatus.setText(orderDetail.status);
        StringBuilder builder = new StringBuilder();
        for (Items items : orderDetail.items) {
            builder.append(items.count).append(" ").append(items.id).append("\n");
        }
        orderItemLayoutBinding.orderItems.setText(builder.toString());
        orderItemLayoutBinding.deliveryAddress.setText(orderDetail.deliveryLocation.address);
    }


}



