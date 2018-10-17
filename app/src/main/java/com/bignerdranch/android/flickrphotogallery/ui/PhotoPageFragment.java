package com.bignerdranch.android.flickrphotogallery.ui;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.bignerdranch.android.flickrphotogallery.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PhotoPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhotoPageFragment extends VisibleFragment {
    public static final String TAG = "PhotoPageFragment";

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_URI_PARAM = "URIparam";


    private Uri mURIParam;


    private ProgressBar mProgressBar;
    private WebView mWebView;


    public PhotoPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * param param1 Parameter 1.
     * param param2 Parameter 2.
     * @return A new instance of fragment PhotoPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PhotoPageFragment newInstance(Uri param) {
        PhotoPageFragment fragment = new PhotoPageFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI_PARAM, param);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(null);



    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mURIParam = (Uri) getArguments().getParcelable(ARG_URI_PARAM);
        }
    }

    public boolean canGoBackOnWebView() {
        return mWebView.canGoBack();
    }

    public boolean goBackOnWebView() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_photo_page, container, false);

        mProgressBar = root.findViewById(R.id.progress_bar);

        mProgressBar.setMax(100);

        mWebView = root.findViewById(R.id.web_view);


        setupWebView();


        mWebView.loadUrl(mURIParam.toString());

        return root;
    }

    private void setupWebView() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mWebView.setWebChromeClient(new WebChromeClient() {




            @Override
            public void onReceivedTitle(final WebView view, final String title) {
                if (getActivity() != null) {

                    ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(title);

                }

            }

            @Override
            public void onProgressChanged(final WebView view, final int newProgress) {
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);

                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);

                }

            }
        });

        mWebView.setWebViewClient(new WebViewClient());
    }

}
