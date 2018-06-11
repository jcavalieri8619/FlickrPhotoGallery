package com.bignerdranch.android.flickrphotogallery;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;

public class PhotoGalleryViewModel extends ViewModel {
    private static final String TAG = "PhotoGalleryViewModel";

    MutableLiveData<List<GalleryItemEntity>> mListGalleryItems = new MutableLiveData<>();


    public PhotoGalleryViewModel() {

        new FetchItemTask(this).execute();
    }

    @Override
    protected void onCleared() {
        super.onCleared();

    }


    public MutableLiveData<List<GalleryItemEntity>> getGalleryItemsList() {
        return mListGalleryItems;
    }

    public void fetchNextPageOfGalleryItems() {
        new FetchItemTask(this).execute();

    }


    private static class FetchItemTask extends AsyncTask<Void, Void, List<GalleryItemEntity>> {

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
         * @param voids The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected List<GalleryItemEntity> doInBackground(final Void... voids) {
            return new FlickrFetcher().fetchItems();


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
