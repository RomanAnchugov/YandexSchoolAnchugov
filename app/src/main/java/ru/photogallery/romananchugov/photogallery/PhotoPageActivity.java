package ru.photogallery.romananchugov.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

/**
 * Created by romananchugov on 14.01.2018.
 */

public class PhotoPageActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context, Uri photoPageUri){
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }
    @Override
    protected Fragment createFragment() {
        return PhotoPageFragment.newIntance(getIntent().getData());
    }



}
