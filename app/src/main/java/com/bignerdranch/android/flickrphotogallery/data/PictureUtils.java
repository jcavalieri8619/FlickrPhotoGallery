package com.bignerdranch.android.flickrphotogallery.data;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;

public class PictureUtils {

    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;


        BitmapFactory.decodeFile(path, options);

        int inSampleSize = computeSampleSize(destWidth, destHeight, options);

        options = new BitmapFactory.Options();


        options.inSampleSize = inSampleSize;

        Bitmap scaleBitmap = BitmapFactory.decodeFile(path, options);


        Matrix rotate90 = new Matrix();
        rotate90.postRotate(90);


        return Bitmap.createBitmap(scaleBitmap, 0, 0, scaleBitmap.getWidth(), scaleBitmap.getHeight(), rotate90, false);


    }

    public static Bitmap getScaledBitmap(byte[] bitMapBytes, int destWidth, int destHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;


        BitmapFactory.decodeByteArray(bitMapBytes,0,bitMapBytes.length, options);

        int inSampleSize = computeSampleSize(destWidth, destHeight, options);

        options = new BitmapFactory.Options();


        options.inSampleSize = inSampleSize;

        Bitmap scaleBitmap = BitmapFactory.decodeByteArray(bitMapBytes,0,bitMapBytes.length, options);


        Matrix rotate90 = new Matrix();
//        rotate90.postRotate(90);


        return Bitmap.createBitmap(scaleBitmap, 0, 0, scaleBitmap.getWidth(), scaleBitmap.getHeight(), rotate90, false);


    }

    private static int computeSampleSize(final int destWidth, final int destHeight, final BitmapFactory.Options options) {
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        int inSampleSize = 1;

        if (srcHeight > destHeight || srcWidth > destWidth) {
            float heightScale = srcHeight / destHeight;

            float widthScale = srcWidth / destWidth;

            inSampleSize = Math.round(heightScale > widthScale ? heightScale : widthScale);

        }
        return inSampleSize;
    }


    public static Bitmap getScaledBitmap(byte[] bitmapBytes, Activity activity) {
        Point size = new Point();

        activity.getWindowManager().getDefaultDisplay().getSize(size);

        return getScaledBitmap(bitmapBytes, size.x ,size.y);
    }

    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();

        activity.getWindowManager().getDefaultDisplay().getSize(size);

        return getScaledBitmap(path, 200, 800);
    }
}
