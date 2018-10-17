package com.bignerdranch.android.flickrphotogallery.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.webkit.WebView;

import com.bignerdranch.android.flickrphotogallery.R;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    public static final String CHANNEL_ID = "FlickrGalleryNotificationChannel";

    public static Intent newIntentFromService(Context ctx) {
        Intent intent = new Intent(ctx, PhotoGalleryActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        return intent;
    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {

        createNotificationChannel();

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed() {

        PhotoPageFragment fragment = (PhotoPageFragment) getSupportFragmentManager().findFragmentByTag(PhotoPageFragment.TAG);

        if (fragment != null && fragment.canGoBackOnWebView()) {
            fragment.goBackOnWebView();
        } else {

            super.onBackPressed();

        }


    }

    @Override
    protected Fragment createFragment() {


        String param1 = "";
        String param2 = "";
        return PhotoGalleryFragment.newInstance(param1, param2);
    }
}
