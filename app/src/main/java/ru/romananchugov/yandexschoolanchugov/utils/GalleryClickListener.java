package ru.romananchugov.yandexschoolanchugov.utils;

import android.util.Log;
import android.view.View;

/**
 * Created by romananchugov on 10.04.2018.
 */

public class GalleryClickListener implements View.OnClickListener {

    private static final String TAG = "GalleryClickListener";

    private int position;

    public GalleryClickListener(int position) {
        this.position = position;
    }

    @Override
    public void onClick(View view) {
        Log.i(TAG, "onClick: click on position " + position);
    }
}
