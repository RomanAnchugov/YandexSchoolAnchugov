package ru.romananchugov.yandexschoolanchugov.fragmetns;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import ru.romananchugov.yandexschoolanchugov.R;

import static ru.romananchugov.yandexschoolanchugov.utils.Constants.PICK_IMAGE;

/**
 * Created by romananchugov on 13.04.2018.
 */

@SuppressLint("ValidFragment")
public class AddNewPhotoFragment extends Fragment {
    private static final String TAG = AddNewPhotoFragment.class.getSimpleName();

    private Activity activity;

    private LinearLayout linearLayout;


    private AddNewPhotoFragment(Activity activity){
        this.activity = activity;
    }

    @NonNull
    public static AddNewPhotoFragment newInstance(Activity activity){
        return new AddNewPhotoFragment(activity);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_new_photo_fragment, container, false);

        linearLayout = v.findViewById(R.id.upload_image_ll);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseFile();
            }
        });

        return v;
    }

    public void chooseFile(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }
}
