package ru.romananchugov.yandexschoolanchugov.fragmetns;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.activities.MainActivity;
import ru.romananchugov.yandexschoolanchugov.adapters.GalleryListAdapter;
import ru.romananchugov.yandexschoolanchugov.models.GalleryItem;
import ru.romananchugov.yandexschoolanchugov.network.Credentials;
import ru.romananchugov.yandexschoolanchugov.network.GalleryLoader;

import static ru.romananchugov.yandexschoolanchugov.activities.MainActivity.TOKEN;

/**
 * Created by romananchugov on 07.04.2018.
 */

@SuppressLint("ValidFragment")
public class GalleryListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<GalleryItem>>
        ,SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "GalleryListFragment";

    private Credentials credentials;

    private SwipeRefreshLayout refresher;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<GalleryItem> galleryItems;


    private GalleryListFragment(){}
    public static GalleryListFragment newInstance(){
        GalleryListFragment f = new GalleryListFragment();
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String username = preferences.getString(MainActivity.USERNAME, null);
        String token = preferences.getString(TOKEN, null);

        credentials = new Credentials(username, token);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gallery_list_fragment, container, false);

        galleryItems = new ArrayList<>();
        recyclerView = v.findViewById(R.id.gallery_recycler_view);
        adapter = new GalleryListAdapter(this, galleryItems);

        refresher = v.findViewById(R.id.list_fragment_refresher);
        refresher.setColorSchemeResources(
                android.R.color.black,
                R.color.blue);

        refresher.setOnRefreshListener(this);

        fab = v.findViewById(R.id.add_new_image_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNewPhotoFragment();
            }
        });


        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener( new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    fab.hide();
                } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                }
            }
        });

        getLoaderManager().initLoader(0, null, this);
        return v;
    }


    @Override
    public Loader<List<GalleryItem>> onCreateLoader(int i, Bundle bundle) {
        return new GalleryLoader(getActivity(), credentials);
    }

    @Override
    public void onLoadFinished(Loader<List<GalleryItem>> loader, List<GalleryItem> galleryItemList) {
        setData(galleryItemList);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<GalleryItem>> loader) {

    }

    @Override
    public void onRefresh() {
        getLoaderManager().restartLoader(0, null, this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refresher.setRefreshing(false);
            }
        }, 1500);
    }

    public void goToNewPhotoFragment(){
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        AddNewPhotoFragment addNewPhotoFragment = AddNewPhotoFragment.newInstance(credentials);
        ft.replace(android.R.id.content, addNewPhotoFragment).addToBackStack(null);
        ft.commit();
    }

    public void setData(List<GalleryItem> galleryItemList) {
        galleryItems.clear();

        for (int i = 0; i < galleryItemList.size(); i++) {
            GalleryItem item = galleryItemList.get(i);
            if (!(item.getMime().equals("image/png") || item.getMime().equals("image/jpeg"))) {
                galleryItemList.remove(item);
                i--;
            }
        }

        if (galleryItemList != null) {
            galleryItems.addAll(galleryItemList);
        }


        adapter.notifyDataSetChanged();
    }
}
