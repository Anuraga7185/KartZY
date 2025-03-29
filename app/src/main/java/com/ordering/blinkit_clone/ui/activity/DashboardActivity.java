package com.ordering.blinkit_clone.ui.activity;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ordering.blinkit_clone.BlinkitApplication;
import com.ordering.blinkit_clone.R;
import com.ordering.blinkit_clone.database.OrderRepo;
import com.ordering.blinkit_clone.databinding.BlinkitDashboardBinding;
import com.ordering.blinkit_clone.databinding.BottomSheetLayoutBinding;
import com.ordering.blinkit_clone.databinding.CategoriyLayoutBinding;
import com.ordering.blinkit_clone.databinding.GooglePlacesRowBinding;
import com.ordering.blinkit_clone.databinding.ItemsLayoutBinding;
import com.ordering.blinkit_clone.http.APIClient;
import com.ordering.blinkit_clone.http.APIService;
import com.ordering.blinkit_clone.http.ApiCallback;
import com.ordering.blinkit_clone.http.RetrofitClient;
import com.ordering.blinkit_clone.service.GooglePlacesService;
import com.ordering.blinkit_clone.ui.adapter.BlinkitRecyclerAdapter;
import com.ordering.blinkit_clone.ui.entity.Category;
import com.ordering.blinkit_clone.ui.entity.Items;
import com.ordering.blinkit_clone.ui.entity.LoggedUser;
import com.ordering.blinkit_clone.ui.entity.OrderCreateRequest;
import com.ordering.blinkit_clone.ui.entity.OrderDetail;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;

public class DashboardActivity extends AppCompatActivity {
    private BlinkitDashboardBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    LoggedUser loggedUser;

    private void userProfileAnim() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(2000);
        binding.userAnim.startAnimation(scaleAnimation);
    }

    private final int[] array = {R.drawable.c1, R.drawable.c2, R.drawable.c3, R.drawable.c4, R.drawable.c5};
    private int index = 0;

    private void imageSwitcher() {
        binding.imageSwitcher.setFactory(() -> {
            ImageView imgView = new ImageView(getApplicationContext());
            imgView.setScaleType(ImageView.ScaleType.FIT_XY);
            return imgView;
        });
        // Set initial image
        binding.imageSwitcher.setImageResource(array[index]);
        // Load animations
        binding.imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));
        binding.imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                index = (index + 1 < array.length) ? index + 1 : 0;
                binding.imageSwitcher.setImageResource(array[index]);
                handler.postDelayed(this, 3000);
            }
        }, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        actionOnStoredItems();
    }

    private void actionOnStoredItems() {
        // Action ON View Click
        OrderRepo orderRepo = OrderRepo.repoInstance(this);
        List<Items> allItems = orderRepo.orderDao().getAllUsers();
        binding.cartButton.setOnClickListener(v -> {
            OrderCreateRequest orderDetail = new OrderCreateRequest();
            for (Items items : allItems) {
                orderDetail.itemsMap.put(items.id, items);
            }
            Intent intent = new Intent(DashboardActivity.this, CartActivity.class);
            Bundle bundle = new Bundle();
            orderDetail.updateItemsPrice();
            orderDetail.calcPrice(); // TODO: Updated Todo Price and Need to look for Cart Detail
            bundle.putSerializable("order", orderDetail);
            intent.putExtra("bundle", bundle);
            startActivity(intent);
        });
        //  Action For UI

        if (allItems == null || allItems.isEmpty()) {
            binding.cartButton.setVisibility(View.GONE);
            return;
        }

        binding.cartButton.setVisibility(View.VISIBLE);
        binding.container1.setVisibility(View.VISIBLE);

        int count = 0;
        for (Items items : allItems) {
            count += items.count;
        }
        int totalItems = count;
        binding.cartCount.setText(totalItems + " Items");
        if (!allItems.isEmpty()) {
            binding.container1.setVisibility(View.VISIBLE);
            binding.container2.setVisibility(View.GONE);
            binding.container3.setVisibility(View.GONE);
            Glide.with(getApplicationContext()).load(R.drawable.category16).into(binding.img1);
        }
        if (allItems.size() >= 2) {
            binding.container2.setVisibility(View.VISIBLE);
            binding.container3.setVisibility(View.GONE);
            Glide.with(getApplicationContext()).load(R.drawable.category16).into(binding.img2);
        }
        if (allItems.size() >= 3) {
            binding.container3.setVisibility(View.VISIBLE);
            Glide.with(getApplicationContext()).load(R.drawable.category16).into(binding.img3);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Grocery App Opening Page -> ", "DashboardActivity");
        binding = BlinkitDashboardBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        if (loggedUser != null) {
            loggedUser = BlinkitApplication.loggedUser();
            Toast.makeText(this, loggedUser.name, Toast.LENGTH_LONG).show();
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();
        userProfileAnim();
        imageSwitcher();
        getCategories(new ApiCallback<>() {
            @Override
            public void onSuccess(List<Category> response) {
                binding.recyclerGrocery.setAdapter(new BlinkitRecyclerAdapter<>(response, R.layout.categoriy_layout, DashboardActivity.this::createCategoryFetchFromServer));
                binding.recyclerGrocery.setLayoutManager(new GridLayoutManager(getApplicationContext(), 4));
            }

            @Override
            public void onError(String error) {

            }
        });

        binding.userAnim.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SignOutActivity.class);
            startActivity(intent);
        });

        binding.recyclerSnacks.setAdapter(new BlinkitRecyclerAdapter<>(getGrocery(), R.layout.categoriy_layout, this::createGrocery));
        binding.recyclerBeauty.setAdapter(new BlinkitRecyclerAdapter<>(getGrocery(), R.layout.categoriy_layout, this::createGrocery));
        binding.recyclerHousehold.setAdapter(new BlinkitRecyclerAdapter<>(getGrocery(), R.layout.categoriy_layout, this::createGrocery));
        binding.recyclerShopStore.setAdapter(new BlinkitRecyclerAdapter<>(getGrocery(), R.layout.categoriy_layout, this::createGrocery));

        binding.recyclerSnacks.setLayoutManager(new GridLayoutManager(getApplicationContext(), 4));
        binding.recyclerBeauty.setLayoutManager(new GridLayoutManager(getApplicationContext(), 4));
        binding.recyclerHousehold.setLayoutManager(new GridLayoutManager(getApplicationContext(), 4));
        binding.recyclerShopStore.setLayoutManager(new GridLayoutManager(getApplicationContext(), 4));
        binding.searchBox.setFactory(() -> {

            TextView textView1 = new TextView(DashboardActivity.this);
            // Defining attributes for the text to be displayed
            textView1.setTextSize(15);
            return textView1;

        });


        changeSearchText();
        openBottomSheet();
    }

    private void createCategoryFetchFromServer(View view, Category s, int i) {
        CategoriyLayoutBinding categoriyLayoutBinding = CategoriyLayoutBinding.bind(view);
        categoriyLayoutBinding.categoryContainer.setVisibility(View.VISIBLE);
        categoriyLayoutBinding.categoryImage.setVisibility(View.GONE);
        categoriyLayoutBinding.nameCtg.setVisibility(View.VISIBLE);
        categoriyLayoutBinding.nameCtg.setText(s.name);
        categoriyLayoutBinding.imgCtg.setImageResource(s.getImage(DashboardActivity.this));
        categoriyLayoutBinding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, CategoryDetailActivity.class);
            intent.putExtra("category_id", s._id);
            startActivity(intent);
        });
    }

    private void openBottomSheet() {
        binding.location.setOnClickListener(v -> {
            Context context = new ContextThemeWrapper(DashboardActivity.this, R.style.Theme_Blinkit_Clone);
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

            View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_layout, null);
            BottomSheetLayoutBinding bottomSheetLayoutBinding = BottomSheetLayoutBinding.bind(bottomSheetView);
            if (bottomSheetDialog.getWindow() != null) {
                bottomSheetDialog.getWindow().setDimAmount(0.1f);
            }
            bottomSheetLayoutBinding.location.setText(binding.location.getText());
            bottomSheetDialog.setContentView(bottomSheetLayoutBinding.getRoot());
            bottomSheetLayoutBinding.currLocation.setOnClickListener(v1 -> {
                binding.location.setText(bottomSheetLayoutBinding.location.getText());
                bottomSheetDialog.dismiss();

            });
            bottomSheetLayoutBinding.clearView.setOnClickListener(v1 -> {
                bottomSheetLayoutBinding.recyclerContainer.setVisibility(View.GONE);
                bottomSheetLayoutBinding.preLocationInfo.setVisibility(View.VISIBLE);
            });
            bottomSheetLayoutBinding.searchLocation.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0) {
                        bottomSheetLayoutBinding.recyclerContainer.setVisibility(View.GONE);
                        bottomSheetLayoutBinding.preLocationInfo.setVisibility(View.VISIBLE);
                    }
                    if (s.length() > 2) { // To reduce unnecessary API calls
                        bottomSheetLayoutBinding.recyclerContainer.setVisibility(View.VISIBLE);
                        bottomSheetLayoutBinding.preLocationInfo.setVisibility(View.GONE);
                     /*   GooglePlacesService.service(DashboardActivity.this).filter(s, places -> {
                            if (places != null) {
                                bottomSheetLayoutBinding.recyclerContainer.setAdapter(new BlinkitRecyclerAdapter<>(places, R.layout.google_places_row, DashboardActivity.this::showLocationList));
                            }
                        });*/
                        GooglePlacesService.service(DashboardActivity.this).filter(s, places -> {
                            Log.d("PlacesService", "Received places: " + (places != null ? places.size() : "null"));

                            if (places != null && !places.isEmpty()) {

                                bottomSheetLayoutBinding.recyclerContainer.setLayoutManager(new LinearLayoutManager(DashboardActivity.this, LinearLayoutManager.VERTICAL, false));
                                bottomSheetLayoutBinding.recyclerContainer.setAdapter(new BlinkitRecyclerAdapter<>(places, R.layout.google_places_row, DashboardActivity.this::showLocationList));
                            }
                        });


                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            bottomSheetDialog.show();
        });
    }

    private void showLocationList(View view, GooglePlacesService.GooglePlacesResult placesResult, int position) {
        GooglePlacesRowBinding placesRowBinding = GooglePlacesRowBinding.bind(view);
        placesRowBinding.primaryAddress.setText(placesResult.primaryAddress);
        placesRowBinding.fullAddress.setText(placesResult.fullAddress);
    }

    private void createGrocery(View view, String s, int i) {
        CategoriyLayoutBinding categoriyLayoutBinding = CategoriyLayoutBinding.bind(view);
        Glide.with(getApplicationContext()).load(s).into(categoriyLayoutBinding.categoryImage);

        categoriyLayoutBinding.categoryImage.setImageURI(Uri.parse(s));
        categoriyLayoutBinding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, CategoryDetailActivity.class);
            startActivity(intent);
        });
    }

    private final String[] searchTexts = {"Electronics", "Food", "Chips"};
    private final Handler handler = new Handler();
    private int currentIndex = 0;

    private void changeSearchText() {
        Runnable textSwitcherRunnable = new Runnable() {
            @Override
            public void run() {
                ObjectAnimator animation = ObjectAnimator.ofFloat(binding.searchBox, "translationY", -90, 0);
                animation.setDuration(2000);
                animation.setRepeatCount(ValueAnimator.INFINITE);
                animation.setRepeatMode(ValueAnimator.RESTART);
                animation.start();
                binding.searchBox.setText("Search " + searchTexts[currentIndex]);


                // Move to the next text
                currentIndex = (currentIndex + 1) % searchTexts.length;

                // Repeat the process after 3 seconds (3000 milliseconds)
                handler.postDelayed(this, 4000);
            }
        };

        // Start the first text change immediately
        handler.post(textSwitcherRunnable);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove callbacks to avoid memory leaks
        handler.removeCallbacksAndMessages(null);
    }

    private void createRow(View view, String s, int i) {
        ItemsLayoutBinding layoutBinding = ItemsLayoutBinding.bind(view);
        layoutBinding.name.setText(s);
    }

    protected ArrayList<String> getListItems() {
        ArrayList<String> blinkitItems = new ArrayList<>();

        // Grocery & Staples
        blinkitItems.add("Fortune Sunflower Oil");
        blinkitItems.add("Aashirvaad Whole Wheat Atta");
        blinkitItems.add("Tata Salt");
        blinkitItems.add("Daawat Basmati Rice");
        blinkitItems.add("Rajma (Kidney Beans)");
        blinkitItems.add("Toor Dal (Pigeon Pea)");
        blinkitItems.add("Besan (Gram Flour)");
        blinkitItems.add("Maggi Instant Noodles");

        // Fruits & Vegetables
        blinkitItems.add("Fresh Bananas");
        blinkitItems.add("Onions");
        blinkitItems.add("Tomatoes");
        blinkitItems.add("Potatoes");
        blinkitItems.add("Carrots");
        blinkitItems.add("Cucumbers");

        // Dairy & Bakery
        blinkitItems.add("Amul Milk 1L");
        blinkitItems.add("Mother Dairy Curd");
        blinkitItems.add("Britannia Brown Bread");
        blinkitItems.add("Eggs (6 Pack)");
/*
        // Beverages
        blinkitItems.add("Coca-Cola 1.25L");
        blinkitItems.add("Red Bull Energy Drink");
        blinkitItems.add("Tata Tea Gold");
        blinkitItems.add("Nescafé Classic Coffee");

        // Snacks & Chocolates
        blinkitItems.add("Lays Potato Chips");
        blinkitItems.add("Cadbury Dairy Milk Chocolate");
        blinkitItems.add("Hide & Seek Biscuits");

        // Personal Care
        blinkitItems.add("Colgate Toothpaste");
        blinkitItems.add("Dettol Handwash");
        blinkitItems.add("Head & Shoulders Shampoo");

        // Household Essentials
        blinkitItems.add("Harpic Toilet Cleaner");
        blinkitItems.add("Surf Excel Detergent Powder");*/
        return blinkitItems;
    }

    protected static ArrayList<String> getGrocery() {
        ArrayList<String> categories = new ArrayList<>();
        categories.add("https://cdn.grofers.com/cdn-cgi/image/f=auto,fit=scale-down,q=70,metadata=none,w=225/layout-engine/2022-11/Slice-3_9.png");
        categories.add("https://cdn.grofers.com/cdn-cgi/image/f=auto,fit=scale-down,q=70,metadata=none,w=270/layout-engine/2022-11/Slice-10.png");
        categories.add("https://cdn.grofers.com/cdn-cgi/image/f=auto,fit=scale-down,q=70,metadata=none,w=270/layout-engine/2022-11/Slice-11.png");
        categories.add("https://cdn.grofers.com/cdn-cgi/image/f=auto,fit=scale-down,q=70,metadata=none,w=270/layout-engine/2022-11/Slice-2_10.png");
        categories.add("https://cdn.grofers.com/cdn-cgi/image/f=auto,fit=scale-down,q=70,metadata=none,w=270/layout-engine/2022-11/Slice-8_4.png");
        categories.add("https://cdn.grofers.com/cdn-cgi/image/f=auto,fit=scale-down,q=70,metadata=none,w=270/layout-engine/2022-11/Slice-6_5.png");
        categories.add("https://cdn.grofers.com/cdn-cgi/image/f=auto,fit=scale-down,q=70,metadata=none,w=270/layout-engine/2022-11/Slice-13.png");
        categories.add("https://cdn.grofers.com/cdn-cgi/image/f=auto,fit=scale-down,q=70,metadata=none,w=270/layout-engine/2022-11/Slice-18.png");

        return categories;
    }

    APIService apiService = RetrofitClient.getClient().create(APIService.class);

    private void getCategories(ApiCallback<List<Category>> listApiCallback) {
        Call<List<Category>> categoryCall = apiService.getCategories();
        APIClient.makeRequest(categoryCall, new ApiCallback<>() {
            @Override
            public void onSuccess(List<Category> response) {
                listApiCallback.onSuccess(response);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DashboardActivity.this, "Error Fetching Data From Server!!", Toast.LENGTH_LONG).show();
                listApiCallback.onError("Error Fetching Data!!");
            }
        });
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getUserLocation();
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    Toast.makeText(DashboardActivity.this, "Lat: " + latitude + ", Lon: " + longitude, Toast.LENGTH_LONG).show();
                    Geocoder geocoder = new Geocoder(DashboardActivity.this, Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    String address = addresses.get(0).getAddressLine(0);
                    Toast.makeText(DashboardActivity.this, address, Toast.LENGTH_LONG).show();

                    binding.location.setText(address);
                    binding.location.setSelected(true);
                } else {
                    Toast.makeText(DashboardActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
