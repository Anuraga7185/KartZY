package com.ordering.blinkit_clone.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.ordering.blinkit_clone.R;
import com.ordering.blinkit_clone.database.OrderRepo;
import com.ordering.blinkit_clone.databinding.CategoryDetailBarItemBinding;
import com.ordering.blinkit_clone.databinding.CategoryDetailLayoutBinding;
import com.ordering.blinkit_clone.databinding.CategoryItemRowDetailMainBinding;
import com.ordering.blinkit_clone.http.APIClient;
import com.ordering.blinkit_clone.http.APIService;
import com.ordering.blinkit_clone.http.ApiCallback;
import com.ordering.blinkit_clone.http.RetrofitClient;
import com.ordering.blinkit_clone.ui.adapter.BlinkitRecyclerAdapter;
import com.ordering.blinkit_clone.ui.entity.Category;
import com.ordering.blinkit_clone.ui.entity.Items;
import com.ordering.blinkit_clone.ui.entity.OrderCreateRequest;
import com.ordering.blinkit_clone.ui.entity.OrderDetail;
import com.ordering.blinkit_clone.ui.entity.Product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;

public class CategoryDetailActivity extends AppCompatActivity {
    private List<String> itemList;
    private CategoryDetailLayoutBinding binding;
    OrderCreateRequest orderDetail = new OrderCreateRequest();
    OrderRepo orderRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Grocery App Opening Page -> ", "CategoryDetailActivity");
        binding = CategoryDetailLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        orderRepo = OrderRepo.repoInstance(getApplicationContext());
        actionOnStoredItems();
        leftBarSelectionSetUp();
        topBarFilterSetUp();
//        mainContainerSetUp();
        binding.cartButton.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryDetailActivity.this, CartActivity.class);
            Bundle bundle = new Bundle();
            orderDetail.updateItemsPrice();
            orderDetail.calcPrice(); // TODO: Updated Todo Price and Need to look for Cart Detail
            bundle.putSerializable("order", orderDetail);
            intent.putExtra("bundle", bundle);
            startActivity(intent);
        });

        getProductsForMain(new ApiCallback<>() {
            @Override
            public void onSuccess(List<Product> response) {
                binding.itemRecycler.setAdapter(new BlinkitRecyclerAdapter<>(response, R.layout.category_item_row_detail_main, CategoryDetailActivity.this::createMainContainerItems));
                binding.itemRecycler.setLayoutManager(new GridLayoutManager(CategoryDetailActivity.this, 2));
            }

            @Override
            public void onError(String error) {

            }
        });
    }

    private int totalItems = 0;

    int selectedPos = 0;

    // Top BAR
    private void topBarFilterSetUp() {
        itemList = Arrays.asList("Kurkure 1", "Kurkure 2", "Kurkure 3", "Kurkure 4", "Kurkure 1", "Kurkure 2", "Kurkure 3", "Kurkure 4", "Kurkure 1", "Kurkure 2", "Kurkure 3", "Kurkure 4", "Kurkure 1", "Kurkure 2", "Kurkure 3", "Kurkure 4");
        binding.filterRecycler.setAdapter(new BlinkitRecyclerAdapter<>(itemList, R.layout.category_detail_bar_item, this::createTabBars));
        binding.filterRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


    }

    private void actionOnStoredItems() {
        List<Items> allItems = orderRepo.orderDao().getAllUsers();
        if (allItems == null || allItems.isEmpty()) {
            return;
        }

        binding.cartButton.setVisibility(View.VISIBLE);
        binding.container1.setVisibility(View.VISIBLE);

        int count = 0;
        for (Items items : allItems) {
            count += items.count;
        }
        totalItems = count;
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

    private void createTopBarFilter(View view, String s, int i) {
    }

    // LEFT BAR
    private void leftBarSelectionSetUp() {
        // Sample data
        itemList = Arrays.asList("Kurkure 1", "Kurkure 2", "Kurkure 3", "Kurkure 4", "Kurkure 1", "Kurkure 2", "Kurkure 3", "Kurkure 4", "Kurkure 1", "Kurkure 2", "Kurkure 3", "Kurkure 4", "Kurkure 1", "Kurkure 2", "Kurkure 3", "Kurkure 4");
        binding.recyclerView.setAdapter(new BlinkitRecyclerAdapter<>(itemList, R.layout.category_detail_bar_item, this::createTabBars));
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

    }

    @SuppressLint("NotifyDataSetChanged")
    private void createTabBars(View view, String name, int position) {
        CategoryDetailBarItemBinding barItemBinding = CategoryDetailBarItemBinding.bind(view);
        barItemBinding.getRoot().setBackgroundColor(getColor(android.R.color.white));
        barItemBinding.image.setEnabled(selectedPos == position);
        barItemBinding.name.setTextColor(getColor(android.R.color.black));
        barItemBinding.name.setTextSize(selectedPos == position ? 14 : 12);

        ViewGroup.LayoutParams params = barItemBinding.image.getLayoutParams();
        params.height = selectedPos == position ? 145 : 140;
        params.width = selectedPos == position ? 145 : 140;
        barItemBinding.image.setLayoutParams(params);
        barItemBinding.customScrollbar.setVisibility(selectedPos == position ? View.VISIBLE : View.GONE);
        barItemBinding.getRoot().setOnClickListener(v -> {
            selectedPos = position;
            Objects.requireNonNull(binding.recyclerView.getAdapter()).notifyDataSetChanged();
        });
    }

    //   Main Container

    private void mainContainerSetUp() {
        binding.itemRecycler.setAdapter(new BlinkitRecyclerAdapter<>(getItemList(), R.layout.category_item_row_detail_main, this::createMainContainerItems));
        binding.itemRecycler.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void createMainContainerItems(View view, Product product, int i) {
        CategoryItemRowDetailMainBinding mainBinding = CategoryItemRowDetailMainBinding.bind(view);
        Glide.with(getApplicationContext()).load(product.getImageUrl()).into(mainBinding.productImage);
        mainBinding.productName.setText(product.name);
        mainBinding.price.setText(product.price + "");
        mainBinding.actualPrice.setText(product.discountedPrice);
        Items dbItem = orderRepo.orderDao().getItem(product._id);

        if (dbItem != null) {
            product.quantitySelected = dbItem.count;
            mainBinding.addText.setVisibility(View.GONE);
            mainBinding.quantityCount.setVisibility(View.VISIBLE);
            mainBinding.countView.setText(dbItem.count + "");
            mainBinding.addBtn.setEnabled(false);
        }
        mainBinding.increase.setOnClickListener(v -> {
            int quantity = product.quantitySelected + 1;
            totalItems++;
            Items items = orderDetail.itemsMap.get(product._id);
            if (items != null) {
                items.count = quantity;
            }
            orderRepo.orderDao().updateCount(product._id, quantity);
            product.quantitySelected = quantity;
            mainBinding.countView.setText(product.quantitySelected + "");
            binding.cartCount.setText(totalItems + " Items");
            List<Items> allItems = orderRepo.orderDao().getAllUsers();

            if (allItems.size() == 2) {
                binding.container2.setVisibility(View.VISIBLE);
                binding.container3.setVisibility(View.GONE);
                Glide.with(getApplicationContext()).load(R.drawable.category16).into(binding.img2);
            } else if (allItems.size() == 3) {
                binding.container3.setVisibility(View.VISIBLE);
                Glide.with(getApplicationContext()).load(R.drawable.category16).into(binding.img3);
            }
        });
        mainBinding.decrease.setOnClickListener(v -> {
            int quantity = product.quantitySelected - 1;
            product.quantitySelected = quantity;
            if (totalItems != 0) {
                totalItems--;
            }

            Items items = orderDetail.itemsMap.get(product._id);
            if (items != null) {
                items.count = quantity;
            }

            if (quantity == 0) {
                if (totalItems == 0) {
                    binding.cartButton.setVisibility(View.GONE);
                }

                orderDetail.itemsMap.remove(product._id);
                orderRepo.orderDao().deleteItems(product._id);
                mainBinding.addText.setVisibility(View.VISIBLE);
                mainBinding.quantityCount.setVisibility(View.GONE);
                binding.cartCount.setText(totalItems + " Items");
                mainBinding.addBtn.setEnabled(true);

            } else {
                orderRepo.orderDao().updateCount(product._id, quantity);
                mainBinding.countView.setText(quantity + "");
                binding.cartCount.setText(totalItems + " Items");

            }
            List<Items> allItems = orderRepo.orderDao().getAllUsers();

            if (allItems.size() == 1) {
                binding.container1.setVisibility(View.VISIBLE);
                binding.container2.setVisibility(View.GONE);
                binding.container3.setVisibility(View.GONE);
                Glide.with(getApplicationContext()).load(R.drawable.category16).into(binding.img1);
            } else if (allItems.size() == 2) {
                binding.container2.setVisibility(View.VISIBLE);
                binding.container3.setVisibility(View.GONE);
                Glide.with(getApplicationContext()).load(R.drawable.category16).into(binding.img2);
            } else if (allItems.size() == 3) {
                binding.container3.setVisibility(View.VISIBLE);
                Glide.with(getApplicationContext()).load(R.drawable.category16).into(binding.img3);
            }

        });
        mainBinding.addText.setOnClickListener(v -> {
            product.quantitySelected = 1;
            totalItems += 1;
            Items items = new Items();
            items.id = product._id;
            items.itemPrice = product.price;
            items.count = 1;
            orderDetail.itemsMap.put(product._id, items);
            orderRepo.orderDao().insert(items);

            mainBinding.addText.setVisibility(View.GONE);
            mainBinding.quantityCount.setVisibility(View.VISIBLE);
            mainBinding.countView.setText(items.count + "");
            mainBinding.addBtn.setEnabled(false);
            Glide.with(getApplicationContext()).load(R.drawable.category16).into(binding.img1);
            binding.cartCount.setText(totalItems + " Items");
            binding.cartButton.setVisibility(View.VISIBLE);
            binding.container1.setVisibility(View.VISIBLE);


            List<Items> allItems = orderRepo.orderDao().getAllUsers();
            if (allItems.size() == 1) {
                binding.container1.setVisibility(View.VISIBLE);
                binding.container2.setVisibility(View.GONE);
                binding.container3.setVisibility(View.GONE);
                Glide.with(getApplicationContext()).load(R.drawable.category16).into(binding.img1);
            } else if (allItems.size() == 2) {
                binding.container2.setVisibility(View.VISIBLE);
                binding.container3.setVisibility(View.GONE);
                Glide.with(getApplicationContext()).load(R.drawable.category16).into(binding.img2);
            } else if (allItems.size() == 3) {
                binding.container3.setVisibility(View.VISIBLE);
                Glide.with(getApplicationContext()).load(R.drawable.category16).into(binding.img3);
            }

        });


    }

    APIService apiService = RetrofitClient.getClient().create(APIService.class);

    private void getProductsForMain(ApiCallback<List<Product>> listApiCallback) {
        String categoryId = getIntent().getStringExtra("category_id");
        Call<List<Product>> productsCall = apiService.getProductsByCategory(categoryId);
        APIClient.makeRequest(productsCall, new ApiCallback<>() {
            @Override
            public void onSuccess(List<Product> response) {
                listApiCallback.onSuccess(response);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CategoryDetailActivity.this, "Error Fetching Data From Server!!", Toast.LENGTH_LONG).show();
                listApiCallback.onError("Error Fetching Data!!");
            }
        });
    }

    private ArrayList<Product> getItemList() {
        ArrayList<Product> itemsList = new ArrayList<>();
        itemsList.add(new Product("Pooja Flower Mix", "", R.drawable.category16, 53, "39", "26", "11 Mins", "100 g", "", true));
        itemsList.add(new Product("Banana", "", R.drawable.category16, 499, "39", "20", "11 Mins", "3 pieces", "Energy Booster", true));
        itemsList.add(new Product("Pooja Flower Mix", "", R.drawable.category16, 53, "39", "26", "11 Mins", "100 g", "", true));
        itemsList.add(new Product("Banana", "", R.drawable.category16, 49, "39", "20", "11 Mins", "3 pieces", "Energy Booster", true));
        itemsList.add(new Product("Banana", "", R.drawable.category16, 49, "39", "20", "11 Mins", "3 pieces", "Energy Booster", true));
        itemsList.add(new Product("Banana", "", R.drawable.category16, 49, "39", "20", "11 Mins", "3 pieces", "Energy Booster", true));
        itemsList.add(new Product("Banana", "", R.drawable.category16, 49, "39", "20", "11 Mins", "3 pieces", "Energy Booster", true));
        itemsList.add(new Product("Banana", "", R.drawable.category16, 49, "39", "20", "11 Mins", "3 pieces", "Energy Booster", true));
        itemsList.add(new Product("Banana", "", R.drawable.category16, 49, "39", "20", "11 Mins", "3 pieces", "Energy Booster", true));
        itemsList.add(new Product("Banana", "", R.drawable.category16, 49, "39", "20", "11 Mins", "3 pieces", "Energy Booster", true));
        itemsList.add(new Product("Banana", "", R.drawable.category16, 49, "39", "20", "11 Mins", "3 pieces", "Energy Booster", true));
        return itemsList;
    }
}
