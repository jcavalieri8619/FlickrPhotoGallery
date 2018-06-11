package com.bignerdranch.android.flickrphotogallery;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PhotoGalleryActivity extends SingleFragmentActivity {


    @Override
    protected Fragment createFragment() {
        String param1 = "";
        String param2 = "";
        return PhotoGalleryFragment.newInstance(param1, param2);
    }
}
