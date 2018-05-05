package ru.romananchugov.yandexschoolanchugov.adapters;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;

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
import ru.romananchugov.yandexschoolanchugov.utils.GalleryClickListener;
import ru.romananchugov.yandexschoolanchugov.utils.GalleryLongClickListener;

/**
 * Created by romananchugov on 11.04.2018.
 *
 * Адаптер для списка всех фотографи в GalleryListFragment
 */

public class GalleryListAdapter extends RecyclerView.Adapter<GalleryListAdapter.ViewHolder>{
    public static final String TAG = GalleryListAdapter.class.getSimpleName();

    private SparseArrayCompat<Call<DownloadLink>> callsMap;//список всех вызовов для загрузки фоток
    private ImageView imageView;
    private List<GalleryItem> galleryItems;
    private MainActivity activity;

    public GalleryListAdapter(MainActivity activity, List<GalleryItem> galleryItems){
        this.activity = activity;
        this.galleryItems = galleryItems;
        callsMap = new SparseArrayCompat<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        imageView = (ImageView) LayoutInflater.from(activity)
                .inflate(R.layout.gallery_item_view, parent, false);

        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Drawable placeHolder = activity.getResources().getDrawable(R.drawable.preload_placeholder);
        holder.bindDrawable(placeHolder);

        holder.imageView.setOnClickListener(
                new GalleryClickListener(position, galleryItems, activity)
        );

        holder.imageView.setOnLongClickListener(
                new GalleryLongClickListener(galleryItems.get(position), activity)
        );
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        if(holder.getAdapterPosition() >= 0) {
            int position = holder.getAdapterPosition();

            //останавливаем загрузку, когда view уже не видно
            if(callsMap.get(position) != null) {
                callsMap.get(position).cancel();
                callsMap.remove(position);
                if(galleryItems.get(position).getDownloadLink().equals("")) {
                    galleryItems.get(position).setDownloadLink(null);
                }
            }
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        if(holder.getAdapterPosition() >= 0){
            int position = holder.getAdapterPosition();
            //если у нас есть ссылка для загруки фотографии
            if (galleryItems.get(position).getDownloadLink() != null) {
                glideLoading(position, holder);
            } else{//если нет, то получаем ссылку для загрузки
                galleryItems.get(position).setDownloadLink("");
                firstLoad(galleryItems.get(position), position, holder);
            }
        }
    }

    @Override
    public int getItemCount() {
        return galleryItems.size();
    }

    //первичная загрузка, получает ссылку для скачивания фотографии
    private void firstLoad(final GalleryItem item, final int position, final GalleryListAdapter.ViewHolder holder) {
        final Retrofit retrofit = activity.getRetrofit();
        DiskClientApi clientApi = retrofit.create(DiskClientApi.class);
        final Call<DownloadLink> call = clientApi.getDownloadFileLink("OAuth " + activity.getToken(), item.getPath());

        try {
            call.enqueue(new Callback<DownloadLink>() {
                @Override
                public void onResponse(Call<DownloadLink> call, Response<DownloadLink> response) {
                    if(response.body() != null && response.body().getHref() != null) {
                        item.setDownloadLink(response.body().getHref());
                        glideLoading(position, holder);
                        call.cancel();
                    }
                }

                @Override
                public void onFailure(Call<DownloadLink> call, Throwable t) {
                    call.cancel();
                }
            });
        }catch (OutOfMemoryError e){
            e.printStackTrace();
        }

        callsMap.put(position, call);
    }

    //при имеющейся ссылки для скачивания, загружаем картинку при помощи Glide
    //по идее нужно загружать миниатюры, но ни глайд, ни ретрофит не хотят грузить картинки
    //с сылки для previewImage
    private void glideLoading(int position, GalleryListAdapter.ViewHolder holder) {
        if(galleryItems.size() > position) {
            Glide
                    .with(activity)
                    .load(galleryItems.get(position).getDownloadLink())
                    .apply(new RequestOptions()
                            .error(R.drawable.ic_refresh)
                            .placeholder(R.drawable.image_placeholder)
                            .priority(Priority.NORMAL)
                            .timeout(60000)
                    )
                    .into(holder.imageView).getRequest();
        }
    }

    //остановка загрузки всех ссылок
    public void stopLoading(){
        for(int i = 0; i < callsMap.size(); i++){
            if(callsMap.get(i) != null) {
                callsMap.get(i).cancel();
            }
        }
        callsMap.clear();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            this.imageView = (ImageView) itemView;
        }

        void bindDrawable(Drawable drawable) {
            imageView.setImageDrawable(drawable);
        }
    }

    //нельзя выделять, пока происходит загрузка галереи из-за обновления recyclera
    public void notifyAdapterDataSetChanged(){
        if(activity.isSelectionMode()) {
            Toast.makeText(activity.getApplicationContext(), R.string.gallery_load_wait, Toast.LENGTH_SHORT).show();
            activity.cancelSelectionMode();
        }
        notifyDataSetChanged();
    }

}
