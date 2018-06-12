package com.bignerdranch.android.flickrphotogallery;

import com.bignerdranch.android.flickrphotogallery.models.GalleryItem;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GalleryItemEntity implements GalleryItem {


    @Expose
    @SerializedName("id")
    private String mID;

    @Expose
    @SerializedName("title")
    private String mCaption;

    @Expose
    @SerializedName("url_s")
    private String mUrl;

    @Expose
    @SerializedName("height_s")
    private String mHeight;

    @Expose
    @SerializedName("width_s")
    private String mWidth;



    @Override
    public String getHeight() {
        return mHeight;
    }

    @Override
    public void setHeight(final String height) {

        mHeight = height;
    }

    @Override
    public void setWidth(String width) {

        mWidth = width;
    }

    @Override
    public String getWidth() {
        return mWidth;
    }

    @Override
    public String getID() {
        return mID;
    }

    @Override
    public void setID(final String ID) {

        mID = ID;
    }

    @Override
    public String getCaption() {
        return mCaption;
    }

    @Override
    public void setCaption(final String caption) {
        mCaption = caption;
    }

    @Override
    public String getUrl() {
        return mUrl;
    }

    @Override
    public void setUrl(final String url) {

        mUrl = url;
    }

    @Override
    public String toString() {
        return mCaption;

    }
}
