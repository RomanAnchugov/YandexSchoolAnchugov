package ru.romananchugov.yandexschoolanchugov.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

import java.util.ArrayList;
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

import static ru.romananchugov.yandexschoolanchugov.activities.MainActivity.TOKEN;

/**
 * Created by romananchugov on 11.04.2018.
 */

public class SliderAdapter extends PagerAdapter {
    public static final String TAG = SliderAdapter.class.getSimpleName();

    private List<GalleryItem> galleryItems;
    private Activity activity;

    public SliderAdapter(Activity activity, List<GalleryItem> galleryItems){
        this.galleryItems = new ArrayList<>(galleryItems);
        this.activity = activity;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.slider_item, container, false);
        ImageView imageView = v.findViewById(R.id.slider_item_image_view);

        GalleryItem item = galleryItems.get(position);
        if(item.getDownloadLink() != null) {
            glideLoading(item, imageView, position);
        }else{
            firstLoad(item, position, imageView);
        }

        container.addView(v);

        return v;
    }

    @Override
    public int getCount() {
        return galleryItems.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((View) object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    private void glideLoading(GalleryItem item, ImageView imageView, int position) {
        Log.i(TAG, "glideLoading: loadign for postion " + position + "|url - " + item.getDownloadLink());

        DrawableCrossFadeFactory.Builder builder = new DrawableCrossFadeFactory.Builder();
        builder.setCrossFadeEnabled(false);
        DrawableCrossFadeFactory fadeFactory = builder.build();
        Glide
                .with(activity)
                .load(item.getDownloadLink())
                .apply(new RequestOptions()
                        .error(R.drawable.ic_error_placeholder)
                        .placeholder(R.drawable.ic_slider_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.IMMEDIATE))
                .into(imageView);
    }

    private void firstLoad(final GalleryItem item, final int position, final ImageView imageView) {

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
                glideLoading(item, imageView, position);
            }

            @Override
            public void onFailure(Call<DownloadLink> call, Throwable t) {

            }
        });
    }
}
