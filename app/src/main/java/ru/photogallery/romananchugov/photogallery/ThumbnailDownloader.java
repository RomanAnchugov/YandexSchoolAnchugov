package ru.photogallery.romananchugov.photogallery;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by romananchugov on 29.12.2017.
 */

public class ThumbnailDownloader<T> extends HandlerThread {
    public static final String TAG = "ThumbnailThread";
    public static final int MESSAGE_DOWNLOAD = 0;

    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponceHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloaderListener;

    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }
    public void setThumbnailDownloaderListener(ThumbnailDownloadListener<T> listener){
        mThumbnailDownloaderListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponceHandler = responseHandler;
    }

    public void queueThumbnail(T target, String url){
        Log.i(TAG, "Got a URL:" + url);

        if(url == null){
            mRequestMap.remove(target);
        }else{
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    public void clearQueue(){
        mResponceHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == MESSAGE_DOWNLOAD){
                    T target = (T) msg.obj;
                    Log.i(TAG, "Gor a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    private void handleRequest(final T target){
        try{
            final String url = mRequestMap.get(target);
            if(url == null){
                return;
            }
            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");

            mResponceHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mRequestMap.get(target) != url){
                        return;
                    }

                    mRequestMap.remove(target);
                    mThumbnailDownloaderListener.onThumbnailDownloaded(target, bitmap);
                }
            });
        }catch (IOException ioe){
            Log.e(TAG, "Error downloading image", ioe);
        }
    }
}
