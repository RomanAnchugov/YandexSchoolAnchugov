package ru.romananchugov.yandexschoolanchugov;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yandex.disk.rest.Credentials;

import java.util.List;

import ru.romananchugov.yandexschoolanchugov.service.GalleryItem;

/**
 * Created by romananchugov on 07.04.2018.
 */

public class GalleryListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<GalleryItem>> {

    private static final String TAG = "GalleryListFragment";

    private Credentials credentials;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String username = preferences.getString(MainActivity.USERNAME, null);
        String token = preferences.getString(MainActivity.TOKEN, null);

        credentials = new Credentials(username, token);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gallery_list_fragment, container, false);
        getLoaderManager().initLoader(0, null, this);
        return v;
    }


    @Override
    public Loader<List<GalleryItem>> onCreateLoader(int i, Bundle bundle) {
        Log.i(TAG, "onCreateLoader: loader created");
        return new GalleryLoader(getActivity(), credentials);
    }

    @Override
    public void onLoadFinished(Loader<List<GalleryItem>> loader, List<GalleryItem> galleryItemList) {
        Log.i(TAG, "onLoadFinished: load finished");
        for(GalleryItem item:galleryItemList){
            Log.i(TAG, "onLoadFinished: item " + item.toString());
        }
    }

    @Override
    public void onLoaderReset(Loader<List<GalleryItem>> loader) {

    }
}
