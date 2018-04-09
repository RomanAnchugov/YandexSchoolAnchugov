package ru.photogallery.romananchugov.photogallery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.List;

/**
 * Created by romananchugov on 27.12.2017.
 */

public class PhotoGalleryFragment extends VisibleFragment{

    private RecyclerView mPhotoRecyclerView;
    private ProgressBar progressBar;
    public static final String TAG = "PhotoGalleryFragment";

    private List<GalleryItem> mItems;

    private ThumbnailDownloader<PhotoHolder> mThumbnailDownLoader;

    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        progressBar =(ProgressBar) getActivity().findViewById(R.id.loading_progress_bar);

        updateItems();

        Handler responseHandler = new Handler();
        mThumbnailDownLoader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownLoader.setThumbnailDownloaderListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                target.bindDrawable(drawable);
            }
        });
        mThumbnailDownLoader.start();
        mThumbnailDownLoader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d(TAG, "QueryTextSubmit: " + s);
                //clear recycler
                mPhotoRecyclerView.setAdapter(null);

                QueryPreferences.setStoredQuery(getActivity(), s);
                FlickrFetchr.PAGE = 0;
                updateItems();
                //close keyboard
                Activity activity = getActivity();
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                View view = activity.getCurrentFocus();
                if (view == null) {
                    view = new View(activity);
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                //close search view
                searchView.onActionViewCollapsed();
                MenuItem item = menu.findItem(R.id.menu_item_toggle_polling);
                ;

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG, "QueryTextChange: " + s);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });


        MenuItem item = menu.findItem(R.id.menu_item_toggle_polling);
        if(PollService.isServiceAlarmOn(getActivity())){
            item.setTitle(R.string.stop_polling);
        }else{
            item.setTitle(R.string.start_polling);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm =
                        !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems(){
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mPhotoRecyclerView = v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mPhotoRecyclerView.addOnScrollListener(new RecyclerEndListener());
        setupAdapter();
        return v;
    }

    private void setupAdapter(){
        if(isAdded()){
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownLoader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>>{

        private String mQuery;

        public FetchItemsTask(String query){
            mQuery = query;
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
            if(mQuery == null){
                return new FlickrFetchr().fetchRecentPhotos();
            }else {
                return new FlickrFetchr().searchPhotos(mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            Log.i(TAG, "onPostExecute: flickr page: " + FlickrFetchr.PAGE);
            if(mItems == null){
                mItems = items;
                setupAdapter();
            }else {
                mItems.addAll(items);
            }


            if(FlickrFetchr.PAGE == 1) {
                mPhotoRecyclerView.getAdapter().notifyDataSetChanged();
            }else{
                mPhotoRecyclerView.getAdapter().notifyDataSetChanged();
            }

        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView mItemImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(View itemView){
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
            itemView.setOnClickListener(this);
        }

        public void bindDrawable(Drawable drawable){
            mItemImageView.setImageDrawable(drawable);
        }

        public void bindGalleryItem(GalleryItem galleryItem){
            mGalleryItem = galleryItem;
        }

        @Override
        public void onClick(View view) {
            Intent i = PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoPageUri());
            startActivity(i);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{
        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> items){
            mGalleryItems = items;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            holder.bindGalleryItem(galleryItem);
            Drawable placeHolder = getResources().getDrawable(android.R.drawable.ic_menu_report_image);
            holder.bindDrawable(placeHolder);
            mThumbnailDownLoader.queueThumbnail(holder, galleryItem.getmUri());
        }

        @Override
        public int getItemCount() {
            if(mGalleryItems != null) {
                return mGalleryItems.size();
            }else{
                return 0;
            }
        }
    }

    private class RecyclerEndListener extends RecyclerView.OnScrollListener{
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager()  ;
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int pastVisibleItems = layoutManager.findFirstCompletelyVisibleItemPosition();

            if(pastVisibleItems+visibleItemCount >= totalItemCount){
                FlickrFetchr.PAGE++;
                new FetchItemsTask(QueryPreferences.getStoredQuery(getActivity())).execute();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownLoader.clearQueue();
    }
}
