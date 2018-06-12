package com.bignerdranch.android.flickrphotogallery;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;

public class PhotoGalleryViewModel extends ViewModel {
    private static final String TAG = "PhotoGalleryViewModel";

    MutableLiveData<List<GalleryItemEntity>> mListGalleryItems = new MutableLiveData<>();


    public enum FlickrMethod {SEARCH, RECENTS}

    FlickrMethod mCurrentMethod;

    String mCurrentQuery;
    int mCurrentPage;


    public PhotoGalleryViewModel() {

        // initially get fetch most recent photos to populate display

        mCurrentMethod = FlickrMethod.RECENTS;
        mCurrentPage = 1;
        mCurrentQuery = null;
        new FetchItemTask(this).execute();

    }

    @Override
    protected void onCleared() {
        super.onCleared();

    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public String getCurrentQuery() {
        return mCurrentQuery;
    }

    public FlickrMethod getCurrentMethod() {
        return mCurrentMethod;
    }

    public MutableLiveData<List<GalleryItemEntity>> getGalleryItemsList() {
        return mListGalleryItems;
    }

    public void fetchNextPageOfRecentPhotos() {
        if (mCurrentMethod == FlickrMethod.SEARCH) {
            mCurrentPage = 1;
            mCurrentMethod = FlickrMethod.RECENTS;
        } else {
            mCurrentPage += 1;
        }

        new FetchItemTask(this).execute();

    }

    public void fetchNextPageOfSearchPhotos(String... params) {
        if (mCurrentMethod == FlickrMethod.RECENTS) {
            mCurrentPage = 1;
            mCurrentMethod = FlickrMethod.SEARCH;
            mCurrentQuery = params[0];
        } else {
            mCurrentPage += 1;
        }

        new FetchItemTask(this).execute(mCurrentQuery);

    }


    private static class FetchItemTask extends AsyncTask<String, Void, List<GalleryItemEntity>> {

        private static final String TAG_AT = "FetchItemTask";
        private final WeakReference<PhotoGalleryViewModel> outer;
        FetchItemTask(PhotoGalleryViewModel outer) {
            this.outer = new WeakReference<>(outer);

        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * aram voids The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected List<GalleryItemEntity> doInBackground(final String... params) {

            if (outer.get() == null) {
                // probably should'nt happen but if the viewModel is destroyed
                // then the activity/fragment must have been finished so just return null here
                return null;
            }

            if (params == null || params.length == 0) {
                Log.d(TAG, "doInBackground: fetching recent");

                return new FlickrFetcher(outer.get().getCurrentPage()).fetchRecentPhotos();

            } else {

                Log.d(TAG, "doInBackground: searching photos for query:  " + params[0]);

                return new FlickrFetcher(outer.get().getCurrentPage()).fetchSearchPhotos(params[0]);

            }



        }

        @Override
        protected void onPostExecute(final List<GalleryItemEntity> galleryItems) {
            outer.get().mListGalleryItems.setValue(galleryItems);


//            List<GalleryItemEntity> items = outer.get().mListGalleryItems.getValue();
//            if (items == null) {
//
//                Log.d(TAG + " : " + TAG_AT, "onPostExecute: items == null");
//                outer.get().mListGalleryItems.setValue(galleryItems);
//
//            } else {
//                Log.d(TAG + " : " + TAG_AT, "onPostExecute: items size: " + items.size());
//
//                items.addAll(galleryItems);
//
//                outer.get().mListGalleryItems.setValue(items);
//            }


        }
    }
}
