package com.bignerdranch.android.flickrphotogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ThumbnailDownloader<T> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";

    private static final int MESSAGE_DOWNLOAD = 0;

    private boolean mHasQuit = false;

    private Handler mResponseHandler;
    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;


    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);

        mResponseHandler = responseHandler;

    }


    public void setThumbnailDownloadListener(final ThumbnailDownloadListener<T> thumbnailDownloadListener) {

        mThumbnailDownloadListener = thumbnailDownloadListener;
    }

    @Override
    protected void onLooperPrepared() {

        mRequestHandler = new Handler(getLooper(), new Handler.Callback() {
            /**
             * when Looper dequeues a Message from its MessageQueue, it will hand that
             * message off to its target handler and this callback will be invoked;
             * this callback does some prep work then delegates to custom method handleRequest
             * @param msg
             * @return
             */
            @Override
            public boolean handleMessage(final Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;

                    Log.d(TAG, "handleMessage: got request for URL: " + mRequestMap.get(target));


                    // run each download task from thread in cached thread pool
                    // so that not all downloads are taking place on same
                    // thread associatd with this HandlerThread class;
                    // since handleRequest has reference to UI Looper, no need
                    // to call RX#observeOn(androidMainThread) because UI looper
                    // ensures all UI mods done on UI thread
                    Completable.fromAction(() -> handleRequest(target))
                            .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                            .subscribe(new CompletableObserver() {
                                @Override
                                public void onSubscribe(final Disposable d) {

                                }

                                @Override
                                public void onComplete() {

                                    Log.d(TAG, "onComplete: finished for some URL");
                                }

                                @Override
                                public void onError(final Throwable e) {
                                    Log.e(TAG, "onError: unable to fetch some URL", e);

                                }
                            });

                }


                return true;
            }
        });

    }

    public void queueThumbnail(T target, String url) {
        Log.d(TAG, "queueThumbnail: got URL: " + url);

        if (url == null) {
            mRequestMap.remove(target);

        } else {
            mRequestMap.put(target, url);

            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }


    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }


    private void handleRequest(final T target) {
        try {
            final String url = mRequestMap.get(target);
            if (url == null) {
                return;
            }


            byte[] bitmapBytes = URLfetcher.getURLBytes(url);


            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

            Log.d(TAG, "handleRequest: bitmap created");

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {

                    if ((mRequestMap.get(target) != null && !mRequestMap.get(target).equals(url)) || mHasQuit) {
                        return;
                    }

                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });


        } catch (IOException e) {
            Log.e(TAG, "handleRequest: failed to get URL bytes", e);
        }
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();

    }

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }


}

