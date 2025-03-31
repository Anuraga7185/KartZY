package com.ordering.blinkit_clone.ui.activity.delivery_partner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.ordering.blinkit_clone.R;
import com.ordering.blinkit_clone.databinding.DeliveryPartnerOrderDetailActivityBinding;
import com.ordering.blinkit_clone.http.APIClient;
import com.ordering.blinkit_clone.http.APIService;
import com.ordering.blinkit_clone.http.ApiCallback;
import com.ordering.blinkit_clone.http.RetrofitClient;
import com.ordering.blinkit_clone.ui.entity.OrderCreateDetailResponse;
import com.ordering.blinkit_clone.ui.entity.OrderCreateRequest;
import com.ordering.blinkit_clone.ui.entity.OrderUpdateResponse;
import com.ordering.blinkit_clone.ui.entity.UserLiveLocation;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Arrays;

import io.socket.client.IO;
import io.socket.client.Socket;
import retrofit2.Call;

public class DeliverPartnerOrderDetailScreen extends AppCompatActivity {
    DeliveryPartnerOrderDetailActivityBinding binding;
    Bundle bundle;
    OrderCreateDetailResponse orderDetail;
    APIService apiService = RetrofitClient.getClient().create(APIService.class);

    /*Location Sharing OR Updates through Web Socket*/
    private final Handler handler = new Handler();
    private Runnable locationUpdateRunnable;
    private FusedLocationProviderClient fusedLocationClient;
    private Socket socket;

    private static final int LOCATION_UPDATE_INTERVAL = 3000; // 3 seconds

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DeliveryPartnerOrderDetailActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bundle = getIntent().getBundleExtra("bundle");
        actionOnBtn();
        binding.buttonAction.setOnClickListener(this::onButtonClick);
    }

    String tempToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI2NzhiOTY4OGQ4ODE4NDNhNmZlZmM0ZjUiLCJyb2xlIjoiRGVsaXZlcnlQYXJ0bmVyIiwiaWF0IjoxNzQzNDQ4NjQ3LCJleHAiOjE3NDM1MzUwNDd9.V2P48eFgi6a4hcZ3kEKufxaJs8K5cwjMwqjY-kcvbg8";

    private void onButtonClick(View view) {

        if (!orderDetail.status.equals("Available")) {

            return;
        }

        OrderCreateRequest orderCreateRequest = new OrderCreateRequest();
        orderCreateRequest.deliveryPersonLocation = new UserLiveLocation();
        orderCreateRequest.deliveryPersonLocation.address = "Street Number 5A Durga Park Colony, Manglapuri, New Delhi, Delhi, 110045";
        orderCreateRequest.deliveryPersonLocation.latitude = 28.602003;
        orderCreateRequest.deliveryPersonLocation.longitude = 77.08877;
        Call<OrderUpdateResponse> confirmOrder = apiService.confirmOrder(tempToken, orderDetail.orderId, orderCreateRequest);
        APIClient.makeRequest(confirmOrder, new ApiCallback<>() {
            @Override
            public void onSuccess(OrderUpdateResponse response) {
                binding.buttonAction.setText(response.status);
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(DeliverPartnerOrderDetailScreen.this);
                socketSetup();
            }

            @Override
            public void onError(String error) {

            }
        });

    }

    private void actionOnBtn() {
        orderDetail = (OrderCreateDetailResponse) bundle.get("orderSelected");
        if (orderDetail != null && !orderDetail.status.equals("Available")) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(DeliverPartnerOrderDetailScreen.this);
            socketSetup();
        }
        if (bundle == null || orderDetail == null) {
            return;
        }

        if (bundle.get("orderSelected") instanceof OrderCreateDetailResponse) {
            binding.buttonAction.setVisibility(View.VISIBLE);
            binding.orderId.setText(orderDetail.orderId);
            binding.deliveryLocation.setText(orderDetail.deliveryLocation.address);
            binding.orderId.setText(String.format(orderDetail.orderId, R.string.order_id_format));
            binding.buttonAction.setText(orderDetail.status);
        }
    }


    private void socketSetup() {

        IO.Options options = new IO.Options();
        options.forceNew = true;
        options.reconnection = true;
        options.secure = false;
        options.transports = new String[]{"websocket"}; // Force WebSocket transport

        try {
            socket = IO.socket("http://192.168.1.45:3000", options);

            socket.on(Socket.EVENT_CONNECT, args -> Log.d("SocketIO", "Connected to server"));
            socket.on(Socket.EVENT_CONNECT_ERROR, args -> Log.e("SocketIO", "Connection Error: " + Arrays.toString(args)));
            socket.on(Socket.EVENT_DISCONNECT, args -> Log.e("SocketIO", "Disconnected from server"));

            socket.connect();
            socket.emit("joinRoom", orderDetail.orderId);
            Log.d("SOcket Setup ", "Success");
            startSendingLocationUpdates();
        } catch (URISyntaxException e) {
            Log.e("SocketIO", "Socket connection error", e);
        }
    }

    public interface LocationCallback {
        void location(LatLng location);
    }

    private void getCurrentLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("get Current Location...", orderDetail.orderId);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

 /*       fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Log.d("get Current Location...", location.getLongitude() + " " + location.getLatitude());
                callback.location(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        });*/

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000) // Request location update every 5 seconds
                .setFastestInterval(2000) // Fastest allowed interval
                .setNumUpdates(1); // Get only one updated location and stop


        fusedLocationClient.requestLocationUpdates(locationRequest, new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() == null) {
                    Log.d("getCurrentLocation", "Location result is null");
                    return;
                }
                Location location = locationResult.getLastLocation();
                Log.d("getCurrentLocation", location.getLongitude() + " " + location.getLatitude());
                callback.location(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        }, Looper.getMainLooper());
    }

    private void startSendingLocationUpdates() {
        handler.postDelayed(locationUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("Start Sending Location...", orderDetail.orderId);
                getCurrentLocation(location -> {
                    JSONObject locationData = new JSONObject();
                    try {
                        locationData.put("orderId", orderDetail.orderId);
                        JSONObject msg = new JSONObject();
                        msg.put("latitude", location.latitude);
                        msg.put("longitude", location.longitude);
                        msg.put("status", orderDetail.status);
                        msg.put("address", "Street Number 5A Durga Park Colony, Manglapuri, New Delhi, Delhi, 110045");
                        locationData.put("message", msg);

                    } catch (JSONException e) {
                        Log.e("JSON ERROR", "Failed to create JSON", e);
                    }
                    socket.emit("message", locationData);
                    Log.d("Start Sending Location...", locationData.toString());

                    handler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
                });
            }
        }, 1000);
    }

    private void stopSendingLocationUpdates() {
        handler.removeCallbacks(locationUpdateRunnable);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSendingLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSendingLocationUpdates();
    }

}
