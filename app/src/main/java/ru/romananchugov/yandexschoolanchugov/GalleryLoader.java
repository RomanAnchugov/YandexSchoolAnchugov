package ru.romananchugov.yandexschoolanchugov;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerIOException;

import java.io.IOException;
import java.util.List;

import ru.romananchugov.yandexschoolanchugov.service.GalleryItem;
import ru.romananchugov.yandexschoolanchugov.service.RestClientUtil;

/**
 * Created by romananchugov on 07.04.2018.
 */

public class GalleryLoader extends AsyncTaskLoader<List<GalleryItem>> {
    private static final String TAG = "GalleryLoader";

    private Credentials credentials;

    public GalleryLoader(Context context, Credentials credentials) {
        super(context);
        this.credentials = credentials;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<GalleryItem> loadInBackground() {
        RestClient client = null;
        client = RestClientUtil.getInstance(credentials);
        try {
            Log.i(TAG, "loadInBackground: " + client.getDiskInfo());
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "loadInBackground: " + e.getMessage());
        } catch (ServerIOException e) {
            e.printStackTrace();
            Log.i(TAG, "loadInBackground: " + e.getMessage());
        }
        return null;
    }
}
