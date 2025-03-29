package com.ordering.blinkit_clone.database;

import android.content.Context;

import androidx.room.Index;

public class OrderRepo {
    private static OrderRepo repo;
    private static OrderDatabase orderDatabase;
    private static OrderDao orderDao;

    public static OrderRepo repoInstance(Context context) {
        if (repo == null) {
            repo = new OrderRepo();
            orderDatabase = OrderDatabase.getInstance(context);
            orderDao = orderDatabase.userDao();
        }
        return repo;
    }

    private OrderRepo() {
    }

    public OrderDao orderDao() {
        return orderDao;
    }

    public OrderDatabase orderDatabase() {
        return orderDatabase;
    }
}
