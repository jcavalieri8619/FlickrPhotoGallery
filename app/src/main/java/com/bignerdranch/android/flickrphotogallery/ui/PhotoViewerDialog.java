package com.bignerdranch.android.flickrphotogallery.ui;


import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bignerdranch.android.flickrphotogallery.R;
import com.bignerdranch.android.flickrphotogallery.data.PictureUtils;

import java.io.ByteArrayOutputStream;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PhotoViewerDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhotoViewerDialog extends DialogFragment {
    private static final String ARG_PHOTOBYTES = "param1";

    private byte[] mBitmapBytes;

    private ImageView mImageView;

    public PhotoViewerDialog() {
        // Required empty public constructor
    }


    public static DialogFragment newInstance(Bitmap bitmap) {
        DialogFragment fragment = new PhotoViewerDialog();
        Bundle args = new Bundle();

        byte[] byteArray = bitmapToBytes(bitmap);



        args.putByteArray(ARG_PHOTOBYTES,byteArray );
        fragment.setArguments(args);
        return fragment;
    }

    private static byte[] bitmapToBytes(final Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }


    private Bitmap fetchAndScaleImage(byte[] bitmapBytes) {
        Bitmap bitmap = PictureUtils.getScaledBitmap(bitmapBytes, getActivity());

        return bitmap;
    }


    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        if (getArguments() != null) {
            mBitmapBytes = getArguments().getByteArray(ARG_PHOTOBYTES);
        }

        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_photo_viewer, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(v).setPositiveButton("Close", null);

        AlertDialog dialog = builder.create();

        mImageView = v.findViewById(R.id.photoviewer_image);

        mImageView.setImageBitmap(fetchAndScaleImage(mBitmapBytes));

        return dialog;
    }
}
