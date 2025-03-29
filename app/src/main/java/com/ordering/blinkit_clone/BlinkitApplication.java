package com.ordering.blinkit_clone;

import android.app.Application;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.ordering.blinkit_clone.ui.entity.LoggedUser;

public class BlinkitApplication extends Application {
    private static LoggedUser user;

    public static int getDrawable(@NonNull String drawable) {
        return getDrawable(drawable);
    }

    public static LoggedUser loggedUser() {
        return user;
    }

    public static void setLoggedUser(LoggedUser loggedUser) {
        user = loggedUser;
    }
}
