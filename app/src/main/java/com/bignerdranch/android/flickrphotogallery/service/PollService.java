package com.bignerdranch.android.flickrphotogallery.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import android.util.Log;

import com.bignerdranch.android.flickrphotogallery.R;
import com.bignerdranch.android.flickrphotogallery.data.FlickrFetcher;
import com.bignerdranch.android.flickrphotogallery.data.GalleryItemEntity;
import com.bignerdranch.android.flickrphotogallery.QueryPreferences;
import com.bignerdranch.android.flickrphotogallery.ui.PhotoGalleryActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PollService extends IntentService {

    private static final String TAG = "PollService";

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_POLL = "com.bignerdranch.android.flickrphotogallery.service.action.POLL";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.bignerdranch.android.flickrphotogallery.service.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.bignerdranch.android.flickrphotogallery.service.extra.PARAM2";


    private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(15);
    private static final int NOTIFICATION_ID = 135600;



    public PollService() {
        super(TAG);




    }



    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionPoll(Context context, String param1, String param2) {
        Intent intent = newIntent_actionPoll(context, param1, param2);
        context.startService(intent);
    }

    @NonNull
    private static Intent newIntent_actionPoll(final Context context, final String param1, final String param2) {
        Intent intent = new Intent(context, PollService.class);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        intent.setAction(ACTION_POLL);



        return intent;
    }



    @Override
    public int onStartCommand(@Nullable final Intent intent, final int flags, final int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: received intent: " + intent);

        if (intent != null) {

            final String action = intent.getAction();
            if (ACTION_POLL.equals(action)) {


                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionPoll(param1, param2);
            }
        }
    }


    private void handleActionPoll(String param1, String param2) {
        Log.d(TAG, "handleActionPoll: handling action POLL");

        if (!isNetworkAvailAndConnected()) {

            return;
        }

        String lastQuery = QueryPreferences.getStoredQuery(this);
        String lastID = QueryPreferences.getLastResultID(this);


        List<GalleryItemEntity> items;
        if (lastQuery == null) {
            items = new FlickrFetcher().fetchRecentPhotos();
        }else{
            items = new FlickrFetcher().fetchSearchPhotos(lastQuery);
        }


        if (items.size() == 0) {
            return;
        }

        String resultID = items.get(0).getID();

        if (resultID.equals(lastID)) {
            Log.i(TAG, "handleActionPoll: got old result ID " + resultID);
        } else {
            Log.i(TAG, "handleActionPoll: got new result ID "
                    + resultID);

            Resources resources = getResources();

            Intent intent = PhotoGalleryActivity.newIntentFromService(this);

            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, PhotoGalleryActivity.CHANNEL_ID);
//                    .setTicker(resources.getString(R.string.new_pictures_title))
            builder.setSmallIcon(R.drawable.ic_android_black_24dp)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_pictures_content_text))
//                    .addAction(R.drawable.ic_android_black_24dp,
//                            getString(R.string.new_pictures_title), pendingIntent)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build();


            NotificationManager notificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);


//            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_ID, builder.build());




        }


        QueryPreferences.setLastResultID(this, resultID);


    }

    private boolean isNetworkAvailAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvail = cm.getActiveNetworkInfo() != null;

        boolean isNetworkConnected = isNetworkAvail && cm.getActiveNetworkInfo().isConnected();


        return isNetworkConnected;

    }




    public static void setServiceAlarm(Context ctx, boolean isAlarmAlreadyOn) {

        String param1 = "";
        String param2 = "";
        Intent intent = PollService.newIntent_actionPoll(ctx, param1, param2);


        PendingIntent pendingIntent = PendingIntent.getService(ctx, 0, intent,
                0);

        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        // our logic dictates that if we are calling this method then
        // the pendingIntent should not already exist hence pendingIntent != null
//        assert pendingIntent != null;


//        assert alarmManager != null;

        if (isAlarmAlreadyOn) {

            if (alarmManager != null) {
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                        POLL_INTERVAL_MS, pendingIntent);
            }

        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();

        }


    }


    public static boolean isServiceAlarmOn(Context ctx) {
        Intent intent = PollService.newIntent_actionPoll(ctx, "", "");

        PendingIntent pendingIntent = PendingIntent.getService(ctx,
                0, intent, PendingIntent.FLAG_NO_CREATE);

        return pendingIntent != null;

    }
}
