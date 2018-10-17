package com.bignerdranch.android.flickrphotogallery.service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bignerdranch.android.flickrphotogallery.R;
import com.bignerdranch.android.flickrphotogallery.data.FlickrFetcher;
import com.bignerdranch.android.flickrphotogallery.data.GalleryItemEntity;
import com.bignerdranch.android.flickrphotogallery.QueryPreferences;
import com.bignerdranch.android.flickrphotogallery.ui.PhotoGalleryActivity;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PollFlickrJobService extends JobService {
    private static final String TAG = "PollFlickrJobService";

    private static final int NOTIFICATION_ID = 0;
    public static final int POLLFLICKR_JOB_ID = 1;

    public static final String ACTION_SHOW_NOTIFICATION = "com.bignerdranch.android.flickrphotogallery.SHOW_NOTIFICATION";

    public static final String PERMISSION_PRIVATE = "com.bignerdranch.android.flickrphotogallery.permission.PROTECTED_RECEIVER";

    public static final String REQUEST_CODE = "REQUEST_CODE";

    public static final String NOTIFICATION = "NOTIFICATION";



    private HandleActionAsync mTaskAsync;

    /**
     * Override this method with the callback logic for your job. Any such logic needs to be
     * performed on a separate thread, as this function is executed on your application's main
     * thread.
     *
     * @param params Parameters specifying info about this job, including the extras bundle you
     *               optionally provided at job-creation time.
     * @return True if your service needs to process the work (on a separate thread). False if
     * there's no more work to be done for this job.
     */
    @Override
    public boolean onStartJob(final JobParameters params) {





        Log.d(TAG, "onStartJob: FLICKR POLL JOB STARTED");

        mTaskAsync = new HandleActionAsync(this, params);

        mTaskAsync.execute(params);


        return true;
    }

    /**
     * This method is called if the system has determined that you must stop execution of your job
     * even before you've had a chance to call {@link #jobFinished(JobParameters, boolean)}.
     * <p>
     * <p>This will happen if the requirements specified at schedule time are no longer met. For
     * example you may have requested WiFi with
     * {@link JobInfo.Builder#setRequiredNetworkType(int)}, yet while your
     * job was executing the user toggled WiFi. Another example is if you had specified
     * {@link JobInfo.Builder#setRequiresDeviceIdle(boolean)}, and the phone left its
     * idle maintenance window. You are solely responsible for the behaviour of your application
     * upon receipt of this message; your app will likely start to misbehave if you ignore it. One
     * immediate repercussion is that the system will cease holding a wakelock for you.</p>
     *
     * @param params Parameters specifying info about this job.
     * @return True to indicate to the JobManager whether you'd like to reschedule this job based
     * on the retry criteria provided at job creation-time. False to drop the job. Regardless of
     * the value returned, your job must stop executing.
     */
    @Override
    public boolean onStopJob(final JobParameters params) {
        Log.d(TAG, "onStopJob: FLICKR JOB STOPPED");

        if (mTaskAsync == null) {
            return false;
        }

        boolean retval=false;
        switch (mTaskAsync.getStatus()) {
            case PENDING:
            case RUNNING:
                mTaskAsync.cancel(true);
                retval= true;
                break;

            case FINISHED:
                mTaskAsync.cancel(true);
                break;
        }
        return retval;
    }


    private static class HandleActionAsync extends AsyncTask<JobParameters, Integer, Boolean> {


        private final JobParameters mJobParams;
        WeakReference<PollFlickrJobService> mOuter;

        public HandleActionAsync(final PollFlickrJobService outer, final JobParameters params) {
            mOuter = new WeakReference<>(outer);
            mJobParams = params;

        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param jobParameters The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Boolean doInBackground(final JobParameters... jobParameters) {
            return handleActionPoll();
        }

        @Override
        protected void onPostExecute(final Boolean needsResched) {
            Log.d(TAG, "onPostExecute: JOB FINISHED needsResched? " + needsResched);
            mOuter.get().jobFinished(mJobParams, !needsResched);

        }


        private boolean handleActionPoll() {
            Log.d(TAG, "handleActionPoll: handling action POLL inside JOB SERVICE");

            if (mOuter.get() == null) {
                Log.d(TAG, "handleActionPoll: service killed--return false to reschedule job");

                return true; // returning true indicates that we need to be reshed'ed
            }

            String lastQuery = QueryPreferences.getStoredQuery(mOuter.get());
            String lastID = QueryPreferences.getLastResultID(mOuter.get());


            List<GalleryItemEntity> items;
            if (lastQuery == null) {
                items = new FlickrFetcher().fetchRecentPhotos();
            }else{
                items = new FlickrFetcher().fetchSearchPhotos(lastQuery);
            }


            if (items.size() == 0) {
                return false;
            }

            String resultID = items.get(0).getID();

            if (resultID.equals(lastID)) {
                Log.i(TAG, "handleActionPoll: JOB got old result ID " + resultID);
            } else {
                Log.i(TAG, "handleActionPoll: JOB got new result ID "
                        + resultID);

                Resources resources = mOuter.get().getResources();

                Intent intent = PhotoGalleryActivity.newIntentFromService(mOuter.get());

                PendingIntent pendingIntent = PendingIntent.getActivity(mOuter.get(),
                        0, intent, 0);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(mOuter.get(), PhotoGalleryActivity.CHANNEL_ID);
                builder.setSmallIcon(R.drawable.ic_android_black_24dp)
                        .setTicker(resources.getString(R.string.new_pictures_title))
                        .setContentTitle(resources.getString(R.string.new_pictures_title))
                        .setContentText(resources.getString(R.string.new_pictures_content_text))
//                    .addAction(R.drawable.ic_android_black_24dp,
//                            getString(R.string.new_pictures_title), pendingIntent)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .build();


                showbackgroundNotification(NOTIFICATION_ID, builder.build());


            }


            QueryPreferences.setLastResultID(mOuter.get(), resultID);

            return false;

        }

        private void showbackgroundNotification(final int request_code, final Notification notification) {
            Intent intent = new Intent(ACTION_SHOW_NOTIFICATION);
            intent.putExtra(REQUEST_CODE, request_code);
            intent.putExtra(NOTIFICATION, notification);
            mOuter.get().sendOrderedBroadcast(intent, PERMISSION_PRIVATE, null, null, Activity.RESULT_OK, null, null);
        }



    }



    public static boolean isJobScheduled(Context ctx) {


        JobScheduler jobScheduler = (JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        boolean isScheduled = false;

        for (JobInfo jobinfo : jobScheduler.getAllPendingJobs()) {
            if (jobinfo.getId() == POLLFLICKR_JOB_ID) {
                isScheduled = true;
                break;
            }
        }

        Log.d(TAG, "isJobScheduled: JOB job scheduled? " + isScheduled);

        return isScheduled;

    }

    public static void togglePollJob(Context ctx,boolean shouldStart){

        JobScheduler jobScheduler = (JobScheduler) ctx.getSystemService(Activity.JOB_SCHEDULER_SERVICE);

        if (shouldStart) {




            Log.d(TAG, "onOptionsItemSelected: JOB STARTING JOB FOR ID: " + PollFlickrJobService.POLLFLICKR_JOB_ID);



            JobInfo jobInfo = new JobInfo.Builder(PollFlickrJobService.POLLFLICKR_JOB_ID,
                    new ComponentName(ctx, PollFlickrJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPeriodic(TimeUnit.MINUTES.toMillis(15))
                    .setPersisted(true)
                    .build();

            jobScheduler.schedule(jobInfo);


        } else {
            Log.d(TAG, "onOptionsItemSelected: JOB CANCELLING JOB FOR ID: " + PollFlickrJobService.POLLFLICKR_JOB_ID);
            jobScheduler.cancel(PollFlickrJobService.POLLFLICKR_JOB_ID);

        }

        QueryPreferences.setAlarmOn(ctx, shouldStart);

    }



}
