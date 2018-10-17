package com.bignerdranch.android.flickrphotogallery.ui;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;


import android.arch.lifecycle.ViewModelProvider;
import android.databinding.ObservableBoolean;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.bignerdranch.android.flickrphotogallery.data.FlickrFetcher;
import com.bignerdranch.android.flickrphotogallery.data.GalleryItemEntity;
import com.bignerdranch.android.flickrphotogallery.data.ThumbnailDownloader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class PhotoGalleryViewModel extends ViewModel {
    private static final String TAG = "PhotoGalleryViewModel";

    private MutableLiveData<List<GalleryItemEntity>> mListGalleryItems = new MutableLiveData<>();

    private List<GalleryItemEntity> mCachedListGalleryItems = null;

    public ObservableBoolean isLoadingJSON;
    private ThumbnailDownloader<? extends RecyclerView.ViewHolder> mThumbnailDownloader;
    private int mPhotoIndex = 0;


    public enum FlickrMethod {SEARCH, RECENTS, INIT}

    private FlickrMethod mCurrentMethod;

    private String mCurrentQuery;
    private int mCurrentPage;


    public PhotoGalleryViewModel(String storedQuery) {

        // initially get fetch most recent photos to populate display

        mCurrentPage = 1;


        isLoadingJSON = new ObservableBoolean();

        if (mCachedListGalleryItems == null || mCachedListGalleryItems.isEmpty()) {

            Log.d(TAG, "PhotoGalleryViewModel: JPC galleryItems null --> spinning up FlickrFetcher");

            mCurrentMethod = FlickrMethod.INIT;
            if (storedQuery == null) {
                mCurrentQuery = null;
                fetchNextPageOfRecentPhotos();
            } else {
                mCurrentQuery = storedQuery;
                fetchNextPageOfSearchPhotos(storedQuery);

            }

        } else {
            Log.d(TAG, "PhotoGalleryViewModel: JPC using cached galleryItems from config change");
            mListGalleryItems.setValue(mCachedListGalleryItems);
            isLoadingJSON.set(false);

        }







    }

    @Override
    protected void onCleared() {
        super.onCleared();

//        tearDownThumbnailDownloader();

    }

    public void setCurrentPhotoIndex(int index) {
        mPhotoIndex = index;
    }

    public int getCurrentPhotoIndex() {

        return mPhotoIndex;
    }

    public void setCurrentMethod(final FlickrMethod currentMethod) {
        mCurrentMethod = currentMethod;
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }


    public FlickrMethod getCurrentMethod() {
        return mCurrentMethod;
    }

    public MutableLiveData<List<GalleryItemEntity>> getGalleryItemsList() {
        return mListGalleryItems;
    }

    public void cacheFetchedGalleryItemsOnReconfig(List<GalleryItemEntity> items) {
        mCachedListGalleryItems = new ArrayList<>(items);

    }

    public void fetchNextPageOfRecentPhotos() {
        if (mCurrentMethod == FlickrMethod.SEARCH || mCurrentMethod==FlickrMethod.INIT) {
            mCurrentPage = 1;
            mCurrentMethod = FlickrMethod.RECENTS;
        } else {
            mCurrentPage += 1;
        }

        new FetchItemTask(this).execute();

    }

    public ThumbnailDownloader<? extends RecyclerView.ViewHolder> setupThumbnailDownloader(ThumbnailDownloader.ThumbnailDownloadListener listener) {

        if (mThumbnailDownloader != null && mThumbnailDownloader.isAlive()) {
            return mThumbnailDownloader;
        }


        Handler responseHandler = new Handler(Looper.getMainLooper());


        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);

        mThumbnailDownloader.setThumbnailDownloadListener(listener);

        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();

        return mThumbnailDownloader;
    }



    public void resetQueueThumbnailDownloader() {
        mThumbnailDownloader.clearQueue();

    }

    public void tearDownThumbnailDownloader() {


        mThumbnailDownloader.quit();
    }

    public void fetchNextPageOfSearchPhotos( String... params) {
        if ( mCurrentMethod == FlickrMethod.RECENTS || mCurrentMethod==FlickrMethod.INIT) {
            mCurrentPage = 1;
            mCurrentMethod = FlickrMethod.SEARCH;
            mCurrentQuery = params[0];
        } else {
            mCurrentPage += 1;
        }

        new FetchItemTask(this).execute(mCurrentQuery);

    }

    public static class PhotoViewModelFactory implements ViewModelProvider.Factory {

        String lastQuery;

        public PhotoViewModelFactory(final String lastQuery) {
            this.lastQuery = lastQuery;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {
            if (modelClass.isAssignableFrom(PhotoGalleryViewModel.class)) {

                return (T) new PhotoGalleryViewModel(lastQuery);
            }

            throw new ClassCastException("Unable to build PhotoGalleryViewModel");

        }
    }



    private static class FetchItemTask extends AsyncTask<String, Void, List<GalleryItemEntity>> {

        private static final String TAG_AT = "FetchItemTask";
        private final WeakReference<PhotoGalleryViewModel> outer;
        FetchItemTask(PhotoGalleryViewModel outer) {
            this.outer = new WeakReference<>(outer);

            this.outer.get().isLoadingJSON.set(true);

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
            if (outer.get() != null) {
                outer.get().mListGalleryItems.setValue(galleryItems);

                outer.get().isLoadingJSON.set(false);

            }else{
                Log.d(TAG, "onPostExecute: " +
                        "viewModel no longer exists --> Activity/Fragment no longer exist");

            }

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
