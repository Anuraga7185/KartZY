package com.ordering.blinkit_clone.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.ordering.blinkit_clone.ui.entity.Items;

import java.util.List;

@Dao
public interface OrderDao {
    @Insert
    void insert(Items item);

    @Query("Select * From `order`")
    List<Items> getAllUsers();

    @Query("Select * From `order` WHERE id=:itemId")
    Items getItem(String itemId);

    @Delete
    void deleteUser(Items user);

    @Query("DELETE FROM `order` WHERE id=:itemId ")
    void deleteItems(String itemId);

    @Query("DELETE FROM `order`")
        // Clear all data from users table
    void clearTable();

    @Query("Update `order` SET count=:newCount WHERE id=:itemId")
    void updateCount(String itemId, int newCount);
}
