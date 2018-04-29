package ru.romananchugov.yandexschoolanchugov.utils;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.activities.MainActivity;
import ru.romananchugov.yandexschoolanchugov.fragments.SliderDialogFragment;
import ru.romananchugov.yandexschoolanchugov.models.GalleryItem;

import static ru.romananchugov.yandexschoolanchugov.utils.Constants.SLIDER_FRAGMENT_TAG;
import static ru.romananchugov.yandexschoolanchugov.utils.Constants.SLIDER_IMAGES_POSITION_KEY;

/**
 * Created by romananchugov on 10.04.2018.
 */

public class GalleryClickListener implements View.OnClickListener {

    private static final String TAG = GalleryClickListener.class.getSimpleName();
    private final List<GalleryItem> galleryItems;
    private int position;
    private MainActivity activity;

    public GalleryClickListener(int position, List<GalleryItem> galleryItems, MainActivity activity) {
        this.position = position;
        this.galleryItems = galleryItems;
        this.activity = activity;
    }

    @Override
    public void onClick(View view) {
        Bundle bundle = new Bundle();
        bundle.putInt(SLIDER_IMAGES_POSITION_KEY, position);

        ImageView imageView = (ImageView) view;

        if (!activity.isSelectionMode()) {
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            SliderDialogFragment slider = SliderDialogFragment.newInstance(galleryItems);
            slider.setArguments(bundle);
            try {
                slider.show(ft, SLIDER_FRAGMENT_TAG);
            }catch (IllegalStateException e){
                Toast.makeText(activity.getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        } else{
            toggleSelection(imageView);
        }
    }

    public void toggleSelection(ImageView imageView){
        if(activity.getSelectedViews().contains(imageView)){
            activity.removeViewFromSelected(imageView, galleryItems.get(position));
        }else{
            activity.addViewInSelected(imageView, galleryItems.get(position));
        }
    }



}
