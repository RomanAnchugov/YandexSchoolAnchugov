package ru.romananchugov.yandexschoolanchugov.adapters;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.interfaces.DiskClientApi;
import ru.romananchugov.yandexschoolanchugov.models.DownloadLink;
import ru.romananchugov.yandexschoolanchugov.models.GalleryItem;
import ru.romananchugov.yandexschoolanchugov.utils.GalleryClickListener;

import static ru.romananchugov.yandexschoolanchugov.activities.MainActivity.TOKEN;

/**
 * Created by romananchugov on 11.04.2018.
 */

public class GalleryListAdapter extends RecyclerView.Adapter<GalleryListAdapter.ViewHolder>{

    private ImageView imageView;
    private Fragment fragment;
    private List<GalleryItem> galleryItems;

    public GalleryListAdapter(Fragment fragment, List<GalleryItem> galleryItems){
        this.fragment = fragment;
        this.galleryItems = galleryItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        imageView = (ImageView) LayoutInflater.from(fragment.getContext())
                .inflate(R.layout.gallery_item_view, parent, false);


        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Drawable placeHolder = fragment.getActivity().getResources().getDrawable(android.R.drawable.ic_menu_report_image);
        holder.bindDrawable(placeHolder);

        holder.imageView.setOnClickListener(
                new GalleryClickListener(position, galleryItems, fragment)
        );

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

    public void firstLoad(final GalleryItem item, final int position, final GalleryListAdapter.ViewHolder holder) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(fragment.getActivity());
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
    public void glideLoading(int position, GalleryListAdapter.ViewHolder holder) {
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
