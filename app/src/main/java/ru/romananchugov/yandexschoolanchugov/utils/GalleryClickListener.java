package ru.romananchugov.yandexschoolanchugov.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.Serializable;
import java.util.List;

import ru.romananchugov.yandexschoolanchugov.R;
import ru.romananchugov.yandexschoolanchugov.fragmetns.SliderDialogFragment;
import ru.romananchugov.yandexschoolanchugov.models.GalleryItem;

/**
 * Created by romananchugov on 10.04.2018.
 */

public class GalleryClickListener implements View.OnClickListener {

    private static final String TAG = "GalleryClickListener";

    private int position;
    private final List<GalleryItem> galleryItems;
    private Fragment fragment;

    public GalleryClickListener(int position, List<GalleryItem> galleryItems, Fragment fragment) {
        this.position = position;
        this.galleryItems = galleryItems;
        this.fragment = fragment;
    }

    @Override
    public void onClick(View view) {
        Bundle bundle = new Bundle();
        bundle.putInt(Keys.SLIDER_IMAGES_POSITION, position);
        bundle.putSerializable(Keys.SLIDER_IMAGES_ARRAY, (Serializable) galleryItems);

        ImageView imageView = (ImageView) view;

        if(imageView.getDrawable().equals(R.drawable.ic_refresh_black_24dp)){
            Log.i(TAG, "onClick: refresh click");

        }else {
            FragmentTransaction ft = fragment.getActivity().getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            SliderDialogFragment slider = SliderDialogFragment.newInstance();
            slider.setArguments(bundle);
            slider.show(ft, "slider");
        }

    }
}
