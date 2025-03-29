package com.ordering.blinkit_clone.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.ordering.blinkit_clone.ui.entity.Items;

@Database(entities = {Items.class}, version = 1)
public abstract class OrderDatabase extends RoomDatabase {
    private static volatile OrderDatabase INSTANCE;

    public abstract OrderDao userDao();

    public static OrderDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (OrderDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), OrderDatabase.class, "order_database").allowMainThreadQueries()  // Not recommended for production
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
