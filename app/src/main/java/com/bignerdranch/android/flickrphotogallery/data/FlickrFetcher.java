package com.bignerdranch.android.flickrphotogallery.data;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlickrFetcher {
    private static final String TAG = "FlickrFetcher";


    private static final String API_KEY = "156d82fd9e142d5482f873c8d85f71ce";

    private static final String API_ENDPOINT = "https://api.flickr.com/services/rest/";

    private static final String API_FORMAT = "json";

    private static final String[] API_JSONP_FORMAT = {"nojsoncallback", "1"};


    private static final String API_METHOD_GETRECENT = "flickr.photos.getRecent";

    private static final String API_METHOD_SEARCH = "flickr.photos.search";

    private static final String[] API_METHOD_SEARCHARG = {"sort","interestingness-desc"};


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




    private static final int MAX_PAGES = 3;


    private static final int ARG_INDEX = 0;
    private static final int VAL_INDEX = 1;
    private static final Uri URL_ENDPOINT = Uri.parse(API_ENDPOINT)
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", API_FORMAT)
            .appendQueryParameter(API_JSONP_FORMAT[ARG_INDEX], API_JSONP_FORMAT[VAL_INDEX])
            .appendQueryParameter("extras", "url_s")
            .build();


    public FlickrFetcher(int whatpage) {
        setPageIndex(whatpage);

    }

    public FlickrFetcher() {
        setPageIndex(1);

    }

    private String buildURL(String method, String query) {
        Uri.Builder builder = URL_ENDPOINT
                .buildUpon()
                .appendQueryParameter("method", method);

        if (method.equals(API_METHOD_SEARCH)) {
            builder.appendQueryParameter("text", query)
                    .appendQueryParameter(API_METHOD_SEARCHARG[ARG_INDEX],API_METHOD_SEARCHARG[VAL_INDEX]);
        }

        return builder.build().toString();
    }


    public List<GalleryItemEntity> fetchRecentPhotos() {
        String url = buildURL(API_METHOD_GETRECENT, null);

        return downloadGalleryItems(url);
    }

    public List<GalleryItemEntity> fetchSearchPhotos(String query) {
        String url = buildURL(API_METHOD_SEARCH, query);

        return downloadGalleryItems(url);
    }

    private List<GalleryItemEntity> downloadGalleryItems(String url) {

        List<GalleryItemEntity> items = new ArrayList<>();

        if (Integer.parseInt(API_METHOD_ARG_WHATPAGE.get()[VAL_INDEX]) > MAX_PAGES) {
            return items;
        }


        try {

            url = addPageArgs(url);


            Log.d(TAG, "downloadGalleryItems: URL: " + url);

            String jsonString = URLfetcher.getURLString(url);

            Log.d(TAG, "downloadGalleryItems: received json: " + jsonString);

            JSONObject jsonBody = new JSONObject(jsonString);


            parseItems(items, jsonString);


        } catch (IOException e) {
            Log.e(TAG, "downloadGalleryItems: failed  to getURLString", e);

        } catch (JSONException e) {
            Log.e(TAG, "downloadGalleryItems: failed to parse JSONObect", e);


        }


        return items;
    }

    private String addPageArgs(String url) {
        return  Uri.parse(url)
                .buildUpon()
                .appendQueryParameter(API_METHOD_ARG_PERPAGE[ARG_INDEX], API_METHOD_ARG_PERPAGE[VAL_INDEX])
                .appendQueryParameter(API_METHOD_ARG_WHATPAGE.get()[ARG_INDEX], API_METHOD_ARG_WHATPAGE.get()[VAL_INDEX])
                .build().toString();

    }

    private void setPageIndex(int whatpage) {
        API_METHOD_ARG_WHATPAGE.get()[VAL_INDEX] = String.valueOf(whatpage);
        Log.d(TAG, "setPageIndex: incremented page index to: " + whatpage);
    }



    //POJOs to deserialize JSON via GSON
    private static class PhotosObject {


        @Expose
        PhotosResult photos;

        private static class PhotosResult{


            @Expose
            int page;

            @Expose
            int pages;

            @Expose
            int perpage;

            @Expose
            int total;

            @Expose
            @SerializedName("photo")
            GalleryItemEntity[] photo_array = new GalleryItemEntity[]{};



        }
    }



    private void parseItems(List<GalleryItemEntity> items, String body) throws JSONException {




        Log.d(TAG, "parseItems: PARSE ITEMS FOR JSON: " + body);



        GsonBuilder builder = new GsonBuilder();



        Gson gson = builder.serializeNulls()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting().create();



        PhotosObject photos = gson.fromJson(body, PhotosObject.class);
        PhotosObject.PhotosResult result = photos.photos;

        Log.d(TAG, "parseItems: result.pages " + result.pages);

        items.addAll(Arrays.asList(result.photo_array));



//        JSONObject photosObject = body.getJSONObject("photos");
//
//        JSONArray photoArray = photosObject.getJSONArray("photo");
//
//        for (int i = 0; i < photoArray.length(); i++) {
//            JSONObject photo = (JSONObject) photoArray.get(i);
//
//            GalleryItemEntity item = new GalleryItemEntity();
//
//            item.setID(photo.getString("id"));
//            item.setCaption(photo.getString("title"));
//
//            if (photo.has("url_s")) {
//                item.setUrl(photo.getString("url_s"));
//
//            }
//
//            items.add(item);
//
//        }


    }





}
