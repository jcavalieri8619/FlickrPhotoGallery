package com.bignerdranch.android.flickrphotogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetcher {
    private static final String TAG = "FlickrFetcher";


    private static final String API_KEY = "156d82fd9e142d5482f873c8d85f71ce";

    private static final String API_ENDPOINT = "https://api.flickr.com/services/rest/";

    private static final String API_FORMAT = "json";

    private static final String[] API_JSONP_FORMAT = {"nojsoncallback", "1"};


    private static final String API_METHOD_GETRECENT = "flickr.photos.getRecent";

    //TODO apparently getPopular is for a particular USER -- so use getRecent
    private static final String API_METHOD_GETPOPULAR = "flickr.photos.getPopular";
    private static final String[] API_METHOD_ARG_PERPAGE = {"per_page", "100"};
    private static final ThreadLocal<String[]> API_METHOD_ARG_WHATPAGE = new ThreadLocal<String[]>() {
        /**
         * Returns the current thread's "initial value" for this
         * thread-local variable.  This method will be invoked the first
         * time a thread accesses the variable with the {@link #get}
         * method, unless the thread previously invoked the {@link #set}
         * method, in which case the {@code initialValue} method will not
         * be invoked for the thread.  Normally, this method is invoked at
         * most once per thread, but it may be invoked again in case of
         * subsequent invocations of {@link #remove} followed by {@link #get}.
         * <p>
         * <p>This implementation simply returns {@code null}; if the
         * programmer desires thread-local variables to have an initial
         * value other than {@code null}, {@code ThreadLocal} must be
         * subclassed, and this method overridden.  Typically, an
         * anonymous inner class will be used.
         *
         * @return the initial value for this thread-local
         */
        @Override
        protected String[] initialValue() {

            return new String[]{"page", "1"};
        }
    };

//    static {
//        API_METHOD_ARG_WHATPAGE.set(new String[]{"page", "1"});
//
//    }


    private static final int MAX_PAGES = 3;


    private static final int ARG_INDEX = 0;
    private static final int VAL_INDEX = 1;


    public List<GalleryItemEntity> fetchItems() {
        List<GalleryItemEntity> items = new ArrayList<>();

        if (Integer.parseInt(API_METHOD_ARG_WHATPAGE.get()[VAL_INDEX]) > MAX_PAGES) {
            return items;
        }

        try {
            String url = Uri.parse(API_ENDPOINT)
                    .buildUpon()
                    .appendQueryParameter("method", API_METHOD_GETRECENT)
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", API_FORMAT)
                    .appendQueryParameter(API_JSONP_FORMAT[ARG_INDEX],API_JSONP_FORMAT[VAL_INDEX])
                    .appendQueryParameter(API_METHOD_ARG_PERPAGE[ARG_INDEX],API_METHOD_ARG_PERPAGE[VAL_INDEX])
                    .appendQueryParameter("extras", "url_s")
                    .appendQueryParameter(API_METHOD_ARG_WHATPAGE.get()[ARG_INDEX],API_METHOD_ARG_WHATPAGE.get()[VAL_INDEX])
                    .build().toString();

            Log.d(TAG, "fetchItems: URL: " + url);

            String jsonString = URLfetcher.getURLString(url);

            Log.d(TAG, "fetchItems: received json: " + jsonString);

            JSONObject jsonBody = new JSONObject(jsonString);


            parseItems(items, jsonBody);


        } catch (IOException e) {
            Log.e(TAG, "fetchItems: failed  to getURLString", e);

        } catch (JSONException e) {
            Log.e(TAG, "fetchItems: failed to parse JSONObect", e);


        }

        incrementPageIndex();
        return items;
    }

    private void incrementPageIndex() {
        int nextPage = Integer.parseInt(API_METHOD_ARG_WHATPAGE.get()[VAL_INDEX]) + 1;
        API_METHOD_ARG_WHATPAGE.get()[VAL_INDEX] = String.valueOf(nextPage);
        Log.d(TAG, "incrementPageIndex: incremented page index to: " + nextPage);
    }


    private void parseItems(List<GalleryItemEntity> items, JSONObject body) throws JSONException {
        JSONObject photosObject = body.getJSONObject("photos");

        JSONArray photoArray = photosObject.getJSONArray("photo");

        for (int i = 0; i < photoArray.length(); i++) {
            JSONObject photo = (JSONObject) photoArray.get(i);

            GalleryItemEntity item = new GalleryItemEntity();

            item.setID(photo.getString("id"));
            item.setCaption(photo.getString("title"));

            if (photo.has("url_s")) {
                item.setUrl(photo.getString("url_s"));

            }

            items.add(item);

        }
    }


}
