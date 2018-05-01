package ru.romananchugov.yandexschoolanchugov.fragments;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import ru.romananchugov.yandexschoolanchugov.network.GalleryItemsLoader;

import static ru.romananchugov.yandexschoolanchugov.activities.MainActivity.TOKEN;
import static ru.romananchugov.yandexschoolanchugov.utils.Constants.ADD_PHOTO_DIALOG_TAG;

/**
 * Created by romananchugov on 07.04.2018.
 */

@SuppressLint("ValidFragment")
public class GalleryListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<GalleryItem>>
        ,SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = GalleryListAdapter.class.getSimpleName();

    private String title;

    private Credentials credentials;

    private SwipeRefreshLayout refresher;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private GalleryListAdapter adapter;
    private List<GalleryItem> galleryItems;
    private MainActivity activity;


    private GalleryListFragment(MainActivity activity){
        this.activity = activity;
        this.title = activity.getResources().getString(R.string.app_name);
    }
    public static GalleryListFragment newInstance(MainActivity activity){
        GalleryListFragment f = new GalleryListFragment(activity);
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
        adapter = new GalleryListAdapter(activity, galleryItems);

        refresher = v.findViewById(R.id.list_fragment_refresher);
        refresher.setColorSchemeResources(
                android.R.color.black,
                R.color.blue);

        refresher.setOnRefreshListener(this);

        fab = v.findViewById(R.id.add_new_image_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToAddPhotoFragment();
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3){
            @Override
            public boolean canScrollVertically() {
                return !activity.isSelectionMode();
            }
        };
        recyclerView.setLayoutManager(gridLayoutManager);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle(title);
    }

    @Override
    public Loader<List<GalleryItem>> onCreateLoader(int i, Bundle bundle) {
        return new GalleryItemsLoader(getActivity(), credentials);
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
        adapter.stopLoading();
        getLoaderManager().restartLoader(0, null, this);
        activity.cancelSelectionMode();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refresher.setRefreshing(false);
            }
        }, 1500);
    }

    public void goToAddPhotoFragment(){
        activity.cancelSelectionMode();
        AddNewPhotoDialog
                .newInstance(activity, getString(R.string.add_new_photo))
                .show(activity.getSupportFragmentManager(), ADD_PHOTO_DIALOG_TAG);
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

        galleryItems.addAll(galleryItemList);
        adapter.notifyAdapterDataSetChanged();
    }

    public void removeItem(GalleryItem item){
        galleryItems.remove(item);
        adapter.notifyAdapterDataSetChanged();
    }
}
