package ru.romananchugov.yandexschoolanchugov.utils;

import android.view.View;
import android.widget.ImageView;

import ru.romananchugov.yandexschoolanchugov.activities.MainActivity;
import ru.romananchugov.yandexschoolanchugov.models.GalleryItem;

/**
 * Created by romananchugov on 21.04.2018.
 */

public class GalleryLongClickListener implements View.OnLongClickListener {

    private GalleryItem galleryItem;
    private MainActivity activity;

    public GalleryLongClickListener(GalleryItem galleryItem, MainActivity activity){
        this.galleryItem = galleryItem;
        this.activity = activity;
    }

    @Override
    public boolean onLongClick(View view) {
        if(!activity.isSelectionMode()) {
            activity.setSelectionMode(true);
            activity.addViewInSelected((ImageView) view, galleryItem);
            activity.updateToolbar();
        }
        return true;
    }
}
