package com.ordering.blinkit_clone.ui.entity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;

import com.ordering.blinkit_clone.BlinkitApplication;
import com.ordering.blinkit_clone.R;

import java.io.Serializable;

public class Category implements Serializable {
    public String _id;
    public String name;
    public String image;
    public String __v;

    public int getImage(Context context) {
        return switch (CategoryENUM.fromString(name)) {
            case CLEANING_ESSENTIALS -> getDrawableResId(context, "category8");
            case MILK_CURD_PANEER -> getDrawableResId(context, "category1");
            case BABY_CARE -> getDrawableResId(context, "category6");
            case ATA_RICE_DAL -> getDrawableResId(context, "category7");
            case PHARMA_WELLNESS -> getDrawableResId(context, "category2");
            case HOME_OFFICE -> getDrawableResId(context, "category5");
            case VEGETABLES_FRUITS -> getDrawableResId(context, "category3");
            case MUNCHIES -> getDrawableResId(context, "category4");
        };
    }

    @SuppressLint("DiscouragedApi")
    private int getDrawableResId(Context context, String resourceName) {
        return context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
    }

    public enum CategoryENUM {
        CLEANING_ESSENTIALS("Cleaning Essentials"),
        MILK_CURD_PANEER("Milk, Curd & Paneer"),
        BABY_CARE("Baby Care"),
        ATA_RICE_DAL("Ata, Rice & Dal"),
        PHARMA_WELLNESS("Pharma & Wellness"),
        HOME_OFFICE("Home & Office"),
        VEGETABLES_FRUITS("Vegetables & Fruits"),
        MUNCHIES("Munchies");

        private final String displayName;

        CategoryENUM(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static CategoryENUM fromString(String name) {
            for (CategoryENUM category : CategoryENUM.values()) {
                if (category.displayName.equalsIgnoreCase(name)) {
                    return category;
                }
            }
            return CategoryENUM.ATA_RICE_DAL;  // default
        }
    }
}
