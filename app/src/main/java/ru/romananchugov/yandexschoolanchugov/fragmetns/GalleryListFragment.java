package ru.romananchugov.yandexschoolanchugov.fragmetns;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.activities.MainActivity;
import ru.romananchugov.yandexschoolanchugov.interfaces.DiskClientApi;
import ru.romananchugov.yandexschoolanchugov.models.DownloadLink;
import ru.romananchugov.yandexschoolanchugov.models.GalleryItem;
import ru.romananchugov.yandexschoolanchugov.network.Credentials;
import ru.romananchugov.yandexschoolanchugov.network.GalleryLoader;
import ru.romananchugov.yandexschoolanchugov.utils.GalleryClickListener;

import static ru.romananchugov.yandexschoolanchugov.activities.MainActivity.TOKEN;

/**
 * Created by romananchugov on 07.04.2018.
 */

public class GalleryListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<GalleryItem>> {

    private static final String TAG = "GalleryListFragment";

    private Credentials credentials;

    private Fragment fragment;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<GalleryItem> galleryItems;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String username = preferences.getString(MainActivity.USERNAME, null);
        String token = preferences.getString(TOKEN, null);

        credentials = new Credentials(username, token);
        fragment = this;
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gallery_list_fragment, container, false);

        galleryItems = new ArrayList<>();
        recyclerView = v.findViewById(R.id.gallery_recycler_view);
        adapter = new Adapter();

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(adapter);

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
    public void onLoaderReset(Loader<List<GalleryItem>> loader) {

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

    public void firstLoad(final GalleryItem item, final int position, final Adapter.ViewHolder holder) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String token = preferences.getString(TOKEN, null);

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("https://cloud-api.yandex.net/v1/disk/")
                .addConverterFactory(GsonConverterFactory.create());

        final Retrofit retrofit = builder.build();

        DiskClientApi clientApi = retrofit.create(DiskClientApi.class);
        final Call<DownloadLink> call = clientApi.getDownloadFileLink("OAuth " + token, item.getPath());


        call.enqueue(new Callback<DownloadLink>() {
            @Override
            public void onResponse(Call<DownloadLink> call, Response<DownloadLink> response) {
                item.setDownloadLink(response.body().getHref());
                glideLoading(position, holder);
            }

            @Override
            public void onFailure(Call<DownloadLink> call, Throwable t) {

            }
        });
    }

    @SuppressLint("ResourceAsColor")
    public void glideLoading(int position, Adapter.ViewHolder holder) {
        Glide
                .with(fragment)
                .load(galleryItems.get(position).getDownloadLink())
                .thumbnail(0.5f)
                .apply(new RequestOptions()
                        .error(new ColorDrawable(Color.WHITE))
                        .placeholder(R.drawable.blue_drawable)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(holder.imageView);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private ImageView imageView;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            imageView = (ImageView) LayoutInflater.from(getContext())
                    .inflate(R.layout.gallery_item_view, parent, false);


            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            Log.i(TAG, "onBindViewHolder: bind position " + position);
            Drawable placeHolder = getResources().getDrawable(android.R.drawable.ic_menu_report_image);
            holder.bindDrawable(placeHolder);

            holder.imageView.setOnClickListener(new GalleryClickListener(position));


            if (galleryItems.get(position).getDownloadLink() != null) {
                glideLoading(position, holder);
            } else {
                firstLoad(galleryItems.get(position), position, holder);
            }

        }

        @Override
        public int getItemCount() {
            return galleryItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView imageView;

            public ViewHolder(View itemView) {
                super(itemView);
                this.imageView = (ImageView) itemView;
            }

            public void bindDrawable(Drawable drawable) {
                imageView.setImageDrawable(drawable);
            }
        }
    }

}
