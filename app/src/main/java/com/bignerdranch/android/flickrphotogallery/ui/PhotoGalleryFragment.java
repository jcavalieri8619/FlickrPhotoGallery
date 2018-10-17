package com.bignerdranch.android.flickrphotogallery.ui;


import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.List;


import com.bignerdranch.android.flickrphotogallery.R;
import com.bignerdranch.android.flickrphotogallery.data.GalleryItemEntity;
import com.bignerdranch.android.flickrphotogallery.QueryPreferences;
import com.bignerdranch.android.flickrphotogallery.data.ThumbnailDownloader;
import com.bignerdranch.android.flickrphotogallery.databinding.FragmentPhotoGalleryBinding;
import com.bignerdranch.android.flickrphotogallery.service.PollFlickrJobService;
import com.bignerdranch.android.flickrphotogallery.service.PollService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PhotoGalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhotoGalleryFragment extends VisibleFragment {

    private static final String TAG = "PhotoGalleryFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String FRAG_TRANS_GALLERY_TO_WEBVIEW = "galleryFragToPhotoFrag";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private RecyclerView mPhotoRecyclerView;
    private PhotoAdapter mAdapter;
    private PhotoGalleryViewModel mModelView;

    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private FragmentPhotoGalleryBinding binding;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }



        setHasOptionsMenu(true);


//        PhotoGalleryViewModel.PhotoViewModelFactory factory =
//                new PhotoGalleryViewModel.PhotoViewModelFactory(QueryPreferences.getStoredQuery(getActivity()));
//
//        mModelView = ViewModelProviders.of(getActivity(),factory).get(PhotoGalleryViewModel.class);
//
//
////        setupThumbnailDownloader();
//
//
//        ThumbnailDownloader.ThumbnailDownloadListener downloadListener = (ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>) (target, thumbnail) -> {
//            Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
//            ((PhotoHolder) target).bindDrawable(drawable,thumbnail);
//        };
//
//        mThumbnailDownloader = (ThumbnailDownloader<PhotoHolder>) mModelView.setupThumbnailDownloader(downloadListener);



    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PhotoGalleryViewModel.PhotoViewModelFactory factory =
                new PhotoGalleryViewModel.PhotoViewModelFactory(QueryPreferences.getStoredQuery(getActivity()));

        mModelView = ViewModelProviders.of(getActivity(),factory).get(PhotoGalleryViewModel.class);


        ThumbnailDownloader.ThumbnailDownloadListener downloadListener = (ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>) (target, thumbnail) -> {
            if (isAdded()) {

                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                ((PhotoHolder) target).bindDrawable(drawable,thumbnail);
            }


        };

        mThumbnailDownloader = (ThumbnailDownloader<PhotoHolder>) mModelView.setupThumbnailDownloader(downloadListener);

        binding.setIsJSONLoading(mModelView.isLoadingJSON);

        setupAdapter();

        setupRecyclerView(getView());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_photo_gallery, container, false);


        return binding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        mThumbnailDownloader.clearQueue();

        mModelView.resetQueueThumbnailDownloader();




    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!getActivity().isFinishing()) {

            Log.d(TAG, "onDestroy: JPC destroying PhotoGalleryFragment but not finishing-->caching items, size: " + mAdapter.mItems.size());



            mModelView.cacheFetchedGalleryItemsOnReconfig(mAdapter.mItems);

        }



//        mThumbnailDownloader.quit();

//        mModelView.tearDownThumbnailDownloader();
    }

    public PhotoGalleryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PhotoGalleryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PhotoGalleryFragment newInstance(String param1, String param2) {
        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } else {
            Log.d(TAG, "hideKeyboard: cannot hide keyboard -- inputManager == null");

        }


    }




    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

        inflater.inflate(R.menu.menu_photo_gallery, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.menu_item_search);

        SearchView searchView = (SearchView) searchMenuItem.getActionView();


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                Log.d(TAG, "onQueryTextSubmit: query submitted: " + query);


                QueryPreferences.setStoredQuery(getContext(), query);
                mAdapter.clearItems();
                mModelView.setCurrentMethod(PhotoGalleryViewModel.FlickrMethod.INIT);

                mModelView.fetchNextPageOfSearchPhotos(query);


                hideKeyboard(getActivity());

                //TODO android API is broke -- collapseActionView doesnt collapse the searchview
                //but the callback style does
                searchMenuItem.collapseActionView();
                searchView.onActionViewCollapsed();


                return true;
            }

            /**
             * Called when the query text is changed by the user.
             *
             * @param newText the new content of the query text field.
             * @return false if the SearchView should perform the default action of showing any
             * suggestions if available, true if the action was handled by the listener.
             */
            @Override
            public boolean onQueryTextChange(final String newText) {
                return false;
            }

        });

        searchView.setOnSearchClickListener(v ->
                searchView.setQuery(QueryPreferences.getStoredQuery(getContext()), false));


        MenuItem togglePolling = menu.findItem(R.id.menu_item_toggle_polling);

        boolean isServiceScheduled;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "onCreateOptionsMenu: JOB using JOBScheduler");

            isServiceScheduled = PollFlickrJobService.isJobScheduled(getActivity());
        }else{

            Log.d(TAG, "onCreateOptionsMenu: JOB USING INTENT SERVICE");

            isServiceScheduled = PollService.isServiceAlarmOn(getActivity());
        }


        if (isServiceScheduled) {

            togglePolling.setTitle(R.string.stop_polling);

        } else {
            togglePolling.setTitle(R.string.start_polling);

        }


    }



    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getContext(), null);
                mAdapter.clearItems();
                mModelView.setCurrentMethod(PhotoGalleryViewModel.FlickrMethod.INIT);

                mModelView.fetchNextPageOfRecentPhotos();
                return true;

            case R.id.menu_item_toggle_polling:

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {

                    boolean shouldStartPollJob = !PollFlickrJobService.isJobScheduled(getActivity());

                    PollFlickrJobService.togglePollJob(getActivity(), shouldStartPollJob);


                } else {

                    boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());


                    PollService.setServiceAlarm(getActivity(), shouldStartAlarm);


                }


                getActivity().invalidateOptionsMenu();


                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }


//    private void setupThumbnailDownloader() {
//        Handler responseHandler = new Handler(Looper.getMainLooper());
//
//
//        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
//
//
//        mThumbnailDownloader.setThumbnailDownloadListener(downloadListener);
//
//        mThumbnailDownloader.start();
//        mThumbnailDownloader.getLooper();
//    }

    private void setupAdapter() {


        mAdapter = new PhotoAdapter();

        mModelView.getGalleryItemsList().observe(PhotoGalleryFragment.this,
                (galleryItemEntities) -> mAdapter.submitItems(galleryItemEntities));


    }



    private void setupRecyclerView(final View v) {
        mPhotoRecyclerView = v.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mPhotoRecyclerView.setAdapter(mAdapter);
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {


            /**
             * Callback method to be invoked when the RecyclerView has been scrolled. This will be
             * called after the scroll has completed.
             * <p>
             * This callback will also be called if visible item range changes after a layout
             * calculation. In that case, dx and dy will be 0.
             *
             * @param recyclerView The RecyclerView which scrolled.
             * @param dx           The amount of horizontal scroll.
             * @param dy           The amount of vertical scroll.
             */
            @Override
            public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {

                if (!recyclerView.canScrollVertically(1)) {
                    if (mModelView.getCurrentMethod() == PhotoGalleryViewModel.FlickrMethod.RECENTS) {

                        mModelView.fetchNextPageOfRecentPhotos();

                    } else {
                        // since we are fetching next page of results for
                        // most recent search--then we pass null here to
                        // indicate to modelView to use its currentQuery field
                        mModelView.fetchNextPageOfSearchPhotos();

                    }
                }

            }
        });


        mPhotoRecyclerView.getLayoutManager().setItemPrefetchEnabled(true);

    }

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView mImageView;
        private Bitmap mBitmap;
        private GalleryItemEntity mItemEntity;

        PhotoHolder(final View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.item_image_view);
            itemView.setOnClickListener(this);


        }

        void bindGalleryItem(GalleryItemEntity galleryItem) {

            mItemEntity = galleryItem;
        }

        void bindDrawable(Drawable drawable, Bitmap bitmap) {


            mImageView.setImageDrawable(drawable);
            mBitmap = bitmap;

        }


        @Override
        public void onClick(final View v) {
//            openInDialogWindow();

//            openInBrowser();
            openInWebView();

        }


        private void openInWebView() {
            Fragment fragment = PhotoPageFragment.newInstance(mItemEntity.constructPhotoPageUri());


            //TODO understand why add(fragment) breaks this transaction while replace
            // works fine. If using add, the new fragment never even shows up
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment, PhotoPageFragment.TAG)
//                    .add(fragment, "webView")
                    .addToBackStack(FRAG_TRANS_GALLERY_TO_WEBVIEW)
                    .commit();
        }


        private void openInBrowser() {
            Intent intent = new Intent(Intent.ACTION_VIEW, mItemEntity.constructPhotoPageUri());
            startActivity(intent);

        }

        private void openInDialogWindow() {
            DialogFragment photoViewer = PhotoViewerDialog.newInstance(mBitmap);
            photoViewer.show(getFragmentManager(), "photoViewerDialog");
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{
        private List<GalleryItemEntity> mItems;
        private boolean mClearOnNextSubmit = false;


        /**
         * Called when RecyclerView needs a new {link ViewHolder} of the given type to represent
         * an item.
         * <p>
         * This new ViewHolder should be constructed with a new View that can represent the items
         * of the given type. You can either create a new View manually or inflate it from an XML
         * layout file.
         * <p>
         * The new ViewHolder will be used to display items of the adapter using
         * ink #onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to display
         * different items in the data set, it is a good idea to cache references to sub views of
         * the View to avoid unnecessary {@link View#findViewById(int)} calls.
         *
         * @param parent   The ViewGroup into which the new View will be added after it is bound to
         *                 an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         * @see #getItemViewType(int)
         * see #onBindViewHolder(ViewHolder, int)
         */
        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {

            return new PhotoHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.list_item_gallery, parent, false));
        }

        /**
         * Called by RecyclerView to display the data at the specified position. This method should
         * update the contents of the {link ViewHolder#itemView} to reflect the item at the given
         * position.
         * <p>
         * Note that unlike {@link ListView}, RecyclerView will not call this method
         * again if the position of the item changes in the data set unless the item itself is
         * invalidated or the new position cannot be determined. For this reason, you should only
         * use the <code>position</code> parameter while acquiring the related data item inside
         * this method and should not keep a copy of it. If you need the position of an item later
         * on (e.g. in a click listener), use link ViewHolder#getAdapterPosition()} which will
         * have the updated adapter position.
         * <p>
         * Override {link #onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
         * handle efficient partial bindDrawable.
         *
         * @param holder   The ViewHolder which should be updated to represent the contents of the
         *                 item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(@NonNull final PhotoHolder holder, final int position) {



            holder.bindGalleryItem(mItems.get(position));

            mThumbnailDownloader.queueThumbnail(holder, mItems.get(position).getUrl());

        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return mItems==null? 0: mItems.size();
        }

        void submitItems(List<GalleryItemEntity> items) {

            if (mItems == null || mClearOnNextSubmit) {

                mClearOnNextSubmit = false;

                mItems = items;

//                notifyItemRangeInserted(0, items.size());
                notifyDataSetChanged();

            } else {

                appendItems(items);


            }
        }

        private void appendItems(List<GalleryItemEntity> newItems) {
            int oldSize = mItems.size();
            mItems.addAll(newItems);
            notifyItemRangeInserted(oldSize, newItems.size());

        }

        public void clearItems() {
            mClearOnNextSubmit = true;
        }
    }





}
