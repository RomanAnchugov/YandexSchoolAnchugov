package ru.romananchugov.yandexschoolanchugov;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yandex.disk.rest.Credentials;

import java.util.ArrayList;
import java.util.List;

import ru.romananchugov.yandexschoolanchugov.service.GalleryItem;

/**
 * Created by romananchugov on 07.04.2018.
 */

public class GalleryListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<GalleryItem>> {

    private static final String TAG = "GalleryListFragment";

    private Credentials credentials;



    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<GalleryItem> galleryItems;

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

        galleryItems = new ArrayList<>();
        recyclerView = v.findViewById(R.id.gallery_recycler_view);
        adapter  = new Adapter();

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(adapter);

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
        setData(galleryItemList);

    }

    @Override
    public void onLoaderReset(Loader<List<GalleryItem>> loader) {

    }

    public void setData(List<GalleryItem> galleryItemList){

        galleryItems.clear();

        if(galleryItemList != null){
            galleryItems.addAll(galleryItemList);
        }

        adapter.notifyDataSetChanged();
    }

    private class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder>{

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView imageView = (ImageView) LayoutInflater.from(getContext())
                    .inflate(R.layout.gallery_item_view, parent, false);

            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return galleryItems.size();
        }

    }


}
