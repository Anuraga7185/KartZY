package com.ordering.blinkit_clone.ui.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "order")
public class Items implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int primaryKey;
    public String id;
    public int count;
    public double itemPrice;
//    public String _id;

}
