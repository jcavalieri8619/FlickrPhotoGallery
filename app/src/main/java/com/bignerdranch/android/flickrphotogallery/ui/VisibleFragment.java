package com.bignerdranch.android.flickrphotogallery.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.bignerdranch.android.flickrphotogallery.service.PollFlickrJobService;

public abstract class VisibleFragment extends Fragment {

    private static final String TAG = "VisibleFragment";


    @Override
    public void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter(PollFlickrJobService.ACTION_SHOW_NOTIFICATION);

        getActivity().registerReceiver(mOnShowNotification,
                filter, PollFlickrJobService.PERMISSION_PRIVATE,null);





    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(mOnShowNotification);

    }





    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            // if we receive this--> we're visible so shouldn't be
            // issuing notifications while user already has app open

            Log.d(TAG, "onReceive: cancelling notification");
            setResultCode(Activity.RESULT_CANCELED);

        }
    };
}
