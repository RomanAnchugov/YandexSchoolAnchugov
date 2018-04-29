package ru.romananchugov.yandexschoolanchugov.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.activities.MainActivity;
import ru.romananchugov.yandexschoolanchugov.models.DownloadLink;
import ru.romananchugov.yandexschoolanchugov.models.GalleryItem;
import ru.romananchugov.yandexschoolanchugov.network.DiskClientApi;

/**
 * Created by romananchugov on 11.04.2018.
 */

public class SliderAdapter extends PagerAdapter {
    public static final String TAG = SliderAdapter.class.getSimpleName();
    private static final int ANIMATION_DURATION = 300;

    private List<GalleryItem> galleryItems;
    private RelativeLayout infoContainer;
    private MainActivity activity;
    private boolean infoVisible;
    private Animation fadeIn;
    private Animation fadeOut;


    public SliderAdapter(MainActivity activity, List<GalleryItem> galleryItems, RelativeLayout infoContainer){
        this.galleryItems = new ArrayList<>(galleryItems);
        this.infoContainer = infoContainer;
        this.activity = activity;
        infoVisible = true;

        fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(ANIMATION_DURATION);

        fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(ANIMATION_DURATION);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.slider_item, container, false);
        PhotoView imageView = v.findViewById(R.id.slider_item_image_view);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleInfoVisibility();
            }
        });

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
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    private void glideLoading(GalleryItem item, ImageView imageView, int position) {
        DrawableCrossFadeFactory.Builder builder = new DrawableCrossFadeFactory.Builder();
        builder.setCrossFadeEnabled(false);
        Glide
                .with(activity)
                .load(item.getDownloadLink())
                .thumbnail(.5f)
                .apply(new RequestOptions()
                        .error(R.drawable.ic_error_placeholder)
                        .placeholder(R.drawable.ic_slider_placeholder)
                        .priority(Priority.IMMEDIATE)
                        .timeout(60000))
                .into(imageView);
    }

    private void firstLoad(final GalleryItem item, final int position, final ImageView imageView) {
        final Retrofit retrofit = activity.getRetrofit();

        DiskClientApi clientApi = retrofit.create(DiskClientApi.class);
        final Call<DownloadLink> call = clientApi.getDownloadFileLink("OAuth " + activity.getToken(), item.getPath());

        call.enqueue(new Callback<DownloadLink>() {
            @Override
            public void onResponse(Call<DownloadLink> call, Response<DownloadLink> response) {
                item.setDownloadLink(response.body().getHref());
                glideLoading(item, imageView, position);
                call.cancel();
            }

            @Override
            public void onFailure(Call<DownloadLink> call, Throwable t) {
                call.cancel();
            }
        });
    }

    public void toggleInfoVisibility(){
        if(infoVisible){
            infoContainer.startAnimation(fadeOut);
            infoContainer.setVisibility(View.GONE);
            infoVisible = false;
        }else{
            infoContainer.startAnimation(fadeIn);
            infoContainer.setVisibility(View.VISIBLE);
            infoVisible = true;
        }
    }
}
