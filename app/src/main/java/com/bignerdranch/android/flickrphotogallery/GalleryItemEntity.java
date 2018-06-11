package com.bignerdranch.android.flickrphotogallery;

import com.bignerdranch.android.flickrphotogallery.models.GalleryItem;

public class GalleryItemEntity implements GalleryItem {


    private String mID;
    private String mCaption;
    private String mUrl;

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
