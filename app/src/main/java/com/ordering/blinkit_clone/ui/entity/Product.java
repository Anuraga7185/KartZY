package com.ordering.blinkit_clone.ui.entity;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.DrawableRes;

import com.google.gson.annotations.SerializedName;

public class Product {
    public String _id;
    public String name;
    @SerializedName("image")
    public String imageUrl;
    public double price;
    @SerializedName("discountPrice")
    public String discountedPrice;
    public String discountOffer;
    public String availability;
    public String quantity;
    public String pros;
    public boolean isItemAvailable;
    @DrawableRes
    public int imageDrawable;

    public int quantitySelected;

    public Product() {

    }

    public Product(String name, String imageUrl, @DrawableRes int imageDrawable, double price, String discountedPrice, String discountOffer, String availability, String quantity, String pros, boolean isItemAvailable) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.imageDrawable = imageDrawable;
        this.price = price;
        this.discountedPrice = discountedPrice;
        this.discountOffer = discountOffer;
        this.availability = availability;
        this.quantity = quantity;
        this.pros = pros;
        this.isItemAvailable = isItemAvailable;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getImage(Context context) {
        return switch (Product.ProductENUM.fromString(name)) {
            case AMUL_MASTI -> getDrawableResId(context, "category8");
            case BROWN_BREAD -> getDrawableResId(context, "category1");
            case NESTLE -> getDrawableResId(context, "category6");
            case AMUL_CHEESE -> getDrawableResId(context, "category7");
            case AMUL_MILK -> getDrawableResId(context, "category2");
            case AMUL_SLICES -> getDrawableResId(context, "category5");
            case PANEER -> getDrawableResId(context, "category3");
            case AMUL_TONED_MILK -> getDrawableResId(context, "category4");
            case GOWARDHAN_PANEER -> getDrawableResId(context, "category4");
            case AMUL_SALTED_BUTTER -> getDrawableResId(context, "category4");
        };
    }

    @SuppressLint("DiscouragedApi")
    private int getDrawableResId(Context context, String resourceName) {
        return context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
    }

    public enum ProductENUM {

        AMUL_MASTI("Amul Masti Curd"), BROWN_BREAD("BranO Plus Brown Bread"), NESTLE("Nestle Milkmaid"), AMUL_CHEESE("Amul Blend Diced Cheese"), AMUL_MILK("Amul Gold Cream Milk"), AMUL_SLICES("Amul Cheese Slices"),
        PANEER("Gyan Paneer"), AMUL_TONED_MILK("Amul Taaza Toned Fresh Milk"), GOWARDHAN_PANEER("Gowardhan Panner"), AMUL_SALTED_BUTTER("Amul Salted Butter");
        private final String displayName;

        ProductENUM(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Product.ProductENUM fromString(String name) {
            for (Product.ProductENUM category : Product.ProductENUM.values()) {
                if (category.displayName.equalsIgnoreCase(name)) {
                    return category;
                }
            }
            return ProductENUM.AMUL_CHEESE;  // default
        }
    }
}
