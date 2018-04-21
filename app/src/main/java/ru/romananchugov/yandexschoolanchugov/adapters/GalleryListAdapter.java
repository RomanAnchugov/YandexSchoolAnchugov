package ru.romananchugov.yandexschoolanchugov.adapters;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;

import java.util.HashMap;
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
import ru.romananchugov.yandexschoolanchugov.utils.GalleryClickListener;

import static ru.romananchugov.yandexschoolanchugov.activities.MainActivity.TOKEN;

/**
 * Created by romananchugov on 11.04.2018.
 */

public class GalleryListAdapter extends RecyclerView.Adapter<GalleryListAdapter.ViewHolder> implements View.OnLongClickListener{
    public static final String TAG = GalleryListAdapter.class.getSimpleName();

    private HashMap<Integer, Call<DownloadLink>> callsMap;
    private HashMap<Integer, Request> glidesMap;
    private ImageView imageView;
    //private Fragment fragment;
    private List<GalleryItem> galleryItems;
    private MainActivity activity;

    public GalleryListAdapter(MainActivity activity, List<GalleryItem> galleryItems){
        this.activity = activity;
        this.galleryItems = galleryItems;
        callsMap = new HashMap<>();
        glidesMap = new HashMap<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        imageView = (ImageView) LayoutInflater.from(activity)
                .inflate(R.layout.gallery_item_view, parent, false);

        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Drawable placeHolder = activity.getResources().getDrawable(R.drawable.ic_preload_placeholder);
        holder.bindDrawable(placeHolder);

        holder.imageView.setOnClickListener(
                new GalleryClickListener(position, galleryItems, activity)
        );

        holder.imageView.setOnLongClickListener(this);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        if(holder.getAdapterPosition() >= 0) {
            Log.i(TAG, "onViewDetachedFromWindow: for postion" + holder.getAdapterPosition());

            int position = holder.getAdapterPosition();

            if(callsMap.get(position) != null) {
                callsMap.get(position).cancel();
                callsMap.remove(position);
                if(galleryItems.get(position).getDownloadLink().equals("")) {
                    galleryItems.get(position).setDownloadLink(null);
                }
            }
            if(glidesMap.get(position) != null && glidesMap.get(position).isRunning()) {
                glidesMap.get(position).pause();
            }
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        Log.i(TAG, "onViewAttachedToWindow: view attach to window " + holder.getAdapterPosition());
        if(holder.getAdapterPosition() >= 0){
            int position = holder.getAdapterPosition();
            if (galleryItems.get(position).getDownloadLink() != null) {
                //if(glidesMap.containsKey(position) && glidesMap.get(position).isPaused()) glidesMap.get(position).recycle();
                glideLoading(position, holder);
            } else {
                galleryItems.get(position).setDownloadLink("");
                firstLoad(galleryItems.get(position), position, holder);
            }
        }
    }

    @Override
    public int getItemCount() {
        return galleryItems.size();
    }

    @Override
    public boolean onLongClick(View view) {
        if(!activity.isSelectionMode()) {
            activity.setSelectionMode(true);
            activity.addViewInSelected((ImageView) view);
            activity.updateToolbar();
        }
        return true;
    }

    public void firstLoad(final GalleryItem item, final int position, final GalleryListAdapter.ViewHolder holder) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
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

        callsMap.put(position, call);
    }

    public void glideLoading(int position, GalleryListAdapter.ViewHolder holder) {
        Request request = Glide
                .with(activity)
                .load(galleryItems.get(position).getDownloadLink())
                .thumbnail(.5f)
                .apply(new RequestOptions()
                        .error(R.drawable.ic_refresh_black_24dp)
                        .placeholder(R.drawable.image_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.NORMAL)
                        .timeout(60000)
                        )
                .into(holder.imageView).getRequest();

        glidesMap.put(position, request);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = (ImageView) itemView;
        }

        public void bindDrawable(Drawable drawable) {
            imageView.setImageDrawable(drawable);
        }
    }

}
