package com.ordering.blinkit_clone.ui.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.ordering.blinkit_clone.R;
import com.ordering.blinkit_clone.databinding.OrderDetailLayoutBinding;
import com.ordering.blinkit_clone.service.SocketManager;
import com.ordering.blinkit_clone.ui.entity.OrderCreateResponse;
import com.ordering.blinkit_clone.ui.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class OrderDetailActivity extends AppCompatActivity {
    OrderDetailLayoutBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker deliveryMarker, myLocationMarker;
    OrderCreateResponse orderCreateResponse;

    private String mapAPIKey() {
        return "AIzaSyBUdfd676Xq_zXN7PrpDgiMPmX-6cm8YRc";
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Grocery App Opening Page -> ", "OrderDetailActivity");
        binding = OrderDetailLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        orderCreateResponse = getIntent().getBundleExtra("bundle") != null ? (OrderCreateResponse) getIntent().getBundleExtra("bundle").getSerializable("order") : null;
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(googleMap -> {
            mMap = googleMap;
            getCurrentLocation();
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        binding.btnResetZoom.setOnClickListener(v -> resetMapZoom());
        socketSetup();
        getDataFromSocket();
    }

    private final Handler handler = new Handler();
    private Runnable locationUpdateRunnable;

    private void socketSetup() {
        if (orderCreateResponse == null) {
            Log.d("Unable To fetch Orders", "Order is empty");
            return;
        }

        IO.Options options = new IO.Options();
        options.forceNew = true;
        options.reconnection = true;
        options.secure = false;
        options.transports = new String[]{"websocket"}; // Force WebSocket transport

        try {
            socket = IO.socket(Constants.APP_URL_BASE, options);

            socket.on(Socket.EVENT_CONNECT, args -> Log.d("SocketIO", "Connected to server"));
            socket.on(Socket.EVENT_CONNECT_ERROR, args -> Log.e("SocketIO", "Connection Error: " + Arrays.toString(args)));
            socket.on(Socket.EVENT_DISCONNECT, args -> Log.e("SocketIO", "Disconnected from server"));

            socket.connect();
            socket.emit("joinRoom", orderCreateResponse.orderId);
        } catch (URISyntaxException e) {
            Log.e("SocketIO", "Socket connection error", e);
        }
    }

    private Socket socket;

    class SocketData {
        public String sender;
        public DataMsg message;

        class DataMsg {
            public double latitude;
            public double longitude;
            public String status;
            public String address;
        }

    }

    private void getDataFromSocket() {
        handler.postDelayed(locationUpdateRunnable = () -> {
            socket.on("message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (args.length > 0) {
                        Object data = args[0];
                        if (data instanceof JSONObject) {
                            JSONObject jsonObject = (JSONObject) data;
                            Gson gson = new Gson();
                            SocketData socketData = gson.fromJson(jsonObject.toString(), SocketData.class);

                            Log.d("SOCKET LISTENER", "Received JSON: " + jsonObject.toString());
                            Log.d("SOCKET LISTENER", "Parsed Data: " + socketData.toString());
                            dataFromSocket(socketData);


                        } else {
                            Log.e("SOCKET LISTENER", "Unexpected data type: " + data.getClass().getSimpleName());
                        }
                    } else {
                        Log.e("SOCKET LISTENER", "Received empty args");
                    }
                }
            });

            handler.postDelayed(locationUpdateRunnable, 4000);
        }, 1);
    }

    private void dataFromSocket(SocketData location) {
        LatLng deliveryLocation = new LatLng(location.message.latitude, location.message.longitude);
        runOnUiThread(() -> {
            if (deliveryMarker != null) {
                deliveryMarker.setPosition(deliveryLocation);
            } else {
                deliveryMarker = mMap.addMarker(new MarkerOptions()
                        .position(deliveryLocation)
                        .title("Delivery Partner")
                        .icon(resizeBitmap(R.drawable.delivery, 120, 120)));
            }

            // Draw a polyline between myLocation and deliveryLocation
            if (myLocationMarker != null) {
                LatLng myLocation = myLocationMarker.getPosition();
                fetchRouteFromGoogle(myLocation, deliveryLocation);
            }

            // Move camera to show both locations
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(myLocationMarker.getPosition());
            builder.include(deliveryMarker.getPosition());
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.off();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng myLocation = new LatLng(28.594761, 77.072715);
                        Toast.makeText(OrderDetailActivity.this, myLocation.latitude + " " + myLocation.longitude, Toast.LENGTH_LONG).show();
                        myLocationMarker = mMap.addMarker(new MarkerOptions()
                                .position(myLocation).icon(resizeBitmap(R.drawable.home, 60, 60))
                                .title("My Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));

                        // Fetch delivery partner's location in real-time
                        listenToDeliveryPartnerLocation();
                    }
                });
    }

    private BitmapDescriptor resizeBitmap(int drawableResId, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), drawableResId);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }

    private void listenToDeliveryPartnerLocation() {
        LatLng deliveryLocation = new LatLng(28.603283, 77.082637);

        if (deliveryMarker != null) {
            deliveryMarker.setPosition(deliveryLocation);
        } else {
            deliveryMarker = mMap.addMarker(new MarkerOptions()
                    .position(deliveryLocation)
                    .title("Delivery Partner")
                    .icon(resizeBitmap(R.drawable.delivery, 120, 120)));
        }

        // Draw a polyline between myLocation and deliveryLocation
        if (myLocationMarker != null) {
            LatLng myLocation = myLocationMarker.getPosition();
            fetchRouteFromGoogle(myLocation, deliveryLocation);
        }

        // Move camera to show both locations
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(myLocationMarker.getPosition());
        builder.include(deliveryMarker.getPosition());
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));

    }

    private void resetMapZoom() {
        if (myLocationMarker != null && deliveryMarker != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(myLocationMarker.getPosition());
            builder.include(deliveryMarker.getPosition());

            // Animate camera to fit both markers in view
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
        }
    }

    private void fetchRouteFromGoogle(LatLng origin, LatLng destination) {
        String apiKey = mapAPIKey(); // Use your API key
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
                "&mode=driving" +
                "&key=" + apiKey;

        new Thread(() -> {
            try {
                URL urlObj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                InputStreamReader inputStreamReader = new InputStreamReader(conn.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }

                runOnUiThread(() -> parseRoute(response.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void parseRoute(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray routesArray = jsonObject.getJSONArray("routes");

            if (routesArray.length() > 0) {
                JSONObject route = routesArray.getJSONObject(0);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String encodedPolyline = overviewPolyline.getString("points");
                List<LatLng> polylinePoints = decodePolyline(encodedPolyline);

                mMap.addPolyline(new PolylineOptions()
                        .addAll(polylinePoints)
                        .width(10)
                        .color(0xFF2196F3) // Blue
                        .geodesic(true));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> polyline = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng(((double) lat / 1E5), ((double) lng / 1E5));
            polyline.add(p);
        }

        return polyline;
    }


}





